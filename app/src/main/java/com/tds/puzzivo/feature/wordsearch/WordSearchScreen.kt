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
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tds.puzzivo.core.design.AccentGreen
import com.tds.puzzivo.core.design.AccentMagenta
import com.tds.puzzivo.core.design.AccentYellow
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
import com.tds.puzzivo.core.design.TextSecondary
import com.tds.puzzivo.R

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WordSearchScreen(
    onBackClick: () -> Unit,
    viewModel: WordSearchViewModel = viewModel()
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
                            text = "${puzzle.title} • ${puzzle.difficulty}",
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
                                    modifier = Modifier
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                        .semantics {
                                            contentDescription = if (isFound) {
                                                "${targetWord.word}, found"
                                            } else if (isHinted) {
                                                "${targetWord.word}, hint highlighted"
                                            } else {
                                                "${targetWord.word}, not found"
                                            }
                                        }
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
                                                 row = r,
                                                 col = c,
                                                 char = char,
                                                isSelected = isSelected,
                                                 isFound = isFoundCell,
                                                 isHinted = isHinted,
                                                  onTap = if (uiState.isGameComplete) null else ({ viewModel.onCellTap(r, c) }),
                                                 modifier = Modifier
                                                    .weight(1f)
                                                    .fillMaxSize()
                                            )
                                        }
                                    }
                                }
                            }

                            if (uiState.selectedCells.size >= 2) {
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
                        text = stringResource(R.string.hint_reveal_word),
                        enabled = !uiState.hasUsedHint && !uiState.isGameComplete,
                        onClick = {
                            if (context is Activity) {
                                viewModel.onRevealWordWithReward(context)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    PrimaryButton(
                        text = stringResource(R.string.word_search_next_puzzle),
                        enabled = uiState.isGameComplete,
                        onClick = { viewModel.onNextPuzzle() },
                        modifier = Modifier.weight(1f)
                    )
                }
                Text(
                    text = stringResource(R.string.hint_watch_ad),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 6.dp)
                )
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

    if (showInstructions) {
        InstructionDialog(
            title = stringResource(R.string.word_search_rules_title),
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
            scoreText = stringResource(R.string.word_search_complete_score),
            isNewBest = false,
            bestScoreText = stringResource(R.string.word_search_completed_label),
            detailsText = pluralStringResource(R.plurals.word_search_complete_detail, uiState.foundWords.size, uiState.foundWords.size),
            onReplayClick = { viewModel.onNextPuzzle() },
            onHomeClick = onBackClick
        )
    }
}

@Composable
fun GridLetterCell(
    modifier: Modifier = Modifier,
    row: Int,
    col: Int,
    char: Char,
    isSelected: Boolean,
    isFound: Boolean,
    isHinted: Boolean,
    onTap: (() -> Unit)? = null,
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

    val status = when {
        isSelected -> stringResource(R.string.word_search_cell_selected)
        isFound -> stringResource(R.string.word_search_cell_found)
        isHinted -> stringResource(R.string.word_search_cell_hinted)
        else -> stringResource(R.string.word_search_cell_unselected)
    }
    val cellDescription = stringResource(
        R.string.word_search_cell_description,
        row + 1,
        col + 1,
        char.toString(),
        status
    )

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(6.dp),
        border = if (isSelected || isFound || isHinted) BorderStroke(1.dp, textColor) else null,
        modifier = modifier
            .clickable(
                enabled = onTap != null,
                role = Role.Button,
                onClick = { onTap?.invoke() }
            )
            .semantics {
                contentDescription = cellDescription
            }
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
