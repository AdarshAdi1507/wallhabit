package com.dev.habitwallpaper.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = GlowEmerald,
    secondary = EmeraldSoft,
    tertiary = GridLow,
    background = DarkGreenBlack,
    surface = DarkGreenBlack,
    onPrimary = DarkGreenBlack,
    onSecondary = DarkGreenBlack,
    onBackground = GlowEmerald,
    onSurface = GlowEmerald
)

private val LightColorScheme = lightColorScheme(
    primary = ForestDeep,
    secondary = EmeraldSoft,
    tertiary = GridMedium,
    background = MintSurface,
    surface = MintSurface,
    onPrimary = MintSurface,
    onSecondary = MintSurface,
    onBackground = DarkGrayText,
    onSurface = DarkGrayText
)

@Composable
fun HabitWallpaperTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Disable dynamic color to maintain brand identity
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
