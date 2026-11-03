package com.tds.puzzivo.core.manager

import com.tds.puzzivo.core.analytics.AnalyticsManager
import com.tds.puzzivo.core.model.DifficultyLevel
import com.tds.puzzivo.core.model.GameType
import com.tds.puzzivo.core.storage.PreferencesRepository
import kotlinx.coroutines.flow.first
import java.util.Collections
import java.util.LinkedHashSet
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Records the complete result of a game in one place.
 *
 * This keeps the local streak/high-score system, Room statistics, progression,
 * achievements, daily challenges, Analytics, and the optional Firestore
 * leaderboard submission in sync for every game screen.
 */
@Singleton
class GameCompletionRecorder @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val statisticsManager: StatisticsManager,
    private val achievementManager: AchievementManager,
    private val progressionManager: ProgressionManager,
    private val challengeManager: ChallengeManager,
    private val analyticsManager: AnalyticsManager,
    private val leaderboardManager: LeaderboardManager
) {
    private val recordedCompletionIds = Collections.synchronizedSet(
        object : LinkedHashSet<String>() {
            override fun add(element: String): Boolean {
                val added = super.add(element)
                if (size > 1_000) iterator().remove()
                return added
            }
        }
    )

    suspend fun record(request: GameCompletionRequest) {
        if (!recordedCompletionIds.add(request.completionId)) return

        val initialPreferences = preferencesRepository.userPreferencesFlow.first()
        val difficulty = request.difficulty ?: initialPreferences.selectedDifficulty
        val finalScore = (request.score * difficulty.scoreMultiplier)
            .toInt()
            .coerceAtLeast(0)

        statisticsManager.recordGameSession(
            gameType = request.gameType,
            score = finalScore,
            durationSeconds = request.durationSeconds.coerceAtLeast(0),
            won = request.won,
            difficulty = difficulty,
            mode = request.mode,
            hintsUsed = request.hintsUsed.coerceAtLeast(0),
            moves = request.moves.coerceAtLeast(0)
        )

        // Preserve the existing local streak and daily-pick behavior.
        preferencesRepository.recordGameCompleted(request.gameType)
        val updatedPreferences = preferencesRepository.userPreferencesFlow.first()

        val statistics = statisticsManager.getStatistics(request.gameType).first()
        if (statistics != null) {
            achievementManager.checkAndUnlockAchievements(
                gameType = request.gameType,
                score = finalScore,
                gamesPlayed = statistics.totalPlays,
                gamesWon = statistics.totalWins,
                perfectGame = request.won && request.hintsUsed == 0,
                hintsUsed = request.hintsUsed,
                durationSeconds = request.durationSeconds
            )

            val allStatistics = statisticsManager.allStatistics.first()
            achievementManager.checkUniversalAchievements(
                totalGamesPlayed = allStatistics.sumOf { it.totalPlays },
                totalGamesWon = allStatistics.sumOf { it.totalWins },
                currentStreak = updatedPreferences.currentStreak,
                gameWon = request.won
            )
        }

        if (request.won) {
            val xp = calculateReward(finalScore, difficulty, baseSmall = 10, baseMedium = 25, baseLarge = 50)
            val coins = calculateReward(finalScore, difficulty, baseSmall = 5, baseMedium = 10, baseLarge = 25)
            progressionManager.addXP(xp)
            progressionManager.addCoins(coins)

            challengeManager.completeIfMatchingToday(request.gameType)
            leaderboardManager.submitScore(
                gameType = request.gameType,
                difficulty = difficulty,
                score = finalScore,
                mode = request.mode
            )
        }

        analyticsManager.logGameCompleted(
            gameType = request.gameType,
            score = finalScore,
            duration = request.durationSeconds
        )
        if (request.gameType == PreferencesRepository.getTodayPickGameType()) {
            analyticsManager.logTodayPickPlayed(request.gameType)
        }
    }

    private fun calculateReward(
        score: Int,
        difficulty: DifficultyLevel,
        baseSmall: Int,
        baseMedium: Int,
        baseLarge: Int
    ): Int {
        val base = when {
            score < 100 -> baseSmall
            score < 1_000 -> baseMedium
            else -> baseLarge
        }
        return (base * difficulty.scoreMultiplier).toInt().coerceAtLeast(1)
    }
}

data class GameCompletionRequest(
    val completionId: String,
    val gameType: GameType,
    val score: Int,
    val durationSeconds: Int = 0,
    val won: Boolean = true,
    val moves: Int = 0,
    val hintsUsed: Int = 0,
    val difficulty: DifficultyLevel? = null,
    val mode: String = "classic"
)
