package com.tds.puzzivo.feature.wordsearch

import android.app.Activity
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
class WordSearchViewModel @Inject constructor(
    private val repository: PreferencesRepository,
    private val adManager: AdManager,
    private val soundAndHapticsManager: SoundAndHapticsManager,
    private val completionRecorder: GameCompletionRecorder,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {

    private val engine = WordSearchGameEngine()

    private val _uiState = MutableStateFlow(engine.state)
    val uiState: StateFlow<WordSearchState> = _uiState.asStateFlow()

    private val _shouldShowInstructions = MutableStateFlow(false)
    val shouldShowInstructions: StateFlow<Boolean> = _shouldShowInstructions.asStateFlow()

    private var hapticsEnabled = true
    private var soundEnabled = true
    private var completionRecordedPuzzleId: String? = null

    init {
        viewModelScope.launch {
            val prefs = repository.userPreferencesFlow.first()
            hapticsEnabled = prefs.hapticsEnabled
            soundEnabled = prefs.soundEnabled
            _shouldShowInstructions.value = GameType.WORD_SEARCH.id !in prefs.seenInstructionIds
            engine.loadPuzzle(prefs.wordSearchCurrentIndex)
            _uiState.value = engine.state
            analyticsManager.logGameStarted(GameType.WORD_SEARCH)
        }
        adManager.preloadRewardedAd()
    }

    fun onInstructionsDismissed() {
        _shouldShowInstructions.value = false
        viewModelScope.launch { repository.markInstructionsSeen(GameType.WORD_SEARCH) }
    }

    fun onCellTouchStart(row: Int, col: Int) {
        engine.selectStartCell(row, col)
        _uiState.value = engine.state
    }

    fun onCellTouchMove(row: Int, col: Int) {
        engine.updateSelection(row, col)
        _uiState.value = engine.state
    }

    fun onCellTouchEnd() {
        val found = engine.confirmSelection()
        _uiState.value = engine.state

        if (found) {
            soundAndHapticsManager.playSuccessSound(soundEnabled)
            soundAndHapticsManager.triggerHaptic(hapticsEnabled, 40)
            if (engine.state.isGameComplete) {
                soundAndHapticsManager.playVictorySound(soundEnabled)
                onGameComplete()
            }
        }
    }

    /** Accessible alternative to drag selection: tap the start cell, then the end cell. */
    fun onCellTap(row: Int, col: Int) {
        if (engine.state.selectedCells.isEmpty()) {
            engine.selectStartCell(row, col)
            _uiState.value = engine.state
            return
        }

        engine.updateSelection(row, col)
        onCellTouchEnd()
    }

    fun onRevealWordWithReward(activity: Activity) {
        adManager.showRewardedAd(
            activity = activity,
            onRewardEarned = {
                val success = engine.useRevealWordHint()
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

    fun onNextPuzzle() {
        if (!engine.state.isGameComplete) return
        val puzzleCount = WordSearchRepository.puzzles.size
        val nextIndex = (engine.state.puzzleIndex + 1) % puzzleCount
        viewModelScope.launch {
            repository.saveWordSearchCurrentIndex(nextIndex)
        }
        engine.loadPuzzle(nextIndex)
        completionRecordedPuzzleId = null
        _uiState.value = engine.state
        analyticsManager.logGameStarted(GameType.WORD_SEARCH)
    }

    private fun onGameComplete() {
        val puzzle = engine.state.puzzle ?: return
        if (completionRecordedPuzzleId == puzzle.id) return
        completionRecordedPuzzleId = puzzle.id
        val puzzleIndex = engine.state.puzzleIndex
        val nextIndex = (puzzleIndex + 1) % WordSearchRepository.puzzles.size
        viewModelScope.launch {
            repository.saveWordSearchCompleted(
                puzzleId = puzzle.id,
                nextIndex = nextIndex
            )
            completionRecorder.record(
                GameCompletionRequest(
                    completionId = puzzle.id,
                    gameType = GameType.WORD_SEARCH,
                    score = engine.state.foundWords.size * 100,
                    durationSeconds = 60,
                    won = true,
                    moves = engine.state.foundWords.size,
                    hintsUsed = if (engine.state.hasUsedHint) 1 else 0
                )
            )
        }
    }

    override fun onCleared() {
        // SoundAndHapticsManager is now singleton, no need to release
    }
}
