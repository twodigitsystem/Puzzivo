package com.tds.puzzivo.core.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.FormError
import com.google.android.ump.UserMessagingPlatform

/**
 * Owns the UMP consent lifecycle. Ads are requested only after this manager
 * reports that the current device is allowed to request them.
 */
class AdsConsentManager(context: Context) {

    private val applicationContext = context.applicationContext
    private val consentInformation = UserMessagingPlatform.getConsentInformation(applicationContext)

    fun requestConsent(activity: Activity, onComplete: () -> Unit) {
        val parameters = ConsentRequestParameters.Builder().build()

        consentInformation.requestConsentInfoUpdate(
            activity,
            parameters,
            {
                if (consentInformation.isConsentFormAvailable) {
                    UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { error ->
                        if (error != null) {
                            Log.w(TAG, "Consent form was not shown: ${error.message}")
                        }
                        onComplete()
                    }
                } else {
                    onComplete()
                }
            },
            { error ->
                Log.w(TAG, "Consent information update failed: ${error.message}")
                onComplete()
            }
        )
    }

    fun canRequestAds(): Boolean = consentInformation.canRequestAds()

    fun isPrivacyOptionsRequired(): Boolean =
        consentInformation.privacyOptionsRequirementStatus ==
            com.google.android.ump.ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

    fun showPrivacyOptions(activity: Activity, onComplete: () -> Unit = {}) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { error: FormError? ->
            if (error != null) {
                Log.w(TAG, "Privacy options form failed: ${error.message}")
            }
            onComplete()
        }
    }

    companion object {
        private const val TAG = "AdsConsentManager"
    }
}
