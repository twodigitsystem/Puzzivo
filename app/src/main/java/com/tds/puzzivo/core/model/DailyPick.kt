package com.tds.puzzivo.core.model

data class DailyPick(
    val gameType: GameType,
    val dateString: String,
    val isCompleted: Boolean
)
