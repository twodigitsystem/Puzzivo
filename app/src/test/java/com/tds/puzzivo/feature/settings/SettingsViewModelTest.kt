package com.tds.puzzivo.feature.settings

import app.cash.turbine.test
import com.tds.puzzivo.core.storage.PreferencesRepository
import com.tds.puzzivo.core.storage.UserPreferences
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private lateinit var repository: PreferencesRepository
    private lateinit var viewModel: SettingsViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
    }

    @Test
    fun `user preferences flow is exposed correctly`() = runTest {
        val mockPrefs = UserPreferences(
            soundEnabled = true,
            hapticsEnabled = false
        )
        every { repository.userPreferencesFlow } returns flowOf(mockPrefs)

        viewModel = SettingsViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.userPreferences.drop(1).test {
            val prefs = awaitItem()
            assertTrue(prefs.soundEnabled)
            assertFalse(prefs.hapticsEnabled)
        }
    }

    @Test
    fun `toggleSound calls repository with correct value`() = runTest {
        every { repository.userPreferencesFlow } returns flowOf(UserPreferences())
        viewModel = SettingsViewModel(repository)

        viewModel.toggleSound(false)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repository.setSoundEnabled(false) }
    }

    @Test
    fun `toggleHaptics calls repository with correct value`() = runTest {
        every { repository.userPreferencesFlow } returns flowOf(UserPreferences())
        viewModel = SettingsViewModel(repository)

        viewModel.toggleHaptics(true)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repository.setHapticsEnabled(true) }
    }

    @Test
    fun `clearAllProgress calls repository clearAllProgress`() = runTest {
        every { repository.userPreferencesFlow } returns flowOf(UserPreferences())
        viewModel = SettingsViewModel(repository)

        viewModel.clearAllProgress()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repository.clearAllProgress() }
    }
}
