package com.tds.puzzivo.feature.numbermerge

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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NumberMergeViewModel @Inject constructor(
    private val repository: PreferencesRepository,
    private val adManager: AdManager,
    private val soundAndHapticsManager: SoundAndHapticsManager,
    private val completionRecorder: GameCompletionRecorder,
    private val analyticsManager: AnalyticsManager,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val engine = NumberMergeGameEngine()

    private val _uiState = MutableStateFlow(engine.state)
    val uiState: StateFlow<NumberMergeState> = _uiState.asStateFlow()

    private val _shouldShowInstructions = MutableStateFlow(false)
    val shouldShowInstructions: StateFlow<Boolean> = _shouldShowInstructions.asStateFlow()

    private var hapticsEnabled = true
    private var soundEnabled = true
    private var completionRecorded = false
    private var gameSessionId = java.util.UUID.randomUUID().toString()

    init {
        viewModelScope.launch {
            val prefs = repository.userPreferencesFlow.first()
            hapticsEnabled = prefs.hapticsEnabled
            soundEnabled = prefs.soundEnabled
            _shouldShowInstructions.value = GameType.NUMBER_MERGE.id !in prefs.seenInstructionIds

            // Restore saved state if exists
            val savedGrid = savedStateHandle.get<List<Int>>("game_grid")
            val savedScore = savedStateHandle.get<Int>("game_score") ?: 0

            if (savedGrid != null && savedGrid.any { it != 0 }) {
                engine.setStateForTesting(savedGrid, savedScore, prefs.numberMergeBestScore, false)
            } else {
                engine.startNewGame(bestScore = prefs.numberMergeBestScore)
            }
            _uiState.value = engine.state
            analyticsManager.logGameStarted(GameType.NUMBER_MERGE)
        }
        adManager.preloadRewardedAd()
    }

    fun onInstructionsDismissed() {
        _shouldShowInstructions.value = false
        viewModelScope.launch { repository.markInstructionsSeen(GameType.NUMBER_MERGE) }
    }

    fun onSwipe(direction: MoveDirection) {
        val moved = engine.move(direction)
        if (moved) {
            soundAndHapticsManager.playTapSound(soundEnabled)
            soundAndHapticsManager.triggerHaptic(hapticsEnabled, 25)
            _uiState.value = engine.state
            saveGameState()

            if (engine.state.isGameOver) {
                soundAndHapticsManager.playVictorySound(soundEnabled)
                onGameOver()
            }
        }
    }

    fun onRestart() {
        completionRecorded = false
        gameSessionId = java.util.UUID.randomUUID().toString()
        viewModelScope.launch {
            val prefs = repository.userPreferencesFlow.first()
            hapticsEnabled = prefs.hapticsEnabled
            soundEnabled = prefs.soundEnabled
            engine.startNewGame(bestScore = prefs.numberMergeBestScore)
            _uiState.value = engine.state
            clearSavedState()
            analyticsManager.logGameStarted(GameType.NUMBER_MERGE)
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
                    saveGameState()
                }
            },
            onAdUnavailable = {
                // Do not grant reward on ad failure
            }
        )
    }

    private fun saveGameState() {
        savedStateHandle["game_grid"] = engine.state.grid
        savedStateHandle["game_score"] = engine.state.score
    }

    private fun clearSavedState() {
        savedStateHandle.remove<List<Int>>("game_grid")
        savedStateHandle.remove<Int>("game_score")
    }

    private fun onGameOver() {
        if (completionRecorded) return
        completionRecorded = true
        viewModelScope.launch {
            val finalScore = engine.state.score
            repository.saveNumberMergeBestScore(finalScore)
            completionRecorder.record(
                GameCompletionRequest(
                    completionId = gameSessionId,
                    gameType = GameType.NUMBER_MERGE,
                    score = finalScore,
                    durationSeconds = 0,
                    won = engine.state.hasReached2048,
                    moves = engine.state.moveCount
                )
            )
            clearSavedState()
        }
    }

    override fun onCleared() {
        // SoundAndHapticsManager is now singleton, no need to release
    }
}
