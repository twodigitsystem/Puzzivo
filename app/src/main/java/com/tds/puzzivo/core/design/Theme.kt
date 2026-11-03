package com.tds.puzzivo.core.design

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.tds.puzzivo.core.model.GameTheme

@Composable
fun PuzzivoTheme(
    gameTheme: GameTheme = GameTheme.NAVY_DARK,
    content: @Composable () -> Unit
) {
    val colorScheme = darkColorScheme(
        primary = gameTheme.getPrimaryColor(),
        secondary = gameTheme.getSecondaryColor(),
        tertiary = gameTheme.getAccentColor(),
        background = gameTheme.getBackgroundColor(),
        surface = gameTheme.getSurfaceColor(),
        surfaceVariant = gameTheme.getSurfaceColor(),
        onPrimary = gameTheme.getBackgroundColor(),
        onSecondary = gameTheme.getBackgroundColor(),
        onBackground = TextPrimary,
        onSurface = TextPrimary
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = false
                insetsController.isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
