package com.tds.puzzivo.feature.quickmath

import kotlin.random.Random

data class MathQuestion(
    val expression: String,
    val correctAnswer: Int,
    val options: List<Int> // 4 options including correct answer
)

data class QuickMathState(
    val currentQuestion: MathQuestion? = null,
    val score: Int = 0,
    val bestScore: Int = 0,
    val timeRemainingSeconds: Int = 60,
    val questionsAnswered: Int = 0,
    val correctAnswersCount: Int = 0,
    val isGameStarted: Boolean = false,
    val isGameComplete: Boolean = false,
    val isPaused: Boolean = false,
    val hasUsedTimeHint: Boolean = false,
    val lastAnswerWasCorrect: Boolean? = null
)

class QuickMathGameEngine(
    private val random: Random = Random.Default
) {
    var state = QuickMathState()
        private set

    fun startNewGame(bestScore: Int = 0) {
        val firstQuestion = generateQuestion(questionIndex = 0)
        state = QuickMathState(
            currentQuestion = firstQuestion,
            score = 0,
            bestScore = bestScore,
            timeRemainingSeconds = 60,
            questionsAnswered = 0,
            correctAnswersCount = 0,
            isGameStarted = true,
            isGameComplete = false,
            isPaused = false,
            hasUsedTimeHint = false,
            lastAnswerWasCorrect = null
        )
    }

    fun answerQuestion(selectedAnswer: Int): Boolean {
        if (state.isGameComplete || state.isPaused) return false
        val question = state.currentQuestion ?: return false

        val isCorrect = (selectedAnswer == question.correctAnswer)
        val newScore = if (isCorrect) state.score + 10 else state.score
        val newBestScore = maxOf(newScore, state.bestScore)
        val newCorrectCount = if (isCorrect) state.correctAnswersCount + 1 else state.correctAnswersCount
        val newQuestionsAnswered = state.questionsAnswered + 1

        val nextQuestion = generateQuestion(questionIndex = newQuestionsAnswered)

        state = state.copy(
            currentQuestion = nextQuestion,
            score = newScore,
            bestScore = newBestScore,
            questionsAnswered = newQuestionsAnswered,
            correctAnswersCount = newCorrectCount,
            lastAnswerWasCorrect = isCorrect
        )

        return isCorrect
    }

    fun tickTimer() {
        if (!state.isGameStarted || state.isGameComplete || state.isPaused) return

        val remaining = state.timeRemainingSeconds - 1
        if (remaining <= 0) {
            state = state.copy(
                timeRemainingSeconds = 0,
                isGameComplete = true
            )
        } else {
            state = state.copy(timeRemainingSeconds = remaining)
        }
    }

    fun setPaused(paused: Boolean) {
        if (state.isGameStarted && !state.isGameComplete) {
            state = state.copy(isPaused = paused)
        }
    }

    fun add15SecondsHint(): Boolean {
        if (state.hasUsedTimeHint || state.isGameComplete) return false

        val newTime = state.timeRemainingSeconds + 15
        state = state.copy(
            timeRemainingSeconds = newTime,
            hasUsedTimeHint = true
        )
        return true
    }

    fun generateQuestion(questionIndex: Int): MathQuestion {
        // Increase difficulty range as questions progress
        val maxOperand = 10 + (questionIndex / 3) * 5

        val operators = listOf("+", "-", "*", "/")
        val operator = operators[random.nextInt(operators.size)]

        var operand1 = 0
        var operand2 = 0
        var correctAnswer = 0

        when (operator) {
            "+" -> {
                operand1 = random.nextInt(1, maxOperand)
                operand2 = random.nextInt(1, maxOperand)
                correctAnswer = operand1 + operand2
            }
            "-" -> {
                operand1 = random.nextInt(1, maxOperand)
                operand2 = random.nextInt(1, operand1 + 1) // Ensure positive result
                correctAnswer = operand1 - operand2
            }
            "*" -> {
                val maxMult = maxOf(5, maxOperand / 2)
                operand1 = random.nextInt(1, maxMult)
                operand2 = random.nextInt(1, maxMult)
                correctAnswer = operand1 * operand2
            }
            "/" -> {
                operand2 = random.nextInt(1, 12) // Avoid div by zero
                val factor = random.nextInt(1, 12)
                operand1 = operand2 * factor // Guarantees exact integer result!
                correctAnswer = factor
            }
        }

        // Generate 3 unique wrong distractors
        val distractors = mutableSetOf<Int>()
        var attempts = 0
        while (distractors.size < 3 && attempts < 100) {
            attempts++
            val offset = random.nextInt(-10, 11)
            val wrong = correctAnswer + offset
            if (wrong != correctAnswer && wrong >= 0) {
                distractors.add(wrong)
            }
        }
        var fallback = 1
        while (distractors.size < 3) {
            val wrong = if (correctAnswer - fallback >= 0 && (correctAnswer - fallback) !in distractors) {
                correctAnswer - fallback
            } else {
                correctAnswer + fallback
            }
            if (wrong != correctAnswer) {
                distractors.add(wrong)
            }
            fallback++
        }

        val allOptions = (distractors + correctAnswer).shuffled(random)

        return MathQuestion(
            expression = "$operand1 $operator $operand2",
            correctAnswer = correctAnswer,
            options = allOptions
        )
    }
}
