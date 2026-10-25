package com.tds.puzzivo.feature.wordsearch

data class TargetWord(
    val id: String,
    val word: String,
    val startRow: Int = -1,
    val startCol: Int = -1,
    val endRow: Int = -1,
    val endCol: Int = -1
)

data class WordSearchPuzzle(
    val id: String,
    val title: String,
    val theme: String,
    val rows: Int,
    val cols: Int,
    val grid: List<List<Char>>,
    val targetWords: List<TargetWord>,
    val difficulty: String = "Challenge"
)

data class CellPosition(val row: Int, val col: Int)

data class WordSearchState(
    val puzzle: WordSearchPuzzle? = null,
    val puzzleIndex: Int = 0,
    val foundWords: Set<String> = emptySet(),
    val foundCellPositions: Set<CellPosition> = emptySet(),
    val selectedCells: List<CellPosition> = emptyList(),
    val isGameComplete: Boolean = false,
    val hasUsedHint: Boolean = false,
    val highlightedHintWord: TargetWord? = null
)
