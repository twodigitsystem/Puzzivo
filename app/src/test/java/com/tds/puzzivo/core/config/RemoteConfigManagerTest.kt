package com.tds.puzzivo.core.config

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class RemoteConfigManagerTest {

    private lateinit var remoteConfig: FirebaseRemoteConfig
    private lateinit var remoteConfigManager: RemoteConfigManager

    @Before
    fun setUp() {
        remoteConfig = mockk(relaxed = true)
        every { remoteConfig.getDouble("xp_multiplier") } returns 1.5
        every { remoteConfig.getDouble("coin_multiplier") } returns 2.0
        every { remoteConfig.getLong("ad_refresh_interval_seconds") } returns 45L
        every { remoteConfig.getLong("daily_pick_xp_reward") } returns 100L
        every { remoteConfig.getLong("daily_pick_coin_reward") } returns 50L

        remoteConfigManager = RemoteConfigManager(remoteConfig)
    }

    @Test
    fun defaultMultipliersAreInitialOnePointZero() {
        assertEquals(1.0, remoteConfigManager.currentXpMultiplier(), 0.01)
        assertEquals(1.0, remoteConfigManager.currentCoinMultiplier(), 0.01)
        assertEquals(30L, remoteConfigManager.currentAdRefreshIntervalSeconds())
    }

    @Test
    fun fetchAndActivateInvocations() {
        remoteConfigManager.fetchAndActivate()
        verify { remoteConfig.fetchAndActivate() }
    }
}
