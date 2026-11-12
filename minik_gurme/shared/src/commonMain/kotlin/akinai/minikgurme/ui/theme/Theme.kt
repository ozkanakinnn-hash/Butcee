package akinai.minikgurme.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Light.primary, onPrimary = Color.White,
    primaryContainer = Light.primaryContainer, onPrimaryContainer = Light.onPrimaryContainer,
    secondary = Light.secondary, onSecondary = Color.White,
    secondaryContainer = Light.secondaryContainer, onSecondaryContainer = Light.onSecondaryContainer,
    tertiary = Light.tertiary, onTertiary = Color(0xFF3A1608),
    tertiaryContainer = Light.tertiaryContainer, onTertiaryContainer = Light.onTertiaryContainer,
    background = Light.background, onBackground = Light.onSurface,
    surface = Light.surface, onSurface = Light.onSurface,
    surfaceVariant = Light.surfaceVariant, onSurfaceVariant = Light.onSurfaceVariant,
    surfaceContainer = Light.surface, surfaceContainerLow = Light.background, surfaceContainerHigh = Light.surfaceVariant,
    outline = Light.outline, outlineVariant = Light.outlineVariant, error = Light.error
)

private val DarkColorScheme = darkColorScheme(
    primary = Dark.primary, onPrimary = Color(0xFF0F1330),
    primaryContainer = Dark.primaryContainer, onPrimaryContainer = Dark.onPrimaryContainer,
    secondary = Dark.secondary, onSecondary = Color(0xFF07241D),
    secondaryContainer = Dark.secondaryContainer, onSecondaryContainer = Dark.onSecondaryContainer,
    tertiary = Dark.tertiary, onTertiary = Color(0xFF3A1608),
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
    SystemBarsAppearance(darkTheme)
    MaterialTheme(colorScheme = colorScheme, typography = appTypography(), content = content)
}

/** Durum/gezinme çubuğu simgelerinin rengi (Android: pencere; iOS: sistem yönetir). */
@Composable
expect fun SystemBarsAppearance(darkTheme: Boolean)
