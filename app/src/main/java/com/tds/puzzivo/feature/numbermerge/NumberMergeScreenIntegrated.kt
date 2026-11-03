package com.tds.puzzivo.feature.numbermerge

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tds.puzzivo.core.design.DifficultySelector
import com.tds.puzzivo.core.game.AchievementMonitor
import com.tds.puzzivo.core.game.LevelUpMonitor
import com.tds.puzzivo.core.game.OfflineMonitor

/**
 * Example of NumberMergeScreen integrated with universal features
 * This shows how to add the monitoring composables
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NumberMergeScreenIntegrated(
    onBackClick: () -> Unit,
    viewModel: NumberMergeViewModelIntegrated = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val selectedDifficulty by viewModel.selectedDifficulty.collectAsState()
    val showDifficultyDialog by viewModel.showDifficultyDialog.collectAsState()

    // Monitor for offline state
    OfflineMonitor(isOnline = isOnline)
    
    // Monitor for achievement unlocks
    AchievementMonitor(achievementFlow = viewModel.achievementUnlocked)
    
    // Monitor for level ups
    LevelUpMonitor(levelUpFlow = viewModel.levelUp)

    // Difficulty selection dialog
    if (showDifficultyDialog) {
        AlertDialog(
            onDismissRequest = { /* Don't allow dismiss */ },
            title = { Text("Select Difficulty") },
            text = {
                Column {
                    Text(
                        text = "Choose your challenge level",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    DifficultySelector(
                        selectedDifficulty = selectedDifficulty,
                        onDifficultySelected = { difficulty ->
                            viewModel.setDifficulty(difficulty)
                        }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.onDifficultySelected() }
                ) {
                    Text("Start Game")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Number Merge") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    // Show current difficulty
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = selectedDifficulty.name,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            )
        }
    ) { padding ->
        // Your existing game UI here
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Score display
            Text(
                text = "Score: ${uiState.score}",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(16.dp)
            )
            
            // Game grid, controls, etc.
            // ... rest of your existing UI
            
            Text(
                text = "Best: ${uiState.bestScore}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}
