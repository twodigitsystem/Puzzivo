package com.tds.puzzivo.feature.wordscramble

import android.app.Activity
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tds.puzzivo.core.ads.AdManager
import com.tds.puzzivo.core.analytics.AnalyticsManager
import com.tds.puzzivo.core.audio.SoundAndHapticsManager
import com.tds.puzzivo.core.manager.AchievementManager
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
class WordScrambleViewModel @Inject constructor(
    private val repository: PreferencesRepository,
    private val adManager: AdManager,
    private val soundAndHapticsManager: SoundAndHapticsManager,
    private val completionRecorder: GameCompletionRecorder,
    private val analyticsManager: AnalyticsManager,
    private val achievementManager: AchievementManager,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private companion object {
        const val KEY_SELECTED_IDS = "ws_selected_ids"
        const val KEY_SCORE = "ws_score"
        const val KEY_COMBO = "ws_combo"
        const val KEY_TIME = "ws_time"
    }

    private val engine = WordScrambleGameEngine()

    private val _uiState = MutableStateFlow(engine.state)
    val uiState: StateFlow<WordScrambleState> = _uiState.asStateFlow()

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
            _shouldShowInstructions.value = GameType.WORD_SCRAMBLE.id !in prefs.seenInstructionIds

            val initialDifficulty = prefs.selectedDifficulty
            engine.startNewGame(
                difficulty = initialDifficulty,
                bestScore = prefs.wordScrambleBestScore
            )

            restoreStateFromHandle()

            _uiState.value = engine.state
            analyticsManager.logGameStarted(GameType.WORD_SCRAMBLE)
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
        savedStateHandle[KEY_SELECTED_IDS] = engine.state.selectedLetterIds.toIntArray()
        savedStateHandle[KEY_SCORE] = engine.state.score
        savedStateHandle[KEY_COMBO] = engine.state.comboStreak
        savedStateHandle[KEY_TIME] = engine.state.timeElapsedSeconds
    }

    private fun restoreStateFromHandle() {
        val savedSelected = savedStateHandle.get<IntArray>(KEY_SELECTED_IDS)
        val savedScore = savedStateHandle.get<Int>(KEY_SCORE)
        val savedCombo = savedStateHandle.get<Int>(KEY_COMBO)
        val savedTime = savedStateHandle.get<Int>(KEY_TIME)

        if (savedSelected != null || savedScore != null) {
            val restored = engine.state.copy(
                selectedLetterIds = savedSelected?.toList() ?: engine.state.selectedLetterIds,
                score = savedScore ?: engine.state.score,
                comboStreak = savedCombo ?: engine.state.comboStreak,
                timeElapsedSeconds = savedTime ?: engine.state.timeElapsedSeconds
            )
            engine.restoreState(restored)
        }
    }

    fun onInstructionsDismissed() {
        _shouldShowInstructions.value = false
        viewModelScope.launch { repository.markInstructionsSeen(GameType.WORD_SCRAMBLE) }
    }

    fun onLetterClick(letterId: Int) {
        if (engine.state.isPaused || engine.state.isGameComplete) return
        engine.selectLetter(letterId)
        soundAndHapticsManager.triggerHaptic(hapticsEnabled, 15)
        _uiState.value = engine.state
        saveStateToHandle()
    }

    fun onAnswerSlotClick(index: Int) {
        if (engine.state.isPaused || engine.state.isGameComplete) return
        engine.deselectAtAnswerIndex(index)
        soundAndHapticsManager.triggerHaptic(hapticsEnabled, 15)
        _uiState.value = engine.state
        saveStateToHandle()
    }

    fun onBackspace() {
        if (engine.state.isPaused || engine.state.isGameComplete) return
        engine.backspace()
        soundAndHapticsManager.triggerHaptic(hapticsEnabled, 15)
        _uiState.value = engine.state
        saveStateToHandle()
    }

    fun onClear() {
        if (engine.state.isPaused || engine.state.isGameComplete) return
        engine.clearSelection()
        soundAndHapticsManager.triggerHaptic(hapticsEnabled, 20)
        _uiState.value = engine.state
        saveStateToHandle()
    }

    fun onShufflePool() {
        if (engine.state.isPaused || engine.state.isGameComplete) return
        engine.shuffleUnselectedLetters()
        soundAndHapticsManager.triggerHaptic(hapticsEnabled, 25)
        _uiState.value = engine.state
        saveStateToHandle()
    }

    fun onSubmit() {
        if (engine.state.isPaused || engine.state.isGameComplete) return
        val result = engine.submitAnswer()
        _uiState.value = engine.state
        saveStateToHandle()

        when (result) {
            WordSubmitResult.CORRECT -> {
                soundAndHapticsManager.playSuccessSound(soundEnabled)
                soundAndHapticsManager.triggerHaptic(hapticsEnabled, 40)
            }
            WordSubmitResult.WRONG -> {
                soundAndHapticsManager.playErrorSound(soundEnabled)
                soundAndHapticsManager.triggerHaptic(hapticsEnabled, 60)
            }
            WordSubmitResult.GAME_COMPLETE -> {
                soundAndHapticsManager.playVictorySound(soundEnabled)
                soundAndHapticsManager.triggerHaptic(hapticsEnabled, 100)
                onGameComplete()
            }
        }
    }

    fun onUseHintWithReward(activity: Activity) {
        if (engine.state.isPaused || engine.state.isGameComplete) return
        adManager.showRewardedAd(
            activity = activity,
            onRewardEarned = {
                val success = engine.useHint()
                if (success) {
                    soundAndHapticsManager.playSuccessSound(soundEnabled)
                    soundAndHapticsManager.triggerHaptic(hapticsEnabled, 40)
                    _uiState.value = engine.state
                    saveStateToHandle()
                }
            },
            onAdUnavailable = {
                val success = engine.useHint()
                if (success) {
                    soundAndHapticsManager.playSuccessSound(soundEnabled)
                    soundAndHapticsManager.triggerHaptic(hapticsEnabled, 40)
                    _uiState.value = engine.state
                    saveStateToHandle()
                }
            }
        )
    }

    fun onDifficultySelected(difficulty: DifficultyLevel) {
        viewModelScope.launch {
            repository.setSelectedDifficulty(difficulty)
            completionRecorded = false
            engine.startNewGame(
                difficulty = difficulty,
                bestScore = repository.userPreferencesFlow.first().wordScrambleBestScore
            )
            _uiState.value = engine.state
            saveStateToHandle()
            startTimer()
        }
    }

    fun onRestart() {
        viewModelScope.launch {
            completionRecorded = false
            engine.startNewGame(
                difficulty = engine.state.difficulty,
                bestScore = repository.userPreferencesFlow.first().wordScrambleBestScore
            )
            _uiState.value = engine.state
            saveStateToHandle()
            startTimer()
        }
    }

    private fun onGameComplete() {
        if (completionRecorded) return
        completionRecorded = true
        timerJob?.cancel()

        viewModelScope.launch {
            val state = engine.state
            repository.saveWordScrambleBestScore(state.score)

            completionRecorder.record(
                GameCompletionRequest(
                    completionId = java.util.UUID.randomUUID().toString(),
                    gameType = GameType.WORD_SCRAMBLE,
                    score = state.score,
                    durationSeconds = state.timeElapsedSeconds,
                    won = true,
                    moves = state.totalMoves,
                    hintsUsed = state.hintsUsed,
                    difficulty = state.difficulty
                )
            )

            analyticsManager.logGameCompleted(
                gameType = GameType.WORD_SCRAMBLE,
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
