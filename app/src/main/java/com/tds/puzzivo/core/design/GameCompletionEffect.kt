package com.tds.puzzivo.core.design

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.tds.puzzivo.PuzzivoApplication
import com.tds.puzzivo.core.model.GameType

/** Records completion for newly added local-state games, including Daily Pick and streak updates. */
@Composable
fun RecordGameCompletionEffect(gameType: GameType, completed: Boolean) {
    val context = LocalContext.current
    var recorded by rememberSaveable(gameType) { mutableStateOf(false) }
    LaunchedEffect(gameType, completed) {
        if (!completed) {
            recorded = false
        } else if (!recorded) {
            recorded = true
            (context.applicationContext as? PuzzivoApplication)
                ?.preferencesRepository
                ?.recordGameCompleted(gameType)
        }
    }
}
