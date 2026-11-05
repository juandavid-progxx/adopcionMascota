package co.edu.uniquindio.adopcionmascota.core.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class User(
    val id: String,
    val name: String,
    val email: String,
    val isModerator: Boolean = false,
    val phone: String = ""
)

object SessionManager {

    var currentUser: User? = null
        private set

    val isModerator: Boolean
        get() = currentUser?.isModerator == true

    fun setUser(user: User) {
        currentUser = user
    }

    fun updateProfile(name: String, phone: String) {
        val user = currentUser ?: return
        currentUser = user.copy(name = name, phone = phone)
        FirebaseFirestore.getInstance().collection("users").document(user.id)
            .update(mapOf("name" to name, "phone" to phone))
    }

    fun logout() {
        FirebaseAuth.getInstance().signOut()
        currentUser = null
    }
}