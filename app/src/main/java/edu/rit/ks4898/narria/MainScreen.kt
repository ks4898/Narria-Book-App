package edu.rit.ks4898.narria

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun MainScreen(
    navController: NavHostController,
    onLogout: () -> Unit,
    darkThemeEnabled: MutableState<Boolean>
) {
    val items = listOf("home", "favorites", "search", "profile")
    var selectedItem by remember { mutableStateOf(items[0]) }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                items.forEach { screen ->
                    NavigationBarItem(
                        icon = {
                            when (screen) {
                                "home" -> Icon(Icons.Default.Home, contentDescription = null)
                                "favorites" -> Icon(Icons.Default.Favorite, contentDescription = null)
                                "search" -> Icon(Icons.Default.Search, contentDescription = null)
                                "profile" -> Icon(Icons.Default.Person, contentDescription = null)
                            }
                        },
                        label = { Text(screen.replaceFirstChar { it.uppercase() }) },
                        selected = selectedItem == screen,
                        onClick = {
                            selectedItem = screen
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = Color.Gray
                        )
                    )
                }
            }
        }
    ) { padding ->
        when (selectedItem) {
            "home" -> HomeScreen(navController, Modifier.padding(padding))
            "favorites" -> FavoritesScreen(navController, Modifier.padding(padding))
            "search" -> SearchScreen(navController, Modifier.padding(padding))
            "profile" -> ProfileScreen(
                onLogout = onLogout,
                darkThemeEnabled = darkThemeEnabled,
                modifier = Modifier.padding(padding)
            )
        }
    }
}