package com.tds.puzzivo.core.design

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.tds.puzzivo.PuzzivoApplication
import com.tds.puzzivo.core.model.GameType
import com.tds.puzzivo.core.manager.GameCompletionRequest
import kotlin.random.Random

/** Records completion for local-state games, including Daily Pick, statistics, and XP/coin rewards. */
@Composable
fun RecordGameCompletionEffect(
    gameType: GameType,
    completed: Boolean,
    score: Int = 150,
    won: Boolean = true,
    durationSeconds: Int = 45,
    moves: Int = 0,
    hintsUsed: Int = 0
) {
    val context = LocalContext.current
    var completionId by rememberSaveable(gameType) { mutableStateOf(java.util.UUID.randomUUID().toString()) }
    var recorded by rememberSaveable(gameType, completionId) { mutableStateOf(false) }
    LaunchedEffect(gameType, completed) {
        if (!completed) {
            recorded = false
            completionId = java.util.UUID.randomUUID().toString()
        } else if (!recorded) {
            recorded = true
            val app = context.applicationContext as? PuzzivoApplication
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                app?.completionRecorder?.record(
                    GameCompletionRequest(
                        completionId = completionId,
                        gameType = gameType,
                        score = score,
                        durationSeconds = durationSeconds,
                        won = won,
                        moves = moves,
                        hintsUsed = hintsUsed
                    )
                )
            }
        }
    }

    if (completed) {
        ConfettiParticleOverlay()
    }
}

@Composable
fun TrackGameStartEffect(gameType: GameType) {
    val context = LocalContext.current
    LaunchedEffect(gameType) {
        val app = context.applicationContext as? PuzzivoApplication
        app?.analyticsManager?.logGameStarted(gameType)
    }
}

private data class Particle(
    val xRatio: Float,
    val initialYRatio: Float,
    val speedY: Float,
    val radius: Float,
    val color: Color
)

@Composable
fun ConfettiParticleOverlay() {
    val progress = remember { Animatable(0f) }
    val particles = remember {
        val colors = listOf(AccentYellow, AccentCyan, AccentGreen, AccentMagenta, AccentOrange)
        List(40) {
            Particle(
                xRatio = Random.nextFloat(),
                initialYRatio = Random.nextFloat() * 0.4f + 0.3f,
                speedY = Random.nextFloat() * 0.5f + 0.5f,
                radius = Random.nextFloat() * 6f + 4f,
                color = colors[Random.nextInt(colors.size)]
            )
        }
    }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1500)
        )
    }

    val alpha = (1f - progress.value).coerceIn(0f, 1f)
    if (alpha > 0f) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            particles.forEach { p ->
                val currentY = height * (p.initialYRatio + (p.speedY * progress.value * 0.6f))
                val currentX = width * p.xRatio
                drawCircle(
                    color = p.color.copy(alpha = alpha),
                    radius = p.radius,
                    center = Offset(currentX, currentY)
                )
            }
        }
    }
}
