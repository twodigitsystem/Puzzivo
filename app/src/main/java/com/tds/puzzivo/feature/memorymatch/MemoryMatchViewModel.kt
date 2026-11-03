package com.tds.puzzivo.feature.memorymatch

import android.app.Activity
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tds.puzzivo.core.ads.AdManager
import com.tds.puzzivo.core.analytics.AnalyticsManager
import com.tds.puzzivo.core.audio.SoundAndHapticsManager
import com.tds.puzzivo.core.manager.GameCompletionRecorder
import com.tds.puzzivo.core.manager.GameCompletionRequest
import com.tds.puzzivo.core.model.GameType
import com.tds.puzzivo.core.storage.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MemoryMatchViewModel @Inject constructor(
    private val repository: PreferencesRepository,
    private val adManager: AdManager,
    private val soundAndHapticsManager: SoundAndHapticsManager,
    private val completionRecorder: GameCompletionRecorder,
    private val analyticsManager: AnalyticsManager,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val engine = MemoryMatchGameEngine()

    private val _uiState = MutableStateFlow(engine.state)
    val uiState: StateFlow<MemoryMatchState> = _uiState.asStateFlow()

    private val _shouldShowInstructions = MutableStateFlow(false)
    val shouldShowInstructions: StateFlow<Boolean> = _shouldShowInstructions.asStateFlow()

    private var timerJob: Job? = null
    private var mismatchJob: Job? = null
    private var hintJob: Job? = null
    private var completionRecorded = false
    private var hapticsEnabled = true
    private var soundEnabled = true
    private var gameSessionId = java.util.UUID.randomUUID().toString()

    init {
        viewModelScope.launch {
            val prefs = repository.userPreferencesFlow.first()
            hapticsEnabled = prefs.hapticsEnabled
            soundEnabled = prefs.soundEnabled
            _shouldShowInstructions.value = GameType.MEMORY_MATCH.id !in prefs.seenInstructionIds

            // Restore saved elapsed time if exists
            val savedElapsedSeconds = savedStateHandle.get<Int>("elapsed_seconds") ?: 0

            engine.startNewGame(
                bestMoves = prefs.memoryMatchBestMoves,
                bestTimeSeconds = prefs.memoryMatchBestTime
            )

            if (savedElapsedSeconds > 0) {
                engine.updateTimer(savedElapsedSeconds)
                startTimer(initialSeconds = savedElapsedSeconds)
            }

            _uiState.value = engine.state
            analyticsManager.logGameStarted(GameType.MEMORY_MATCH)
        }
        adManager.preloadRewardedAd()
    }

    fun onInstructionsDismissed() {
        _shouldShowInstructions.value = false
        viewModelScope.launch { repository.markInstructionsSeen(GameType.MEMORY_MATCH) }
    }

    fun onCardClick(index: Int) {
        val previousState = engine.state
        val mismatchEvent = engine.flipCard(index)
        if (engine.state == previousState) return

        _uiState.value = engine.state

        soundAndHapticsManager.playTapSound(soundEnabled)
        soundAndHapticsManager.triggerHaptic(hapticsEnabled, 20)

        // Start timer if first move
        if (engine.state.isGameStarted && timerJob == null) {
            startTimer()
        }

        if (mismatchEvent != null) {
            soundAndHapticsManager.playErrorSound(soundEnabled)
            mismatchJob?.cancel()
            mismatchJob = viewModelScope.launch {
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
                    hintJob?.cancel()
                    hintJob = viewModelScope.launch {
                        delay(1200)
                        engine.clearHint()
                        _uiState.value = engine.state
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
        mismatchJob?.cancel()
        mismatchJob = null
        hintJob?.cancel()
        hintJob = null
        completionRecorded = false
        gameSessionId = java.util.UUID.randomUUID().toString()
        clearSavedState()

        viewModelScope.launch {
            val prefs = repository.userPreferencesFlow.first()
            hapticsEnabled = prefs.hapticsEnabled
            soundEnabled = prefs.soundEnabled
            engine.startNewGame(
                bestMoves = prefs.memoryMatchBestMoves,
                bestTimeSeconds = prefs.memoryMatchBestTime
            )
            _uiState.value = engine.state
            analyticsManager.logGameStarted(GameType.MEMORY_MATCH)
        }
    }

    private fun startTimer(initialSeconds: Int = 0) {
        timerJob = viewModelScope.launch {
            var seconds = initialSeconds
            while (true) {
                delay(1000)
                seconds++
                engine.updateTimer(seconds)
                _uiState.value = engine.state
                savedStateHandle["elapsed_seconds"] = seconds
            }
        }
    }

    private fun clearSavedState() {
        savedStateHandle.remove<Int>("elapsed_seconds")
    }

    private fun onGameVictory() {
        if (completionRecorded) return
        completionRecorded = true
        timerJob?.cancel()
        timerJob = null
        clearSavedState()

        viewModelScope.launch {
            val moves = engine.state.moves
            val time = engine.state.elapsedSeconds
            repository.saveMemoryMatchBest(moves, time)
            completionRecorder.record(
                GameCompletionRequest(
                    completionId = gameSessionId,
                    gameType = GameType.MEMORY_MATCH,
                    score = (1000 - moves * 10).coerceAtLeast(0),
                    durationSeconds = time,
                    won = true,
                    moves = moves,
                    hintsUsed = if (engine.state.hasUsedHint) 1 else 0
                )
            )
        }
    }

    override fun onCleared() {
        timerJob?.cancel()
        mismatchJob?.cancel()
        hintJob?.cancel()
        // SoundAndHapticsManager is now singleton, no need to release
    }
}
