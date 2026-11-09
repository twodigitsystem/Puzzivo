package com.tds.puzzivo.feature.minesweeper

import com.tds.puzzivo.core.model.DifficultyLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

class MinesweeperGameEngineTest {

    private lateinit var engine: MinesweeperGameEngine

    @Before
    fun setup() {
        engine = MinesweeperGameEngine(Random(12345))
        engine.startNewGame(difficulty = DifficultyLevel.EASY)
    }

    @Test
    fun `startNewGame initializes grid size and mine count`() {
        assertEquals(5, engine.state.boardSize)
        assertEquals(4, engine.state.totalMines)
        assertEquals(25, engine.state.cells.size)
        assertFalse(engine.state.firstClickDone)
        assertFalse(engine.state.isGameOver)
    }

    @Test
    fun `first click provides safe area and generates board`() {
        val clickIndex = 12 // center cell
        engine.onCellClick(clickIndex)

        assertTrue(engine.state.firstClickDone)
        assertFalse(engine.state.cells[clickIndex].mine)
        assertTrue(engine.state.cells[clickIndex].revealed)
    }

    @Test
    fun `toggleFlagMode changes flag mode state`() {
        assertFalse(engine.state.flagMode)
        engine.toggleFlagMode()
        assertTrue(engine.state.flagMode)
    }

    @Test
    fun `useSafeRevealHint reveals a unrevealed safe cell`() {
        engine.onCellClick(0) // Initialize board
        val initialRevealed = engine.state.cells.count { it.revealed }
        val success = engine.useSafeRevealHint()

        assertTrue(success)
        assertTrue(engine.state.cells.count { it.revealed } > initialRevealed)
        assertEquals(1, engine.state.hintsUsed)
    }
}
