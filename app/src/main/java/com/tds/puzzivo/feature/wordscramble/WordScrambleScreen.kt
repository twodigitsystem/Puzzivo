package com.tds.puzzivo.feature.wordscramble

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import com.tds.puzzivo.core.design.PuzzivoTopBar
import com.tds.puzzivo.core.design.RecordGameCompletionEffect
import com.tds.puzzivo.core.design.ResultDialog
import com.tds.puzzivo.core.design.PrimaryButton
import com.tds.puzzivo.core.design.SecondaryButton
import com.tds.puzzivo.core.design.TextPrimary
import com.tds.puzzivo.core.design.TextSecondary

private val scrambleWords = listOf("PUZZLE", "MEMORY", "NUMBER", "GALAXY", "CIPHER", "BRIGHT")

private data class ScrambleRound(val word: String, val letters: List<Char>) {
    companion object {
        fun create(word: String): ScrambleRound {
            val shuffled = word.toList().shuffled().let { if (it == word.toList()) it.reversed() else it }
            return ScrambleRound(word, shuffled)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WordScrambleScreen(onBackClick: () -> Unit) {
    var wordIndex by remember { mutableIntStateOf(0) }
    var round by remember { mutableStateOf(ScrambleRound.create(scrambleWords[0])) }
    var selectedPositions by remember { mutableStateOf(emptyList<Int>()) }
    var feedback by remember { mutableStateOf<String?>(null) }
    var complete by remember { mutableStateOf(false) }
    var showHelp by remember { mutableStateOf(false) }
    val correctText = stringResource(R.string.word_scramble_correct)
    val wrongText = stringResource(R.string.word_scramble_wrong)

    fun reset() {
        wordIndex = 0
        round = ScrambleRound.create(scrambleWords[0])
        selectedPositions = emptyList()
        feedback = null
        complete = false
    }

    fun select(position: Int) {
        feedback = null
        selectedPositions = if (position in selectedPositions) {
            selectedPositions - position
        } else {
            selectedPositions + position
        }
    }

    fun submit() {
        val answer = selectedPositions.map { round.letters[it] }.joinToString("")
        if (answer == round.word) {
            feedback = correctText
            if (wordIndex == scrambleWords.lastIndex) {
                complete = true
            } else {
                wordIndex += 1
                round = ScrambleRound.create(scrambleWords[wordIndex])
                selectedPositions = emptyList()
            }
        } else {
            feedback = wrongText
            selectedPositions = emptyList()
        }
    }

    RecordGameCompletionEffect(com.tds.puzzivo.core.model.GameType.WORD_SCRAMBLE, complete)

    Surface(color = NavyBackground, modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            PuzzivoTopBar(
                title = stringResource(R.string.word_scramble_title),
                onBackClick = onBackClick,
                onActionClick = { showHelp = true },
                actionContentDescription = stringResource(R.string.action_help)
            )
            Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.word_scramble_round, wordIndex + 1), color = AccentYellow, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = selectedPositions.map { round.letters[it] }.joinToString(" "),
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    round.letters.forEachIndexed { index, letter ->
                        LetterButton(
                            letter = letter,
                            selected = index in selectedPositions,
                            onClick = { select(index) }
                        )
                    }
                }
                feedback?.let {
                    Text(
                        text = it,
                        color = if (it == stringResource(R.string.word_scramble_correct)) AccentGreen else AccentYellow,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 18.dp)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SecondaryButton(
                        text = stringResource(R.string.word_scramble_clear),
                        onClick = { selectedPositions = emptyList(); feedback = null },
                        modifier = Modifier.weight(1f)
                    )
                    PrimaryButton(
                        text = stringResource(R.string.word_scramble_submit),
                        onClick = ::submit,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    if (showHelp) {
        InstructionDialog(
            title = stringResource(R.string.word_scramble_title),
            description = stringResource(R.string.word_scramble_rules),
            onDismiss = { showHelp = false }
        )
    }
    if (complete) {
        ResultDialog(
            title = stringResource(R.string.word_scramble_complete),
            scoreText = stringResource(R.string.word_scramble_round, scrambleWords.size),
            isNewBest = false,
            bestScoreText = stringResource(R.string.word_scramble_title),
            onReplayClick = ::reset,
            onHomeClick = onBackClick
        )
    }
}

@Composable
private fun LetterButton(letter: Char, selected: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (selected) AccentYellow else NavySurface,
        border = BorderStroke(1.dp, if (selected) AccentYellow else CardBorderColor),
        shape = RoundedCornerShape(10.dp),
        onClick = onClick,
        modifier = Modifier.size(54.dp).semantics {
            contentDescription = "Letter $letter"
            role = Role.Button
        }
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(letter.toString(), color = if (selected) NavyBackground else TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
}
