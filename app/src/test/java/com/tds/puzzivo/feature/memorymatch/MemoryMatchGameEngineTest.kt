package com.tds.puzzivo.feature.memorymatch

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

class MemoryMatchGameEngineTest {

    private lateinit var engine: MemoryMatchGameEngine

    @Before
    fun setup() {
        engine = MemoryMatchGameEngine(Random(12345))
        engine.startNewGame(bestMoves = 20, bestTimeSeconds = 45)
    }

    @Test
    fun `deck initializes with 16 cards in 8 matching pairs`() {
        val cards = engine.state.cards
        assertEquals(16, cards.size)
        val pairCounts = cards.groupBy { it.pairId }
        assertEquals(8, pairCounts.size)
        pairCounts.values.forEach { pairList ->
            assertEquals(2, pairList.size)
        }
    }

    @Test
    fun `flipping two matching cards marks them matched`() {
        val cards = engine.state.cards
        val firstPairId = cards.first().pairId
        val matchingIndices = cards.indices.filter { cards[it].pairId == firstPairId }

        engine.flipCard(matchingIndices[0])
        val mismatch = engine.flipCard(matchingIndices[1])

        assertNull(mismatch)
        assertTrue(engine.state.cards[matchingIndices[0]].isMatched)
        assertTrue(engine.state.cards[matchingIndices[1]].isMatched)
        assertEquals(1, engine.state.moves)
    }

    @Test
    fun `flipping mismatch cards locks input until cleared`() {
        val cards = engine.state.cards
        val firstCard = cards[0]
        val mismatchIndex = cards.indices.first { cards[it].pairId != firstCard.pairId }

        engine.flipCard(0)
        val mismatch = engine.flipCard(mismatchIndex)

        assertNotNull(mismatch)
        assertTrue(engine.state.isInputLocked)

        // Clear mismatch
        engine.clearMismatch(mismatch!!.firstIndex, mismatch.secondIndex)
        assertFalse(engine.state.isInputLocked)
        assertFalse(engine.state.cards[0].isFaceUp)
        assertFalse(engine.state.cards[mismatchIndex].isFaceUp)
    }

    @Test
    fun `reveal pair hint reveals one unmatched pair and clears flipped indices`() {
        // Use hint
        assertTrue(engine.useRevealPairHint())
        assertTrue(engine.state.hasUsedHint)

        // A temporary hint is locked until the ViewModel clears it.
        assertTrue(engine.state.flippedCardIndices.isEmpty())
        assertTrue(engine.state.isInputLocked)
    }

    @Test
    fun `reveal pair hint temporarily shows cards without completing them`() {
        assertTrue(engine.useRevealPairHint())
        assertEquals(2, engine.state.hintedCardIndices.size)
        assertTrue(engine.state.isInputLocked)
        assertTrue(engine.state.hintedCardIndices.all { engine.state.cards[it].isFaceUp })
        assertTrue(engine.state.hintedCardIndices.all { !engine.state.cards[it].isMatched })

        engine.clearHint()
        assertFalse(engine.state.isInputLocked)
        assertTrue(engine.state.hintedCardIndices.isEmpty())
        assertTrue(engine.state.cards.all { !it.isMatched })
    }

    @Test
    fun `hint cannot be used while a card is already flipped`() {
        engine.flipCard(0)

        assertFalse(engine.useRevealPairHint())
    }
}
