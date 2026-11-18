package com.tds.puzzivo.feature.wordsearch

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
class WordSearchViewModel @Inject constructor(
    private val repository: PreferencesRepository,
    private val adManager: AdManager,
    private val soundAndHapticsManager: SoundAndHapticsManager,
    private val completionRecorder: GameCompletionRecorder,
    private val analyticsManager: AnalyticsManager,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private companion object {
        const val KEY_PUZZLE_INDEX = "ws_puzzle_index"
        const val KEY_SCORE = "ws_score"
        const val KEY_TIME = "ws_time"
    }

    private val engine = WordSearchGameEngine()

    private val _uiState = MutableStateFlow(engine.state)
    val uiState: StateFlow<WordSearchState> = _uiState.asStateFlow()

    private val _shouldShowInstructions = MutableStateFlow(false)
    val shouldShowInstructions: StateFlow<Boolean> = _shouldShowInstructions.asStateFlow()

    private var timerJob: Job? = null
    private var hapticsEnabled = true
    private var soundEnabled = true
    private var completionRecordedPuzzleId: String? = null

    init {
        viewModelScope.launch {
            val prefs = repository.userPreferencesFlow.first()
            hapticsEnabled = prefs.hapticsEnabled
            soundEnabled = prefs.soundEnabled
            _shouldShowInstructions.value = GameType.WORD_SEARCH.id !in prefs.seenInstructionIds

            val savedIndex = savedStateHandle.get<Int>(KEY_PUZZLE_INDEX) ?: prefs.wordSearchCurrentIndex
            val savedScore = savedStateHandle.get<Int>(KEY_SCORE) ?: 0
            val savedTime = savedStateHandle.get<Int>(KEY_TIME) ?: 0

            engine.loadPuzzle(savedIndex, bestScore = 0)
            _uiState.value = engine.state
            analyticsManager.logGameStarted(GameType.WORD_SEARCH)
            startTimer()
        }
        adManager.preloadRewardedAd()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                engine.tickTimer()
                _uiState.value = engine.state
                saveStateToHandle()
            }
        }
    }

    private fun saveStateToHandle() {
        savedStateHandle[KEY_PUZZLE_INDEX] = engine.state.puzzleIndex
        savedStateHandle[KEY_SCORE] = engine.state.score
        savedStateHandle[KEY_TIME] = engine.state.timeElapsedSeconds
    }

    fun onInstructionsDismissed() {
        _shouldShowInstructions.value = false
        viewModelScope.launch { repository.markInstructionsSeen(GameType.WORD_SEARCH) }
    }

    fun onCellTouchStart(row: Int, col: Int) {
        engine.selectStartCell(row, col)
        _uiState.value = engine.state
        soundAndHapticsManager.triggerHaptic(hapticsEnabled, 10)
    }

    fun onCellTouchMove(row: Int, col: Int) {
        engine.updateSelection(row, col)
        _uiState.value = engine.state
        soundAndHapticsManager.triggerHaptic(hapticsEnabled, 5)
    }

    fun onCellTouchEnd() {
        val found = engine.confirmSelection()
        _uiState.value = engine.state
        saveStateToHandle()

        if (found) {
            soundAndHapticsManager.playSuccessSound(soundEnabled)
            soundAndHapticsManager.triggerHaptic(hapticsEnabled, 40)
            if (engine.state.isGameComplete) {
                soundAndHapticsManager.playVictorySound(soundEnabled)
                soundAndHapticsManager.triggerHaptic(hapticsEnabled, 100)
                onGameComplete()
            }
        }
    }

    fun onCellTap(row: Int, col: Int) {
        if (engine.state.selectedCells.isEmpty()) {
            engine.selectStartCell(row, col)
            _uiState.value = engine.state
            soundAndHapticsManager.triggerHaptic(hapticsEnabled, 10)
            return
        }

        engine.updateSelection(row, col)
        onCellTouchEnd()
    }

    fun onRevealFirstLetterWithReward(activity: Activity) {
        if (engine.state.isGameComplete) return
        adManager.showRewardedAd(
            activity = activity,
            onRewardEarned = {
                val success = engine.useRevealFirstLetterHint()
                if (success) {
                    soundAndHapticsManager.playSuccessSound(soundEnabled)
                    soundAndHapticsManager.triggerHaptic(hapticsEnabled, 30)
                    _uiState.value = engine.state
                }
            },
            onAdUnavailable = {
                val success = engine.useRevealFirstLetterHint()
                if (success) {
                    soundAndHapticsManager.playSuccessSound(soundEnabled)
                    soundAndHapticsManager.triggerHaptic(hapticsEnabled, 30)
                    _uiState.value = engine.state
                }
            }
        )
    }

    fun onRevealWordWithReward(activity: Activity) {
        if (engine.state.isGameComplete) return
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
                val success = engine.useRevealWordHint()
                if (success) {
                    soundAndHapticsManager.playSuccessSound(soundEnabled)
                    soundAndHapticsManager.triggerHaptic(hapticsEnabled, 50)
                    _uiState.value = engine.state
                }
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
        saveStateToHandle()
        analyticsManager.logGameStarted(GameType.WORD_SEARCH)
    }

    private fun onGameComplete() {
        val puzzle = engine.state.puzzle ?: return
        if (completionRecordedPuzzleId == puzzle.id) return
        completionRecordedPuzzleId = puzzle.id
        timerJob?.cancel()

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
                    score = engine.state.score,
                    durationSeconds = engine.state.timeElapsedSeconds,
                    won = true,
                    moves = engine.state.foundWords.size,
                    hintsUsed = engine.state.hintsUsed
                )
            )
        }
    }

    override fun onCleared() {
        timerJob?.cancel()
    }
}
