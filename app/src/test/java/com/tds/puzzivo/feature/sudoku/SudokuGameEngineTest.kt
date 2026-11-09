package com.tds.puzzivo.feature.sudoku

import com.tds.puzzivo.core.model.DifficultyLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SudokuGameEngineTest {

    private lateinit var engine: SudokuGameEngine

    @Before
    fun setup() {
        engine = SudokuGameEngine()
        engine.startNewGame(difficulty = DifficultyLevel.EASY, puzzleNumber = 0)
    }

    @Test
    fun `startNewGame populates puzzle and givens`() {
        assertEquals(36, engine.state.values.size)
        assertTrue(engine.state.puzzle.givens.isNotEmpty())
        assertFalse(engine.state.isWon)
    }

    @Test
    fun `fillValue fills non-given cell and checks correctness`() {
        val nonGivenIndex = (0 until 36).first { it !in engine.state.puzzle.givens }
        engine.selectCell(nonGivenIndex)

        val correctVal = engine.state.puzzle.solution[nonGivenIndex]
        val isCorrect = engine.fillValue(correctVal)

        assertTrue(isCorrect)
        assertEquals(correctVal, engine.state.values[nonGivenIndex])
    }

    @Test
    fun `useHint fills correct value into selected cell`() {
        val nonGivenIndex = (0 until 36).first { it !in engine.state.puzzle.givens }
        engine.selectCell(nonGivenIndex)

        val success = engine.useHint()
        assertTrue(success)
        assertEquals(engine.state.puzzle.solution[nonGivenIndex], engine.state.values[nonGivenIndex])
        assertEquals(1, engine.state.hintsUsed)
    }
}
