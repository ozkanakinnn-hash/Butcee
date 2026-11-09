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
val CoralSoft get() = c(0xFFFFF0E9, 0xFF2E1F1A)
val CoralLight get() = c(0xFFFFF8F4, 0xFF211A18)
val CoralBorder get() = c(0xFFF5D3C6, 0xFF4A3028)
val CoralText get() = c(0xFF6E2C1D, 0xFFF8C0AF)

// İkincil: adaçayı yeşili (güven, güvenli gıda)
val SageGreen get() = c(0xFF2F9478, 0xFF5CC4A6)
val SageDark get() = c(0xFF1F6B57, 0xFF93DBC5)
val SageSoft get() = c(0xFFE8F5EF, 0xFF152925)
val SageBorder get() = c(0xFFBFE4D6, 0xFF254239)
val SageText get() = c(0xFF14523F, 0xFFAEE6D4)

// Üçüncül: bal / papatya
val OchreWarm get() = c(0xFFE5A92F, 0xFFEBC160)
val OchreDark get() = c(0xFF8C640A, 0xFFF1D08A)
val OchreSoft get() = c(0xFFFFF7E3, 0xFF2A2618)
val OchreBorder get() = c(0xFFF2DFA6, 0xFF493E21)
val OchreText get() = c(0xFF6A4A05, 0xFFF3D99C)

// Vurgu: gök mavisi ve lavanta
val SlateBlue get() = c(0xFF3F7AA0, 0xFF86B9DB)
val SlateBlueSoft get() = c(0xFFECF3F9, 0xFF17222C)
val BlueBorder get() = c(0xFFCADDED, 0xFF293A49)
val SoftPurple get() = c(0xFF7D6AAE, 0xFFB7A8DD)
val SoftPurpleSoft get() = c(0xFFF3F0FA, 0xFF201D2E)

// Zemin ve yüzeyler
val CanvasBackground get() = c(0xFFFAF6F1, 0xFF0C0E11)
val CardSurface get() = c(0xFFFFFFFF, 0xFF15181D)
val CardBorder get() = c(0xFFEAE2D7, 0xFF262B33)
val DividerColor get() = c(0xFFF1EAE0, 0xFF1E2228)

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
val ReactionAllergySoft get() = c(0xFFFDECEE, 0xFF2E1C1F)
val AllergyBorder get() = c(0xFFF4C4C9, 0xFF4C282D)
val AllergyText get() = c(0xFF8A1E32, 0xFFF7B0B7)
val SuccessGreen get() = c(0xFF1E8E4E, 0xFF6FD39B)

// Renkli dolgu (mercan/adaçayı/alerji butonları, seçili çipler) üzerindeki yazı ve simgeler.
// Açık temada beyaz; koyu temada dolgular açık tonlu olduğundan koyu kahve (okunurluk için).
val OnAccent get() = c(0xFFFFFFFF, 0xFF26140E)
// Fotoğraf ve turuncu→pembe geçişli kahraman kartları her iki temada da koyu kalır: üzerindeki yazı beyaz.
val OnHero = Color(0xFFFFFFFF)
// Kahraman kartlarındaki beyaz etiketlerin yazısı (zemin hep beyaz olduğu için sabit koyu mercan)
val HeroPillText = Color(0xFFB9502F)
// Fotoğraf üstü karartma (yazının okunması için)
val PhotoScrim = Color(0xA6000000)
// Logo beyaz zemin üzerinde tasarlandı; her iki temada beyaz daire içinde gösterilir.
val LogoBackdrop = Color(0xFFFFFFFF)
// Grafik ızgara çizgileri
val ChartGrid get() = c(0x66D9CFC3, 0x403A4049)
// "100 Gıda" ilerleme çubuğu: palet tonlarıyla gökkuşağı
val RainbowGradient: List<Color> get() = if (ThemeState.dark)
    listOf(Color(0xFFF0828A), Color(0xFFF08C72), Color(0xFFEBC160), Color(0xFF5CC4A6), Color(0xFF86B9DB), Color(0xFFB7A8DD))
else listOf(Color(0xFFEE7B84), Color(0xFFE8735A), Color(0xFFE5A92F), Color(0xFF2F9478), Color(0xFF3F7AA0), Color(0xFF7D6AAE))

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
    val primary = Color(0xFFF08C72); val primaryContainer = Color(0xFF2E1F1A); val onPrimaryContainer = Color(0xFFF8C0AF)
    val secondary = Color(0xFF5CC4A6); val secondaryContainer = Color(0xFF152925); val onSecondaryContainer = Color(0xFFAEE6D4)
    val tertiary = Color(0xFFEBC160); val tertiaryContainer = Color(0xFF2A2618); val onTertiaryContainer = Color(0xFFF3D99C)
    val background = Color(0xFF0C0E11); val surface = Color(0xFF15181D); val surfaceVariant = Color(0xFF1C2026)
    val onSurface = Color(0xFFEFE9E2); val onSurfaceVariant = Color(0xFFBDB5AC); val outline = Color(0xFF3A4049)
    val outlineVariant = Color(0xFF262B33); val error = Color(0xFFF0828A)
}

/** Kullanıcının tema tercihi: "system" (telefona göre), "light" veya "dark". Ayarlar'dan değişir, kaydedilir. */
object ThemePrefs {
    private const val PREFS = "app_prefs"
    private const val KEY = "theme_mode"
    var mode by mutableStateOf("system")
        private set

    fun init(context: android.content.Context) {
        mode = context.getSharedPreferences(PREFS, android.content.Context.MODE_PRIVATE).getString(KEY, "system") ?: "system"
    }

    fun set(context: android.content.Context, value: String) {
        mode = value
        context.getSharedPreferences(PREFS, android.content.Context.MODE_PRIVATE).edit().putString(KEY, value).apply()
    }
}
