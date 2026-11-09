package com.tds.puzzivo.feature.wordscramble

import com.tds.puzzivo.core.model.DifficultyLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class WordScrambleGameEngineTest {

    private lateinit var engine: WordScrambleGameEngine

    @Before
    fun setup() {
        engine = WordScrambleGameEngine()
        engine.startNewGame(difficulty = DifficultyLevel.EASY, bestScore = 500, roundCount = 3)
    }

    @Test
    fun `startNewGame initializes state correctly`() {
        assertEquals(DifficultyLevel.EASY, engine.state.difficulty)
        assertEquals(3, engine.state.totalRounds)
        assertEquals(0, engine.state.roundIndex)
        assertEquals(0, engine.state.score)
        assertEquals(500, engine.state.bestScore)
        assertNotNull(engine.state.currentRound)
        assertFalse(engine.state.isGameComplete)
    }

    @Test
    fun `selectLetter toggles and adds letter to selection`() {
        val round = engine.state.currentRound!!
        val firstLetterId = round.letters[0].id

        engine.selectLetter(firstLetterId)
        assertEquals(listOf(firstLetterId), engine.state.selectedLetterIds)

        // Select second letter
        val secondLetterId = round.letters[1].id
        engine.selectLetter(secondLetterId)
        assertEquals(listOf(firstLetterId, secondLetterId), engine.state.selectedLetterIds)

        // Toggle first letter to deselect
        engine.selectLetter(firstLetterId)
        assertEquals(listOf(secondLetterId), engine.state.selectedLetterIds)
    }

    @Test
    fun `backspace removes last selected letter`() {
        val round = engine.state.currentRound!!
        val id1 = round.letters[0].id
        val id2 = round.letters[1].id

        engine.selectLetter(id1)
        engine.selectLetter(id2)
        assertEquals(listOf(id1, id2), engine.state.selectedLetterIds)

        engine.backspace()
        assertEquals(listOf(id1), engine.state.selectedLetterIds)
    }

    @Test
    fun `clearSelection empties selected letters`() {
        val round = engine.state.currentRound!!
        engine.selectLetter(round.letters[0].id)
        engine.selectLetter(round.letters[1].id)

        engine.clearSelection()
        assertTrue(engine.state.selectedLetterIds.isEmpty())
    }

    @Test
    fun `shuffleUnselectedLetters preserves selected letters in round`() {
        val round = engine.state.currentRound!!
        val selectedId = round.letters[0].id
        engine.selectLetter(selectedId)

        engine.shuffleUnselectedLetters()
        val updatedRound = engine.state.currentRound!!
        assertTrue(updatedRound.letters.any { it.id == selectedId })
        assertEquals(listOf(selectedId), engine.state.selectedLetterIds)
    }

    @Test
    fun `submitting wrong answer resets combo streak and sets error state`() {
        val round = engine.state.currentRound!!
        round.letters.forEach { engine.selectLetter(it.id) }

        val result = engine.submitAnswer()
        if (result == WordSubmitResult.WRONG) {
            assertTrue(engine.state.isErrorState)
            assertEquals("Not quite", engine.state.feedbackMessage)
            assertEquals(0, engine.state.comboStreak)
        }
    }

    @Test
    fun `submitting correct answer increases combo streak and provides solved definition`() {
        val round = engine.state.currentRound!!
        val targetWord = round.word.word

        val letterMap = round.letters.groupBy { it.char }.mapValues { it.value.toMutableList() }
        targetWord.forEach { char ->
            val letter = letterMap[char]?.removeAt(0)!!
            engine.selectLetter(letter.id)
        }

        val result = engine.submitAnswer()
        assertTrue(result == WordSubmitResult.CORRECT || result == WordSubmitResult.GAME_COMPLETE)
        assertTrue(engine.state.score > 0)
        assertEquals(1, engine.state.comboStreak)
        assertNotNull(engine.state.solvedDefinition)
        assertFalse(engine.state.isErrorState)
    }

    @Test
    fun `useHint reveals and selects correct letter`() {
        val success = engine.useHint()
        assertTrue(success)
        assertEquals(1, engine.state.selectedLetterIds.size)
        assertEquals(1, engine.state.hintsUsed)

        val targetChar = engine.state.currentRound!!.word.word[0]
        val selectedLetter = engine.state.currentRound!!.letters.first { it.id == engine.state.selectedLetterIds[0] }
        assertEquals(targetChar, selectedLetter.char)
    }
}
