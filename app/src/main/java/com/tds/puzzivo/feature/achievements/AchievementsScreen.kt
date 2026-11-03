package com.tds.puzzivo.feature.achievements

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tds.puzzivo.R
import com.tds.puzzivo.core.design.AccentCyan
import com.tds.puzzivo.core.design.AccentGreen
import com.tds.puzzivo.core.design.AccentYellow
import com.tds.puzzivo.core.design.AchievementCard
import com.tds.puzzivo.core.design.CardBorderColor
import com.tds.puzzivo.core.design.NavyBackground
import com.tds.puzzivo.core.design.NavySurface
import com.tds.puzzivo.core.design.PuzzivoCard
import com.tds.puzzivo.core.design.PuzzivoTopBar
import com.tds.puzzivo.core.design.TextPrimary
import com.tds.puzzivo.core.design.TextSecondary

@Composable
fun AchievementsScreen(
    onNavigateBack: () -> Unit,
    viewModel: AchievementsViewModel = hiltViewModel()
) {
    val achievements by viewModel.achievements.collectAsState()
    val stats by viewModel.achievementStats.collectAsState()
    var selectedFilter by remember { mutableIntStateOf(0) } // 0: All, 1: Unlocked, 2: Locked

    val filteredAchievements = remember(achievements, selectedFilter) {
        when (selectedFilter) {
            1 -> achievements.filter { it.unlocked }
            2 -> achievements.filter { !it.unlocked }
            else -> achievements
        }
    }

    Surface(
        color = NavyBackground,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            PuzzivoTopBar(
                title = "ACHIEVEMENTS",
                onBackClick = onNavigateBack
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Hero Progress Banner
                item {
                    AchievementProgressHeader(
                        unlockedCount = stats.unlockedCount,
                        totalCount = stats.totalCount,
                        totalXP = stats.totalXPEarned,
                        totalCoins = stats.totalCoinsEarned
                    )
                }

                // Filter Pill Bar
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterPill(
                            label = "All (${stats.totalCount})",
                            isSelected = selectedFilter == 0,
                            onClick = { selectedFilter = 0 },
                            modifier = Modifier.weight(1f)
                        )
                        FilterPill(
                            label = "Unlocked (${stats.unlockedCount})",
                            isSelected = selectedFilter == 1,
                            onClick = { selectedFilter = 1 },
                            modifier = Modifier.weight(1f)
                        )
                        FilterPill(
                            label = "Locked (${(stats.totalCount - stats.unlockedCount).coerceAtLeast(0)})",
                            isSelected = selectedFilter == 2,
                            onClick = { selectedFilter = 2 },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Achievement List
                items(filteredAchievements, key = { it.id }) { achievement ->
                    AchievementCard(
                        achievement = achievement,
                        onClick = {}
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun AchievementProgressHeader(
    unlockedCount: Int,
    totalCount: Int,
    totalXP: Long,
    totalCoins: Long
) {
    var animatedProgress by remember { mutableFloatStateOf(0f) }
    val targetProgress = if (totalCount > 0) (unlockedCount.toFloat() / totalCount).coerceIn(0f, 1f) else 0f

    LaunchedEffect(targetProgress) {
        animate(
            initialValue = animatedProgress,
            targetValue = targetProgress,
            animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
        ) { value, _ ->
            animatedProgress = value
        }
    }

    PuzzivoCard(
        backgroundColor = NavySurface,
        borderColor = AccentYellow.copy(alpha = 0.6f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Trophy Collection",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "$unlockedCount of $totalCount Badges Unlocked",
                        fontSize = 12.sp,
                        color = AccentYellow,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Surface(
                    color = AccentYellow.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, AccentYellow)
                ) {
                    Text(
                        text = "${(targetProgress * 100).toInt()}%",
                        color = AccentYellow,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = AccentYellow,
                trackColor = NavyBackground
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RewardSummaryTag(icon = "⭐", value = "+$totalXP XP Earned")
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(24.dp)
                        .background(CardBorderColor)
                )
                RewardSummaryTag(icon = "🪙", value = "+$totalCoins Coins Earned")
            }
        }
    }
}

@Composable
private fun RewardSummaryTag(icon: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(text = icon, fontSize = 16.sp)
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
    }
}

@Composable
private fun FilterPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (isSelected) AccentYellow else NavySurface,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, if (isSelected) AccentYellow else CardBorderColor),
        onClick = onClick,
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(vertical = 10.dp)
        ) {
            Text(
                text = label,
                color = if (isSelected) NavyBackground else TextSecondary,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                maxLines = 1
            )
        }
    }
}

data class AchievementStats(
    val unlockedCount: Int = 0,
    val totalCount: Int = 0,
    val totalXPEarned: Long = 0,
    val totalCoinsEarned: Long = 0
)
