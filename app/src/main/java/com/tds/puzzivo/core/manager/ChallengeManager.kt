package com.tds.puzzivo.core.manager

import com.tds.puzzivo.R
import com.tds.puzzivo.core.database.ChallengeDao
import com.tds.puzzivo.core.model.DailyChallenge
import com.tds.puzzivo.core.model.GameType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class ChallengeManager @Inject constructor(
    private val challengeDao: ChallengeDao,
    private val progressionManager: ProgressionManager
) {
    @Volatile
    private var activeChallengeId: String? = null

    suspend fun getTodayChallenge(): DailyChallenge {
        val today = LocalDate.now().toString()
        val existing = challengeDao.getChallengeForDate(today).first()
        
        return existing ?: generateAndSaveDailyChallenge(today)
    }

    fun observeTodayChallenge(): Flow<DailyChallenge> = kotlinx.coroutines.flow.flow {
        val today = LocalDate.now().toString()
        getTodayChallenge()
        emitAll(challengeDao.getChallengeForDate(today).filterNotNull())
    }

    suspend fun completeIfMatchingToday(gameType: GameType): Boolean {
        val challenge = getTodayChallenge()
        if (activeChallengeId != challenge.id || challenge.gameType != gameType || challenge.isCompleted) return false
        challengeDao.updateProgress(challenge.id, challenge.requirement)
        completeChallenge(challenge)
        activeChallengeId = null
        return true
    }

    fun startTodayChallenge(challenge: DailyChallenge) {
        if (!challenge.isCompleted && challenge.date == LocalDate.now().toString()) {
            activeChallengeId = challenge.id
        }
    }

    fun getChallengeForDate(date: String): Flow<DailyChallenge?> =
        challengeDao.getChallengeForDate(date)

    val completedChallenges: Flow<List<DailyChallenge>> =
        challengeDao.getCompletedChallenges()

    suspend fun getCompletedCount(): Int = challengeDao.getCompletedCount()

    private suspend fun generateAndSaveDailyChallenge(date: String): DailyChallenge {
        val dateValue = LocalDate.parse(date)
        val seed = dateValue.toEpochDay()
        val random = Random(seed)
        
        // Select random game
        val gameType = GameType.entries[random.nextInt(GameType.entries.size)]
        
        // Generate challenge based on game
        val challenge = generateChallengeForGame(gameType, date, random)
        
        challengeDao.insertChallenge(challenge)
        return challenge
    }

    private fun generateChallengeForGame(
        gameType: GameType,
        date: String,
        random: Random
    ): DailyChallenge {
        val challengeTypes = when (gameType) {
            GameType.NUMBER_MERGE -> listOf(
                ChallengeData("score_2000", R.string.challenge_nm_score_2000, 2000, 50, 30),
                ChallengeData("tile_512", R.string.challenge_nm_tile_512, 512, 40, 25),
                ChallengeData("moves_100", R.string.challenge_nm_moves_100, 100, 30, 20)
            )
            GameType.MEMORY_MATCH -> listOf(
                ChallengeData("moves_20", R.string.challenge_mm_moves_20, 20, 40, 25),
                ChallengeData("time_60", R.string.challenge_mm_time_60, 60, 45, 30),
                ChallengeData("perfect", R.string.challenge_mm_perfect, 1, 60, 40)
            )
            GameType.QUICK_MATH -> listOf(
                ChallengeData("score_20", R.string.challenge_qm_score_20, 20, 50, 30),
                ChallengeData("correct_15", R.string.challenge_qm_correct_15, 15, 40, 25),
                ChallengeData("no_mistakes", R.string.challenge_qm_no_mistakes, 1, 70, 45)
            )
            else -> listOf(
                ChallengeData("win_1", R.string.challenge_generic_win, 1, 30, 20),
                ChallengeData("play_3", R.string.challenge_generic_play_3, 3, 40, 25)
            )
        }
        
        val selectedChallenge = challengeTypes[random.nextInt(challengeTypes.size)]
        
        return DailyChallenge(
            id = "daily_${date}_${gameType.id}",
            date = date,
            gameType = gameType,
            challengeTypeId = selectedChallenge.typeId,
            descriptionRes = selectedChallenge.descriptionRes,
            requirement = selectedChallenge.requirement,
            rewardCoins = selectedChallenge.coins,
            rewardXP = selectedChallenge.xp
        )
    }

    suspend fun updateChallengeProgress(challengeId: String, progress: Int) {
        challengeDao.updateProgress(challengeId, progress)
        
        val challenge = challengeDao.getChallengeForDate(
            challengeId.substringAfter("daily_").substringBefore("_")
        ).first()
        
        if (challenge != null && !challenge.isCompleted && progress >= challenge.requirement) {
            completeChallenge(challenge)
        }
    }

    private suspend fun completeChallenge(challenge: DailyChallenge) {
        val today = LocalDate.now().toString()
        challengeDao.completeChallenge(challenge.id, today)
        
        // Award rewards
        progressionManager.addCoins(challenge.rewardCoins)
        progressionManager.addXP(challenge.rewardXP)
    }

    suspend fun cleanupOldChallenges() {
        val thirtyDaysAgo = LocalDate.now().minusDays(30).toString()
        challengeDao.deleteOldChallenges(thirtyDaysAgo)
    }
}

private data class ChallengeData(
    val typeId: String,
    val descriptionRes: Int,
    val requirement: Int,
    val coins: Int,
    val xp: Int
)
