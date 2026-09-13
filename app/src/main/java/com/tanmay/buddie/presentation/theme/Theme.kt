package com.tanmay.buddie.presentation.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Comprehensive Dark Color Scheme using Design System Tokens
val BuddieDarkColorScheme = darkColorScheme(
    primary = AccentBlueLight,
    onPrimary = CharcoalBackground,
    primaryContainer = AccentBlue,
    onPrimaryContainer = TextPrimary,
    secondary = AccentBlueLight,
    onSecondary = CharcoalBackground,
    secondaryContainer = AccentBlueSubtle,
    onSecondaryContainer = AccentBlueLight,
    background = CharcoalBackground,
    onBackground = TextPrimary,
    surface = CharcoalSurface,
    onSurface = TextPrimary,
    surfaceVariant = CharcoalSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = BorderOutline,
    outlineVariant = DividerSubtle,
    error = StatusError,
    onError = CharcoalBackground,
    errorContainer = StatusErrorContainer,
    onErrorContainer = StatusOnErrorContainer
)

@Composable
fun BuddieTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // True Edge-to-Edge System Bar Blending
            window.statusBarColor = CharcoalBackground.toArgb()
            window.navigationBarColor = CharcoalBackground.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = BuddieDarkColorScheme,
        typography = BuddieTypography,
        shapes = BuddieShapes,
        content = content
    )
}
