package akinai.minikgurme.ui.screens

import akinai.minikgurme.shared.forStore
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import akinai.minikgurme.i18n.AppLanguage
import akinai.minikgurme.i18n.t
import akinai.minikgurme.ui.theme.*
import androidx.compose.ui.text.withLink
import akinai.minikgurme.util.LegalTexts

/** Gizlilik politikası / kullanım koşulları / açık kaynak lisansları. Metinler dil başına ayrı yazıldı. */
@Composable
fun LegalScreen(kind: String, onBack: () -> Unit) {
    val lang = AppLanguage.current
    val sections = when (kind) {
        "privacy" -> LegalTexts.privacy(lang).map { (h, b) -> h.forStore() to b.forStore() }
        "terms" -> LegalTexts.terms(lang).map { (h, b) -> h.forStore() to b.forStore() }
        "sources" -> LegalTexts.sources(lang)
        else -> LegalTexts.licenses(lang)
    }
    val title = when (kind) { "privacy" -> t("Gizlilik Politikası"); "terms" -> t("Kullanım Koşulları"); "sources" -> t("Kaynaklar"); else -> t("Açık Kaynak Lisansları") }
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp).testTag("legal_$kind"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 6.dp)) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = t("Geri"), tint = TextPrimary) }
            Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
        }
        sections.forEachIndexed { i, (h, body) ->
            if (!(i == 0 && kind != "licenses")) Text(h, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
            if (kind == "sources") LinkifiedText(body) else Text(body, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        }
        Spacer(Modifier.height(24.dp))
    }
}


private val URL = Regex("https?://[^\\s·]+")

/** Metindeki adresleri tıklanabilir bağlantıya çevirir (tarayıcıda açılır; Android ve iOS). */
@Composable
private fun LinkifiedText(text: String) {
    val linkColor = CoralPrimary
    val annotated = androidx.compose.ui.text.buildAnnotatedString {
        var last = 0
        URL.findAll(text).forEach { m ->
            append(text.substring(last, m.range.first))
            withLink(androidx.compose.ui.text.LinkAnnotation.Url(m.value, androidx.compose.ui.text.TextLinkStyles(
                style = androidx.compose.ui.text.SpanStyle(color = linkColor, textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline)
            ))) { append(m.value.removePrefix("https://").removePrefix("www.")) }
            last = m.range.last + 1
        }
        append(text.substring(last))
    }
    androidx.compose.material3.Text(annotated, style = MaterialTheme.typography.bodyMedium, color = TextSecondary, modifier = Modifier.testTag("sources_text"))
}
