package com.tds.puzzivo.core.model

import androidx.annotation.StringRes
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_challenges")
data class DailyChallenge(
    @PrimaryKey val id: String,
    val date: String,
    val gameType: GameType,
    val challengeTypeId: String,
    @StringRes val descriptionRes: Int,
    val requirement: Int,
    val rewardCoins: Int,
    val rewardXP: Int,
    val isCompleted: Boolean = false,
    val completedDate: String? = null,
    val progress: Int = 0
)

sealed class ChallengeType {
    data class ReachScore(val score: Int) : ChallengeType()
    data class WinInTime(val seconds: Int) : ChallengeType()
    data class WinInMoves(val moves: Int) : ChallengeType()
    data class WinWithoutHints(val games: Int) : ChallengeType()
    data class PerfectGame(val count: Int) : ChallengeType()
    data class PlayGames(val count: Int) : ChallengeType()
    data class WinStreak(val streak: Int) : ChallengeType()
}

sealed class ChallengeReward {
    data class Coins(val amount: Int) : ChallengeReward()
    data class XP(val amount: Int) : ChallengeReward()
    data class ThemeUnlock(val themeId: String) : ChallengeReward()
    data class Combined(val coins: Int, val xp: Int) : ChallengeReward()
}
