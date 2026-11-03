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

private val LightColorScheme = lightColorScheme(
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

private val DarkColorScheme = darkColorScheme(
  primary = WarmGoldDark,
  onPrimary = NightBackgroundDark,
  primaryContainer = NightSurfaceVariantDark,
  onPrimaryContainer = CreamTextPrimaryDark,
  secondary = SageGreenDark,
  onSecondary = NightBackgroundDark,
  secondaryContainer = NightSurfaceVariantDark,
  onSecondaryContainer = CreamTextPrimaryDark,
  tertiary = NeonGoldGlow,
  background = NightBackgroundDark,
  onBackground = CreamTextPrimaryDark,
  surface = NightSurfaceDark,
  onSurface = CreamTextPrimaryDark,
  surfaceVariant = NightSurfaceVariantDark,
  onSurfaceVariant = SandTextSecondaryDark,
  outline = DividerDark,
  outlineVariant = DividerDark.copy(alpha = 0.5f)
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
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
