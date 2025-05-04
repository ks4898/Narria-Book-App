package edu.rit.ks4898.narria

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun AppNavigation(
    isLoggedIn: Boolean,
    onLoginSuccess: () -> Unit,
    onLogout: () -> Unit,
    darkThemeEnabled: MutableState<Boolean>
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) "main" else "login"
    ) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = onLoginSuccess,
                onNavigateToRegister = { navController.navigate("register") }
            )
        }

        composable("register") {
            RegisterScreen(
                onRegisterSuccess = onLoginSuccess,
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        composable("main") {
            MainScreen(
                navController = navController,
                onLogout = onLogout,
                darkThemeEnabled = darkThemeEnabled
            )
        }

        composable(
            "bookDetail/{bookId}/{initialIsFavorite}",
            arguments = listOf(
                navArgument("bookId") { type = NavType.StringType },
                navArgument("initialIsFavorite") { type = NavType.BoolType }
            )
        ) {
            val bookId = it.arguments?.getString("bookId") ?: ""
            val initialIsFavorite = it.arguments?.getBoolean("initialIsFavorite") ?: false
            BookDetailScreen(
                bookId = bookId,
                initialIsFavorite = initialIsFavorite,
                navController = navController
            )
        }

        composable("search") {
            SearchScreen(navController = navController)
        }
    }
}