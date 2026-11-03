package com.tds.puzzivo.core.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tds.puzzivo.core.model.Achievement
import com.tds.puzzivo.core.model.GameType
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements ORDER BY rarity DESC, unlocked ASC")
    fun getAllAchievements(): Flow<List<Achievement>>

    @Query("SELECT * FROM achievements WHERE gameType = :gameType")
    fun getAchievementsForGame(gameType: GameType): Flow<List<Achievement>>

    @Query("SELECT * FROM achievements WHERE unlocked = 1 ORDER BY unlockedDate DESC")
    fun getUnlockedAchievements(): Flow<List<Achievement>>

    @Query("SELECT COUNT(*) FROM achievements WHERE unlocked = 1")
    suspend fun getUnlockedCount(): Int

    @Query("SELECT * FROM achievements WHERE id = :id")
    suspend fun getAchievementById(id: String): Achievement?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievement(achievement: Achievement)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(achievements: List<Achievement>)

    @Update
    suspend fun updateAchievement(achievement: Achievement)

    @Query("UPDATE achievements SET progress = :progress WHERE id = :id")
    suspend fun updateProgress(id: String, progress: Int)

    @Query("UPDATE achievements SET unlocked = 1, unlockedDate = :date WHERE id = :id")
    suspend fun unlockAchievement(id: String, date: String)

    @Delete
    suspend fun deleteAchievement(achievement: Achievement)

    @Query("DELETE FROM achievements")
    suspend fun deleteAll()
}
