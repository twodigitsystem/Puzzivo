package com.tds.puzzivo

import android.app.Application
import com.google.android.libraries.ads.mobile.sdk.MobileAds
import com.google.android.libraries.ads.mobile.sdk.initialization.InitializationConfig
import com.tds.puzzivo.core.storage.PreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PuzzivoApplication : Application() {

    lateinit var preferencesRepository: PreferencesRepository
        private set

    override fun onCreate() {
        super.onCreate()
        preferencesRepository = PreferencesRepository(applicationContext)

        // Initialize Next-Gen Google Mobile Ads SDK on background thread to prevent ANRs
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val config = InitializationConfig.Builder("ca-app-pub-7375786078266181~7139790190").build()
                MobileAds.initialize(this@PuzzivoApplication, config)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
