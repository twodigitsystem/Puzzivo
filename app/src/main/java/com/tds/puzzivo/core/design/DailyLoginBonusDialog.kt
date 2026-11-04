package com.tds.puzzivo.core.design

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun DailyLoginBonusDialog(
    currentDay: Int,
    isClaimedToday: Boolean,
    onClaimClick: () -> Unit,
    onDismiss: () -> Unit
) {
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
                Text(text = "🎁", fontSize = 42.sp)

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "DAILY REWARD CHEST",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = AccentYellow,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "Log in daily to unlock bigger rewards!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(18.dp))

                // 7-day progression grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (day in 1..7) {
                        val isCurrent = day == currentDay
                        val isPast = day < currentDay || (isClaimedToday && isCurrent)

                        DayRewardTile(
                            day = day,
                            isCurrent = isCurrent && !isClaimedToday,
                            isClaimed = isPast,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                if (!isClaimedToday) {
                    PrimaryButton(
                        text = "CLAIM DAY $currentDay REWARD",
                        onClick = onClaimClick,
                        containerColor = AccentYellow,
                        contentColor = NavyBackground
                    )
                } else {
                    Surface(
                        color = AccentGreen.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, AccentGreen),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "✅ Today's Reward Claimed!",
                            color = AccentGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(vertical = 12.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                SecondaryButton(
                    text = "Close",
                    onClick = onDismiss
                )
            }
        }
    }
}

@Composable
private fun DayRewardTile(
    day: Int,
    isCurrent: Boolean,
    isClaimed: Boolean,
    modifier: Modifier = Modifier
) {
    val (rewardIcon, rewardVal) = when (day) {
        1 -> Pair("🪙", "50")
        2 -> Pair("🪙", "75")
        3 -> Pair("💎", "10")
        4 -> Pair("🪙", "150")
        5 -> Pair("💎", "20")
        6 -> Pair("🪙", "300")
        7 -> Pair("🎁", "500")
        else -> Pair("🪙", "50")
    }

    val (bg, border) = when {
        isCurrent -> Pair(AccentYellow.copy(alpha = 0.25f), AccentYellow)
        isClaimed -> Pair(AccentGreen.copy(alpha = 0.15f), AccentGreen)
        else -> Pair(NavySurfaceVariant, CardBorderColor)
    }

    Surface(
        color = bg,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, border),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "D$day",
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isCurrent) AccentYellow else TextSecondary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (isClaimed) "✓" else rewardIcon,
                fontSize = 14.sp
            )
            Text(
                text = rewardVal,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1
            )
        }
    }
}
