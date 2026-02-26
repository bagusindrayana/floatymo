package com.potadev.floatymo.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.potadev.floatymo.ui.screens.gallery.GalleryScreen
import com.potadev.floatymo.ui.screens.main.MainScreen
import com.potadev.floatymo.ui.screens.position.PositionScreen
import com.potadev.floatymo.ui.screens.search.SearchScreen
import com.potadev.floatymo.ui.screens.settings.SettingsScreen

sealed class Screen(val route: String) {
    data object Main : Screen("main")
    data object Gallery : Screen("gallery")
    data object Search : Screen("search")
    data object Settings : Screen("settings")
    data object Position : Screen("position")
}

@Composable
fun FloatyMoNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Main.route
    ) {
        composable(Screen.Main.route) {
            MainScreen(
                onNavigateToGallery = { navController.navigate(Screen.Gallery.route) },
                onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.Gallery.route) {
            GalleryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Search.route) {
            SearchScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPosition = { navController.navigate(Screen.Position.route) }
            )
        }

        composable(Screen.Position.route) {
            PositionScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
