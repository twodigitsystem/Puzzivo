package com.tds.puzzivo.feature.challenge

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tds.puzzivo.R
import com.tds.puzzivo.core.design.AccentCyan
import com.tds.puzzivo.core.design.AccentGreen
import com.tds.puzzivo.core.design.AccentOrange
import com.tds.puzzivo.core.design.AccentYellow
import com.tds.puzzivo.core.design.CardBorderColor
import com.tds.puzzivo.core.design.ChallengeCard
import com.tds.puzzivo.core.design.NavyBackground
import com.tds.puzzivo.core.design.NavySurface
import com.tds.puzzivo.core.design.PuzzivoCard
import com.tds.puzzivo.core.design.PuzzivoTopBar
import com.tds.puzzivo.core.design.TextPrimary
import com.tds.puzzivo.core.design.TextSecondary
import com.tds.puzzivo.core.model.GameType

@Composable
fun DailyChallengeScreen(
    onNavigateBack: () -> Unit,
    onNavigateToGame: (GameType) -> Unit,
    viewModel: DailyChallengeViewModel = hiltViewModel()
) {
    val todayChallenge by viewModel.todayChallenge.collectAsState()
    val recentChallenges by viewModel.recentChallenges.collectAsState()
    val timeRemaining by viewModel.timeRemaining.collectAsState()

    Surface(
        color = NavyBackground,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            PuzzivoTopBar(
                title = "DAILY CHALLENGE",
                onBackClick = onNavigateBack
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Timer Banner
                item {
                    PuzzivoCard(
                        backgroundColor = NavySurface,
                        borderColor = AccentOrange.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "⏰", fontSize = 36.sp)

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Daily Quest Reset Timer",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Surface(
                                color = AccentOrange.copy(alpha = 0.18f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, AccentOrange)
                            ) {
                                Text(
                                    text = "Resets in $timeRemaining",
                                    color = AccentOrange,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Today's challenge
                todayChallenge?.let { challenge ->
                    item {
                        Text(
                            text = "TODAY'S FEATURED QUEST",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                    }

                    item {
                        ChallengeCard(
                            challenge = challenge,
                            onStart = {
                                viewModel.startChallenge(challenge)
                                onNavigateToGame(challenge.gameType)
                            }
                        )
                    }
                }

                // Recent challenges
                if (recentChallenges.isNotEmpty()) {
                    item {
                        Text(
                            text = "PREVIOUS CHALLENGES",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    items(recentChallenges, key = { it.id }) { challenge ->
                        ChallengeCard(
                            challenge = challenge,
                            onStart = {
                                viewModel.startChallenge(challenge)
                                onNavigateToGame(challenge.gameType)
                            }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
