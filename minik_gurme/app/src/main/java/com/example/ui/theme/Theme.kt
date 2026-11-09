package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = CoralPrimary,
    onPrimary = Color.White,
    primaryContainer = CoralSoft,
    onPrimaryContainer = CoralDark,
    secondary = SageGreen,
    onSecondary = Color.White,
    secondaryContainer = SageSoft,
    onSecondaryContainer = SageDark,
    tertiary = OchreWarm,
    onTertiary = Color(0xFF3E2D04),
    tertiaryContainer = OchreSoft,
    onTertiaryContainer = OchreDark,
    background = CanvasBackground,
    onBackground = TextPrimary,
    surface = CardSurface,
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFFF4F0EA),
    onSurfaceVariant = TextSecondary,
    outline = CardBorder
)

private val DarkColorScheme = darkColorScheme(
    primary = CoralPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF5C2418),
    onPrimaryContainer = CoralSoft,
    secondary = SageGreen,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF144D46),
    onSecondaryContainer = SageSoft,
    tertiary = OchreWarm,
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF4A3B12),
    onTertiaryContainer = OchreSoft,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkCardBorder
)

@Composable
fun MinikGurmeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
