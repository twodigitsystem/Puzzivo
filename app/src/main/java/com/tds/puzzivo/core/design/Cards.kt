package com.tds.puzzivo.core.design

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tds.puzzivo.core.model.Achievement
import com.tds.puzzivo.core.model.AchievementRarity
import com.tds.puzzivo.core.model.DailyChallenge
import com.tds.puzzivo.core.model.DifficultyLevel
import com.tds.puzzivo.core.model.GameStatistics
import com.tds.puzzivo.core.model.GameTheme
import com.tds.puzzivo.core.util.formatTime

/**
 * Card displaying achievement details in Puzzivo Dark Theme
 */
@Composable
fun AchievementCard(
    modifier: Modifier = Modifier,
    achievement: Achievement,
    onClick: () -> Unit = {}
) {
    val isLocked = !achievement.unlocked
    val rarityColor = Color(achievement.rarity.displayColor)

    PuzzivoCard(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (isLocked) 0.65f else 1f),
        backgroundColor = NavySurface,
        borderColor = if (isLocked) CardBorderColor else rarityColor.copy(alpha = 0.6f),
        onClick = if (isLocked) null else onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon with rarity background
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        color = rarityColor.copy(alpha = if (isLocked) 0.15f else 0.25f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = rarityColor.copy(alpha = if (isLocked) 0.3f else 0.8f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isLocked) "🔒" else "🏆",
                    fontSize = 24.sp
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(achievement.titleRes),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    RarityBadge(rarity = achievement.rarity)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = stringResource(achievement.descriptionRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )

                if (!isLocked && achievement.unlockedDate != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Unlocked ${achievement.unlockedDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = AccentGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Progress bar for achievements with progress tracking
                if (isLocked && achievement.requirement > 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                    val progressRatio = (achievement.progress.toFloat() / achievement.requirement).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { progressRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = AccentYellow,
                        trackColor = NavySurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${achievement.progress} / ${achievement.requirement}",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

/**
 * Card displaying daily challenge
 */
@Composable
fun ChallengeCard(
    challenge: DailyChallenge,
    onStart: () -> Unit,
    modifier: Modifier = Modifier
) {
    PuzzivoCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = NavySurface,
        borderColor = if (challenge.isCompleted) AccentGreen else CardBorderColor
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Daily Challenge",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Surface(
                    color = AccentYellow.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, AccentYellow)
                ) {
                    Text(
                        text = challenge.gameType.name.replace('_', ' '),
                        color = AccentYellow,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(challenge.descriptionRes),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Rewards
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RewardBadge(icon = "⭐", label = "+${challenge.rewardXP} XP")
                RewardBadge(icon = "🪙", label = "+${challenge.rewardCoins} Coins")
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (challenge.isCompleted) {
                Surface(
                    color = AccentGreen.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, AccentGreen),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "✅ Challenge Completed",
                        color = AccentGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 10.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                PrimaryButton(
                    text = "Start Challenge",
                    onClick = onStart
                )
            }
        }
    }
}

/**
 * Card displaying game statistics
 */
@Composable
fun StatisticsCard(
    statistics: GameStatistics,
    modifier: Modifier = Modifier
) {
    PuzzivoCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = NavySurface,
        borderColor = CardBorderColor
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = statistics.gameType.name.replace('_', ' '),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Surface(
                    color = AccentCyan.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, AccentCyan)
                ) {
                    Text(
                        text = "${statistics.totalPlays} Games",
                        color = AccentCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(label = "Played", value = statistics.totalPlays.toString(), accentColor = TextPrimary)
                StatItem(label = "Won", value = statistics.totalWins.toString(), accentColor = AccentGreen)
                StatItem(
                    label = "Win Rate",
                    value = if (statistics.totalPlays > 0)
                        "${kotlin.math.round(statistics.totalWins * 100.0 / statistics.totalPlays).toInt()}%"
                    else "0%",
                    accentColor = AccentYellow
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(label = "Best Score", value = statistics.highestScore.toString(), accentColor = AccentCyan)
                StatItem(label = "Play Time", value = formatTime(statistics.totalTimePlayedSeconds), accentColor = TextPrimary)
                StatItem(label = "Streak", value = "${statistics.currentWinStreak} 🔥", accentColor = AccentOrange)
            }
        }
    }
}

/**
 * Theme selection card
 */
@Composable
fun ThemeCard(
    theme: GameTheme,
    isUnlocked: Boolean,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themePrimary = Color(theme.primaryColor)

    PuzzivoCard(
        modifier = modifier
            .alpha(if (isUnlocked) 1f else 0.6f),
        backgroundColor = if (isSelected) NavySurfaceVariant else NavySurface,
        borderColor = if (isSelected) AccentYellow else CardBorderColor,
        onClick = if (isUnlocked) onSelect else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        color = themePrimary.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .border(
                        width = 2.dp,
                        color = themePrimary,
                        shape = RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            color = themePrimary,
                            shape = RoundedCornerShape(8.dp)
                        )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = theme.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            if (isSelected) {
                Surface(
                    color = AccentYellow.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, AccentYellow)
                ) {
                    Text(
                        text = "ACTIVE",
                        color = AccentYellow,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            } else if (!isUnlocked) {
                Text(
                    text = "🔒 Locked",
                    style = MaterialTheme.typography.bodySmall,
                    color = AccentOrange,
                    fontWeight = FontWeight.SemiBold
                )
            } else {
                Text(
                    text = "Unlocked",
                    style = MaterialTheme.typography.bodySmall,
                    color = AccentGreen
                )
            }
        }
    }
}

@Composable
fun StatItem(
    label: String,
    value: String,
    accentColor: Color = AccentYellow
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = accentColor
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = TextSecondary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun RarityBadge(rarity: AchievementRarity) {
    val bg = Color(rarity.displayColor)
    Surface(
        color = bg.copy(alpha = 0.2f),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, bg)
    ) {
        Text(
            text = rarity.name,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            color = bg
        )
    }
}

@Composable
private fun RewardBadge(
    icon: String,
    label: String
) {
    Surface(
        color = NavySurfaceVariant,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, CardBorderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 12.sp)
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
    }
}


