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
import com.tds.puzzivo.feature.sudoku.SudokuScreen
import com.tds.puzzivo.feature.minesweeper.MinesweeperScreen
import com.tds.puzzivo.feature.patternrecall.PatternRecallScreen
import com.tds.puzzivo.feature.wordscramble.WordScrambleScreen
import com.tds.puzzivo.feature.lightsout.LightsOutScreen
import com.tds.puzzivo.feature.nonogram.NonogramScreen
import com.tds.puzzivo.feature.hexmerge.HexMergeScreen

object NavRoutes {
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val NUMBER_MERGE = "game/number_merge"
    const val MEMORY_MATCH = "game/memory_match"
    const val QUICK_MATH = "game/quick_math"
    const val WORD_SEARCH = "game/word_search"
    const val MINI_SUDOKU = "game/mini_sudoku"
    const val MINESWEEPER = "game/minesweeper"
    const val PATTERN_RECALL = "game/pattern_recall"
    const val WORD_SCRAMBLE = "game/word_scramble"
    const val LIGHTS_OUT = "game/lights_out"
    const val NONOGRAM = "game/nonogram"
    const val HEX_MERGE = "game/hex_merge"
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
                        GameType.MINI_SUDOKU -> NavRoutes.MINI_SUDOKU
                        GameType.MINESWEEPER -> NavRoutes.MINESWEEPER
                        GameType.PATTERN_RECALL -> NavRoutes.PATTERN_RECALL
                        GameType.WORD_SCRAMBLE -> NavRoutes.WORD_SCRAMBLE
                        GameType.LIGHTS_OUT -> NavRoutes.LIGHTS_OUT
                        GameType.NONOGRAM -> NavRoutes.NONOGRAM
                        GameType.HEX_MERGE -> NavRoutes.HEX_MERGE
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

        composable(NavRoutes.MINI_SUDOKU) {
            SudokuScreen(onBackClick = { navController.popBackStack() })
        }
        composable(NavRoutes.MINESWEEPER) {
            MinesweeperScreen(onBackClick = { navController.popBackStack() })
        }
        composable(NavRoutes.PATTERN_RECALL) {
            PatternRecallScreen(onBackClick = { navController.popBackStack() })
        }
        composable(NavRoutes.WORD_SCRAMBLE) {
            WordScrambleScreen(onBackClick = { navController.popBackStack() })
        }
        composable(NavRoutes.LIGHTS_OUT) {
            LightsOutScreen(onBackClick = { navController.popBackStack() })
        }
        composable(NavRoutes.NONOGRAM) {
            NonogramScreen(onBackClick = { navController.popBackStack() })
        }
        composable(NavRoutes.HEX_MERGE) {
            HexMergeScreen(onBackClick = { navController.popBackStack() })
        }
    }
}
