package com.tds.puzzivo.feature.patternrecall

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tds.puzzivo.core.ads.AdManager
import com.tds.puzzivo.core.analytics.AnalyticsManager
import com.tds.puzzivo.core.audio.SoundAndHapticsManager
import com.tds.puzzivo.core.manager.GameCompletionRecorder
import com.tds.puzzivo.core.manager.GameCompletionRequest
import com.tds.puzzivo.core.model.DifficultyLevel
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
class PatternRecallViewModel @Inject constructor(
    private val repository: PreferencesRepository,
    private val adManager: AdManager,
    private val soundAndHapticsManager: SoundAndHapticsManager,
    private val completionRecorder: GameCompletionRecorder,
    private val analyticsManager: AnalyticsManager,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val engine = PatternRecallGameEngine()

    private val _uiState = MutableStateFlow(engine.state)
    val uiState: StateFlow<PatternRecallState> = _uiState.asStateFlow()

    private val _shouldShowInstructions = MutableStateFlow(false)
    val shouldShowInstructions: StateFlow<Boolean> = _shouldShowInstructions.asStateFlow()

    private var timerJob: Job? = null
    private var playJob: Job? = null
    private var hapticsEnabled = true
    private var soundEnabled = true
    private var completionRecorded = false

    init {
        viewModelScope.launch {
            val prefs = repository.userPreferencesFlow.first()
            hapticsEnabled = prefs.hapticsEnabled
            soundEnabled = prefs.soundEnabled
            _shouldShowInstructions.value = GameType.PATTERN_RECALL.id !in prefs.seenInstructionIds

            val initialDifficulty = prefs.selectedDifficulty
            engine.startNewGame(difficulty = initialDifficulty)
            _uiState.value = engine.state
            analyticsManager.logGameStarted(GameType.PATTERN_RECALL)
            startSequencePlayback()
            startTimer()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                engine.tickTimer()
                _uiState.value = engine.state
            }
        }
    }

    private fun startSequencePlayback() {
        playJob?.cancel()
        playJob = viewModelScope.launch {
            engine.setIsShowingSequence(true)
            _uiState.value = engine.state
            delay(500)

            val seq = engine.state.sequence
            val flashDelay = when (engine.state.difficulty) {
                DifficultyLevel.EASY -> 450L
                DifficultyLevel.MEDIUM -> 350L
                DifficultyLevel.HARD, DifficultyLevel.EXPERT -> 250L
            }

            for (cell in seq) {
                engine.setLitCell(cell)
                _uiState.value = engine.state
                soundAndHapticsManager.triggerHaptic(hapticsEnabled, 15)
                delay(flashDelay)

                engine.setLitCell(null)
                _uiState.value = engine.state
                delay(100L)
            }

            engine.setIsShowingSequence(false)
            _uiState.value = engine.state
        }
    }

    fun onInstructionsDismissed() {
        _shouldShowInstructions.value = false
        viewModelScope.launch { repository.markInstructionsSeen(GameType.PATTERN_RECALL) }
    }

    fun onCellTap(cellIndex: Int) {
        if (engine.state.isShowingSequence || engine.state.isGameOver) return

        val result = engine.tapCell(cellIndex)
        _uiState.value = engine.state
        soundAndHapticsManager.triggerHaptic(hapticsEnabled, 20)

        when (result) {
            PatternTapResult.CORRECT_STEP -> {
                soundAndHapticsManager.playSuccessSound(soundEnabled)
            }
            PatternTapResult.ROUND_COMPLETE -> {
                soundAndHapticsManager.playSuccessSound(soundEnabled)
                soundAndHapticsManager.triggerHaptic(hapticsEnabled, 50)
                startSequencePlayback()
            }
            PatternTapResult.WRONG -> {
                soundAndHapticsManager.playErrorSound(soundEnabled)
                soundAndHapticsManager.triggerHaptic(hapticsEnabled, 80)
                onGameComplete()
            }
        }
    }

    fun onDifficultySelected(difficulty: DifficultyLevel) {
        viewModelScope.launch {
            repository.setSelectedDifficulty(difficulty)
            completionRecorded = false
            engine.startNewGame(difficulty = difficulty)
            _uiState.value = engine.state
            startSequencePlayback()
            startTimer()
        }
    }

    fun onRestart() {
        completionRecorded = false
        engine.startNewGame(difficulty = engine.state.difficulty)
        _uiState.value = engine.state
        startSequencePlayback()
        startTimer()
    }

    private fun onGameComplete() {
        if (completionRecorded) return
        completionRecorded = true
        timerJob?.cancel()

        viewModelScope.launch {
            val state = engine.state
            completionRecorder.record(
                GameCompletionRequest(
                    completionId = java.util.UUID.randomUUID().toString(),
                    gameType = GameType.PATTERN_RECALL,
                    score = state.score,
                    durationSeconds = state.timeElapsedSeconds,
                    won = state.round > 3,
                    moves = state.round - 1,
                    difficulty = state.difficulty
                )
            )

            analyticsManager.logGameCompleted(
                gameType = GameType.PATTERN_RECALL,
                score = state.score,
                duration = state.timeElapsedSeconds
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        playJob?.cancel()
    }
}
