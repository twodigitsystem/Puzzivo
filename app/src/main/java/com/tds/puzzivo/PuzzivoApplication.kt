package com.tds.puzzivo

import android.app.Application
import com.google.android.libraries.ads.mobile.sdk.MobileAds
import com.google.android.libraries.ads.mobile.sdk.initialization.InitializationConfig
import com.tds.puzzivo.core.ads.AdManager
import com.tds.puzzivo.core.ads.AdsConsentManager
import com.tds.puzzivo.core.manager.GameCompletionRecorder
import com.tds.puzzivo.core.analytics.AnalyticsManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltAndroidApp
class PuzzivoApplication : Application() {

    @Inject
    lateinit var adManager: AdManager

    @Inject
    lateinit var adsConsentManager: AdsConsentManager

    @Inject
    lateinit var preferencesRepository: com.tds.puzzivo.core.storage.PreferencesRepository

    @Inject
    lateinit var statisticsManager: com.tds.puzzivo.core.manager.StatisticsManager

    @Inject
    lateinit var progressionManager: com.tds.puzzivo.core.manager.ProgressionManager

    @Inject
    lateinit var completionRecorder: GameCompletionRecorder

    @Inject
    lateinit var analyticsManager: AnalyticsManager

    var adsReady: Boolean = false
        private set

    private var adsInitializationStarted = false

    private val _adsReadyFlow = MutableStateFlow(false)
    val adsReadyFlow: StateFlow<Boolean> = _adsReadyFlow.asStateFlow()

    fun initializeAds(activity: android.app.Activity) {
        if (adsInitializationStarted || adsReady) return
        adsInitializationStarted = true
        adsConsentManager.requestConsent(activity) {
            if (!adsConsentManager.canRequestAds()) {
                adsInitializationStarted = false
                return@requestConsent
            }

            try {
                val config = InitializationConfig.Builder(getString(R.string.admob_application_id)).build()
                MobileAds.initialize(this, config)
                adsReady = true
                _adsReadyFlow.value = true
                adManager.preloadRewardedAd()
            } catch (error: Exception) {
                adsInitializationStarted = false
                android.util.Log.e("PuzzivoApplication", "Mobile Ads initialization failed", error)
            }
        }
    }
}
