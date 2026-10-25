package com.tds.puzzivo.feature.numbermerge

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tds.puzzivo.core.design.AccentCyan
import com.tds.puzzivo.core.design.AccentGreen
import com.tds.puzzivo.core.design.AccentMagenta
import com.tds.puzzivo.core.design.AccentOrange
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
import kotlin.math.abs

@Composable
fun NumberMergeScreen(
    onBackClick: () -> Unit,
    viewModel: NumberMergeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val shouldShowInstructions by viewModel.shouldShowInstructions.collectAsState()
    val context = LocalContext.current
    var showInstructions by remember { mutableStateOf(false) }
    var showExitConfirmation by remember { mutableStateOf(false) }
    var showRestartConfirmation by remember { mutableStateOf(false) }

    var totalDragX by remember { mutableFloatStateOf(0f) }
    var totalDragY by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(shouldShowInstructions) {
        if (shouldShowInstructions) showInstructions = true
    }

    BackHandler(enabled = !uiState.isGameOver && uiState.moveCount > 0) {
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
                title = stringResource(R.string.number_merge_title),
                onBackClick = {
                    if (!uiState.isGameOver && uiState.moveCount > 0) {
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
                // Score Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PuzzivoCard(modifier = Modifier.weight(1f)) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = stringResource(R.string.number_merge_score), fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                            Text(text = "${uiState.score}", fontSize = 22.sp, color = AccentYellow, fontWeight = FontWeight.Bold)
                        }
                    }

                    PuzzivoCard(modifier = Modifier.weight(1f)) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = stringResource(R.string.number_merge_best), fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                            Text(text = "${uiState.bestScore}", fontSize = 22.sp, color = AccentCyan, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 4x4 Grid Board with Swipe Pointer Input
                PuzzivoCard(
                    backgroundColor = NavySurfaceVariant,
                    borderColor = CardBorderColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = {
                                    totalDragX = 0f
                                    totalDragY = 0f
                                },
                                onDragEnd = {
                                    val threshold = 50f
                                    if (abs(totalDragX) > abs(totalDragY)) {
                                        if (totalDragX > threshold) {
                                            viewModel.onSwipe(MoveDirection.RIGHT)
                                        } else if (totalDragX < -threshold) {
                                            viewModel.onSwipe(MoveDirection.LEFT)
                                        }
                                    } else {
                                        if (totalDragY > threshold) {
                                            viewModel.onSwipe(MoveDirection.DOWN)
                                        } else if (totalDragY < -threshold) {
                                            viewModel.onSwipe(MoveDirection.UP)
                                        }
                                    }
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    totalDragX += dragAmount.x
                                    totalDragY += dragAmount.y
                                }
                            )
                        }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (row in 0 until 4) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                for (col in 0 until 4) {
                                    val tileValue = uiState.grid[row * 4 + col]
                                    NumberTile(
                                        value = tileValue,
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxSize()
                                    )
                                }
                            }
                        }
                    }
                }

                if (uiState.hasReached2048) {
                    Text(
                        text = stringResource(R.string.number_merge_milestone),
                        color = AccentYellow,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Controls: Undo and New Game
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SecondaryButton(
                        text = stringResource(R.string.hint_undo),
                        enabled = uiState.canUndo,
                        onClick = {
                            if (context is Activity) {
                                viewModel.onUndoWithReward(context)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    PrimaryButton(
                        text = stringResource(R.string.action_new_game),
                        onClick = {
                            if (uiState.moveCount > 0) showRestartConfirmation = true
                            else viewModel.onRestart()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                Text(
                    text = stringResource(R.string.hint_watch_ad),
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

        }
    }

    if (showExitConfirmation) {
        ConfirmationDialog(
            title = stringResource(R.string.dialog_leave_title),
            message = stringResource(R.string.number_merge_leave_message),
            confirmText = stringResource(R.string.action_leave),
            dismissText = stringResource(R.string.action_keep_playing),
            onConfirm = {
                showExitConfirmation = false
                onBackClick()
            },
            onDismiss = { showExitConfirmation = false }
        )
    }

    if (showRestartConfirmation) {
        ConfirmationDialog(
            title = stringResource(R.string.number_merge_new_game_title),
            message = stringResource(R.string.number_merge_new_game_message),
            confirmText = stringResource(R.string.action_new_game),
            dismissText = stringResource(R.string.action_keep_playing),
            onConfirm = {
                showRestartConfirmation = false
                viewModel.onRestart()
            },
            onDismiss = { showRestartConfirmation = false }
        )
    }

    if (showInstructions) {
        InstructionDialog(
            title = stringResource(R.string.number_merge_rules_title),
            description = stringResource(R.string.number_merge_rules_description),
                 onDismiss = {
                     showInstructions = false
                     viewModel.onInstructionsDismissed()
                 }
        )
    }

    if (uiState.isGameOver) {
        ResultDialog(
            title = stringResource(R.string.number_merge_game_over),
            scoreText = pluralStringResource(R.plurals.number_merge_score_format, uiState.score, uiState.score),
            isNewBest = uiState.isNewBest,
            bestScoreText = "${uiState.bestScore}",
            detailsText = pluralStringResource(R.plurals.number_merge_result_detail, uiState.moveCount, uiState.moveCount),
            onReplayClick = { viewModel.onRestart() },
            onHomeClick = onBackClick
        )
    }
}

@Composable
fun NumberTile(
    value: Int,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = getTileColors(value)
    val tileDescription = if (value == 0) {
        stringResource(R.string.number_merge_empty_tile)
    } else {
        stringResource(R.string.number_merge_tile_description, value)
    }

    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (value > 0) 1f else 0.92f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
        ),
        label = "tilePopScale"
    )

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp),
        border = if (value > 0) null else androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor.copy(alpha = 0.3f)),
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }.semantics {
            contentDescription = tileDescription
        }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            if (value > 0) {
                Text(
                    text = "$value",
                    fontSize = if (value > 512) 18.sp else 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
        }
    }
}

private fun getTileColors(value: Int): Pair<Color, Color> {
    return when (value) {
        0 -> Pair(NavySurface, TextSecondary)
        2 -> Pair(Color(0xFF334155), TextPrimary)
        4 -> Pair(Color(0xFF475569), TextPrimary)
        8 -> Pair(AccentOrange, NavyBackground)
        16 -> Pair(Color(0xFFEA580C), NavyBackground)
        32 -> Pair(AccentMagenta, TextPrimary)
        64 -> Pair(Color(0xFFDB2777), TextPrimary)
        128 -> Pair(AccentYellow, NavyBackground)
        256 -> Pair(AccentCyan, NavyBackground)
        512 -> Pair(AccentGreen, NavyBackground)
        1024 -> Pair(Color(0xFF10B981), NavyBackground)
        2048 -> Pair(Color(0xFF8B5CF6), TextPrimary)
        else -> Pair(Color(0xFF6366F1), TextPrimary)
    }
}
