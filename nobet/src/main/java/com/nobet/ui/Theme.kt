package com.nobet.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import com.nobet.data.ShiftType
import com.nobet.data.Store

val LightColors: ColorScheme = lightColorScheme(
  primary = Color(0xFF3B6FB6),
  onPrimary = Color.White,
  primaryContainer = Color(0xFFDCE7F7),
  onPrimaryContainer = Color(0xFF0E2A50),
  secondary = Color(0xFF5C7A99),
  onSecondary = Color.White,
  secondaryContainer = Color(0xFFE1E8F0),
  onSecondaryContainer = Color(0xFF1A2433),
  tertiary = Color(0xFF3E9B7A),
  background = Color(0xFFF6F8FB),
  onBackground = Color(0xFF1A2433),
  surface = Color.White,
  onSurface = Color(0xFF1A2433),
  surfaceVariant = Color(0xFFEBEFF5),
  onSurfaceVariant = Color(0xFF5B6878),
  outline = Color(0xFFC9D1DC),
  outlineVariant = Color(0xFFE1E6ED),
  error = Color(0xFFC8605A),
  surfaceContainerLowest = Color.White,
  surfaceContainerLow = Color(0xFFF9FAFC),
  surfaceContainer = Color(0xFFF0F3F8),
  surfaceContainerHigh = Color(0xFFEAEEF4),
  surfaceContainerHighest = Color(0xFFE4E9F0),
)

val DarkColors: ColorScheme = darkColorScheme(
  primary = Color(0xFF8AB4F0),
  onPrimary = Color(0xFF0B1A2E),
  primaryContainer = Color(0xFF24406A),
  onPrimaryContainer = Color(0xFFDCE7F7),
  secondary = Color(0xFF9AB0C8),
  onSecondary = Color(0xFF0E141C),
  secondaryContainer = Color(0xFF243142),
  onSecondaryContainer = Color(0xFFE8EEF6),
  tertiary = Color(0xFF6CC4A1),
  background = Color(0xFF0E141C),
  onBackground = Color(0xFFE8EEF6),
  surface = Color(0xFF161E29),
  onSurface = Color(0xFFE8EEF6),
  surfaceVariant = Color(0xFF1F2935),
  onSurfaceVariant = Color(0xFF9AA8B8),
  outline = Color(0xFF3A4757),
  outlineVariant = Color(0xFF2C3846),
  error = Color(0xFFE38B85),
  surfaceContainerLowest = Color(0xFF0B1017),
  surfaceContainerLow = Color(0xFF131A24),
  surfaceContainer = Color(0xFF161E29),
  surfaceContainerHigh = Color(0xFF1C2532),
  surfaceContainerHighest = Color(0xFF232D3B),
)

/** Gece teması: koğuşta/karanlık odada göz yormayan kırmızımsı, çok loş tema (Premium). */
val NightColors: ColorScheme = darkColorScheme(
  primary = Color(0xFFD9785F),
  onPrimary = Color(0xFF1A0A06),
  primaryContainer = Color(0xFF3A1A12),
  onPrimaryContainer = Color(0xFFE6B8A8),
  secondary = Color(0xFFB0705F),
  onSecondary = Color(0xFF1A0A06),
  secondaryContainer = Color(0xFF2A140F),
  onSecondaryContainer = Color(0xFFE6B8A8),
  tertiary = Color(0xFFC98A5A),
  background = Color(0xFF000000),
  onBackground = Color(0xFFE6B8A8),
  surface = Color(0xFF120A08),
  onSurface = Color(0xFFE6B8A8),
  surfaceVariant = Color(0xFF1C100C),
  onSurfaceVariant = Color(0xFF9E7468),
  outline = Color(0xFF4A2A22),
  outlineVariant = Color(0xFF2E1912),
  error = Color(0xFFE08070),
  surfaceContainerLowest = Color(0xFF000000),
  surfaceContainerLow = Color(0xFF0A0504),
  surfaceContainer = Color(0xFF120A08),
  surfaceContainerHigh = Color(0xFF1A0E0B),
  surfaceContainerHighest = Color(0xFF22130F),
)

/** Amber gece teması: mavi ışığı azaltan sıcak, loş tonlar (Premium). */
val AmberColors: ColorScheme = darkColorScheme(
  primary = Color(0xFFE8A84A),
  onPrimary = Color(0xFF1C1205),
  primaryContainer = Color(0xFF3A2A10),
  onPrimaryContainer = Color(0xFFF2D3A0),
  secondary = Color(0xFFC9964F),
  onSecondary = Color(0xFF1C1205),
  secondaryContainer = Color(0xFF2B1F0D),
  onSecondaryContainer = Color(0xFFF2D3A0),
  tertiary = Color(0xFFB8A15A),
  background = Color(0xFF050402),
  onBackground = Color(0xFFF2D3A0),
  surface = Color(0xFF130E07),
  onSurface = Color(0xFFF2D3A0),
  surfaceVariant = Color(0xFF1E160B),
  onSurfaceVariant = Color(0xFFA88C62),
  outline = Color(0xFF4A3920),
  outlineVariant = Color(0xFF2E2313),
  error = Color(0xFFE09070),
  surfaceContainerLowest = Color(0xFF050402),
  surfaceContainerLow = Color(0xFF0C0904),
  surfaceContainer = Color(0xFF130E07),
  surfaceContainerHigh = Color(0xFF1A130A),
  surfaceContainerHighest = Color(0xFF22190D),
)

val Success = Color(0xFF3E9B7A)
val Warning = Color(0xFFD69A2D)
val Alert = Color(0xFFC8605A)

data class Mode(val dark: Boolean, val night: Boolean)

val LocalMode = staticCompositionLocalOf { Mode(dark = false, night = false) }

@Composable
fun NobetTheme(content: @Composable () -> Unit) {
  val sys = isSystemInDarkTheme()
  val amber = Store.theme == "amber" && Store.premium
  val night = (Store.theme == "night" && Store.premium) || amber
  val dark = night || when (Store.theme) {
    "light" -> false
    "dark" -> true
    else -> sys
  }
  val scheme = when {
    amber -> AmberColors
    night -> NightColors
    dark -> DarkColors
    else -> LightColors
  }
  CompositionLocalProvider(LocalMode provides Mode(dark, night)) {
    MaterialTheme(colorScheme = scheme, content = content)
  }
}

@Composable
fun shiftColor(t: ShiftType): Color {
  val m = LocalMode.current
  val c = Color(t.color)
  return when {
    m.night -> lerp(c, Color(0xFFD9785F), 0.55f).copy(alpha = 0.9f)
    m.dark -> lerp(c, Color.White, 0.22f)
    else -> c
  }
}

fun onColor(c: Color): Color = if (c.luminance() > 0.45f) Color(0xFF1A2433) else Color.White
