package com.virtualworld.easymusic.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.virtualworld.easymusic.ui.equalizer.EqualizerScreen
import com.virtualworld.easymusic.ui.library.LibraryScreen
import com.virtualworld.easymusic.ui.player.PlayerScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.PLAYER
    ) {
        composable(Routes.PLAYER) {
            PlayerScreen(
                onNavigateToLibrary = { navController.navigate(Routes.LIBRARY) },
                onNavigateToEqualizer = { navController.navigate(Routes.EQUALIZER) }
            )
        }
        composable(Routes.LIBRARY) {
            LibraryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Routes.EQUALIZER) {
            EqualizerScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
