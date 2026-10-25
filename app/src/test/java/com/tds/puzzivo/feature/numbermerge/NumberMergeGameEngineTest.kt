package com.tds.puzzivo.feature.numbermerge

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

class NumberMergeGameEngineTest {

    private lateinit var engine: NumberMergeGameEngine

    @Before
    fun setup() {
        engine = NumberMergeGameEngine(Random(12345))
        engine.startNewGame(bestScore = 100)
    }

    @Test
    fun `new game starts with two tiles and initial score zero`() {
        val nonZeroTiles = engine.state.grid.count { it != 0 }
        assertEquals(2, nonZeroTiles)
        assertEquals(0, engine.state.score)
        assertEquals(100, engine.state.bestScore)
        assertFalse(engine.state.isGameOver)
    }

    @Test
    fun `undo last move restores previous state and preserves 2048 milestone`() {
        val initialGrid = engine.state.grid
        val initialScore = engine.state.score

        val moved = engine.move(MoveDirection.LEFT)
        if (moved) {
            assertTrue(engine.state.canUndo)
            val undoSuccess = engine.undoLastMove()
            assertTrue(undoSuccess)
            assertEquals(initialGrid, engine.state.grid)
            assertEquals(initialScore, engine.state.score)
            assertFalse(engine.state.canUndo)
        }
    }

    @Test
    fun `move slides and merges adjacent equal tiles correctly`() {
        engine.setStateForTesting(
            grid = listOf(
                2, 2, 2, 2,
                0, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 0
            )
        )

        assertTrue(engine.move(MoveDirection.LEFT))
        assertEquals(listOf(4, 4, 0, 0), engine.state.grid.take(4))
        assertEquals(8, engine.state.score)
    }

    @Test
    fun `no-op move does not spawn a tile`() {
        val grid = listOf(
            2, 0, 0, 0,
            0, 0, 0, 0,
            0, 0, 0, 0,
            0, 0, 0, 0
        )
        engine.setStateForTesting(grid)

        assertFalse(engine.move(MoveDirection.LEFT))
        assertEquals(grid, engine.state.grid)
    }

    @Test
    fun `a full board without merges is game over after the final spawn`() {
        engine.setStateForTesting(
            grid = listOf(
                0, 2, 4, 8,
                16, 32, 64, 128,
                256, 512, 1024, 2048,
                4096, 8192, 16384, 32768
            )
        )

        assertTrue(engine.move(MoveDirection.LEFT))
        assertTrue(engine.state.isGameOver)
    }

    @Test
    fun `a full board with an available merge is not game over`() {
        engine.setStateForTesting(
            grid = listOf(
                2, 2, 4, 8,
                16, 32, 64, 128,
                256, 512, 1024, 2048,
                4096, 8192, 16384, 32768
            )
        )

        assertTrue(engine.move(MoveDirection.LEFT))
        assertFalse(engine.state.isGameOver)
    }

    @Test
    fun `merging 1024 tiles records the 2048 milestone`() {
        engine.setStateForTesting(
            grid = listOf(
                1024, 1024, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 0
            )
        )

        engine.move(MoveDirection.LEFT)
        assertTrue(engine.state.hasReached2048)
    }
}
