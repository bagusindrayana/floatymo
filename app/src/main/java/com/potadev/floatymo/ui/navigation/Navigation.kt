package com.potadev.floatymo.ui.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.potadev.floatymo.MainActivity
import com.potadev.floatymo.service.FloatingOverlayService
import com.potadev.floatymo.ui.screens.gallery.GalleryScreen
import com.potadev.floatymo.ui.screens.home.HomeScreen
import com.potadev.floatymo.ui.screens.position.PositionScreen
import com.potadev.floatymo.ui.screens.search.SearchScreen

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Gallery : Screen("gallery")
    data object Search : Screen("search")
    data object Position : Screen("position")
}

@Composable
fun FloatyMoNavHost(
    context: Context
) {
    val navController = rememberNavController()
    val mainActivity = context as? MainActivity

    var selectedGifIds by remember { mutableStateOf(setOf<Long>()) }
    var isServiceRunning by remember { mutableStateOf(FloatingOverlayService.isRunning()) }

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            if (mainActivity != null) {
                HomeScreen(
                    onNavigateToGallery = { navController.navigate(Screen.Gallery.route) },
                    onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                    onNavigateToSettings = {
                        navController.navigate(Screen.Position.route)
                    },
                    onToggleService = { enabled ->
                        if (enabled) {
                            mainActivity.startOverlayService()
                        } else {
                            mainActivity.stopOverlayService()
                        }
                        isServiceRunning = enabled
                    },
                    isServiceRunning = isServiceRunning
                )
            }
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

        composable(Screen.Position.route) {
            PositionScreen(
                selectedGifIds = selectedGifIds,
                onNavigateBack = { navController.popBackStack() },
                onSave = {
                    navController.popBackStack()
                }
            )
        }
    }
}
