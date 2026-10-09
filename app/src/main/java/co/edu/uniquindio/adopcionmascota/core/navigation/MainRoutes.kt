package co.edu.uniquindio.adopcionmascota.core.navigation

import kotlinx.serialization.Serializable

sealed class MainRoutes {

    @Serializable
    data object Home : MainRoutes()

    @Serializable
    data object Login : MainRoutes()

    @Serializable
    data object Register : MainRoutes()
    @Serializable
    data object Feed : MainRoutes()
    @Serializable
    data object CreatePosts : MainRoutes()

    @Serializable
    data object PasswordRecovery : MainRoutes()


}