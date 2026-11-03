package com.tds.puzzivo.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tds.puzzivo.core.storage.PreferencesRepository
import com.tds.puzzivo.core.storage.UserPreferences
import com.tds.puzzivo.core.model.DifficultyLevel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: PreferencesRepository
) : ViewModel() {

    val userPreferences: StateFlow<UserPreferences> = repository.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    fun toggleSound(enabled: Boolean) {
        viewModelScope.launch {
            repository.setSoundEnabled(enabled)
        }
    }

    fun toggleHaptics(enabled: Boolean) {
        viewModelScope.launch {
            repository.setHapticsEnabled(enabled)
        }
    }

    fun setDifficulty(difficulty: DifficultyLevel) {
        viewModelScope.launch {
            repository.setSelectedDifficulty(difficulty)
        }
    }

    fun clearAllProgress() {
        viewModelScope.launch {
            repository.clearAllProgress()
        }
    }
}
