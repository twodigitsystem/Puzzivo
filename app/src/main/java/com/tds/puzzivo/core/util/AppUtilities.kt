package com.tds.puzzivo.core.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.review.ReviewManagerFactory
import androidx.core.net.toUri

object AppUtilities {

    const val PRIVACY_POLICY_URL = "https://puzzivo.app/privacy"
    const val SUPPORT_EMAIL = "support@puzzivo.app"
    private const val PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=com.tds.puzzivo"
    private const val MARKET_URL = "market://details?id=com.tds.puzzivo"

    fun shareApp(context: Context) {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Puzzivo - Mini Brain Games")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Check out Puzzivo — 4 quick, offline brain and puzzle games in one app! Download now: $PLAY_STORE_URL"
                )
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Puzzivo via"))
        } catch (e: Exception) {
            Log.e("AppUtilities", "Error sharing app", e)
        }
    }

    fun rateApp(activity: Activity) {
        val reviewManager = ReviewManagerFactory.create(activity)
        val request = reviewManager.requestReviewFlow()

        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val reviewInfo = task.result
                val flow = reviewManager.launchReviewFlow(activity, reviewInfo)
                flow.addOnCompleteListener {
                    Log.d("AppUtilities", "In-app review flow completed.")
                }
            } else {
                // Fallback to opening Play Store page directly
                openPlayStorePage(activity)
            }
        }
    }

    fun openPlayStorePage(context: Context) {
        try {
            val marketIntent = Intent(Intent.ACTION_VIEW, MARKET_URL.toUri()).apply {
                addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            }
            context.startActivity(marketIntent)
        } catch (e: Exception) {
            val webIntent = Intent(Intent.ACTION_VIEW, PLAY_STORE_URL.toUri())
            try {
                context.startActivity(webIntent)
            } catch (webError: Exception) {
                Log.e("AppUtilities", "Unable to open Play Store", webError)
            }
        }
    }

    fun checkForAppUpdates(activity: Activity) {
        try {
            val appUpdateManager = AppUpdateManagerFactory.create(activity)
            lateinit var updateListener: InstallStateUpdatedListener
            updateListener = InstallStateUpdatedListener { state ->
                if (state.installStatus() == InstallStatus.DOWNLOADED) {
                    appUpdateManager.unregisterListener(updateListener)
                    appUpdateManager.completeUpdate()
                }
            }
            val appUpdateInfoTask = appUpdateManager.appUpdateInfo

            appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                    appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
                ) {
                    appUpdateManager.registerListener(updateListener)
                    appUpdateManager.startUpdateFlow(
                        appUpdateInfo,
                        activity,
                        AppUpdateOptions.defaultOptions(AppUpdateType.FLEXIBLE)
                    )
                } else {
                    appUpdateManager.unregisterListener(updateListener)
                }
            }
            appUpdateInfoTask.addOnFailureListener {
                appUpdateManager.unregisterListener(updateListener)
            }
        } catch (e: Exception) {
            Log.e("AppUtilities", "Error checking for app updates", e)
        }
    }
}
