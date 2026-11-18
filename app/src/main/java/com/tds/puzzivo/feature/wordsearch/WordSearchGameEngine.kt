package com.tds.puzzivo.feature.wordsearch

import kotlin.math.abs

class WordSearchGameEngine {

    var state = WordSearchState()
        private set

    fun loadPuzzle(puzzleIndex: Int = 0, bestScore: Int = 0) {
        val puzzles = WordSearchRepository.puzzles
        val index = puzzleIndex.coerceIn(puzzles.indices)
        val selectedPuzzle = puzzles[index]

        state = WordSearchState(
            puzzle = selectedPuzzle,
            puzzleIndex = index,
            foundWords = emptySet(),
            foundCellPositions = emptySet(),
            selectedCells = emptyList(),
            isGameComplete = false,
            hintsUsed = 0,
            highlightedHintWord = null,
            highlightedFirstLetterCell = null,
            score = 0,
            bestScore = bestScore,
            timeElapsedSeconds = 0
        )
    }

    fun selectStartCell(row: Int, col: Int) {
        val puzzle = state.puzzle ?: return
        if (state.isGameComplete) return
        if (row !in 0 until puzzle.rows || col !in 0 until puzzle.cols) return

        state = state.copy(
            selectedCells = listOf(CellPosition(row, col))
        )
    }

    fun updateSelection(endRow: Int, endCol: Int) {
        val puzzle = state.puzzle ?: return
        if (state.isGameComplete) return
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
        if (state.isGameComplete) return false
        val selected = state.selectedCells
        if (selected.isEmpty()) return false

        val matchingWord = puzzle.targetWords.find { tw ->
            !state.foundWords.contains(tw.word) &&
                    selectionMatchesTarget(puzzle, selected, tw)
        }

        if (matchingWord != null) {
            val updatedFoundWords = state.foundWords + matchingWord.word
            val updatedFoundCells = state.foundCellPositions + selected.toSet()
            val isComplete = updatedFoundWords.size == puzzle.targetWords.size

            val wordScore = matchingWord.word.length * 100
            val newScore = state.score + wordScore
            val newBest = maxOf(newScore, state.bestScore)

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
                hintsUsed = state.hintsUsed,
                highlightedHintWord = updatedHint,
                highlightedFirstLetterCell = null,
                score = newScore,
                bestScore = newBest
            )
            return true
        } else {
            state = state.copy(selectedCells = emptyList())
            return false
        }
    }

    fun useRevealFirstLetterHint(): Boolean {
        val puzzle = state.puzzle ?: return false
        if (state.isGameComplete) return false

        val unfoundTarget = puzzle.targetWords.find { !state.foundWords.contains(it.word) }
            ?: return false

        if (unfoundTarget.startRow < 0 || unfoundTarget.startCol < 0) return false

        state = state.copy(
            hintsUsed = state.hintsUsed + 1,
            highlightedFirstLetterCell = CellPosition(unfoundTarget.startRow, unfoundTarget.startCol)
        )
        return true
    }

    fun useRevealWordHint(): Boolean {
        val puzzle = state.puzzle ?: return false
        if (state.isGameComplete) return false

        val unfoundTarget = puzzle.targetWords.find { !state.foundWords.contains(it.word) }
            ?: return false

        state = state.copy(
            hintsUsed = state.hintsUsed + 1,
            highlightedHintWord = unfoundTarget,
            highlightedFirstLetterCell = null
        )
        return true
    }

    fun tickTimer() {
        if (!state.isGameComplete) {
            state = state.copy(timeElapsedSeconds = state.timeElapsedSeconds + 1)
        }
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

    private fun selectionMatchesTarget(
        puzzle: WordSearchPuzzle,
        selected: List<CellPosition>,
        target: TargetWord
    ): Boolean {
        if (selected.size != target.word.length) return false

        val selectedWord = selected.joinToString(separator = "") { cell ->
            puzzle.grid[cell.row][cell.col].toString()
        }
        val targetWord = target.word.uppercase()

        return selectedWord.equals(targetWord, ignoreCase = true) ||
                selectedWord.reversed().equals(targetWord, ignoreCase = true)
    }
}
