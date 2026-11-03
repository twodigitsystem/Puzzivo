package com.tds.puzzivo.feature.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.tds.puzzivo.core.auth.AuthManager
import com.tds.puzzivo.core.manager.AchievementManager
import com.tds.puzzivo.core.manager.ProgressionManager
import com.tds.puzzivo.core.manager.StatisticsManager
import com.tds.puzzivo.core.manager.ThemeManager
import com.tds.puzzivo.core.model.GameTheme
import com.tds.puzzivo.core.model.PlayerProgress
import com.tds.puzzivo.core.storage.PreferencesRepository
import com.tds.puzzivo.core.storage.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val user: FirebaseUser? = null,
    val playerProgress: PlayerProgress = PlayerProgress(),
    val userPreferences: UserPreferences = UserPreferences(),
    val unlockedAchievementsCount: Int = 0,
    val totalAchievementsCount: Int = 0,
    val totalGamesPlayed: Int = 0,
    val totalWins: Int = 0,
    val selectedTheme: GameTheme = GameTheme.NAVY_DARK
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authManager: AuthManager,
    private val progressionManager: ProgressionManager,
    private val preferencesRepository: PreferencesRepository,
    private val achievementManager: AchievementManager,
    private val statisticsManager: StatisticsManager,
    private val themeManager: ThemeManager
) : ViewModel() {

    val uiState: StateFlow<ProfileUiState> = combine(
        authManager.currentUser,
        progressionManager.playerProgress,
        preferencesRepository.userPreferencesFlow,
        achievementManager.allAchievements,
        achievementManager.unlockedAchievements
    ) { user, progress, prefs, allAchievements, unlockedAchievements ->
        ProfileUiState(
            user = user,
            playerProgress = progress,
            userPreferences = prefs,
            unlockedAchievementsCount = unlockedAchievements.size,
            totalAchievementsCount = allAchievements.size
        )
    }.combine(statisticsManager.allStatistics) { state, allStats ->
        state.copy(
            totalGamesPlayed = allStats.sumOf { it.totalPlays },
            totalWins = allStats.sumOf { it.totalWins }
        )
    }.combine(themeManager.selectedTheme) { state, selectedTheme ->
        state.copy(selectedTheme = selectedTheme)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProfileUiState()
    )

    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            authManager.signInWithGoogle(context)
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authManager.signOut()
        }
    }
}
