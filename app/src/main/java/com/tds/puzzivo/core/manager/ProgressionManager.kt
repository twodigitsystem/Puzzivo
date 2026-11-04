package com.tds.puzzivo.core.manager

import com.tds.puzzivo.core.database.ProgressDao
import com.tds.puzzivo.core.model.LevelReward
import com.tds.puzzivo.core.model.LevelUpResult
import com.tds.puzzivo.core.model.PlayerProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map as flowMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProgressionManager @Inject constructor(
    private val progressDao: ProgressDao
) {

    val playerProgress: Flow<PlayerProgress> = progressDao.getProgress().flowMap { progress ->
        progress ?: PlayerProgress() // Return default if null
    }

    suspend fun initializeProgress() {
        val existing = progressDao.getProgress().first()
        if (existing == null) {
            progressDao.insertProgress(PlayerProgress())
        }
    }

    suspend fun initializePlayerProgress() {
        initializeProgress()
    }

    suspend fun addXP(amount: Int): LevelUpResult {
        val current = playerProgress.first()
        val totalXPEarned = current.totalXPEarned + amount
        var newXP = current.currentXP + amount
        var newLevel = current.level
        var leveledUp = false
        val levelsGained = mutableListOf<Int>()

        // Check for level ups
        while (newXP >= PlayerProgress.calculateXPForLevel(newLevel + 1)) {
            val xpNeeded = PlayerProgress.calculateXPForLevel(newLevel + 1)
            newXP -= xpNeeded
            newLevel++
            leveledUp = true
            levelsGained.add(newLevel)
        }

        // Atomically save updated level, currentXP, and totalXPEarned
        val updated = current.copy(
            level = newLevel,
            currentXP = newXP,
            totalXPEarned = totalXPEarned
        )
        progressDao.updateProgress(updated)

        return LevelUpResult(
            leveledUp = leveledUp,
            newLevel = newLevel,
            levelsGained = levelsGained,
            rewards = calculateLevelRewards(levelsGained)
        )
    }

    private fun calculateLevelRewards(levels: List<Int>): List<LevelReward> {
        return levels.map { level ->
            when {
                level % 10 == 0 -> LevelReward.Gems(50) as LevelReward
                level % 5 == 0 -> LevelReward.Coins(level * 20) as LevelReward
                else -> LevelReward.Coins(level * 10) as LevelReward
            }
        }
    }

    suspend fun addCoins(amount: Int) {
        progressDao.addCoins(amount)
    }

    suspend fun spendCoins(amount: Int): Boolean {
        val rowsAffected = progressDao.spendCoins(amount)
        return rowsAffected > 0
    }

    suspend fun addGems(amount: Int) {
        progressDao.addGems(amount)
    }

    suspend fun spendGems(amount: Int): Boolean {
        val rowsAffected = progressDao.spendGems(amount)
        return rowsAffected > 0
    }

    suspend fun isThemeUnlocked(themeId: String): Boolean {
        val current = playerProgress.first()
        return current.unlockedThemes.contains(themeId)
    }

    suspend fun setSelectedTheme(themeId: String) {
        val current = playerProgress.first()
        progressDao.updateProgress(current.copy(selectedThemeId = themeId))
    }

    suspend fun unlockTheme(themeId: String) {
        val current = playerProgress.first()
        val updatedThemes = current.unlockedThemes + themeId
        progressDao.updateProgress(current.copy(unlockedThemes = updatedThemes))
    }

    suspend fun addInventoryItem(itemId: String, quantity: Int = 1) {
        // Log inventory acquisition
    }
}
