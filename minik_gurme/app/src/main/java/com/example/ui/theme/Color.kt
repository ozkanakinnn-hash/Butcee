package com.example.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

/**
 * Minik Gurme paleti — "sıcak kayısı + adaçayı + bal" (göz yormayan, iştah açıcı tonlar).
 *
 * Her renk açık ve koyu tema için ayrı tanımlıdır; ekranlar aynı adları kullanır, seçim [ThemeState.dark]'a
 * göre yapılır (sistem ayarı). Koyu temada saf siyah yerine yumuşak kömür grisi zemin, yazılarda saf beyaz
 * yerine sıcak kırık beyaz kullanılır; pastel kartlar koyu temada aynı rengin koyu tonuna döner.
 */
object ThemeState {
    var dark by mutableStateOf(false)
}

private fun c(light: Long, dark: Long): Color = if (ThemeState.dark) Color(dark) else Color(light)

// Ana renk: sıcak kayısı / mercan
val CoralPrimary get() = c(0xFFE8735A, 0xFFF08C72)
val CoralDark get() = c(0xFFB9502F, 0xFFF7AE98)
val CoralSoft get() = c(0xFFFFF0E9, 0xFF3A2721)
val CoralLight get() = c(0xFFFFF8F4, 0xFF2B221F)
val CoralBorder get() = c(0xFFF5D3C6, 0xFF5B3B31)
val CoralText get() = c(0xFF6E2C1D, 0xFFF8C0AF)

// İkincil: adaçayı yeşili (güven, güvenli gıda)
val SageGreen get() = c(0xFF2F9478, 0xFF5CC4A6)
val SageDark get() = c(0xFF1F6B57, 0xFF93DBC5)
val SageSoft get() = c(0xFFE8F5EF, 0xFF1C3530)
val SageBorder get() = c(0xFFBFE4D6, 0xFF2E5248)
val SageText get() = c(0xFF14523F, 0xFFAEE6D4)

// Üçüncül: bal / papatya
val OchreWarm get() = c(0xFFE5A92F, 0xFFEBC160)
val OchreDark get() = c(0xFF8C640A, 0xFFF1D08A)
val OchreSoft get() = c(0xFFFFF7E3, 0xFF35301F)
val OchreBorder get() = c(0xFFF2DFA6, 0xFF5A4C29)
val OchreText get() = c(0xFF6A4A05, 0xFFF3D99C)

// Vurgu: gök mavisi ve lavanta
val SlateBlue get() = c(0xFF3F7AA0, 0xFF86B9DB)
val SlateBlueSoft get() = c(0xFFECF3F9, 0xFF1D2B37)
val BlueBorder get() = c(0xFFCADDED, 0xFF33485A)
val SoftPurple get() = c(0xFF7D6AAE, 0xFFB7A8DD)
val SoftPurpleSoft get() = c(0xFFF3F0FA, 0xFF29253A)

// Zemin ve yüzeyler
val CanvasBackground get() = c(0xFFFAF6F1, 0xFF14171C)
val CardSurface get() = c(0xFFFFFFFF, 0xFF1D2127)
val CardBorder get() = c(0xFFEAE2D7, 0xFF2F353E)
val DividerColor get() = c(0xFFF1EAE0, 0xFF272C33)

// Yazı renkleri (açıkta sıcak koyu kahve-gri, koyuda sıcak kırık beyaz)
val TextPrimary get() = c(0xFF27221E, 0xFFEFE9E2)
val TextSecondary get() = c(0xFF5A524B, 0xFFBDB5AC)
val TextTertiary get() = c(0xFF857D75, 0xFF8F959E)

// Tepki renkleri
val ReactionLoved get() = SageGreen
val ReactionLovedSoft get() = SageSoft
val ReactionNeutral get() = SlateBlue
val ReactionNeutralSoft get() = SlateBlueSoft
val ReactionRejected get() = OchreWarm
val ReactionRejectedSoft get() = OchreSoft
val ReactionAllergy get() = c(0xFFD6454E, 0xFFF0828A)
val ReactionAllergySoft get() = c(0xFFFDECEE, 0xFF3A2327)
val AllergyBorder get() = c(0xFFF4C4C9, 0xFF5E3238)
val AllergyText get() = c(0xFF8A1E32, 0xFFF7B0B7)
val SuccessGreen get() = c(0xFF1E8E4E, 0xFF6FD39B)

// Kahraman kartı geçişi (turuncu → mercan → pembe; koyu temada biraz daha derin)
val HeroGradient: List<Color> get() = if (ThemeState.dark)
    listOf(Color(0xFFD9823A), Color(0xFFCF5F4C), Color(0xFFB84A68))
else listOf(Color(0xFFF7A548), Color(0xFFF27A5E), Color(0xFFEE6A86))

// Material renk şemaları için sabit değerler
internal object Light {
    val primary = Color(0xFFE8735A); val primaryContainer = Color(0xFFFFF0E9); val onPrimaryContainer = Color(0xFF6E2C1D)
    val secondary = Color(0xFF2F9478); val secondaryContainer = Color(0xFFE8F5EF); val onSecondaryContainer = Color(0xFF14523F)
    val tertiary = Color(0xFFE5A92F); val tertiaryContainer = Color(0xFFFFF7E3); val onTertiaryContainer = Color(0xFF6A4A05)
    val background = Color(0xFFFAF6F1); val surface = Color(0xFFFFFFFF); val surfaceVariant = Color(0xFFF3EDE5)
    val onSurface = Color(0xFF27221E); val onSurfaceVariant = Color(0xFF5A524B); val outline = Color(0xFFD9CFC3)
    val outlineVariant = Color(0xFFEAE2D7); val error = Color(0xFFD6454E)
}

internal object Dark {
    val primary = Color(0xFFF08C72); val primaryContainer = Color(0xFF3A2721); val onPrimaryContainer = Color(0xFFF8C0AF)
    val secondary = Color(0xFF5CC4A6); val secondaryContainer = Color(0xFF1C3530); val onSecondaryContainer = Color(0xFFAEE6D4)
    val tertiary = Color(0xFFEBC160); val tertiaryContainer = Color(0xFF35301F); val onTertiaryContainer = Color(0xFFF3D99C)
    val background = Color(0xFF14171C); val surface = Color(0xFF1D2127); val surfaceVariant = Color(0xFF262B32)
    val onSurface = Color(0xFFEFE9E2); val onSurfaceVariant = Color(0xFFBDB5AC); val outline = Color(0xFF454C56)
    val outlineVariant = Color(0xFF2F353E); val error = Color(0xFFF0828A)
}
