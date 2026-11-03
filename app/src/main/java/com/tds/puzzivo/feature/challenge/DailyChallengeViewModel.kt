package com.tds.puzzivo.feature.challenge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tds.puzzivo.core.manager.ChallengeManager
import com.tds.puzzivo.core.model.DailyChallenge
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class DailyChallengeViewModel @Inject constructor(
    private val challengeManager: ChallengeManager
) : ViewModel() {

    val todayChallenge: StateFlow<DailyChallenge?> = challengeManager.observeTodayChallenge()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val recentChallenges: StateFlow<List<DailyChallenge>> = challengeManager.completedChallenges
        .map { it.take(7) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _timeRemaining = MutableStateFlow("")
    val timeRemaining: StateFlow<String> = _timeRemaining.asStateFlow()

    init {
        // Update countdown timer
        viewModelScope.launch {
            while (true) {
                _timeRemaining.value = calculateTimeRemaining()
                delay(60000) // Update every minute
            }
        }
    }

    fun startChallenge(challenge: DailyChallenge) {
        challengeManager.startTodayChallenge(challenge)
    }

    private fun calculateTimeRemaining(): String {
        val now = LocalDateTime.now()
        val midnight = LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.MIDNIGHT)
        val duration = Duration.between(now, midnight)
        
        val hours = duration.toHours()
        val minutes = duration.toMinutes() % 60
        
        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m"
            else -> "Less than 1 minute"
        }
    }
}
