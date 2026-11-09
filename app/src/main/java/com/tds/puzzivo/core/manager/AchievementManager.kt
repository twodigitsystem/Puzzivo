package com.tds.puzzivo.core.manager

import com.tds.puzzivo.core.database.AchievementDao
import com.tds.puzzivo.core.database.ProgressDao
import com.tds.puzzivo.core.model.Achievement
import com.tds.puzzivo.core.model.GameType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AchievementManager @Inject constructor(
    private val achievementDao: AchievementDao,
    private val progressDao: ProgressDao,
    private val progressionManager: ProgressionManager
) {

    val allAchievements: Flow<List<Achievement>> = achievementDao.getAllAchievements()

    fun getAchievementsForGame(gameType: GameType): Flow<List<Achievement>> =
        achievementDao.getAchievementsForGame(gameType)

    val unlockedAchievements: Flow<List<Achievement>> = achievementDao.getUnlockedAchievements()

    suspend fun getUnlockedCount(): Int = achievementDao.getUnlockedCount()

    suspend fun checkAndUnlockAchievements(
        gameType: GameType,
        score: Int,
        gamesPlayed: Int,
        gamesWon: Int,
        perfectGame: Boolean,
        hintsUsed: Int,
        durationSeconds: Int = Int.MAX_VALUE
    ) {
        val achievements = achievementDao.getAchievementsForGame(gameType).first()
        
        achievements.forEach { achievement ->
            if (!achievement.unlocked) {
                val shouldUnlock = checkAchievementRequirement(
                    achievement,
                    score,
                    gamesPlayed,
                    gamesWon,
                    perfectGame,
                    hintsUsed,
                    durationSeconds
                )
                
                if (shouldUnlock) {
                    unlockAchievement(achievement)
                }
            }
        }
    }

    suspend fun checkUniversalAchievements(
        totalGamesPlayed: Int,
        totalGamesWon: Int,
        currentStreak: Int,
        gameWon: Boolean
    ) {
        val achievements = achievementDao.getAllAchievements().first()
        achievements
            .filter { !it.unlocked }
            .forEach { achievement ->
                val shouldUnlock = when (achievement.id) {
                    "first_game" -> totalGamesPlayed >= 1
                    "play_10_games" -> totalGamesPlayed >= 10
                    "play_50_games" -> totalGamesPlayed >= 50
                    "play_100_games" -> totalGamesPlayed >= 100
                    "win_1_game" -> totalGamesWon >= 1 && gameWon
                    "win_10_games" -> totalGamesWon >= 10
                    "win_50_games" -> totalGamesWon >= 50
                    "streak_3_days" -> currentStreak >= 3
                    "streak_7_days" -> currentStreak >= 7
                    else -> false
                }
                if (shouldUnlock) unlockAchievement(achievement)
            }
    }

    private fun checkAchievementRequirement(
        achievement: Achievement,
        score: Int,
        gamesPlayed: Int,
        gamesWon: Int,
        perfectGame: Boolean,
        hintsUsed: Int,
        durationSeconds: Int
    ): Boolean {
        return when {
            achievement.id.startsWith("first_win_") -> gamesWon >= 1
            achievement.id.startsWith("10_wins_") -> gamesWon >= 10
            achievement.id.startsWith("50_wins_") -> gamesWon >= 50
            achievement.id.startsWith("100_wins_") -> gamesWon >= 100
            achievement.id.startsWith("speed_") -> gamesWon >= 1 && durationSeconds < 60
            achievement.id.contains("score") -> score >= achievement.requirement
            achievement.id.contains("games_played") -> gamesPlayed >= achievement.requirement
            achievement.id.contains("games_won") -> gamesWon >= achievement.requirement
            achievement.id.contains("perfect") -> perfectGame && gamesPlayed >= achievement.requirement
            achievement.id.contains("no_hints") -> hintsUsed == 0 && gamesWon >= achievement.requirement
            else -> false
        }
    }

    suspend fun unlockAchievement(achievementId: String): Achievement? {
        val achievement = achievementDao.getAchievementById(achievementId)
        if (achievement != null && !achievement.unlocked) {
            unlockAchievement(achievement)
            return achievementDao.getAchievementById(achievementId)
        }
        return null
    }

    suspend fun unlockAchievement(achievement: Achievement) {
        val today = LocalDate.now().toString()
        achievementDao.unlockAchievement(achievement.id, today)
        
        if (achievement.coinReward > 0) {
            progressDao.addCoins(achievement.coinReward)
        }
        if (achievement.xpReward > 0) {
            progressionManager.addXP(achievement.xpReward)
        }
    }

    suspend fun updateProgress(achievementId: String, progress: Int) {
        achievementDao.updateProgress(achievementId, progress)
        
        val achievement = achievementDao.getAchievementById(achievementId)
        if (achievement != null && !achievement.unlocked && progress >= achievement.requirement) {
            unlockAchievement(achievement)
        }
    }

    suspend fun initializeDefaultAchievements() {
        val existing = achievementDao.getAllAchievements().first()
        if (existing.isEmpty()) {
            val defaults = DefaultAchievements.getAll()
            achievementDao.insertAll(defaults)
        }
    }
}
