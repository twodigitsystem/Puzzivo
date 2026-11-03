package com.tds.puzzivo.feature.leaderboard

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tds.puzzivo.core.design.AccentCyan
import com.tds.puzzivo.core.design.AccentOrange
import com.tds.puzzivo.core.design.AccentYellow
import com.tds.puzzivo.core.design.CardBorderColor
import com.tds.puzzivo.core.design.NavyBackground
import com.tds.puzzivo.core.design.NavySurface
import com.tds.puzzivo.core.design.NavySurfaceVariant
import com.tds.puzzivo.core.design.PrimaryButton
import com.tds.puzzivo.core.design.PuzzivoCard
import com.tds.puzzivo.core.design.PuzzivoTopBar
import com.tds.puzzivo.core.design.SignInPromptDialog
import com.tds.puzzivo.core.design.TextPrimary
import com.tds.puzzivo.core.design.TextSecondary
import com.tds.puzzivo.core.manager.LeaderboardEntry
import com.tds.puzzivo.core.model.GameType

@Composable
fun LeaderboardScreen(
    gameType: GameType,
    onNavigateBack: () -> Unit,
    viewModel: LeaderboardViewModel = hiltViewModel()
) {
    val entries by viewModel.leaderboardEntries.collectAsState()
    val isSignedIn by viewModel.isSignedIn.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    var showSignInDialog by remember { mutableStateOf(false) }
    var selectedScope by remember { mutableStateOf(LeaderboardScope.GLOBAL) }
    val context = LocalContext.current

    LaunchedEffect(gameType, selectedScope) {
        if (isSignedIn) {
            viewModel.loadLeaderboard(gameType, selectedScope)
        }
    }

    Surface(
        color = NavyBackground,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            PuzzivoTopBar(
                title = "LEADERBOARD",
                onBackClick = onNavigateBack
            )

            if (!isSignedIn) {
                // Sign in required card
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    PuzzivoCard(
                        backgroundColor = NavySurface,
                        borderColor = AccentYellow,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "🔐", fontSize = 48.sp)

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "Sign In Required",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Connect your account to view global rankings and compete against players worldwide!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            PrimaryButton(
                                text = "Sign In to View Rankings",
                                onClick = { showSignInDialog = true }
                            )
                        }
                    }
                }
            } else {
                // Scope selector pill tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LeaderboardScope.entries.forEach { scope ->
                        val isSelected = selectedScope == scope
                        Surface(
                            color = if (isSelected) AccentYellow else NavySurface,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, if (isSelected) AccentYellow else CardBorderColor),
                            onClick = { selectedScope = scope },
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(vertical = 10.dp)
                            ) {
                                Text(
                                    text = scope.displayName,
                                    color = if (isSelected) NavyBackground else TextSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // Leaderboard content
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AccentYellow)
                    }
                } else if (entries.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        PuzzivoCard(
                            backgroundColor = NavySurface,
                            borderColor = CardBorderColor,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = "🏆", fontSize = 36.sp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No Scores Posted Yet",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Be the first player to submit a high score on this leaderboard!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        itemsIndexed(entries) { index, entry ->
                            LeaderboardEntryCard(
                                rank = index + 1,
                                entry = entry
                            )
                        }
                    }
                }
            }
        }
    }

    if (showSignInDialog) {
        SignInPromptDialog(
            feature = "Leaderboards",
            onSignIn = {
                viewModel.signIn(context)
                showSignInDialog = false
            },
            onDismiss = { showSignInDialog = false }
        )
    }
}

@Composable
private fun LeaderboardEntryCard(
    rank: Int,
    entry: LeaderboardEntry
) {
    val (rankText, rankColor, borderColor) = when (rank) {
        1 -> Triple("🥇", AccentYellow, AccentYellow)
        2 -> Triple("🥈", Color(0xFFE2E8F0), Color(0xFFCBD5E1))
        3 -> Triple("🥉", AccentOrange, AccentOrange)
        else -> Triple("#$rank", TextSecondary, CardBorderColor)
    }

    PuzzivoCard(
        backgroundColor = if (rank <= 3) NavySurfaceVariant else NavySurface,
        borderColor = borderColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank badge
            Box(
                modifier = Modifier.width(44.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = rankText,
                    fontSize = if (rank <= 3) 22.sp else 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = rankColor
                )
            }

            // Player name
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            // High Score
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = entry.score.toString(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AccentCyan
                )
                Text(
                    text = "Points",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

enum class LeaderboardScope(val displayName: String) {
    GLOBAL("Global"),
    FRIENDS("Friends")
}
