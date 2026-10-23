package com.tds.puzzivo.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tds.puzzivo.core.model.GameType
import com.tds.puzzivo.feature.home.HomeScreen
import com.tds.puzzivo.feature.memorymatch.MemoryMatchScreen
import com.tds.puzzivo.feature.numbermerge.NumberMergeScreen
import com.tds.puzzivo.feature.quickmath.QuickMathScreen
import com.tds.puzzivo.feature.settings.SettingsScreen
import com.tds.puzzivo.feature.wordsearch.WordSearchScreen

object NavRoutes {
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val NUMBER_MERGE = "game/number_merge"
    const val MEMORY_MATCH = "game/memory_match"
    const val QUICK_MATH = "game/quick_math"
    const val WORD_SEARCH = "game/word_search"
}

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.HOME,
        modifier = modifier
    ) {
        composable(NavRoutes.HOME) {
            HomeScreen(
                onNavigateToGame = { gameType ->
                    val route = when (gameType) {
                        GameType.NUMBER_MERGE -> NavRoutes.NUMBER_MERGE
                        GameType.MEMORY_MATCH -> NavRoutes.MEMORY_MATCH
                        GameType.QUICK_MATH -> NavRoutes.QUICK_MATH
                        GameType.WORD_SEARCH -> NavRoutes.WORD_SEARCH
                    }
                    navController.navigate(route)
                },
                onNavigateToSettings = {
                    navController.navigate(NavRoutes.SETTINGS)
                }
            )
        }

        composable(NavRoutes.SETTINGS) {
            SettingsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.NUMBER_MERGE) {
            NumberMergeScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.MEMORY_MATCH) {
            MemoryMatchScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.QUICK_MATH) {
            QuickMathScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.WORD_SEARCH) {
            WordSearchScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
