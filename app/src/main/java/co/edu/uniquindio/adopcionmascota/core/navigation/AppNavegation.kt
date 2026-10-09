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

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = MainRoutes.Home
    ) {
        composable<MainRoutes.Home> {
            HomeScreen(
                onUserAccessClick = { navController.navigate(MainRoutes.Feed) },
                onLoginClick = { navController.navigate(MainRoutes.Login) },
                onRegisterClick = { navController.navigate(MainRoutes.Register) }
            )
        }
        composable<MainRoutes.Login> {
            LoginScreen(
                onLoginSuccess = { navController.navigate(MainRoutes.Feed) },
                onForgotPasswordClick = { navController.navigate(MainRoutes.PasswordRecovery) },
                onRegisterClick = { navController.navigate(MainRoutes.Register) }
            )
        }
        composable<MainRoutes.Register> {
            RegisterScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable<MainRoutes.Feed> {
            FeedScreen(
                onCreatePostClick = { navController.navigate(MainRoutes.CreatePosts) }
            )
        }
        composable<MainRoutes.CreatePosts> {
            CreatePostsScreen(
                onBack = { navController.popBackStack() }
            )
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
