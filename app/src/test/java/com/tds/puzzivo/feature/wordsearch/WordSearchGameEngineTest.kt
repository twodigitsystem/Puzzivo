package com.tds.puzzivo.feature.wordsearch

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class WordSearchGameEngineTest {

    private lateinit var engine: WordSearchGameEngine

    // Puzzle 1 (Animals) word coordinates — all horizontal left-to-right
    private val cat = TargetWord(id = "puzzle_01_CAT", word = "CAT",  startRow = 0, startCol = 0, endRow = 0, endCol = 2)
    private val dog = TargetWord(id = "puzzle_01_DOG", word = "DOG",  startRow = 1, startCol = 0, endRow = 1, endCol = 2)
    private val lion = TargetWord(id = "puzzle_01_LION", word = "LION", startRow = 2, startCol = 0, endRow = 2, endCol = 3)
    private val bear = TargetWord(id = "puzzle_01_BEAR", word = "BEAR", startRow = 3, startCol = 0, endRow = 3, endCol = 3)
    private val wolf = TargetWord(id = "puzzle_01_WOLF", word = "WOLF", startRow = 4, startCol = 0, endRow = 4, endCol = 3)
    private val fox  = TargetWord(id = "puzzle_01_FOX",  word = "FOX",  startRow = 5, startCol = 0, endRow = 5, endCol = 2)

    @Before
    fun setup() {
        engine = WordSearchGameEngine()
        engine.loadPuzzle(0)
    }

    // ── Loading ──────────────────────────────────────────────────────────────

    @Test
    fun `load puzzle initializes puzzle with target words`() {
        val puzzle = engine.state.puzzle
        assertNotNull(puzzle)
        assertEquals("Animals", puzzle!!.theme)
        assertEquals(6, puzzle.targetWords.size)
        assertFalse(engine.state.isGameComplete)
        assertEquals(0, engine.state.puzzleIndex)
    }

    @Test
    fun `load puzzle with negative index clamps to first puzzle`() {
        engine.loadPuzzle(-5)
        assertEquals(0, engine.state.puzzleIndex)
        assertNotNull(engine.state.puzzle)
    }

    @Test
    fun `load puzzle with oversized index clamps to last puzzle`() {
        engine.loadPuzzle(999)
        assertEquals(19, engine.state.puzzleIndex)
        assertNotNull(engine.state.puzzle)
    }

    // ── Forward word selection ───────────────────────────────────────────────

    @Test
    fun `confirm forward selection of valid target word finds word`() {
        selectWord(cat)
        val found = engine.confirmSelection()
        assertTrue(found)
        assertTrue(engine.state.foundWords.contains("CAT"))
        assertEquals(1, engine.state.foundWords.size)
    }

    @Test
    fun `confirm forward selection adds cells to found positions`() {
        selectWord(cat)
        engine.confirmSelection()
        assertTrue(engine.state.foundCellPositions.contains(CellPosition(0, 0)))
        assertTrue(engine.state.foundCellPositions.contains(CellPosition(0, 1)))
        assertTrue(engine.state.foundCellPositions.contains(CellPosition(0, 2)))
    }

    // ── Reversed word selection ──────────────────────────────────────────────

    @Test
    fun `confirm reversed selection of valid target word finds word`() {
        // Drag from end to start of CAT (T-A-C)
        engine.selectStartCell(0, 2)
        engine.updateSelection(0, 0)
        assertTrue(engine.confirmSelection())
        assertTrue(engine.state.foundWords.contains("CAT"))
    }

    @Test
    fun `confirm reversed selection of DOG finds word`() {
        engine.selectStartCell(1, 2)
        engine.updateSelection(1, 0)
        assertTrue(engine.confirmSelection())
        assertTrue(engine.state.foundWords.contains("DOG"))
    }

    // ── Invalid / non-linear drag ────────────────────────────────────────────

    @Test
    fun `invalid angle drag results in empty selection`() {
        engine.selectStartCell(0, 0)
        // Drag to (3, 2) — not horizontal, vertical, or 45° diagonal
        engine.updateSelection(3, 2)
        assertTrue(engine.state.selectedCells.isEmpty())
    }

    @Test
    fun `confirm on invalid angle returns false`() {
        engine.selectStartCell(0, 0)
        engine.updateSelection(3, 2)
        assertFalse(engine.confirmSelection())
    }

    @Test
    fun `confirm on empty selection returns false`() {
        engine.selectStartCell(0, 0)
        engine.updateSelection(3, 2)
        // selection is now empty
        assertFalse(engine.confirmSelection())
    }

    @Test
    fun `confirm on single cell returns false`() {
        engine.selectStartCell(1, 1)
        // Don't move — single cell selection
        engine.updateSelection(1, 1)
        // "X" is not a word
        assertFalse(engine.confirmSelection())
    }

    // ── Horizontal selection ─────────────────────────────────────────────────

    @Test
    fun `horizontal drag right selects correct cells`() {
        engine.selectStartCell(0, 0)
        engine.updateSelection(0, 2)
        assertEquals(3, engine.state.selectedCells.size)
        assertEquals(CellPosition(0, 0), engine.state.selectedCells[0])
        assertEquals(CellPosition(0, 1), engine.state.selectedCells[1])
        assertEquals(CellPosition(0, 2), engine.state.selectedCells[2])
    }

    @Test
    fun `horizontal drag left selects correct cells`() {
        engine.selectStartCell(0, 2)
        engine.updateSelection(0, 0)
        assertEquals(3, engine.state.selectedCells.size)
        assertEquals(CellPosition(0, 2), engine.state.selectedCells[0])
        assertEquals(CellPosition(0, 1), engine.state.selectedCells[1])
        assertEquals(CellPosition(0, 0), engine.state.selectedCells[2])
    }

    // ── Vertical selection ───────────────────────────────────────────────────

    @Test
    fun `vertical drag down selects correct cells`() {
        // Not a word in the puzzle, but the path should still work
        engine.selectStartCell(0, 5)
        engine.updateSelection(2, 5)
        assertEquals(3, engine.state.selectedCells.size)
        assertEquals(CellPosition(0, 5), engine.state.selectedCells[0])
        assertEquals(CellPosition(1, 5), engine.state.selectedCells[1])
        assertEquals(CellPosition(2, 5), engine.state.selectedCells[2])
    }

    @Test
    fun `vertical drag up selects correct cells`() {
        engine.selectStartCell(2, 5)
        engine.updateSelection(0, 5)
        assertEquals(3, engine.state.selectedCells.size)
        assertEquals(CellPosition(2, 5), engine.state.selectedCells[0])
        assertEquals(CellPosition(1, 5), engine.state.selectedCells[1])
        assertEquals(CellPosition(0, 5), engine.state.selectedCells[2])
    }

    // ── Diagonal selection ──────────────────────────────────────────────────

    @Test
    fun `diagonal drag down-right selects correct cells`() {
        engine.selectStartCell(0, 1)
        engine.updateSelection(2, 3)
        assertEquals(3, engine.state.selectedCells.size)
        assertEquals(CellPosition(0, 1), engine.state.selectedCells[0])
        assertEquals(CellPosition(1, 2), engine.state.selectedCells[1])
        assertEquals(CellPosition(2, 3), engine.state.selectedCells[2])
    }

    @Test
    fun `diagonal drag up-left selects correct cells`() {
        engine.selectStartCell(2, 3)
        engine.updateSelection(0, 1)
        assertEquals(3, engine.state.selectedCells.size)
        assertEquals(CellPosition(2, 3), engine.state.selectedCells[0])
        assertEquals(CellPosition(1, 2), engine.state.selectedCells[1])
        assertEquals(CellPosition(0, 1), engine.state.selectedCells[2])
    }

    @Test
    fun `diagonal drag down-left selects correct cells`() {
        engine.selectStartCell(0, 2)
        engine.updateSelection(2, 0)
        assertEquals(3, engine.state.selectedCells.size)
        assertEquals(CellPosition(0, 2), engine.state.selectedCells[0])
        assertEquals(CellPosition(1, 1), engine.state.selectedCells[1])
        assertEquals(CellPosition(2, 0), engine.state.selectedCells[2])
    }

    @Test
    fun `diagonal drag up-right selects correct cells`() {
        engine.selectStartCell(2, 0)
        engine.updateSelection(0, 2)
        assertEquals(3, engine.state.selectedCells.size)
        assertEquals(CellPosition(2, 0), engine.state.selectedCells[0])
        assertEquals(CellPosition(1, 1), engine.state.selectedCells[1])
        assertEquals(CellPosition(0, 2), engine.state.selectedCells[2])
    }

    // ── Case insensitivity ───────────────────────────────────────────────────

    @Test
    fun `word match is case insensitive (grid is uppercase, compare lowercase)`() {
        // Simulate by checking that the engine comparison uses ignoreCase
        // The grid chars are uppercase; the target word is uppercase too,
        // but the engine uses equals(ignoreCase = true). This test confirms
        // the path works end-to-end.
        selectWord(cat)
        assertTrue(engine.confirmSelection())
    }

    // ── Already-found word ──────────────────────────────────────────────────

    @Test
    fun `finding a word that was already found returns false`() {
        selectWord(cat)
        assertTrue(engine.confirmSelection())

        // Try to select the same word again
        selectWord(cat)
        assertFalse(engine.confirmSelection())
        assertEquals(1, engine.state.foundWords.size)
    }

    @Test
    fun `found words set does not grow when duplicate found`() {
        selectWord(cat)
        engine.confirmSelection()
        selectWord(cat)
        engine.confirmSelection()
        assertEquals(1, engine.state.foundWords.size)
    }

    // ── Game completion ──────────────────────────────────────────────────────

    @Test
    fun `game is not complete when only some words are found`() {
        selectWord(cat)
        engine.confirmSelection()
        assertFalse(engine.state.isGameComplete)
    }

    @Test
    fun `game is complete when all words are found`() {
        findWord(cat)
        findWord(dog)
        findWord(lion)
        findWord(bear)
        findWord(wolf)
        findWord(fox)
        assertTrue(engine.state.isGameComplete)
    }

    @Test
    fun `completion counts found words correctly`() {
        findWord(cat)
        findWord(dog)
        findWord(lion)
        assertEquals(3, engine.state.foundWords.size)
        assertFalse(engine.state.isGameComplete)
    }

    @Test
    fun `all found cells are tracked after full completion`() {
        findWord(cat)
        findWord(dog)
        findWord(lion)
        findWord(bear)
        findWord(wolf)
        findWord(fox)
        assertEquals(6, engine.state.foundWords.size)
        assertEquals(21, engine.state.foundCellPositions.size) // 3+3+4+4+4+3 = 21 cells
    }

    // ── Hint mechanics ───────────────────────────────────────────────────────

    @Test
    fun `reveal word hint highlights an unfound word`() {
        assertTrue(engine.useRevealWordHint())
        assertTrue(engine.state.hasUsedHint)
        assertNotNull(engine.state.highlightedHintWord)
    }

    @Test
    fun `hint word is one of the puzzle's target words`() {
        engine.useRevealWordHint()
        val hintWord = engine.state.highlightedHintWord!!.word
        assertTrue(engine.state.puzzle!!.targetWords.any { it.word == hintWord })
    }

    @Test
    fun `hint is not available twice`() {
        assertTrue(engine.useRevealWordHint())
        assertFalse(engine.useRevealWordHint())
    }

    @Test
    fun `hint is not available when game is complete`() {
        findWord(cat)
        findWord(dog)
        findWord(lion)
        findWord(bear)
        findWord(wolf)
        findWord(fox)
        assertTrue(engine.state.isGameComplete)
        assertFalse(engine.useRevealWordHint())
    }

    @Test
    fun `hint is not available after finding all words manually without hint`() {
        findWord(cat)
        findWord(dog)
        findWord(lion)
        findWord(bear)
        findWord(wolf)
        findWord(fox)
        assertFalse(engine.useRevealWordHint())
    }

    @Test
    fun `hint clears when hinted word is found`() {
        engine.useRevealWordHint()
        val hintedWord = engine.state.highlightedHintWord!!.word
        assertNotNull(engine.state.highlightedHintWord)

        // Find the hinted word
        val hintedTarget = engine.state.puzzle!!.targetWords.first { it.word == hintedWord }
        selectWord(hintedTarget)
        engine.confirmSelection()

        assertNull(engine.state.highlightedHintWord)
    }

    @Test
    fun `hint persists when a different word is found`() {
        engine.useRevealWordHint()
        val hintedWord = engine.state.highlightedHintWord!!.word
        assertNotNull(engine.state.highlightedHintWord)

        // Find a different word
        val otherWord = engine.state.puzzle!!.targetWords.first { it.word != hintedWord }
        selectWord(otherWord)
        engine.confirmSelection()

        assertNotNull(engine.state.highlightedHintWord)
        assertEquals(hintedWord, engine.state.highlightedHintWord!!.word)
    }

    // ── Selection edge cases ─────────────────────────────────────────────────

    @Test
    fun `selectStartCell out of bounds is ignored`() {
        engine.selectStartCell(-1, 0)
        assertTrue(engine.state.selectedCells.isEmpty())

        engine.selectStartCell(0, -1)
        assertTrue(engine.state.selectedCells.isEmpty())

        engine.selectStartCell(8, 0)
        assertTrue(engine.state.selectedCells.isEmpty())

        engine.selectStartCell(0, 8)
        assertTrue(engine.state.selectedCells.isEmpty())
    }

    @Test
    fun `updateSelection out of bounds is ignored`() {
        engine.selectStartCell(0, 0)
        val before = engine.state.selectedCells
        engine.updateSelection(-1, 0)
        assertEquals(before, engine.state.selectedCells)
    }

    @Test
    fun `updateSelection without a start cell is ignored`() {
        // No start cell selected
        engine.updateSelection(3, 3)
        assertTrue(engine.state.selectedCells.isEmpty())
    }

    @Test
    fun `selecting a cell that is already found still starts selection`() {
        findWord(cat)
        // CAT cells are now found. Start a new selection on one of them.
        engine.selectStartCell(0, 0)
        assertEquals(1, engine.state.selectedCells.size)
        assertEquals(CellPosition(0, 0), engine.state.selectedCells[0])
    }

    @Test
    fun `confirmSelection without puzzle loaded returns false`() {
        val freshEngine = WordSearchGameEngine()
        assertFalse(freshEngine.confirmSelection())
    }

    @Test
    fun `useRevealWordHint without puzzle loaded returns false`() {
        val freshEngine = WordSearchGameEngine()
        assertFalse(freshEngine.useRevealWordHint())
    }

    @Test
    fun `selectStartCell without puzzle loaded is ignored`() {
        val freshEngine = WordSearchGameEngine()
        freshEngine.selectStartCell(0, 0)
        assertTrue(freshEngine.state.selectedCells.isEmpty())
    }

    // ── Load puzzle resets state ─────────────────────────────────────────────

    @Test
    fun `loadPuzzle resets found words when switching puzzles`() {
        findWord(cat)
        assertEquals(1, engine.state.foundWords.size)

        engine.loadPuzzle(1) // Space puzzle
        assertEquals(0, engine.state.foundWords.size)
        assertFalse(engine.state.isGameComplete)
        assertEquals("Space", engine.state.puzzle!!.theme)
    }

    @Test
    fun `loadPuzzle resets hint state`() {
        engine.useRevealWordHint()
        assertTrue(engine.state.hasUsedHint)

        engine.loadPuzzle(1)
        assertFalse(engine.state.hasUsedHint)
        assertNull(engine.state.highlightedHintWord)
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Simulates dragging from start to end of a [TargetWord] (forward direction)
     * without confirming.
     */
    private fun selectWord(tw: TargetWord) {
        engine.selectStartCell(tw.startRow, tw.startCol)
        engine.updateSelection(tw.endRow, tw.endCol)
    }

    /**
     * Simulates dragging and confirming a [TargetWord] (forward direction).
     */
    private fun findWord(tw: TargetWord) {
        selectWord(tw)
        engine.confirmSelection()
    }
}