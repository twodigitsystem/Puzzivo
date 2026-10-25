package com.tds.puzzivo.feature.wordsearch

import kotlin.math.abs
import kotlin.random.Random

/**
 * Bundled Word Search content.
 *
 * Boards are generated deterministically from the definitions below so every
 * installation receives the same puzzles, while words are distributed across
 * the full 10x10 board using mixed directions, reversals, overlaps, and
 * randomized filler letters instead of obvious one-word-per-row layouts.
 */
object WordSearchRepository {

    private const val GRID_SIZE = 10
    private const val MAX_GENERATION_ATTEMPTS = 250
    private const val EMPTY_CELL = '\u0000'

    private val directions = listOf(
        Direction(-1, -1), Direction(-1, 0), Direction(-1, 1),
        Direction(0, -1),                      Direction(0, 1),
        Direction(1, -1),  Direction(1, 0),  Direction(1, 1)
    )

    private val definitions = listOf(
        PuzzleDefinition("puzzle_01", "Puzzle 1: Animals", "Animals", "Challenge", listOf("ELEPHANT", "TIGER", "MONKEY", "RABBIT", "ZEBRA", "HORSE")),
        PuzzleDefinition("puzzle_02", "Puzzle 2: Space", "Space", "Challenge", listOf("PLANET", "COMET", "ORBIT", "GALAXY", "METEOR", "NEBULA")),
        PuzzleDefinition("puzzle_03", "Puzzle 3: Fruits", "Fruits", "Challenge", listOf("APPLE", "BANANA", "CHERRY", "ORANGE", "PAPAYA", "MELON")),
        PuzzleDefinition("puzzle_04", "Puzzle 4: Colors", "Colors", "Challenge", listOf("PURPLE", "ORANGE", "YELLOW", "INDIGO", "VIOLET", "SILVER")),
        PuzzleDefinition("puzzle_05", "Puzzle 5: Tech", "Tech", "Challenge", listOf("CODING", "BINARY", "SERVER", "KERNEL", "OBJECT", "PIXEL")),
        PuzzleDefinition("puzzle_06", "Puzzle 6: Weather", "Weather", "Challenge", listOf("RAINBOW", "THUNDER", "BREEZE", "WINTER", "CLOUDS", "STORM")),
        PuzzleDefinition("puzzle_07", "Puzzle 7: Music", "Music", "Challenge", listOf("MELODY", "GUITAR", "RHYTHM", "PIANO", "SINGER", "HARMONY")),
        PuzzleDefinition("puzzle_08", "Puzzle 8: Sports", "Sports", "Challenge", listOf("SOCCER", "TENNIS", "HOCKEY", "RUNNING", "ATHLETE", "STADIUM")),
        PuzzleDefinition("puzzle_09", "Puzzle 9: Nature", "Nature", "Challenge", listOf("FOREST", "GARDEN", "FLOWER", "MOUNTAIN", "RIVER", "VALLEY")),
        PuzzleDefinition("puzzle_10", "Puzzle 10: Ocean", "Ocean", "Challenge", listOf("DOLPHIN", "WHALE", "CORAL", "ISLAND", "SEASHELL", "TURTLE")),
        PuzzleDefinition("puzzle_11", "Puzzle 11: Food", "Food", "Expert", listOf("NOODLE", "PIZZA", "COOKIE", "CHEESE", "TOMATO", "BREAD")),
        PuzzleDefinition("puzzle_12", "Puzzle 12: Time", "Time", "Expert", listOf("MINUTE", "SECOND", "WEEK", "SEASON", "FUTURE", "MIDNIGHT")),
        PuzzleDefinition("puzzle_13", "Puzzle 13: Gems", "Gems", "Expert", listOf("RUBY", "JADE", "DIAMOND", "SAPPHIRE", "OPAL", "CRYSTAL")),
        PuzzleDefinition("puzzle_14", "Puzzle 14: City", "City", "Expert", listOf("STREET", "BRIDGE", "TUNNEL", "MARKET", "OFFICE", "STATION")),
        PuzzleDefinition("puzzle_15", "Puzzle 15: Plants", "Plants", "Expert", listOf("FLOWER", "LEAF", "ROOTS", "BRANCH", "PETALS", "CACTUS")),
        PuzzleDefinition("puzzle_16", "Puzzle 16: Math", "Math", "Expert", listOf("NUMBER", "PLUS", "VECTOR", "MATRIX", "ALGEBRA", "FRACTION")),
        PuzzleDefinition("puzzle_17", "Puzzle 17: Tools", "Tools", "Expert", listOf("HAMMER", "WRENCH", "SCREW", "PLIERS", "DRILL", "SOCKET")),
        PuzzleDefinition("puzzle_18", "Puzzle 18: Myth", "Myth", "Expert", listOf("DRAGON", "WIZARD", "CASTLE", "KNIGHT", "LEGEND", "PHOENIX")),
        PuzzleDefinition("puzzle_19", "Puzzle 19: Energy", "Energy", "Expert", listOf("POWER", "SOLAR", "ATOMIC", "FUSION", "TURBINE", "BATTERY")),
        PuzzleDefinition("puzzle_20", "Puzzle 20: Galaxy", "Galaxy", "Expert", listOf("NEBULA", "GALAXY", "ASTRAL", "COSMOS", "ORBIT", "QUASAR"))
    )

