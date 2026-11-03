package com.tds.puzzivo.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tds.puzzivo.core.model.DailyPick
import com.tds.puzzivo.core.model.GameStatistics
import com.tds.puzzivo.core.manager.StatisticsManager
import com.tds.puzzivo.core.storage.PreferencesRepository
import com.tds.puzzivo.core.storage.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

data class HomeUiState(
    val userPreferences: UserPreferences = UserPreferences(),
    val todayPick: DailyPick = DailyPick(
        gameType = PreferencesRepository.getTodayPickGameType(),
        dateString = LocalDate.now().toString(),
        isCompleted = false
    ),
    val gameStatistics: Map<com.tds.puzzivo.core.model.GameType, GameStatistics> = emptyMap()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: PreferencesRepository,
    private val statisticsManager: StatisticsManager
) : ViewModel() {

    private val localDateFlow = flow {
        while (true) {
            val today = LocalDate.now()
            emit(today)
            val tomorrow = today.plusDays(1).atStartOfDay()
            val now = java.time.LocalDateTime.now()
            val millisUntilMidnight = java.time.Duration.between(now, tomorrow).toMillis().coerceAtLeast(60_000)
            delay(millisUntilMidnight.milliseconds)
        }
    }

    val uiState: StateFlow<HomeUiState> = repository.userPreferencesFlow
        .combine(localDateFlow) { prefs, today -> prefs to today }
        .combine(statisticsManager.allStatistics) { (prefs, today), statistics ->
            val todayGame = PreferencesRepository.getTodayPickGameType(today)
            val todayStr = today.toString()
            val isCompleted = (prefs.todayPickLastDate == todayStr) && prefs.todayPickCompleted

            HomeUiState(
                userPreferences = prefs,
                todayPick = DailyPick(
                    gameType = todayGame,
                    dateString = todayStr,
                    isCompleted = isCompleted
                ),
                gameStatistics = statistics.associateBy { it.gameType }
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeUiState()
        )
}
