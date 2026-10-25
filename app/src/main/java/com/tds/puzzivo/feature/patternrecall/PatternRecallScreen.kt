package com.tds.puzzivo.feature.patternrecall

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tds.puzzivo.R
import com.tds.puzzivo.core.design.AccentCyan
import com.tds.puzzivo.core.design.AccentGreen
import com.tds.puzzivo.core.design.AccentYellow
import com.tds.puzzivo.core.design.CardBorderColor
import com.tds.puzzivo.core.design.InstructionDialog
import com.tds.puzzivo.core.design.NavyBackground
import com.tds.puzzivo.core.design.NavySurface
import com.tds.puzzivo.core.design.NavySurfaceVariant
import com.tds.puzzivo.core.design.PrimaryButton
import com.tds.puzzivo.core.design.PuzzivoCard
import com.tds.puzzivo.core.design.PuzzivoTopBar
import com.tds.puzzivo.core.design.RecordGameCompletionEffect
import com.tds.puzzivo.core.design.ResultDialog
import com.tds.puzzivo.core.design.TextPrimary
import com.tds.puzzivo.core.design.TextSecondary
import kotlin.random.Random

@Composable
fun PatternRecallScreen(onBackClick: () -> Unit) {
    var sequence by remember { mutableStateOf(listOf(Random.nextInt(9))) }
    var inputIndex by remember { mutableIntStateOf(0) }
    var litCell by remember { mutableStateOf<Int?>(null) }
    var isShowing by remember { mutableStateOf(true) }
    var round by remember { mutableIntStateOf(1) }
    var gameOver by remember { mutableStateOf(false) }
    var showHelp by remember { mutableStateOf(false) }

    LaunchedEffect(sequence, gameOver) {
        if (gameOver) return@LaunchedEffect
        isShowing = true
        inputIndex = 0
        kotlinx.coroutines.delay(500)
        sequence.forEach { cell ->
            litCell = cell
            kotlinx.coroutines.delay(450)
            litCell = null
            kotlinx.coroutines.delay(120)
        }
        isShowing = false
    }

    fun tapCell(cell: Int) {
        if (isShowing || gameOver) return
        if (cell != sequence[inputIndex]) {
            gameOver = true
            litCell = null
            return
        }
        inputIndex += 1
        if (inputIndex == sequence.size) {
            round += 1
            sequence = sequence + Random.nextInt(9)
        }
    }

    fun reset() {
        sequence = listOf(Random.nextInt(9))
        round = 1
        inputIndex = 0
        litCell = null
        gameOver = false
    }

    RecordGameCompletionEffect(com.tds.puzzivo.core.model.GameType.PATTERN_RECALL, gameOver)

    Surface(color = NavyBackground, modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            PuzzivoTopBar(
                title = stringResource(R.string.pattern_recall_title),
                onBackClick = onBackClick,
                onActionClick = { showHelp = true },
                actionContentDescription = stringResource(R.string.action_help)
            )
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(stringResource(R.string.pattern_recall_round, round), color = AccentCyan, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isShowing) stringResource(R.string.pattern_recall_watch) else stringResource(R.string.pattern_recall_repeat),
                    color = if (isShowing) AccentYellow else TextSecondary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(18.dp))
                PuzzivoCard(
                    backgroundColor = NavySurfaceVariant,
                    borderColor = CardBorderColor,
                    modifier = Modifier.fillMaxWidth().aspectRatio(1f)
                ) {
                    Column(modifier = Modifier.fillMaxSize().padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        repeat(3) { row ->
                            Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                repeat(3) { col ->
                                    val cell = row * 3 + col
                                    RecallCell(
                                        cell = cell,
                                        isLit = litCell == cell,
                                        isEnabled = !isShowing && !gameOver,
                                        onClick = { tapCell(cell) },
                                        modifier = Modifier.weight(1f).fillMaxSize()
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
                PrimaryButton(text = stringResource(R.string.action_restart), onClick = ::reset)
            }
        }
    }

    if (showHelp) {
        InstructionDialog(
            title = stringResource(R.string.pattern_recall_title),
            description = stringResource(R.string.pattern_recall_rules),
            onDismiss = { showHelp = false }
        )
    }
    if (gameOver) {
        ResultDialog(
            title = stringResource(R.string.pattern_recall_game_over),
            scoreText = stringResource(R.string.pattern_recall_round, (round - 1).coerceAtLeast(0)),
            isNewBest = false,
            bestScoreText = stringResource(R.string.pattern_recall_title),
            onReplayClick = ::reset,
            onHomeClick = onBackClick
        )
    }
}

@Composable
private fun RecallCell(
    cell: Int,
    isLit: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier
) {
    Surface(
        color = if (isLit) AccentYellow else NavySurface,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (isLit) AccentYellow else CardBorderColor),
        onClick = onClick,
        enabled = isEnabled,
        modifier = modifier.semantics {
            contentDescription = "Pattern cell ${cell + 1}"
            role = Role.Button
        }
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            if (isLit) Text("•", color = NavyBackground, fontSize = 32.sp, fontWeight = FontWeight.Bold)
        }
    }
}
