package com.tds.puzzivo.feature.patternrecall

import com.tds.puzzivo.core.model.DifficultyLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

class PatternRecallGameEngineTest {

    private lateinit var engine: PatternRecallGameEngine

    @Before
    fun setup() {
        engine = PatternRecallGameEngine(Random(12345))
        engine.startNewGame(difficulty = DifficultyLevel.EASY)
    }

    @Test
    fun `startNewGame initializes grid and sequence`() {
        assertEquals(3, engine.state.gridSide)
        assertEquals(2, engine.state.sequence.size)
        assertEquals(1, engine.state.round)
        assertTrue(engine.state.isShowingSequence)
        assertFalse(engine.state.isGameOver)
    }

    @Test
    fun `tapCell with correct sequence advances step and round`() {
        engine.setIsShowingSequence(false)
        val targetCell = engine.state.sequence[0]
        val result = engine.tapCell(targetCell)

        assertEquals(PatternTapResult.CORRECT_STEP, result)
        assertEquals(1, engine.state.currentInputIndex)
    }

    @Test
    fun `tapCell with wrong sequence sets game over`() {
        engine.setIsShowingSequence(false)
        val wrongCell = (0..8).first { it != engine.state.sequence[0] }
        val result = engine.tapCell(wrongCell)

        assertEquals(PatternTapResult.WRONG, result)
        assertTrue(engine.state.isGameOver)
    }
}
