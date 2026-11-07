package co.edu.uniquindio.adopcionmascota.core.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class User(
    val id: String,
    val name: String,
    val email: String,
    val isModerator: Boolean = false,
    val phone: String = "",
    val badges: List<String> = emptyList(), // insignias ya notificadas
    val level: String? = null               // nivel ya notificado
)

object SessionManager {

    var currentUser: User? = null
        private set

    val isModerator: Boolean
        get() = currentUser?.isModerator == true

    fun setUser(user: User) {
        currentUser = user
        PetRepository.start()
        NotificationRepository.start()
    }

    fun updateProfile(name: String, phone: String) {
        val user = currentUser ?: return
        currentUser = user.copy(name = name, phone = phone)
        FirebaseFirestore.getInstance().collection("users").document(user.id)
            .update(mapOf("name" to name, "phone" to phone))
    }

    fun logout() {
        PetRepository.stop()
        NotificationRepository.stop()
        ReputationManager.reset()
        FirebaseAuth.getInstance().signOut()
        currentUser = null
    }
}