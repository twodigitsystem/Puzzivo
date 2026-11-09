package com.tds.puzzivo.feature.lightsout

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tds.puzzivo.R
import com.tds.puzzivo.core.design.AccentGreen
import com.tds.puzzivo.core.design.AccentYellow
import com.tds.puzzivo.core.design.CardBorderColor
import com.tds.puzzivo.core.design.ConfettiParticleOverlay
import com.tds.puzzivo.core.design.InstructionDialog
import com.tds.puzzivo.core.design.NavyBackground
import com.tds.puzzivo.core.design.NavySurface
import com.tds.puzzivo.core.design.NavySurfaceVariant
import com.tds.puzzivo.core.design.PrimaryButton
import com.tds.puzzivo.core.design.PuzzivoCard
import com.tds.puzzivo.core.design.PuzzivoTopBar
import com.tds.puzzivo.core.design.ResultDialog
import com.tds.puzzivo.core.design.TextPrimary
import com.tds.puzzivo.core.design.TextSecondary
import com.tds.puzzivo.core.model.DifficultyLevel

@Composable
fun LightsOutScreen(
    onBackClick: () -> Unit,
    viewModel: LightsOutViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val shouldShowInstructions by viewModel.shouldShowInstructions.collectAsState()
    var showHelpDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Surface(color = NavyBackground, modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                PuzzivoTopBar(
                    title = stringResource(R.string.lights_out_title),
                    onBackClick = onBackClick,
                    onActionClick = { showHelpDialog = true },
                    actionContentDescription = stringResource(R.string.action_help)
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Difficulty Chips
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        listOf(DifficultyLevel.EASY, DifficultyLevel.MEDIUM, DifficultyLevel.HARD).forEach { difficulty ->
                            FilterChip(
                                selected = uiState.difficulty == difficulty,
                                onClick = { viewModel.onDifficultySelected(difficulty) },
                                label = { Text(difficulty.name, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AccentYellow,
                                    selectedLabelColor = NavyBackground,
                                    containerColor = NavySurface,
                                    labelColor = TextSecondary
                                )
                            )
                        }
                    }

                    // Stats Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.lights_out_moves, uiState.moves),
                            color = AccentYellow,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Score: ${uiState.score}",
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )

                            // Hint Button (Ad Rewarded)
                            IconButton(
                                onClick = {
                                    if (context is Activity) {
                                        viewModel.onUseHintWithReward(context)
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = "Hint",
                                    tint = AccentYellow
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    PuzzivoCard(
                        backgroundColor = NavySurfaceVariant,
                        borderColor = CardBorderColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            repeat(uiState.gridSize) { row ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    repeat(uiState.gridSize) { col ->
                                        val index = row * uiState.gridSize + col
                                        val isLit = uiState.lights.getOrElse(index) { false }
                                        val isHinted = uiState.hintCellIndex == index

                                        val tileBorderColor = when {
                                            isHinted -> AccentGreen
                                            isLit -> AccentYellow
                                            else -> CardBorderColor
                                        }

                                        Surface(
                                            color = if (isLit) AccentYellow else NavySurface,
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(if (isHinted) 2.5.dp else 1.dp, tileBorderColor),
                                            onClick = { viewModel.onCellTap(index) },
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxSize()
                                                .semantics {
                                                    contentDescription = "Light row ${row + 1}, column ${col + 1}"
                                                    role = Role.Button
                                                }
                                        ) {
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier.fillMaxSize()
                                            ) {
                                                Text(
                                                    text = if (isLit) "●" else "",
                                                    color = NavyBackground,
                                                    fontSize = 24.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    PrimaryButton(
                        text = stringResource(R.string.action_restart),
                        onClick = { viewModel.onRestart() }
                    )
                }
            }

            if (uiState.isGameComplete) {
                ConfettiParticleOverlay()
            }
        }
    }

    if (shouldShowInstructions || showHelpDialog) {
        InstructionDialog(
            title = stringResource(R.string.lights_out_title),
            description = stringResource(R.string.lights_out_rules),
            onDismiss = {
                showHelpDialog = false
                viewModel.onInstructionsDismissed()
            }
        )
    }

    if (uiState.isGameComplete) {
        ResultDialog(
            title = stringResource(R.string.lights_out_complete),
            scoreText = stringResource(R.string.lights_out_moves, uiState.moves),
            isNewBest = uiState.score > 0,
            bestScoreText = "Score: ${uiState.score}",
            onReplayClick = { viewModel.onRestart() },
            onHomeClick = onBackClick
        )
    }
}
