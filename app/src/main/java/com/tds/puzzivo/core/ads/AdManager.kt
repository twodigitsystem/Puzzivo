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

class AdManager(
    private val context: Context,
    private val canRequestAds: () -> Boolean = { true }
) {

    private var rewardedAd: RewardedAd? = null
    private var isLoadingRewarded = false

    fun preloadRewardedAd() {
        if (!canRequestAds() || rewardedAd != null || isLoadingRewarded) return
        isLoadingRewarded = true

        val adRequest = AdRequest.Builder(AdUnitIds.getRewardedAdUnitId()).build()
        RewardedAd.load(adRequest, object : AdLoadCallback<RewardedAd> {
            override fun onAdLoaded(ad: RewardedAd) {
                rewardedAd = ad
                isLoadingRewarded = false
                Log.d("AdManager", "Rewarded ad loaded successfully.")
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                rewardedAd = null
                isLoadingRewarded = false
                Log.e("AdManager", "Rewarded ad failed to load: ${adError.message}")
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
        if (ad != null) {
            var rewardDelivered = false
            try {
                ad.show(activity) { _ ->
                    if (!rewardDelivered) {
                        rewardDelivered = true
                        onRewardEarned()
                    }
                    preloadRewardedAd()
                }
            } catch (e: Exception) {
                Log.e("AdManager", "Error showing rewarded ad", e)
                notifyUnavailable(onAdUnavailable)
                preloadRewardedAd()
            }
        } else {
            notifyUnavailable(onAdUnavailable)
            preloadRewardedAd()
        }
    }

    private fun notifyUnavailable(onAdUnavailable: () -> Unit) {
        Toast.makeText(context, "Rewarded ad unavailable. Please try again later.", Toast.LENGTH_SHORT).show()
        onAdUnavailable()
    }
}
