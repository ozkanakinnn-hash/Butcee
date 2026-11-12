package akinai.minikgurme.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import akinai.minikgurme.data.guide.PediatricRecipesData
import akinai.minikgurme.data.model.BabyRecipe
import akinai.minikgurme.i18n.Text
import akinai.minikgurme.i18n.t
import akinai.minikgurme.ui.components.FoodImage
import akinai.minikgurme.ui.theme.*
import akinai.minikgurme.util.Babies
import akinai.minikgurme.shared.KeyValueStore
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/** Haftalık plan: gün (0-6) × öğün → tarif kimliği. Etkin bebeğe göre ayrı saklanır. */
object WeeklyPlan {
    val SLOTS = listOf("Kahvaltı", "Öğle", "Akşam")
    val DAYS = listOf("Pazartesi", "Salı", "Çarşamba", "Perşembe", "Cuma", "Cumartesi", "Pazar")
    private val prefs by lazy { KeyValueStore("app_prefs") }
    private fun key() = "weekly_plan_" + Babies.dbName(Babies.activeId())
    fun load(): Map<String, String> {
        val raw = prefs.getString(key(), null) ?: return emptyMap()
        return runCatching { Json.parseToJsonElement(raw).jsonObject.mapValues { it.value.jsonPrimitive.content } }.getOrDefault(emptyMap())
    }
    fun save(plan: Map<String, String>) {
        prefs.putString(key(), JsonObject(plan.mapValues { JsonPrimitive(it.value) }).toString())
    }
    fun slotKey(day: Int, slot: Int) = "${day}_$slot"
    private fun matches(r: BabyRecipe, slot: Int) = when (slot) {
        0 -> r.mealType.contains("Kahvaltı") || r.mealType.contains("Ara")
        1 -> r.mealType.contains("Öğle")
        else -> r.mealType.contains("Akşam") || r.mealType.contains("Öğle")
    }
    fun suitable(ageMonths: Int): List<BabyRecipe> = PediatricRecipesData.allRecipes.filter { it.ageMonths <= ageMonths.coerceAtLeast(6) }.ifEmpty { PediatricRecipesData.allRecipes }
    /** Boş öğünleri yaşa uygun, birbirini tekrar etmeyen tariflerle doldurur. */
    fun autoFill(current: Map<String, String>, ageMonths: Int): Map<String, String> {
        val out = current.toMutableMap(); val pool = suitable(ageMonths)
        var i = 0
        for (d in 0 until 7) for (s in SLOTS.indices) {
            val k = slotKey(d, s); if (out[k] != null) continue
            val cands = pool.filter { matches(it, s) }.let { if (it.size >= 4) it else pool }
            out[k] = cands[(d * 3 + s + i++) % cands.size].id
        }
        return out
    }
}

@Composable
fun WeeklyPlannerScreen(ageMonths: Int, onBack: () -> Unit, onOpenRecipe: (BabyRecipe) -> Unit, onAddToShopping: (List<BabyRecipe>) -> Unit) {
    var plan by remember { mutableStateOf(WeeklyPlan.load()) }
    var picking by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    val byId = remember { PediatricRecipesData.allRecipes.associateBy { it.id } }
    fun update(p: Map<String, String>) { plan = p; WeeklyPlan.save(p) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp).testTag("planner"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 6.dp)) {
                IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = t("Geri"), tint = TextPrimary) }
                Column(Modifier.weight(1f)) {
                    Text("Haftalık Menü", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                    Text("Yaşa uygun tariflerle 7 günlük plan", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { update(WeeklyPlan.autoFill(plan, ageMonths)) },
                    modifier = Modifier.weight(1f).testTag("planner_autofill"),
                    colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary), shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = OnAccent); Spacer(Modifier.width(6.dp))
                    androidx.compose.material3.Text(t("Boşları doldur"), color = OnAccent, fontWeight = FontWeight.Bold, maxLines = 1)
                }
                OutlinedButton(
                    onClick = { onAddToShopping(plan.values.mapNotNull { byId[it] }) },
                    enabled = plan.isNotEmpty(),
                    modifier = Modifier.weight(1f).testTag("planner_to_shopping"),
                    border = BorderStroke(1.dp, CoralBorder), shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Filled.AddShoppingCart, contentDescription = null, tint = CoralDark); Spacer(Modifier.width(6.dp))
                    androidx.compose.material3.Text(t("Alışverişe ekle"), color = CoralDark, fontWeight = FontWeight.Bold, maxLines = 1)
                }
            }
        }
        items(7) { d ->
            Card(
                colors = CardDefaults.cardColors(containerColor = CardSurface), border = BorderStroke(1.dp, CardBorder),
                shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(WeeklyPlan.DAYS[d], style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = CoralDark)
                    WeeklyPlan.SLOTS.forEachIndexed { s, slotName ->
                        val r = plan[WeeklyPlan.slotKey(d, s)]?.let { byId[it] }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().clickable { if (r != null) onOpenRecipe(r) else picking = d to s }.padding(vertical = 2.dp)
                        ) {
                            Text(slotName, style = MaterialTheme.typography.labelMedium, color = TextTertiary, modifier = Modifier.width(72.dp))
                            if (r != null) {
                                FoodImage(r.emoji, 26.dp); Spacer(Modifier.width(8.dp))
                                Text(r.title, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                IconButton(onClick = { update(plan - WeeklyPlan.slotKey(d, s)) }) { Icon(Icons.Filled.Close, contentDescription = t("Kaldır"), tint = TextTertiary) }
                            } else {
                                Text("+ Tarif seç", style = MaterialTheme.typography.bodyMedium, color = CoralPrimary, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f).padding(vertical = 12.dp))
                            }
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.padding(12.dp)) }
    }

    picking?.let { (d, s) ->
        AlertDialog(
            onDismissRequest = { picking = null },
            title = { Text("${WeeklyPlan.DAYS[d]} · ${WeeklyPlan.SLOTS[s]}") },
            text = {
                LazyColumn(Modifier.heightIn(max = 420.dp)) {
                    items(WeeklyPlan.suitable(ageMonths)) { r ->
                        Surface(onClick = { update(plan + (WeeklyPlan.slotKey(d, s) to r.id)); picking = null }, color = CardSurface) {
                            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                FoodImage(r.emoji, 28.dp); Spacer(Modifier.width(10.dp))
                                Column { Text(r.title, color = TextPrimary, maxLines = 2); Text(r.ageLabel, style = MaterialTheme.typography.labelSmall, color = TextTertiary) }
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { picking = null }) { Text("Vazgeç") } }
        )
    }
}
