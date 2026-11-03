package com.tds.puzzivo.core.manager

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.tds.puzzivo.core.auth.AuthManager
import com.tds.puzzivo.core.model.DifficultyLevel
import com.tds.puzzivo.core.model.GameType
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LeaderboardManager @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authManager: AuthManager
) {

    private companion object {
        const val TAG = "LeaderboardManager"
        const val LEADERBOARDS_COLLECTION = "leaderboards"
        const val USER_PROFILES_COLLECTION = "user_profiles"
        const val TOP_SCORES_LIMIT = 100L
    }

    suspend fun submitScore(
        gameType: GameType,
        difficulty: DifficultyLevel,
        score: Int,
        mode: String = "classic"
    ): Result<Unit> {
        if (score < 0 || score > 1_000_000) {
            return Result.failure(IllegalArgumentException("Invalid score: $score"))
        }

        val user = authManager.getCurrentUser()
            ?: return Result.failure(Exception("User not signed in"))
        if (user.isAnonymous) {
            return Result.failure(Exception("A connected account is required for leaderboards"))
        }
        val userId = user.uid
        val rawName = authManager.getUserDisplayName()
        val displayName = if (!rawName.isNullOrBlank()) rawName.take(30) else "Anonymous"
        val photoUrl = authManager.getUserPhotoUrl()
        
        return try {
            val leaderboardPath = "${gameType.id}_${difficulty.name.lowercase()}_$mode"
            val timestamp = System.currentTimeMillis()
            
            val scoreData = hashMapOf(
                "userId" to userId,
                "displayName" to displayName,
                "photoUrl" to photoUrl,
                "score" to score,
                "gameType" to gameType.id,
                "difficulty" to difficulty.name,
                "mode" to mode,
                "timestamp" to timestamp
            )
            
            val scoreDocument = firestore.collection(LEADERBOARDS_COLLECTION)
                .document(leaderboardPath)
                .collection("scores")
                .document(userId)
            val previousScore = scoreDocument.get().await().getLong("score")?.toInt()
            if (previousScore == null || score > previousScore) {
                scoreDocument.set(scoreData).await()
                updateUserBestScore(userId, gameType, difficulty, mode, score)
            }

            // Update user's best score
            
            Log.d(TAG, "Score submitted successfully: $score for $gameType")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to submit score", e)
            Result.failure(e)
        }
    }

    private suspend fun updateUserBestScore(
        userId: String,
        gameType: GameType,
        difficulty: DifficultyLevel,
        mode: String,
        score: Int
    ) {
        try {
            val key = "${gameType.id}_${difficulty.name}_$mode"
            firestore.collection(USER_PROFILES_COLLECTION)
                .document(userId)
                .update("bestScores.$key", score)
                .await()
        } catch (e: Exception) {
            // Document might not exist, create it
            createUserProfile(userId)
        }
    }

    private suspend fun createUserProfile(userId: String) {
        try {
            val displayName = authManager.getUserDisplayName() ?: "Player"
            val photoUrl = authManager.getUserPhotoUrl()
            
            val profileData = hashMapOf(
                "userId" to userId,
                "displayName" to displayName,
                "photoUrl" to photoUrl,
                "createdAt" to System.currentTimeMillis(),
                "bestScores" to hashMapOf<String, Int>()
            )
            
            firestore.collection(USER_PROFILES_COLLECTION)
                .document(userId)
                .set(profileData)
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create user profile", e)
        }
    }

    fun getGlobalLeaderboard(
        gameType: GameType,
        difficulty: DifficultyLevel,
        mode: String = "classic",
        limit: Long = TOP_SCORES_LIMIT
    ): Flow<List<LeaderboardEntry>> = callbackFlow {
        val leaderboardPath = "${gameType.id}_${difficulty.name.lowercase()}_$mode"
        
        val listenerRegistration = firestore.collection(LEADERBOARDS_COLLECTION)
            .document(leaderboardPath)
            .collection("scores")
            .orderBy("score", Query.Direction.DESCENDING)
            .limit(limit)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Leaderboard listener error", error)
                    return@addSnapshotListener
                }
                
                val entries = snapshot?.documents?.mapIndexedNotNull { index, doc ->
                    try {
                        LeaderboardEntry(
                            rank = index + 1,
                            userId = doc.getString("userId") ?: "",
                            displayName = doc.getString("displayName") ?: "Unknown",
                            photoUrl = doc.getString("photoUrl"),
                            score = doc.getLong("score")?.toInt() ?: 0,
                            timestamp = doc.getLong("timestamp") ?: 0L
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing leaderboard entry", e)
                        null
                    }
                } ?: emptyList()
                
                trySend(entries)
            }
        
        awaitClose {
            listenerRegistration.remove()
        }
    }

    suspend fun getUserRank(
        gameType: GameType,
        difficulty: DifficultyLevel,
        mode: String = "classic"
    ): Int? {
        val userId = authManager.getUserId() ?: return null
        
        return try {
            val leaderboardPath = "${gameType.id}_${difficulty.name.lowercase()}_$mode"
            
            // Get user's score
            val userDoc = firestore.collection(LEADERBOARDS_COLLECTION)
                .document(leaderboardPath)
                .collection("scores")
                .document(userId)
                .get()
                .await()
            
            val userScore = userDoc.getLong("score")?.toInt() ?: return null
            
            // Count how many scores are higher
            val higherScoresCount = firestore.collection(LEADERBOARDS_COLLECTION)
                .document(leaderboardPath)
                .collection("scores")
                .whereGreaterThan("score", userScore)
                .get()
                .await()
                .size()
            
            higherScoresCount + 1
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get user rank", e)
            null
        }
    }

    fun getFriendsLeaderboard(
        gameType: GameType,
        difficulty: DifficultyLevel,
        friendIds: List<String>,
        mode: String = "classic"
    ): Flow<List<LeaderboardEntry>> = callbackFlow {
        if (friendIds.isEmpty()) {
            trySend(emptyList())
            awaitClose {}
            return@callbackFlow
        }
        
        val leaderboardPath = "${gameType.id}_${difficulty.name.lowercase()}_$mode"
        
        val listenerRegistration = firestore.collection(LEADERBOARDS_COLLECTION)
            .document(leaderboardPath)
            .collection("scores")
            .whereIn("userId", friendIds.take(10)) // Firestore limit
            .orderBy("score", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Friends leaderboard error", error)
                    return@addSnapshotListener
                }
                
                val entries = snapshot?.documents?.mapIndexedNotNull { index, doc ->
                    try {
                        LeaderboardEntry(
                            rank = index + 1,
                            userId = doc.getString("userId") ?: "",
                            displayName = doc.getString("displayName") ?: "Unknown",
                            photoUrl = doc.getString("photoUrl"),
                            score = doc.getLong("score")?.toInt() ?: 0,
                            timestamp = doc.getLong("timestamp") ?: 0L
                        )
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                
                trySend(entries)
            }
        
        awaitClose {
            listenerRegistration.remove()
        }
    }

    suspend fun deleteUserScores(userId: String): Result<Unit> {
        return try {
            // This would require cloud function or batch delete
            // For now, just mark as deleted
            firestore.collection(USER_PROFILES_COLLECTION)
                .document(userId)
                .delete()
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete user scores", e)
            Result.failure(e)
        }
    }
}

data class LeaderboardEntry(
    val rank: Int,
    val userId: String,
    val displayName: String,
    val photoUrl: String?,
    val score: Int,
    val timestamp: Long
)
