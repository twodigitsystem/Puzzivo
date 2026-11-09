package com.tds.puzzivo.feature.wordscramble

import com.tds.puzzivo.core.model.DifficultyLevel

data class ScrambleLetter(
    val id: Int,
    val char: Char
)

data class WordScrambleRound(
    val word: ScrambleWord,
    val letters: List<ScrambleLetter>,
    val revealedIndices: Set<Int> = emptySet()
) {
    companion object {
        fun create(scrambleWord: ScrambleWord): WordScrambleRound {
            val originalChars = scrambleWord.word.toList()
            val indexedLetters = originalChars.mapIndexed { idx, char -> ScrambleLetter(idx, char) }
            var shuffled = indexedLetters.shuffled()
            var attempts = 0
            while (shuffled.map { it.char } == originalChars && originalChars.distinct().size > 1 && attempts < 20) {
                shuffled = indexedLetters.shuffled()
                attempts++
            }
            return WordScrambleRound(scrambleWord, shuffled)
        }
    }
}

enum class WordSubmitResult {
    CORRECT,
    WRONG,
    GAME_COMPLETE
}

data class WordScrambleState(
    val difficulty: DifficultyLevel = DifficultyLevel.MEDIUM,
    val roundIndex: Int = 0,
    val totalRounds: Int = 5,
    val rounds: List<WordScrambleRound> = emptyList(),
    val currentRound: WordScrambleRound? = null,
    val selectedLetterIds: List<Int> = emptyList(),
    val score: Int = 0,
    val bestScore: Int = 0,
    val comboStreak: Int = 0,
    val totalMoves: Int = 0,
    val hintsUsed: Int = 0,
    val timeElapsedSeconds: Int = 0,
    val roundStartTimeSeconds: Int = 0,
    val feedbackMessage: String? = null,
    val solvedDefinition: String? = null,
    val lastPointsEarned: Int = 0,
    val isErrorState: Boolean = false,
    val isGameComplete: Boolean = false,
    val isPaused: Boolean = false
)

class WordScrambleGameEngine {

    var state = WordScrambleState()
        private set

    fun startNewGame(
        difficulty: DifficultyLevel = DifficultyLevel.MEDIUM,
        bestScore: Int = 0,
        roundCount: Int = 5
    ) {
        val wordPool = WordScrambleRepository.getWordsForDifficulty(difficulty)
        val selectedWords = wordPool.take(roundCount.coerceAtMost(wordPool.size))
        val gameRounds = selectedWords.map { WordScrambleRound.create(it) }

        state = WordScrambleState(
            difficulty = difficulty,
            roundIndex = 0,
            totalRounds = gameRounds.size,
            rounds = gameRounds,
            currentRound = gameRounds.firstOrNull(),
            selectedLetterIds = emptyList(),
            score = 0,
            bestScore = bestScore,
            comboStreak = 0,
            totalMoves = 0,
            hintsUsed = 0,
            timeElapsedSeconds = 0,
            roundStartTimeSeconds = 0,
            feedbackMessage = null,
            solvedDefinition = null,
            lastPointsEarned = 0,
            isErrorState = false,
            isGameComplete = false,
            isPaused = false
        )
    }

    fun restoreState(restoredState: WordScrambleState) {
        state = restoredState
    }

    fun selectLetter(letterId: Int) {
        val round = state.currentRound ?: return
        if (state.isGameComplete) return

        val currentlySelected = state.selectedLetterIds
        val isAlreadySelected = letterId in currentlySelected

        val updatedSelection = if (isAlreadySelected) {
            currentlySelected - letterId
        } else {
            if (currentlySelected.size < round.word.word.length) {
                currentlySelected + letterId
            } else {
                currentlySelected
            }
        }

        state = state.copy(
            selectedLetterIds = updatedSelection,
            isErrorState = false,
            feedbackMessage = null
        )
    }

    fun deselectAtAnswerIndex(index: Int) {
        if (state.isGameComplete) return
        val currentlySelected = state.selectedLetterIds
        if (index in currentlySelected.indices) {
            val updated = currentlySelected.toMutableList().apply { removeAt(index) }
            state = state.copy(
                selectedLetterIds = updated,
                isErrorState = false,
                feedbackMessage = null
            )
        }
    }

    fun backspace() {
        if (state.isGameComplete) return
        val currentlySelected = state.selectedLetterIds
        if (currentlySelected.isNotEmpty()) {
            state = state.copy(
                selectedLetterIds = currentlySelected.dropLast(1),
                isErrorState = false,
                feedbackMessage = null
            )
        }
    }

    fun clearSelection() {
        if (state.isGameComplete) return
        state = state.copy(
            selectedLetterIds = emptyList(),
            isErrorState = false,
            feedbackMessage = null
        )
    }

