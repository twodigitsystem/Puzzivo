package com.tds.puzzivo.core.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.tds.puzzivo.core.model.GameType
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Centralized analytics manager for tracking user events.
 * All analytics events should go through this class for consistency.
 */
@Singleton
class AnalyticsManager @Inject constructor(
    private val firebaseAnalytics: FirebaseAnalytics
) {

    fun logGameStarted(gameType: GameType) {
        logEvent("game_started") {
            putString("game_type", gameType.id)
            putString("game_name", gameType.name)
        }
    }

    fun logGameCompleted(gameType: GameType, score: Int, duration: Int? = null) {
        logEvent("game_completed") {
            putString("game_type", gameType.id)
            putInt("score", score)
            duration?.let { putInt("duration_seconds", it) }
        }
    }

    fun logGameAbandoned(gameType: GameType, currentScore: Int) {
        logEvent("game_abandoned") {
            putString("game_type", gameType.id)
            putInt("score_at_abandon", currentScore)
        }
    }

    fun logStreakMilestone(days: Int) {
        logEvent("streak_milestone") {
            putInt("streak_days", days)
            putString("milestone_type", when {
                days >= 30 -> "month"
                days >= 7 -> "week"
                days >= 3 -> "three_day"
                else -> "daily"
            })
        }
    }

    fun logAdWatched(adType: String, context: String) {
        logEvent("ad_watched") {
            putString("ad_type", adType)
            putString("ad_context", context)
        }
    }

    fun logAdFailed(adType: String, errorMessage: String?) {
        logEvent("ad_failed") {
            putString("ad_type", adType)
            errorMessage?.let { putString("error", it) }
        }
    }

    fun logHintUsed(gameType: GameType, hintType: String) {
        logEvent("hint_used") {
            putString("game_type", gameType.id)
            putString("hint_type", hintType)
        }
    }

    fun logSettingsChanged(setting: String, newValue: String) {
        logEvent("settings_changed") {
            putString("setting_name", setting)
            putString("new_value", newValue)
        }
    }

    fun logShareApp() {
        logEvent("share_app") {}
    }

    fun logRateApp() {
        logEvent("rate_app_clicked") {}
    }

    fun logTodayPickPlayed(gameType: GameType) {
        logEvent("today_pick_played") {
            putString("game_type", gameType.id)
        }
    }

    fun setUserId(userId: String?) {
        firebaseAnalytics.setUserId(userId)
    }

    fun setUserProperty(name: String, value: String?) {
        firebaseAnalytics.setUserProperty(name, value)
    }

    private inline fun logEvent(eventName: String, block: Bundle.() -> Unit = {}) {
        val bundle = Bundle().apply(block)
        firebaseAnalytics.logEvent(eventName, bundle)
    }
}
