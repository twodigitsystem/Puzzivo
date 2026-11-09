package com.tds.puzzivo.feature.profile

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Login
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Stars
import androidx.compose.material.icons.outlined.VideogameAsset
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tds.puzzivo.R
import com.tds.puzzivo.core.design.AccentCyan
import com.tds.puzzivo.core.design.AccentGreen
import com.tds.puzzivo.core.design.AccentMagenta
import com.tds.puzzivo.core.design.AccentOrange
import com.tds.puzzivo.core.design.AccentYellow
import com.tds.puzzivo.core.design.CardBorderColor
import com.tds.puzzivo.core.design.NavyBackground
import com.tds.puzzivo.core.design.NavySurface
import com.tds.puzzivo.core.design.NavySurfaceVariant
import com.tds.puzzivo.core.design.PrimaryButton
import com.tds.puzzivo.core.design.PuzzivoCard
import com.tds.puzzivo.core.design.PuzzivoTopBar
import com.tds.puzzivo.core.design.SecondaryButton
import com.tds.puzzivo.core.design.SignInPromptDialog
import com.tds.puzzivo.core.design.TextPrimary
import com.tds.puzzivo.core.design.TextSecondary

@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAchievements: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToThemes: () -> Unit,
    onNavigateToShop: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isSignedIn = uiState.user != null && !uiState.user!!.isAnonymous
    val context = LocalContext.current
    var showSignInDialog by remember { mutableStateOf(false) }

    Surface(
        color = NavyBackground,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            PuzzivoTopBar(
                title = "PROFILE",
                onBackClick = onNavigateBack
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Profile Header Card
                item {
                    ProfileHeaderCard(
                        displayName = if (isSignedIn && !uiState.user?.displayName.isNullOrBlank()) {
                            uiState.user!!.displayName!!
                        } else {
                            "Guest Player"
                        },
                        email = uiState.user?.email,
                        isSignedIn = isSignedIn,
                        level = uiState.playerProgress.level,
                        onSignInClick = { showSignInDialog = true },
                        onSignOutClick = { viewModel.signOut() }
                    )
                }

                // 2. XP & Progression Banner
                item {
                    ProgressionBannerCard(
                        currentLevel = uiState.playerProgress.level,
                        currentXP = uiState.playerProgress.currentXP,
                        xpForNextLevel = uiState.playerProgress.xpToNextLevel()
                    )
                }

                // 3. Currency & Streak Row
                item {
                    CurrencyAndStreakRow(
                        coins = uiState.playerProgress.coins,
                        gems = uiState.playerProgress.gems,
                        currentStreak = uiState.userPreferences.currentStreak
                    )
                }

                // 4. Quick Gameplay Summary Grid
                item {
                    QuickStatsGrid(
                        gamesPlayed = uiState.totalGamesPlayed,
                        gamesWon = uiState.totalWins,
                        unlockedAchievements = uiState.unlockedAchievementsCount,
                        totalAchievements = uiState.totalAchievementsCount
                    )
                }

                // 5. Section Header
                item {
                    Text(
                        text = "PLAYER HUB",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }

                // 6. Navigation Feature Cards
                item {
                    ActionFeatureCard(
                        icon = Icons.Outlined.EmojiEvents,
                        accentColor = AccentYellow,
                        title = "Achievements & Trophies",
                        subtitle = "Unlocked ${uiState.unlockedAchievementsCount} of ${uiState.totalAchievementsCount} badges",
                        badgeText = "${uiState.unlockedAchievementsCount}/${uiState.totalAchievementsCount}",
                        onClick = onNavigateToAchievements
                    )
                }

                item {
                    ActionFeatureCard(
                        icon = Icons.Outlined.BarChart,
                        accentColor = AccentCyan,
                        title = "Game Statistics",
                        subtitle = "View win rates, solve times & high scores",
                        badgeText = "Stats",
                        onClick = onNavigateToStatistics
                    )
                }

                item {
                    ActionFeatureCard(
                        icon = Icons.Outlined.Palette,
                        accentColor = AccentMagenta,
                        title = "Themes & Appearance",
                        subtitle = "Active Theme: ${uiState.selectedTheme.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }}",
                        badgeText = "Theme",
                        onClick = onNavigateToThemes
                    )
                }

                item {
                    ActionFeatureCard(
                        icon = Icons.Outlined.ShoppingCart,
                        accentColor = AccentYellow,
                        title = "In-App Shop",
                        subtitle = "Spend coins & gems on items & themes",
                        badgeText = "Shop",
                        onClick = onNavigateToShop
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    if (showSignInDialog) {
        SignInPromptDialog(
            feature = "Profile Sync",
            onSignIn = {
                viewModel.signInWithGoogle(context)
                showSignInDialog = false
            },
            onDismiss = { showSignInDialog = false }
        )
    }
}

@Composable
private fun ProfileHeaderCard(
    displayName: String,
    email: String?,
    isSignedIn: Boolean,
    level: Int,
    onSignInClick: () -> Unit,
    onSignOutClick: () -> Unit
) {
    PuzzivoCard(
        backgroundColor = NavySurface,
        borderColor = AccentYellow.copy(alpha = 0.6f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar with Level Badge Overlay
            Box(contentAlignment = Alignment.BottomEnd) {
                Surface(
                    shape = CircleShape,
                    color = NavySurfaceVariant,
                    border = BorderStroke(2.dp, AccentYellow),
                    modifier = Modifier.size(88.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (isSignedIn && displayName.isNotBlank() && displayName != "Guest Player") {
                            Text(
                                text = displayName.first().uppercase(),
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Black,
                                color = AccentYellow
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(44.dp)
                            )
                        }
                    }
                }

                // Level Pill Overlay
                Surface(
                    color = AccentYellow,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, NavyBackground)
                ) {
                    Text(
                        text = "LVL $level",
                        color = NavyBackground,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Display Name
            Text(
                text = displayName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Status Indicator Tag
            if (isSignedIn) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = AccentGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = email ?: "Signed In with Google",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AccentGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                Surface(
                    color = AccentOrange.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, AccentOrange.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "Guest Account • Progress stored locally",
                        color = AccentOrange,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Auth Action Button
            if (isSignedIn) {
                SecondaryButton(
                    text = "Sign Out",
                    onClick = onSignOutClick,
                    modifier = Modifier.fillMaxWidth(0.6f)
                )
            } else {
                PrimaryButton(
                    text = "Connect Google Account",
                    onClick = onSignInClick,
                    containerColor = AccentYellow,
                    contentColor = NavyBackground,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun ProgressionBannerCard(
    currentLevel: Int,
    currentXP: Int,
    xpForNextLevel: Int
) {
    val targetProgress = (currentXP.toFloat() / xpForNextLevel.coerceAtLeast(1)).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "ProfileProgressAnimation"
    )

    val levelTitle = when {
        currentLevel <= 1 -> "Novice Solver"
        currentLevel <= 3 -> "Apprentice Strategist"
        currentLevel <= 5 -> "Puzzle Enthusiast"
        currentLevel <= 8 -> "Logic Mastermind"
        currentLevel <= 12 -> "Grandmaster Solver"
        else -> "Legendary Puzzler"
    }

    PuzzivoCard(
        backgroundColor = NavySurfaceVariant,
        borderColor = CardBorderColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(AccentYellow.copy(alpha = 0.2f), shape = RoundedCornerShape(10.dp))
                            .border(1.dp, AccentYellow, shape = RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Stars,
                            contentDescription = null,
                            tint = AccentYellow,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Level $currentLevel",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = levelTitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = AccentYellow,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Text(
                    text = "$currentXP / $xpForNextLevel XP",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = AccentYellow,
                trackColor = NavySurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "${(animatedProgress * 100).toInt()}% completed towards Level ${currentLevel + 1}",
                fontSize = 11.sp,
                color = TextSecondary,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
private fun CurrencyAndStreakRow(
    coins: Int,
    gems: Int,
    currentStreak: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MetricTile(
            icon = "🪙",
            value = "$coins",
            label = "Coins",
            accentColor = AccentYellow,
            modifier = Modifier.weight(1f)
        )
        MetricTile(
            icon = "💎",
            value = "$gems",
            label = "Gems",
            accentColor = AccentCyan,
            modifier = Modifier.weight(1f)
        )
        MetricTile(
            icon = "🔥",
            value = "$currentStreak",
            label = "Streak",
            accentColor = AccentOrange,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MetricTile(
    icon: String,
    value: String,
    label: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    PuzzivoCard(
        backgroundColor = NavySurface,
        borderColor = CardBorderColor,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = icon, fontSize = 22.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor,
                maxLines = 1
            )
            Text(
                text = label,
                fontSize = 11.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun QuickStatsGrid(
    gamesPlayed: Int,
    gamesWon: Int,
    unlockedAchievements: Int,
    totalAchievements: Int
) {
    PuzzivoCard(
        backgroundColor = NavySurface,
        borderColor = CardBorderColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatSummaryItem(
                icon = Icons.Outlined.VideogameAsset,
                iconColor = AccentCyan,
                value = "$gamesPlayed",
                label = "Games Played"
            )

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(36.dp)
                    .background(CardBorderColor)
            )

            StatSummaryItem(
                icon = Icons.Outlined.EmojiEvents,
                iconColor = AccentYellow,
                value = "$gamesWon",
                label = "Games Won"
            )

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(36.dp)
                    .background(CardBorderColor)
            )

            StatSummaryItem(
                icon = Icons.Outlined.Stars,
                iconColor = AccentGreen,
                value = "$unlockedAchievements/$totalAchievements",
                label = "Badges"
            )
        }
    }
}

@Composable
private fun StatSummaryItem(
    icon: ImageVector,
    iconColor: Color,
    value: String,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = TextSecondary
        )
    }
}

@Composable
private fun ActionFeatureCard(
    icon: ImageVector,
    accentColor: Color,
    title: String,
    subtitle: String,
    badgeText: String,
    onClick: () -> Unit
) {
    PuzzivoCard(
        onClick = onClick,
        backgroundColor = NavySurface,
        borderColor = CardBorderColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(accentColor.copy(alpha = 0.18f), shape = RoundedCornerShape(12.dp))
                    .border(1.dp, accentColor, shape = RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Surface(
                color = accentColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f))
            ) {
                Text(
                    text = badgeText,
                    color = accentColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
