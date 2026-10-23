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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tds.puzzivo.core.design.AccentCyan
import com.tds.puzzivo.core.design.AccentGreen
import com.tds.puzzivo.core.design.AccentMagenta
import com.tds.puzzivo.core.design.AccentOrange
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
import com.tds.puzzivo.core.design.TextSecondary
import kotlin.math.abs

@Composable
fun NumberMergeScreen(
    onBackClick: () -> Unit,
    viewModel: NumberMergeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showInstructions by remember { mutableStateOf(false) }
    var showExitConfirmation by remember { mutableStateOf(false) }

    var totalDragX by remember { mutableFloatStateOf(0f) }
    var totalDragY by remember { mutableFloatStateOf(0f) }

    BackHandler(enabled = !uiState.isGameOver && uiState.score > 0) {
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
                title = "Number Merge",
                onBackClick = {
                    if (!uiState.isGameOver && uiState.score > 0) {
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
                            Text(text = "SCORE", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                            Text(text = "${uiState.score}", fontSize = 22.sp, color = AccentYellow, fontWeight = FontWeight.Bold)
                        }
                    }

                    PuzzivoCard(modifier = Modifier.weight(1f)) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "BEST", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
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

                Spacer(modifier = Modifier.height(20.dp))

                // Action Controls: Undo and New Game
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SecondaryButton(
                        text = "⏪ Undo (Ad)",
                        enabled = uiState.canUndo,
                        onClick = {
                            if (context is Activity) {
                                viewModel.onUndoWithReward(context)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    PrimaryButton(
                        text = "New Game",
                        onClick = { viewModel.onRestart() },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            BannerAdView(modifier = Modifier.padding(16.dp))
        }
    }

    if (showExitConfirmation) {
        ConfirmationDialog(
            title = "Leave Game?",
            message = "Your current score will be saved locally, but your active undo move history will reset.",
            confirmText = "Leave",
            dismissText = "Keep Playing",
            onConfirm = {
                showExitConfirmation = false
                onBackClick()
            },
            onDismiss = { showExitConfirmation = false }
        )
    }

    if (showInstructions) {
        InstructionDialog(
            title = "Number Merge Rules",
            description = "Swipe Up, Down, Left, or Right to slide tiles. When two equal numbers touch during a swipe, they merge into one with double the value! Score increases with each merge.",
            onDismiss = { showInstructions = false }
        )
    }

    if (uiState.isGameOver) {
        ResultDialog(
            title = "Game Over!",
            scoreText = "${uiState.score} points",
            isNewBest = uiState.score > 0 && uiState.score >= uiState.bestScore,
            bestScoreText = "${uiState.bestScore}",
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
