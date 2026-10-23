package com.tds.puzzivo.feature.wordsearch

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tds.puzzivo.core.design.AccentGreen
import com.tds.puzzivo.core.design.AccentMagenta
import com.tds.puzzivo.core.design.AccentYellow
import com.tds.puzzivo.core.design.BannerAdView
import com.tds.puzzivo.core.design.CardBorderColor
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WordSearchScreen(
    onBackClick: () -> Unit,
    viewModel: WordSearchViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showInstructions by remember { mutableStateOf(false) }
    var showExitConfirmation by remember { mutableStateOf(false) }

    val puzzle = uiState.puzzle

    BackHandler(enabled = uiState.foundWords.isNotEmpty() && !uiState.isGameComplete) {
        showExitConfirmation = true
    }

    Surface(
        color = NavyBackground,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            PuzzivoTopBar(
                title = "Word Search",
                onBackClick = {
                    if (uiState.foundWords.isNotEmpty() && !uiState.isGameComplete) {
                        showExitConfirmation = true
                    } else {
                        onBackClick()
                    }
                },
                onSettingsClick = { showInstructions = true }
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
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
                            text = puzzle.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = AccentYellow
                        )
                        Text(
                            text = "${uiState.foundWords.size} / ${puzzle.targetWords.size} Words",
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

                            val chipBg = when {
                                isFound -> AccentGreen.copy(alpha = 0.25f)
                                isHinted -> AccentYellow.copy(alpha = 0.25f)
                                else -> NavySurface
                            }

                            val chipBorder = when {
                                isFound -> AccentGreen
                                isHinted -> AccentYellow
                                else -> CardBorderColor
                            }

                            Surface(
                                color = chipBg,
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, chipBorder)
                            ) {
                                Text(
                                    text = targetWord.word,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isFound) AccentGreen else TextPrimary,
                                    textDecoration = if (isFound) TextDecoration.LineThrough else TextDecoration.None,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Letter Grid with Pointer Input
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
                                .pointerInput(puzzle) {
                                    detectDragGestures(
                                        onDragStart = { offset ->
                                            if (size.width > 0 && size.height > 0) {
                                                val cellWidth = size.width / puzzle.cols
                                                val cellHeight = size.height / puzzle.rows
                                                val col = (offset.x / cellWidth).toInt().coerceIn(0, puzzle.cols - 1)
                                                val row = (offset.y / cellHeight).toInt().coerceIn(0, puzzle.rows - 1)
                                                viewModel.onCellTouchStart(row, col)
                                            }
                                        },
                                        onDragEnd = {
                                            viewModel.onCellTouchEnd()
                                        },
                                        onDragCancel = {
                                            viewModel.onCellTouchEnd()
                                        },
                                        onDrag = { change, _ ->
                                            change.consume()
                                            if (size.width > 0 && size.height > 0) {
                                                val cellWidth = size.width / puzzle.cols
                                                val cellHeight = size.height / puzzle.rows
                                                val col = (change.position.x / cellWidth).toInt().coerceIn(0, puzzle.cols - 1)
                                                val row = (change.position.y / cellHeight).toInt().coerceIn(0, puzzle.rows - 1)
                                                viewModel.onCellTouchMove(row, col)
                                            }
                                        }
                                    )
                                }
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                for (r in 0 until puzzle.rows) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        for (c in 0 until puzzle.cols) {
                                            val pos = CellPosition(r, c)
                                            val isSelected = uiState.selectedCells.contains(pos)
                                            val isFoundCell = uiState.foundCellPositions.contains(pos)
                                            val isHinted = isHintedCell(pos, uiState.highlightedHintWord)

                                            val char = puzzle.grid[r][c]

                                            GridLetterCell(
                                                char = char,
                                                isSelected = isSelected,
                                                isFound = isFoundCell,
                                                isHinted = isHinted,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .fillMaxSize()
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Actions: Hint & Next Puzzle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SecondaryButton(
                        text = "💡 Reveal Word (Ad)",
                        enabled = !uiState.hasUsedHint && !uiState.isGameComplete,
                        onClick = {
                            if (context is Activity) {
                                viewModel.onRevealWordWithReward(context)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    PrimaryButton(
                        text = "Next Puzzle",
                        onClick = { viewModel.onNextPuzzle() },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            BannerAdView(modifier = Modifier.padding(16.dp))
        }
    }

    if (showExitConfirmation) {
        ConfirmationDialog(
            title = "Leave Puzzle?",
            message = "Are you sure you want to leave this Word Search puzzle?",
            confirmText = "Leave",
            dismissText = "Keep Searching",
            onConfirm = {
                showExitConfirmation = false
                onBackClick()
            },
            onDismiss = { showExitConfirmation = false }
        )
    }

    if (showInstructions) {
        InstructionDialog(
            title = "Word Search Rules",
            description = "Touch and drag a straight line over the letter grid to highlight target words. Words can appear horizontally, vertically, or diagonally!",
            onDismiss = { showInstructions = false }
        )
    }

    if (uiState.isGameComplete) {
        ResultDialog(
            title = "Puzzle Complete!",
            scoreText = "Found All Words!",
            isNewBest = false,
            bestScoreText = "Completed",
            onReplayClick = { viewModel.onNextPuzzle() },
            onHomeClick = onBackClick
        )
    }
}

@Composable
fun GridLetterCell(
    char: Char,
    isSelected: Boolean,
    isFound: Boolean,
    isHinted: Boolean,
    modifier: Modifier = Modifier
) {
    val bgColor = when {
        isSelected -> AccentYellow.copy(alpha = 0.5f)
        isFound -> AccentGreen.copy(alpha = 0.35f)
        isHinted -> AccentMagenta.copy(alpha = 0.4f)
        else -> NavySurface
    }

    val textColor = when {
        isSelected -> NavyBackground
        isFound -> AccentGreen
        isHinted -> AccentMagenta
        else -> TextPrimary
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(6.dp),
        border = if (isSelected || isFound || isHinted) BorderStroke(1.dp, textColor) else null,
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "$char",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

private fun isHintedCell(pos: CellPosition, hintWord: TargetWord?): Boolean {
    if (hintWord == null || hintWord.startRow == -1) return false

    val dRow = hintWord.endRow - hintWord.startRow
    val dCol = hintWord.endCol - hintWord.startCol
    val steps = maxOf(kotlin.math.abs(dRow), kotlin.math.abs(dCol))
    if (steps == 0) return pos.row == hintWord.startRow && pos.col == hintWord.startCol

    val stepRow = if (dRow == 0) 0 else dRow / steps
    val stepCol = if (dCol == 0) 0 else dCol / steps

    var r = hintWord.startRow
    var c = hintWord.startCol
    for (i in 0..steps) {
        if (pos.row == r && pos.col == c) return true
        r += stepRow
        c += stepCol
    }
    return false
}
