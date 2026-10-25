package com.tds.puzzivo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.tds.puzzivo.app.AppNavHost
import com.tds.puzzivo.core.design.PuzzivoTheme
import com.tds.puzzivo.core.util.AppUtilities

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        (application as? PuzzivoApplication)?.initializeAds(this)

        // Check for Google Play In-App Updates
        AppUtilities.checkForAppUpdates(this)

        setContent {
            PuzzivoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavHost(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}
