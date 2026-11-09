package com.tds.puzzivo.feature.nonogram

import com.tds.puzzivo.core.model.DifficultyLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NonogramGameEngineTest {

    private lateinit var engine: NonogramGameEngine

    @Before
    fun setup() {
        engine = NonogramGameEngine()
        engine.startNewGame(difficulty = DifficultyLevel.EASY, puzzleIndex = 0)
    }

    @Test
    fun `startNewGame initializes puzzle size and clues`() {
        assertEquals(5, engine.state.puzzle.size)
        assertEquals(5, engine.state.puzzle.rowClues.size)
        assertEquals(5, engine.state.puzzle.colClues.size)
        assertFalse(engine.state.isWon)
    }

    @Test
    fun `toggleCell updates filled or marked state`() {
        engine.toggleCell(0)
        assertTrue(engine.state.filledCells[0] || engine.state.markedCells[0])
        assertEquals(1, engine.state.moves)
    }

    @Test
    fun `useHint reveals a correct correct cell`() {
        val success = engine.useHint()
        assertTrue(success)
        assertTrue(engine.state.filledCells.contains(true))
        assertEquals(1, engine.state.hintsUsed)
    }
}
