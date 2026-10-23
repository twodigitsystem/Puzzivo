package com.tds.puzzivo.core.model

data class GameResult(
    val gameType: GameType,
    val score: Int = 0,
    val isNewBest: Boolean = false,
    val moves: Int = 0,
    val timeInSeconds: Int = 0,
    val details: String = ""
)

data class DailyPick(
    val gameType: GameType,
    val dateString: String,
    val isCompleted: Boolean
)
