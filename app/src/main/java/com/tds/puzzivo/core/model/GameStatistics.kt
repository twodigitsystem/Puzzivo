package com.tds.puzzivo.core.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_statistics")
data class GameStatistics(
    @PrimaryKey val gameType: GameType,
    val totalPlays: Int = 0,
    val totalWins: Int = 0,
    val totalTimePlayedSeconds: Long = 0,
    val averageScore: Float = 0f,
    val highestScore: Int = 0,
    val lowestScore: Int = 0,
    val currentWinStreak: Int = 0,
    val longestWinStreak: Int = 0,
    val perfectGames: Int = 0, // No mistakes/hints
    val hintsUsed: Int = 0,
    val firstPlayedDate: String? = null,
    val lastPlayedDate: String? = null
)

@Entity(tableName = "game_sessions")
data class GameSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val gameType: GameType,
    val playedDate: String,
    val score: Int,
    val durationSeconds: Int,
    val won: Boolean,
    val difficulty: DifficultyLevel,
    val mode: String = "classic",
    val hintsUsed: Int = 0,
    val moves: Int = 0
)

enum class DifficultyLevel(val scoreMultiplier: Float) {
    EASY(0.75f),
    MEDIUM(1.0f),
    HARD(1.5f),
    EXPERT(2.0f);

    val multiplier: Float get() = scoreMultiplier
}
