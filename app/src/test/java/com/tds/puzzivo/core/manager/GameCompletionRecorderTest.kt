package com.tds.puzzivo.core.manager

import com.tds.puzzivo.core.analytics.AnalyticsManager
import com.tds.puzzivo.core.model.DifficultyLevel
import com.tds.puzzivo.core.model.GameStatistics
import com.tds.puzzivo.core.model.GameType
import com.tds.puzzivo.core.storage.PreferencesRepository
import com.tds.puzzivo.core.storage.UserPreferences
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class GameCompletionRecorderTest {
    @Test
    fun `same completion id is recorded only once`() = runTest {
        val preferencesRepository = mockk<PreferencesRepository>(relaxed = true)
        val statisticsManager = mockk<StatisticsManager>(relaxed = true)
        val achievementManager = mockk<AchievementManager>(relaxed = true)
        val progressionManager = mockk<ProgressionManager>(relaxed = true)
        val challengeManager = mockk<ChallengeManager>(relaxed = true)
        val analyticsManager = mockk<AnalyticsManager>(relaxed = true)
        val leaderboardManager = mockk<LeaderboardManager>(relaxed = true)
        val stats = GameStatistics(gameType = GameType.LIGHTS_OUT)

        every { preferencesRepository.userPreferencesFlow } returns flowOf(UserPreferences())
        every { statisticsManager.getStatistics(GameType.LIGHTS_OUT) } returns flowOf(stats)
        every { statisticsManager.allStatistics } returns flowOf(listOf(stats))

        val recorder = GameCompletionRecorder(
            preferencesRepository = preferencesRepository,
            statisticsManager = statisticsManager,
            achievementManager = achievementManager,
            progressionManager = progressionManager,
            challengeManager = challengeManager,
            analyticsManager = analyticsManager,
            leaderboardManager = leaderboardManager
        )
        val request = GameCompletionRequest(
            completionId = "completion-1",
            gameType = GameType.LIGHTS_OUT,
            score = 500,
            difficulty = DifficultyLevel.MEDIUM
        )

        recorder.record(request)
        recorder.record(request)

        coVerify(exactly = 1) {
            statisticsManager.recordGameSession(
                gameType = GameType.LIGHTS_OUT,
                score = 500,
                durationSeconds = 0,
                won = true,
                difficulty = DifficultyLevel.MEDIUM,
                mode = "classic",
                hintsUsed = 0,
                moves = 0
            )
        }
        coVerify(exactly = 1) { preferencesRepository.recordGameCompleted(GameType.LIGHTS_OUT) }
    }
}
