package com.tds.puzzivo.core.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.tds.puzzivo.core.model.GameType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.time.Clock
import java.time.LocalDate

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "puzzivo_prefs",
    corruptionHandler = ReplaceFileCorruptionHandler { androidx.datastore.preferences.core.emptyPreferences() }
)

data class UserPreferences(
    val numberMergeBestScore: Int = 0,
    val memoryMatchBestMoves: Int = 0,
    val memoryMatchBestTime: Int = 0,
    val quickMathBestScore: Int = 0,
    val wordSearchCurrentIndex: Int = 0,
    val wordSearchCompletedIndex: Int = 0,
    val wordSearchSolvedIds: Set<String> = emptySet(),
    val seenInstructionIds: Set<String> = emptySet(),
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val todayPickLastDate: String = "",
    val todayPickCompleted: Boolean = false,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val lastStreakDate: String = "",
    val totalGamesPlayed: Int = 0
)

class PreferencesRepository(
    private val context: Context,
    private val clock: Clock = Clock.systemDefaultZone()
) {

    private object Keys {
        val NUMBER_MERGE_BEST_SCORE = intPreferencesKey("number_merge_best_score")
        val MEMORY_MATCH_BEST_MOVES = intPreferencesKey("memory_match_best_moves")
        val MEMORY_MATCH_BEST_TIME = intPreferencesKey("memory_match_best_time")
        val QUICK_MATH_BEST_SCORE = intPreferencesKey("quick_math_best_score")
        val WORD_SEARCH_CURRENT_INDEX = intPreferencesKey("word_search_current_index")
        val WORD_SEARCH_COMPLETED_INDEX = intPreferencesKey("word_search_completed_index")
        val WORD_SEARCH_SOLVED_IDS = stringSetPreferencesKey("word_search_solved_ids")
        val SEEN_INSTRUCTION_IDS = stringSetPreferencesKey("seen_instruction_ids")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val TODAY_PICK_LAST_DATE = stringPreferencesKey("today_pick_last_date")
        val TODAY_PICK_COMPLETED = booleanPreferencesKey("today_pick_completed")
        val CURRENT_STREAK = intPreferencesKey("current_streak")
        val BEST_STREAK = intPreferencesKey("best_streak")
        val LAST_STREAK_DATE = stringPreferencesKey("last_streak_date")
        val TOTAL_GAMES_PLAYED = intPreferencesKey("total_games_played")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .catch { error ->
            if (error is IOException) emit(androidx.datastore.preferences.core.emptyPreferences())
            else throw error
        }
        .map { prefs ->
        val today = LocalDate.now(clock).toString()
        val yesterday = LocalDate.now(clock).minusDays(1).toString()
        val lastPickDate = prefs[Keys.TODAY_PICK_LAST_DATE] ?: ""
        val lastStreakDate = prefs[Keys.LAST_STREAK_DATE] ?: ""
        val rawCurrentStreak = prefs[Keys.CURRENT_STREAK] ?: 0
        val activeStreak = if (lastStreakDate == today || lastStreakDate == yesterday) rawCurrentStreak else 0

        UserPreferences(
            numberMergeBestScore = (prefs[Keys.NUMBER_MERGE_BEST_SCORE] ?: 0).coerceAtLeast(0),
            memoryMatchBestMoves = (prefs[Keys.MEMORY_MATCH_BEST_MOVES] ?: 0).coerceAtLeast(0),
            memoryMatchBestTime = (prefs[Keys.MEMORY_MATCH_BEST_TIME] ?: 0).coerceAtLeast(0),
            quickMathBestScore = (prefs[Keys.QUICK_MATH_BEST_SCORE] ?: 0).coerceAtLeast(0),
            wordSearchCurrentIndex = (prefs[Keys.WORD_SEARCH_CURRENT_INDEX] ?: 0).coerceAtLeast(0),
            wordSearchCompletedIndex = (prefs[Keys.WORD_SEARCH_COMPLETED_INDEX] ?: 0).coerceAtLeast(0),
            wordSearchSolvedIds = prefs[Keys.WORD_SEARCH_SOLVED_IDS] ?: emptySet(),
            seenInstructionIds = prefs[Keys.SEEN_INSTRUCTION_IDS] ?: emptySet(),
            soundEnabled = prefs[Keys.SOUND_ENABLED] ?: true,
            hapticsEnabled = prefs[Keys.HAPTICS_ENABLED] ?: true,
            todayPickLastDate = prefs[Keys.TODAY_PICK_LAST_DATE] ?: "",
            todayPickCompleted = (prefs[Keys.TODAY_PICK_COMPLETED] ?: false) && lastPickDate == today,
            currentStreak = activeStreak.coerceAtLeast(0),
            bestStreak = (prefs[Keys.BEST_STREAK] ?: 0).coerceAtLeast(0),
            lastStreakDate = lastStreakDate,
            totalGamesPlayed = (prefs[Keys.TOTAL_GAMES_PLAYED] ?: 0).coerceAtLeast(0)
        )
    }

    suspend fun saveNumberMergeBestScore(score: Int) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.NUMBER_MERGE_BEST_SCORE] ?: 0
            if (score > current) {
                prefs[Keys.NUMBER_MERGE_BEST_SCORE] = score
            }
        }
    }

    suspend fun saveMemoryMatchBest(moves: Int, timeInSeconds: Int) {
        context.dataStore.edit { prefs ->
            val currentMoves = prefs[Keys.MEMORY_MATCH_BEST_MOVES] ?: 0
            val currentTime = prefs[Keys.MEMORY_MATCH_BEST_TIME] ?: 0

            val isBetter = if (currentMoves == 0) true
            else if (moves < currentMoves) true
            else if (moves == currentMoves && timeInSeconds < currentTime) true
            else false

            if (isBetter) {
                prefs[Keys.MEMORY_MATCH_BEST_MOVES] = moves
                prefs[Keys.MEMORY_MATCH_BEST_TIME] = timeInSeconds
            }
        }
    }

    suspend fun saveQuickMathBestScore(score: Int) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.QUICK_MATH_BEST_SCORE] ?: 0
            if (score > current) {
                prefs[Keys.QUICK_MATH_BEST_SCORE] = score
            }
        }
    }

    suspend fun saveWordSearchCompleted(puzzleId: String, nextIndex: Int) {
        context.dataStore.edit { prefs ->
            val set = (prefs[Keys.WORD_SEARCH_SOLVED_IDS] ?: emptySet()).toMutableSet()
            set.add(puzzleId)
            prefs[Keys.WORD_SEARCH_SOLVED_IDS] = set

            val currentIndex = prefs[Keys.WORD_SEARCH_COMPLETED_INDEX] ?: 0
            if (nextIndex > currentIndex) {
                prefs[Keys.WORD_SEARCH_COMPLETED_INDEX] = nextIndex.coerceAtLeast(0)
            }
            prefs[Keys.WORD_SEARCH_CURRENT_INDEX] = nextIndex.coerceAtLeast(0)
        }
    }

    suspend fun saveWordSearchCurrentIndex(index: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.WORD_SEARCH_CURRENT_INDEX] = index.coerceAtLeast(0)
        }
    }

    suspend fun markInstructionsSeen(gameType: GameType) {
        context.dataStore.edit { prefs ->
            val seen = (prefs[Keys.SEEN_INSTRUCTION_IDS] ?: emptySet()).toMutableSet()
            seen += gameType.id
            prefs[Keys.SEEN_INSTRUCTION_IDS] = seen
        }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SOUND_ENABLED] = enabled
        }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.HAPTICS_ENABLED] = enabled
        }
    }

    suspend fun recordGameCompleted(playedGameType: GameType) {
        val today = LocalDate.now(clock).toString()
        val yesterday = LocalDate.now(clock).minusDays(1).toString()

        context.dataStore.edit { prefs ->
            // Increment total games
            val total = prefs[Keys.TOTAL_GAMES_PLAYED] ?: 0
            prefs[Keys.TOTAL_GAMES_PLAYED] = (total + 1).coerceAtLeast(0)

            // Only completing the selected daily game completes Today's Pick and advances the streak.
            val todayPickGame = getTodayPickGameType(LocalDate.now(clock))
            if (playedGameType != todayPickGame) return@edit

            prefs[Keys.TODAY_PICK_LAST_DATE] = today
            prefs[Keys.TODAY_PICK_COMPLETED] = true

            // Streak calculation
            val lastStreakDate = prefs[Keys.LAST_STREAK_DATE] ?: ""
            var currentStreak = prefs[Keys.CURRENT_STREAK] ?: 0
            var bestStreak = prefs[Keys.BEST_STREAK] ?: 0

            if (lastStreakDate == today) {
                // Already recorded today's streak
            } else if (lastStreakDate == yesterday) {
                currentStreak += 1
                prefs[Keys.CURRENT_STREAK] = currentStreak
                prefs[Keys.LAST_STREAK_DATE] = today
                if (currentStreak > bestStreak) {
                    prefs[Keys.BEST_STREAK] = currentStreak
                }
            } else {
                // Streak broken or brand new
                currentStreak = 1
                prefs[Keys.CURRENT_STREAK] = currentStreak
                prefs[Keys.LAST_STREAK_DATE] = today
                if (currentStreak > bestStreak) {
                    prefs[Keys.BEST_STREAK] = currentStreak
                }
            }
        }
    }

    suspend fun clearAllProgress() {
        context.dataStore.edit { prefs ->
            val sound = prefs[Keys.SOUND_ENABLED] ?: true
            val haptics = prefs[Keys.HAPTICS_ENABLED] ?: true
            prefs.clear()
            prefs[Keys.SOUND_ENABLED] = sound
            prefs[Keys.HAPTICS_ENABLED] = haptics
        }
    }

    companion object {
        fun getTodayPickGameType(date: LocalDate = LocalDate.now()): GameType {
            val games = GameType.entries.toTypedArray()
            return games[Math.floorMod(date.toEpochDay(), games.size.toLong()).toInt()]
        }
    }
}
