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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tds.puzzivo.R
import com.tds.puzzivo.core.design.AccentCyan
import com.tds.puzzivo.core.design.AccentGreen
import com.tds.puzzivo.core.design.CardBorderColor
import com.tds.puzzivo.core.design.InstructionDialog
import com.tds.puzzivo.core.design.NavyBackground
import com.tds.puzzivo.core.design.NavySurface
import com.tds.puzzivo.core.design.NavySurfaceVariant
import com.tds.puzzivo.core.design.PrimaryButton
import com.tds.puzzivo.core.design.PuzzivoCard
import com.tds.puzzivo.core.design.PuzzivoTopBar
import com.tds.puzzivo.core.design.RecordGameCompletionEffect
import com.tds.puzzivo.core.design.TrackGameStartEffect
import com.tds.puzzivo.core.design.ResultDialog
import com.tds.puzzivo.core.design.SecondaryButton
import com.tds.puzzivo.core.design.TextSecondary
import com.tds.puzzivo.core.util.booleanListSaver

private const val SIZE = 5

private val nonogramPatterns = listOf(
    listOf(0, 1, 0, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 0, 0, 0, 1, 0, 0),
    listOf(0, 1, 0, 1, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 1, 1, 1, 0),
    listOf(0, 0, 1, 0, 0, 0, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 0, 1, 0, 1, 1, 0, 1, 0, 1),
    listOf(0, 0, 1, 0, 0, 1, 1, 1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 0, 0, 0, 1)
)

/** Converts a 0/1 row/column pattern into space-separated block-count strings (e.g. [0,1,1,0,1] -> "2 1"). */
private fun parseClues(pattern: List<Int>): String {
    val asString = pattern.joinToString("") { it.toString() }
    val blocks = asString.split('0').filter { it.isNotBlank() }
    return blocks.map { it.length.toString() }.joinToString(" ")
}

/** Returns the row clues and column clues for a given pattern index. */
private fun computeClues(patternIndex: Int): Pair<List<String>, List<String>> {
    val pattern = nonogramPatterns[patternIndex]
    val rowClues = pattern.chunked(SIZE).map { parseClues(it) }
    val columnClues = (0 until SIZE).map { col ->
        parseClues((0 until SIZE).map { row -> pattern[row * SIZE + col] })
    }
    return rowClues to columnClues
}

@Composable
fun NonogramScreen(onBackClick: () -> Unit) {
    var patternIndex by rememberSaveable { mutableIntStateOf(0) }
    val solution = nonogramPatterns[patternIndex % nonogramPatterns.size]
    var filled by rememberSaveable(stateSaver = booleanListSaver) {
        mutableStateOf(List(SIZE * SIZE) { false })
    }
    var markedEmpty by rememberSaveable(stateSaver = booleanListSaver) {
        mutableStateOf(List(SIZE * SIZE) { false })
    }
    var markMode by rememberSaveable { mutableStateOf(false) }
    var showHelp by remember { mutableStateOf(false) }
    var complete by rememberSaveable { mutableStateOf(false) }

    fun reset() {
        patternIndex = (patternIndex + 1) % nonogramPatterns.size
        filled = List(SIZE * SIZE) { false }
        markedEmpty = List(SIZE * SIZE) { false }
        markMode = false
        complete = false
    }

    fun patternDescription(): String {
        return "Pattern ${patternIndex + 1} of ${nonogramPatterns.size}. Fill rows matching the side clues and columns matching the top clues."
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

    TrackGameStartEffect(com.tds.puzzivo.core.model.GameType.NONOGRAM)
    RecordGameCompletionEffect(
        gameType = com.tds.puzzivo.core.model.GameType.NONOGRAM,
        completed = complete,
        score = 500,
        moves = filled.count { it }
    )

    val (rowClues, columnClues) = computeClues(patternIndex)

    Surface(color = NavyBackground, modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            PuzzivoTopBar(
                title = stringResource(R.string.nonogram_title),
                onBackClick = onBackClick,
                onActionClick = { showHelp = true },
                actionContentDescription = stringResource(R.string.action_help)
            )
            Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(patternDescription(), color = TextSecondary, style = MaterialTheme.typography.bodySmall)
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
                                Text(rowClues[row], color = AccentCyan, fontWeight = FontWeight.Bold, modifier = Modifier.width(50.dp), textAlign = androidx.compose.ui.text.style.TextAlign.End)
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
