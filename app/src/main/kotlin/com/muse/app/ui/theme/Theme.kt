package com.muse.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = MuseAmber,
    secondary = MuseAmberGlow,
    tertiary = MuseAmber,
    background = MuseDarkBackground,
    surface = MuseDarkSurfaceLow,
    surfaceVariant = MuseDarkSurfaceMid,
    onPrimary = MuseDarkBackground,
    onSecondary = MuseDarkTextPrimary,
    onBackground = MuseDarkTextPrimary,
    onSurface = MuseDarkTextPrimary,
    onSurfaceVariant = MuseDarkTextSecondary
)

private val LightColorScheme = lightColorScheme(
    primary = MuseAmberDark,
    secondary = MuseAmberGlow,
    tertiary = MuseAmberDark,
    background = MuseLightBackground,
    surface = MuseLightSurfaceLow,
    surfaceVariant = MuseLightSurfaceMid,
    onPrimary = MuseLightBackground,
    onSecondary = MuseLightTextPrimary,
    onBackground = MuseLightTextPrimary,
    onSurface = MuseLightTextPrimary,
    onSurfaceVariant = MuseLightTextSecondary
)

@Composable
fun MuseTheme(
    isLightMode: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (isLightMode) LightColorScheme else DarkColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = isLightMode
            insetsController.isAppearanceLightNavigationBars = isLightMode
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
