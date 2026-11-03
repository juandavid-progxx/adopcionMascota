package co.edu.uniquindio.adopcionmascota.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import co.edu.uniquindio.adopcionmascota.features.createPosts.CreatePostsScreen
import co.edu.uniquindio.adopcionmascota.features.feed.FeedScreen
import co.edu.uniquindio.adopcionmascota.features.home.HomeScreen
import co.edu.uniquindio.adopcionmascota.features.login.LoginScreen
import co.edu.uniquindio.adopcionmascota.features.passwordRecovery.PasswordRecoveryScreen
import co.edu.uniquindio.adopcionmascota.features.register.RegisterScreen
import co.edu.uniquindio.adopcionmascota.core.data.SessionManager
import co.edu.uniquindio.adopcionmascota.features.moderation.ModerationScreen
import androidx.navigation.toRoute
import co.edu.uniquindio.adopcionmascota.features.notifications.NotificationsScreen
import co.edu.uniquindio.adopcionmascota.features.profile.ProfileScreen
import co.edu.uniquindio.adopcionmascota.features.notifications.RequesterProfileScreen
import co.edu.uniquindio.adopcionmascota.features.myposts.MyPostsScreen
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = MainRoutes.Home
    ) {
        composable<MainRoutes.Home> {
            HomeScreen(
                onUserAccessClick = { navController.navigate(MainRoutes.Login) },
                onModeratorAccessClick = { navController.navigate(MainRoutes.Login) },
                onLoginClick = { navController.navigate(MainRoutes.Login) },
                onRegisterClick = { navController.navigate(MainRoutes.Register) }
            )
        }
        composable<MainRoutes.Login> {
            LoginScreen(
                onLoginSuccess = {
                    if (SessionManager.isModerator) {
                        navController.navigate(MainRoutes.Moderation) { popUpTo(MainRoutes.Home) }
                    } else {
                        navController.navigate(MainRoutes.Feed())
                    }
                },
                onForgotPasswordClick = { navController.navigate(MainRoutes.PasswordRecovery) },
                onRegisterClick = { navController.navigate(MainRoutes.Register) },
                onGoogleLoginClick = { navController.navigate(MainRoutes.Feed) }
            )
        }
        composable<MainRoutes.Register> {
            RegisterScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable<MainRoutes.Moderation> {
            ModerationScreen(
                onLogout = {
                    SessionManager.logout()
                    navController.navigate(MainRoutes.Home) { popUpTo(MainRoutes.Home) { inclusive = true } }
                }
            )
        }
        composable<MainRoutes.Feed> { backStackEntry ->
            val route = backStackEntry.toRoute<MainRoutes.Feed>()
            FeedScreen(
                initialPetId = route.petId,
                onCreatePostClick = { navController.navigate(MainRoutes.CreatePosts) },
                onNotificationsClick = { navController.navigate(MainRoutes.Notifications) },
                onProfileClick = { navController.navigate(MainRoutes.Profile) }
            )
        }
        composable<MainRoutes.Notifications> {
            NotificationsScreen(
                onBack = { navController.popBackStack() },
                onOpenPet = { petId ->
                    navController.navigate(MainRoutes.Feed(petId)) {
                        popUpTo<MainRoutes.Feed> { inclusive = true }
                    }
                },
                onOpenRequester = { id -> navController.navigate(MainRoutes.RequesterProfile(id)) }
            )
        }
        composable<MainRoutes.CreatePosts> {
            CreatePostsScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable<MainRoutes.Profile> {
            ProfileScreen(
                onBack = { navController.popBackStack() },
                onLoggedOut = {
                    navController.navigate(MainRoutes.Home) {
                        popUpTo(MainRoutes.Home) { inclusive = true }
                    }
                },
                onMyPostsClick = { navController.navigate(MainRoutes.MyPosts) }
            )
        }
        composable<MainRoutes.RequesterProfile> { backStackEntry ->
            val route = backStackEntry.toRoute<MainRoutes.RequesterProfile>()
            RequesterProfileScreen(
                notificationId = route.notificationId,
                onBack = { navController.popBackStack() },
                onOpenPet = { petId ->
                    navController.navigate(MainRoutes.Feed(petId)) {
                        popUpTo<MainRoutes.Feed> { inclusive = true }
                    }
                }
            )
        }
        composable<MainRoutes.MyPosts> {
            MyPostsScreen(onBack = { navController.popBackStack() })
        }
        composable<MainRoutes.PasswordRecovery> {
            PasswordRecoveryScreen(
                onBack = { navController.popBackStack() },
                onLoginClick = {
                    navController.navigate(MainRoutes.Login) {
                        popUpTo(MainRoutes.Home)
                    }
                }
            )
        }
    }
}

@Deprecated("Usa AppNavigation con el nombre correcto", ReplaceWith("AppNavigation()"))
@Composable
fun AppNagegation() {
    AppNavigation()
}
