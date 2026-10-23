package com.tds.puzzivo.feature.quickmath

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.tds.puzzivo.core.design.TextSecondary

@Composable
fun QuickMathScreen(
    onBackClick: () -> Unit,
    viewModel: QuickMathViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showInstructions by remember { mutableStateOf(false) }
    var showExitConfirmation by remember { mutableStateOf(false) }

    BackHandler(enabled = uiState.isGameStarted && !uiState.isGameComplete) {
        if (!uiState.isPaused) viewModel.togglePause()
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
                title = "Quick Math",
                onBackClick = {
                    if (uiState.isGameStarted && !uiState.isGameComplete) {
                        if (!uiState.isPaused) viewModel.togglePause()
                        showExitConfirmation = true
                    } else {
                        onBackClick()
                    }
                },
                onSettingsClick = {
                    if (!uiState.isPaused && !uiState.isGameComplete) viewModel.togglePause()
                    showInstructions = true
                }
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Timer & Score Counters
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PuzzivoCard(modifier = Modifier.weight(1f)) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "TIMER", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                            Text(
                                text = "${uiState.timeRemainingSeconds}s",
                                fontSize = 22.sp,
                                color = if (uiState.timeRemainingSeconds <= 10) AccentMagenta else AccentYellow,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    PuzzivoCard(modifier = Modifier.weight(1f)) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "SCORE", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                            Text(text = "${uiState.score}", fontSize = 22.sp, color = AccentGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LinearProgressIndicator(
                    progress = { (uiState.timeRemainingSeconds / 60f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = if (uiState.timeRemainingSeconds <= 10) AccentMagenta else AccentGreen,
                    trackColor = NavySurfaceVariant,
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Math Expression Display
                val question = uiState.currentQuestion
                PuzzivoCard(
                    backgroundColor = NavySurfaceVariant,
                    borderColor = CardBorderColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        if (uiState.isPaused) {
                            Text(
                                text = "⏸️ PAUSED",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = AccentYellow,
                                textAlign = TextAlign.Center
                            )
                        } else if (question != null) {
                            Text(
                                text = "${question.expression} = ?",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Answer Choices Grid (2x2)
                if (question != null) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            AnswerOptionButton(
                                answer = question.options[0],
                                onClick = { viewModel.onAnswerClick(question.options[0]) },
                                modifier = Modifier.weight(1f)
                            )
                            AnswerOptionButton(
                                answer = question.options[1],
                                onClick = { viewModel.onAnswerClick(question.options[1]) },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            AnswerOptionButton(
                                answer = question.options[2],
                                onClick = { viewModel.onAnswerClick(question.options[2]) },
                                modifier = Modifier.weight(1f)
                            )
                            AnswerOptionButton(
                                answer = question.options[3],
                                onClick = { viewModel.onAnswerClick(question.options[3]) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Action controls: +15s Hint & Pause
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SecondaryButton(
                        text = "⏳ +15 Seconds (Ad)",
                        enabled = !uiState.hasUsedTimeHint && !uiState.isGameComplete,
                        onClick = {
                            if (context is Activity) {
                                viewModel.onAdd15SecondsWithReward(context)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    PrimaryButton(
                        text = if (uiState.isPaused) "Resume" else "Pause",
                        onClick = { viewModel.togglePause() },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            BannerAdView(modifier = Modifier.padding(16.dp))
        }
    }

    if (showExitConfirmation) {
        ConfirmationDialog(
            title = "Quit Quick Math?",
            message = "Are you sure you want to exit? Your current timed run will be ended.",
            confirmText = "Exit",
            dismissText = "Resume",
            onConfirm = {
                showExitConfirmation = false
                onBackClick()
            },
            onDismiss = {
                showExitConfirmation = false
                if (uiState.isPaused) viewModel.togglePause()
            }
        )
    }

    if (showInstructions) {
        InstructionDialog(
            title = "Quick Math Rules",
            description = "Answer mental math questions before the 60-second timer runs out! Each correct answer earns 10 points. Earn high score streaks as you progress.",
            onDismiss = {
                showInstructions = false
                if (uiState.isPaused) viewModel.togglePause()
            }
        )
    }

    if (uiState.isGameComplete) {
        val isNewBest = uiState.score > 0 && uiState.score >= uiState.bestScore
        ResultDialog(
            title = "Time's Up!",
            scoreText = "${uiState.score} Pts (${uiState.correctAnswersCount}/${uiState.questionsAnswered} Correct)",
            isNewBest = isNewBest,
            bestScoreText = "${uiState.bestScore} Pts",
            onReplayClick = { viewModel.onRestart() },
            onHomeClick = onBackClick
        )
    }
}

@Composable
fun AnswerOptionButton(
    answer: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = NavySurface,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor),
        modifier = modifier
            .height(64.dp)
            .padding(0.dp),
        onClick = onClick
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "$answer",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
    }
}
