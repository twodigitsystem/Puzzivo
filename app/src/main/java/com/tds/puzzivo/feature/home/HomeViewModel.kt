package com.tds.puzzivo.feature.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tds.puzzivo.PuzzivoApplication
import com.tds.puzzivo.core.model.DailyPick
import com.tds.puzzivo.core.storage.PreferencesRepository
import com.tds.puzzivo.core.storage.UserPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.delay
import java.time.LocalDate

data class HomeUiState(
    val userPreferences: UserPreferences = UserPreferences(),
    val todayPick: DailyPick = DailyPick(
        gameType = PreferencesRepository.getTodayPickGameType(),
        dateString = LocalDate.now().toString(),
        isCompleted = false
    )
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as PuzzivoApplication).preferencesRepository

    private val localDateFlow = flow {
        while (true) {
            emit(LocalDate.now())
            delay(60_000)
        }
    }

    val uiState: StateFlow<HomeUiState> = repository.userPreferencesFlow
        .combine(localDateFlow) { prefs, today ->
            val todayGame = PreferencesRepository.getTodayPickGameType(today)
            val todayStr = today.toString()
            val isCompleted = (prefs.todayPickLastDate == todayStr) && prefs.todayPickCompleted

            HomeUiState(
                userPreferences = prefs,
                todayPick = DailyPick(
                    gameType = todayGame,
                    dateString = todayStr,
                    isCompleted = isCompleted
                )
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeUiState()
        )
}
