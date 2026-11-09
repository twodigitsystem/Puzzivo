package com.tds.puzzivo.feature.sudoku

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
import com.tds.puzzivo.core.design.AccentCyan
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
import com.tds.puzzivo.core.design.SecondaryButton
import com.tds.puzzivo.core.design.TextPrimary
import com.tds.puzzivo.core.design.TextSecondary
import com.tds.puzzivo.core.model.DifficultyLevel

@Composable
fun SudokuScreen(
    onBackClick: () -> Unit,
    viewModel: SudokuViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val shouldShowInstructions by viewModel.shouldShowInstructions.collectAsState()
    var showHelpDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Surface(color = NavyBackground, modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                PuzzivoTopBar(
                    title = stringResource(R.string.sudoku_title),
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
                    // Difficulty Selector
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 8.dp)
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

                    // Header Info Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Mistakes: ${uiState.mistakes}/3",
                            color = AccentCyan,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.sudoku_score, uiState.score),
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
                                    contentDescription = stringResource(R.string.sudoku_hint),
                                    tint = AccentYellow
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 6x6 Sudoku Board
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
                                .padding(6.dp),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            repeat(6) { row ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    repeat(6) { col ->
                                        val index = row * 6 + col
                                        val value = uiState.values.getOrElse(index) { 0 }
                                        val isGiven = index in uiState.puzzle.givens
                                        val isSelected = uiState.selectedIndex == index

                                        Surface(
                                            color = when {
                                                isSelected -> AccentYellow.copy(alpha = 0.3f)
                                                isGiven -> NavySurfaceVariant
                                                else -> NavySurface
                                            },
                                            shape = RoundedCornerShape(6.dp),
                                            border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) AccentYellow else CardBorderColor),
                                            onClick = { viewModel.onCellSelect(index) },
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxSize()
                                                .semantics {
                                                    contentDescription = "Sudoku cell row ${row + 1}, column ${col + 1}"
                                                    role = Role.Button
                                                }
                                        ) {
                                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                                Text(
                                                    text = if (value > 0) value.toString() else "",
                                                    color = if (isGiven) TextPrimary else AccentGreen,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 20.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Number Keypad (1 - 6) + Erase
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (digit in 1..6) {
                            SecondaryButton(
                                text = digit.toString(),
                                onClick = { viewModel.onNumberFill(digit) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        SecondaryButton(
                            text = "✕",
                            onClick = { viewModel.onNumberFill(0) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            if (uiState.isWon) {
                ConfettiParticleOverlay()
            }
        }
    }

    if (shouldShowInstructions || showHelpDialog) {
        InstructionDialog(
            title = stringResource(R.string.sudoku_title),
            description = stringResource(R.string.sudoku_rules),
            onDismiss = {
                showHelpDialog = false
                viewModel.onInstructionsDismissed()
            }
        )
    }

    if (uiState.isWon) {
        ResultDialog(
            title = stringResource(R.string.sudoku_complete),
            scoreText = stringResource(R.string.sudoku_score, uiState.score),
            isNewBest = uiState.score > 0,
            bestScoreText = stringResource(R.string.sudoku_score, uiState.score),
            onReplayClick = { viewModel.onRestart() },
            onHomeClick = onBackClick
        )
    }
}
