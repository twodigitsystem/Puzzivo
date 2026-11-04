package com.tds.puzzivo.core.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardedAd
import com.tds.puzzivo.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

object AdUnitIds {
    const val BANNER_TEST_ID = "ca-app-pub-3940256099942544/6300978111"
    const val REWARDED_TEST_ID = "ca-app-pub-3940256099942544/5224354917"

    // Set production AdMob Ad Unit IDs before store submission:
    const val BANNER_PRODUCTION_ID = "ca-app-pub-7375786078266181/1319206044"
    const val REWARDED_PRODUCTION_ID = "ca-app-pub-7375786078266181/2712178761"

    fun getBannerAdUnitId(): String {
        return if (BuildConfig.DEBUG) BANNER_TEST_ID else BANNER_PRODUCTION_ID
    }

    fun getRewardedAdUnitId(): String {
        return if (BuildConfig.DEBUG) REWARDED_TEST_ID else REWARDED_PRODUCTION_ID
    }
}

sealed class AdLoadState {
    object Idle : AdLoadState()
    object Loading : AdLoadState()
    object Loaded : AdLoadState()
    data class Error(val message: String) : AdLoadState()
}

@Singleton
class AdManager @Inject constructor(
    private val context: Context,
    private val remoteConfigManager: com.tds.puzzivo.core.config.RemoteConfigManager,
    private val canRequestAds: () -> Boolean = { true }
) {

    private var rewardedAd: RewardedAd? = null
    private var isLoadingRewarded = false
    private var refreshJob: Job? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _adLoadState = MutableStateFlow<AdLoadState>(AdLoadState.Idle)
    val adLoadState: StateFlow<AdLoadState> = _adLoadState.asStateFlow()

    init {
        startAdRefreshCycle()
    }

    /**
     * Refresh ad based on Remote Config refresh interval
     */
    private fun startAdRefreshCycle() {
        refreshJob?.cancel()
        refreshJob = scope.launch {
            while (true) {
                val refreshIntervalMs = remoteConfigManager.currentAdRefreshIntervalSeconds() * 1000L
                delay(refreshIntervalMs)
                if (rewardedAd == null && !isLoadingRewarded && canRequestAds()) {
                    preloadRewardedAd()
                }
            }
        }
    }

    fun preloadRewardedAd() {
        if (!canRequestAds() || rewardedAd != null || isLoadingRewarded) return
        isLoadingRewarded = true
        _adLoadState.value = AdLoadState.Loading

        val adRequest = AdRequest.Builder(AdUnitIds.getRewardedAdUnitId()).build()
        RewardedAd.load(adRequest, object : AdLoadCallback<RewardedAd> {
            override fun onAdLoaded(ad: RewardedAd) {
                rewardedAd = ad
                isLoadingRewarded = false
                _adLoadState.value = AdLoadState.Loaded
                Log.d(TAG, "Rewarded ad loaded successfully.")
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                rewardedAd = null
                isLoadingRewarded = false
                _adLoadState.value = AdLoadState.Error(adError.message)
                Log.e(TAG, "Rewarded ad failed to load: ${adError.message}")
            }
        })
    }

    fun showRewardedAd(
        activity: Activity,
        onRewardEarned: () -> Unit,
        onAdUnavailable: () -> Unit
    ) {
        if (!canRequestAds()) {
            notifyUnavailable(onAdUnavailable)
            return
        }

        val ad = rewardedAd
        rewardedAd = null
        _adLoadState.value = AdLoadState.Idle

        if (ad != null) {
            var rewardDelivered = false
            try {
                ad.show(activity) { _ ->
                    if (!rewardDelivered) {
                        rewardDelivered = true
                        onRewardEarned()
                    }
                    // Preload next ad after showing
                    scope.launch {
                        delay(1000) // Small delay before next load
                        preloadRewardedAd()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error showing rewarded ad", e)
                _adLoadState.value = AdLoadState.Error(e.message ?: "Unknown error")
                notifyUnavailable(onAdUnavailable)
                preloadRewardedAd()
            }
        } else {
            notifyUnavailable(onAdUnavailable)
            preloadRewardedAd()
        }
    }

    fun isAdReady(): Boolean = rewardedAd != null

    private fun notifyUnavailable(onAdUnavailable: () -> Unit) {
        Toast.makeText(context, "Rewarded ad unavailable. Please try again later.", Toast.LENGTH_SHORT).show()
        onAdUnavailable()
    }

    fun cleanup() {
        refreshJob?.cancel()
        refreshJob = null
    }

    companion object {
        private const val TAG = "AdManager"
    }
}
