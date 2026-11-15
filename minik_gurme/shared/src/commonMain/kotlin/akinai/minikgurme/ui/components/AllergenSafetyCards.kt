package akinai.minikgurme.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import akinai.minikgurme.i18n.Text
import akinai.minikgurme.i18n.t
import akinai.minikgurme.ui.theme.*
import akinai.minikgurme.util.AllergenMaintenance

/** Yüksek alerji riski uyarısı (kırmızı): alerjen tanıtımı doktorla planlanmalı. */
@Composable
fun HighRiskAllergyNotice(modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ReactionAllergySoft),
        border = BorderStroke(1.5.dp, ReactionAllergy),
        modifier = modifier.fillMaxWidth().testTag("high_risk_notice")
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
            Icon(Icons.Filled.WarningAmber, contentDescription = null, tint = ReactionAllergy, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Önce doktorunuza danışın", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.ExtraBold, color = ReactionAllergy)
                Text(
                    "Bebeğinizde şiddetli egzama veya bilinen bir gıda alerjisi olduğunu belirttiniz. Bu bebeklerde alerjen tanıtımı çocuk doktoru veya alerji uzmanıyla birlikte planlanmalıdır. Bu bilgiyi Ayarlar › Bebek bölümünden değiştirebilirsiniz.",
                    style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = AllergyText
                )
            }
        }
    }
}

/** Güvenli bulunan alerjenlerin düzenli verilmeye devam edilmesi (sürdürme) kartı. */
@Composable
fun AllergenMaintenanceCard(items: List<AllergenMaintenance.Item>, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardSurface), border = BorderStroke(1.dp, CardBorder),
        shape = RoundedCornerShape(20.dp), modifier = modifier.fillMaxWidth().testTag("allergen_maintenance")
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Alerjen Sürdürme", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
            Text(
                "Güvenli bulunan alerjenleri, toleransın sürmesi için haftada birkaç kez vermeye devam edin. Öğün eklediğinizde tarih otomatik güncellenir.",
                style = MaterialTheme.typography.bodySmall, color = TextSecondary
            )
            items.forEach { item ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    FoodImage(item.emoji, 28.dp); Spacer(Modifier.width(10.dp))
                    Text(item.name, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, modifier = Modifier.weight(1f))
                    val label = when {
                        item.overdue -> "${item.daysSince} " + t("gündür verilmedi")
                        item.daysSince == 0 -> t("Bugün verildi")
                        else -> "${item.daysSince} " + t("gün önce verildi")
                    }
                    Surface(shape = RoundedCornerShape(10.dp), color = if (item.overdue) OchreSoft else SageSoft) {
                        Text(
                            label, translate = false, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold,
                            color = if (item.overdue) OchreDark else SageText,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
