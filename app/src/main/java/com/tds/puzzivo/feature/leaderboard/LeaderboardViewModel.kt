package com.tds.puzzivo.feature.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tds.puzzivo.core.auth.AuthManager
import com.tds.puzzivo.core.manager.LeaderboardEntry
import com.tds.puzzivo.core.manager.LeaderboardManager
import com.tds.puzzivo.core.model.GameType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    private val leaderboardManager: LeaderboardManager,
    private val authManager: AuthManager
) : ViewModel() {

    private val _leaderboardEntries = MutableStateFlow<List<LeaderboardEntry>>(emptyList())
    val leaderboardEntries: StateFlow<List<LeaderboardEntry>> = _leaderboardEntries.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val isSignedIn: StateFlow<Boolean> = authManager.currentUser
        .map { it != null && !it.isAnonymous }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun loadLeaderboard(gameType: GameType, scope: LeaderboardScope) {
        viewModelScope.launch {
            _isLoading.value = true
            when (scope) {
                LeaderboardScope.GLOBAL -> {
                    leaderboardManager.getGlobalLeaderboard(
                        gameType = gameType,
                        difficulty = com.tds.puzzivo.core.model.DifficultyLevel.MEDIUM
                    ).collect { entries ->
                        _leaderboardEntries.value = entries
                        _isLoading.value = false
                    }
                }
                LeaderboardScope.FRIENDS -> {
                    leaderboardManager.getFriendsLeaderboard(
                        gameType = gameType,
                        difficulty = com.tds.puzzivo.core.model.DifficultyLevel.MEDIUM,
                        friendIds = emptyList()
                    ).collect { entries ->
                        _leaderboardEntries.value = entries
                        _isLoading.value = false
                    }
                }
            }
        }
    }

    fun signIn(context: android.content.Context) {
        viewModelScope.launch {
            authManager.signInWithGoogle(context)
        }
    }
}
