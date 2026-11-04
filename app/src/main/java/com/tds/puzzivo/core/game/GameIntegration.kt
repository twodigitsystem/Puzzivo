package com.tds.puzzivo.core.game

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tds.puzzivo.core.manager.*
import com.tds.puzzivo.core.model.*
import com.tds.puzzivo.core.network.NetworkManager
import kotlinx.coroutines.flow.*
import java.util.UUID

/**
 * Base ViewModel functionality for game integration
 * Handles common features: offline detection, difficulty, stats, achievements, rewards
 */
abstract class BaseGameViewModel(
    protected val gameType: GameType,
    protected val networkManager: NetworkManager,
    protected val statisticsManager: StatisticsManager,
    protected val achievementManager: AchievementManager,
    protected val progressionManager: ProgressionManager
) : ViewModel() {

    // Network state
    val isOnline: StateFlow<Boolean> = networkManager.isOnline
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    // Selected difficulty
    private val _selectedDifficulty = MutableStateFlow(DifficultyLevel.MEDIUM)
    val selectedDifficulty: StateFlow<DifficultyLevel> = _selectedDifficulty.asStateFlow()

    // Achievement unlock events
    private val _achievementUnlocked = MutableSharedFlow<Achievement>()
    val achievementUnlocked: SharedFlow<Achievement> = _achievementUnlocked.asSharedFlow()

    // Level up events
    private val _levelUp = MutableSharedFlow<Int>()
    val levelUp: SharedFlow<Int> = _levelUp.asSharedFlow()

    fun setDifficulty(difficulty: DifficultyLevel) {
        _selectedDifficulty.value = difficulty
    }

    /**
     * Call this when a game is completed
     */
    protected suspend fun recordGameResult(
        won: Boolean,
        score: Long,
        timeInSeconds: Long,
        movesOrAttempts: Int = 0,
        hintsUsed: Int = 0
    ) {
        val difficulty = _selectedDifficulty.value
        val finalScore = (score * difficulty.scoreMultiplier).toInt()
        val durationSeconds = timeInSeconds.toInt()

        // Record statistics session
        statisticsManager.recordGameSession(
            gameType = gameType,
            score = finalScore,
            durationSeconds = durationSeconds,
            won = won,
            difficulty = difficulty,
            hintsUsed = hintsUsed,
            moves = movesOrAttempts,
            completionId = UUID.randomUUID().toString()
        )

        // Award XP and coins if won
        if (won) {
            val xpReward = calculateXPReward(score, difficulty)
            val coinReward = calculateCoinReward(score, difficulty)

            val levelUpResult = progressionManager.addXP(xpReward.toInt())
            progressionManager.addCoins(coinReward.toInt())

            if (levelUpResult.leveledUp) {
                _levelUp.emit(levelUpResult.newLevel)
            }

            // Check achievements
            checkAchievements(finalScore, durationSeconds, won, difficulty, hintsUsed)
        }
    }

    private fun calculateXPReward(score: Long, difficulty: DifficultyLevel): Long {
        val baseXP = when {
            score < 100 -> 10L
            score < 500 -> 25L
            score < 1000 -> 50L
            score < 5000 -> 100L
            else -> 200L
        }
        return (baseXP * difficulty.scoreMultiplier).toLong()
    }

    private fun calculateCoinReward(score: Long, difficulty: DifficultyLevel): Long {
        val baseCoins = when {
            score < 100 -> 5L
            score < 500 -> 10L
            score < 1000 -> 25L
            score < 5000 -> 50L
            else -> 100L
        }
        return (baseCoins * difficulty.scoreMultiplier).toLong()
    }

    private suspend fun checkAchievements(
        score: Int,
        durationSeconds: Int,
        won: Boolean,
        difficulty: DifficultyLevel,
        hintsUsed: Int
    ) {
        val stats = statisticsManager.getStatistics(gameType).first() ?: return

        // First win
        if (stats.totalWins == 1) {
            unlockAchievement("first_win_${gameType.name.lowercase()}")
        }

        // 10 wins
        if (stats.totalWins == 10) {
            unlockAchievement("10_wins_${gameType.name.lowercase()}")
        }

        // 50 wins
        if (stats.totalWins == 50) {
            unlockAchievement("50_wins_${gameType.name.lowercase()}")
        }

        // 100 wins
        if (stats.totalWins == 100) {
            unlockAchievement("100_wins_${gameType.name.lowercase()}")
        }

        // Perfect game (no hints, high score)
        if (hintsUsed == 0 && score > 1000) {
            unlockAchievement("perfect_${gameType.name.lowercase()}")
        }

        // Speed demon (very fast completion)
        if (durationSeconds < 60 && won) {
            unlockAchievement("speed_demon_${gameType.name.lowercase()}")
        }

        // Win streak
        if (stats.currentWinStreak == 5) {
            unlockAchievement("win_streak_5")
        }
        if (stats.currentWinStreak == 10) {
            unlockAchievement("win_streak_10")
        }

        // Expert difficulty wins
        if (difficulty == DifficultyLevel.EXPERT && won) {
            if (stats.totalWins >= 10) {
                unlockAchievement("expert_master_${gameType.name.lowercase()}")
            }
        }
    }

    private suspend fun unlockAchievement(achievementId: String) {
        val unlocked = achievementManager.unlockAchievement(achievementId)
        if (unlocked != null) {
            _achievementUnlocked.emit(unlocked)
        }
    }

    /**
     * Track progress for incremental achievements
     */
    protected suspend fun trackAchievementProgress(achievementId: String, progress: Int = 1) {
        achievementManager.updateAchievementProgress(achievementId, progress)
    }
}

/**
 * Composable to show offline dialog when network is lost
 */
@Composable
fun OfflineMonitor(
    isOnline: Boolean,
    onDismiss: () -> Unit = {}
) {
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(isOnline) {
        if (!isOnline) {
            showDialog = true
        }
    }

    if (showDialog && !isOnline) {
        com.tds.puzzivo.core.design.OfflineDialog(
            onDismiss = {
                showDialog = false
                onDismiss()
            }
        )
    }
}

/**
 * Composable to show achievement unlock dialog
 */
@Composable
fun AchievementMonitor(
    achievementFlow: SharedFlow<Achievement>
) {
    var currentAchievement by remember { mutableStateOf<Achievement?>(null) }

    LaunchedEffect(Unit) {
        achievementFlow.collect { achievement ->
            currentAchievement = achievement
        }
    }

    currentAchievement?.let { achievement ->
        com.tds.puzzivo.core.design.AchievementUnlockedDialog(
            achievement = achievement,
            onDismiss = { currentAchievement = null }
        )
    }
}

/**
 * Composable to show level up dialog
 */
@Composable
fun LevelUpMonitor(
    levelUpFlow: SharedFlow<Int>
) {
    var newLevel by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) {
        levelUpFlow.collect { level ->
            newLevel = level
        }
    }

    newLevel?.let { level ->
        com.tds.puzzivo.core.design.LevelUpDialog(
            newLevel = level,
            onDismiss = { newLevel = null }
        )
    }
}
