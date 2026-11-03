package com.tds.puzzivo.feature.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.tds.puzzivo.R
import com.tds.puzzivo.core.model.GameType
import com.tds.puzzivo.core.storage.UserPreferences

/**
 * Pre-computed game statistics to avoid recomposition overhead.
 * Use remember() to cache these computations.
 */
data class GameStats(
    val gameType: GameType,
    val statText: String
)

@Composable
fun rememberGameStats(userPreferences: UserPreferences): Map<GameType, String> {
    val numberMergeBest = stringResource(R.string.home_best_score_format, userPreferences.numberMergeBestScore)
    val memoryMatchBest = if (userPreferences.memoryMatchBestMoves > 0) {
        pluralStringResource(
            R.plurals.home_best_moves_format,
            userPreferences.memoryMatchBestMoves,
            userPreferences.memoryMatchBestMoves
        )
    } else {
        stringResource(R.string.home_no_score_yet)
    }
    val quickMathBest = stringResource(R.string.home_best_score_format, userPreferences.quickMathBestScore)
    val wordSearchBest = stringResource(
        R.string.home_solved_format,
        userPreferences.wordSearchSolvedIds.size,
        GameType.WORD_SEARCH.totalContentCount ?: 0
    )
    val newGameText = stringResource(R.string.home_new_game)

    return remember(userPreferences, numberMergeBest, memoryMatchBest, quickMathBest, wordSearchBest, newGameText) {
        GameType.entries.associateWith { gameType ->
            when (gameType) {
                GameType.NUMBER_MERGE -> numberMergeBest
                GameType.MEMORY_MATCH -> memoryMatchBest
                GameType.QUICK_MATH -> quickMathBest
                GameType.WORD_SEARCH -> wordSearchBest
                else -> newGameText
            }
        }
    }
}
