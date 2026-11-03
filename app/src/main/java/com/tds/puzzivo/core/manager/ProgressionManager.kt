package com.tds.puzzivo.core.manager

import com.tds.puzzivo.core.database.ProgressDao
import com.tds.puzzivo.core.model.PlayerProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProgressionManager @Inject constructor(
    private val progressDao: ProgressDao
) {

    val playerProgress: Flow<PlayerProgress> = progressDao.getProgress().map { progress ->
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
                level % 10 == 0 -> LevelReward.Gems(50)
                level % 5 == 0 -> LevelReward.Coins(level * 20)
                else -> LevelReward.Coins(level * 10)
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

    suspend fun getCoins(): Int {
        return playerProgress.first().coins
    }

    suspend fun getGems(): Int {
        return playerProgress.first().gems
    }

    suspend fun getLevel(): Int {
        return playerProgress.first().level
    }

    suspend fun setSelectedTheme(themeId: String) {
        progressDao.setSelectedTheme(themeId)
    }

    suspend fun unlockTheme(themeId: String) {
        val current = playerProgress.first()
        val updated = current.copy(
            unlockedThemes = current.unlockedThemes + themeId
        )
        progressDao.updateProgress(updated)
    }

    suspend fun isThemeUnlocked(themeId: String): Boolean {
        val current = playerProgress.first()
        return current.unlockedThemes.contains(themeId)
    }

    suspend fun setPremiumUnlocked(unlocked: Boolean) {
        val current = playerProgress.first()
        val updated = current.copy(premiumUnlocked = unlocked)
        progressDao.updateProgress(updated)
    }

    // Inventory management
    suspend fun addInventoryItem(itemId: String, quantity: Int) {
        progressDao.addInventoryItem(itemId, quantity)
    }

    suspend fun useInventoryItem(itemId: String, quantity: Int = 1): Boolean {
        val rowsAffected = progressDao.useInventoryItem(itemId, quantity)
        return rowsAffected > 0
    }

    suspend fun getInventoryQuantity(itemId: String): Int {
        return progressDao.getInventoryItem(itemId)?.quantity ?: 0
    }
}

data class LevelUpResult(
    val leveledUp: Boolean,
    val newLevel: Int,
    val levelsGained: List<Int>,
    val rewards: List<LevelReward>
)

sealed class LevelReward {
    data class Coins(val amount: Int) : LevelReward()
    data class Gems(val amount: Int) : LevelReward()
    data class ThemeUnlock(val themeId: String) : LevelReward()
}
