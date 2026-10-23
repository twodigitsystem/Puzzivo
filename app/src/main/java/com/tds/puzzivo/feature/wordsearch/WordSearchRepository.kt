package com.tds.puzzivo.feature.wordsearch

object WordSearchRepository {

    val puzzles: List<WordSearchPuzzle> by lazy {
        listOf(
            createPuzzle(
                id = "puzzle_01",
                title = "Puzzle 1: Animals",
                theme = "Animals",
                rows = 8,
                cols = 8,
                words = listOf("CAT", "DOG", "LION", "BEAR", "WOLF", "FOX"),
                gridStrings = listOf(
                    "C A T X Y Z A B",
                    "D O G X P Q R S",
                    "L I O N T U V W",
                    "B E A R M N O P",
                    "W O L F A B C D",
                    "F O X K L M N O",
                    "P Q R S T U V W",
                    "X Y Z A B C D E"
                )
            ),
            createPuzzle(
                id = "puzzle_02",
                title = "Puzzle 2: Space",
                theme = "Space",
                rows = 8,
                cols = 8,
                words = listOf("STAR", "MOON", "SUN", "MARS", "ORBIT", "COMET"),
                gridStrings = listOf(
                    "S T A R A B C D",
                    "M O O N E F G H",
                    "S U N I J K L M",
                    "M A R S N O P Q",
                    "O R B I T R S T",
                    "C O M E T U V W",
                    "X Y Z A B C D E",
                    "F G H I J K L M"
                )
            ),
            createPuzzle(
                id = "puzzle_03",
                title = "Puzzle 3: Fruits",
                theme = "Fruits",
                rows = 8,
                cols = 8,
                words = listOf("APPLE", "BANANA", "FIG", "PEAR", "PLUM", "KIWI"),
                gridStrings = listOf(
                    "A P P L E X Y Z",
                    "B A N A N A A B",
                    "F I G C D E F G",
                    "P E A R H I J K",
                    "P L U M L M N O",
                    "K I W I P Q R S",
                    "T U V W X Y Z A",
                    "B C D E F G H I"
                )
            ),
            createPuzzle(
                id = "puzzle_04",
                title = "Puzzle 4: Colors",
                theme = "Colors",
                rows = 8,
                cols = 8,
                words = listOf("RED", "BLUE", "GREEN", "PINK", "GOLD", "CYAN"),
                gridStrings = listOf(
                    "R E D A B C D E",
                    "B L U E F G H I",
                    "G R E E N J K L",
                    "P I N K M N O P",
                    "G O L D Q R S T",
                    "C Y A N U V W X",
                    "Y Z A B C D E F",
                    "G H I J K L M N"
                )
            ),
            createPuzzle(
                id = "puzzle_05",
                title = "Puzzle 5: Tech",
                theme = "Tech",
                rows = 8,
                cols = 8,
                words = listOf("CODE", "BYTE", "DATA", "CHIP", "NODE", "JAVA"),
                gridStrings = listOf(
                    "C O D E A B C D",
                    "B Y T E E F G H",
                    "D A T A I J K L",
                    "C H I P M N O P",
                    "N O D E Q R S T",
                    "J A V A U V W X",
                    "Y Z A B C D E F",
                    "G H I J K L M N"
                )
            ),
            createPuzzle(
                id = "puzzle_06",
                title = "Puzzle 6: Weather",
                theme = "Weather",
                rows = 8,
                cols = 8,
                words = listOf("RAIN", "SNOW", "WIND", "HAIL", "SUN", "CLOUDS"),
                gridStrings = listOf(
                    "R A I N A B C D",
                    "S N O W E F G H",
                    "W I N D I J K L",
                    "H A I L M N O P",
                    "S U N Q R S T U",
                    "C L O U D S V W",
                    "X Y Z A B C D E",
                    "F G H I J K L M"
                )
            ),
            createPuzzle(
                id = "puzzle_07",
                title = "Puzzle 7: Music",
                theme = "Music",
                rows = 8,
                cols = 8,
                words = listOf("SONG", "BEAT", "ROCK", "JAZZ", "NOTE", "BASS"),
                gridStrings = listOf(
                    "S O N G A B C D",
                    "B E A T E F G H",
                    "R O C K I J K L",
                    "J A Z Z M N O P",
                    "N O T E Q R S T",
                    "B A S S U V W X",
                    "Y Z A B C D E F",
                    "G H I J K L M N"
                )
            ),
            createPuzzle(
                id = "puzzle_08",
                title = "Puzzle 8: Sports",
                theme = "Sports",
                rows = 8,
                cols = 8,
                words = listOf("GOLF", "SWIM", "RUN", "BALL", "RACE", "TEAM"),
                gridStrings = listOf(
                    "G O L F A B C D",
                    "S W I M E F G H",
                    "R U N I J K L M",
                    "B A L L N O P Q",
                    "R A C E R S T U",
                    "T E A M V W X Y",
                    "Z A B C D E F G",
                    "H I J K L M N O"
                )
            ),
            createPuzzle(
                id = "puzzle_09",
                title = "Puzzle 9: Nature",
                theme = "Nature",
                rows = 8,
                cols = 8,
                words = listOf("TREE", "LEAF", "WOOD", "POND", "HILL", "BIRD"),
                gridStrings = listOf(
                    "T R E E A B C D",
                    "L E A F E F G H",
                    "W O O D I J K L",
                    "P O N D M N O P",
                    "H I L L Q R S T",
                    "B I R D U V W X",
                    "Y Z A B C D E F",
                    "G H I J K L M N"
                )
            ),
            createPuzzle(
                id = "puzzle_10",
                title = "Puzzle 10: Ocean",
                theme = "Ocean",
                rows = 8,
                cols = 8,
                words = listOf("FISH", "WAVE", "SAND", "SHIP", "KELP", "SEAL"),
                gridStrings = listOf(
                    "F I S H A B C D",
                    "W A V E E F G H",
                    "S A N D I J K L",
                    "S H I P M N O P",
                    "K E L P Q R S T",
                    "S E A L U V W X",
                    "Y Z A B C D E F",
                    "G H I J K L M N"
                )
            ),
            createPuzzle(
                id = "puzzle_11",
                title = "Puzzle 11: Food",
                theme = "Food",
                rows = 8,
                cols = 8,
                words = listOf("RICE", "SOUP", "CAKE", "PIE", "MILK", "MEAT"),
                gridStrings = listOf(
                    "R I C E A B C D",
                    "S O U P E F G H",
                    "C A K E I J K L",
                    "P I E M N O P Q",
                    "M I L K R S T U",
                    "M E A T V W X Y",
                    "Z A B C D E F G",
                    "H I J K L M N O"
                )
            ),
            createPuzzle(
                id = "puzzle_12",
                title = "Puzzle 12: Time",
                theme = "Time",
                rows = 8,
                cols = 8,
                words = listOf("HOUR", "NOON", "WEEK", "YEAR", "TICK", "DAWN"),
                gridStrings = listOf(
                    "H O U R A B C D",
                    "N O O N E F G H",
                    "W E E K I J K L",
                    "Y E A R M N O P",
                    "T I C K Q R S T",
                    "D A W N U V W X",
                    "Y Z A B C D E F",
                    "G H I J K L M N"
                )
            ),
            createPuzzle(
                id = "puzzle_13",
                title = "Puzzle 13: Gems",
                theme = "Gems",
                rows = 8,
                cols = 8,
                words = listOf("RUBY", "JADE", "OPAL", "GOLD", "PEARL", "ONYX"),
                gridStrings = listOf(
                    "R U B Y A B C D",
                    "J A D E E F G H",
                    "O P A L I J K L",
                    "G O L D M N O P",
                    "P E A R L Q R S",
                    "O N Y X T U V W",
                    "X Y Z A B C D E",
                    "F G H I J K L M"
                )
            ),
            createPuzzle(
                id = "puzzle_14",
                title = "Puzzle 14: City",
                theme = "City",
                rows = 8,
                cols = 8,
                words = listOf("PARK", "ROAD", "BUS", "TAXI", "WALL", "PORT"),
                gridStrings = listOf(
                    "P A R K A B C D",
                    "R O A D E F G H",
                    "B U S I J K L M",
                    "T A X I N O P Q",
                    "W A L L R S T U",
                    "P O R T V W X Y",
                    "Z A B C D E F G",
                    "H I J K L M N O"
                )
            ),
            createPuzzle(
                id = "puzzle_15",
                title = "Puzzle 15: Plants",
                theme = "Plants",
                rows = 8,
                cols = 8,
                words = listOf("ROSE", "FERN", "MOSS", "MINT", "REED", "BUSH"),
                gridStrings = listOf(
                    "R O S E A B C D",
                    "F E R N E F G H",
                    "M O S S I J K L",
                    "M I N T M N O P",
                    "R E E D Q R S T",
                    "B U S H U V W X",
                    "Y Z A B C D E F",
                    "G H I J K L M N"
                )
            ),
            createPuzzle(
                id = "puzzle_16",
                title = "Puzzle 16: Math",
                theme = "Math",
                rows = 8,
                cols = 8,
                words = listOf("ZERO", "PLUS", "UNIT", "SUM", "GRID", "EVEN"),
                gridStrings = listOf(
                    "Z E R O A B C D",
                    "P L U S E F G H",
                    "U N I T I J K L",
                    "S U M M N O P Q",
                    "G R I D R S T U",
                    "E V E N V W X Y",
                    "Z A B C D E F G",
                    "H I J K L M N O"
                )
            ),
            createPuzzle(
                id = "puzzle_17",
                title = "Puzzle 17: Tools",
                theme = "Tools",
                rows = 8,
                cols = 8,
                words = listOf("SAW", "AXE", "NAIL", "GEAR", "BOLT", "PUMP"),
                gridStrings = listOf(
                    "S A W A B C D E",
                    "A X E F G H I J",
                    "N A I L K L M N",
                    "G E A R O P Q R",
                    "B O L T S T U V",
                    "P U M P W X Y Z",
                    "A B C D E F G H",
                    "I J K L M N O P"
                )
            ),
            createPuzzle(
                id = "puzzle_18",
                title = "Puzzle 18: Myth",
                theme = "Myth",
                rows = 8,
                cols = 8,
                words = listOf("HERO", "RUNE", "SAGA", "KING", "CROWN", "FATE"),
                gridStrings = listOf(
                    "H E R O A B C D",
                    "R U N E E F G H",
                    "S A G A I J K L",
                    "K I N G M N O P",
                    "C R O W N Q R S",
                    "F A T E T U V W",
                    "X Y Z A B C D E",
                    "F G H I J K L M"
                )
            ),
            createPuzzle(
                id = "puzzle_19",
                title = "Puzzle 19: Energy",
                theme = "Energy",
                rows = 8,
                cols = 8,
                words = listOf("VOLT", "HEAT", "WATT", "GLOW", "FUEL", "ATOM"),
                gridStrings = listOf(
                    "V O L T A B C D",
                    "H E A T E F G H",
                    "W A T T I J K L",
                    "G L O W M N O P",
                    "F U E L Q R S T",
                    "A T O M U V W X",
                    "Y Z A B C D E F",
                    "G H I J K L M N"
                )
            ),
            createPuzzle(
                id = "puzzle_20",
                title = "Puzzle 20: Galaxy",
                theme = "Galaxy",
                rows = 8,
                cols = 8,
                words = listOf("NEBULA", "NOVA", "DUST", "VOID", "MASS", "HALO"),
                gridStrings = listOf(
                    "N E B U L A A B",
                    "N O V A C D E F",
                    "D U S T G H I J",
                    "V O I D K L M N",
                    "M A S S O P Q R",
                    "H A L O S T U V",
                    "W X Y Z A B C D",
                    "E F G H I J K L"
                )
            )
        )
    }

