package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.data.AppColorTheme

// ─────────────────────────── 1) SARAY: Krem & Osmanlı Altını ───────────────────────────
private val SarayLight = lightColorScheme(
  primary = WarmGoldLight,
  onPrimary = CreamBackgroundLight,
  primaryContainer = CreamSurfaceVariantLight,
  onPrimaryContainer = CharcoalTextPrimaryLight,
  secondary = SageGreenLight,
  onSecondary = CreamBackgroundLight,
  secondaryContainer = CreamSurfaceVariantLight,
  onSecondaryContainer = CharcoalTextPrimaryLight,
  tertiary = WarmGoldAccent,
  background = CreamBackgroundLight,
  onBackground = CharcoalTextPrimaryLight,
  surface = CreamSurfaceLight,
  onSurface = CharcoalTextPrimaryLight,
  surfaceVariant = CreamSurfaceVariantLight,
  onSurfaceVariant = SlateTextSecondaryLight,
  outline = DividerLight,
  outlineVariant = DividerLight.copy(alpha = 0.5f)
)

private val SarayDark = nearBlackScheme(
  primary = WarmGoldDark,
  secondary = SageGreenDark,
  tertiary = NeonGoldGlow,
  tint = Color(0xFF1A1712) // altın tonlu çok hafif ısı
)

// ───────────────────── 2) İZNİK ÇİNİSİ: Kobalt, Turkuaz, Mercan Kırmızısı ─────────────────────
// Klasik 16. yy İznik çinilerindeki kobalt (kobalt oksit), turkuaz (bakır) ve mercan kırmızısı (Ermeni bolü).
private val IznikLight = lightColorScheme(
  primary = Color(0xFF1A3F7A),
  onPrimary = Color(0xFFFAFAF6),
  primaryContainer = Color(0xFFDDE7F3),
  onPrimaryContainer = Color(0xFF0F2547),
  secondary = Color(0xFF2A8C9C),
  onSecondary = Color.White,
  secondaryContainer = Color(0xFFD5EEF0),
  onSecondaryContainer = Color(0xFF0E3A41),
  tertiary = Color(0xFFC0392B),
  background = Color(0xFFFAFAF6),
  onBackground = Color(0xFF1B2230),
  surface = Color(0xFFF2F5F8),
  onSurface = Color(0xFF1B2230),
  surfaceVariant = Color(0xFFE3EAF2),
  onSurfaceVariant = Color(0xFF55627A),
  outline = Color(0xFFCBD5E1),
  outlineVariant = Color(0xFFCBD5E1).copy(alpha = 0.5f)
)

private val IznikDark = nearBlackScheme(
  primary = Color(0xFF8DB1EA),
  secondary = Color(0xFF55C6CF),
  tertiary = Color(0xFFE8705F),
  tint = Color(0xFF10141C) // kobalt tonlu
)

// ───────────────────────── 3) ZÜMRÜT: Zümrüt Yeşili & Altın ─────────────────────────
private val ZumrutLight = lightColorScheme(
  primary = Color(0xFF1E6B4F),
  onPrimary = Color(0xFFF5F8F3),
  primaryContainer = Color(0xFFDCEBE2),
  onPrimaryContainer = Color(0xFF0D3325),
  secondary = Color(0xFFA9831B),
  onSecondary = Color.White,
  secondaryContainer = Color(0xFFF3EACB),
  onSecondaryContainer = Color(0xFF3D2F06),
  tertiary = Color(0xFFC9A227),
  background = Color(0xFFF5F8F3),
  onBackground = Color(0xFF1A241F),
  surface = Color(0xFFEDF3EE),
  onSurface = Color(0xFF1A241F),
  surfaceVariant = Color(0xFFDFEAE2),
  onSurfaceVariant = Color(0xFF52665A),
  outline = Color(0xFFC6D6CB),
  outlineVariant = Color(0xFFC6D6CB).copy(alpha = 0.5f)
)

private val ZumrutDark = nearBlackScheme(
  primary = Color(0xFF6FD3A2),
  secondary = Color(0xFFE5B869),
  tertiary = Color(0xFFE5C55A),
  tint = Color(0xFF0E1511) // zümrüt tonlu
)

/**
 * Koyu mod: arka plan neredeyse saf siyah (#0A0A0A civarı), yüzeyler bir-iki kademe açık.
 * [tint] temanın rengini yüzeylere çok hafifçe yansıtır.
 */
private fun nearBlackScheme(primary: Color, secondary: Color, tertiary: Color, tint: Color): ColorScheme {
  val background = Color(0xFF050505)
  val surface = lerpColor(Color(0xFF0E0E0E), tint, 0.5f)
  val surfaceVariant = lerpColor(Color(0xFF181818), tint, 0.4f)
  val outline = Color(0xFF2A2A2A)
  val onBg = Color(0xFFEDEBE6)
  return darkColorScheme(
    primary = primary,
    onPrimary = Color(0xFF050505),
    primaryContainer = surfaceVariant,
    onPrimaryContainer = onBg,
    secondary = secondary,
    onSecondary = Color(0xFF050505),
    secondaryContainer = surfaceVariant,
    onSecondaryContainer = onBg,
    tertiary = tertiary,
    background = background,
    onBackground = onBg,
    surface = surface,
    onSurface = onBg,
    surfaceVariant = surfaceVariant,
    onSurfaceVariant = Color(0xFFA6A39C),
    outline = outline,
    outlineVariant = outline.copy(alpha = 0.5f)
  )
}

private fun lerpColor(a: Color, b: Color, t: Float) = Color(
  red = a.red + (b.red - a.red) * t,
  green = a.green + (b.green - a.green) * t,
  blue = a.blue + (b.blue - a.blue) * t,
  alpha = 1f
)

fun colorSchemeFor(theme: AppColorTheme, dark: Boolean): ColorScheme = when (theme) {
  AppColorTheme.SARAY -> if (dark) SarayDark else SarayLight
  AppColorTheme.IZNIK -> if (dark) IznikDark else IznikLight
  AppColorTheme.ZUMRUT -> if (dark) ZumrutDark else ZumrutLight
}

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  colorTheme: AppColorTheme = AppColorTheme.SARAY,
  content: @Composable () -> Unit
) {
  val colorScheme = colorSchemeFor(colorTheme, darkTheme)
  val view = LocalView.current

  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as Activity).window
      window.statusBarColor = colorScheme.background.toArgb()
      window.navigationBarColor = colorScheme.surface.toArgb()
      val insetsController = WindowCompat.getInsetsController(window, view)
      insetsController.isAppearanceLightStatusBars = !darkTheme
      insetsController.isAppearanceLightNavigationBars = !darkTheme
    }
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}

/** Uygulamanın o anki teması koyu mu? (Sistem ayarı değil, kullanıcının seçtiği tema.) */
@Composable
fun isAppInDarkTheme(): Boolean = MaterialTheme.colorScheme.background.luminance() < 0.5f
