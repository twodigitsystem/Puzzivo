package com.tds.puzzivo.feature.wordscramble

import com.tds.puzzivo.core.model.DifficultyLevel

data class ScrambleWord(
    val id: String,
    val word: String,
    val category: String,
    val hint: String,
    val difficulty: DifficultyLevel
)

object WordScrambleRepository {

    val easyWords = listOf(
        ScrambleWord("e1", "CAT", "ANIMAL", "A small domesticated feline", DifficultyLevel.EASY),
        ScrambleWord("e2", "DOG", "ANIMAL", "Man's best friend", DifficultyLevel.EASY),
        ScrambleWord("e3", "SUN", "NATURE", "The star at the center of our solar system", DifficultyLevel.EASY),
        ScrambleWord("e4", "BLUE", "COLOR", "The color of a clear sky", DifficultyLevel.EASY),
        ScrambleWord("e5", "GAME", "RECREATION", "An activity played for fun", DifficultyLevel.EASY),
        ScrambleWord("e6", "BOOK", "EDUCATION", "Written or printed pages bound together", DifficultyLevel.EASY),
        ScrambleWord("e7", "FISH", "ANIMAL", "A limbless cold-blooded vertebrate animal with gills", DifficultyLevel.EASY),
        ScrambleWord("e8", "TREE", "NATURE", "A woody perennial plant with a trunk", DifficultyLevel.EASY),
        ScrambleWord("e9", "STAR", "SPACE", "A luminous point in the night sky", DifficultyLevel.EASY),
        ScrambleWord("e10", "MILK", "FOOD", "A white liquid produced by mammals", DifficultyLevel.EASY)
    )

    val mediumWords = listOf(
        ScrambleWord("m1", "PUZZLE", "GAMES", "A game or problem that tests ingenuity", DifficultyLevel.MEDIUM),
        ScrambleWord("m2", "MEMORY", "BRAIN", "The faculty by which the mind stores information", DifficultyLevel.MEDIUM),
        ScrambleWord("m3", "NUMBER", "MATH", "A mathematical value used for counting", DifficultyLevel.MEDIUM),
        ScrambleWord("m4", "GALAXY", "SPACE", "A system of millions or billions of stars", DifficultyLevel.MEDIUM),
        ScrambleWord("m5", "CIPHER", "SECURITY", "A secret or disguised way of writing", DifficultyLevel.MEDIUM),
        ScrambleWord("m6", "BRIGHT", "LIGHT", "Giving out or reflecting much light", DifficultyLevel.MEDIUM),
        ScrambleWord("m7", "PLANET", "SPACE", "A celestial body moving in an elliptical orbit", DifficultyLevel.MEDIUM),
        ScrambleWord("m8", "SPRING", "NATURE", "The season after winter and before summer", DifficultyLevel.MEDIUM),
        ScrambleWord("m9", "CASTLE", "BUILDING", "A large fortified building", DifficultyLevel.MEDIUM),
        ScrambleWord("m10", "SILVER", "ELEMENT", "A precious shiny metallic element", DifficultyLevel.MEDIUM)
    )

    val hardWords = listOf(
        ScrambleWord("h1", "ALGORITHM", "TECH", "A process or set of rules in calculations", DifficultyLevel.HARD),
        ScrambleWord("h2", "CHALLENGE", "GAMES", "A call to take part in a contest", DifficultyLevel.HARD),
        ScrambleWord("h3", "SPECTRUM", "SCIENCE", "A band of colors produced by light", DifficultyLevel.HARD),
        ScrambleWord("h4", "SYMPHONY", "MUSIC", "An elaborate musical composition", DifficultyLevel.HARD),
        ScrambleWord("h5", "UNIVERSE", "SPACE", "All existing matter and space considered as a whole", DifficultyLevel.HARD),
        ScrambleWord("h6", "AVALANCHE", "NATURE", "A mass of snow sliding rapidly down a mountain", DifficultyLevel.HARD),
        ScrambleWord("h7", "LABYRINTH", "PUZZLE", "A complicated irregular network of passages", DifficultyLevel.HARD),
        ScrambleWord("h8", "PYRAMID", "STRUCTURE", "A monumental structure with a square base", DifficultyLevel.HARD),
        ScrambleWord("h9", "CHAMPION", "SPORTS", "A person who has defeated all rivals in a contest", DifficultyLevel.HARD),
        ScrambleWord("h10", "TREASURE", "ADVENTURE", "A quantity of precious metals, gems, or valuable objects", DifficultyLevel.HARD)
    )

    fun getWordsForDifficulty(difficulty: DifficultyLevel): List<ScrambleWord> {
        return when (difficulty) {
            DifficultyLevel.EASY -> easyWords.shuffled()
            DifficultyLevel.MEDIUM -> mediumWords.shuffled()
            DifficultyLevel.HARD, DifficultyLevel.EXPERT -> hardWords.shuffled()
        }
    }
}