    private fun createPuzzle(
        id: String,
        title: String,
        theme: String,
        rows: Int,
        cols: Int,
        words: List<String>,
        gridStrings: List<String>
    ): WordSearchPuzzle {
        val grid = gridStrings.map { line ->
            line.replace(" ", "").toList()
        }

        val directions = listOf(
            Pair(0, 1),   // Horizontal Right
            Pair(0, -1),  // Horizontal Left
            Pair(1, 0),   // Vertical Down
            Pair(-1, 0),  // Vertical Up
            Pair(1, 1),   // Diagonal Down-Right
            Pair(1, -1),  // Diagonal Down-Left
            Pair(-1, 1),  // Diagonal Up-Right
            Pair(-1, -1)  // Diagonal Up-Left
        )

        val targetWords = words.map { word ->
            var startR = -1
            var startC = -1
            var endR = -1
            var endC = -1

            searchLoop@ for (r in 0 until rows) {
                for (c in 0 until cols) {
                    for ((dr, dc) in directions) {
                        var match = true
                        for (i in word.indices) {
                            val nr = r + dr * i
                            val nc = c + dc * i
                            if (nr !in 0 until rows || nc !in 0 until cols || grid[nr][nc] != word[i]) {
                                match = false
                                break
                            }
                        }
                        if (match) {
                            startR = r
                            startC = c
                            endR = r + dr * (word.length - 1)
                            endC = c + dc * (word.length - 1)
                            break@searchLoop
                        }
                    }
                }
            }

            TargetWord(
                id = "${id}_$word",
                word = word,
                startRow = startR,
                startCol = startC,
                endRow = endR,
                endCol = endC
            )
        }

        return WordSearchPuzzle(
            id = id,
            title = title,
            theme = theme,
            rows = rows,
            cols = cols,
            grid = grid,
            targetWords = targetWords
        )
    }
}
