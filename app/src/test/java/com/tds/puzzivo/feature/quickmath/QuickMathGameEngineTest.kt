package com.tds.puzzivo.feature.quickmath

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

class QuickMathGameEngineTest {

    private lateinit var engine: QuickMathGameEngine

    @Before
    fun setup() {
        engine = QuickMathGameEngine(Random(12345))
        engine.startNewGame(bestScore = 50)
    }

    @Test
    fun `question generated contains correct answer among 4 unique options`() {
        val q = engine.state.currentQuestion!!
        assertEquals(4, q.options.size)
        assertEquals(4, q.options.toSet().size) // All 4 unique
        assertTrue(q.options.contains(q.correctAnswer))
    }

    @Test
    fun `division questions always evaluate to exact integers and avoid division by zero`() {
        for (i in 0..50) {
            val q = engine.generateQuestion(i)
            if (q.expression.contains("/")) {
                val parts = q.expression.split(" / ")
                val num = parts[0].toInt()
                val den = parts[1].toInt()
                assertTrue(den > 0)
                assertEquals(0, num % den) // Exact division!
                assertEquals(num / den, q.correctAnswer)
            }
        }
    }

    @Test
    fun `answering correctly adds 10 points and updates best score`() {
        val q = engine.state.currentQuestion!!
        val isCorrect = engine.answerQuestion(q.correctAnswer)
        assertTrue(isCorrect)
        assertEquals(10, engine.state.score)
        assertEquals(50, engine.state.bestScore)
    }

    @Test
    fun `answering incorrectly awards 0 points and loads next question`() {
        val q = engine.state.currentQuestion!!
        val wrongChoice = q.options.first { it != q.correctAnswer }
        val isCorrect = engine.answerQuestion(wrongChoice)
        assertFalse(isCorrect)
        assertEquals(0, engine.state.score)
        assertEquals(1, engine.state.questionsAnswered)
    }

    @Test
    fun `timer ticks and ends game when reaching zero`() {
        assertEquals(60, engine.state.timeRemainingSeconds)
        engine.tickTimer()
        assertEquals(59, engine.state.timeRemainingSeconds)

        // Pause timer
        engine.setPaused(true)
        engine.tickTimer()
        assertEquals(59, engine.state.timeRemainingSeconds) // Timer paused

        // Resume timer and run to 0
        engine.setPaused(false)
        for (i in 0 until 59) {
            engine.tickTimer()
        }
        assertEquals(0, engine.state.timeRemainingSeconds)
        assertTrue(engine.state.isGameComplete)
    }

    @Test
    fun `add 15 seconds hint increases remaining time`() {
        val initialTime = engine.state.timeRemainingSeconds
        assertTrue(engine.add15SecondsHint())
        assertEquals(initialTime + 15, engine.state.timeRemainingSeconds)
        assertTrue(engine.state.hasUsedTimeHint)
        assertFalse(engine.add15SecondsHint()) // Cannot use twice
    }

    @Test
    fun `paused game ignores answer input and hints`() {
        val question = engine.state.currentQuestion!!
        engine.setPaused(true)

        assertFalse(engine.answerQuestion(question.correctAnswer))
        assertEquals(0, engine.state.questionsAnswered)
        assertFalse(engine.add15SecondsHint())
        assertEquals(60, engine.state.timeRemainingSeconds)
    }

    @Test
    fun `new best is recorded only when score exceeds previous best`() {
        val question = engine.state.currentQuestion!!
        engine.answerQuestion(question.correctAnswer)

        assertFalse(engine.state.isNewBest)
        assertEquals(50, engine.state.bestScore)
    }
}
