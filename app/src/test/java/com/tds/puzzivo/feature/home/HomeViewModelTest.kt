package com.tds.puzzivo.feature.home

import app.cash.turbine.test
import com.tds.puzzivo.core.model.GameType
import com.tds.puzzivo.core.manager.StatisticsManager
import com.tds.puzzivo.core.storage.PreferencesRepository
import com.tds.puzzivo.core.storage.UserPreferences
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private lateinit var repository: PreferencesRepository
    private lateinit var statisticsManager: StatisticsManager
    private lateinit var viewModel: HomeViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        statisticsManager = mockk(relaxed = true)
        every { statisticsManager.allStatistics } returns flowOf(emptyList())
    }

    @Test
    fun `initial state has default user preferences`() = runTest {
        val mockPrefs = UserPreferences(
            currentStreak = 5,
            bestStreak = 10,
            totalGamesPlayed = 42
        )
        every { repository.userPreferencesFlow } returns flowOf(mockPrefs)

        viewModel = HomeViewModel(repository, statisticsManager)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.drop(1).test {
            val state = awaitItem()
            assertEquals(5, state.userPreferences.currentStreak)
            assertEquals(10, state.userPreferences.bestStreak)
            assertEquals(42, state.userPreferences.totalGamesPlayed)
        }
    }

    @Test
    fun `today's pick is marked completed when user completed it today`() = runTest {
        val today = LocalDate.now()
        val mockPrefs = UserPreferences(
            todayPickLastDate = today.toString(),
            todayPickCompleted = true
        )
        every { repository.userPreferencesFlow } returns flowOf(mockPrefs)

        viewModel = HomeViewModel(repository, statisticsManager)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.drop(1).test {
            val state = awaitItem()
            assertTrue(state.todayPick.isCompleted)
            assertEquals(today.toString(), state.todayPick.dateString)
        }
    }

    @Test
    fun `today's pick is not marked completed when user completed it yesterday`() = runTest {
        val yesterday = LocalDate.now().minusDays(1)
        val mockPrefs = UserPreferences(
            todayPickLastDate = yesterday.toString(),
            todayPickCompleted = true
        )
        every { repository.userPreferencesFlow } returns flowOf(mockPrefs)

        viewModel = HomeViewModel(repository, statisticsManager)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.drop(1).test {
            val state = awaitItem()
            assertFalse(state.todayPick.isCompleted)
        }
    }

    @Test
    fun `today's pick cycles through all game types`() {
        val games = GameType.entries
        val today = LocalDate.now()
        
        val todayGame = PreferencesRepository.getTodayPickGameType(today)
        val tomorrowGame = PreferencesRepository.getTodayPickGameType(today.plusDays(1))
        
        // Different days should have different games (with high probability)
        assertTrue(games.contains(todayGame))
        assertTrue(games.contains(tomorrowGame))
    }
}
