package com.tds.puzzivo.feature.wordscramble

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tds.puzzivo.R
import com.tds.puzzivo.core.design.AccentGreen
import com.tds.puzzivo.core.design.AccentYellow
import com.tds.puzzivo.core.design.CardBorderColor
import com.tds.puzzivo.core.design.ConfettiParticleOverlay
import com.tds.puzzivo.core.design.InstructionDialog
import com.tds.puzzivo.core.design.NavyBackground
import com.tds.puzzivo.core.design.NavySurface
import com.tds.puzzivo.core.design.PrimaryButton
import com.tds.puzzivo.core.design.PuzzivoTopBar
import com.tds.puzzivo.core.design.ResultDialog
import com.tds.puzzivo.core.design.SecondaryButton
import com.tds.puzzivo.core.design.TextPrimary
import com.tds.puzzivo.core.design.TextSecondary
import com.tds.puzzivo.core.model.DifficultyLevel
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WordScrambleScreen(
    onBackClick: () -> Unit,
    viewModel: WordScrambleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val shouldShowInstructions by viewModel.shouldShowInstructions.collectAsState()
    var showHelpDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val shakeOffset = remember { Animatable(0f) }
    LaunchedEffect(uiState.isErrorState) {
        if (uiState.isErrorState) {
            repeat(3) {
                shakeOffset.animateTo(12f, tween(50))
                shakeOffset.animateTo(-12f, tween(50))
            }
            shakeOffset.animateTo(0f, tween(50))
        }
    }

    Surface(color = NavyBackground, modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                PuzzivoTopBar(
                    title = stringResource(R.string.word_scramble_title),
                    onBackClick = onBackClick,
                    onActionClick = { showHelpDialog = true },
                    actionContentDescription = stringResource(R.string.action_help)
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Difficulty Chips
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 8.dp)
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

                    // Stats Header Bar (Round, Score & Combo)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(
                                R.string.word_scramble_round,
                                uiState.roundIndex + 1,
                                uiState.totalRounds
                            ),
                            color = AccentYellow,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )

                        if (uiState.comboStreak > 1) {
                            Text(
                                text = "🔥 ${uiState.comboStreak}x",
                                color = AccentYellow,
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = stringResource(R.string.word_scramble_score, uiState.score),
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = stringResource(R.string.word_scramble_best, uiState.bestScore),
                                color = TextSecondary,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }

                    uiState.currentRound?.word?.let { word ->
                        Text(
                            text = stringResource(R.string.word_scramble_category, word.category),
                            color = AccentGreen,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                        )
                    }

                    // Definition Card / Solved Hint Banner
                    AnimatedVisibility(
                        visible = uiState.solvedDefinition != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        uiState.solvedDefinition?.let { def ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = NavySurface),
                                border = BorderStroke(1.dp, CardBorderColor),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.word_scramble_definition, def),
                                    color = TextSecondary,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Answer Slot Display Box (Target Word Slots)
                    uiState.currentRound?.let { round ->
                        val letterMap = round.letters.associateBy { it.id }
                        val targetLength = round.word.word.length

                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (i in 0 until targetLength) {
                                val selectedId = uiState.selectedLetterIds.getOrNull(i)
                                val char = selectedId?.let { letterMap[it]?.char }
                                val isRevealed = i in round.revealedIndices

                                AnswerSlotTile(
                                    char = char,
                                    isRevealed = isRevealed,
                                    isError = uiState.isErrorState,
                                    onClick = { viewModel.onAnswerSlotClick(i) }
                                )
                            }
                        }
                    }

                    // Feedback text
                    uiState.feedbackMessage?.let { msg ->
                        Text(
                            text = msg,
                            color = if (uiState.isErrorState) MaterialTheme.colorScheme.error else AccentGreen,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Scrambled Letter Pool
                    uiState.currentRound?.let { round ->
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            round.letters.forEach { scrambleLetter ->
                                val isSelected = scrambleLetter.id in uiState.selectedLetterIds
                                ScrambleLetterTile(
                                    letter = scrambleLetter.char,
                                    isSelected = isSelected,
                                    onClick = { viewModel.onLetterClick(scrambleLetter.id) }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Control Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Reshuffle Pool Button
                        IconButton(
                            onClick = { viewModel.onShufflePool() },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shuffle,
                                contentDescription = stringResource(R.string.word_scramble_shuffle),
                                tint = AccentYellow
                            )
                        }

                        // Backspace Button
                        IconButton(
                            onClick = { viewModel.onBackspace() },
                            enabled = uiState.selectedLetterIds.isNotEmpty(),
                            modifier = Modifier
                                .size(44.dp)
                                .semantics {
                                    role = Role.Button
                                }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Backspace,
                                contentDescription = stringResource(R.string.word_scramble_backspace),
                                tint = if (uiState.selectedLetterIds.isNotEmpty()) AccentYellow else TextSecondary
                            )
                        }

                        // Hint Button (Ad Rewarded)
                        IconButton(
                            onClick = {
                                if (context is Activity) {
                                    viewModel.onUseHintWithReward(context)
                                }
                            },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = stringResource(R.string.word_scramble_hint),
                                tint = AccentYellow
                            )
                        }

                        // Clear Button
                        SecondaryButton(
                            text = stringResource(R.string.word_scramble_clear),
                            onClick = { viewModel.onClear() },
                            modifier = Modifier.weight(1f)
                        )

                        // Submit Button
                        PrimaryButton(
                            text = stringResource(R.string.word_scramble_submit),
                            onClick = { viewModel.onSubmit() },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Confetti Overlay on victory
            if (uiState.isGameComplete) {
                ConfettiParticleOverlay()
            }
        }
    }

    if (shouldShowInstructions || showHelpDialog) {
        InstructionDialog(
            title = stringResource(R.string.word_scramble_title),
            description = stringResource(R.string.word_scramble_rules),
            onDismiss = {
                showHelpDialog = false
                viewModel.onInstructionsDismissed()
            }
        )
    }

    if (uiState.isGameComplete) {
        ResultDialog(
            title = stringResource(R.string.word_scramble_complete),
            scoreText = stringResource(R.string.word_scramble_score, uiState.score),
            isNewBest = uiState.score > 0 && uiState.score >= uiState.bestScore,
            bestScoreText = stringResource(R.string.word_scramble_best, uiState.bestScore),
            onReplayClick = { viewModel.onRestart() },
            onHomeClick = onBackClick
        )
    }
}

@Composable
private fun AnswerSlotTile(
    char: Char?,
    isRevealed: Boolean,
    isError: Boolean,
    onClick: () -> Unit
) {
    val borderColor = when {
        isError -> MaterialTheme.colorScheme.error
        isRevealed -> AccentGreen
        char != null -> AccentYellow
        else -> CardBorderColor
    }

    Surface(
        color = NavySurface,
        border = BorderStroke(1.5.dp, borderColor),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .padding(4.dp)
            .size(48.dp)
            .clickable(enabled = char != null, onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                text = char?.toString() ?: "",
                color = if (isRevealed) AccentGreen else TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun ScrambleLetterTile(
    letter: Char,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (isSelected) AccentYellow else NavySurface,
        border = BorderStroke(1.dp, if (isSelected) AccentYellow else CardBorderColor),
        shape = RoundedCornerShape(10.dp),
        onClick = onClick,
        modifier = Modifier
            .padding(4.dp)
            .size(52.dp)
            .semantics {
                contentDescription = "Letter $letter"
                role = Role.Button
            }
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                text = letter.toString(),
                color = if (isSelected) NavyBackground else TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
