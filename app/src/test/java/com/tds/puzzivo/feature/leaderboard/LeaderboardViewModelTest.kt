package com.tds.puzzivo.feature.leaderboard

import app.cash.turbine.test
import com.tds.puzzivo.core.auth.AuthManager
import com.tds.puzzivo.core.manager.LeaderboardManager
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LeaderboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var leaderboardManager: LeaderboardManager
    private lateinit var authManager: AuthManager

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        leaderboardManager = mockk(relaxed = true)
        authManager = mockk(relaxed = true)

        every { authManager.currentUser } returns MutableStateFlow(null)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun isSignedInEmitsFalseWhenUserIsNull() = runTest {
        val viewModel = LeaderboardViewModel(leaderboardManager, authManager)

        viewModel.isSignedIn.test {
            assertEquals(false, awaitItem())
        }
    }
}
