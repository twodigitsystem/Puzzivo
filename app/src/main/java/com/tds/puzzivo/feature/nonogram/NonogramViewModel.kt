package com.tds.puzzivo.feature.nonogram

import android.app.Activity
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
class NonogramViewModel @Inject constructor(
    private val repository: PreferencesRepository,
    private val adManager: AdManager,
    private val soundAndHapticsManager: SoundAndHapticsManager,
    private val completionRecorder: GameCompletionRecorder,
    private val analyticsManager: AnalyticsManager,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val engine = NonogramGameEngine()

    private val _uiState = MutableStateFlow(engine.state)
    val uiState: StateFlow<NonogramState> = _uiState.asStateFlow()

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
            _shouldShowInstructions.value = GameType.NONOGRAM.id !in prefs.seenInstructionIds

            val initialDifficulty = prefs.selectedDifficulty
            engine.startNewGame(difficulty = initialDifficulty, puzzleIndex = 0)
            _uiState.value = engine.state
            analyticsManager.logGameStarted(GameType.NONOGRAM)
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
            }
        }
    }

    fun onInstructionsDismissed() {
        _shouldShowInstructions.value = false
        viewModelScope.launch { repository.markInstructionsSeen(GameType.NONOGRAM) }
    }

    fun onCellClick(index: Int) {
        if (engine.state.isWon) return
        val isWon = engine.toggleCell(index)
        _uiState.value = engine.state
        soundAndHapticsManager.triggerHaptic(hapticsEnabled, 20)

        if (isWon) {
            soundAndHapticsManager.playVictorySound(soundEnabled)
            soundAndHapticsManager.triggerHaptic(hapticsEnabled, 100)
            onGameComplete()
        } else {
            soundAndHapticsManager.playSuccessSound(soundEnabled)
        }
    }

    fun onToggleMarkMode() {
        engine.toggleMarkMode()
        soundAndHapticsManager.triggerHaptic(hapticsEnabled, 25)
        _uiState.value = engine.state
    }

    fun onUseHintWithReward(activity: Activity) {
        if (engine.state.isWon) return
        adManager.showRewardedAd(
            activity = activity,
            onRewardEarned = {
                val success = engine.useHint()
                if (success) {
                    soundAndHapticsManager.playSuccessSound(soundEnabled)
                    soundAndHapticsManager.triggerHaptic(hapticsEnabled, 40)
                    _uiState.value = engine.state
                    if (engine.state.isWon) onGameComplete()
                }
            },
            onAdUnavailable = {
                val success = engine.useHint()
                if (success) {
                    soundAndHapticsManager.playSuccessSound(soundEnabled)
                    soundAndHapticsManager.triggerHaptic(hapticsEnabled, 40)
                    _uiState.value = engine.state
                    if (engine.state.isWon) onGameComplete()
                }
            }
        )
    }

    fun onDifficultySelected(difficulty: DifficultyLevel) {
        viewModelScope.launch {
            repository.setSelectedDifficulty(difficulty)
            completionRecorded = false
            engine.startNewGame(difficulty = difficulty, puzzleIndex = 0)
            _uiState.value = engine.state
            startTimer()
        }
    }

    fun onRestart() {
        completionRecorded = false
        engine.startNewGame(difficulty = engine.state.difficulty, puzzleIndex = engine.state.puzzleIndex + 1)
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
                    gameType = GameType.NONOGRAM,
                    score = state.score,
                    durationSeconds = state.timeElapsedSeconds,
                    won = true,
                    moves = state.moves,
                    hintsUsed = state.hintsUsed,
                    difficulty = state.difficulty
                )
            )

            analyticsManager.logGameCompleted(
                gameType = GameType.NONOGRAM,
                score = state.score,
                duration = state.timeElapsedSeconds
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
