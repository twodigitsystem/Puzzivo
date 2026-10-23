package com.tds.puzzivo.feature.numbermerge

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tds.puzzivo.PuzzivoApplication
import com.tds.puzzivo.core.ads.AdManager
import com.tds.puzzivo.core.audio.SoundAndHapticsManager
import com.tds.puzzivo.core.model.GameType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class NumberMergeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as PuzzivoApplication).preferencesRepository
    private val adManager = AdManager(application)
    private val soundAndHapticsManager = SoundAndHapticsManager(application)

    private val engine = NumberMergeGameEngine()

    private val _uiState = MutableStateFlow(engine.state)
    val uiState: StateFlow<NumberMergeState> = _uiState.asStateFlow()

    private var hapticsEnabled = true
    private var soundEnabled = true

    init {
        viewModelScope.launch {
            val prefs = repository.userPreferencesFlow.first()
            hapticsEnabled = prefs.hapticsEnabled
            soundEnabled = prefs.soundEnabled
            engine.startNewGame(bestScore = prefs.numberMergeBestScore)
            _uiState.value = engine.state
        }
        adManager.preloadRewardedAd()
    }

    fun onSwipe(direction: MoveDirection) {
        val moved = engine.move(direction)
        if (moved) {
            soundAndHapticsManager.playTapSound(soundEnabled)
            soundAndHapticsManager.triggerHaptic(hapticsEnabled, 25)
            _uiState.value = engine.state

            if (engine.state.isGameOver) {
                soundAndHapticsManager.playVictorySound(soundEnabled)
                onGameOver()
            }
        }
    }

    fun onRestart() {
        viewModelScope.launch {
            val prefs = repository.userPreferencesFlow.first()
            hapticsEnabled = prefs.hapticsEnabled
            soundEnabled = prefs.soundEnabled
            engine.startNewGame(bestScore = prefs.numberMergeBestScore)
            _uiState.value = engine.state
        }
    }

    fun onUndoWithReward(activity: Activity) {
        adManager.showRewardedAd(
            activity = activity,
            onRewardEarned = {
                val success = engine.undoLastMove()
                if (success) {
                    soundAndHapticsManager.playSuccessSound(soundEnabled)
                    soundAndHapticsManager.triggerHaptic(hapticsEnabled, 50)
                    _uiState.value = engine.state
                }
            },
            onAdUnavailable = {
                // Do not grant reward on ad failure
            }
        )
    }

    private fun onGameOver() {
        viewModelScope.launch {
            val finalScore = engine.state.score
            repository.saveNumberMergeBestScore(finalScore)
            repository.recordGameCompleted(GameType.NUMBER_MERGE)
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundAndHapticsManager.release()
    }
}
