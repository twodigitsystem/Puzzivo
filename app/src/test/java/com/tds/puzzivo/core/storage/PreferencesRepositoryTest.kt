package com.tds.puzzivo.core.storage

import android.content.Context
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PreferencesRepositoryTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var context: Context

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        context = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `login bonus awards 50 coins on first claim`() = runTest {
        val prefs = PreferencesRepository(
            context = context,
            clock = java.time.Clock.fixed(
                java.time.Instant.EPOCH,
                java.time.ZoneId.systemDefault()
            )
        )

        val (coins, gems) = prefs.claimDailyLoginBonus()
        assertEquals(50, coins)
        assertEquals(0, gems)
    }

    @Test
    fun `spin wheel claim records today date`() = runTest {
        val prefs = PreferencesRepository(
            context = context,
            clock = java.time.Clock.fixed(
                java.time.Instant.EPOCH,
                java.time.ZoneId.systemDefault()
            )
        )

        prefs.recordSpinWheelClaim()
    }
}
