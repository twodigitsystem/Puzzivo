package com.tds.puzzivo.feature.minesweeper

import com.tds.puzzivo.core.model.DifficultyLevel
import kotlin.random.Random

data class MineCell(
    val mine: Boolean = false,
    val adjacent: Int = 0,
    val revealed: Boolean = false,
    val flagged: Boolean = false
)

data class MinesweeperState(
    val difficulty: DifficultyLevel = DifficultyLevel.MEDIUM,
    val boardSize: Int = 6,
    val totalMines: Int = 8,
    val cells: List<MineCell> = emptyList(),
    val flagMode: Boolean = false,
    val moves: Int = 0,
    val hintsUsed: Int = 0,
    val score: Int = 0,
    val bestScore: Int = 0,
    val timeElapsedSeconds: Int = 0,
    val firstClickDone: Boolean = false,
    val isGameOver: Boolean = false,
    val isWon: Boolean = false,
    val isPaused: Boolean = false
)

class MinesweeperGameEngine(private val random: Random = Random.Default) {

    var state = MinesweeperState()
        private set

    fun startNewGame(
        difficulty: DifficultyLevel = DifficultyLevel.MEDIUM,
        bestScore: Int = 0
    ) {
        val size = when (difficulty) {
            DifficultyLevel.EASY -> 5
            DifficultyLevel.MEDIUM -> 6
            DifficultyLevel.HARD, DifficultyLevel.EXPERT -> 8
        }
        val mines = when (difficulty) {
            DifficultyLevel.EASY -> 4
            DifficultyLevel.MEDIUM -> 8
            DifficultyLevel.HARD, DifficultyLevel.EXPERT -> 14
        }

        val emptyCells = List(size * size) { MineCell() }

        state = MinesweeperState(
            difficulty = difficulty,
            boardSize = size,
            totalMines = mines,
            cells = emptyCells,
            flagMode = false,
            moves = 0,
            hintsUsed = 0,
            score = 0,
            bestScore = bestScore,
            timeElapsedSeconds = 0,
            firstClickDone = false,
            isGameOver = false,
            isWon = false,
            isPaused = false
        )
    }

    fun toggleFlagMode() {
        if (!state.isGameOver && !state.isWon) {
            state = state.copy(flagMode = !state.flagMode)
        }
    }

    fun onCellClick(index: Int) {
        if (state.isGameOver || state.isWon || index !in state.cells.indices) return

        if (!state.firstClickDone) {
            generateBoardWithFirstClickSafety(index)
        }

        if (state.flagMode) {
            toggleFlag(index)
        } else {
            revealCell(index)
        }
    }

    fun useSafeRevealHint(): Boolean {
        if (state.isGameOver || state.isWon) return false
        val safeHiddenIndices = state.cells.mapIndexedNotNull { idx, cell ->
            if (!cell.revealed && !cell.flagged && !cell.mine) idx else null
        }
        if (safeHiddenIndices.isEmpty()) return false

        val hintIdx = safeHiddenIndices.random(random)
        state = state.copy(hintsUsed = state.hintsUsed + 1)
        revealCell(hintIdx)
        return true
    }

    fun tickTimer() {
        if (!state.isGameOver && !state.isWon && !state.isPaused) {
            state = state.copy(timeElapsedSeconds = state.timeElapsedSeconds + 1)
        }
    }

    private fun generateBoardWithFirstClickSafety(firstIndex: Int) {
        val size = state.boardSize
        val total = size * size
        val allIndices = (0 until total).toMutableList()

        // Exclude firstIndex and its immediate neighbors to ensure safe starting area
        val firstRow = firstIndex / size
        val firstCol = firstIndex % size
        val safeZone = mutableSetOf<Int>()

        for (dr in -1..1) for (dc in -1..1) {
            val r = firstRow + dr
            val c = firstCol + dc
            if (r in 0 until size && c in 0 until size) {
                safeZone.add(r * size + c)
            }
        }

        allIndices.removeAll(safeZone)
        val mineIndices = allIndices.shuffled(random).take(state.totalMines.coerceAtMost(allIndices.size)).toSet()

        val cells = (0 until total).map { idx ->
            val isMine = idx in mineIndices
            val adj = countAdjacent(idx, mineIndices, size)
            MineCell(mine = isMine, adjacent = adj)
        }

        state = state.copy(cells = cells, firstClickDone = true)
    }

    private fun countAdjacent(index: Int, mines: Set<Int>, size: Int): Int {
        val row = index / size
        val col = index % size
        var count = 0
        for (dr in -1..1) for (dc in -1..1) {
            if (dr == 0 && dc == 0) continue
            val r = row + dr
            val c = col + dc
            if (r in 0 until size && c in 0 until size && (r * size + c) in mines) {
                count++
            }
        }
        return count
    }

    private fun toggleFlag(index: Int) {
        val cell = state.cells[index]
        if (cell.revealed) return
        val currentFlags = state.cells.count { it.flagged }
        if (!cell.flagged && currentFlags >= state.totalMines) return

        val updatedCells = state.cells.toMutableList()
        updatedCells[index] = cell.copy(flagged = !cell.flagged)
        state = state.copy(cells = updatedCells)
    }

    private fun revealCell(index: Int) {
        val cell = state.cells[index]
        if (cell.revealed || cell.flagged) return

        val updatedCells = state.cells.toMutableList()

        if (cell.mine) {
            // Mine hit - game over
            updatedCells.indices.filter { updatedCells[it].mine }.forEach { mineIdx ->
                updatedCells[mineIdx] = updatedCells[mineIdx].copy(revealed = true)
            }
            state = state.copy(
                cells = updatedCells,
                moves = state.moves + 1,
                isGameOver = true
            )
            return
        }

        // BFS / Flood fill for zero adjacent cells
        val queue = ArrayDeque<Int>()
        queue.add(index)

        while (queue.isNotEmpty()) {
            val curr = queue.removeFirst()
            if (curr !in updatedCells.indices || updatedCells[curr].revealed || updatedCells[curr].flagged || updatedCells[curr].mine) continue

            updatedCells[curr] = updatedCells[curr].copy(revealed = true)

            if (updatedCells[curr].adjacent == 0) {
                val row = curr / state.boardSize
                val col = curr % state.boardSize
                for (dr in -1..1) for (dc in -1..1) {
                    val r = row + dr
                    val c = col + dc
                    if (r in 0 until state.boardSize && c in 0 until state.boardSize) {
                        queue.add(r * state.boardSize + c)
                    }
                }
            }
        }

        val safeRevealed = updatedCells.count { it.revealed && !it.mine }
        val safeTotal = updatedCells.count { !it.mine }
        val isWon = safeRevealed == safeTotal

        val calculatedScore = if (isWon) {
            val base = safeTotal * 50
            val multiplier = state.difficulty.scoreMultiplier
            val hintPenalty = state.hintsUsed * 20
            ((base * multiplier).toInt() - hintPenalty).coerceAtLeast(100)
        } else state.score

        val updatedBest = maxOf(calculatedScore, state.bestScore)

        state = state.copy(
            cells = updatedCells,
            moves = state.moves + 1,
            score = calculatedScore,
            bestScore = updatedBest,
            isWon = isWon
        )
    }
}
