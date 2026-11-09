package com.tds.puzzivo.feature.sudoku

import com.tds.puzzivo.core.model.DifficultyLevel

data class SudokuPuzzle(
    val solution: List<Int>,
    val givens: Set<Int>
) {
    companion object {
        fun create(variant: Int = 0): SudokuPuzzle {
            val base = listOf(
                1, 2, 3, 4, 5, 6,
                4, 5, 6, 1, 2, 3,
                2, 3, 4, 5, 6, 1,
                5, 6, 1, 2, 3, 4,
                3, 4, 5, 6, 1, 2,
                6, 1, 2, 3, 4, 5
            )
            val shift = variant % 6
            val solution = base.map { number -> ((number - 1 + shift) % 6) + 1 }
            val givens = setOf(0, 2, 3, 5, 6, 7, 9, 11, 12, 14, 17, 18, 20, 21, 23, 26, 28, 29)
            return SudokuPuzzle(solution, givens)
        }
    }
}

data class SudokuState(
    val difficulty: DifficultyLevel = DifficultyLevel.MEDIUM,
    val puzzleNumber: Int = 0,
    val puzzle: SudokuPuzzle = SudokuPuzzle.create(0),
    val values: List<Int> = emptyList(),
    val selectedIndex: Int? = null,
    val mistakes: Int = 0,
    val hintsUsed: Int = 0,
    val score: Int = 0,
    val bestScore: Int = 0,
    val timeElapsedSeconds: Int = 0,
    val isWon: Boolean = false,
    val isPaused: Boolean = false
)

class SudokuGameEngine {

    var state = SudokuState()
        private set

    fun startNewGame(
        difficulty: DifficultyLevel = DifficultyLevel.MEDIUM,
        puzzleNumber: Int = 0,
        bestScore: Int = 0
    ) {
        val puzzle = SudokuPuzzle.create(puzzleNumber)
        val initialValues = List(36) { index -> if (index in puzzle.givens) puzzle.solution[index] else 0 }

        state = SudokuState(
            difficulty = difficulty,
            puzzleNumber = puzzleNumber,
            puzzle = puzzle,
            values = initialValues,
            selectedIndex = null,
            mistakes = 0,
            hintsUsed = 0,
            score = 0,
            bestScore = bestScore,
            timeElapsedSeconds = 0,
            isWon = false,
            isPaused = false
        )
    }

    fun selectCell(index: Int) {
        if (state.isWon || index !in state.values.indices) return
        if (index !in state.puzzle.givens) {
            state = state.copy(selectedIndex = index)
        }
    }

    fun fillValue(value: Int): Boolean {
        val idx = state.selectedIndex ?: return false
        if (idx in state.puzzle.givens || state.isWon) return false

        val updatedValues = state.values.toMutableList()
        val correctVal = state.puzzle.solution[idx]
        val isCorrect = value == 0 || value == correctVal

        updatedValues[idx] = value

        val mistakesCount = if (!isCorrect && value != 0) state.mistakes + 1 else state.mistakes
        val isWon = updatedValues == state.puzzle.solution

        val newScore = if (isWon) {
            val base = 1000
            val multiplier = state.difficulty.scoreMultiplier
            val mistakePenalty = mistakesCount * 100
            val hintPenalty = state.hintsUsed * 150
            ((base * multiplier).toInt() - mistakePenalty - hintPenalty).coerceAtLeast(100)
        } else state.score

        val newBest = maxOf(newScore, state.bestScore)

        state = state.copy(
            values = updatedValues,
            mistakes = mistakesCount,
            score = newScore,
            bestScore = newBest,
            isWon = isWon
        )
        return isCorrect
    }

    fun useHint(): Boolean {
        val idx = state.selectedIndex ?: state.values.indexOfFirst { it == 0 }
        if (idx !in state.values.indices || state.isWon) return false

        val updatedValues = state.values.toMutableList()
        updatedValues[idx] = state.puzzle.solution[idx]

        val isWon = updatedValues == state.puzzle.solution
        val newScore = if (isWon) 500 else state.score

        state = state.copy(
            values = updatedValues,
            hintsUsed = state.hintsUsed + 1,
            score = newScore,
            isWon = isWon
        )
        return true
    }

    fun tickTimer() {
        if (!state.isWon && !state.isPaused) {
            state = state.copy(timeElapsedSeconds = state.timeElapsedSeconds + 1)
        }
    }
}
