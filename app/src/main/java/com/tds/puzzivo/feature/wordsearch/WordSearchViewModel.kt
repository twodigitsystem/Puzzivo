package com.tds.puzzivo.feature.wordsearch

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tds.puzzivo.PuzzivoApplication
import com.tds.puzzivo.core.ads.AdManager
import com.tds.puzzivo.core.audio.SoundAndHapticsManager
import com.tds.puzzivo.core.model.GameType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class WordSearchViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as PuzzivoApplication).preferencesRepository
    private val adManager = AdManager(application)
    private val soundAndHapticsManager = SoundAndHapticsManager(application)

    private val engine = WordSearchGameEngine()

    private val _uiState = MutableStateFlow(engine.state)
    val uiState: StateFlow<WordSearchState> = _uiState.asStateFlow()

    private var hapticsEnabled = true
    private var soundEnabled = true

    init {
        viewModelScope.launch {
            val prefs = repository.userPreferencesFlow.first()
            hapticsEnabled = prefs.hapticsEnabled
            soundEnabled = prefs.soundEnabled
            engine.loadPuzzle(prefs.wordSearchCompletedIndex)
            _uiState.value = engine.state
        }
        adManager.preloadRewardedAd()
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
        val nextIndex = engine.state.puzzleIndex + 1
        engine.loadPuzzle(nextIndex)
        _uiState.value = engine.state
    }

    private fun onGameComplete() {
        val puzzle = engine.state.puzzle ?: return
        viewModelScope.launch {
            repository.saveWordSearchCompleted(
                puzzleId = puzzle.id,
                nextIndex = engine.state.puzzleIndex + 1
            )
            repository.recordGameCompleted(GameType.WORD_SEARCH)
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundAndHapticsManager.release()
    }
}