    val puzzles: List<WordSearchPuzzle> by lazy {
        definitions.mapIndexed { index, definition ->
            generatePuzzle(definition, index)
        }
    }

    /** Returns all bundled-content validation errors; an empty list means the pack is valid. */
    fun validateAll(): List<String> = puzzles.flatMap { puzzle ->
        buildList {
            if (puzzle.grid.size != puzzle.rows) {
                add("${puzzle.id}: expected ${puzzle.rows} rows, found ${puzzle.grid.size}")
            }

            puzzle.grid.forEachIndexed { row, cells ->
                if (cells.size != puzzle.cols) {
                    add("${puzzle.id}: row $row expected ${puzzle.cols} columns, found ${cells.size}")
                }
            }

            puzzle.targetWords.forEach { target ->
                if (target.startRow !in 0 until puzzle.rows ||
                    target.startCol !in 0 until puzzle.cols ||
                    target.endRow !in 0 until puzzle.rows ||
                    target.endCol !in 0 until puzzle.cols
                ) {
                    add("${puzzle.id}: ${target.word} has invalid coordinates")
                    return@forEach
                }

                val path = pathBetween(target)
                if (path == null || path.size != target.word.length) {
                    add("${puzzle.id}: ${target.word} has invalid coordinates")
                    return@forEach
                }

                val boardWord = path.joinToString("") { position ->
                    puzzle.grid[position.row][position.col].toString()
                }
                if (!boardWord.equals(target.word, ignoreCase = true)) {
                    add("${puzzle.id}: ${target.word} does not match its coordinates")
                }

                val occurrences = findOccurrences(puzzle.grid, target.word)
                if (occurrences.size != 1) {
                    add("${puzzle.id}: ${target.word} has ${occurrences.size} possible paths")
                }
            }

            if (!difficultyRequirementsMet(puzzle)) {
                add("${puzzle.id}: insufficient direction variety for ${puzzle.difficulty} difficulty")
            }
        }
    }

    private fun generatePuzzle(definition: PuzzleDefinition, definitionIndex: Int): WordSearchPuzzle {
        repeat(MAX_GENERATION_ATTEMPTS) { attempt ->
            val random = Random(definition.id.hashCode() * 31 + definitionIndex * 997 + attempt)
            val grid = Array(GRID_SIZE) { CharArray(GRID_SIZE) { EMPTY_CELL } }
            val placements = mutableListOf<Placement>()
            var failed = false

            definition.words
                .sortedByDescending(String::length)
                .forEachIndexed { wordIndex, word ->
                    if (failed) return@forEachIndexed

                    val candidates = allPlacements(word, grid, random)
                        .filterNot { placement ->
                            // Avoid the easiest visual pattern: normal left-to-right
                            // words concentrated at the beginning of the pack.
                            definition.difficulty == "Challenge" &&
                                wordIndex < 4 &&
                                placement.deltaRow == 0 &&
                                placement.deltaCol == 1
                        }

                    val selected = choosePlacement(candidates, grid, random)
                    if (selected == null) {
                        failed = true
                    } else {
                        writePlacement(grid, selected)
                        placements += selected
                    }
                }

            if (failed) return@repeat

            fillEmptyCells(grid, random)
            val targetWords = placements
                .shuffled(random)
                .map { placement -> placement.toTargetWord(definition.id) }

            val puzzle = WordSearchPuzzle(
                id = definition.id,
                title = definition.title,
                theme = definition.theme,
                rows = GRID_SIZE,
                cols = GRID_SIZE,
                grid = grid.map(CharArray::toList),
                targetWords = targetWords,
                difficulty = definition.difficulty
            )

            if (validatePuzzle(puzzle)) return puzzle
        }

        error("Unable to generate a valid Word Search puzzle for ${definition.id}")
    }

    private fun validatePuzzle(puzzle: WordSearchPuzzle): Boolean =
        puzzle.targetWords.all { target ->
            val path = pathBetween(target) ?: return@all false
            val boardWord = path.joinToString("") { position ->
                puzzle.grid[position.row][position.col].toString()
            }
            boardWord.equals(target.word, ignoreCase = true) &&
                findOccurrences(puzzle.grid, target.word).size == 1
        } && difficultyRequirementsMet(puzzle)

    private fun difficultyRequirementsMet(puzzle: WordSearchPuzzle): Boolean {
        val directionsUsed = puzzle.targetWords.map { target ->
            (target.endRow - target.startRow).sign() to
                (target.endCol - target.startCol).sign()
        }.toSet()

        val hasDiagonal = directionsUsed.any { (row, col) -> row != 0 && col != 0 }
        val hasVertical = directionsUsed.any { (row, col) -> row != 0 && col == 0 }
        val hasReverse = directionsUsed.any { (row, col) -> row < 0 || col < 0 }

        return directionsUsed.size >= 4 && hasDiagonal && hasVertical && hasReverse
    }