    fun shuffleUnselectedLetters() {
        val round = state.currentRound ?: return
        if (state.isGameComplete) return

        val unselected = round.letters.filter { it.id !in state.selectedLetterIds }.shuffled()
        var unselectedIdx = 0
        val newLetters = round.letters.map { letter ->
            if (letter.id in state.selectedLetterIds) {
                letter
            } else {
                unselected[unselectedIdx++]
            }
        }

        val updatedRound = round.copy(letters = newLetters)
        val updatedRounds = state.rounds.toMutableList().apply {
            set(state.roundIndex, updatedRound)
        }

        state = state.copy(
            currentRound = updatedRound,
            rounds = updatedRounds
        )
    }

    fun submitAnswer(): WordSubmitResult {
        val round = state.currentRound ?: return WordSubmitResult.WRONG
        if (state.isGameComplete) return WordSubmitResult.WRONG

        val letterMap = round.letters.associateBy { it.id }
        val guessedWord = state.selectedLetterIds.mapNotNull { letterMap[it]?.char }.joinToString("")
        val targetWord = round.word.word

        state = state.copy(totalMoves = state.totalMoves + 1)

        return if (guessedWord.equals(targetWord, ignoreCase = true)) {
            val roundTime = state.timeElapsedSeconds - state.roundStartTimeSeconds
            val speedBonus = if (roundTime <= 10) 50 else 0

            val newCombo = state.comboStreak + 1
            val comboMultiplier = 1.0f + minOf(newCombo * 0.2f, 1.0f)
            val basePoints = targetWord.length * 100
            val difficultyMultiplier = state.difficulty.scoreMultiplier
            val hintPenalty = state.hintsUsed * 20

            val roundScore = ((basePoints * difficultyMultiplier * comboMultiplier).toInt() + speedBonus - hintPenalty).coerceAtLeast(50)
            val newScore = state.score + roundScore
            val newBest = maxOf(newScore, state.bestScore)
            val definitionText = round.word.hint

            val nextRoundIndex = state.roundIndex + 1
            if (nextRoundIndex >= state.rounds.size) {
                state = state.copy(
                    score = newScore,
                    bestScore = newBest,
                    comboStreak = newCombo,
                    lastPointsEarned = roundScore,
                    solvedDefinition = definitionText,
                    isGameComplete = true,
                    feedbackMessage = "Correct!",
                    isErrorState = false
                )
                WordSubmitResult.GAME_COMPLETE
            } else {
                val nextRound = state.rounds[nextRoundIndex]
                state = state.copy(
                    roundIndex = nextRoundIndex,
                    currentRound = nextRound,
                    selectedLetterIds = emptyList(),
                    score = newScore,
                    bestScore = newBest,
                    comboStreak = newCombo,
                    lastPointsEarned = roundScore,
                    solvedDefinition = definitionText,
                    roundStartTimeSeconds = state.timeElapsedSeconds,
                    feedbackMessage = "Correct!",
                    isErrorState = false
                )
                WordSubmitResult.CORRECT
            }
        } else {
            state = state.copy(
                comboStreak = 0,
                isErrorState = true,
                feedbackMessage = "Not quite"
            )
            WordSubmitResult.WRONG
        }
    }

    fun useHint(): Boolean {
        val round = state.currentRound ?: return false
        if (state.isGameComplete) return false

        val targetWord = round.word.word
        val currentLetters = round.letters
        val selectedIds = state.selectedLetterIds.toMutableList()

        val letterMap = currentLetters.associateBy { it.id }
        var targetIndexToFix = -1
        for (i in 0 until targetWord.length) {
            val selectedIdAtI = selectedIds.getOrNull(i)
            val charAtI = selectedIdAtI?.let { letterMap[it]?.char }
            if (charAtI != targetWord[i]) {
                targetIndexToFix = i
                break
            }
        }

        if (targetIndexToFix == -1) return false

        val neededChar = targetWord[targetIndexToFix]
        val availableLetter = currentLetters.firstOrNull { l ->
            l.char == neededChar && l.id !in selectedIds
        } ?: return false

        val updatedSelectedIds = selectedIds.take(targetIndexToFix).toMutableList()
        updatedSelectedIds.add(availableLetter.id)

        val updatedRevealed = round.revealedIndices + targetIndexToFix
        val updatedRound = round.copy(revealedIndices = updatedRevealed)

        val updatedRounds = state.rounds.toMutableList().apply {
            set(state.roundIndex, updatedRound)
        }

        state = state.copy(
            rounds = updatedRounds,
            currentRound = updatedRound,
            selectedLetterIds = updatedSelectedIds,
            hintsUsed = state.hintsUsed + 1,
            isErrorState = false,
            feedbackMessage = null
        )
        return true
    }

    fun tickTimer() {
        if (!state.isGameComplete && !state.isPaused) {
            state = state.copy(timeElapsedSeconds = state.timeElapsedSeconds + 1)
        }
    }

    fun togglePause() {
        if (!state.isGameComplete) {
            state = state.copy(isPaused = !state.isPaused)
        }
    }
}
