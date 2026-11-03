package com.tds.puzzivo.core.manager

import com.tds.puzzivo.core.model.GameTheme
import com.tds.puzzivo.core.model.ThemeRequirementType
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemeManager @Inject constructor(
    private val progressionManager: ProgressionManager,
    private val achievementManager: AchievementManager,
    private val statisticsManager: StatisticsManager
) {

    val selectedTheme: Flow<GameTheme> = progressionManager.playerProgress.map { progress ->
        GameTheme.entries.find { it.id == progress.selectedThemeId } ?: GameTheme.NAVY_DARK
    }

    val unlockedThemes: Flow<List<GameTheme>> = progressionManager.playerProgress.map { progress ->
        GameTheme.entries.filter { theme ->
            progress.unlockedThemes.contains(theme.id)
        }
    }

    suspend fun getAvailableThemes(): List<ThemeInfo> = coroutineScope {
        val progressDeferred = async { progressionManager.playerProgress.first() }
        val unlockedCountDeferred = async { achievementManager.getUnlockedCount() }
        val allStatsDeferred = async { statisticsManager.allStatistics.first() }

        val progress = progressDeferred.await()
        val unlockedCount = unlockedCountDeferred.await()
        val allStats = allStatsDeferred.await()
        val totalGamesPlayed = allStats.sumOf { it.totalPlays }
        val currentStreak = allStats.maxOfOrNull { it.currentWinStreak } ?: 0
        
        GameTheme.entries.map { theme ->
            val isUnlocked = progress.unlockedThemes.contains(theme.id)
            val canUnlock = if (!isUnlocked) {
                checkRequirement(
                    theme,
                    progress.level,
                    totalGamesPlayed,
                    currentStreak,
                    unlockedCount,
                    progress.premiumUnlocked
                )
            } else {
                true
            }
            
            ThemeInfo(
                theme = theme,
                isUnlocked = isUnlocked,
                canUnlock = canUnlock,
                requirementMet = canUnlock,
                requirementText = getRequirementText(theme, progress.level, totalGamesPlayed, currentStreak, unlockedCount)
            )
        }
    }

    private fun checkRequirement(
        theme: GameTheme,
        level: Int,
        gamesPlayed: Int,
        streak: Int,
        achievementCount: Int,
        isPremium: Boolean
    ): Boolean {
        return when (theme.requirementType) {
            ThemeRequirementType.NONE -> true
            ThemeRequirementType.LEVEL -> level >= theme.requirementValue
            ThemeRequirementType.GAMES_PLAYED -> gamesPlayed >= theme.requirementValue
            ThemeRequirementType.STREAK -> streak >= theme.requirementValue
            ThemeRequirementType.ACHIEVEMENT_COUNT -> achievementCount >= theme.requirementValue
            ThemeRequirementType.PREMIUM -> isPremium
        }
    }

    private fun getRequirementText(
        theme: GameTheme,
        level: Int,
        gamesPlayed: Int,
        streak: Int,
        achievementCount: Int
    ): String {
        return when (theme.requirementType) {
            ThemeRequirementType.NONE -> "Unlocked"
            ThemeRequirementType.LEVEL -> "Reach level ${theme.requirementValue} ($level/${theme.requirementValue})"
            ThemeRequirementType.GAMES_PLAYED -> "Play ${theme.requirementValue} games ($gamesPlayed/${theme.requirementValue})"
            ThemeRequirementType.STREAK -> "Win streak of ${theme.requirementValue} ($streak/${theme.requirementValue})"
            ThemeRequirementType.ACHIEVEMENT_COUNT -> "Unlock ${theme.requirementValue} achievements ($achievementCount/${theme.requirementValue})"
            ThemeRequirementType.PREMIUM -> "Premium feature"
        }
    }

    suspend fun selectTheme(themeId: String): Boolean {
        val isUnlocked = progressionManager.isThemeUnlocked(themeId)
        if (isUnlocked) {
            progressionManager.setSelectedTheme(themeId)
            return true
        }
        return false
    }

    suspend fun unlockTheme(themeId: String) {
        progressionManager.unlockTheme(themeId)
    }

    suspend fun checkAndUnlockThemes() {
        val themes = getAvailableThemes()
        themes.forEach { themeInfo ->
            if (!themeInfo.isUnlocked && themeInfo.canUnlock) {
                unlockTheme(themeInfo.theme.id)
            }
        }
    }
}

data class ThemeInfo(
    val theme: GameTheme,
    val isUnlocked: Boolean,
    val canUnlock: Boolean,
    val requirementMet: Boolean,
    val requirementText: String
)
