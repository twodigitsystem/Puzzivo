package com.tds.puzzivo.feature.wordsearch

import kotlin.math.abs

class WordSearchGameEngine {

    var state = WordSearchState()
        private set

    fun loadPuzzle(puzzleIndex: Int = 0) {
        val puzzles = WordSearchRepository.puzzles
        val index = if (puzzleIndex >= puzzles.size) 0 else puzzleIndex.coerceIn(puzzles.indices)
        val selectedPuzzle = puzzles[index]

        state = WordSearchState(
            puzzle = selectedPuzzle,
            puzzleIndex = index,
            foundWords = emptySet(),
            foundCellPositions = emptySet(),
            selectedCells = emptyList(),
            isGameComplete = false,
            hasUsedHint = false,
            highlightedHintWord = null
        )
    }

    fun selectStartCell(row: Int, col: Int) {
        val puzzle = state.puzzle ?: return
        if (row !in 0 until puzzle.rows || col !in 0 until puzzle.cols) return

        state = state.copy(
            selectedCells = listOf(CellPosition(row, col))
        )
    }

    fun updateSelection(endRow: Int, endCol: Int) {
        val puzzle = state.puzzle ?: return
        val startCell = state.selectedCells.firstOrNull() ?: return

        if (endRow !in 0 until puzzle.rows || endCol !in 0 until puzzle.cols) return

        val cellsInLine = computeLineCells(
            startRow = startCell.row,
            startCol = startCell.col,
            endRow = endRow,
            endCol = endCol
        )

        state = state.copy(selectedCells = cellsInLine)
    }

    fun confirmSelection(): Boolean {
        val puzzle = state.puzzle ?: return false
        val selected = state.selectedCells
        if (selected.isEmpty()) return false

        val selectedWord = selected.map { puzzle.grid[it.row][it.col] }.joinToString("")
        val reversedWord = selectedWord.reversed()

        val matchingWord = puzzle.targetWords.find { tw ->
            !state.foundWords.contains(tw.word) &&
                    (tw.word.equals(selectedWord, ignoreCase = true) || tw.word.equals(reversedWord, ignoreCase = true))
        }

        if (matchingWord != null) {
            val updatedFoundWords = state.foundWords + matchingWord.word
            val updatedFoundCells = state.foundCellPositions + selected.toSet()
            val isComplete = updatedFoundWords.size == puzzle.targetWords.size

            val updatedHint = if (state.highlightedHintWord?.word == matchingWord.word) {
                null
            } else {
                state.highlightedHintWord
            }

            state = state.copy(
                foundWords = updatedFoundWords,
                foundCellPositions = updatedFoundCells,
                selectedCells = emptyList(),
                isGameComplete = isComplete,
                highlightedHintWord = updatedHint
            )
            return true
        } else {
            state = state.copy(selectedCells = emptyList())
            return false
        }
    }

    fun useRevealWordHint(): Boolean {
        val puzzle = state.puzzle ?: return false
        if (state.hasUsedHint || state.isGameComplete) return false

        val unfoundTarget = puzzle.targetWords.find { !state.foundWords.contains(it.word) }
            ?: return false

        state = state.copy(
            hasUsedHint = true,
            highlightedHintWord = unfoundTarget
        )
        return true
    }

    private fun computeLineCells(
        startRow: Int,
        startCol: Int,
        endRow: Int,
        endCol: Int
    ): List<CellPosition> {
        val dRow = endRow - startRow
        val dCol = endCol - startCol

        val absDRow = abs(dRow)
        val absDCol = abs(dCol)

        // Must be horizontal, vertical, or 45-degree diagonal
        if (dRow != 0 && dCol != 0 && absDRow != absDCol) {
            return emptyList()
        }

        val stepRow = when {
            dRow > 0 -> 1
            dRow < 0 -> -1
            else -> 0
        }

        val stepCol = when {
            dCol > 0 -> 1
            dCol < 0 -> -1
            else -> 0
        }

        val steps = maxOf(absDRow, absDCol)
        val result = mutableListOf<CellPosition>()

        var r = startRow
        var c = startCol

        for (i in 0..steps) {
            result.add(CellPosition(r, c))
            r += stepRow
            c += stepCol
        }

        return result
    }
}