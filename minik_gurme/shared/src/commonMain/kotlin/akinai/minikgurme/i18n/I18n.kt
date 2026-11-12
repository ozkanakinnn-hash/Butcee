package akinai.minikgurme.i18n

import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import akinai.minikgurme.resources.Res
import akinai.minikgurme.shared.KeyValueStore
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.compose.resources.ExperimentalResourceApi

/**
 * Uygulama dili (Türkçe varsayılan + İngilizce).
 *
 * Kaynak metinler Türkçe yazılmıştır; İngilizce, `composeResources/files/i18n/en.json` sözlüğünden (birebir eşleşme)
 * ve `composeResources/files/i18n/en_patterns.json` kalıplarından (değişken içeren metinler) gelir.
 * Ekranlar `androidx.compose.material3.Text` yerine bu paketteki [Text]'i import eder; böylece
 * gösterilen her metin, veri içeriği dahil, otomatik çevrilir. Bulunamayan metin Türkçe kalır.
 */
object AppLanguage {
    private const val PREFS = "app_prefs"
    private const val KEY = "language"

    /** "tr" veya "en". Compose durumu: değişince tüm metinler yeniden çizilir. */
    var current by mutableStateOf("tr")
        private set

    private val store by lazy { KeyValueStore(PREFS) }

    /** Kayıtlı dili (yoksa telefonun dilini) yükler. Sözlük için ayrıca [Translator.load] çağrılır. */
    fun init() {
        val saved = store.getString(KEY, null)
        current = saved ?: if (systemLanguage() == "tr") "tr" else "en"
    }

    fun set(language: String) {
        current = language
        store.putString(KEY, language)
    }
}

object Translator {
    private var exact: Map<String, String> = emptyMap()
    private var patterns: List<Pair<Regex, String>> = emptyList()

    val isLoaded get() = exact.isNotEmpty()

    /** Sözlüğü uygulama kaynaklarından bir kez yükler (Android ve iOS ortak). */
    @OptIn(ExperimentalResourceApi::class)
    suspend fun load(read: suspend (String) -> ByteArray = { Res.readBytes(it) }) {
        if (exact.isNotEmpty()) return
        try {
            val json = Json.parseToJsonElement(read("files/i18n/en.json").decodeToString()).jsonObject
            exact = json.mapValues { it.value.jsonPrimitive.content }
            val arr = Json.parseToJsonElement(read("files/i18n/en_patterns.json").decodeToString()).jsonArray
            patterns = arr.map { e ->
                val p = e.jsonArray
                Regex(p[0].jsonPrimitive.content) to p[1].jsonPrimitive.content
            }
        } catch (e: Exception) {
            // Sözlük yüklenemezse uygulama Türkçe çalışmaya devam eder.
        }
    }

    /** Testler için sözlüğü doğrudan verir. */
    fun setForTest(exactMap: Map<String, String>, patternList: List<Pair<Regex, String>>) {
        exact = exactMap
        patterns = patternList
    }

    fun toEnglish(tr: String): String {
        exact[tr]?.let { return it }
        val trimmed = tr.trim()
        if (trimmed != tr) exact[trimmed]?.let { return tr.replace(trimmed, it) }
        for ((regex, replacement) in patterns) {
            val m = regex.matchEntire(tr) ?: continue
            // Yakalanan parçalar da (ör. yiyecek adı) sözlükte varsa çevrilir.
            var out = replacement
            for (i in m.groupValues.size - 1 downTo 1) {
                val g = m.groupValues[i]
                out = out.replace("$$i", exact[g] ?: g)
            }
            return out
        }
        return tr
    }
}

/** Türkçe kaynak metni geçerli dile çevirir. */
fun t(tr: String): String = if (AppLanguage.current == "en") Translator.toEnglish(tr) else tr

/**
 * `androidx.compose.material3.Text`'in çeviren karşılığı (aynı parametreler).
 * Ekranlar `import akinai.minikgurme.i18n.Text` kullanır.
 */
@Composable
fun Text(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
    style: TextStyle = LocalTextStyle.current,
    /** false: kullanıcının yazdığı metin (ör. topluluk gönderisi) olduğu gibi gösterilir. */
    translate: Boolean = true,
) {
    androidx.compose.material3.Text(
        text = if (translate) t(text) else text,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines,
        onTextLayout = onTextLayout,
        style = style,
    )
}

/**
 * Türkçe tamlayan eki: "Alp" → "Alp'in", "Defne" → "Defne'nin", "Umut" → "Umut'un", "Ayşe" → "Ayşe'nin".
 * Son ünlüye göre ünlü uyumu uygulanır; ünlüyle bitiyorsa kaynaştırma "n" eklenir.
 */
fun trGenitive(name: String): String {
    val n = name.trim()
    if (n.isEmpty()) return n
    val lower = n.replace('I', 'ı').replace('İ', 'i').lowercase()
    val lastVowel = lower.lastOrNull { it in "aeıioöuü" } ?: 'e'
    val suffix = when (lastVowel) {
        'a', 'ı' -> "ın"
        'e', 'i' -> "in"
        'o', 'u' -> "un"
        else -> "ün"
    }
    val buffer = if (lower.last() in "aeıioöuü") "n" else ""
    return "$n'$buffer$suffix"
}

/** Telefonun dil kodu ("tr", "en", ...). */
expect fun systemLanguage(): String
