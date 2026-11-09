package com.tds.puzzivo.feature.minesweeper

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
import androidx.compose.ui.res.pluralStringResource
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
import com.tds.puzzivo.core.design.AccentMagenta
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
fun MinesweeperScreen(
    onBackClick: () -> Unit,
    viewModel: MinesweeperViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val shouldShowInstructions by viewModel.shouldShowInstructions.collectAsState()
    var showHelpDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Surface(color = NavyBackground, modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                PuzzivoTopBar(
                    title = stringResource(R.string.minesweeper_title),
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

                    // Header Info Bar (Mines Left & Safe Reveal Hint)
                    val flagsCount = uiState.cells.count { it.flagged }
                    val minesLeft = (uiState.totalMines - flagsCount).coerceAtLeast(0)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.minesweeper_mines_left, minesLeft),
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

                            // Safe Reveal Hint Button (Ad Rewarded)
                            IconButton(
                                onClick = {
                                    if (context is Activity) {
                                        viewModel.onUseSafeRevealWithReward(context)
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = "Safe Reveal Hint",
                                    tint = AccentYellow
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

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
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            repeat(uiState.boardSize) { row ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    repeat(uiState.boardSize) { col ->
                                        val index = row * uiState.boardSize + col
                                        val cell = uiState.cells.getOrElse(index) { MineCell() }

                                        MineCellView(
                                            row = row,
                                            col = col,
                                            cell = cell,
                                            showMines = uiState.isGameOver,
                                            onClick = { viewModel.onCellClick(index) },
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxSize()
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SecondaryButton(
                            text = if (uiState.flagMode) stringResource(R.string.minesweeper_reveal) else stringResource(R.string.minesweeper_flag),
                            onClick = { viewModel.onToggleFlagMode() },
                            modifier = Modifier.weight(1f)
                        )
                        PrimaryButton(
                            text = stringResource(R.string.action_restart),
                            onClick = { viewModel.onRestart() },
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
            title = stringResource(R.string.minesweeper_title),
            description = stringResource(R.string.minesweeper_rules),
            onDismiss = {
                showHelpDialog = false
                viewModel.onInstructionsDismissed()
            }
        )
    }

    if (uiState.isWon || uiState.isGameOver) {
        ResultDialog(
            title = stringResource(if (uiState.isWon) R.string.minesweeper_won else R.string.minesweeper_lost),
            scoreText = stringResource(R.string.minesweeper_mines_left, uiState.cells.count { it.flagged }),
            isNewBest = uiState.isWon && uiState.score > 0,
            bestScoreText = "Score: ${uiState.score}",
            onReplayClick = { viewModel.onRestart() },
            onHomeClick = onBackClick
        )
    }
}

@Composable
private fun MineCellView(
    row: Int,
    col: Int,
    cell: MineCell,
    showMines: Boolean,
    onClick: () -> Unit,
    modifier: Modifier
) {
    val visibleMine = cell.mine && (cell.revealed || showMines)
    val label = when {
        visibleMine -> stringResource(R.string.minesweeper_mine)
        cell.flagged -> stringResource(R.string.minesweeper_flagged)
        !cell.revealed -> stringResource(R.string.minesweeper_hidden)
        else -> pluralStringResource(R.plurals.minesweeper_number, cell.adjacent, cell.adjacent)
    }
    val cellDescription = stringResource(R.string.minesweeper_cell, row + 1, col + 1, label)
    Surface(
        color = when {
            visibleMine -> AccentMagenta.copy(alpha = 0.5f)
            cell.revealed -> NavySurface
            cell.flagged -> AccentYellow.copy(alpha = 0.35f)
            else -> NavySurfaceVariant
        },
        shape = RoundedCornerShape(5.dp),
        border = BorderStroke(1.dp, CardBorderColor),
        onClick = onClick,
        modifier = modifier.semantics {
            contentDescription = cellDescription
            role = Role.Button
        }
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                text = when {
                    visibleMine -> "✕"
                    cell.flagged -> "⚑"
                    cell.revealed && cell.adjacent > 0 -> cell.adjacent.toString()
                    else -> ""
                },
                color = if (cell.flagged) NavyBackground else if (visibleMine) AccentMagenta else AccentGreen,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }
}
