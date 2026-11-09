package com.tds.puzzivo.feature.statistics

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tds.puzzivo.core.design.AccentCyan
import com.tds.puzzivo.core.design.AccentGreen
import com.tds.puzzivo.core.design.AccentOrange
import com.tds.puzzivo.core.design.AccentYellow
import com.tds.puzzivo.core.design.CardBorderColor
import com.tds.puzzivo.core.design.NavyBackground
import com.tds.puzzivo.core.design.NavySurface
import com.tds.puzzivo.core.design.NavySurfaceVariant
import com.tds.puzzivo.core.design.PuzzivoCard
import com.tds.puzzivo.core.design.PuzzivoTopBar
import com.tds.puzzivo.core.design.StatisticsCard
import com.tds.puzzivo.core.design.TextPrimary
import com.tds.puzzivo.core.design.TextSecondary
import com.tds.puzzivo.core.util.formatTime

@Composable
fun StatisticsScreen(
    onNavigateBack: () -> Unit,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val statistics by viewModel.allStatistics.collectAsState()
    val overallStats by viewModel.overallStatistics.collectAsState()

    Surface(
        color = NavyBackground,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            PuzzivoTopBar(
                title = "GAME STATISTICS",
                onBackClick = onNavigateBack
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Hero Overall Stats Banner
                item {
                    OverallPerformanceHeader(
                        totalPlayed = overallStats.totalGamesPlayed,
                        totalWins = overallStats.totalWins,
                        playTimeSeconds = overallStats.totalPlayTime,
                        totalScore = overallStats.totalScore,
                        bestStreak = overallStats.bestStreak
                    )
                }

                // Current Streak Card
                if (overallStats.currentStreak > 0) {
                    item {
                        CurrentStreakCard(currentStreak = overallStats.currentStreak)
                    }
                }

                // Section Header
                item {
                    Text(
                        text = "PER-GAME BREAKDOWN",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                    )
                }

                // Per-game statistics list
                if (statistics.isEmpty()) {
                    item {
                        PuzzivoCard(
                            backgroundColor = NavySurface,
                            borderColor = CardBorderColor,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = "📊", fontSize = 36.sp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No Game Stats Recorded Yet",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Play games from the home screen to track your scores, win rates, and solve times!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(statistics, key = { it.gameType.id }) { stat ->
                        StatisticsCard(statistics = stat)
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun OverallPerformanceHeader(
    totalPlayed: Int,
    totalWins: Int,
    playTimeSeconds: Long,
    totalScore: Long,
    bestStreak: Int
) {
    val winRate = if (totalPlayed > 0) kotlin.math.round(totalWins * 100.0 / totalPlayed).toInt() else 0

    PuzzivoCard(
        backgroundColor = NavySurface,
        borderColor = AccentCyan.copy(alpha = 0.6f),
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
                Text(
                    text = "Overall Performance",
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
                        text = "$winRate% WIN RATE",
                        color = AccentCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Row 1: Key Gameplay Metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OverallStatMetric(label = "Games Played", value = "$totalPlayed", color = TextPrimary)
                OverallStatMetric(label = "Games Won", value = "$totalWins", color = AccentGreen)
                OverallStatMetric(label = "Win Rate", value = "$winRate%", color = AccentYellow)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Row 2: Secondary Metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OverallStatMetric(label = "Play Time", value = formatTime(playTimeSeconds), color = TextPrimary)
                OverallStatMetric(label = "Best Streak", value = "$bestStreak 🔥", color = AccentOrange)
                OverallStatMetric(label = "High Score", value = "$totalScore", color = AccentCyan)
            }
        }
    }
}

@Composable
private fun OverallStatMetric(
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = TextSecondary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun CurrentStreakCard(currentStreak: Int) {
    PuzzivoCard(
        backgroundColor = NavySurfaceVariant,
        borderColor = AccentOrange,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                color = AccentOrange.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, AccentOrange),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(text = "🔥", fontSize = 24.sp)
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$currentStreak Day Winning Streak!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AccentOrange
                )
                Text(
                    text = "Keep playing daily to build your streak and earn extra rewards!",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}


