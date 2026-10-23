package com.tds.puzzivo.feature.memorymatch

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tds.puzzivo.PuzzivoApplication
import com.tds.puzzivo.core.ads.AdManager
import com.tds.puzzivo.core.audio.SoundAndHapticsManager
import com.tds.puzzivo.core.model.GameType
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MemoryMatchViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as PuzzivoApplication).preferencesRepository
    private val adManager = AdManager(application)
    private val soundAndHapticsManager = SoundAndHapticsManager(application)

    private val engine = MemoryMatchGameEngine()

    private val _uiState = MutableStateFlow(engine.state)
    val uiState: StateFlow<MemoryMatchState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var hapticsEnabled = true
    private var soundEnabled = true

    init {
        viewModelScope.launch {
            val prefs = repository.userPreferencesFlow.first()
            hapticsEnabled = prefs.hapticsEnabled
            soundEnabled = prefs.soundEnabled
            engine.startNewGame(
                bestMoves = prefs.memoryMatchBestMoves,
                bestTimeSeconds = prefs.memoryMatchBestTime
            )
            _uiState.value = engine.state
        }
        adManager.preloadRewardedAd()
    }

    fun onCardClick(index: Int) {
        val mismatchEvent = engine.flipCard(index)
        _uiState.value = engine.state

        soundAndHapticsManager.playTapSound(soundEnabled)
        soundAndHapticsManager.triggerHaptic(hapticsEnabled, 20)

        // Start timer if first move
        if (engine.state.isGameStarted && timerJob == null) {
            startTimer()
        }

        if (mismatchEvent != null) {
            soundAndHapticsManager.playErrorSound(soundEnabled)
            viewModelScope.launch {
                delay(800)
                engine.clearMismatch(mismatchEvent.firstIndex, mismatchEvent.secondIndex)
                _uiState.value = engine.state
            }
        } else if (engine.state.flippedCardIndices.isEmpty() && engine.state.cards.getOrNull(index)?.isMatched == true) {
            soundAndHapticsManager.playSuccessSound(soundEnabled)
        }

        if (engine.state.isGameComplete) {
            soundAndHapticsManager.playVictorySound(soundEnabled)
            onGameVictory()
        }
    }

    fun onRevealPairHintWithReward(activity: Activity) {
        adManager.showRewardedAd(
            activity = activity,
            onRewardEarned = {
                val success = engine.useRevealPairHint()
                if (success) {
                    soundAndHapticsManager.playSuccessSound(soundEnabled)
                    soundAndHapticsManager.triggerHaptic(hapticsEnabled, 50)
                    _uiState.value = engine.state
                    if (engine.state.isGameComplete) {
                        soundAndHapticsManager.playVictorySound(soundEnabled)
                        onGameVictory()
                    }
                }
            },
            onAdUnavailable = {
                // Do not grant reward on ad failure
            }
        )
    }

    fun onRestart() {
        timerJob?.cancel()
        timerJob = null

        viewModelScope.launch {
            val prefs = repository.userPreferencesFlow.first()
            hapticsEnabled = prefs.hapticsEnabled
            soundEnabled = prefs.soundEnabled
            engine.startNewGame(
                bestMoves = prefs.memoryMatchBestMoves,
                bestTimeSeconds = prefs.memoryMatchBestTime
            )
            _uiState.value = engine.state
        }
    }

    private fun startTimer() {
        timerJob = viewModelScope.launch {
            var seconds = 0
            while (true) {
                delay(1000)
                seconds++
                engine.updateTimer(seconds)
                _uiState.value = engine.state
            }
        }
    }

    private fun onGameVictory() {
        timerJob?.cancel()
        timerJob = null

        viewModelScope.launch {
            val moves = engine.state.moves
            val time = engine.state.elapsedSeconds
            repository.saveMemoryMatchBest(moves, time)
            repository.recordGameCompleted(GameType.MEMORY_MATCH)
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundAndHapticsManager.release()
    }
}
