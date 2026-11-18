package com.tds.puzzivo.feature.wordsearch

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tds.puzzivo.R
import com.tds.puzzivo.core.design.AccentGreen
import com.tds.puzzivo.core.design.AccentMagenta
import com.tds.puzzivo.core.design.AccentYellow
import com.tds.puzzivo.core.design.CardBorderColor
import com.tds.puzzivo.core.design.ConfettiParticleOverlay
import com.tds.puzzivo.core.design.ConfirmationDialog
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WordSearchScreen(
    onBackClick: () -> Unit,
    viewModel: WordSearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val shouldShowInstructions by viewModel.shouldShowInstructions.collectAsState()
    val context = LocalContext.current
    var showInstructions by remember { mutableStateOf(false) }
    var showExitConfirmation by remember { mutableStateOf(false) }

    LaunchedEffect(shouldShowInstructions) {
        if (shouldShowInstructions) showInstructions = true
    }

    val puzzle = uiState.puzzle

    BackHandler(enabled = uiState.foundWords.isNotEmpty() && !uiState.isGameComplete) {
        showExitConfirmation = true
    }

    Surface(
        color = NavyBackground,
        modifier = Modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                PuzzivoTopBar(
                    title = stringResource(R.string.word_search_title),
                    onBackClick = {
                        if (uiState.foundWords.isNotEmpty() && !uiState.isGameComplete) {
                            showExitConfirmation = true
                        } else {
                            onBackClick()
                        }
                    },
                    onActionClick = { showInstructions = true },
                    actionContentDescription = stringResource(R.string.action_help)
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (puzzle != null) {
                        // Puzzle Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${puzzle.title} • Score: ${uiState.score}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = AccentYellow
                            )
                            Text(
                                text = pluralStringResource(R.plurals.word_search_words_progress, puzzle.targetWords.size, uiState.foundWords.size, puzzle.targetWords.size),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = AccentGreen
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Target Word List FlowRow
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            puzzle.targetWords.forEach { targetWord ->
                                val isFound = uiState.foundWords.contains(targetWord.word)
                                val isHinted = uiState.highlightedHintWord?.word == targetWord.word

                                Surface(
                                    color = when {
                                        isFound -> AccentGreen.copy(alpha = 0.2f)
                                        isHinted -> AccentYellow.copy(alpha = 0.3f)
                                        else -> NavySurface
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, if (isFound) AccentGreen else if (isHinted) AccentYellow else CardBorderColor)
                                ) {
                                    Text(
                                        text = targetWord.word,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isFound) AccentGreen else if (isHinted) AccentYellow else TextPrimary,
                                        textDecoration = if (isFound) TextDecoration.LineThrough else null,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Grid Box with Drag Gestures
                        PuzzivoCard(
                            backgroundColor = NavySurfaceVariant,
                            borderColor = CardBorderColor,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp)
                                    .pointerInput(Unit) {
                                        detectDragGestures(
                                            onDragStart = { offset ->
                                                val cellWidth = size.width / puzzle.cols
                                                val cellHeight = size.height / puzzle.rows
                                                val col = (offset.x / cellWidth).toInt()
                                                val row = (offset.y / cellHeight).toInt()
                                                viewModel.onCellTouchStart(row, col)
                                            },
                                            onDrag = { change, _ ->
                                                val cellWidth = size.width / puzzle.cols
                                                val cellHeight = size.height / puzzle.rows
                                                val col = (change.position.x / cellWidth).toInt()
                                                val row = (change.position.y / cellHeight).toInt()
                                                viewModel.onCellTouchMove(row, col)
                                            },
                                            onDragEnd = {
                                                viewModel.onCellTouchEnd()
                                            }
                                        )
                                    }
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    repeat(puzzle.rows) { row ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .weight(1f),
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            repeat(puzzle.cols) { col ->
                                                val cellPos = CellPosition(row, col)
                                                val isSelected = cellPos in uiState.selectedCells
                                                val isFound = cellPos in uiState.foundCellPositions
                                                val isFirstLetterHint = uiState.highlightedFirstLetterCell == cellPos
                                                val char = puzzle.grid[row][col]

                                                Surface(
                                                    color = when {
                                                        isFound -> AccentGreen.copy(alpha = 0.35f)
                                                        isSelected -> AccentYellow.copy(alpha = 0.4f)
                                                        isFirstLetterHint -> AccentMagenta.copy(alpha = 0.4f)
                                                        else -> NavySurface
                                                    },
                                                    shape = RoundedCornerShape(6.dp),
                                                    border = BorderStroke(
                                                        1.dp,
                                                        when {
                                                            isFound -> AccentGreen
                                                            isSelected -> AccentYellow
                                                            isFirstLetterHint -> AccentMagenta
                                                            else -> CardBorderColor
                                                        }
                                                    ),
                                                    onClick = {
                                                        if (context is Activity) {
                                                            viewModel.onCellTap(row, col)
                                                        }
                                                    },
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .fillMaxSize()
                                                        .semantics {
                                                            contentDescription = "Letter $char at row ${row + 1}, column ${col + 1}"
                                                            role = Role.Button
                                                        }
                                                ) {
                                                    Box(
                                                        contentAlignment = Alignment.Center,
                                                        modifier = Modifier.fillMaxSize()
                                                    ) {
                                                        Text(
                                                            text = char.toString(),
                                                            color = if (isFound || isSelected) TextPrimary else TextSecondary,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 18.sp
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // Draw selection line overlay if dragging
                                if (uiState.selectedCells.size > 1) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        val first = uiState.selectedCells.first()
                                        val last = uiState.selectedCells.last()
                                        val cellWidth = size.width / puzzle.cols
                                        val cellHeight = size.height / puzzle.rows

                                        drawLine(
                                            color = AccentYellow,
                                            start = Offset(
                                                x = (first.col + 0.5f) * cellWidth,
                                                y = (first.row + 0.5f) * cellHeight
                                            ),
                                            end = Offset(
                                                x = (last.col + 0.5f) * cellWidth,
                                                y = (last.row + 0.5f) * cellHeight
                                            ),
                                            strokeWidth = 6.dp.toPx(),
                                            cap = StrokeCap.Round,
                                            alpha = 0.65f
                                        )
                                    }
                                }
                            }
                        }

                        Text(
                            text = stringResource(R.string.word_search_select_cells),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Actions: Hint & Next Puzzle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            SecondaryButton(
                                text = "Hint (Letter)",
                                enabled = !uiState.isGameComplete,
                                onClick = {
                                    if (context is Activity) {
                                        viewModel.onRevealFirstLetterWithReward(context)
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )

                            SecondaryButton(
                                text = stringResource(R.string.hint_reveal_word),
                                enabled = !uiState.isGameComplete,
                                onClick = {
                                    if (context is Activity) {
                                        viewModel.onRevealWordWithReward(context)
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        PrimaryButton(
                            text = stringResource(R.string.word_search_next_puzzle),
                            enabled = uiState.isGameComplete,
                            onClick = { viewModel.onNextPuzzle() },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            if (uiState.isGameComplete) {
                ConfettiParticleOverlay()
            }
        }
    }

    if (showExitConfirmation) {
        ConfirmationDialog(
            title = stringResource(R.string.word_search_leave_title),
            message = stringResource(R.string.word_search_leave_message),
            confirmText = stringResource(R.string.action_leave),
            dismissText = stringResource(R.string.action_keep_playing),
            onConfirm = {
                showExitConfirmation = false
                onBackClick()
            },
            onDismiss = { showExitConfirmation = false }
        )
    }

    if (showInstructions || shouldShowInstructions) {
        InstructionDialog(
            title = stringResource(R.string.word_search_title),
            description = stringResource(R.string.word_search_rules_description),
            onDismiss = {
                showInstructions = false
                viewModel.onInstructionsDismissed()
            }
        )
    }

    if (uiState.isGameComplete) {
        ResultDialog(
            title = stringResource(R.string.word_search_complete),
            scoreText = "Score: ${uiState.score}",
            isNewBest = uiState.score > 0,
            bestScoreText = "Score: ${uiState.score}",
            onReplayClick = { viewModel.onNextPuzzle() },
            onHomeClick = onBackClick
        )
    }
}
