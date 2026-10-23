package com.tds.puzzivo.core.model

enum class GameType(
    val id: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val primaryColorHex: Long
) {
    NUMBER_MERGE(
        id = "number_merge",
        title = "Number Merge",
        subtitle = "Slide & Merge Tiles",
        description = "Swipe tiles on a 4×4 grid to combine matching numbers and reach higher scores!",
        primaryColorHex = 0xFFFACC15 // Yellow
    ),
    MEMORY_MATCH(
        id = "memory_match",
        title = "Memory Match",
        subtitle = "Find Matching Pairs",
        description = "Flip cards to test your memory and find all 8 matching pairs in as few moves as possible.",
        primaryColorHex = 0xFF06B6D4 // Cyan
    ),
    QUICK_MATH(
        id = "quick_math",
        title = "Quick Math",
        subtitle = "60s Timed Challenge",
        description = "Solve mental arithmetic questions against a 60-second timer to build your score streak!",
        primaryColorHex = 0xFF22C55E // Green
    ),
    WORD_SEARCH(
        id = "word_search",
        title = "Word Search",
        subtitle = "Find Hidden Words",
        description = "Locate target words hidden horizontally, vertically, or diagonally inside the letter grid.",
        primaryColorHex = 0xFFEC4899 // Magenta
    );

    companion object {
        fun fromId(id: String?): GameType = entries.find { it.id == id } ?: NUMBER_MERGE
    }
}
