package com.tds.puzzivo.feature.wordguess

import kotlin.random.Random

enum class LetterStatus {
    CORRECT,
    PRESENT,
    ABSENT,
    UNKNOWN
}

data class GuessRow(
    val word: String = "",
    val isSubmitted: Boolean = false,
    val statuses: List<LetterStatus> = List(5) { LetterStatus.UNKNOWN }
)

data class WordGuessState(
    val targetWord: String = "PLANT",
    val rows: List<GuessRow> = List(6) { GuessRow() },
    val currentRowIndex: Int = 0,
    val isGameComplete: Boolean = false,
    val isWon: Boolean = false,
    val keyboardLetterStatuses: Map<Char, LetterStatus> = emptyMap(),
    val attemptsUsed: Int = 0
)

class WordGuessGameEngine(
    private val wordList: List<String> = defaultWordList,
    private val random: Random = Random.Default
) {
    var state = WordGuessState()
        private set

    fun startNewGame(targetWord: String? = null) {
        val word = (targetWord ?: wordList[random.nextInt(wordList.size)]).uppercase()
        state = WordGuessState(
            targetWord = word,
            rows = List(6) { GuessRow() },
            currentRowIndex = 0,
            isGameComplete = false,
            isWon = false,
            keyboardLetterStatuses = emptyMap(),
            attemptsUsed = 0
        )
    }

    fun typeLetter(letter: Char) {
        if (state.isGameComplete) return
        val currentIdx = state.currentRowIndex
        if (currentIdx !in 0..5) return

        val currentRow = state.rows[currentIdx]
        if (currentRow.word.length < 5) {
            val updatedWord = currentRow.word + letter.uppercaseChar()
            val updatedRows = state.rows.toMutableList()
            updatedRows[currentIdx] = currentRow.copy(word = updatedWord)
            state = state.copy(rows = updatedRows)
        }
    }

    fun deleteLetter() {
        if (state.isGameComplete) return
        val currentIdx = state.currentRowIndex
        if (currentIdx !in 0..5) return

        val currentRow = state.rows[currentIdx]
        if (currentRow.word.isNotEmpty()) {
            val updatedWord = currentRow.word.dropLast(1)
            val updatedRows = state.rows.toMutableList()
            updatedRows[currentIdx] = currentRow.copy(word = updatedWord)
            state = state.copy(rows = updatedRows)
        }
    }

    fun submitGuess(): Boolean {
        if (state.isGameComplete) return false
        val currentIdx = state.currentRowIndex
        if (currentIdx !in 0..5) return false

        val currentRow = state.rows[currentIdx]
        if (currentRow.word.length != 5) return false

        val guess = currentRow.word
        val target = state.targetWord
        val statuses = MutableList(5) { LetterStatus.ABSENT }

        val targetCharCounts = target.groupingBy { it }.eachCount().toMutableMap()

        // 1st pass: exact matches
        for (i in 0 until 5) {
            if (guess[i] == target[i]) {
                statuses[i] = LetterStatus.CORRECT
                targetCharCounts[guess[i]] = (targetCharCounts[guess[i]] ?: 1) - 1
            }
        }

        // 2nd pass: present matches
        for (i in 0 until 5) {
            if (statuses[i] != LetterStatus.CORRECT) {
                val char = guess[i]
                if ((targetCharCounts[char] ?: 0) > 0) {
                    statuses[i] = LetterStatus.PRESENT
                    targetCharCounts[char] = (targetCharCounts[char] ?: 1) - 1
                } else {
                    statuses[i] = LetterStatus.ABSENT
                }
            }
        }

        val updatedRows = state.rows.toMutableList()
        updatedRows[currentIdx] = GuessRow(
            word = guess,
            isSubmitted = true,
            statuses = statuses
        )

        val newKeyboardStatuses = state.keyboardLetterStatuses.toMutableMap()
        for (i in 0 until 5) {
            val char = guess[i]
            val newStatus = statuses[i]
            val existing = newKeyboardStatuses[char]
            if (existing == null || newStatus == LetterStatus.CORRECT || (newStatus == LetterStatus.PRESENT && existing != LetterStatus.CORRECT)) {
                newKeyboardStatuses[char] = newStatus
            }
        }

        val isWon = (guess == target)
        val isComplete = isWon || (currentIdx == 5)

        state = state.copy(
            rows = updatedRows,
            currentRowIndex = if (isComplete) currentIdx else currentIdx + 1,
            isGameComplete = isComplete,
            isWon = isWon,
            keyboardLetterStatuses = newKeyboardStatuses,
            attemptsUsed = currentIdx + 1
        )

        return true
    }

    companion object {
        val defaultWordList = listOf(
            "PLANT", "BRAIN", "SMART", "FLASH", "LIGHT",
            "POWER", "SHINE", "CROWN", "DREAM", "GLOBE",
            "HEART", "MAGIC", "OCEAN", "PEACE", "SOLAR",
            "TIGER", "WATER", "YOUTH", "ZEBRA", "SPARK",
            "BLAZE", "CLOUD", "FLAME", "GRAND", "HONOR"
        )
    }
}
