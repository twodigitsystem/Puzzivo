package com.tds.puzzivo.core.manager

import com.tds.puzzivo.core.database.StatisticsDao
import com.tds.puzzivo.core.database.PuzzivoDatabase
import com.tds.puzzivo.core.model.DifficultyLevel
import com.tds.puzzivo.core.model.GameSession
import com.tds.puzzivo.core.model.GameStatistics
import com.tds.puzzivo.core.model.GameType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import androidx.room.withTransaction
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatisticsManager @Inject constructor(
    private val database: PuzzivoDatabase,
    private val statisticsDao: StatisticsDao
) {

    fun getStatistics(gameType: GameType): Flow<GameStatistics?> =
        statisticsDao.getStatistics(gameType)

    val allStatistics: Flow<List<GameStatistics>> = statisticsDao.getAllStatistics()

    fun getRecentSessions(gameType: GameType): Flow<List<GameSession>> =
        statisticsDao.getRecentSessions(gameType)

    fun getTopScores(gameType: GameType, daysBack: Int = 30): Flow<List<GameSession>> {
        val fromDate = LocalDate.now().minusDays(daysBack.toLong()).toString()
        return statisticsDao.getTopScores(gameType, fromDate)
    }

    suspend fun recordGameSession(
        gameType: GameType,
        score: Int,
        durationSeconds: Int,
        won: Boolean,
        difficulty: DifficultyLevel,
        mode: String = "classic",
        hintsUsed: Int = 0,
        moves: Int = 0,
        completionId: String = UUID.randomUUID().toString()
    ): Boolean {
        return database.withTransaction {
            val today = LocalDate.now().toString()
            val session = GameSession(
                completionId = completionId,
                gameType = gameType,
                playedDate = today,
                score = score,
                durationSeconds = durationSeconds,
                won = won,
                difficulty = difficulty,
                mode = mode,
                hintsUsed = hintsUsed,
                moves = moves
            )
            val insertedId = statisticsDao.insertSession(session)
            if (insertedId == -1L) {
                false
            } else {
                updateStatistics(gameType, score, durationSeconds, won, hintsUsed, today)
                true
            }
        }
    }

    private suspend fun updateStatistics(
        gameType: GameType,
        score: Int,
        durationSeconds: Int,
        won: Boolean,
        hintsUsed: Int,
        today: String
    ) {
        val current = statisticsDao.getStatistics(gameType).first()
        
        val updated = if (current == null) {
            GameStatistics(
                gameType = gameType,
                totalPlays = 1,
                totalWins = if (won) 1 else 0,
                totalTimePlayedSeconds = durationSeconds.toLong(),
                averageScore = score.toFloat(),
                highestScore = score,
                lowestScore = score,
                currentWinStreak = if (won) 1 else 0,
                longestWinStreak = if (won) 1 else 0,
                perfectGames = if (hintsUsed == 0 && won) 1 else 0,
                hintsUsed = hintsUsed,
                firstPlayedDate = today,
                lastPlayedDate = today
            )
        } else {
            val newTotalPlays = current.totalPlays + 1
            val newTotalWins = current.totalWins + if (won) 1 else 0
            val newTotalTime = current.totalTimePlayedSeconds + durationSeconds
            val newAverage = ((current.averageScore * current.totalPlays) + score) / newTotalPlays
            val newWinStreak = if (won) current.currentWinStreak + 1 else 0
            val newLongestStreak = maxOf(current.longestWinStreak, newWinStreak)
            val newPerfectGames = current.perfectGames + if (hintsUsed == 0 && won) 1 else 0
            
            current.copy(
                totalPlays = newTotalPlays,
                totalWins = newTotalWins,
                totalTimePlayedSeconds = newTotalTime,
                averageScore = newAverage,
                highestScore = maxOf(current.highestScore, score),
                lowestScore = if (current.lowestScore == 0) score else minOf(current.lowestScore, score),
                currentWinStreak = newWinStreak,
                longestWinStreak = newLongestStreak,
                perfectGames = newPerfectGames,
                hintsUsed = current.hintsUsed + hintsUsed,
                lastPlayedDate = today
            )
        }
        
        statisticsDao.insertStatistics(updated)
    }

    suspend fun resetStatisticsForGame(gameType: GameType) {
        statisticsDao.deleteSessionsForGame(gameType)
        val empty = GameStatistics(gameType = gameType)
        statisticsDao.insertStatistics(empty)
    }

    suspend fun clearAllStatistics() {
        statisticsDao.deleteAllStatistics()
    }
}
