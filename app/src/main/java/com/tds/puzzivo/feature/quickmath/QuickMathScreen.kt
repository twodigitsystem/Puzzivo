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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
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

@Composable
fun QuickMathScreen(
    onBackClick: () -> Unit,
    viewModel: QuickMathViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val shouldShowInstructions by viewModel.shouldShowInstructions.collectAsState()
    val context = LocalContext.current
    var showInstructions by remember { mutableStateOf(false) }
    var showExitConfirmation by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(shouldShowInstructions) {
        if (shouldShowInstructions) {
            viewModel.pauseForLifecycle()
            showInstructions = true
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                viewModel.pauseForLifecycle()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

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
                title = stringResource(R.string.quick_math_title),
                onBackClick = {
                    if (uiState.isGameStarted && !uiState.isGameComplete) {
                        if (!uiState.isPaused) viewModel.togglePause()
                        showExitConfirmation = true
                    } else {
                        onBackClick()
                    }
                },
                onActionClick = {
                    if (!uiState.isPaused && !uiState.isGameComplete) viewModel.togglePause()
                    showInstructions = true
                },
                actionContentDescription = stringResource(R.string.action_help)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
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
                            Text(text = stringResource(R.string.quick_math_timer), fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                            Text(
                                text = stringResource(R.string.quick_math_seconds_format, uiState.timeRemainingSeconds),
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
                            Text(text = stringResource(R.string.quick_math_score), fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
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
                                text = stringResource(R.string.quick_math_paused),
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

                uiState.lastAnswerWasCorrect?.let { wasCorrect ->
                    Text(
                        text = if (wasCorrect) stringResource(R.string.quick_math_correct) else stringResource(R.string.quick_math_not_quite),
                        color = if (wasCorrect) AccentGreen else AccentMagenta,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
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
                                enabled = !uiState.isPaused && !uiState.isGameComplete,
                                onClick = { viewModel.onAnswerClick(question.options[0]) },
                                modifier = Modifier.weight(1f)
                            )
                            AnswerOptionButton(
                                answer = question.options[1],
                                enabled = !uiState.isPaused && !uiState.isGameComplete,
                                onClick = { viewModel.onAnswerClick(question.options[1]) },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            AnswerOptionButton(
                                answer = question.options[2],
                                enabled = !uiState.isPaused && !uiState.isGameComplete,
                                onClick = { viewModel.onAnswerClick(question.options[2]) },
                                modifier = Modifier.weight(1f)
                            )
                            AnswerOptionButton(
                                answer = question.options[3],
                                enabled = !uiState.isPaused && !uiState.isGameComplete,
                                onClick = { viewModel.onAnswerClick(question.options[3]) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                 Spacer(modifier = Modifier.height(20.dp))

                // Action controls: +15s Hint & Pause
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SecondaryButton(
                        text = stringResource(R.string.hint_add_time),
                        enabled = !uiState.hasUsedTimeHint && !uiState.isGameComplete && !uiState.isPaused,
                        onClick = {
                            if (context is Activity) {
                                viewModel.onAdd15SecondsWithReward(context)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    PrimaryButton(
                        text = if (uiState.isPaused) stringResource(R.string.action_resume) else stringResource(R.string.action_pause),
                        onClick = { viewModel.togglePause() },
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
            title = stringResource(R.string.quick_math_leave_title),
            message = stringResource(R.string.quick_math_leave_message),
            confirmText = stringResource(R.string.action_leave),
            dismissText = stringResource(R.string.action_resume),
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
            title = stringResource(R.string.quick_math_rules_title),
            description = stringResource(R.string.quick_math_rules_description),
            onDismiss = {
                showInstructions = false
                viewModel.onInstructionsDismissed()
                if (uiState.isPaused) viewModel.togglePause()
            }
        )
    }

    if (uiState.isGameComplete) {
        ResultDialog(
            title = stringResource(R.string.quick_math_time_up),
            scoreText = stringResource(R.string.quick_math_score_format, uiState.score, uiState.correctAnswersCount, uiState.questionsAnswered),
            isNewBest = uiState.isNewBest,
            bestScoreText = stringResource(R.string.quick_math_best_format, uiState.bestScore),
            detailsText = stringResource(
                R.string.quick_math_result_detail,
                if (uiState.questionsAnswered == 0) 0 else (uiState.correctAnswersCount * 100 / uiState.questionsAnswered),
                pluralStringResource(R.plurals.quick_math_questions_format, uiState.questionsAnswered, uiState.questionsAnswered)
            ),
            onReplayClick = { viewModel.onRestart() },
            onHomeClick = onBackClick
        )
    }
}

@Composable
fun AnswerOptionButton(
    modifier: Modifier = Modifier,
    answer: Int,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
        Surface(
            color = NavySurface,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor),
        modifier = modifier
            .height(64.dp)
            .padding(0.dp),
            enabled = enabled,
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
