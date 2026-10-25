package com.tds.puzzivo.core.audio

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

class SoundAndHapticsManager(context: Context) {

    private val appContext = context.applicationContext

    private var vibrator: Vibrator? = null
    private var toneGenerator: ToneGenerator? = null

    init {
        // Initialize Vibrator
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

        // Initialize ToneGenerator for zero-asset sound effects
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 60)
        } catch (e: Exception) {
            Log.w(TAG, "Unable to initialize tone generator", e)
        }
    }

    fun playTapSound(enabled: Boolean) {
        if (!enabled) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 30)
        } catch (e: Exception) {
            Log.w(TAG, "Unable to play tap sound", e)
        }
    }

    fun playSuccessSound(enabled: Boolean) {
        if (!enabled) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 80)
        } catch (e: Exception) {
            Log.w(TAG, "Unable to play success sound", e)
        }
    }

    fun playErrorSound(enabled: Boolean) {
        if (!enabled) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_NACK, 100)
        } catch (e: Exception) {
            Log.w(TAG, "Unable to play error sound", e)
        }
    }

    fun playVictorySound(enabled: Boolean) {
        if (!enabled) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200)
        } catch (e: Exception) {
            Log.w(TAG, "Unable to play victory sound", e)
        }
    }

    fun triggerHaptic(enabled: Boolean, milliseconds: Long = 30) {
        if (!enabled) return
        try {
            vibrator?.let { v ->
                if (v.hasVibrator()) {
                    v.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE))
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Unable to trigger haptic feedback", e)
        }
    }

    fun release() {
        toneGenerator?.release()
        toneGenerator = null
    }

    private companion object {
        const val TAG = "SoundAndHaptics"
    }
}
