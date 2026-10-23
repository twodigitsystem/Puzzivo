package com.tds.puzzivo.feature.numbermerge

import kotlin.random.Random

enum class MoveDirection { UP, DOWN, LEFT, RIGHT }

data class BoardSnapshot(
    val grid: List<Int>,
    val score: Int,
    val isGameOver: Boolean,
    val hasReached2048: Boolean
)

data class NumberMergeState(
    val grid: List<Int> = List(16) { 0 }, // 4x4 flat grid
    val score: Int = 0,
    val bestScore: Int = 0,
    val isGameOver: Boolean = false,
    val hasReached2048: Boolean = false,
    val canUndo: Boolean = false
)

class NumberMergeGameEngine(
    private val random: Random = Random.Default
) {
    var state = NumberMergeState()
        private set

    private var previousSnapshot: BoardSnapshot? = null

    fun startNewGame(bestScore: Int = 0) {
        val emptyGrid = MutableList(16) { 0 }
        spawnTile(emptyGrid)
        spawnTile(emptyGrid)
        previousSnapshot = null

        state = NumberMergeState(
            grid = emptyGrid.toList(),
            score = 0,
            bestScore = bestScore,
            isGameOver = false,
            hasReached2048 = false,
            canUndo = false
        )
    }

    fun move(direction: MoveDirection): Boolean {
        if (state.isGameOver) return false

        val currentGrid = state.grid.toMutableList()
        val currentScore = state.score

        val (newGrid, pointsEarned) = processSlideAndMerge(currentGrid, direction)

        // Check if board changed
        if (newGrid == currentGrid) {
            return false // Invalid move, no change
        }

        // Save snapshot before mutating state for Undo feature
        previousSnapshot = BoardSnapshot(
            grid = state.grid,
            score = state.score,
            isGameOver = state.isGameOver,
            hasReached2048 = state.hasReached2048
        )

        // Spawn new tile in empty spot
        spawnTile(newGrid)

        val newScore = currentScore + pointsEarned
        val newBestScore = maxOf(newScore, state.bestScore)
        val reached2048Now = state.hasReached2048 || newGrid.contains(2048)
        val gameOver = isBoardGameOver(newGrid)

        state = state.copy(
            grid = newGrid.toList(),
            score = newScore,
            bestScore = newBestScore,
            isGameOver = gameOver,
            hasReached2048 = reached2048Now,
            canUndo = true
        )

        return true
    }

    fun undoLastMove(): Boolean {
        val snapshot = previousSnapshot ?: return false
        state = state.copy(
            grid = snapshot.grid,
            score = snapshot.score,
            isGameOver = snapshot.isGameOver,
            hasReached2048 = state.hasReached2048 || snapshot.hasReached2048,
            canUndo = false
        )
        previousSnapshot = null
        return true
    }

    private fun spawnTile(grid: MutableList<Int>) {
        val emptyIndices = grid.indices.filter { grid[it] == 0 }
        if (emptyIndices.isNotEmpty()) {
            val randomIndex = emptyIndices[random.nextInt(emptyIndices.size)]
            grid[randomIndex] = if (random.nextFloat() < 0.9f) 2 else 4
        }
    }

    private fun processSlideAndMerge(
        grid: MutableList<Int>,
        direction: MoveDirection
    ): Pair<MutableList<Int>, Int> {
        val resultGrid = grid.toMutableList()
        var pointsGained = 0

        for (i in 0 until 4) {
            val lineIndices = when (direction) {
                MoveDirection.LEFT -> listOf(i * 4, i * 4 + 1, i * 4 + 2, i * 4 + 3)
                MoveDirection.RIGHT -> listOf(i * 4 + 3, i * 4 + 2, i * 4 + 1, i * 4)
                MoveDirection.UP -> listOf(i, i + 4, i + 8, i + 12)
                MoveDirection.DOWN -> listOf(i + 12, i + 8, i + 4, i)
            }

            val lineValues = lineIndices.map { resultGrid[it] }.filter { it != 0 }
            val mergedLine = mutableListOf<Int>()
            var idx = 0

            while (idx < lineValues.size) {
                if (idx + 1 < lineValues.size && lineValues[idx] == lineValues[idx + 1]) {
                    val mergedVal = lineValues[idx] * 2
                    mergedLine.add(mergedVal)
                    pointsGained += mergedVal
                    idx += 2
                } else {
                    mergedLine.add(lineValues[idx])
                    idx += 1
                }
            }

            while (mergedLine.size < 4) {
                mergedLine.add(0)
            }

            for (j in 0 until 4) {
                resultGrid[lineIndices[j]] = mergedLine[j]
            }
        }

        return Pair(resultGrid, pointsGained)
    }

    private fun isBoardGameOver(grid: List<Int>): Boolean {
        if (grid.contains(0)) return false

        // Check horizontal merges
        for (row in 0 until 4) {
            for (col in 0 until 3) {
                val idx = row * 4 + col
                if (grid[idx] == grid[idx + 1]) return false
            }
        }

        // Check vertical merges
        for (col in 0 until 4) {
            for (row in 0 until 3) {
                val idx = row * 4 + col
                if (grid[idx] == grid[idx + 4]) return false
            }
        }

        return true
    }
}
