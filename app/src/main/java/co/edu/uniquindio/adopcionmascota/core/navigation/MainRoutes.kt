package co.edu.uniquindio.adopcionmascota.core.navigation

import kotlinx.serialization.Serializable

sealed class MainRoutes {
    @Serializable
    data object Moderation : MainRoutes()

    @Serializable
    data object Home : MainRoutes()

    @Serializable
    data object Login : MainRoutes()

    @Serializable
    data object Register : MainRoutes()
    @Serializable
    data class Feed(val petId: String? = null) : MainRoutes()

    @Serializable
    data object Notifications : MainRoutes()
    @Serializable
    data object CreatePosts : MainRoutes()

    @Serializable
    data object MyPosts : MainRoutes()
    @Serializable
    data object PasswordRecovery : MainRoutes()
    @Serializable
    data object Profile : MainRoutes()
    @Serializable
    data class RequesterProfile(val notificationId: String) : MainRoutes()
}