package com.virtualworld.easymusic.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.virtualworld.easymusic.ui.equalizer.EqualizerScreen
import com.virtualworld.easymusic.ui.library.CollectionSongsScreen
import com.virtualworld.easymusic.ui.library.LibraryScreen
import com.virtualworld.easymusic.ui.player.PlayerScreen
import com.virtualworld.easymusic.ui.settings.SettingsScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.PLAYER
    ) {
        composable(Routes.PLAYER) {
            PlayerScreen(
                onNavigateToLibrary = { navController.navigate(Routes.library(openSearch = false)) },
                onNavigateToLibrarySearch = { navController.navigate(Routes.library(openSearch = true)) },
                onNavigateToEqualizer = { navController.navigate(Routes.EQUALIZER) },
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }
        composable(
            route = Routes.LIBRARY,
            arguments = listOf(
                navArgument("openSearch") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val openSearch = backStackEntry.arguments?.getBoolean("openSearch") ?: false
            LibraryScreen(
                initialFocusSearch = openSearch,
                onNavigateBack = { navController.popBackStack() },
                onOpenAlbum = { albumId ->
                    navController.navigate(Routes.collectionAlbum(albumId))
                },
                onOpenArtist = { artistId ->
                    navController.navigate(Routes.collectionArtist(artistId))
                }
            )
        }
        composable(
            route = Routes.COLLECTION,
            arguments = listOf(
                navArgument("kind") { type = NavType.StringType },
                navArgument("itemId") { type = NavType.LongType }
            )
        ) {
            CollectionSongsScreen(
                onNavigateBack = { navController.popBackStack() },
                onPlaybackStarted = {
                    navController.popBackStack(Routes.PLAYER, inclusive = false)
                }
            )
        }
        composable(Routes.EQUALIZER) {
            EqualizerScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
