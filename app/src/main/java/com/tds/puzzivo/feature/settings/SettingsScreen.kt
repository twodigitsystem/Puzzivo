package com.tds.puzzivo.feature.settings

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.core.net.toUri
import com.tds.puzzivo.PuzzivoApplication
import com.tds.puzzivo.BuildConfig
import com.tds.puzzivo.R
import com.tds.puzzivo.core.design.AccentYellow
import com.tds.puzzivo.core.design.ConfirmationDialog
import com.tds.puzzivo.core.design.NavyBackground
import com.tds.puzzivo.core.design.NavySurface
import com.tds.puzzivo.core.design.PuzzivoCard
import com.tds.puzzivo.core.design.PuzzivoTopBar
import com.tds.puzzivo.core.design.SecondaryButton
import com.tds.puzzivo.core.design.TextPrimary
import com.tds.puzzivo.core.design.TextSecondary
import com.tds.puzzivo.core.util.AppUtilities

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val prefs by viewModel.userPreferences.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var showResetConfirmation by remember { mutableStateOf(false) }

    Surface(
        color = NavyBackground,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            PuzzivoTopBar(
                title = stringResource(R.string.settings_title),
                onBackClick = onBackClick
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(scrollState)
            ) {
                // Sound & Haptics Section
                PuzzivoCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.settings_preferences),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        ListItem(
                            headlineContent = { Text(stringResource(R.string.settings_sound_effects), fontWeight = FontWeight.Bold, color = TextPrimary) },
                            supportingContent = { Text(stringResource(R.string.settings_sound_desc), color = TextSecondary) },
                            leadingContent = { Icon(Icons.AutoMirrored.Outlined.VolumeUp, contentDescription = null, tint = AccentYellow) },
                            trailingContent = {
                                Switch(
                                    checked = prefs.soundEnabled,
                                    onCheckedChange = { viewModel.toggleSound(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = NavyBackground,
                                        checkedTrackColor = AccentYellow
                                    )
                                )
                            },
                            colors = androidx.compose.material3.ListItemDefaults.colors(containerColor = NavySurface)
                        )

                        ListItem(
                            headlineContent = { Text(stringResource(R.string.settings_haptics), fontWeight = FontWeight.Bold, color = TextPrimary) },
                            supportingContent = { Text(stringResource(R.string.settings_haptics_desc), color = TextSecondary) },
                            leadingContent = { Icon(Icons.Outlined.Vibration, contentDescription = null, tint = AccentYellow) },
                            trailingContent = {
                                Switch(
                                    checked = prefs.hapticsEnabled,
                                    onCheckedChange = { viewModel.toggleHaptics(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = NavyBackground,
                                        checkedTrackColor = AccentYellow
                                    )
                                )
                            },
                            colors = androidx.compose.material3.ListItemDefaults.colors(containerColor = NavySurface)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // App Actions Section
                PuzzivoCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.settings_spread_word),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        SettingsActionRow(
                            icon = Icons.Outlined.Share,
                            title = stringResource(R.string.settings_share_app),
                            description = stringResource(R.string.settings_share_desc),
                            onClick = { AppUtilities.shareApp(context) }
                        )

                        SettingsActionRow(
                            icon = Icons.Outlined.Star,
                            title = stringResource(R.string.settings_rate_app),
                            description = stringResource(R.string.settings_rate_desc),
                            onClick = {
                                if (context is Activity) {
                                    AppUtilities.rateApp(context)
                                } else {
                                    AppUtilities.openPlayStorePage(context)
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // About & Data Section
                PuzzivoCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.settings_about_data),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        SettingsActionRow(
                            icon = Icons.Outlined.Lock,
                            title = stringResource(R.string.settings_privacy_policy),
                            description = stringResource(R.string.settings_privacy_desc),
                            onClick = {
                                try {
                                     val intent = Intent(Intent.ACTION_VIEW, AppUtilities.PRIVACY_POLICY_URL.toUri())
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Log.e("SettingsScreen", "Unable to open privacy policy", e)
                                }
                            }
                        )

                        val application = context.applicationContext as? PuzzivoApplication
                        if (context is Activity && application?.adsConsentManager?.isPrivacyOptionsRequired() == true) {
                            Spacer(modifier = Modifier.height(12.dp))

                            SettingsActionRow(
                                icon = Icons.Outlined.Shield,
                                title = stringResource(R.string.settings_privacy_choices),
                                description = stringResource(R.string.settings_privacy_choices_desc),
                                onClick = {
                                    application.adsConsentManager.showPrivacyOptions(context) {
                                        application.initializeAds(context)
                                    }
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        SettingsActionRow(
                            icon = Icons.Outlined.Email,
                            title = stringResource(R.string.settings_support),
                            description = stringResource(R.string.settings_support_desc),
                            onClick = {
                                try {
                                     val intent = Intent(Intent.ACTION_SENDTO, "mailto:${AppUtilities.SUPPORT_EMAIL}".toUri())
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Log.e("SettingsScreen", "Unable to open support email", e)
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = { showResetConfirmation = true },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Outlined.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.settings_reset_data),
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = stringResource(R.string.settings_reset_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            modifier = Modifier.padding(start = 16.dp, top = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(R.string.settings_version_format, BuildConfig.VERSION_NAME),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showResetConfirmation) {
        ConfirmationDialog(
            title = stringResource(R.string.settings_reset_title),
            message = stringResource(R.string.settings_reset_message),
            confirmText = stringResource(R.string.action_reset_data),
            dismissText = stringResource(R.string.action_cancel),
            onConfirm = {
                viewModel.clearAllProgress()
                showResetConfirmation = false
            },
            onDismiss = { showResetConfirmation = false }
        )
    }
}

@Composable
private fun SettingsActionRow(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title, fontWeight = FontWeight.Bold, color = TextPrimary) },
        supportingContent = { Text(description, color = TextSecondary) },
        leadingContent = { Icon(icon, contentDescription = null, tint = AccentYellow) },
        trailingContent = { Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = TextSecondary) },
        modifier = Modifier.clickable(onClick = onClick),
        colors = androidx.compose.material3.ListItemDefaults.colors(containerColor = NavySurface)
    )
}
