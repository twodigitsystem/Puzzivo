package com.tds.puzzivo.feature.achievements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tds.puzzivo.core.manager.AchievementManager
import com.tds.puzzivo.core.model.Achievement
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AchievementsViewModel @Inject constructor(
    private val achievementManager: AchievementManager
) : ViewModel() {

    val achievements: StateFlow<List<Achievement>> = achievementManager.allAchievements
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val achievementStats: StateFlow<AchievementStats> = achievements
        .map { list ->
            val unlocked = list.filter { it.unlocked }
            AchievementStats(
                unlockedCount = unlocked.size,
                totalCount = list.size,
                totalXPEarned = unlocked.sumOf { it.xpReward }.toLong(),
                totalCoinsEarned = unlocked.sumOf { it.coinReward }.toLong()
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AchievementStats()
        )

    init {
        viewModelScope.launch {
            achievementManager.initializeDefaultAchievements()
        }
    }
}
