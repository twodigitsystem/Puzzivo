package com.tds.puzzivo

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.tds.puzzivo.app.AppNavHost
import com.tds.puzzivo.core.design.PuzzivoTheme
import com.tds.puzzivo.core.init.AppInitializer
import com.tds.puzzivo.core.manager.ThemeManager
import com.tds.puzzivo.core.model.GameTheme
import com.tds.puzzivo.core.network.NetworkManager
import com.tds.puzzivo.core.util.AppUtilities
import com.tds.puzzivo.core.game.OfflineMonitor
import androidx.compose.foundation.layout.Box
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var appInitializer: AppInitializer

    @Inject
    lateinit var themeManager: ThemeManager

    @Inject
    lateinit var networkManager: NetworkManager

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )

        (application as? PuzzivoApplication)?.initializeAds(this)

        // Initialize app features
        appInitializer.initialize()

        // Check for Google Play In-App Updates
        AppUtilities.checkForAppUpdates(this)

        setContent {
            val selectedTheme by themeManager.selectedTheme.collectAsState(initial = GameTheme.NAVY_DARK)
            val isOnline by networkManager.isOnline.collectAsState(initial = true)
            PuzzivoTheme(gameTheme = selectedTheme) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        AppNavHost(modifier = Modifier.padding(innerPadding))
                    }
                    OfflineMonitor(isOnline = isOnline)
                }
            }
        }
    }
}
