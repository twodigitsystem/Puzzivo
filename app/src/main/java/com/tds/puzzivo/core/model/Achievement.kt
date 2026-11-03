package com.tds.puzzivo.core.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey val id: String,
    val gameType: GameType,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    @DrawableRes val iconRes: Int,
    val requirement: Int,
    val rarity: AchievementRarity,
    val coinReward: Int,
    val xpReward: Int,
    val unlocked: Boolean = false,
    val unlockedDate: String? = null,
    val progress: Int = 0
)

enum class AchievementRarity(val displayColor: Long) {
    COMMON(0xFF94A3B8),
    RARE(0xFF06B6D4),
    EPIC(0xFF8B5CF6),
    LEGENDARY(0xFFFACC15)
}

sealed class AchievementRequirement {
    data class ScoreThreshold(val score: Int) : AchievementRequirement()
    data class GamesPlayed(val count: Int) : AchievementRequirement()
    data class GamesWon(val count: Int) : AchievementRequirement()
    data class PerfectGames(val count: Int) : AchievementRequirement()
    data class StreakDays(val days: Int) : AchievementRequirement()
    data class TotalPlayTime(val minutes: Int) : AchievementRequirement()
    data class NoHintsUsed(val count: Int) : AchievementRequirement()
}
