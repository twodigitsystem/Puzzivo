package com.tds.puzzivo.core.model

import androidx.annotation.StringRes
import com.tds.puzzivo.R

enum class GameType(
    val id: String,
    @StringRes val titleRes: Int,
    @StringRes val subtitleRes: Int,
    @StringRes val descriptionRes: Int,
    val primaryColorHex: Long,
    val totalContentCount: Int? = null
) {
    NUMBER_MERGE(
        id = "number_merge",
        titleRes = R.string.game_number_merge_title,
        subtitleRes = R.string.game_number_merge_subtitle,
        descriptionRes = R.string.game_number_merge_description,
        primaryColorHex = 0xFFFACC15 // Yellow
    ),
    MEMORY_MATCH(
        id = "memory_match",
        titleRes = R.string.game_memory_match_title,
        subtitleRes = R.string.game_memory_match_subtitle,
        descriptionRes = R.string.game_memory_match_description,
        primaryColorHex = 0xFF06B6D4 // Cyan
    ),
    QUICK_MATH(
        id = "quick_math",
        titleRes = R.string.game_quick_math_title,
        subtitleRes = R.string.game_quick_math_subtitle,
        descriptionRes = R.string.game_quick_math_description,
        primaryColorHex = 0xFF22C55E // Green
    ),
    WORD_SEARCH(
        id = "word_search",
        titleRes = R.string.game_word_search_title,
        subtitleRes = R.string.game_word_search_subtitle,
        descriptionRes = R.string.game_word_search_description,
        primaryColorHex = 0xFFEC4899, // Magenta
        totalContentCount = 20
    ),
    MINI_SUDOKU(
        id = "mini_sudoku",
        titleRes = R.string.game_mini_sudoku_title,
        subtitleRes = R.string.game_mini_sudoku_subtitle,
        descriptionRes = R.string.game_mini_sudoku_description,
        primaryColorHex = 0xFF8B5CF6
    ),
    MINESWEEPER(
        id = "minesweeper",
        titleRes = R.string.game_minesweeper_title,
        subtitleRes = R.string.game_minesweeper_subtitle,
        descriptionRes = R.string.game_minesweeper_description,
        primaryColorHex = 0xFFF97316
    ),
    PATTERN_RECALL(
        id = "pattern_recall",
        titleRes = R.string.game_pattern_recall_title,
        subtitleRes = R.string.game_pattern_recall_subtitle,
        descriptionRes = R.string.game_pattern_recall_description,
        primaryColorHex = 0xFF14B8A6
    ),
    WORD_SCRAMBLE(
        id = "word_scramble",
        titleRes = R.string.game_word_scramble_title,
        subtitleRes = R.string.game_word_scramble_subtitle,
        descriptionRes = R.string.game_word_scramble_description,
        primaryColorHex = 0xFFEAB308
    ),
    LIGHTS_OUT(
        id = "lights_out",
        titleRes = R.string.game_lights_out_title,
        subtitleRes = R.string.game_lights_out_subtitle,
        descriptionRes = R.string.game_lights_out_description,
        primaryColorHex = 0xFF0EA5E9
    ),
    NONOGRAM(
        id = "nonogram",
        titleRes = R.string.game_nonogram_title,
        subtitleRes = R.string.game_nonogram_subtitle,
        descriptionRes = R.string.game_nonogram_description,
        primaryColorHex = 0xFFEF4444
    ),
    HEX_MERGE(
        id = "hex_merge",
        titleRes = R.string.game_hex_merge_title,
        subtitleRes = R.string.game_hex_merge_subtitle,
        descriptionRes = R.string.game_hex_merge_description,
        primaryColorHex = 0xFF10B981
    );

    companion object {
        fun fromId(id: String?): GameType = entries.find { it.id == id } ?: NUMBER_MERGE
    }
}
