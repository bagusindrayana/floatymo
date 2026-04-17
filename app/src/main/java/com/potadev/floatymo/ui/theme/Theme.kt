package com.potadev.floatymo.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val FloatyMoDarkScheme = darkColorScheme(
    primary = CyanPrimary,
    onPrimary = DarkBg,
    primaryContainer = CyanSubtle,
    onPrimaryContainer = CyanLight,
    secondary = VioletSecondary,
    onSecondary = DarkBg,
    secondaryContainer = VioletDark,
    onSecondaryContainer = VioletLight,
    tertiary = StatusInfo,
    background = DarkBg,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkCard,
    onSurfaceVariant = TextSecondary,
    error = StatusError,
    onError = TextPrimary,
    outline = DarkBorder,
    outlineVariant = TextMuted
)

@Composable
fun FloatyMoTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = FloatyMoDarkScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = DarkBg.toArgb()
            window.navigationBarColor = DarkBg.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}