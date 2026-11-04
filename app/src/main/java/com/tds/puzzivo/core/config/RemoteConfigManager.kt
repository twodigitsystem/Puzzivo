package com.tds.puzzivo.core.config

import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteConfigManager @Inject constructor(
    private val remoteConfig: FirebaseRemoteConfig
) {

    private companion object {
        const val TAG = "RemoteConfigManager"
        const val KEY_XP_MULTIPLIER = "xp_multiplier"
        const val KEY_COIN_MULTIPLIER = "coin_multiplier"
        const val KEY_AD_REFRESH_INTERVAL = "ad_refresh_interval_seconds"
        const val KEY_DAILY_PICK_XP = "daily_pick_xp_reward"
        const val KEY_DAILY_PICK_COINS = "daily_pick_coin_reward"
    }

    private val _xpMultiplier = MutableStateFlow(1.0)
    val xpMultiplier: StateFlow<Double> = _xpMultiplier.asStateFlow()

    private val _coinMultiplier = MutableStateFlow(1.0)
    val coinMultiplier: StateFlow<Double> = _coinMultiplier.asStateFlow()

    private val _adRefreshIntervalSeconds = MutableStateFlow(30L)
    val adRefreshIntervalSeconds: StateFlow<Long> = _adRefreshIntervalSeconds.asStateFlow()

    init {
        val defaults = mapOf<String, Any>(
            KEY_XP_MULTIPLIER to 1.0,
            KEY_COIN_MULTIPLIER to 1.0,
            KEY_AD_REFRESH_INTERVAL to 30L,
            KEY_DAILY_PICK_XP to 50L,
            KEY_DAILY_PICK_COINS to 25L
        )
        remoteConfig.setDefaultsAsync(defaults)
    }

    fun fetchAndActivate() {
        remoteConfig.fetchAndActivate()
            .addOnSuccessListener {
                updateStateFromConfig()
                Log.d(TAG, "Remote config fetched & activated successfully")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Failed to fetch remote config, using defaults", e)
            }
    }

    private fun updateStateFromConfig() {
        _xpMultiplier.value = remoteConfig.getDouble(KEY_XP_MULTIPLIER).coerceAtLeast(0.5)
        _coinMultiplier.value = remoteConfig.getDouble(KEY_COIN_MULTIPLIER).coerceAtLeast(0.5)
        _adRefreshIntervalSeconds.value = remoteConfig.getLong(KEY_AD_REFRESH_INTERVAL).coerceIn(15L, 300L)
    }

    fun currentXpMultiplier(): Double = _xpMultiplier.value

    fun currentCoinMultiplier(): Double = _coinMultiplier.value

    fun currentAdRefreshIntervalSeconds(): Long = _adRefreshIntervalSeconds.value

    fun getDailyPickXpReward(): Long = remoteConfig.getLong(KEY_DAILY_PICK_XP).coerceAtLeast(10L)

    fun getDailyPickCoinReward(): Long = remoteConfig.getLong(KEY_DAILY_PICK_COINS).coerceAtLeast(5L)
}
