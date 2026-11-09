package com.tds.puzzivo.core.design

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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

@Composable
fun PuzzivoCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = NavySurface,
    borderColor: Color = CardBorderColor,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, borderColor),
        onClick = onClick ?: {},
        enabled = onClick != null,
        modifier = modifier
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

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
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                if (isLocked) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { achievement.progress.toFloat() / achievement.requirement.toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = rarityColor,
                        trackColor = NavySurfaceVariant,
                    )
                } else if (achievement.coinReward > 0 || achievement.xpReward > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (achievement.coinReward > 0) {
                            RewardBadge(icon = "🪙", label = "+${achievement.coinReward}")
                        }
                        if (achievement.xpReward > 0) {
                            RewardBadge(icon = "⭐", label = "+${achievement.xpReward} XP")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Card displaying daily challenge status
 */
@Composable
fun ChallengeCard(
    modifier: Modifier = Modifier,
    challenge: DailyChallenge,
    onStart: () -> Unit = {}
) {
    val isCompleted = challenge.isCompleted

    PuzzivoCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = NavySurface,
        borderColor = if (isCompleted) AccentGreen else AccentYellow,
        onClick = if (isCompleted) null else onStart
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        color = if (isCompleted) AccentGreen.copy(alpha = 0.2f) else AccentYellow.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = if (isCompleted) AccentGreen else AccentYellow,
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isCompleted) "✅" else "🎯",
                    fontSize = 24.sp
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Daily Challenge",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = stringResource(challenge.descriptionRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${challenge.progress} / ${challenge.requirement}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isCompleted) AccentGreen else AccentYellow
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (challenge.rewardCoins > 0) {
                            RewardBadge(icon = "🪙", label = "+${challenge.rewardCoins}")
                        }
                        if (challenge.rewardXP > 0) {
                            RewardBadge(icon = "⭐", label = "+${challenge.rewardXP} XP")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Card displaying game statistics
 */
@Composable
fun StatisticsCard(
    modifier: Modifier = Modifier,
    statistics: GameStatistics
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
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = statistics.gameType.name.replace("_", " ").uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Surface(
                    color = NavySurfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, CardBorderColor)
                ) {
                    Text(
                        text = "Wins: ${statistics.totalWins}/${statistics.totalPlays}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = AccentGreen,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

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
