package com.tds.puzzivo.feature.sudoku

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

private data class SudokuPuzzle(
    val solution: List<Int>,
    val givens: Set<Int>
) {
    companion object {
        fun create(variant: Int = 0): SudokuPuzzle {
            val base = listOf(
                1, 2, 3, 4, 5, 6,
                4, 5, 6, 1, 2, 3,
                2, 3, 4, 5, 6, 1,
                5, 6, 1, 2, 3, 4,
                3, 4, 5, 6, 1, 2,
                6, 1, 2, 3, 4, 5
            )
            val shift = variant % 6
            return SudokuPuzzle(
                solution = base.map { number -> ((number - 1 + shift) % 6) + 1 },
                givens = setOf(0, 2, 3, 5, 6, 7, 9, 11, 12, 14, 17, 18, 20, 21, 23, 26, 28, 29)
            )
        }
    }
}

@Composable
fun SudokuScreen(onBackClick: () -> Unit) {
    var puzzle by remember { mutableStateOf(SudokuPuzzle.create()) }
    var values by remember { mutableStateOf(List(36) { index -> if (index in puzzle.givens) puzzle.solution[index] else 0 }) }
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var mistakes by remember { mutableIntStateOf(0) }
    var puzzleNumber by remember { mutableIntStateOf(0) }
    var showHelp by remember { mutableStateOf(false) }
    var showResult by remember { mutableStateOf(false) }

    fun resetPuzzle() {
        puzzleNumber += 1
        puzzle = SudokuPuzzle.create(puzzleNumber)
        values = List(36) { index -> if (index in puzzle.givens) puzzle.solution[index] else 0 }
        selectedIndex = null
        mistakes = 0
        showResult = false
    }

    fun fill(value: Int) {
        val index = selectedIndex ?: return
        if (index in puzzle.givens || values[index] != 0) return
        if (puzzle.solution[index] == value) {
            values = values.toMutableList().also { it[index] = value }
            if (values.withIndex().all { (cell, number) -> number == puzzle.solution[cell] }) {
                showResult = true
            }
        } else {
            mistakes += 1
        }
    }

    fun applyHint() {
        val index = values.indexOfFirst { it == 0 && it !in puzzle.givens }
        if (index >= 0) {
            values = values.toMutableList().also { it[index] = puzzle.solution[index] }
            selectedIndex = index
            if (values.withIndex().all { (cell, number) -> number == puzzle.solution[cell] }) {
                showResult = true
            }
        }
    }

    RecordGameCompletionEffect(com.tds.puzzivo.core.model.GameType.MINI_SUDOKU, showResult)

    Surface(color = NavyBackground, modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            PuzzivoTopBar(
                title = stringResource(R.string.sudoku_title),
                onBackClick = onBackClick,
                onActionClick = { showHelp = true },
                actionContentDescription = stringResource(R.string.action_help)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.sudoku_mistakes, mistakes),
                    color = if (mistakes == 0) TextSecondary else AccentYellow,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                PuzzivoCard(
                    backgroundColor = NavySurfaceVariant,
                    borderColor = CardBorderColor,
                    modifier = Modifier.fillMaxWidth().aspectRatio(1f)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        repeat(6) { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth().weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                repeat(6) { col ->
                                    val index = row * 6 + col
                                    SudokuCell(
                                        row = row,
                                        col = col,
                                        value = values[index],
                                        isGiven = index in puzzle.givens,
                                        isSelected = selectedIndex == index,
                                        onClick = { selectedIndex = index },
                                        modifier = Modifier.weight(1f).fillMaxSize()
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (1..6).forEach { value ->
                        NumberButton(number = value, onClick = { fill(value) })
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SecondaryButton(
                        text = stringResource(R.string.sudoku_clear),
                        onClick = {
                            val index = selectedIndex
                            if (index != null && index !in puzzle.givens) {
                                values = values.toMutableList().also { it[index] = 0 }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    PrimaryButton(
                        text = stringResource(R.string.sudoku_hint),
                        onClick = ::applyHint,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                SecondaryButton(text = stringResource(R.string.sudoku_new_puzzle), onClick = ::resetPuzzle)
            }
        }
    }

    if (showHelp) {
        InstructionDialog(
            title = stringResource(R.string.sudoku_title),
            description = stringResource(R.string.sudoku_rules),
            onDismiss = { showHelp = false }
        )
    }
    if (showResult) {
        ResultDialog(
            title = stringResource(R.string.sudoku_complete),
            scoreText = stringResource(R.string.sudoku_mistakes, mistakes),
            isNewBest = false,
            bestScoreText = stringResource(R.string.sudoku_complete),
            onReplayClick = ::resetPuzzle,
            onHomeClick = onBackClick
        )
    }
}

@Composable
private fun SudokuCell(
    row: Int,
    col: Int,
    value: Int,
    isGiven: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier
) {
    val description = if (value == 0) {
        stringResource(R.string.sudoku_cell, row + 1, col + 1, stringResource(R.string.sudoku_empty))
    } else {
        stringResource(R.string.sudoku_cell, row + 1, col + 1, stringResource(R.string.sudoku_filled, value))
    }
    Surface(
        color = when {
            isSelected -> AccentYellow.copy(alpha = 0.5f)
            isGiven -> NavySurfaceVariant
            value != 0 -> AccentGreen.copy(alpha = 0.22f)
            else -> NavySurface
        },
        shape = RoundedCornerShape(5.dp),
        border = BorderStroke(1.dp, if (isSelected) AccentYellow else CardBorderColor),
        onClick = onClick,
        modifier = modifier.semantics {
            contentDescription = description
            role = Role.Button
        }
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                text = if (value == 0) "" else value.toString(),
                color = if (isSelected) NavyBackground else TextPrimary,
                fontWeight = if (isGiven) FontWeight.Bold else FontWeight.SemiBold,
                fontSize = 20.sp
            )
        }
    }
}

@Composable
private fun NumberButton(number: Int, onClick: () -> Unit) {
    Surface(
        color = NavySurface,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, CardBorderColor),
        onClick = onClick,
        modifier = Modifier.size(44.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(text = number.toString(), color = TextPrimary, fontWeight = FontWeight.Bold)
        }
    }
}
