package com.tds.puzzivo.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tds.puzzivo.core.model.PlayerInventoryItem
import com.tds.puzzivo.core.model.PlayerProgress
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressDao {
    @Query("SELECT * FROM player_progress WHERE id = 1")
    fun getProgress(): Flow<PlayerProgress?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: PlayerProgress)

    @Update
    suspend fun updateProgress(progress: PlayerProgress)

    @Query("UPDATE player_progress SET currentXP = currentXP + :xp, totalXPEarned = totalXPEarned + :xp WHERE id = 1")
    suspend fun addXP(xp: Int)

    @Query("UPDATE player_progress SET coins = coins + :amount, totalCoinsEarned = totalCoinsEarned + :amount WHERE id = 1")
    suspend fun addCoins(amount: Int)

    @Query("UPDATE player_progress SET coins = coins - :amount WHERE id = 1 AND coins >= :amount")
    suspend fun spendCoins(amount: Int): Int

    @Query("UPDATE player_progress SET gems = gems + :amount WHERE id = 1")
    suspend fun addGems(amount: Int)

    @Query("UPDATE player_progress SET gems = gems - :amount WHERE id = 1 AND gems >= :amount")
    suspend fun spendGems(amount: Int): Int

    @Query("UPDATE player_progress SET level = :level WHERE id = 1")
    suspend fun setLevel(level: Int)

    @Query("UPDATE player_progress SET selectedThemeId = :themeId WHERE id = 1")
    suspend fun setSelectedTheme(themeId: String)

    // Inventory
    @Query("SELECT * FROM player_inventory WHERE itemId = :itemId")
    suspend fun getInventoryItem(itemId: String): PlayerInventoryItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventoryItem(item: PlayerInventoryItem)

    @Query("UPDATE player_inventory SET quantity = quantity + :amount WHERE itemId = :itemId")
    suspend fun addInventoryItem(itemId: String, amount: Int)

    @Query("UPDATE player_inventory SET quantity = quantity - :amount WHERE itemId = :itemId AND quantity >= :amount")
    suspend fun useInventoryItem(itemId: String, amount: Int): Int
}
