package com.tds.puzzivo.feature.challenge

import app.cash.turbine.test
import com.tds.puzzivo.core.manager.ChallengeManager
import com.tds.puzzivo.core.model.DailyChallenge
import com.tds.puzzivo.core.model.GameType
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
class DailyChallengeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var challengeManager: ChallengeManager

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        challengeManager = mockk(relaxed = true)
        val challenge = DailyChallenge(
            id = "test_challenge",
            date = "2026-01-01",
            gameType = GameType.QUICK_MATH,
            challengeTypeId = "quick_math_score",
            descriptionRes = 0,
            requirement = 100,
            rewardCoins = 25,
            rewardXP = 50,
            isCompleted = false
        )
        every { challengeManager.observeTodayChallenge() } returns flowOf(challenge)
        every { challengeManager.completedChallenges } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun todayChallengeEmitsCorrectChallenge() = runTest {
        val viewModel = DailyChallengeViewModel(challengeManager)

        viewModel.todayChallenge.test {
            val challenge = awaitItem()
            assertEquals(GameType.QUICK_MATH, challenge?.gameType)
            assertEquals(50, challenge?.rewardXP)
        }
    }
}
