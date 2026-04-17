package com.potadev.floatymo.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.potadev.floatymo.ui.screens.gallery.GalleryScreen
import com.potadev.floatymo.ui.screens.onboarding.OnboardingScreen
import com.potadev.floatymo.ui.screens.overlay.OverlayManagementScreen
import com.potadev.floatymo.ui.screens.position.PositionScreen
import com.potadev.floatymo.ui.screens.search.SearchScreen

private const val TRANSITION_DURATION = 300

@Composable
fun AppNavigation(
    navController: NavHostController,
    onToggleService: (Boolean) -> Unit,
    isServiceRunning: Boolean,
    startDestination: String = "overlay_management"
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(TRANSITION_DURATION)
            ) + fadeIn(tween(TRANSITION_DURATION))
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(TRANSITION_DURATION)
            ) + fadeOut(tween(TRANSITION_DURATION))
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(TRANSITION_DURATION)
            ) + fadeIn(tween(TRANSITION_DURATION))
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(TRANSITION_DURATION)
            ) + fadeOut(tween(TRANSITION_DURATION))
        }
    ) {
        composable("onboarding") {
            OnboardingScreen(
                onOnboardingComplete = {
                    navController.navigate("overlay_management") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                },
                onSkip = {
                    navController.navigate("overlay_management") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }

        composable("overlay_management") {
            OverlayManagementScreen(
                onNavigateToGallery = { navController.navigate("gallery") },
                onNavigateToSearch = { navController.navigate("search") },
                onNavigateToPosition = { navController.navigate("position") },
                onToggleService = onToggleService,
                isServiceRunning = isServiceRunning
            )
        }

        composable("gallery") {
            GalleryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("search") {
            SearchScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("position") {
            PositionScreen(
                selectedGifIds = emptySet(),
                onNavigateBack = { navController.popBackStack() },
                onSave = { navController.popBackStack() }
            )
        }
    }
}
