package co.edu.uniquindio.adopcionmascota.core.data

import android.util.Log
import co.edu.uniquindio.adopcionmascota.features.feed.Comment
import co.edu.uniquindio.adopcionmascota.features.feed.LocationFilter
import co.edu.uniquindio.adopcionmascota.features.feed.Pet
import co.edu.uniquindio.adopcionmascota.features.feed.PetCategory
import co.edu.uniquindio.adopcionmascota.features.feed.PetType
import co.edu.uniquindio.adopcionmascota.features.feed.PostStatus
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import co.edu.uniquindio.adopcionmascota.core.util.await
object PetRepository {

    private val db get() = FirebaseFirestore.getInstance()

    private val _pets = MutableStateFlow<List<Pet>>(emptyList())
    val pets: StateFlow<List<Pet>> = _pets.asStateFlow()

    private var listener: ListenerRegistration? = null

    // Empieza a escuchar la colección "pets" en tiempo real
    fun start() {
        stop()
        listener = db.collection("pets").addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) {
                Log.e("PetRepository", "Error escuchando publicaciones", error)
                return@addSnapshotListener
            }
            val me = SessionManager.currentUser?.id
            _pets.value = snapshot.documents
                .mapNotNull { toPet(it, me) }
                .sortedByDescending { it.createdAt }
            ReputationManager.checkCurrentUser()
        }
    }

    fun stop() {
        listener?.remove()
        listener = null
        _pets.value = emptyList()
    }

    // ---------- Crear ----------
    fun add(pet: Pet) {
        val data = mapOf<String, Any?>(
            "name" to pet.name,
            "type" to pet.type.name,
            "breed" to pet.breed,
            "age" to pet.age,
            "gender" to pet.gender,
            "size" to pet.size,
            "city" to pet.city,
            "locationFilter" to pet.locationFilter.name,
            "description" to pet.description,
            "healthInfo" to pet.healthInfo,
            "shelterName" to pet.shelterName,
            "shelterContact" to pet.shelterContact,
            "colorHex" to pet.colorHex,
            "authorId" to pet.authorId,
            "status" to pet.status.name,
            "rejectionReason" to pet.rejectionReason,
            "category" to pet.category.name,
            "latitude" to pet.latitude,
            "longitude" to pet.longitude,
            "imageUrls" to pet.imageUrls,
            "createdAt" to System.currentTimeMillis(),
            "votedBy" to emptyList<String>(),
            "favoritedBy" to emptyList<String>(),
            "comments" to emptyList<Map<String, String>>()
        )
        db.collection("pets").document(pet.id).set(data)
    }

    // ---------- Actualizar: solo envía lo que cambió ----------
    fun update(id: String, transform: (Pet) -> Pet) {
        val old = _pets.value.find { it.id == id } ?: return
        val new = transform(old)
        val me = SessionManager.currentUser?.id
        val changes = mutableMapOf<String, Any?>()

        if (new.name != old.name) changes["name"] = new.name
        if (new.description != old.description) changes["description"] = new.description
        if (new.age != old.age) changes["age"] = new.age
        if (new.status != old.status) changes["status"] = new.status.name
        if (new.rejectionReason != old.rejectionReason) changes["rejectionReason"] = new.rejectionReason
        if (new.category != old.category) changes["category"] = new.category.name

        // Comentarios nuevos: se agregan sin pisar los de otros usuarios
        if (new.comments.size > old.comments.size) {
            val added = new.comments.drop(old.comments.size).map {
                mapOf(
                    "id" to it.id,
                    "authorName" to it.authorName,
                    "text" to it.text,
                    "authorId" to it.authorId
                )
            }
            changes["comments"] = FieldValue.arrayUnion(*added.toTypedArray())
        }

        // Votos y favoritos son por usuario
        if (me != null) {
            if (new.votedByMe != old.votedByMe) {
                changes["votedBy"] =
                    if (new.votedByMe) FieldValue.arrayUnion(me) else FieldValue.arrayRemove(me)
            }
            if (new.isFavorite != old.isFavorite) {
                changes["favoritedBy"] =
                    if (new.isFavorite) FieldValue.arrayUnion(me) else FieldValue.arrayRemove(me)
            }
        }

        if (changes.isNotEmpty()) {
            db.collection("pets").document(id).update(changes)
        }
    }

    fun delete(id: String) {
        db.collection("pets").document(id).delete()
    }

    suspend fun deleteByAuthor(authorId: String) {
        val snap = db.collection("pets").whereEqualTo("authorId", authorId).get().await()
        val batch = db.batch()
        snap.documents.forEach { batch.delete(it.reference) }
        batch.commit().await()
    }

    // ---------- Documento de Firestore -> Pet ----------
    private fun toPet(doc: DocumentSnapshot, me: String?): Pet? {
        return try {
            val votedBy = (doc.get("votedBy") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
            val favoritedBy = (doc.get("favoritedBy") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
            val comments = (doc.get("comments") as? List<*>)?.mapNotNull { c ->
                val m = c as? Map<*, *> ?: return@mapNotNull null
                Comment(
                    id = m["id"] as? String ?: "",
                    authorName = m["authorName"] as? String ?: "",
                    text = m["text"] as? String ?: "",
                    authorId = m["authorId"] as? String ?: ""
                )
            } ?: emptyList()

            Pet(
                id = doc.id,
                name = doc.getString("name") ?: "",
                type = enumOr(doc.getString("type"), PetType.OTHER),
                breed = doc.getString("breed") ?: "",
                age = doc.getString("age") ?: "",
                gender = doc.getString("gender") ?: "",
                size = doc.getString("size") ?: "",
                city = doc.getString("city") ?: "",
                locationFilter = enumOr(doc.getString("locationFilter"), LocationFilter.IN_CITY),
                description = doc.getString("description") ?: "",
                healthInfo = (doc.get("healthInfo") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                shelterName = doc.getString("shelterName") ?: "",
                shelterContact = doc.getString("shelterContact") ?: "",
                isFavorite = me != null && me in favoritedBy,
                colorHex = doc.getLong("colorHex") ?: 0xFF80CBC4,
                authorId = doc.getString("authorId") ?: "",
                status = enumOr(doc.getString("status"), PostStatus.PENDING),
                rejectionReason = doc.getString("rejectionReason"),
                importantVotes = votedBy.size,
                votedByMe = me != null && me in votedBy,
                comments = comments,
                category = enumOr(doc.getString("category"), PetCategory.ADOPTION),
                latitude = doc.getDouble("latitude"),
                longitude = doc.getDouble("longitude"),
                imageUrls = (doc.get("imageUrls") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                createdAt = doc.getLong("createdAt") ?: 0L
            )
        } catch (e: Exception) {
            Log.e("PetRepository", "No se pudo leer la publicación ${doc.id}", e)
            null
        }
    }

    private inline fun <reified T : Enum<T>> enumOr(name: String?, default: T): T =
        runCatching { enumValueOf<T>(name ?: "") }.getOrDefault(default)
}