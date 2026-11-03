package com.tds.puzzivo.core.manager

import com.tds.puzzivo.R
import com.tds.puzzivo.core.model.Achievement
import com.tds.puzzivo.core.model.AchievementRarity
import com.tds.puzzivo.core.model.GameType

/**
 * Default achievements for all games
 */
object DefaultAchievements {

    fun getAll(): List<Achievement> = buildList {
        // Universal Achievements
        addAll(getUniversalAchievements())

        // Per-Game Achievements
        GameType.entries.forEach { gameType ->
            addAll(getGameAchievements(gameType))
        }
    }

    private fun getUniversalAchievements() = listOf(
        Achievement(
            id = "first_game",
            gameType = GameType.NUMBER_MERGE,
            titleRes = R.string.achievement_first_game_title,
            descriptionRes = R.string.achievement_first_game_desc,
            iconRes = R.mipmap.ic_launcher,
            requirement = 1,
            rarity = AchievementRarity.COMMON,
            coinReward = 10,
            xpReward = 10
        ),
        Achievement(
            id = "play_10_games",
            gameType = GameType.NUMBER_MERGE,
            titleRes = R.string.achievement_play_10_title,
            descriptionRes = R.string.achievement_play_10_desc,
            iconRes = R.mipmap.ic_launcher,
            requirement = 10,
            rarity = AchievementRarity.COMMON,
            coinReward = 25,
            xpReward = 50
        ),
        Achievement(
            id = "play_50_games",
            gameType = GameType.NUMBER_MERGE,
            titleRes = R.string.achievement_play_50_title,
            descriptionRes = R.string.achievement_play_50_desc,
            iconRes = R.mipmap.ic_launcher,
            requirement = 50,
            rarity = AchievementRarity.RARE,
            coinReward = 50,
            xpReward = 100
        ),
        Achievement(
            id = "play_100_games",
            gameType = GameType.NUMBER_MERGE,
            titleRes = R.string.achievement_play_100_title,
            descriptionRes = R.string.achievement_play_100_desc,
            iconRes = R.mipmap.ic_launcher,
            requirement = 100,
            rarity = AchievementRarity.EPIC,
            coinReward = 100,
            xpReward = 200
        ),
        Achievement(
            id = "win_1_game",
            gameType = GameType.NUMBER_MERGE,
            titleRes = R.string.achievement_win_1_title,
            descriptionRes = R.string.achievement_win_1_desc,
            iconRes = R.mipmap.ic_launcher,
            requirement = 1,
            rarity = AchievementRarity.COMMON,
            coinReward = 15,
            xpReward = 20
        ),
        Achievement(
            id = "win_10_games",
            gameType = GameType.NUMBER_MERGE,
            titleRes = R.string.achievement_win_10_title,
            descriptionRes = R.string.achievement_win_10_desc,
            iconRes = R.mipmap.ic_launcher,
            requirement = 10,
            rarity = AchievementRarity.RARE,
            coinReward = 50,
            xpReward = 100
        ),
        Achievement(
            id = "win_50_games",
            gameType = GameType.NUMBER_MERGE,
            titleRes = R.string.achievement_win_50_title,
            descriptionRes = R.string.achievement_win_50_desc,
            iconRes = R.mipmap.ic_launcher,
            requirement = 50,
            rarity = AchievementRarity.EPIC,
            coinReward = 150,
            xpReward = 300
        ),
        Achievement(
            id = "streak_3_days",
            gameType = GameType.NUMBER_MERGE,
            titleRes = R.string.achievement_streak_3_title,
            descriptionRes = R.string.achievement_streak_3_desc,
            iconRes = R.mipmap.ic_launcher,
            requirement = 3,
            rarity = AchievementRarity.COMMON,
            coinReward = 20,
            xpReward = 30
        ),
        Achievement(
            id = "streak_7_days",
            gameType = GameType.NUMBER_MERGE,
            titleRes = R.string.achievement_streak_7_title,
            descriptionRes = R.string.achievement_streak_7_desc,
            iconRes = R.mipmap.ic_launcher,
            requirement = 7,
            rarity = AchievementRarity.RARE,
            coinReward = 50,
            xpReward = 100
        )
    )

    private fun getGameAchievements(gameType: GameType) = listOf(
        Achievement(
            id = "first_win_${gameType.name.lowercase()}",
            gameType = gameType,
            titleRes = R.string.achievement_game_first_win_title,
            descriptionRes = R.string.achievement_game_first_win_desc,
            iconRes = R.mipmap.ic_launcher,
            requirement = 1,
            rarity = AchievementRarity.COMMON,
            coinReward = 15,
            xpReward = 25
        ),
        Achievement(
            id = "10_wins_${gameType.name.lowercase()}",
            gameType = gameType,
            titleRes = R.string.achievement_game_10_wins_title,
            descriptionRes = R.string.achievement_game_10_wins_desc,
            iconRes = R.mipmap.ic_launcher,
            requirement = 10,
            rarity = AchievementRarity.RARE,
            coinReward = 50,
            xpReward = 100
        ),
        Achievement(
            id = "speed_${gameType.name.lowercase()}",
            gameType = gameType,
            titleRes = R.string.achievement_game_speed_title,
            descriptionRes = R.string.achievement_game_speed_desc,
            iconRes = R.mipmap.ic_launcher,
            requirement = 1,
            rarity = AchievementRarity.EPIC,
            coinReward = 75,
            xpReward = 150
        )
    )
}
