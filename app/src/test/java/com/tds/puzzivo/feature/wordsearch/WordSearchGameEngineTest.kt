package com.tds.puzzivo.feature.wordsearch

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.math.abs

class WordSearchGameEngineTest {

    private lateinit var engine: WordSearchGameEngine

    @Before
    fun setup() {
        engine = WordSearchGameEngine()
        engine.loadPuzzle(0)
    }

    @Test
    fun `all bundled puzzles are valid and deliberately difficult`() {
        assertTrue(WordSearchRepository.validateAll().isEmpty())

        WordSearchRepository.puzzles.forEach { puzzle ->
            assertEquals(10, puzzle.rows)
            assertEquals(10, puzzle.cols)

            val directions = puzzle.targetWords.map { target ->
                (target.endRow - target.startRow).sign() to
                    (target.endCol - target.startCol).sign()
            }.toSet()

            assertTrue("${puzzle.id} should use multiple directions", directions.size >= 4)
            assertTrue("${puzzle.id} should include a diagonal word", directions.any { it.first != 0 && it.second != 0 })
            assertTrue("${puzzle.id} should include a vertical word", directions.any { it.first != 0 && it.second == 0 })
            assertTrue("${puzzle.id} should include a reversed-direction word", directions.any { it.first < 0 || it.second < 0 })
        }
    }

    @Test
    fun `load puzzle initializes a ten by ten challenge board`() {
        val puzzle = engine.state.puzzle

        assertNotNull(puzzle)
        assertEquals("Animals", puzzle!!.theme)
        assertEquals("Challenge", puzzle.difficulty)
        assertEquals(10, puzzle.grid.size)
        assertTrue(puzzle.grid.all { it.size == 10 })
        assertEquals(6, puzzle.targetWords.size)
        assertFalse(engine.state.isGameComplete)
    }

    @Test
    fun `target can be found in its forward direction`() {
        val target = firstTarget()

        selectTarget(target)

        assertTrue(engine.confirmSelection())
        assertTrue(engine.state.foundWords.contains(target.word))
        assertEquals(target.word.length, engine.state.foundCellPositions.size)
    }

    @Test
    fun `target can be found in reverse direction`() {
        val target = firstTarget()
        val path = pathFor(target).asReversed()

        engine.selectStartCell(path.first().row, path.first().col)
        engine.updateSelection(path.last().row, path.last().col)

        assertTrue(engine.confirmSelection())
        assertTrue(engine.state.foundWords.contains(target.word))
    }

    @Test
    fun `selection must match the target path and cannot use a different line`() {
        val target = firstTarget()
        val path = pathFor(target)
        val wrongEnd = CellPosition(
            row = if (path.first().row < 9) path.first().row + 1 else path.first().row - 1,
            col = path.first().col
        )

        engine.selectStartCell(path.first().row, path.first().col)
        engine.updateSelection(wrongEnd.row, wrongEnd.col)

        assertFalse(engine.confirmSelection())
        assertTrue(engine.state.foundWords.isEmpty())
    }

    @Test
    fun `invalid angle selection is rejected`() {
        engine.selectStartCell(0, 0)
        engine.updateSelection(3, 2)

        assertTrue(engine.state.selectedCells.isEmpty())
        assertFalse(engine.confirmSelection())
    }

    @Test
    fun `a found word cannot be counted twice`() {
        val target = firstTarget()
        selectTarget(target)
        assertTrue(engine.confirmSelection())

        selectTarget(target)
        assertFalse(engine.confirmSelection())
        assertEquals(1, engine.state.foundWords.size)
    }

    @Test
    fun `all target words complete the puzzle exactly once`() {
        val targets = engine.state.puzzle!!.targetWords
        targets.forEach { target ->
            selectTarget(target)
            assertTrue("${target.word} should be found", engine.confirmSelection())
        }

        assertTrue(engine.state.isGameComplete)
        assertEquals(targets.size, engine.state.foundWords.size)
        assertFalse(engine.confirmSelection())
    }

    @Test
    fun `hint highlights one unfound word and cannot be reused`() {
        assertTrue(engine.useRevealWordHint())
        assertTrue(engine.state.hasUsedHint)
        assertNotNull(engine.state.highlightedHintWord)
        assertFalse(engine.useRevealWordHint())
    }

    @Test
    fun `finding the hinted word clears the hint`() {
        assertTrue(engine.useRevealWordHint())
        val hinted = engine.state.highlightedHintWord!!

        selectTarget(hinted)
        assertTrue(engine.confirmSelection())
        assertNull(engine.state.highlightedHintWord)
    }

    @Test
    fun `load puzzle clamps indexes and resets state`() {
        val target = firstTarget()
        selectTarget(target)
        assertTrue("target=$target selected=${engine.state.selectedCells}", engine.confirmSelection())
        assertEquals(1, engine.state.foundWords.size)

        engine.loadPuzzle(-5)
        assertEquals(0, engine.state.puzzleIndex)
        assertTrue(engine.state.foundWords.isEmpty())

        engine.loadPuzzle(999)
        assertEquals(WordSearchRepository.puzzles.lastIndex, engine.state.puzzleIndex)
        assertTrue(engine.state.foundWords.isEmpty())
    }

    @Test
    fun `selection and hints are ignored after completion`() {
        engine.state.puzzle!!.targetWords.forEach { target ->
            selectTarget(target)
            engine.confirmSelection()
        }
        assertTrue(engine.state.isGameComplete)

        engine.selectStartCell(0, 0)
        engine.updateSelection(0, 1)
        assertTrue(engine.state.selectedCells.isEmpty())
        assertFalse(engine.useRevealWordHint())
    }

    private fun firstTarget(): TargetWord = engine.state.puzzle!!.targetWords.first()

    private fun selectTarget(target: TargetWord) {
        val path = pathFor(target)
        engine.selectStartCell(path.first().row, path.first().col)
        engine.updateSelection(path.last().row, path.last().col)
    }

    private fun pathFor(target: TargetWord): List<CellPosition> {
        val rowDelta = target.endRow - target.startRow
        val colDelta = target.endCol - target.startCol
        val steps = maxOf(abs(rowDelta), abs(colDelta))
        val rowStep = rowDelta.sign()
        val colStep = colDelta.sign()

        return List(steps + 1) { offset ->
            CellPosition(
                row = target.startRow + rowStep * offset,
                col = target.startCol + colStep * offset
            )
        }
    }

    private fun Int.sign(): Int = when {
        this > 0 -> 1
        this < 0 -> -1
        else -> 0
    }
}
