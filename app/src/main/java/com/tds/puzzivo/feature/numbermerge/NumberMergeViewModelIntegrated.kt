package com.tds.puzzivo.feature.numbermerge

import android.app.Activity
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.tds.puzzivo.core.ads.AdManager
import com.tds.puzzivo.core.audio.SoundAndHapticsManager
import com.tds.puzzivo.core.game.BaseGameViewModel
import com.tds.puzzivo.core.manager.AchievementManager
import com.tds.puzzivo.core.manager.ProgressionManager
import com.tds.puzzivo.core.manager.StatisticsManager
import com.tds.puzzivo.core.model.GameType
import com.tds.puzzivo.core.network.NetworkManager
import com.tds.puzzivo.core.storage.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Example of NumberMerge integrated with universal features
 * Replace the existing NumberMergeViewModel with this implementation
 */
@HiltViewModel
class NumberMergeViewModelIntegrated @Inject constructor(
    private val repository: PreferencesRepository,
    private val adManager: AdManager,
    private val soundAndHapticsManager: SoundAndHapticsManager,
    private val savedStateHandle: SavedStateHandle,
    networkManager: NetworkManager,
    statisticsManager: StatisticsManager,
    achievementManager: AchievementManager,
    progressionManager: ProgressionManager
) : BaseGameViewModel(
    gameType = GameType.NUMBER_MERGE,
    networkManager = networkManager,
    statisticsManager = statisticsManager,
    achievementManager = achievementManager,
    progressionManager = progressionManager
) {

    private val engine = NumberMergeGameEngine()

    private val _uiState = MutableStateFlow(engine.state)
    val uiState: StateFlow<NumberMergeState> = _uiState.asStateFlow()

    private val _shouldShowInstructions = MutableStateFlow(false)
    val shouldShowInstructions: StateFlow<Boolean> = _shouldShowInstructions.asStateFlow()

    private val _showDifficultyDialog = MutableStateFlow(false)
    val showDifficultyDialog: StateFlow<Boolean> = _showDifficultyDialog.asStateFlow()

    private var hapticsEnabled = true
    private var soundEnabled = true
    private var completionRecorded = false
    private var gameStartTime = 0L
    private var moveCount = 0

    init {
        viewModelScope.launch {
            val prefs = repository.userPreferencesFlow.first()
            hapticsEnabled = prefs.hapticsEnabled
            soundEnabled = prefs.soundEnabled
            _shouldShowInstructions.value = GameType.NUMBER_MERGE.id !in prefs.seenInstructionIds
            
            // Show difficulty selection on first launch
            if (GameType.NUMBER_MERGE.id !in prefs.seenInstructionIds) {
                _showDifficultyDialog.value = true
            } else {
                startNewGameWithDifficulty()
            }
        }
        adManager.preloadRewardedAd()
    }

    fun onInstructionsDismissed() {
        _shouldShowInstructions.value = false
        viewModelScope.launch { 
            repository.markInstructionsSeen(GameType.NUMBER_MERGE)
            _showDifficultyDialog.value = true // Show difficulty after instructions
        }
    }

    fun onDifficultySelected() {
        _showDifficultyDialog.value = false
        startNewGameWithDifficulty()
    }

    private fun startNewGameWithDifficulty() {
        completionRecorded = false
        moveCount = 0
        gameStartTime = System.currentTimeMillis()
        
        viewModelScope.launch {
            val prefs = repository.userPreferencesFlow.first()
            engine.startNewGame(bestScore = prefs.numberMergeBestScore)
            _uiState.value = engine.state
            clearSavedState()
        }
    }

    fun onSwipe(direction: MoveDirection) {
        val moved = engine.move(direction)
        if (moved) {
            moveCount++
            soundAndHapticsManager.playTapSound(soundEnabled)
            soundAndHapticsManager.triggerHaptic(hapticsEnabled, 25)
            _uiState.value = engine.state
            saveGameState()

            // Check for merge 2048 achievement
            if (engine.state.grid.any { it == 2048 }) {
                viewModelScope.launch {
                    trackAchievementProgress("merge_2048", 1)
                }
            }

            if (engine.state.isGameOver) {
                soundAndHapticsManager.playVictorySound(soundEnabled)
                onGameComplete()
            }
        }
    }

    private fun onGameComplete() {
        if (!completionRecorded) {
            completionRecorded = true
            val gameTimeSeconds = (System.currentTimeMillis() - gameStartTime) / 1000
            
            viewModelScope.launch {
                // Record the game result
                recordGameResult(
                    won = true,
                    score = engine.state.score.toLong(),
                    timeInSeconds = gameTimeSeconds,
                    movesOrAttempts = moveCount,
                    hintsUsed = 0 // NumberMerge doesn't use hints
                )
                
                // Update best score in preferences
                val prefs = repository.userPreferencesFlow.first()
                if (engine.state.score > prefs.numberMergeBestScore) {
                    repository.saveNumberMergeBestScore(engine.state.score)
                }
            }
        }
    }

    fun onRestart() {
        _showDifficultyDialog.value = true // Let user choose difficulty again
    }

    fun onUndoWithReward(activity: Activity) {
        adManager.showRewardedAd(
            activity = activity,
            onRewardEarned = {
                val success = engine.undoLastMove()
                if (success) {
                    moveCount--
                    soundAndHapticsManager.playSuccessSound(soundEnabled)
                    soundAndHapticsManager.triggerHaptic(hapticsEnabled, 50)
                    _uiState.value = engine.state
                    saveGameState()
                }
            },
            onAdUnavailable = {
                // Handle ad unavailable
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
}
