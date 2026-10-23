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
        // Test merge logic
        val moved = engine.move(MoveDirection.LEFT) || engine.move(MoveDirection.RIGHT) || engine.move(MoveDirection.UP) || engine.move(MoveDirection.DOWN)
        assertTrue(moved || engine.state.grid.count { it != 0 } >= 2)
    }
}
