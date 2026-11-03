package com.tds.puzzivo.core.init

import com.tds.puzzivo.core.auth.AuthManager
import com.tds.puzzivo.core.manager.AchievementManager
import com.tds.puzzivo.core.manager.ProgressionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Initializes the app on first launch
 */
@Singleton
class AppInitializer @Inject constructor(
    private val authManager: AuthManager,
    private val achievementManager: AchievementManager,
    private val progressionManager: ProgressionManager
) {
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    fun initialize() {
        scope.launch {
            // Sign in anonymously if no user
            authManager.ensureSignedIn()
            
            // Initialize default achievements
            achievementManager.initializeDefaultAchievements()
            
            // Initialize player progress
            progressionManager.initializePlayerProgress()
        }
    }
}
