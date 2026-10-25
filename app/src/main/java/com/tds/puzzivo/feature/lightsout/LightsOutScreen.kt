package com.tds.puzzivo.feature.lightsout

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.tds.puzzivo.core.design.AccentCyan
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

private const val GRID = 5

@Composable
fun LightsOutScreen(onBackClick: () -> Unit) {
    var lights by remember { mutableStateOf(createLightsOutBoard()) }
    var moves by remember { mutableIntStateOf(0) }
    var showHelp by remember { mutableStateOf(false) }
    var complete by remember { mutableStateOf(false) }

    fun reset() {
        lights = createLightsOutBoard()
        moves = 0
        complete = false
    }

    fun tap(index: Int) {
        if (complete) return
        lights = toggleLights(lights, index)
        moves += 1
        complete = lights.none { it }
    }

    RecordGameCompletionEffect(com.tds.puzzivo.core.model.GameType.LIGHTS_OUT, complete)

    Surface(color = NavyBackground, modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            PuzzivoTopBar(
                title = stringResource(R.string.lights_out_title),
                onBackClick = onBackClick,
                onActionClick = { showHelp = true },
                actionContentDescription = stringResource(R.string.action_help)
            )
            Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.lights_out_moves, moves), color = AccentYellow, fontWeight = FontWeight.Bold)
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(14.dp))
                PuzzivoCard(
                    backgroundColor = NavySurfaceVariant,
                    borderColor = CardBorderColor,
                    modifier = Modifier.fillMaxWidth().aspectRatio(1f)
                ) {
                    Column(modifier = Modifier.fillMaxSize().padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        repeat(GRID) { row ->
                            Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                repeat(GRID) { col ->
                                    val index = row * GRID + col
                                    Surface(
                                        color = if (lights[index]) AccentYellow else NavySurface,
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, if (lights[index]) AccentYellow else CardBorderColor),
                                        onClick = { tap(index) },
                                        modifier = Modifier.weight(1f).fillMaxSize().semantics {
                                            contentDescription = "Light row ${row + 1}, column ${col + 1}"
                                            role = Role.Button
                                        }
                                    ) {
                                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                            Text(if (lights[index]) "●" else "", color = NavyBackground, fontSize = 24.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
                PrimaryButton(text = stringResource(R.string.action_restart), onClick = ::reset)
            }
        }
    }

    if (showHelp) {
        InstructionDialog(
            title = stringResource(R.string.lights_out_title),
            description = stringResource(R.string.lights_out_rules),
            onDismiss = { showHelp = false }
        )
    }
    if (complete) {
        ResultDialog(
            title = stringResource(R.string.lights_out_complete),
            scoreText = stringResource(R.string.lights_out_moves, moves),
            isNewBest = false,
            bestScoreText = stringResource(R.string.lights_out_title),
            onReplayClick = ::reset,
            onHomeClick = onBackClick
        )
    }
}

private fun createLightsOutBoard(): List<Boolean> {
    var board = List(GRID * GRID) { false }
    listOf(2, 7, 12, 18, 22).forEach { board = toggleLights(board, it) }
    return board
}

private fun toggleLights(board: List<Boolean>, index: Int): List<Boolean> {
    val row = index / GRID
    val col = index % GRID
    val result = board.toMutableList()
    listOf(row to col, row - 1 to col, row + 1 to col, row to col - 1, row to col + 1).forEach { (r, c) ->
        if (r in 0 until GRID && c in 0 until GRID) {
            val target = r * GRID + c
            result[target] = !result[target]
        }
    }
    return result
}
