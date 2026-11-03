package com.tds.puzzivo.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tds.puzzivo.core.model.DailyChallenge
import kotlinx.coroutines.flow.Flow

@Dao
interface ChallengeDao {
    @Query("SELECT * FROM daily_challenges WHERE date = :date")
    fun getChallengeForDate(date: String): Flow<DailyChallenge?>

    @Query("SELECT * FROM daily_challenges WHERE isCompleted = 1 ORDER BY completedDate DESC LIMIT 30")
    fun getCompletedChallenges(): Flow<List<DailyChallenge>>

    @Query("SELECT COUNT(*) FROM daily_challenges WHERE isCompleted = 1")
    suspend fun getCompletedCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenge(challenge: DailyChallenge)

    @Update
    suspend fun updateChallenge(challenge: DailyChallenge)

    @Query("UPDATE daily_challenges SET progress = :progress WHERE id = :id")
    suspend fun updateProgress(id: String, progress: Int)

    @Query("UPDATE daily_challenges SET isCompleted = 1, completedDate = :date WHERE id = :id")
    suspend fun completeChallenge(id: String, date: String)

    @Query("DELETE FROM daily_challenges WHERE date < :beforeDate")
    suspend fun deleteOldChallenges(beforeDate: String)
}
