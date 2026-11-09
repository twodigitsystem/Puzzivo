package com.tds.puzzivo.core.design

import android.view.ViewGroup
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize
import com.google.android.libraries.ads.mobile.sdk.banner.AdView
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRequest
import com.tds.puzzivo.PuzzivoApplication
import com.tds.puzzivo.R
import com.tds.puzzivo.core.ads.AdUnitIds

@Composable
fun PuzzivoTopBar(
    title: String,
    onBackClick: () -> Unit,
    onActionClick: (() -> Unit)? = null,
    actionIcon: ImageVector = Icons.AutoMirrored.Outlined.HelpOutline,
    actionContentDescription: String? = null
) {
    Surface(
        color = NavySurface,
        border = BorderStroke(1.dp, CardBorderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.semantics {
                    contentDescription = "Back"
                    role = Role.Button
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = TextPrimary,
                letterSpacing = 0.5.sp
            )

            if (onActionClick != null) {
                IconButton(
                    onClick = onActionClick,
                    modifier = Modifier.semantics {
                        contentDescription = actionContentDescription ?: "Action"
                        role = Role.Button
                    }
                ) {
                    Icon(
                        imageVector = actionIcon,
                        contentDescription = actionContentDescription,
                        tint = AccentYellow
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(48.dp))
            }
        }
    }
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = AccentYellow,
    contentColor: Color = NavyBackground
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = NavySurfaceVariant,
            disabledContentColor = TextSecondary
        ),
        modifier = modifier.fillMaxWidth().height(50.dp)
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, CardBorderColor),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = TextPrimary,
            disabledContentColor = TextSecondary
        ),
        modifier = modifier.fillMaxWidth().height(50.dp)
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun InstructionDialog(
    title: String,
    description: String,
    onDismiss: () -> Unit
) {
    val steps = remember(description) {
        description.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
    }

    var animateTrigger by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { animateTrigger = true }
    val scale by animateFloatAsState(
        targetValue = if (animateTrigger) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "InstructionDialogScale"
    )

    Dialog(onDismissRequest = onDismiss) {
        PuzzivoCard(
            backgroundColor = NavySurface,
            borderColor = AccentYellow,
            modifier = Modifier.padding(12.dp).scale(scale)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    color = AccentYellow,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    steps.forEach { step ->
                        Surface(
                            color = NavySurfaceVariant,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, CardBorderColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = step,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(12.dp),
                                textAlign = TextAlign.Start
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                PrimaryButton(
                    text = stringResource(R.string.action_got_it),
                    onClick = onDismiss
                )
            }
        }
    }
}

@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    confirmText: String,
    dismissText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var animateTrigger by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { animateTrigger = true }
    val scale by animateFloatAsState(
        targetValue = if (animateTrigger) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "ConfirmationDialogScale"
    )

    Dialog(onDismissRequest = onDismiss) {
        PuzzivoCard(
            modifier = Modifier.padding(16.dp).scale(scale)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = AccentYellow,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SecondaryButton(
                        text = dismissText,
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )

                    PrimaryButton(
                        text = confirmText,
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun ResultDialog(
    title: String,
    scoreText: String,
    isNewBest: Boolean,
    bestScoreText: String,
    resultIcon: ImageVector? = null,
    resultIconContentDescription: String? = null,
    detailsText: String? = null,
    rewardButtonText: String? = null,
    onRewardClick: (() -> Unit)? = null,
    onReplayClick: () -> Unit,
    onHomeClick: () -> Unit
) {
    var animateTrigger by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { animateTrigger = true }
    val scale by animateFloatAsState(
        targetValue = if (animateTrigger) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "ResultDialogScale"
    )

    Dialog(onDismissRequest = onHomeClick) {
        PuzzivoCard(
            modifier = Modifier.padding(16.dp).scale(scale)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                resultIcon?.let { icon ->
                    Icon(
                        imageVector = icon,
                        contentDescription = resultIconContentDescription,
                        tint = AccentYellow,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = AccentYellow,
                    fontWeight = FontWeight.Bold
                )

                if (isNewBest) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = AccentGreen.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, AccentGreen)
                    ) {
                        Text(
                            text = stringResource(R.string.dialog_new_best),
                            color = AccentGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = scoreText,
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.ExtraBold
                )

                detailsText?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = stringResource(R.string.dialog_best_format, bestScoreText),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (rewardButtonText != null && onRewardClick != null) {
                    PrimaryButton(
                        text = rewardButtonText,
                        onClick = onRewardClick,
                        containerColor = AccentCyan,
                        contentColor = NavyBackground
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                PrimaryButton(
                    text = stringResource(R.string.action_play_again),
                    onClick = onReplayClick
                )

                Spacer(modifier = Modifier.height(12.dp))

                SecondaryButton(
                    text = stringResource(R.string.action_main_menu),
                    onClick = onHomeClick
                )
            }
        }
    }
}

@Composable
fun BannerAdView(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val application = context.applicationContext as? PuzzivoApplication
    val adsReady by (application?.adsReadyFlow ?: kotlinx.coroutines.flow.flowOf(false))
        .collectAsState(initial = false)
    var adFailed by remember(adsReady) { mutableStateOf(false) }

    if (!adsReady || adFailed) return

    Surface(
        color = NavySurfaceVariant,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        AndroidView(
            factory = { context ->
                val bannerRequest = BannerAdRequest.Builder(AdUnitIds.getBannerAdUnitId(), AdSize.BANNER).build()
                AdView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                     loadAd(bannerRequest, object : com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback<com.google.android.libraries.ads.mobile.sdk.banner.BannerAd> {
                         override fun onAdLoaded(ad: com.google.android.libraries.ads.mobile.sdk.banner.BannerAd) {}
                         override fun onAdFailedToLoad(adError: com.google.android.libraries.ads.mobile.sdk.common.LoadAdError) {
                             adFailed = true
                         }
                     })
                }
            }
        )
    }
}
