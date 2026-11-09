package com.tds.puzzivo.feature.hexmerge

import com.tds.puzzivo.core.model.DifficultyLevel
import kotlin.random.Random

data class HexCoord(val q: Int, val r: Int) {
    val s: Int get() = -q - r
}

enum class HexDirection(val dq: Int, val dr: Int) {
    EAST(1, 0),
    SOUTH_EAST(0, 1),
    SOUTH_WEST(-1, 1),
    WEST(-1, 0),
    NORTH_WEST(0, -1),
    NORTH_EAST(1, -1)
}

val hexCoords = buildList {
    for (r in -2..2) {
        val minQ = maxOf(-2, -r - 2)
        val maxQ = minOf(2, -r + 2)
        for (q in minQ..maxQ) add(HexCoord(q, r))
    }
}

data class HexMergeState(
    val difficulty: DifficultyLevel = DifficultyLevel.MEDIUM,
    val values: Map<HexCoord, Int> = emptyMap(),
    val score: Int = 0,
    val bestScore: Int = 0,
    val moves: Int = 0,
    val gameOver: Boolean = false,
    val isPaused: Boolean = false
)

class HexMergeGameEngine(private val random: Random = Random.Default) {

    var state = HexMergeState()
        private set

    fun startNewGame(
        difficulty: DifficultyLevel = DifficultyLevel.MEDIUM,
        bestScore: Int = 0
    ) {
        var board = mapOf<HexCoord, Int>()
        // Spawn 2 initial tiles
        val empty = hexCoords.shuffled(random).take(2)
        val mutable = board.toMutableMap()
        empty.forEach { coord ->
            mutable[coord] = if (random.nextFloat() < 0.9f) 2 else 4
        }

        state = HexMergeState(
            difficulty = difficulty,
            values = mutable,
            score = 0,
            bestScore = bestScore,
            moves = 0,
            gameOver = false,
            isPaused = false
        )
    }

    fun slide(direction: HexDirection): Boolean {
        if (state.gameOver) return false

        // Group coordinates into lines along direction axis
        val currentValues = state.values.toMutableMap()
        var moved = false
        var gainedScore = 0

        // Simplified line sorting based on direction vector projection
        val sortedCoords = when (direction) {
            HexDirection.EAST -> hexCoords.sortedByDescending { it.q }
            HexDirection.WEST -> hexCoords.sortedBy { it.q }
            HexDirection.SOUTH_EAST -> hexCoords.sortedByDescending { it.r }
            HexDirection.NORTH_WEST -> hexCoords.sortedBy { it.r }
            HexDirection.SOUTH_WEST -> hexCoords.sortedByDescending { it.s }
            HexDirection.NORTH_EAST -> hexCoords.sortedBy { it.s }
        }

        val mergedThisTurn = mutableSetOf<HexCoord>()

        for (coord in sortedCoords) {
            val value = currentValues[coord] ?: continue
            var curr = coord

            while (true) {
                val next = HexCoord(curr.q + direction.dq, curr.r + direction.dr)
                if (next !in hexCoords) break

                val nextVal = currentValues[next]
                if (nextVal == null) {
                    currentValues.remove(curr)
                    currentValues[next] = value
                    curr = next
                    moved = true
                } else if (nextVal == value && next !in mergedThisTurn && curr !in mergedThisTurn) {
                    currentValues.remove(curr)
                    val newVal = value * 2
                    currentValues[next] = newVal
                    mergedThisTurn.add(next)
                    gainedScore += newVal
                    moved = true
                    break
                } else {
                    break
                }
            }
        }

        if (!moved) return false

        val newScore = state.score + (gainedScore * state.difficulty.scoreMultiplier).toInt()
        val newBest = maxOf(newScore, state.bestScore)

        // Spawn new random tile (2 or 4)
        val emptyCoords = hexCoords.filter { it !in currentValues }
        if (emptyCoords.isNotEmpty()) {
            val spawnCoord = emptyCoords.random(random)
            currentValues[spawnCoord] = if (random.nextFloat() < 0.85f) 2 else 4
        }

        val isOver = emptyCoords.size <= 1 && !canAnyMerge(currentValues)

        state = state.copy(
            values = currentValues,
            score = newScore,
            bestScore = newBest,
            moves = state.moves + 1,
            gameOver = isOver
        )
        return true
    }

    private fun canAnyMerge(values: Map<HexCoord, Int>): Boolean {
        for ((coord, v) in values) {
            for (dir in HexDirection.entries) {
                val neighbor = HexCoord(coord.q + dir.dq, coord.r + dir.dr)
                if (values[neighbor] == v) return true
            }
        }
        return false
    }
}
