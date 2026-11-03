package com.tds.puzzivo.core.manager

import com.tds.puzzivo.core.database.StatisticsDao
import com.tds.puzzivo.core.model.DifficultyLevel
import com.tds.puzzivo.core.model.GameSession
import com.tds.puzzivo.core.model.GameStatistics
import com.tds.puzzivo.core.model.GameType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatisticsManager @Inject constructor(
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
        moves: Int = 0
    ) {
        val today = LocalDate.now().toString()
        
        // Record session
        val session = GameSession(
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
        statisticsDao.insertSession(session)
        
        // Update statistics
        updateStatistics(gameType, score, durationSeconds, won, hintsUsed, today)
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
