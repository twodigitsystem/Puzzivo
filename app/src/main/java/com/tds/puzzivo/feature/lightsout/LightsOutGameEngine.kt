package com.tds.puzzivo.feature.lightsout

import com.tds.puzzivo.core.model.DifficultyLevel
import kotlin.random.Random

data class LightsOutState(
    val difficulty: DifficultyLevel = DifficultyLevel.MEDIUM,
    val gridSize: Int = 4,
    val lights: List<Boolean> = emptyList(),
    val moves: Int = 0,
    val hintsUsed: Int = 0,
    val hintCellIndex: Int? = null,
    val score: Int = 0,
    val bestScore: Int = 0,
    val timeElapsedSeconds: Int = 0,
    val isGameComplete: Boolean = false,
    val isPaused: Boolean = false
)

class LightsOutGameEngine(private val random: Random = Random.Default) {

    var state = LightsOutState()
        private set

    fun startNewGame(
        difficulty: DifficultyLevel = DifficultyLevel.MEDIUM,
        bestScore: Int = 0
    ) {
        val size = when (difficulty) {
            DifficultyLevel.EASY -> 3
            DifficultyLevel.MEDIUM -> 4
            DifficultyLevel.HARD, DifficultyLevel.EXPERT -> 5
        }

        var board = List(size * size) { false }
        val numTaps = when (difficulty) {
            DifficultyLevel.EASY -> random.nextInt(3, 6)
            DifficultyLevel.MEDIUM -> random.nextInt(5, 9)
            DifficultyLevel.HARD, DifficultyLevel.EXPERT -> random.nextInt(8, 14)
        }

        val solutionTaps = mutableListOf<Int>()
        val availableIndices = (0 until size * size).toMutableList()
        repeat(numTaps) {
            if (availableIndices.isNotEmpty()) {
                val idx = availableIndices.removeAt(random.nextInt(availableIndices.size))
                solutionTaps.add(idx)
                board = toggle(board, idx, size)
            }
        }

        // Ensure board isn't accidentally already cleared
        if (board.none { it }) {
            val idx = random.nextInt(size * size)
            solutionTaps.add(idx)
            board = toggle(board, idx, size)
        }

        state = LightsOutState(
            difficulty = difficulty,
            gridSize = size,
            lights = board,
            moves = 0,
            hintsUsed = 0,
            hintCellIndex = null,
            score = 0,
            bestScore = bestScore,
            timeElapsedSeconds = 0,
            isGameComplete = false,
            isPaused = false
        )
    }

    fun tapCell(index: Int): Boolean {
        if (state.isGameComplete || index !in state.lights.indices) return false

        val updatedBoard = toggle(state.lights, index, state.gridSize)
        val updatedMoves = state.moves + 1
        val isCleared = updatedBoard.none { it }

        val newScore = if (isCleared) {
            val base = state.gridSize * 250
            val multiplier = state.difficulty.scoreMultiplier
            val movePenalty = updatedMoves * 15
            val hintPenalty = state.hintsUsed * 30
            val calculated = ((base * multiplier).toInt() - movePenalty - hintPenalty).coerceAtLeast(100)
            calculated
        } else state.score

        val updatedBest = maxOf(newScore, state.bestScore)

        state = state.copy(
            lights = updatedBoard,
            moves = updatedMoves,
            score = newScore,
            bestScore = updatedBest,
            hintCellIndex = null,
            isGameComplete = isCleared
        )
        return isCleared
    }

    fun useHint(): Boolean {
        if (state.isGameComplete) return false
        val litIndices = state.lights.mapIndexedNotNull { index, isLit -> if (isLit) index else null }
        if (litIndices.isEmpty()) return false

        val hintIdx = litIndices.random(random)
        state = state.copy(
            hintsUsed = state.hintsUsed + 1,
            hintCellIndex = hintIdx
        )
        return true
    }

    fun tickTimer() {
        if (!state.isGameComplete && !state.isPaused) {
            state = state.copy(timeElapsedSeconds = state.timeElapsedSeconds + 1)
        }
    }

    private fun toggle(board: List<Boolean>, index: Int, size: Int): List<Boolean> {
        val row = index / size
        val col = index % size
        val result = board.toMutableList()

        val neighbors = listOf(
            row to col,
            row - 1 to col,
            row + 1 to col,
            row to col - 1,
            row to col + 1
        )

        neighbors.forEach { (r, c) ->
            if (r in 0 until size && c in 0 until size) {
                val target = r * size + c
                result[target] = !result[target]
            }
        }
        return result
    }
}
