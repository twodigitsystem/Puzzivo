package com.tds.puzzivo.feature.wordguess

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tds.puzzivo.core.ads.AdManager
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
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class WordGuessViewModel @Inject constructor(
    private val repository: PreferencesRepository,
    private val adManager: AdManager,
    private val soundAndHapticsManager: SoundAndHapticsManager,
    private val completionRecorder: GameCompletionRecorder
) : ViewModel() {

    private val engine = WordGuessGameEngine()

    private val _uiState = MutableStateFlow(engine.state)
    val uiState: StateFlow<WordGuessState> = _uiState.asStateFlow()

    private var hapticsEnabled = true
    private var soundEnabled = true
    private var completionRecorded = false

    init {
        viewModelScope.launch {
            val prefs = repository.userPreferencesFlow.first()
            hapticsEnabled = prefs.hapticsEnabled
            soundEnabled = prefs.soundEnabled
            engine.startNewGame()
            _uiState.value = engine.state
        }
        adManager.preloadRewardedAd()
    }

    fun onKeyPress(char: Char) {
        soundAndHapticsManager.playTapSound(soundEnabled)
        soundAndHapticsManager.triggerHaptic(hapticsEnabled, 20)
        engine.typeLetter(char)
        _uiState.value = engine.state
    }

    fun onDeletePress() {
        soundAndHapticsManager.playTapSound(soundEnabled)
        soundAndHapticsManager.triggerHaptic(hapticsEnabled, 20)
        engine.deleteLetter()
        _uiState.value = engine.state
    }

    fun onSubmitPress() {
        soundAndHapticsManager.playTapSound(soundEnabled)
        soundAndHapticsManager.triggerHaptic(hapticsEnabled, 30)
        val submitted = engine.submitGuess()
        _uiState.value = engine.state

        if (submitted && engine.state.isGameComplete && !completionRecorded) {
            completionRecorded = true
            val score = if (engine.state.isWon) (7 - engine.state.attemptsUsed) * 100 else 0
            if (engine.state.isWon) {
                soundAndHapticsManager.playVictorySound(soundEnabled)
            } else {
                soundAndHapticsManager.playErrorSound(soundEnabled)
            }
            viewModelScope.launch {
                completionRecorder.record(
                    GameCompletionRequest(
                        completionId = UUID.randomUUID().toString(),
                        gameType = GameType.WORD_GUESS,
                        score = score,
                        won = engine.state.isWon,
                        moves = engine.state.attemptsUsed
                    )
                )
            }
        }
    }

    fun startNewGame() {
        completionRecorded = false
        engine.startNewGame()
        _uiState.value = engine.state
    }
}
