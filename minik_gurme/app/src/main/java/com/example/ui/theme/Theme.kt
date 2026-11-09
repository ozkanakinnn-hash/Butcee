package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Light.primary, onPrimary = Color.White,
    primaryContainer = Light.primaryContainer, onPrimaryContainer = Light.onPrimaryContainer,
    secondary = Light.secondary, onSecondary = Color.White,
    secondaryContainer = Light.secondaryContainer, onSecondaryContainer = Light.onSecondaryContainer,
    tertiary = Light.tertiary, onTertiary = Color(0xFF3B2A03),
    tertiaryContainer = Light.tertiaryContainer, onTertiaryContainer = Light.onTertiaryContainer,
    background = Light.background, onBackground = Light.onSurface,
    surface = Light.surface, onSurface = Light.onSurface,
    surfaceVariant = Light.surfaceVariant, onSurfaceVariant = Light.onSurfaceVariant,
    surfaceContainer = Light.surface, surfaceContainerLow = Light.background, surfaceContainerHigh = Light.surfaceVariant,
    outline = Light.outline, outlineVariant = Light.outlineVariant, error = Light.error
)

private val DarkColorScheme = darkColorScheme(
    primary = Dark.primary, onPrimary = Color(0xFF3A160C),
    primaryContainer = Dark.primaryContainer, onPrimaryContainer = Dark.onPrimaryContainer,
    secondary = Dark.secondary, onSecondary = Color(0xFF0B2A22),
    secondaryContainer = Dark.secondaryContainer, onSecondaryContainer = Dark.onSecondaryContainer,
    tertiary = Dark.tertiary, onTertiary = Color(0xFF2E2203),
    tertiaryContainer = Dark.tertiaryContainer, onTertiaryContainer = Dark.onTertiaryContainer,
    background = Dark.background, onBackground = Dark.onSurface,
    surface = Dark.surface, onSurface = Dark.onSurface,
    surfaceVariant = Dark.surfaceVariant, onSurfaceVariant = Dark.onSurfaceVariant,
    surfaceContainer = Dark.surface, surfaceContainerLow = Dark.background, surfaceContainerHigh = Dark.surfaceVariant,
    outline = Dark.outline, outlineVariant = Dark.outlineVariant, error = Dark.error
)

/** Tema sistem ayarını izler (açık / koyu). Palet renkleri [ThemeState] üzerinden aynı seçimi kullanır. */
@Composable
fun MinikGurmeTheme(
    darkTheme: Boolean = when (ThemePrefs.mode) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
    },
    content: @Composable () -> Unit
) {
    ThemeState.dark = darkTheme
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        // Yalnızca tema değişince uygula (her çizimde uygulamak diyaloglarla sonsuz yeniden ölçüme yol açıyordu)
        androidx.compose.runtime.LaunchedEffect(darkTheme) {
            val window = (view.context as? Activity)?.window ?: return@LaunchedEffect
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
