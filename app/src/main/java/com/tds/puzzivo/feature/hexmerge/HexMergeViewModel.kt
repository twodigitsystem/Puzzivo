package com.tds.puzzivo.feature.hexmerge

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
class HexMergeViewModel @Inject constructor(
    private val repository: PreferencesRepository,
    private val adManager: AdManager,
    private val soundAndHapticsManager: SoundAndHapticsManager,
    private val completionRecorder: GameCompletionRecorder,
    private val analyticsManager: AnalyticsManager,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val engine = HexMergeGameEngine()

    private val _uiState = MutableStateFlow(engine.state)
    val uiState: StateFlow<HexMergeState> = _uiState.asStateFlow()

    private val _shouldShowInstructions = MutableStateFlow(false)
    val shouldShowInstructions: StateFlow<Boolean> = _shouldShowInstructions.asStateFlow()

    private var timerJob: Job? = null
    private var hapticsEnabled = true
    private var soundEnabled = true
    private var completionRecorded = false

    init {
        viewModelScope.launch {
            val prefs = repository.userPreferencesFlow.first()
            hapticsEnabled = prefs.hapticsEnabled
            soundEnabled = prefs.soundEnabled
            _shouldShowInstructions.value = GameType.HEX_MERGE.id !in prefs.seenInstructionIds

            val initialDifficulty = prefs.selectedDifficulty
            engine.startNewGame(difficulty = initialDifficulty)
            _uiState.value = engine.state
            analyticsManager.logGameStarted(GameType.HEX_MERGE)
            startTimer()
        }
        adManager.preloadRewardedAd()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _uiState.value = engine.state
            }
        }
    }

    fun onInstructionsDismissed() {
        _shouldShowInstructions.value = false
        viewModelScope.launch { repository.markInstructionsSeen(GameType.HEX_MERGE) }
    }

    fun onSlide(direction: HexDirection) {
        if (engine.state.gameOver) return
        val moved = engine.slide(direction)
        _uiState.value = engine.state

        if (moved) {
            soundAndHapticsManager.playSuccessSound(soundEnabled)
            soundAndHapticsManager.triggerHaptic(hapticsEnabled, 20)
            if (engine.state.gameOver) {
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
            startTimer()
        }
    }

    fun onRestart() {
        completionRecorded = false
        engine.startNewGame(difficulty = engine.state.difficulty)
        _uiState.value = engine.state
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
                    gameType = GameType.HEX_MERGE,
                    score = state.score,
                    durationSeconds = 60,
                    won = state.score >= 500,
                    moves = state.moves,
                    difficulty = state.difficulty
                )
            )

            analyticsManager.logGameCompleted(
                gameType = GameType.HEX_MERGE,
                score = state.score
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
