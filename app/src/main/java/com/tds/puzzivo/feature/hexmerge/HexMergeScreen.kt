package com.tds.puzzivo.feature.hexmerge

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.tds.puzzivo.core.design.TextPrimary
import kotlin.random.Random

private data class HexCoord(val q: Int, val r: Int)
private enum class HexDirection(val dq: Int, val dr: Int, val label: String) {
    EAST(1, 0, "→"),
    SOUTH_EAST(0, 1, "↘"),
    SOUTH_WEST(-1, 1, "↙"),
    WEST(-1, 0, "←"),
    NORTH_WEST(0, -1, "↖"),
    NORTH_EAST(1, -1, "↗")
}

private val hexCoords = buildList {
    for (r in -2..2) {
        val minQ = maxOf(-2, -r - 2)
        val maxQ = minOf(2, -r + 2)
        for (q in minQ..maxQ) add(HexCoord(q, r))
    }
}

private data class HexBoard(val values: Map<HexCoord, Int>, val score: Int, val gameOver: Boolean) {
    companion object {
        fun create(): HexBoard {
            var board = HexBoard(emptyMap(), 0, false)
            board = spawn(board)
            return spawn(board)
        }
    }
}

@Composable
fun HexMergeScreen(onBackClick: () -> Unit) {
    var board by remember { mutableStateOf(HexBoard.create()) }
    var showHelp by remember { mutableStateOf(false) }

    fun reset() { board = HexBoard.create() }

    fun move(direction: HexDirection) {
        board = moveBoard(board, direction)
    }

    RecordGameCompletionEffect(com.tds.puzzivo.core.model.GameType.HEX_MERGE, board.gameOver)

    Surface(color = NavyBackground, modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            PuzzivoTopBar(
                title = stringResource(R.string.hex_merge_title),
                onBackClick = onBackClick,
                onActionClick = { showHelp = true },
                actionContentDescription = stringResource(R.string.action_help)
            )
            Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.hex_merge_score, board.score), color = AccentYellow, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                PuzzivoCard(backgroundColor = NavySurfaceVariant, borderColor = CardBorderColor, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (r in -2..2) {
                            val row = hexCoords.filter { it.r == r }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                row.forEach { coord ->
                                    HexCell(value = board.values[coord] ?: 0)
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HexDirection.values().take(3).forEach { direction -> DirectionButton(direction.label) { move(direction) } }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HexDirection.values().drop(3).forEach { direction -> DirectionButton(direction.label) { move(direction) } }
                }
                Spacer(modifier = Modifier.height(12.dp))
                PrimaryButton(text = stringResource(R.string.action_restart), onClick = ::reset)
            }
        }
    }

    if (showHelp) {
        InstructionDialog(title = stringResource(R.string.hex_merge_title), description = stringResource(R.string.hex_merge_rules), onDismiss = { showHelp = false })
    }
    if (board.gameOver) {
        ResultDialog(
            title = stringResource(R.string.hex_merge_game_over),
            scoreText = stringResource(R.string.hex_merge_score, board.score),
            isNewBest = false,
            bestScoreText = stringResource(R.string.hex_merge_title),
            onReplayClick = ::reset,
            onHomeClick = onBackClick
        )
    }
}

@Composable
private fun HexCell(value: Int) {
    val color = if (value == 0) NavySurface else AccentGreen.copy(alpha = 0.2f)
    Surface(
        color = color,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, if (value == 0) CardBorderColor else AccentGreen),
        modifier = Modifier.size(52.dp).semantics {
            contentDescription = if (value == 0) "Empty hex cell" else "Hex tile $value"
        }
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            if (value > 0) Text(value.toString(), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
private fun DirectionButton(label: String, onClick: () -> Unit) {
    Surface(color = NavySurface, shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, CardBorderColor), onClick = onClick, modifier = Modifier.size(48.dp)) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(label, color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
}

private fun spawn(board: HexBoard): HexBoard {
    val empty = hexCoords.filter { it !in board.values }
    if (empty.isEmpty()) return board
    val coord = empty[Random.nextInt(empty.size)]
    return board.copy(values = board.values + (coord to if (Random.nextFloat() < 0.9f) 2 else 4))
}

private fun moveBoard(board: HexBoard, direction: HexDirection): HexBoard {
    if (board.gameOver) return board
    val vector = HexCoord(direction.dq, direction.dr)
    val lines = hexCoords.filter { HexCoord(it.q - vector.q, it.r - vector.r) !in hexCoords }
        .map { start ->
            buildList {
                var current = start
                while (current in hexCoords) {
                    add(current)
                    current = HexCoord(current.q + vector.q, current.r + vector.r)
                }
            }
        }
    val result = board.values.toMutableMap()
    var changed = false
    var gained = 0
    lines.forEach { line ->
        val values = line.mapNotNull { board.values[it] }
        val merged = mutableListOf<Int>()
        var index = 0
        while (index < values.size) {
            if (index + 1 < values.size && values[index] == values[index + 1]) {
                val value = values[index] * 2
                merged += value
                gained += value
                index += 2
            } else {
                merged += values[index]
                index++
            }
        }
        line.forEach { result.remove(it) }
        merged.forEachIndexed { offset, value -> result[line[offset]] = value }
        if (line.map { board.values[it] ?: 0 } != line.map { result[it] ?: 0 }) changed = true
    }
    if (!changed) return board.copy(gameOver = !hasMoves(board.values))
    val spawned = spawn(board.copy(values = result, score = board.score + gained))
    return spawned.copy(gameOver = !hasMoves(spawned.values))
}

private fun hasMoves(values: Map<HexCoord, Int>): Boolean {
    if (values.size < hexCoords.size) return true
    return hexCoords.any { coord ->
        HexDirection.values().any { direction ->
            val neighbor = HexCoord(coord.q + direction.dq, coord.r + direction.dr)
            neighbor in values && values[neighbor] == values[coord]
        }
    }
}
