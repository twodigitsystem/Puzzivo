package com.tds.puzzivo.feature.themes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tds.puzzivo.core.manager.ThemeManager
import com.tds.puzzivo.core.model.GameTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemesViewModel @Inject constructor(
    private val themeManager: ThemeManager
) : ViewModel() {

    val allThemes: StateFlow<List<GameTheme>> = flow {
        emit(GameTheme.entries.toList())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GameTheme.entries.toList()
    )

    val selectedTheme: StateFlow<String> = themeManager.selectedTheme
        .map { it.id }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = GameTheme.NAVY_DARK.id
        )

    val unlockedThemes: StateFlow<Set<String>> = themeManager.unlockedThemes
        .map { themes -> themes.map { it.id }.toSet() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = setOf(GameTheme.NAVY_DARK.id)
        )

    fun selectTheme(themeId: String) {
        viewModelScope.launch {
            themeManager.selectTheme(themeId)
        }
    }
}
