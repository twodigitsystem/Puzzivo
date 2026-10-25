package com.tds.puzzivo

import android.app.Application
import com.google.android.libraries.ads.mobile.sdk.MobileAds
import com.google.android.libraries.ads.mobile.sdk.initialization.InitializationConfig
import com.tds.puzzivo.core.ads.AdManager
import com.tds.puzzivo.core.ads.AdsConsentManager
import com.tds.puzzivo.core.storage.PreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PuzzivoApplication : Application() {

    lateinit var preferencesRepository: PreferencesRepository
        private set

    lateinit var adManager: AdManager
        private set

    lateinit var adsConsentManager: AdsConsentManager
        private set

    var adsReady: Boolean = false
        private set

    private var adsInitializationStarted = false

    private val _adsReadyFlow = MutableStateFlow(false)
    val adsReadyFlow: StateFlow<Boolean> = _adsReadyFlow.asStateFlow()

    override fun onCreate() {
        super.onCreate()
        preferencesRepository = PreferencesRepository(applicationContext)
        adsConsentManager = AdsConsentManager(applicationContext)
        adManager = AdManager(applicationContext) { adsReady && adsConsentManager.canRequestAds() }
    }

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
