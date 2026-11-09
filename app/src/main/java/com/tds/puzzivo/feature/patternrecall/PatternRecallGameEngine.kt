package com.tds.puzzivo.feature.patternrecall

import com.tds.puzzivo.core.model.DifficultyLevel
import kotlin.random.Random

data class PatternRecallState(
    val difficulty: DifficultyLevel = DifficultyLevel.MEDIUM,
    val gridSide: Int = 3,
    val sequence: List<Int> = emptyList(),
    val currentInputIndex: Int = 0,
    val litCell: Int? = null,
    val isShowingSequence: Boolean = false,
    val round: Int = 1,
    val score: Int = 0,
    val bestScore: Int = 0,
    val timeElapsedSeconds: Int = 0,
    val isGameOver: Boolean = false,
    val isWon: Boolean = false,
    val isPaused: Boolean = false
)

enum class PatternTapResult {
    CORRECT_STEP,
    ROUND_COMPLETE,
    WRONG
}

class PatternRecallGameEngine(private val random: Random = Random.Default) {

    var state = PatternRecallState()
        private set

    fun startNewGame(
        difficulty: DifficultyLevel = DifficultyLevel.MEDIUM,
        bestScore: Int = 0
    ) {
        val side = when (difficulty) {
            DifficultyLevel.EASY -> 3
            DifficultyLevel.MEDIUM -> 3
            DifficultyLevel.HARD, DifficultyLevel.EXPERT -> 4
        }
        val initialLength = when (difficulty) {
            DifficultyLevel.EASY -> 2
            DifficultyLevel.MEDIUM -> 3
            DifficultyLevel.HARD, DifficultyLevel.EXPERT -> 4
        }

        val initialSeq = List(initialLength) { random.nextInt(side * side) }

        state = PatternRecallState(
            difficulty = difficulty,
            gridSide = side,
            sequence = initialSeq,
            currentInputIndex = 0,
            litCell = null,
            isShowingSequence = true,
            round = 1,
            score = 0,
            bestScore = bestScore,
            timeElapsedSeconds = 0,
            isGameOver = false,
            isWon = false,
            isPaused = false
        )
    }

    fun setLitCell(cellIndex: Int?) {
        state = state.copy(litCell = cellIndex)
    }

    fun setIsShowingSequence(showing: Boolean) {
        state = state.copy(
            isShowingSequence = showing,
            currentInputIndex = if (showing) 0 else state.currentInputIndex
        )
    }

    fun tapCell(cellIndex: Int): PatternTapResult {
        if (state.isShowingSequence || state.isGameOver) return PatternTapResult.WRONG
        if (cellIndex !in 0 until (state.gridSide * state.gridSide)) return PatternTapResult.WRONG

        val expectedCell = state.sequence.getOrNull(state.currentInputIndex)
        if (cellIndex != expectedCell) {
            state = state.copy(isGameOver = true)
            return PatternTapResult.WRONG
        }

        val nextIndex = state.currentInputIndex + 1
        return if (nextIndex >= state.sequence.size) {
            val nextRound = state.round + 1
            val nextCell = random.nextInt(state.gridSide * state.gridSide)
            val updatedSeq = state.sequence + nextCell

            val basePoints = state.sequence.size * 100
            val newScore = state.score + (basePoints * state.difficulty.scoreMultiplier).toInt()
            val newBest = maxOf(newScore, state.bestScore)

            state = state.copy(
                sequence = updatedSeq,
                currentInputIndex = 0,
                round = nextRound,
                score = newScore,
                bestScore = newBest,
                isShowingSequence = true
            )
            PatternTapResult.ROUND_COMPLETE
        } else {
            state = state.copy(currentInputIndex = nextIndex)
            PatternTapResult.CORRECT_STEP
        }
    }

    fun tickTimer() {
        if (!state.isGameOver && !state.isPaused) {
            state = state.copy(timeElapsedSeconds = state.timeElapsedSeconds + 1)
        }
    }
}
