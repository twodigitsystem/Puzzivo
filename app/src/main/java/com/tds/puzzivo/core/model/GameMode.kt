package com.tds.puzzivo.core.model

enum class GameMode(val id: String, val title: String) {
    CLASSIC("classic", "Classic Mode"),
    TIMED("timed", "Timed Attack"),
    ENDLESS("endless", "Endless Marathon"),
    DAILY_PUZZLE("daily", "Puzzle of the Day")
}
