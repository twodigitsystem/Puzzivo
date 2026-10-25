package com.tds.puzzivo.feature.memorymatch

import kotlin.random.Random

data class MemoryCard(
    val id: Int,
    val pairId: Int,
    val icon: String,
    val isFaceUp: Boolean = false,
    val isMatched: Boolean = false
)

data class MemoryMatchState(
    val cards: List<MemoryCard> = emptyList(),
    val moves: Int = 0,
    val bestMoves: Int = 0,
    val bestTimeSeconds: Int = 0,
    val elapsedSeconds: Int = 0,
    val isGameStarted: Boolean = false,
    val isGameComplete: Boolean = false,
    val isInputLocked: Boolean = false,
    val flippedCardIndices: List<Int> = emptyList(),
    val hasUsedHint: Boolean = false,
    val hintedCardIndices: List<Int> = emptyList(),
    val isNewBest: Boolean = false
)

class MemoryMatchGameEngine(
    private val random: Random = Random.Default
) {
    var state = MemoryMatchState()
        private set

    private val pairIcons = listOf("A", "B", "C", "D", "E", "F", "G", "H")

    fun startNewGame(bestMoves: Int = 0, bestTimeSeconds: Int = 0) {
        val deck = mutableListOf<MemoryCard>()
        var cardId = 0

        for (pairId in 0 until 8) {
            val icon = pairIcons[pairId]
            deck.add(MemoryCard(id = cardId++, pairId = pairId, icon = icon))
            deck.add(MemoryCard(id = cardId++, pairId = pairId, icon = icon))
        }

        deck.shuffle(random)

        state = MemoryMatchState(
            cards = deck.mapIndexed { index, card -> card.copy(id = index) },
            moves = 0,
            bestMoves = bestMoves,
            bestTimeSeconds = bestTimeSeconds,
            elapsedSeconds = 0,
            isGameStarted = false,
            isGameComplete = false,
            isInputLocked = false,
            flippedCardIndices = emptyList(),
            hasUsedHint = false,
            hintedCardIndices = emptyList(),
            isNewBest = false
        )
    }

    fun flipCard(index: Int): MismatchEvent? {
        if (state.isInputLocked || state.isGameComplete) return null
        if (index !in state.cards.indices) return null

        val card = state.cards[index]
        if (card.isFaceUp || card.isMatched) return null

        val updatedCards = state.cards.toMutableList()
        updatedCards[index] = card.copy(isFaceUp = true)

        val currentFlipped = state.flippedCardIndices + index

        if (currentFlipped.size == 1) {
            state = state.copy(
                cards = updatedCards,
                isGameStarted = true,
                flippedCardIndices = currentFlipped
            )
            return null
        } else if (currentFlipped.size == 2) {
            val firstIdx = currentFlipped[0]
            val secondIdx = currentFlipped[1]

            val firstCard = updatedCards[firstIdx]
            val secondCard = updatedCards[secondIdx]

            val newMoves = state.moves + 1

            if (firstCard.pairId == secondCard.pairId) {
                // Match found!
                updatedCards[firstIdx] = firstCard.copy(isMatched = true)
                updatedCards[secondIdx] = secondCard.copy(isMatched = true)

                val allMatched = updatedCards.all { it.isMatched }
                val isNewBest = allMatched && (
                    state.bestMoves == 0 ||
                        newMoves < state.bestMoves ||
                        (newMoves == state.bestMoves &&
                            (state.bestTimeSeconds == 0 || state.elapsedSeconds < state.bestTimeSeconds))
                    )

                state = state.copy(
                    cards = updatedCards,
                    moves = newMoves,
                    isGameComplete = allMatched,
                    flippedCardIndices = emptyList(),
                    bestMoves = if (isNewBest) newMoves else state.bestMoves,
                    bestTimeSeconds = if (isNewBest) state.elapsedSeconds else state.bestTimeSeconds,
                    isNewBest = state.isNewBest || isNewBest
                )
                return null
            } else {
                // Mismatch
                state = state.copy(
                    cards = updatedCards,
                    moves = newMoves,
                    isInputLocked = true,
                    flippedCardIndices = currentFlipped
                )
                return MismatchEvent(firstIdx, secondIdx)
            }
        }

        return null
    }

    fun clearMismatch(firstIndex: Int, secondIndex: Int) {
        if (!state.isInputLocked || state.flippedCardIndices != listOf(firstIndex, secondIndex)) return

        val updatedCards = state.cards.toMutableList()
        if (firstIndex in updatedCards.indices && !updatedCards[firstIndex].isMatched) {
            updatedCards[firstIndex] = updatedCards[firstIndex].copy(isFaceUp = false)
        }
        if (secondIndex in updatedCards.indices && !updatedCards[secondIndex].isMatched) {
            updatedCards[secondIndex] = updatedCards[secondIndex].copy(isFaceUp = false)
        }

        state = state.copy(
            cards = updatedCards,
            isInputLocked = false,
            flippedCardIndices = emptyList()
        )
    }

    fun clearHint() {
        if (state.hintedCardIndices.isEmpty()) return

        val updatedCards = state.cards.toMutableList()
        state.hintedCardIndices.forEach { index ->
            if (index in updatedCards.indices && !updatedCards[index].isMatched) {
                updatedCards[index] = updatedCards[index].copy(isFaceUp = false)
            }
        }

        state = state.copy(
            cards = updatedCards,
            isInputLocked = false,
            hintedCardIndices = emptyList()
        )
    }

    fun useRevealPairHint(): Boolean {
        if (state.hasUsedHint || state.isGameComplete || state.isInputLocked || state.flippedCardIndices.isNotEmpty()) return false

        // Find an unmatched pair
        val unmatchedCards = state.cards.filter { !it.isMatched }
        if (unmatchedCards.isEmpty()) return false

        val targetPairId = unmatchedCards.first().pairId
        val targetIndices = state.cards.indices.filter { state.cards[it].pairId == targetPairId }

        val updatedCards = state.cards.toMutableList()
        for (idx in targetIndices) {
            updatedCards[idx] = updatedCards[idx].copy(isFaceUp = true)
        }

        state = state.copy(
            cards = updatedCards,
            hasUsedHint = true,
            isInputLocked = true,
            flippedCardIndices = emptyList(),
            hintedCardIndices = targetIndices
        )

        return true
    }

    fun updateTimer(seconds: Int) {
        if (state.isGameStarted && !state.isGameComplete) {
            state = state.copy(elapsedSeconds = seconds)
        }
    }
}

data class MismatchEvent(val firstIndex: Int, val secondIndex: Int)
