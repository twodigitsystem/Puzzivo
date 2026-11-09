package com.tds.puzzivo.feature.nonogram

import com.tds.puzzivo.core.model.DifficultyLevel
import kotlin.random.Random

data class NonogramPuzzle(
    val size: Int,
    val solution: List<Boolean>,
    val rowClues: List<String>,
    val colClues: List<String>
)

object NonogramRepository {
    private val easyPuzzles = listOf(
        // 5x5 Cross pattern
        listOf(
            false, true, false, true, false,
            true, true, true, true, true,
            false, true, false, true, false,
            false, true, false, true, false,
            false, true, false, true, false
        ),
        // 5x5 Square border
        listOf(
            true, true, true, true, true,
            true, false, false, false, true,
            true, false, false, false, true,
            true, false, false, false, true,
            true, true, true, true, true
        )
    )

    private val mediumPuzzles = listOf(
        // 6x6 Grid pattern
        listOf(
            true, false, true, false, true, false,
            false, true, false, true, false, true,
            true, false, true, false, true, false,
            false, true, false, true, false, true,
            true, false, true, false, true, false,
            false, true, false, true, false, true
        )
    )

    fun getPuzzle(difficulty: DifficultyLevel, index: Int = 0): NonogramPuzzle {
        val solution = when (difficulty) {
            DifficultyLevel.EASY -> easyPuzzles[index % easyPuzzles.size]
            DifficultyLevel.MEDIUM, DifficultyLevel.HARD, DifficultyLevel.EXPERT -> mediumPuzzles[index % mediumPuzzles.size]
        }
        val size = when (difficulty) {
            DifficultyLevel.EASY -> 5
            DifficultyLevel.MEDIUM -> 6
            DifficultyLevel.HARD, DifficultyLevel.EXPERT -> 6
        }

        val rowClues = (0 until size).map { row ->
            parseClues((0 until size).map { col -> solution[row * size + col] })
        }
        val colClues = (0 until size).map { col ->
            parseClues((0 until size).map { row -> solution[row * size + col] })
        }

        return NonogramPuzzle(size, solution, rowClues, colClues)
    }

    private fun parseClues(row: List<Boolean>): String {
        val asStr = row.joinToString("") { if (it) "1" else "0" }
        val blocks = asStr.split('0').filter { it.isNotBlank() }
        if (blocks.isEmpty()) return "0"
        return blocks.map { it.length.toString() }.joinToString(" ")
    }
}

data class NonogramState(
    val difficulty: DifficultyLevel = DifficultyLevel.EASY,
    val puzzleIndex: Int = 0,
    val puzzle: NonogramPuzzle = NonogramRepository.getPuzzle(DifficultyLevel.EASY, 0),
    val filledCells: List<Boolean> = emptyList(),
    val markedCells: List<Boolean> = emptyList(),
    val markMode: Boolean = false,
    val moves: Int = 0,
    val hintsUsed: Int = 0,
    val score: Int = 0,
    val bestScore: Int = 0,
    val timeElapsedSeconds: Int = 0,
    val isWon: Boolean = false,
    val isGameOver: Boolean = false,
    val isPaused: Boolean = false
)

class NonogramGameEngine {

    var state = NonogramState()
        private set

    fun startNewGame(
        difficulty: DifficultyLevel = DifficultyLevel.EASY,
        puzzleIndex: Int = 0,
        bestScore: Int = 0
    ) {
        val puzzle = NonogramRepository.getPuzzle(difficulty, puzzleIndex)
        val total = puzzle.size * puzzle.size

        state = NonogramState(
            difficulty = difficulty,
            puzzleIndex = puzzleIndex,
            puzzle = puzzle,
            filledCells = List(total) { false },
            markedCells = List(total) { false },
            markMode = false,
            moves = 0,
            hintsUsed = 0,
            score = 0,
            bestScore = bestScore,
            timeElapsedSeconds = 0,
            isWon = false,
            isGameOver = false,
            isPaused = false
        )
    }

    fun toggleMarkMode() {
        if (!state.isWon) {
            state = state.copy(markMode = !state.markMode)
        }
    }

    fun toggleCell(index: Int): Boolean {
        if (state.isWon || index !in state.filledCells.indices) return false

        val filled = state.filledCells.toMutableList()
        val marked = state.markedCells.toMutableList()

        if (state.markMode) {
            marked[index] = !marked[index]
            filled[index] = false
        } else {
            filled[index] = !filled[index]
            marked[index] = false
        }

        val updatedMoves = state.moves + 1
        val isWon = filled == state.puzzle.solution

        val newScore = if (isWon) {
            val base = state.puzzle.size * 200
            val multiplier = state.difficulty.scoreMultiplier
            val hintPenalty = state.hintsUsed * 25
            ((base * multiplier).toInt() - hintPenalty).coerceAtLeast(100)
        } else state.score

        val newBest = maxOf(newScore, state.bestScore)

        state = state.copy(
            filledCells = filled,
            markedCells = marked,
            moves = updatedMoves,
            score = newScore,
            bestScore = newBest,
            isWon = isWon
        )
        return isWon
    }

    fun useHint(): Boolean {
        if (state.isWon) return false
        val solution = state.puzzle.solution
        val hiddenCorrectIndices = solution.indices.filter { solution[it] && !state.filledCells[it] }
        if (hiddenCorrectIndices.isEmpty()) return false

        val hintIdx = hiddenCorrectIndices.random()
        val filled = state.filledCells.toMutableList()
        val marked = state.markedCells.toMutableList()
        filled[hintIdx] = true
        marked[hintIdx] = false

        val isWon = filled == solution
        val newScore = if (isWon) 200 else state.score

        state = state.copy(
            filledCells = filled,
            markedCells = marked,
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
