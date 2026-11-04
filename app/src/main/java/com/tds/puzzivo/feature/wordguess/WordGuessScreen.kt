package com.tds.puzzivo.feature.wordguess

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tds.puzzivo.R
import com.tds.puzzivo.core.design.*
import com.tds.puzzivo.core.model.GameType

@Composable
fun WordGuessScreen(
    onBackClick: () -> Unit,
    viewModel: WordGuessViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showHelp by remember { mutableStateOf(false) }

    RecordGameCompletionEffect(GameType.WORD_GUESS, uiState.isGameComplete && uiState.isWon)

    Surface(
        color = NavyBackground,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            PuzzivoTopBar(
                title = stringResource(R.string.game_word_guess_title),
                onBackClick = onBackClick,
                onActionClick = { showHelp = true },
                actionContentDescription = stringResource(R.string.action_help)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // 5x6 Board Grid
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    uiState.rows.forEach { row ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (i in 0 until 5) {
                                val char = row.word.getOrNull(i)
                                val status = if (row.isSubmitted) row.statuses[i] else LetterStatus.UNKNOWN
                                LetterTile(char = char, status = status)
                            }
                        }
                    }
                }

                // Keyboard
                WordGuessKeyboard(
                    letterStatuses = uiState.keyboardLetterStatuses,
                    onKeyPress = { viewModel.onKeyPress(it) },
                    onDeletePress = { viewModel.onDeletePress() },
                    onSubmitPress = { viewModel.onSubmitPress() }
                )
            }
        }
    }

    if (showHelp) {
        InstructionDialog(
            title = stringResource(R.string.game_word_guess_title),
            description = stringResource(R.string.game_word_guess_description),
            onDismiss = { showHelp = false }
        )
    }

    if (uiState.isGameComplete) {
        ResultDialog(
            title = if (uiState.isWon) "WORD GUESSED!" else "GAME OVER",
            scoreText = if (uiState.isWon) "Guessed in ${uiState.attemptsUsed} attempts!" else "Word was: ${uiState.targetWord}",
            isNewBest = uiState.isWon,
            bestScoreText = "Target: ${uiState.targetWord}",
            onReplayClick = { viewModel.startNewGame() },
            onHomeClick = onBackClick
        )
    }
}

@Composable
private fun LetterTile(
    char: Char?,
    status: LetterStatus
) {
    val (bg, border, textColor) = when (status) {
        LetterStatus.CORRECT -> Triple(AccentGreen, AccentGreen, NavyBackground)
        LetterStatus.PRESENT -> Triple(AccentYellow, AccentYellow, NavyBackground)
        LetterStatus.ABSENT -> Triple(NavySurfaceVariant, CardBorderColor, TextSecondary)
        LetterStatus.UNKNOWN -> Triple(NavySurface, CardBorderColor, TextPrimary)
    }

    Surface(
        color = bg,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, border),
        modifier = Modifier.size(52.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                text = char?.toString() ?: "",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun WordGuessKeyboard(
    letterStatuses: Map<Char, LetterStatus>,
    onKeyPress: (Char) -> Unit,
    onDeletePress: () -> Unit,
    onSubmitPress: () -> Unit
) {
    val row1 = "QWERTYUIOP".toList()
    val row2 = "ASDFGHJKL".toList()
    val row3 = "ZXCVBNM".toList()

    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            row1.forEach { char ->
                KeyTile(char = char, status = letterStatuses[char] ?: LetterStatus.UNKNOWN, onClick = { onKeyPress(char) })
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            row2.forEach { char ->
                KeyTile(char = char, status = letterStatuses[char] ?: LetterStatus.UNKNOWN, onClick = { onKeyPress(char) })
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Enter key
            Surface(
                color = AccentYellow,
                shape = RoundedCornerShape(6.dp),
                onClick = onSubmitPress,
                modifier = Modifier.height(42.dp).width(58.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text("GO", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = NavyBackground)
                }
            }

            row3.forEach { char ->
                KeyTile(char = char, status = letterStatuses[char] ?: LetterStatus.UNKNOWN, onClick = { onKeyPress(char) })
            }

            // Backspace key
            Surface(
                color = NavySurfaceVariant,
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, CardBorderColor),
                onClick = onDeletePress,
                modifier = Modifier.height(42.dp).width(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text("⌫", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
            }
        }
    }
}

@Composable
private fun KeyTile(
    char: Char,
    status: LetterStatus,
    onClick: () -> Unit
) {
    val (bg, textColor) = when (status) {
        LetterStatus.CORRECT -> Pair(AccentGreen, NavyBackground)
        LetterStatus.PRESENT -> Pair(AccentYellow, NavyBackground)
        LetterStatus.ABSENT -> Pair(NavySurfaceVariant.copy(alpha = 0.4f), TextSecondary.copy(alpha = 0.5f))
        LetterStatus.UNKNOWN -> Pair(NavySurface, TextPrimary)
    }

    Surface(
        color = bg,
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, CardBorderColor),
        onClick = onClick,
        modifier = Modifier.height(42.dp).width(32.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(char.toString(), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textColor)
        }
    }
}
