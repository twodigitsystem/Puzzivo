package com.tds.puzzivo.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material.icons.outlined.VideogameAsset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tds.puzzivo.core.design.AccentCyan
import com.tds.puzzivo.core.design.AccentGreen
import com.tds.puzzivo.core.design.AccentOrange
import com.tds.puzzivo.core.design.AccentYellow
import com.tds.puzzivo.core.design.BannerAdView
import com.tds.puzzivo.core.design.CardBorderColor
import com.tds.puzzivo.core.design.NavyBackground
import com.tds.puzzivo.core.design.NavySurface
import com.tds.puzzivo.core.design.NavySurfaceVariant
import com.tds.puzzivo.core.design.PuzzivoCard
import com.tds.puzzivo.core.design.TextPrimary
import com.tds.puzzivo.core.design.TextSecondary
import com.tds.puzzivo.core.model.GameType
import com.tds.puzzivo.R

@Composable
fun HomeScreen(
    onNavigateToGame: (GameType) -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val settingsDescription = stringResource(R.string.action_settings)

    Surface(
        color = NavyBackground,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Header / Brand
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PUZZIVO",
                        style = MaterialTheme.typography.headlineLarge,
                        color = AccentYellow,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = stringResource(R.string.home_brand_tagline),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }

                IconButton(
                    onClick = onNavigateToSettings,
                    modifier = Modifier.semantics { contentDescription = settingsDescription }
                ) {
                    Surface(
                        color = NavySurface,
                        shape = CircleShape,
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = null,
                                tint = TextPrimary
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
            ) {
                // Compact progress summary
                PuzzivoCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LocalFireDepartment,
                            contentDescription = null,
                            tint = AccentOrange,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = pluralStringResource(R.plurals.home_days_format, uiState.userPreferences.currentStreak, uiState.userPreferences.currentStreak),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = AccentOrange
                            )
                            Text(
                                text = stringResource(
                                    R.string.home_streak_summary,
                                    stringResource(R.string.home_daily_streak),
                                    uiState.userPreferences.bestStreak
                                ),
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(36.dp)
                                .background(CardBorderColor)
                        )

                        Spacer(modifier = Modifier.width(14.dp))
                        Icon(
                            imageVector = Icons.Outlined.VideogameAsset,
                            contentDescription = null,
                            tint = AccentCyan,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${uiState.userPreferences.totalGamesPlayed}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = AccentCyan
                            )
                            Text(
                                text = stringResource(R.string.home_games_played),
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Today's Pick Card
                val pickGame = uiState.todayPick.gameType
                PuzzivoCard(
                    backgroundColor = NavySurfaceVariant,
                    borderColor = AccentYellow,
                    onClick = { onNavigateToGame(pickGame) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = AccentYellow.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(20.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, AccentYellow)
                            ) {
                                Text(
                                     text = stringResource(R.string.home_todays_pick),
                                    color = AccentYellow,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }

                            if (uiState.todayPick.isCompleted) {
                                Surface(
                                    color = AccentGreen.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(20.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentGreen)
                                ) {
                                    Text(
                                        text = stringResource(R.string.home_completed),
                                        color = AccentGreen,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = stringResource(pickGame.titleRes),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Text(
                            text = stringResource(pickGame.descriptionRes),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                        )

                        Surface(
                            color = AccentYellow,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(R.string.action_play_today),
                                color = NavyBackground,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 10.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                     text = stringResource(R.string.home_select_game),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // 2x2 game grid keeps the hub compact and scannable.
                GameType.entries.chunked(2).forEach { rowGames ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowGames.forEach { gameType ->
                            val color = Color(gameType.primaryColorHex)
                            val statText = when (gameType) {
                                GameType.NUMBER_MERGE -> stringResource(R.string.home_best_score_format, uiState.userPreferences.numberMergeBestScore)
                                GameType.MEMORY_MATCH -> if (uiState.userPreferences.memoryMatchBestMoves > 0) {
                                    pluralStringResource(R.plurals.home_best_moves_format, uiState.userPreferences.memoryMatchBestMoves, uiState.userPreferences.memoryMatchBestMoves)
                                } else {
                                    stringResource(R.string.home_no_score_yet)
                                }
                                GameType.QUICK_MATH -> stringResource(R.string.home_best_score_format, uiState.userPreferences.quickMathBestScore)
                                GameType.WORD_SEARCH -> stringResource(
                                    R.string.home_solved_format,
                                    uiState.userPreferences.wordSearchSolvedIds.size,
                                    gameType.totalContentCount ?: 0
                                )
                                else -> stringResource(R.string.home_new_game)
                            }
                            val icon = when (gameType) {
                                GameType.NUMBER_MERGE -> Icons.Outlined.Calculate
                                GameType.MEMORY_MATCH -> Icons.Outlined.Style
                                GameType.QUICK_MATH -> Icons.Outlined.Bolt
                                GameType.WORD_SEARCH -> Icons.Outlined.Search
                                else -> Icons.Outlined.Style
                            }
                            val gameCardDescription = stringResource(
                                R.string.home_game_card_description,
                                stringResource(gameType.titleRes),
                                stringResource(gameType.subtitleRes),
                                statText
                            )
                            PuzzivoCard(
                                onClick = { onNavigateToGame(gameType) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(174.dp)
                                    .semantics {
                                        contentDescription = gameCardDescription
                                    }
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .background(color.copy(alpha = 0.2f), shape = RoundedCornerShape(12.dp))
                                            .border(1.dp, color, shape = RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(imageVector = icon, contentDescription = null, tint = color)
                                    }
                                    Text(
                                        text = stringResource(gameType.titleRes),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = stringResource(gameType.subtitleRes),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = statText,
                                        fontSize = 12.sp,
                                        color = color,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1
                                    )
                                    Icon(
                                        imageVector = Icons.Outlined.ChevronRight,
                                        contentDescription = null,
                                        tint = color,
                                        modifier = Modifier.align(Alignment.End)
                                    )
                                }
                            }
                        }
                        if (rowGames.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Bottom Banner Ad
            BannerAdView(modifier = Modifier.padding(vertical = 8.dp))
        }
    }
}
