package com.tds.puzzivo.feature.memorymatch

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tds.puzzivo.core.design.AccentCyan
import com.tds.puzzivo.core.design.AccentGreen
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

@Composable
fun MemoryMatchScreen(
    onBackClick: () -> Unit,
    viewModel: MemoryMatchViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showInstructions by remember { mutableStateOf(false) }
    var showExitConfirmation by remember { mutableStateOf(false) }

    BackHandler(enabled = uiState.isGameStarted && !uiState.isGameComplete) {
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
                title = "Memory Match",
                onBackClick = {
                    if (uiState.isGameStarted && !uiState.isGameComplete) {
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
                // Header Counters
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PuzzivoCard(modifier = Modifier.weight(1f)) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "MOVES", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                            Text(text = "${uiState.moves}", fontSize = 22.sp, color = AccentCyan, fontWeight = FontWeight.Bold)
                        }
                    }

                    PuzzivoCard(modifier = Modifier.weight(1f)) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "TIME", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                            Text(text = "${uiState.elapsedSeconds}s", fontSize = 22.sp, color = AccentYellow, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 4x4 Card Grid
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
                                    val index = row * 4 + col
                                    val card = uiState.cards.getOrNull(index)

                                    MemoryCardItem(
                                        card = card,
                                        onClick = { viewModel.onCardClick(index) },
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

                // Actions: Hint & Restart
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SecondaryButton(
                        text = "🔍 Reveal Pair (Ad)",
                        enabled = !uiState.hasUsedHint && !uiState.isGameComplete,
                        onClick = {
                            if (context is Activity) {
                                viewModel.onRevealPairHintWithReward(context)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    PrimaryButton(
                        text = "Restart",
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
            message = "Are you sure you want to leave? Your current card match run will be lost.",
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
            title = "Memory Match Rules",
            description = "Tap a card to flip it over, then tap a second card. If both cards match, they stay face-up! Find all 8 matching pairs in as few moves and seconds as possible.",
            onDismiss = { showInstructions = false }
        )
    }

    if (uiState.isGameComplete) {
        val isNewBest = uiState.bestMoves == 0 || uiState.moves <= uiState.bestMoves
        ResultDialog(
            title = "Puzzle Solved!",
            scoreText = "${uiState.moves} moves in ${uiState.elapsedSeconds}s",
            isNewBest = isNewBest,
            bestScoreText = if (uiState.bestMoves > 0) "${uiState.bestMoves} moves" else "${uiState.moves} moves",
            onReplayClick = { viewModel.onRestart() },
            onHomeClick = onBackClick
        )
    }
}

@Composable
fun MemoryCardItem(
    card: MemoryCard?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isFaceUp = card?.isFaceUp == true || card?.isMatched == true
    val isMatched = card?.isMatched == true

    val rotation by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isFaceUp) 180f else 0f,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 300),
        label = "cardFlipAnimation"
    )

    val bgColor = when {
        isMatched -> AccentGreen.copy(alpha = 0.25f)
        isFaceUp -> NavySurface
        else -> NavySurfaceVariant
    }

    val borderColor = when {
        isMatched -> AccentGreen
        isFaceUp -> AccentCyan
        else -> CardBorderColor
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier
            .graphicsLayer {
                rotationY = rotation
            }
            .clickable(enabled = card != null && !isFaceUp) { onClick() }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    if (rotation > 90f) {
                        rotationY = 180f
                    }
                }
        ) {
            if (rotation > 90f && card != null) {
                Text(
                    text = card.icon,
                    fontSize = 28.sp
                )
            } else {
                Text(
                    text = "❓",
                    fontSize = 20.sp,
                    color = TextSecondary.copy(alpha = 0.5f)
                )
            }
        }
    }
}
