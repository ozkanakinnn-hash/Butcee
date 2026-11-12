package akinai.minikgurme.i18n

import android.content.Context
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
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

/**
 * Uygulama dili (Türkçe varsayılan + İngilizce).
 *
 * Kaynak metinler Türkçe yazılmıştır; İngilizce, `assets/i18n/en.json` sözlüğünden (birebir eşleşme)
 * ve `assets/i18n/en_patterns.json` kalıplarından (değişken içeren metinler) gelir.
 * Ekranlar `androidx.compose.material3.Text` yerine bu paketteki [Text]'i import eder; böylece
 * gösterilen her metin, veri içeriği dahil, otomatik çevrilir. Bulunamayan metin Türkçe kalır.
 */
object AppLanguage {
    private const val PREFS = "app_prefs"
    private const val KEY = "language"

    /** "tr" veya "en". Compose durumu: değişince tüm metinler yeniden çizilir. */
    var current by mutableStateOf("tr")
        private set

    fun init(context: Context) {
        val saved = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, null)
        current = saved ?: if (Locale.getDefault().language == "tr") "tr" else "en"
        Translator.load(context)
    }

    fun set(context: Context, language: String) {
        current = language
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY, language).apply()
    }
}

object Translator {
    private var exact: Map<String, String> = emptyMap()
    private var patterns: List<Pair<Regex, String>> = emptyList()

    fun load(context: Context) {
        if (exact.isNotEmpty()) return
        try {
            val json = JSONObject(context.assets.open("i18n/en.json").bufferedReader().use { it.readText() })
            exact = buildMap { json.keys().forEach { k -> put(k, json.getString(k)) } }
            val arr = JSONArray(context.assets.open("i18n/en_patterns.json").bufferedReader().use { it.readText() })
            patterns = (0 until arr.length()).map { i ->
                val p = arr.getJSONArray(i)
                Regex(p.getString(0)) to p.getString(1)
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
) {
    androidx.compose.material3.Text(
        text = t(text),
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
    val lower = n.lowercase(Locale("tr"))
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
