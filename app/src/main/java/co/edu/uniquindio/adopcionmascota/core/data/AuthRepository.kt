package co.edu.uniquindio.adopcionmascota.core.data

import co.edu.uniquindio.adopcionmascota.core.util.await
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.firebase.auth.GoogleAuthProvider
object AuthRepository {

    private val auth get() = FirebaseAuth.getInstance()
    private val db get() = FirebaseFirestore.getInstance()

    // Crea la cuenta en Firebase Auth y guarda el perfil en Firestore
    suspend fun register(name: String, email: String, password: String, phone: String, city: String) {
        val cleanEmail = email.trim().lowercase()
        val uid = auth.createUserWithEmailAndPassword(cleanEmail, password).await().user!!.uid
        db.collection("users").document(uid).set(
            mapOf(
                "name" to name.trim(),
                "email" to cleanEmail,
                "phone" to phone.trim(),
                "city" to city.trim(),
                "role" to "user"
            )
        ).await()
        SessionManager.setUser(User(uid, name.trim(), cleanEmail, false, phone.trim()))
    }

    suspend fun login(email: String, password: String) {
        val uid = auth.signInWithEmailAndPassword(email.trim(), password).await().user!!.uid
        loadProfile(uid)
    }
    suspend fun loginWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val uid = auth.signInWithCredential(credential).await().user!!.uid
        loadProfile(uid) // si es la primera vez, crea su perfil en Firestore
    }

    // Lee el perfil (nombre, rol...) del usuario desde Firestore
    suspend fun loadProfile(uid: String) {
        val fbUser = auth.currentUser
        val ref = db.collection("users").document(uid)
        val doc = ref.get().await()
        if (doc.exists()) {
            SessionManager.setUser(
                User(
                    id = uid,
                    name = doc.getString("name") ?: fbUser?.displayName ?: "Usuario",
                    email = doc.getString("email") ?: fbUser?.email.orEmpty(),
                    isModerator = doc.getString("role") == "moderator",
                    phone = doc.getString("phone").orEmpty(),
                    badges = (doc.get("badges") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                    level = doc.getString("level")
                )
            )
        } else {
            // Primera vez que entra (por ejemplo con Google): crear su perfil
            val name = fbUser?.displayName ?: fbUser?.email?.substringBefore("@") ?: "Usuario"
            val email = fbUser?.email.orEmpty()
            ref.set(mapOf("name" to name, "email" to email, "phone" to "", "role" to "user")).await()
            SessionManager.setUser(User(uid, name, email, false, ""))
        }
    }

    suspend fun sendPasswordReset(email: String) {
        auth.useAppLanguage() // el correo llega en el idioma del celular
        auth.sendPasswordResetEmail(email.trim()).await()
    }

    fun friendlyError(e: Throwable): String = when (e) {
        is FirebaseAuthWeakPasswordException -> "La contraseña es muy débil (mínimo 6 caracteres)."
        is FirebaseAuthUserCollisionException -> "Ya existe una cuenta con ese correo."
        is FirebaseAuthInvalidUserException -> "No existe una cuenta con ese correo."
        is FirebaseAuthInvalidCredentialsException -> "Correo o contraseña incorrectos."
        is FirebaseNetworkException -> "Sin conexión a internet."
        is GetCredentialCancellationException -> "Inicio de sesión con Google cancelado."
        is NoCredentialException -> "No hay una cuenta de Google en este dispositivo."
        else -> "Ocurrió un error: ${e.localizedMessage ?: "intenta de nuevo"}"
    }
}