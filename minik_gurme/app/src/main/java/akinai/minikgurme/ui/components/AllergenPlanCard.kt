package akinai.minikgurme.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import akinai.minikgurme.data.model.AllergenTest
import akinai.minikgurme.i18n.Text
import akinai.minikgurme.ui.theme.*
import java.util.Locale

/** Sık görülen 9 alerjen; önerilen tanıtım sırasıyla. Eşleşme deneme adındaki anahtar kelimelerle yapılır. */
data class MajorAllergen(val name: String, val emoji: String, val keywords: List<String>, val tip: String)

val MAJOR_ALLERGENS = listOf(
    MajorAllergen("Yumurta", "🥚", listOf("yumurta"), "İyi pişmiş yumurta sarısıyla başlayın."),
    MajorAllergen("Süt (yoğurt)", "🥛", listOf("yoğurt", "yogurt", "süt", "lor", "labne", "peynir"), "Tam yağlı ev yoğurduyla başlayın; inek sütü içecek olarak 12. aydan sonra."),
    MajorAllergen("Buğday", "🌾", listOf("buğday", "irmik", "gluten", "makarna", "ekmek", "bulgur"), "Bebek irmiği veya tam buğday lapası."),
    MajorAllergen("Yer fıstığı", "🥜", listOf("fıstık"), "Şekersiz ezmeyi suyla inceltip az miktarda verin; asla bütün fıstık vermeyin."),
    MajorAllergen("Susam", "🌰", listOf("susam", "tahin"), "Yoğurda birkaç damla tahin ekleyin."),
    MajorAllergen("Balık", "🐟", listOf("balık", "somon", "levrek", "mezgit"), "Kılçıksız, iyi pişmiş beyaz balık veya somon."),
    MajorAllergen("Ağaç kuruyemişleri", "🌰", listOf("ceviz", "fındık", "badem", "kaju"), "İnce öğütülmüş toz hâlinde lapaya karıştırın."),
    MajorAllergen("Soya", "🫘", listOf("soya"), "Az miktarda tofu veya soya yoğurdu."),
    MajorAllergen("Kabuklu deniz ürünleri", "🦐", listOf("karides", "midye", "kabuklu"), "Daha geç ve doktorunuza danışarak; iyi pişmiş, ince kıyılmış."),
)

private fun statusOf(a: MajorAllergen, tests: List<AllergenTest>): AllergenTest? {
    val matches = tests.filter { t -> val f = t.food.lowercase(Locale("tr")); a.keywords.any { f.contains(it) } }
    return matches.firstOrNull { it.status == "Reaksiyon" } ?: matches.firstOrNull { it.status == "Devam Ediyor" } ?: matches.firstOrNull { it.status == "Güvenli" } ?: matches.firstOrNull()
}

/** Alerjen tanıtım planı: 9 alerjenin durumu ve sıradakini 3 gün kuralıyla başlatma. */
@Composable
fun AllergenPlanCard(tests: List<AllergenTest>, onStart: (String) -> Unit) {
    val rows = MAJOR_ALLERGENS.map { it to statusOf(it, tests) }
    val done = rows.count { it.second?.status == "Güvenli" }
    val ongoing = tests.any { it.status == "Devam Ediyor" }
    val next = rows.firstOrNull { it.second == null }?.first
    Card(
        colors = CardDefaults.cardColors(containerColor = CardSurface), border = BorderStroke(1.dp, CardBorder),
        shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth().testTag("allergen_plan")
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Alerjen Tanıtım Planı", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                    Text("Sık görülen 9 alerjeni sırayla, her birini 3 gün deneyin", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
                Text("$done/9", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = SageDark)
            }
            LinearProgressIndicator(progress = { done / 9f }, modifier = Modifier.fillMaxWidth().height(6.dp), color = SageGreen, trackColor = DividerColor)
            rows.forEach { (a, test) ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    FoodImage(a.emoji, 28.dp); Spacer(Modifier.width(10.dp))
                    Text(a.name, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, modifier = Modifier.weight(1f))
                    val (label, bg, fg) = when (test?.status) {
                        "Güvenli" -> Triple("Güvenli", SageSoft, SageText)
                        "Devam Ediyor" -> Triple("${test.day}/3 gün", CoralSoft, CoralText)
                        "Reaksiyon" -> Triple("Reaksiyon", ReactionAllergySoft, AllergyText)
                        else -> Triple("Başlanmadı", DividerColor, TextSecondary)
                    }
                    Surface(shape = RoundedCornerShape(10.dp), color = bg) {
                        Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = fg, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }
            }
            if (next != null) {
                Surface(shape = RoundedCornerShape(14.dp), color = CoralSoft, border = BorderStroke(1.dp, CoralBorder)) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Sıradaki: ${next.name}", fontWeight = FontWeight.Bold, color = CoralText)
                        Text(next.tip, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        if (ongoing) Text("Önce devam eden denemeyi tamamlayın (aynı anda tek yeni alerjen).", style = MaterialTheme.typography.labelSmall, color = OchreText)
                        else TextButton(onClick = { onStart(next.name) }, modifier = Modifier.testTag("allergen_plan_start")) { Text("3 günlük denemeyi başlat", color = CoralDark, fontWeight = FontWeight.Bold) }
                    }
                }
            }
            Text("Alerji öyküsü veya şiddetli egzaması olan bebeklerde önce doktorunuza danışın.", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
        }
    }
}
