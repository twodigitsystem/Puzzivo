package com.tds.puzzivo.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tds.puzzivo.core.model.GameSession
import com.tds.puzzivo.core.model.GameStatistics
import com.tds.puzzivo.core.model.GameType
import kotlinx.coroutines.flow.Flow

@Dao
interface StatisticsDao {
    @Query("SELECT * FROM game_statistics WHERE gameType = :gameType")
    fun getStatistics(gameType: GameType): Flow<GameStatistics?>

    @Query("SELECT * FROM game_statistics")
    fun getAllStatistics(): Flow<List<GameStatistics>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStatistics(statistics: GameStatistics)

    @Update
    suspend fun updateStatistics(statistics: GameStatistics)

    // Game Sessions
    @Query("SELECT * FROM game_sessions WHERE gameType = :gameType ORDER BY playedDate DESC LIMIT 100")
    fun getRecentSessions(gameType: GameType): Flow<List<GameSession>>

    @Query("SELECT * FROM game_sessions WHERE gameType = :gameType AND playedDate >= :fromDate ORDER BY score DESC LIMIT 10")
    fun getTopScores(gameType: GameType, fromDate: String): Flow<List<GameSession>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSession(session: GameSession): Long

    @Query("DELETE FROM game_sessions WHERE gameType = :gameType")
    suspend fun deleteSessionsForGame(gameType: GameType)

    @Query("DELETE FROM game_statistics")
    suspend fun deleteAllStatistics()
}
