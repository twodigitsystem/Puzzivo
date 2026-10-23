package com.tds.puzzivo.core.design

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = AccentYellow,
    secondary = AccentCyan,
    tertiary = AccentGreen,
    background = NavyBackground,
    surface = NavySurface,
    surfaceVariant = NavySurfaceVariant,
    onPrimary = NavyBackground,
    onSecondary = NavyBackground,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun PuzzivoTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
