package com.tds.puzzivo.feature.wordguess

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class WordGuessGameEngineTest {

    private lateinit var engine: WordGuessGameEngine

    @Before
    fun setUp() {
        engine = WordGuessGameEngine(wordList = listOf("PLANT"))
        engine.startNewGame("PLANT")
    }

    @Test
    fun typingLetterUpdatesCurrentRowWord() {
        engine.typeLetter('P')
        engine.typeLetter('L')
        assertEquals("PL", engine.state.rows[0].word)
    }

    @Test
    fun correctGuessWinsGame() {
        "PLANT".forEach { engine.typeLetter(it) }
        val submitted = engine.submitGuess()

        assertTrue(submitted)
        assertTrue(engine.state.isWon)
        assertTrue(engine.state.isGameComplete)
        assertEquals(LetterStatus.CORRECT, engine.state.rows[0].statuses[0])
    }

    @Test
    fun incorrectGuessMarksStatus() {
        "BRAIN".forEach { engine.typeLetter(it) }
        val submitted = engine.submitGuess()

        assertTrue(submitted)
        assertFalse(engine.state.isWon)
        assertFalse(engine.state.isGameComplete)
        assertEquals(LetterStatus.PRESENT, engine.state.rows[0].statuses[2])
    }
}
