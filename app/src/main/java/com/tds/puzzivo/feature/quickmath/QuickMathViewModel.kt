package com.tds.puzzivo.feature.quickmath

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tds.puzzivo.PuzzivoApplication
import com.tds.puzzivo.core.audio.SoundAndHapticsManager
import com.tds.puzzivo.core.model.GameType
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class QuickMathViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as PuzzivoApplication).preferencesRepository
    private val adManager = (application as PuzzivoApplication).adManager
    private val soundAndHapticsManager = SoundAndHapticsManager(application)

    private val engine = QuickMathGameEngine()

    private val _uiState = MutableStateFlow(engine.state)
    val uiState: StateFlow<QuickMathState> = _uiState.asStateFlow()

    private val _shouldShowInstructions = MutableStateFlow(false)
    val shouldShowInstructions: StateFlow<Boolean> = _shouldShowInstructions.asStateFlow()

    private var timerJob: Job? = null
    private var completionRecorded = false
    private var hapticsEnabled = true
    private var soundEnabled = true

    init {
        viewModelScope.launch {
            val prefs = repository.userPreferencesFlow.first()
            hapticsEnabled = prefs.hapticsEnabled
            soundEnabled = prefs.soundEnabled
            _shouldShowInstructions.value = GameType.QUICK_MATH.id !in prefs.seenInstructionIds
            engine.startNewGame(bestScore = prefs.quickMathBestScore)
            _uiState.value = engine.state
            startTimer()
        }
        adManager.preloadRewardedAd()
    }

    fun onInstructionsDismissed() {
        _shouldShowInstructions.value = false
        viewModelScope.launch { repository.markInstructionsSeen(GameType.QUICK_MATH) }
    }

    fun onAnswerClick(selectedOption: Int) {
        if (engine.state.isPaused || engine.state.isGameComplete) return
        val isCorrect = engine.answerQuestion(selectedOption)
        _uiState.value = engine.state

        if (isCorrect) {
            soundAndHapticsManager.playSuccessSound(soundEnabled)
            soundAndHapticsManager.triggerHaptic(hapticsEnabled, 25)
        } else {
            soundAndHapticsManager.playErrorSound(soundEnabled)
            soundAndHapticsManager.triggerHaptic(hapticsEnabled, 50)
        }
    }

    fun onAdd15SecondsWithReward(activity: Activity) {
        adManager.showRewardedAd(
            activity = activity,
            onRewardEarned = {
                val success = engine.add15SecondsHint()
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

    fun onRestart() {
        timerJob?.cancel()
        completionRecorded = false
        viewModelScope.launch {
            val prefs = repository.userPreferencesFlow.first()
            hapticsEnabled = prefs.hapticsEnabled
            soundEnabled = prefs.soundEnabled
            engine.startNewGame(bestScore = prefs.quickMathBestScore)
            _uiState.value = engine.state
            startTimer()
        }
    }

    fun togglePause() {
        engine.setPaused(!engine.state.isPaused)
        _uiState.value = engine.state
    }

    fun pauseForLifecycle() {
        if (engine.state.isGameStarted && !engine.state.isGameComplete) {
            engine.setPaused(true)
            _uiState.value = engine.state
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                engine.tickTimer()
                _uiState.value = engine.state

                if (engine.state.isGameComplete) {
                    soundAndHapticsManager.playVictorySound(soundEnabled)
                    onGameOver()
                    break
                }
            }
        }
    }

    private fun onGameOver() {
        if (completionRecorded) return
        completionRecorded = true
        timerJob?.cancel()
        viewModelScope.launch {
            val finalScore = engine.state.score
            repository.saveQuickMathBestScore(finalScore)
            repository.recordGameCompleted(GameType.QUICK_MATH)
        }
    }

    override fun onCleared() {
        timerJob?.cancel()
        soundAndHapticsManager.release()
    }
}
