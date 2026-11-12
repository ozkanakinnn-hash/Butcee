package akinai.minikgurme.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

/**
 * Minik Gurme paleti — "Yaban Mersini & Krem" (sakin, güven veren; sıcak mercan vurgulu).
 *
 * Her renk açık ve koyu tema için ayrı tanımlıdır; ekranlar aynı adları kullanır, seçim [ThemeState.dark]'a
 * göre yapılır (sistem ayarı). Koyu temada saf siyah yerine yumuşak kömür grisi zemin, yazılarda saf beyaz
 * yerine sıcak kırık beyaz kullanılır; pastel kartlar koyu temada aynı rengin koyu tonuna döner.
 */
object ThemeState {
    var dark by mutableStateOf(false)
}

private fun c(light: Long, dark: Long): Color = if (ThemeState.dark) Color(dark) else Color(light)

// Ana renk: yaban mersini (indigo)
val CoralPrimary get() = c(0xFF4F63D8, 0xFF8C9BF2)
val CoralDark get() = c(0xFF3446B0, 0xFFB3BEF7)
val CoralSoft get() = c(0xFFECEEFC, 0xFF1C2140)
val CoralLight get() = c(0xFFF5F6FE, 0xFF161A30)
val CoralBorder get() = c(0xFFCDD3F6, 0xFF2E3566)
val CoralText get() = c(0xFF23307A, 0xFFC4CCFA)

// Başarı/güvenli: deniz yeşili
val SageGreen get() = c(0xFF2E9C84, 0xFF5CCBB1)
val SageDark get() = c(0xFF1F7A66, 0xFF8FDCC9)
val SageSoft get() = c(0xFFE4F5F0, 0xFF122A25)
val SageBorder get() = c(0xFFBDE5DA, 0xFF22463E)
val SageText get() = c(0xFF145646, 0xFFAEE8D9)

// Sıcak vurgu: mercan-şeftali
val OchreWarm get() = c(0xFFF08A6C, 0xFFF5A58C)
val OchreDark get() = c(0xFFB4512F, 0xFFF8BBA6)
val OchreSoft get() = c(0xFFFFF1EC, 0xFF2E1E1A)
val OchreBorder get() = c(0xFFF8D2C5, 0xFF54342A)
val OchreText get() = c(0xFF7A3418, 0xFFF9C9B8)

// Vurgu: gök mavisi ve lavanta
val SlateBlue get() = c(0xFF3F7AA0, 0xFF86B9DB)
val SlateBlueSoft get() = c(0xFFECF3F9, 0xFF17222C)
val BlueBorder get() = c(0xFFCADDED, 0xFF293A49)
val SoftPurple get() = c(0xFF7D6AAE, 0xFFB7A8DD)
val SoftPurpleSoft get() = c(0xFFF3F0FA, 0xFF201D2E)

// Zemin ve yüzeyler
val CanvasBackground get() = c(0xFFF7F6FB, 0xFF0D0E16)
val CardSurface get() = c(0xFFFFFFFF, 0xFF151726)
val CardBorder get() = c(0xFFE6E4F0, 0xFF262942)
val DividerColor get() = c(0xFFEFEDF6, 0xFF1D2033)

// Yazı renkleri (açıkta sıcak koyu kahve-gri, koyuda sıcak kırık beyaz)
val TextPrimary get() = c(0xFF1F2238, 0xFFECEDF7)
val TextSecondary get() = c(0xFF555A75, 0xFFB7BAD0)
val TextTertiary get() = c(0xFF8589A3, 0xFF8A8EA8)

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
val OnAccent get() = c(0xFFFFFFFF, 0xFF0F1330)
// Fotoğraf ve turuncu→pembe geçişli kahraman kartları her iki temada da koyu kalır: üzerindeki yazı beyaz.
val OnHero = Color(0xFFFFFFFF)
// Kahraman kartlarındaki beyaz etiketlerin yazısı (zemin hep beyaz olduğu için sabit koyu mercan)
val HeroPillText = Color(0xFF3446B0)
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
    listOf(Color(0xFF3A4BB8), Color(0xFF5B4BB0), Color(0xFFB0566A))
else listOf(Color(0xFF4F63D8), Color(0xFF7A62D6), Color(0xFFF08A6C))

// Material renk şemaları için sabit değerler
internal object Light {
    val primary = Color(0xFF4F63D8); val primaryContainer = Color(0xFFECEEFC); val onPrimaryContainer = Color(0xFF23307A)
    val secondary = Color(0xFF2E9C84); val secondaryContainer = Color(0xFFE4F5F0); val onSecondaryContainer = Color(0xFF145646)
    val tertiary = Color(0xFFF08A6C); val tertiaryContainer = Color(0xFFFFF1EC); val onTertiaryContainer = Color(0xFF7A3418)
    val background = Color(0xFFF7F6FB); val surface = Color(0xFFFFFFFF); val surfaceVariant = Color(0xFFF0EFF7)
    val onSurface = Color(0xFF1F2238); val onSurfaceVariant = Color(0xFF555A75); val outline = Color(0xFFCFCDE0)
    val outlineVariant = Color(0xFFE6E4F0); val error = Color(0xFFD6454E)
}

internal object Dark {
    val primary = Color(0xFF8C9BF2); val primaryContainer = Color(0xFF1C2140); val onPrimaryContainer = Color(0xFFC4CCFA)
    val secondary = Color(0xFF5CCBB1); val secondaryContainer = Color(0xFF122A25); val onSecondaryContainer = Color(0xFFAEE8D9)
    val tertiary = Color(0xFFF5A58C); val tertiaryContainer = Color(0xFF2E1E1A); val onTertiaryContainer = Color(0xFFF9C9B8)
    val background = Color(0xFF0D0E16); val surface = Color(0xFF151726); val surfaceVariant = Color(0xFF1D2033)
    val onSurface = Color(0xFFECEDF7); val onSurfaceVariant = Color(0xFFB7BAD0); val outline = Color(0xFF3A3E5C)
    val outlineVariant = Color(0xFF262942); val error = Color(0xFFF0828A)
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
