package co.edu.uniquindio.adopcionmascota.core.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

data class AppNotification(
    val id: String,
    val userId: String,        // a quién le llega
    val title: String,
    val message: String,
    val petId: String? = null, // publicación relacionada
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    // Datos de contacto de quien envió la solicitud (solo en solicitudes)
    val contactName: String? = null,
    val contactEmail: String? = null,
    val contactPhone: String? = null,
    val isAchievement: Boolean = false
)

object NotificationRepository {

    private val db get() = FirebaseFirestore.getInstance()

    // Solo las notificaciones del usuario que tiene la sesión abierta
    private val _all = MutableStateFlow<List<AppNotification>>(emptyList())
    val all: StateFlow<List<AppNotification>> = _all.asStateFlow()

    private var listener: ListenerRegistration? = null

    fun start() {
        stop()
        val me = SessionManager.currentUser?.id ?: return
        listener = db.collection("notifications").whereEqualTo("userId", me)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    Log.e("NotificationRepo", "Error escuchando notificaciones", error)
                    return@addSnapshotListener
                }
                _all.value = snapshot.documents.map { d ->
                    AppNotification(
                        id = d.id,
                        userId = d.getString("userId") ?: "",
                        title = d.getString("title") ?: "",
                        message = d.getString("message") ?: "",
                        petId = d.getString("petId"),
                        isRead = d.getBoolean("isRead") ?: false,
                        timestamp = d.getLong("timestamp") ?: 0L,
                        contactName = d.getString("contactName"),
                        contactEmail = d.getString("contactEmail"),
                        contactPhone = d.getString("contactPhone"),
                        isAchievement = d.getBoolean("isAchievement") ?: false
                    )
                }.sortedByDescending { it.timestamp }
            }
    }

    fun stop() {
        listener?.remove()
        listener = null
        _all.value = emptyList()
    }

    // Crea una notificación para cualquier usuario (el destinatario la recibe en tiempo real)
    fun add(
        userId: String,
        title: String,
        message: String,
        petId: String?,
        contactName: String? = null,
        contactEmail: String? = null,
        contactPhone: String? = null,
        isAchievement: Boolean = false
    ) {
        val id = UUID.randomUUID().toString()
        db.collection("notifications").document(id).set(
            mapOf<String, Any?>(
                "userId" to userId,
                "title" to title,
                "message" to message,
                "petId" to petId,
                "isRead" to false,
                "timestamp" to System.currentTimeMillis(),
                "contactName" to contactName,
                "contactEmail" to contactEmail,
                "contactPhone" to contactPhone,
                "isAchievement" to isAchievement
            )
        )
    }

    fun markRead(id: String) {
        db.collection("notifications").document(id).update("isRead", true)
    }

    fun toggleRead(id: String) {
        val n = _all.value.find { it.id == id } ?: return
        db.collection("notifications").document(id).update("isRead", !n.isRead)
    }

    fun markAllRead(userId: String) {
        _all.value.filter { !it.isRead }.forEach { markRead(it.id) }
    }

    fun deleteForUser(userId: String) {
        db.collection("notifications").whereEqualTo("userId", userId).get()
            .addOnSuccessListener { snap ->
                val batch = db.batch()
                snap.documents.forEach { batch.delete(it.reference) }
                batch.commit()
            }
    }
}