package com.tds.puzzivo.core.design

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tds.puzzivo.core.model.DifficultyLevel

/**
 * XP Progress bar with level display
 */
@Composable
fun XPProgressBar(
    currentXP: Long,
    xpForNextLevel: Long,
    currentLevel: Int,
    modifier: Modifier = Modifier
) {
    var animatedProgress by remember { mutableFloatStateOf(0f) }
    val targetProgress = (currentXP.toFloat() / xpForNextLevel).coerceIn(0f, 1f)

    LaunchedEffect(targetProgress) {
        animate(
            initialValue = animatedProgress,
            targetValue = targetProgress,
            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
        ) { value, _ ->
            animatedProgress = value
        }
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentLevel.toString(),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                Text(
                    text = "Level $currentLevel",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "$currentXP / $xpForNextLevel XP",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = animatedProgress,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

/**
 * Circular progress indicator for challenges
 */
@Composable
fun PuzzivoCircularProgressIndicator(
    progress: Float,
    size: androidx.compose.ui.unit.Dp = 120.dp,
    strokeWidth: androidx.compose.ui.unit.Dp = 12.dp,
    content: @Composable BoxScope.() -> Unit
) {
    var animatedProgress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(progress) {
        animate(
            initialValue = animatedProgress,
            targetValue = progress.coerceIn(0f, 1f),
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        ) { value, _ ->
            animatedProgress = value
        }
    }

    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val canvasSize = this.size.minDimension
            val radius = (canvasSize / 2) - strokeWidth.toPx() / 2

            // Background circle
            drawArc(
                color = Color.LightGray.copy(alpha = 0.3f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(
                    x = (this.size.width - canvasSize) / 2,
                    y = (this.size.height - canvasSize) / 2
                ),
                size = Size(canvasSize, canvasSize),
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )

            // Progress arc
            drawArc(
                color = Color(0xFF4CAF50),
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                topLeft = Offset(
                    x = (this.size.width - canvasSize) / 2,
                    y = (this.size.height - canvasSize) / 2
                ),
                size = Size(canvasSize, canvasSize),
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
        }

        content()
    }
}

/**
 * Currency display (coins, gems)
 */
@Composable
fun CurrencyDisplay(
    icon: String,
    amount: Long,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.titleMedium
            )
            Column {
                Text(
                    text = amount.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Difficulty level selector
 */
@Composable
fun DifficultySelector(
    selectedDifficulty: DifficultyLevel,
    onDifficultySelected: (DifficultyLevel) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Select Difficulty",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DifficultyLevel.values().forEach { difficulty ->
                DifficultyOption(
                    difficulty = difficulty,
                    isSelected = difficulty == selectedDifficulty,
                    onClick = { onDifficultySelected(difficulty) }
                )
            }
        }
    }
}

@Composable
private fun DifficultyOption(
    difficulty: DifficultyLevel,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = if (isSelected) {
            when (difficulty) {
                DifficultyLevel.EASY -> Color(0xFF4CAF50)
                DifficultyLevel.MEDIUM -> Color(0xFFFF9800)
                DifficultyLevel.HARD -> Color(0xFFE91E63)
                DifficultyLevel.EXPERT -> Color(0xFF9C27B0)
            }
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = difficulty.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = getDifficultyDescription(difficulty),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "${difficulty.scoreMultiplier}x",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * Streak indicator
 */
@Composable
fun StreakIndicator(
    currentStreak: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFFFF9800).copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "🔥",
                style = MaterialTheme.typography.titleLarge
            )
            Column {
                Text(
                    text = "$currentStreak Day Streak",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF9800)
                )
                Text(
                    text = "Keep it going!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun getDifficultyDescription(difficulty: DifficultyLevel): String {
    return when (difficulty) {
        DifficultyLevel.EASY -> "Perfect for beginners"
        DifficultyLevel.MEDIUM -> "Balanced challenge"
        DifficultyLevel.HARD -> "For experienced players"
        DifficultyLevel.EXPERT -> "Ultimate challenge"
    }
}
