package com.tds.puzzivo.feature.lightsout

import com.tds.puzzivo.core.model.DifficultyLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

class LightsOutGameEngineTest {

    private lateinit var engine: LightsOutGameEngine

    @Before
    fun setup() {
        engine = LightsOutGameEngine(Random(12345))
        engine.startNewGame(difficulty = DifficultyLevel.EASY)
    }

    @Test
    fun `startNewGame initializes grid size based on difficulty`() {
        assertEquals(3, engine.state.gridSize)
        assertEquals(9, engine.state.lights.size)
        assertEquals(0, engine.state.moves)
        assertFalse(engine.state.isGameComplete)
    }

    @Test
    fun `tapCell toggles cell and neighbors`() {
        val initialBoard = engine.state.lights.toList()
        engine.tapCell(0) // Top-left cell (0,0)
        val updatedBoard = engine.state.lights

        // Cell 0, Cell 1 (right), Cell 3 (down) should be toggled
        assertEquals(!initialBoard[0], updatedBoard[0])
        assertEquals(!initialBoard[1], updatedBoard[1])
        assertEquals(!initialBoard[3], updatedBoard[3])
        assertEquals(1, engine.state.moves)
    }

    @Test
    fun `useHint sets hint cell index`() {
        val success = engine.useHint()
        assertTrue(success)
        assertNotNull(engine.state.hintCellIndex)
        assertEquals(1, engine.state.hintsUsed)
    }
}
