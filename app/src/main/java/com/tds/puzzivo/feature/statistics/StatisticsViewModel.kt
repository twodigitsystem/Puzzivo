package com.tds.puzzivo.feature.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tds.puzzivo.core.manager.StatisticsManager
import com.tds.puzzivo.core.model.GameStatistics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val statisticsManager: StatisticsManager
) : ViewModel() {

    val allStatistics: StateFlow<List<GameStatistics>> = statisticsManager.allStatistics
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val overallStatistics: StateFlow<OverallStatistics> = allStatistics
        .map { statsList ->
            OverallStatistics(
                totalGamesPlayed = statsList.sumOf { it.totalPlays },
                totalWins = statsList.sumOf { it.totalWins },
                totalPlayTime = statsList.sumOf { it.totalTimePlayedSeconds },
                totalScore = statsList.sumOf { it.highestScore.toLong() },
                currentStreak = statsList.maxOfOrNull { it.currentWinStreak } ?: 0,
                bestStreak = statsList.maxOfOrNull { it.longestWinStreak } ?: 0
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = OverallStatistics()
        )
}

data class OverallStatistics(
    val totalGamesPlayed: Int = 0,
    val totalWins: Int = 0,
    val totalPlayTime: Long = 0,
    val totalScore: Long = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0
)
