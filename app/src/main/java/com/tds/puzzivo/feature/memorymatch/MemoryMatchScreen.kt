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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tds.puzzivo.core.design.AccentCyan
import com.tds.puzzivo.core.design.AccentGreen
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

@Composable
fun MemoryMatchScreen(
    onBackClick: () -> Unit,
    viewModel: MemoryMatchViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val shouldShowInstructions by viewModel.shouldShowInstructions.collectAsState()
    val context = LocalContext.current
    var showInstructions by remember { mutableStateOf(false) }
    var showExitConfirmation by remember { mutableStateOf(false) }
    var showRestartConfirmation by remember { mutableStateOf(false) }

    LaunchedEffect(shouldShowInstructions) {
        if (shouldShowInstructions) showInstructions = true
    }

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
                title = stringResource(R.string.memory_match_title),
                onBackClick = {
                    if (uiState.isGameStarted && !uiState.isGameComplete) {
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
                            Text(text = stringResource(R.string.memory_match_moves), fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                            Text(text = "${uiState.moves}", fontSize = 22.sp, color = AccentCyan, fontWeight = FontWeight.Bold)
                        }
                    }

                    PuzzivoCard(modifier = Modifier.weight(1f)) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = stringResource(R.string.memory_match_time), fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                            Text(text = stringResource(R.string.quick_math_seconds_format, uiState.elapsedSeconds), fontSize = 22.sp, color = AccentYellow, fontWeight = FontWeight.Bold)
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
                        text = stringResource(R.string.hint_reveal_pair),
                        enabled = !uiState.hasUsedHint &&
                            !uiState.isGameComplete &&
                            !uiState.isInputLocked &&
                            uiState.flippedCardIndices.isEmpty(),
                        onClick = {
                            if (context is Activity) {
                                viewModel.onRevealPairHintWithReward(context)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    PrimaryButton(
                        text = stringResource(R.string.action_restart),
                        onClick = {
                            if (uiState.isGameStarted && !uiState.isGameComplete) {
                                showRestartConfirmation = true
                            } else {
                                viewModel.onRestart()
                            }
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
            message = stringResource(R.string.memory_match_leave_message),
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
            title = stringResource(R.string.memory_match_restart_title),
            message = stringResource(R.string.memory_match_restart_message),
            confirmText = stringResource(R.string.action_restart),
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
            title = stringResource(R.string.memory_match_rules_title),
            description = stringResource(R.string.memory_match_rules_description),
            onDismiss = {
                showInstructions = false
                viewModel.onInstructionsDismissed()
            }
        )
    }

    if (uiState.isGameComplete) {
        ResultDialog(
            title = stringResource(R.string.memory_match_complete),
            scoreText = stringResource(
                R.string.memory_match_score_format,
                pluralStringResource(R.plurals.memory_match_moves_format, uiState.moves, uiState.moves),
                pluralStringResource(R.plurals.memory_match_seconds_format, uiState.elapsedSeconds, uiState.elapsedSeconds)
            ),
            isNewBest = uiState.isNewBest,
            bestScoreText = if (uiState.bestMoves > 0) {
                pluralStringResource(R.plurals.memory_match_best_moves_format, uiState.bestMoves, uiState.bestMoves)
            } else {
                pluralStringResource(R.plurals.memory_match_best_moves_format, uiState.moves, uiState.moves)
            },
            detailsText = stringResource(R.string.memory_match_result_detail),
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
    val cardDescription = when {
        isMatched -> stringResource(R.string.memory_match_card_matched, card.icon)
        isFaceUp -> stringResource(R.string.memory_match_card_revealed, card.icon)
        else -> stringResource(R.string.memory_match_card_face_down)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier
            .graphicsLayer {
                rotationY = rotation
            }
            .semantics { contentDescription = cardDescription }
            .clickable(
                enabled = card != null && !isFaceUp,
                role = Role.Button
            ) { onClick() }
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
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                    contentDescription = null,
                    tint = TextSecondary.copy(alpha = 0.5f)
                )
            }
        }
    }
}
