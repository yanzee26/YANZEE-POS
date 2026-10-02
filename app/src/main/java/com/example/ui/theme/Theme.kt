package com.example.ui.theme

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
    primary = NavyAccent,
    onPrimary = PureWhite,
    primaryContainer = NavySurface,
    onPrimaryContainer = PureWhite,
    secondary = NavyLight,
    onSecondary = PureWhite,
    background = NavyDark,
    surface = NavyPrimary,
    onBackground = PureWhite,
    onSurface = PureWhite,
    surfaceVariant = NavySurface,
    onSurfaceVariant = TextMuted,
    outline = NavySurface
)

// Primary Light Color Scheme: White scheme with Navy Blue accents
private val LightColorScheme = lightColorScheme(
    primary = NavyPrimary,
    onPrimary = PureWhite,
    primaryContainer = SurfaceVariantLight,
    onPrimaryContainer = NavyPrimary,
    secondary = NavyLight,
    onSecondary = PureWhite,
    secondaryContainer = SurfaceVariantLight,
    onSecondaryContainer = NavyLight,
    tertiary = NavyAccent,
    onTertiary = PureWhite,
    background = BackgroundLight,
    onBackground = TextPrimary,
    surface = SurfaceCard,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondary,
    outline = SurfaceBorder,
    outlineVariant = SurfaceVariantLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Keep clean white with navy accents by default
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.primary.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
