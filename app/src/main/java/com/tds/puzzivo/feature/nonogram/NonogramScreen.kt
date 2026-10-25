package com.tds.puzzivo.feature.nonogram

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.tds.puzzivo.core.design.SecondaryButton
import com.tds.puzzivo.core.design.TextPrimary
import com.tds.puzzivo.core.design.TextSecondary

private const val SIZE = 5

@Composable
fun NonogramScreen(onBackClick: () -> Unit) {
    val solution = remember { listOf(0, 1, 0, 1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1, 0, 1, 0) }
    var filled by remember { mutableStateOf(List(SIZE * SIZE) { false }) }
    var markedEmpty by remember { mutableStateOf(List(SIZE * SIZE) { false }) }
    var markMode by remember { mutableStateOf(false) }
    var showHelp by remember { mutableStateOf(false) }
    var complete by remember { mutableStateOf(false) }

    fun reset() {
        filled = List(SIZE * SIZE) { false }
        markedEmpty = List(SIZE * SIZE) { false }
        markMode = false
        complete = false
    }

    fun tap(index: Int) {
        if (complete) return
        if (markMode) {
            markedEmpty = markedEmpty.toMutableList().also { it[index] = !it[index] }
            filled = filled.toMutableList().also { it[index] = false }
        } else {
            filled = filled.toMutableList().also { it[index] = !it[index] }
            markedEmpty = markedEmpty.toMutableList().also { it[index] = false }
            if (filled.map { if (it) 1 else 0 } == solution) complete = true
        }
    }

    RecordGameCompletionEffect(com.tds.puzzivo.core.model.GameType.NONOGRAM, complete)

    val rowClues = listOf("1 1", "5", "3", "5", "1 1")
    val columnClues = listOf("1 1", "5", "3", "5", "1 1")

    Surface(color = NavyBackground, modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            PuzzivoTopBar(
                title = stringResource(R.string.nonogram_title),
                onBackClick = onBackClick,
                onActionClick = { showHelp = true },
                actionContentDescription = stringResource(R.string.action_help)
            )
            Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.nonogram_rules), color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Spacer(modifier = Modifier.width(54.dp))
                    columnClues.forEach { clue ->
                        Text(clue, color = AccentCyan, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                PuzzivoCard(backgroundColor = NavySurfaceVariant, borderColor = CardBorderColor, modifier = Modifier.fillMaxWidth().aspectRatio(1.15f)) {
                    Column(modifier = Modifier.fillMaxSize().padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        repeat(SIZE) { row ->
                            Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(rowClues[row], color = AccentCyan, fontWeight = FontWeight.Bold, modifier = Modifier.width(46.dp), textAlign = androidx.compose.ui.text.style.TextAlign.End)
                                repeat(SIZE) { col ->
                                    val index = row * SIZE + col
                                    Surface(
                                        color = when {
                                            filled[index] -> AccentGreen
                                            markedEmpty[index] -> NavySurfaceVariant
                                            else -> NavySurface
                                        },
                                        shape = RoundedCornerShape(5.dp),
                                        border = BorderStroke(1.dp, CardBorderColor),
                                        onClick = { tap(index) },
                                        modifier = Modifier.weight(1f).fillMaxSize().semantics {
                                            contentDescription = "Nonogram row ${row + 1}, column ${col + 1}"
                                            role = Role.Button
                                        }
                                    ) {
                                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                            if (markedEmpty[index]) Text("×", color = TextSecondary, fontSize = 18.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SecondaryButton(text = stringResource(if (markMode) R.string.nonogram_fill else R.string.nonogram_mark_empty), onClick = { markMode = !markMode }, modifier = Modifier.weight(1f))
                    PrimaryButton(text = stringResource(R.string.nonogram_reset), onClick = ::reset, modifier = Modifier.weight(1f))
                }
            }
        }
    }

    if (showHelp) {
        InstructionDialog(title = stringResource(R.string.nonogram_title), description = stringResource(R.string.nonogram_rules), onDismiss = { showHelp = false })
    }
    if (complete) {
        ResultDialog(
            title = stringResource(R.string.nonogram_complete),
            scoreText = stringResource(R.string.nonogram_title),
            isNewBest = false,
            bestScoreText = stringResource(R.string.nonogram_complete),
            onReplayClick = ::reset,
            onHomeClick = onBackClick
        )
    }
}
