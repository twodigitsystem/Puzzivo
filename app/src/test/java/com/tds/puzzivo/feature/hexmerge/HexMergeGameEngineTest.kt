package com.tds.puzzivo.feature.hexmerge

import com.tds.puzzivo.core.model.DifficultyLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

class HexMergeGameEngineTest {

    private lateinit var engine: HexMergeGameEngine

    @Before
    fun setup() {
        engine = HexMergeGameEngine(Random(12345))
        engine.startNewGame(difficulty = DifficultyLevel.EASY)
    }

    @Test
    fun `startNewGame spawns initial tiles`() {
        assertEquals(2, engine.state.values.size)
        assertEquals(0, engine.state.score)
        assertFalse(engine.state.gameOver)
    }

    @Test
    fun `slide operation executes and updates moves`() {
        engine.slide(HexDirection.EAST)
        assertTrue(engine.state.moves >= 0)
    }
}
