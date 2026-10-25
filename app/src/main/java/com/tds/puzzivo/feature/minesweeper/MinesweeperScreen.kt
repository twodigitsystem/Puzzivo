package com.tds.puzzivo.feature.minesweeper

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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tds.puzzivo.R
import com.tds.puzzivo.core.design.AccentGreen
import com.tds.puzzivo.core.design.AccentMagenta
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

private const val BOARD_SIZE = 6
private const val MINE_COUNT = 8

private data class MineCell(
    val mine: Boolean,
    val adjacent: Int,
    val revealed: Boolean = false,
    val flagged: Boolean = false
)

private data class MineBoard(
    val cells: List<MineCell>,
    val gameOver: Boolean = false,
    val won: Boolean = false
) {
    companion object {
        fun create(): MineBoard {
            val mineIndices = setOf(1, 7, 14, 19, 27, 31, 34, 35)
            val mines = mineIndices.map { it.coerceIn(0, BOARD_SIZE * BOARD_SIZE - 1) }.toSet()
            val cells = List(BOARD_SIZE * BOARD_SIZE) { index ->
                MineCell(mine = index in mines, adjacent = countAdjacent(index, mines))
            }
            return MineBoard(cells)
        }

        private fun countAdjacent(index: Int, mines: Set<Int>): Int {
            val row = index / BOARD_SIZE
            val col = index % BOARD_SIZE
            var count = 0
            for (dr in -1..1) for (dc in -1..1) {
                if (dr == 0 && dc == 0) continue
                val r = row + dr
                val c = col + dc
                if (r in 0 until BOARD_SIZE && c in 0 until BOARD_SIZE && r * BOARD_SIZE + c in mines) count++
            }
            return count
        }
    }
}

