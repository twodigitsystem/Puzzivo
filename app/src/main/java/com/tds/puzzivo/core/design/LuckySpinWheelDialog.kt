package com.tds.puzzivo.core.design

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch
import kotlin.random.Random

data class SpinSector(
    val label: String,
    val icon: String,
    val coins: Int,
    val gems: Int,
    val color: Color
)

@Composable
fun LuckySpinWheelDialog(
    isSpunToday: Boolean,
    onRewardWon: (coins: Int, gems: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val sectors = remember {
        listOf(
            SpinSector("50 Coins", "🪙", 50, 0, Color(0xFF3B82F6)),
            SpinSector("10 Gems", "💎", 0, 10, Color(0xFF06B6D4)),
            SpinSector("100 Coins", "🪙", 100, 0, Color(0xFF22C55E)),
            SpinSector("20 Gems", "💎", 0, 20, Color(0xFFEC4899)),
            SpinSector("250 Coins", "🪙", 250, 0, Color(0xFFF97316)),
            SpinSector("JACKPOT!", "👑", 500, 50, Color(0xFFFACC15))
        )
    }

    val rotationAnim = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var isSpinning by remember { mutableStateOf(false) }
    var wonSector by remember { mutableStateOf<SpinSector?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        PuzzivoCard(
            backgroundColor = NavySurface,
            borderColor = AccentYellow,
            modifier = Modifier.padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🎡 LUCKY SPIN WHEEL",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = AccentYellow,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "Spin once daily to win Coins & Gems!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    contentAlignment = Alignment.TopCenter,
                    modifier = Modifier.size(240.dp)
                ) {
                    Canvas(
                        modifier = Modifier
                            .size(220.dp)
                            .align(Alignment.Center)
                            .rotate(rotationAnim.value)
                    ) {
                        val sweepAngle = 360f / sectors.size
                        sectors.forEachIndexed { i, sector ->
                            drawArc(
                                color = sector.color,
                                startAngle = i * sweepAngle,
                                sweepAngle = sweepAngle,
                                useCenter = true,
                                size = Size(size.width, size.height)
                            )
                        }
                    }

                    Text(
                        text = "🔻",
                        fontSize = 24.sp,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (wonSector != null) {
                    Surface(
                        color = AccentGreen.copy(alpha = 0.2f),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentGreen),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "🎉 WON: ${wonSector?.label}!",
                            color = AccentGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(vertical = 12.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                } else if (!isSpunToday) {
                    PrimaryButton(
                        text = if (isSpinning) "SPINNING..." else "SPIN WHEEL",
                        onClick = {
                            if (isSpinning) return@PrimaryButton
                            isSpinning = true
                            scope.launch {
                                val selectedIndex = Random.nextInt(sectors.size)
                                val sector = sectors[selectedIndex]
                                val sweepAngle = 360f / sectors.size
                                val targetAngle = (360f * 5) + (360f - (selectedIndex * sweepAngle) - (sweepAngle / 2f))

                                rotationAnim.animateTo(
                                    targetValue = targetAngle,
                                    animationSpec = tween(durationMillis = 3500, easing = FastOutSlowInEasing)
                                )

                                isSpinning = false
                                wonSector = sector
                                onRewardWon(sector.coins, sector.gems)
                            }
                        },
                        containerColor = AccentYellow,
                        contentColor = NavyBackground,
                        enabled = !isSpinning
                    )
                } else {
                    Surface(
                        color = NavySurfaceVariant,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "✅ Today's Spin Claimed!",
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(vertical = 12.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                SecondaryButton(
                    text = "Close",
                    onClick = onDismiss
                )
            }
        }
    }
}
