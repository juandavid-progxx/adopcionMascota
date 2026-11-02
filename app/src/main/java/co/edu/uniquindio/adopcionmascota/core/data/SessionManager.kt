package co.edu.uniquindio.adopcionmascota.core.data

data class User(
    val id: String,
    val name: String,
    val email: String,
    val isModerator: Boolean = false
)

object SessionManager {

    // Moderadores precargados (el requisito dice que ya existen en la base de datos)
    private val moderators = mapOf("moderador@adoptaya.com" to "Moderador AdoptaYa")

    var currentUser: User? = null
        private set

    val isModerator: Boolean
        get() = currentUser?.isModerator == true

    fun login(email: String) {
        val cleanEmail = email.trim().lowercase()
        val moderatorName = moderators[cleanEmail]
        currentUser = if (moderatorName != null) {
            User(id = cleanEmail, name = moderatorName, email = cleanEmail, isModerator = true)
        } else {
            User(id = cleanEmail, name = cleanEmail.substringBefore("@"), email = cleanEmail)
        }
    }

    fun loginWithGoogle() {
        currentUser = User("google@gmail.com", "Usuario Google", "google@gmail.com")
    }

    fun logout() {
        currentUser = null
    }
}