    private fun allPlacements(
        word: String,
        grid: Array<CharArray>,
        random: Random
    ): List<Placement> = buildList {
        directions.shuffled(random).forEach { direction ->
            for (row in 0 until GRID_SIZE) {
                for (col in 0 until GRID_SIZE) {
                    val endRow = row + direction.deltaRow * (word.length - 1)
                    val endCol = col + direction.deltaCol * (word.length - 1)
                    if (endRow !in 0 until GRID_SIZE || endCol !in 0 until GRID_SIZE) continue

                    val placement = Placement(
                        word = word,
                        startRow = row,
                        startCol = col,
                        deltaRow = direction.deltaRow,
                        deltaCol = direction.deltaCol
                    )

                    if (canWrite(grid, placement)) add(placement)
                }
            }
        }
    }

    private fun choosePlacement(
        candidates: List<Placement>,
        grid: Array<CharArray>,
        random: Random
    ): Placement? {
        if (candidates.isEmpty()) return null

        val ranked = candidates
            .map { it to overlapCount(grid, it) }
            .sortedByDescending { (_, overlap) -> overlap }
            .take(minOf(24, candidates.size))

        return ranked[random.nextInt(ranked.size)].first
    }

    private fun canWrite(grid: Array<CharArray>, placement: Placement): Boolean =
        placement.positions().all { position ->
            val existing = grid[position.row][position.col]
            existing == EMPTY_CELL || existing == placement.word[position.offset]
        }

    private fun overlapCount(grid: Array<CharArray>, placement: Placement): Int =
        placement.positions().count { position ->
            grid[position.row][position.col] == placement.word[position.offset]
        }

    private fun writePlacement(grid: Array<CharArray>, placement: Placement) {
        placement.positions().forEach { position ->
            grid[position.row][position.col] = placement.word[position.offset]
        }
    }

    private fun fillEmptyCells(grid: Array<CharArray>, random: Random) {
        val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        for (row in grid.indices) {
            for (col in grid[row].indices) {
                if (grid[row][col] == EMPTY_CELL) {
                    grid[row][col] = alphabet[random.nextInt(alphabet.length)]
                }
            }
        }
    }

    private fun findOccurrences(
        grid: List<List<Char>>,
        word: String
    ): Set<PathKey> {
        val occurrences = mutableSetOf<PathKey>()
        for (row in grid.indices) {
            for (col in grid[row].indices) {
                directions.forEach { direction ->
                    val endRow = row + direction.deltaRow * (word.length - 1)
                    val endCol = col + direction.deltaCol * (word.length - 1)
                    if (endRow !in grid.indices || endCol !in grid[row].indices) return@forEach

                    val matches = word.indices.all { offset ->
                        grid[row + direction.deltaRow * offset][col + direction.deltaCol * offset] == word[offset]
                    }
                    if (matches) {
                        occurrences += PathKey.canonical(
                            CellPosition(row, col),
                            CellPosition(endRow, endCol)
                        )
                    }
                }
            }
        }
        return occurrences
    }

    private fun pathBetween(target: TargetWord): List<CellPosition>? {
        val deltaRow = target.endRow - target.startRow
        val deltaCol = target.endCol - target.startCol
        val absRow = abs(deltaRow)
        val absCol = abs(deltaCol)
        if (deltaRow != 0 && deltaCol != 0 && absRow != absCol) return null

        val steps = maxOf(absRow, absCol)
        val stepRow = deltaRow.sign()
        val stepCol = deltaCol.sign()
        return List(steps + 1) { offset ->
            CellPosition(
                row = target.startRow + offset * stepRow,
                col = target.startCol + offset * stepCol
            )
        }
    }

    private fun Int.sign(): Int = when {
        this > 0 -> 1
        this < 0 -> -1
        else -> 0
    }

    private data class PuzzleDefinition(
        val id: String,
        val title: String,
        val theme: String,
        val difficulty: String,
        val words: List<String>
    )

    private data class Direction(val deltaRow: Int, val deltaCol: Int)

    private data class Placement(
        val word: String,
        val startRow: Int,
        val startCol: Int,
        val deltaRow: Int,
        val deltaCol: Int
    ) {
        fun positions(): List<PositionWithOffset> = word.indices.map { offset ->
            PositionWithOffset(
                row = startRow + deltaRow * offset,
                col = startCol + deltaCol * offset,
                offset = offset
            )
        }

        fun toTargetWord(puzzleId: String): TargetWord = TargetWord(
            id = "${puzzleId}_$word",
            word = word,
            startRow = startRow,
            startCol = startCol,
            endRow = startRow + deltaRow * (word.length - 1),
            endCol = startCol + deltaCol * (word.length - 1)
        )
    }

    private data class PositionWithOffset(val row: Int, val col: Int, val offset: Int)

    private data class PathKey(val first: CellPosition, val second: CellPosition) {
        companion object {
            fun canonical(first: CellPosition, second: CellPosition): PathKey {
                val firstComesFirst = first.row < second.row ||
                    (first.row == second.row && first.col <= second.col)
                return if (firstComesFirst) PathKey(first, second) else PathKey(second, first)
            }
        }
    }
}
