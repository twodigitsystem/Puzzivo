package com.tds.puzzivo.feature.profile

import app.cash.turbine.test
import com.tds.puzzivo.core.auth.AuthManager
import com.tds.puzzivo.core.manager.AchievementManager
import com.tds.puzzivo.core.manager.ProgressionManager
import com.tds.puzzivo.core.manager.StatisticsManager
import com.tds.puzzivo.core.manager.ThemeManager
import com.tds.puzzivo.core.model.GameTheme
import com.tds.puzzivo.core.model.PlayerProgress
import com.tds.puzzivo.core.storage.PreferencesRepository
import com.tds.puzzivo.core.storage.UserPreferences
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var authManager: AuthManager
    private lateinit var progressionManager: ProgressionManager
    private lateinit var preferencesRepository: PreferencesRepository
    private lateinit var achievementManager: AchievementManager
    private lateinit var statisticsManager: StatisticsManager
    private lateinit var themeManager: ThemeManager

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authManager = mockk(relaxed = true)
        progressionManager = mockk(relaxed = true)
        preferencesRepository = mockk(relaxed = true)
        achievementManager = mockk(relaxed = true)
        statisticsManager = mockk(relaxed = true)
        themeManager = mockk(relaxed = true)

        every { authManager.currentUser } returns kotlinx.coroutines.flow.MutableStateFlow(null)
        every { progressionManager.playerProgress } returns flowOf(PlayerProgress(level = 3, coins = 150))
        every { preferencesRepository.userPreferencesFlow } returns flowOf(UserPreferences())
        every { achievementManager.allAchievements } returns flowOf(emptyList())
        every { achievementManager.unlockedAchievements } returns flowOf(emptyList())
        every { statisticsManager.allStatistics } returns flowOf(emptyList())
        every { themeManager.selectedTheme } returns flowOf(GameTheme.NAVY_DARK)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun uiStateEmitsInitialPlayerProgress() = runTest {
        val viewModel = ProfileViewModel(
            authManager,
            progressionManager,
            preferencesRepository,
            achievementManager,
            statisticsManager,
            themeManager
        )

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(3, state.playerProgress.level)
            assertEquals(150, state.playerProgress.coins)
            assertEquals(GameTheme.NAVY_DARK, state.selectedTheme)
        }
    }
}