@Composable
fun MinesweeperScreen(onBackClick: () -> Unit) {
    var board by remember { mutableStateOf(MineBoard.create()) }
    var flagMode by remember { mutableStateOf(false) }
    var showHelp by remember { mutableStateOf(false) }

    fun reset() {
        board = MineBoard.create()
        flagMode = false
    }

    RecordGameCompletionEffect(com.tds.puzzivo.core.model.GameType.MINESWEEPER, board.won)

    Surface(color = NavyBackground, modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            PuzzivoTopBar(
                title = stringResource(R.string.minesweeper_title),
                onBackClick = onBackClick,
                onActionClick = { showHelp = true },
                actionContentDescription = stringResource(R.string.action_help)
            )
            Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                val flags = board.cells.count { it.flagged }
                Text(stringResource(R.string.minesweeper_mines_left, MINE_COUNT - flags), color = AccentYellow, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(14.dp))
                PuzzivoCard(
                    backgroundColor = NavySurfaceVariant,
                    borderColor = CardBorderColor,
                    modifier = Modifier.fillMaxWidth().aspectRatio(1f)
                ) {
                    Column(modifier = Modifier.fillMaxSize().padding(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        repeat(BOARD_SIZE) { row ->
                            Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                repeat(BOARD_SIZE) { col ->
                                    val index = row * BOARD_SIZE + col
                                    val cell = board.cells[index]
                                    MineCellView(
                                        row = row,
                                        col = col,
                                        cell = cell,
                                        showMines = board.gameOver,
                                        onClick = {
                                            board = if (flagMode) toggleFlag(board, index) else revealCell(board, index)
                                        },
                                        modifier = Modifier.weight(1f).fillMaxSize()
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SecondaryButton(
                        text = if (flagMode) stringResource(R.string.minesweeper_reveal) else stringResource(R.string.minesweeper_flag),
                        onClick = { flagMode = !flagMode },
                        modifier = Modifier.weight(1f)
                    )
                    PrimaryButton(text = stringResource(R.string.action_restart), onClick = ::reset, modifier = Modifier.weight(1f))
                }
            }
        }
    }

    if (showHelp) {
        InstructionDialog(
            title = stringResource(R.string.minesweeper_title),
            description = stringResource(R.string.minesweeper_rules),
            onDismiss = { showHelp = false }
        )
    }
    if (board.won || board.gameOver) {
        ResultDialog(
            title = stringResource(if (board.won) R.string.minesweeper_won else R.string.minesweeper_lost),
            scoreText = stringResource(R.string.minesweeper_mines_left, board.cells.count { it.flagged }),
            isNewBest = false,
            bestScoreText = stringResource(R.string.minesweeper_title),
            onReplayClick = ::reset,
            onHomeClick = onBackClick
        )
    }
}

private fun toggleFlag(board: MineBoard, index: Int): MineBoard {
    if (board.gameOver || board.won) return board
    val cell = board.cells[index]
    if (cell.revealed) return board
    if (!cell.flagged && board.cells.count { it.flagged } >= MINE_COUNT) return board
    val cells = board.cells.toMutableList()
    cells[index] = cell.copy(flagged = !cell.flagged)
    return board.copy(cells = cells)
}

private fun revealCell(board: MineBoard, index: Int): MineBoard {
    if (board.gameOver || board.won) return board
    if (index !in board.cells.indices || board.cells[index].flagged || board.cells[index].revealed) return board
    val cells = board.cells.toMutableList()
    if (cells[index].mine) {
        cells.indices.filter { cells[it].mine }.forEach { mineIndex ->
            cells[mineIndex] = cells[mineIndex].copy(revealed = true)
        }
        return board.copy(cells = cells, gameOver = true)
    }

    val queue = ArrayDeque<Int>()
    queue.add(index)
    while (queue.isNotEmpty()) {
        val current = queue.removeFirst()
        if (current !in cells.indices || cells[current].revealed || cells[current].flagged || cells[current].mine) continue
        cells[current] = cells[current].copy(revealed = true)
        if (cells[current].adjacent == 0) {
            val row = current / BOARD_SIZE
            val col = current % BOARD_SIZE
            for (dr in -1..1) for (dc in -1..1) {
                val r = row + dr
                val c = col + dc
                if (r in 0 until BOARD_SIZE && c in 0 until BOARD_SIZE) queue.add(r * BOARD_SIZE + c)
            }
        }
    }
    val safeRevealed = cells.count { it.revealed && !it.mine }
    val safeTotal = cells.count { !it.mine }
    return board.copy(cells = cells, won = safeRevealed == safeTotal)
}

@Composable
private fun MineCellView(
    row: Int,
    col: Int,
    cell: MineCell,
    showMines: Boolean,
    onClick: () -> Unit,
    modifier: Modifier
) {
    val visibleMine = cell.mine && (cell.revealed || showMines)
    val label = when {
        visibleMine -> stringResource(R.string.minesweeper_mine)
        cell.flagged -> stringResource(R.string.minesweeper_flagged)
        !cell.revealed -> stringResource(R.string.minesweeper_hidden)
        else -> pluralStringResource(R.plurals.minesweeper_number, cell.adjacent, cell.adjacent)
    }
    val cellDescription = stringResource(R.string.minesweeper_cell, row + 1, col + 1, label)
    Surface(
        color = when {
            visibleMine -> AccentMagenta.copy(alpha = 0.5f)
            cell.revealed -> NavySurface
            cell.flagged -> AccentYellow.copy(alpha = 0.35f)
            else -> NavySurfaceVariant
        },
        shape = RoundedCornerShape(5.dp),
        border = BorderStroke(1.dp, CardBorderColor),
        onClick = onClick,
        modifier = modifier.semantics {
            contentDescription = cellDescription
            role = Role.Button
        }
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                text = when {
                    visibleMine -> "✕"
                    cell.flagged -> "⚑"
                    cell.revealed && cell.adjacent > 0 -> cell.adjacent.toString()
                    else -> ""
                },
                color = if (cell.flagged) NavyBackground else if (visibleMine) AccentMagenta else AccentGreen,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }
}
