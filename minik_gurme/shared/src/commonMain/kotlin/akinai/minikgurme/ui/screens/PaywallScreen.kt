package akinai.minikgurme.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import akinai.minikgurme.billing.PlusManager
import akinai.minikgurme.i18n.t
import akinai.minikgurme.ui.theme.*

/** Plus özellikleri (ücretli menü öğeleri bu listeden gelir). */
data class PlusFeature(val key: String, val icon: ImageVector, val title: String, val desc: String)

val PLUS_FEATURES = listOf(
    PlusFeature("planner", Icons.Filled.CalendarMonth, "Haftalık menü planlayıcı", "7 günlük kahvaltı, öğle ve akşam planı; tek dokunuşla alışveriş listesine aktar."),
    PlusFeature("report", Icons.Filled.PictureAsPdf, "Doktor raporu (PDF)", "Büyüme ölçümleri, alerjen denemeleri ve tepkiler tek sayfada; kontrolde doktorunuza gösterin."),
    PlusFeature("babies", Icons.Filled.ChildCare, "Birden fazla bebek", "İkizler ve kardeşler için ayrı profiller; her birinin verisi ayrı tutulur."),
    PlusFeature("reminders", Icons.Filled.NotificationsActive, "Öğün hatırlatıcıları", "Kahvaltı, öğle ve akşam için günlük hatırlatma."),
    PlusFeature("community", Icons.Filled.Forum, "Toplulukta soru sor", "Ebeveyn Topluluğu'nda gönderi ve yorum yazın; okumak herkese açık."),
)

/** Minik Gurme Plus abonelik sayfası. Fiyatlar Google Play'den gelir. */
@Composable
fun PaywallScreen(onBack: () -> Unit, onOpenTerms: () -> Unit, onOpenPrivacy: () -> Unit) {
    val plans = PlusManager.plans
    var selected by remember(plans) { mutableStateOf(plans.firstOrNull()?.productId) }
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp).testTag("paywall"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 6.dp)) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = t("Geri"), tint = TextPrimary) }
        }
        Box(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Brush.linearGradient(HeroGradient)).padding(22.dp)
        ) {
            Column {
                Text(t("Minik Gurme Plus"), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = OnHero)
                Spacer(Modifier.height(4.dp))
                Text(
                    t(if (PlusManager.isPlus) "Plus etkin. Desteğiniz için teşekkürler!" else "Planlayın, raporlayın, birden fazla bebeği takip edin."),
                    style = MaterialTheme.typography.bodyMedium, color = OnHero.copy(alpha = 0.92f)
                )
            }
        }
        PLUS_FEATURES.forEach { f ->
            Row(verticalAlignment = Alignment.Top) {
                Box(Modifier.size(40.dp).clip(CircleShape).background(CoralSoft), contentAlignment = Alignment.Center) {
                    Icon(f.icon, contentDescription = null, tint = CoralPrimary, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(t(f.title), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(t(f.desc), style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }
        }
        Text(t("Ücretsiz sürümde kalanlar: tüm takip ekranları, 100 gıda, tarifler, rehber, alerjen planı, alerjen hatırlatıcısı ve yedekleme."),
            style = MaterialTheme.typography.bodySmall, color = TextTertiary)

        if (!PlusManager.isPlus) {
            if (plans.isEmpty()) {
                Surface(shape = RoundedCornerShape(16.dp), color = OchreSoft, border = BorderStroke(1.dp, OchreBorder)) {
                    Text(
                        t("Abonelik seçenekleri yüklenemedi. Uygulama Google Play'den yüklendiğinde ve internet bağlantısı olduğunda fiyatlar burada görünür."),
                        style = MaterialTheme.typography.bodySmall, color = OchreText, modifier = Modifier.padding(14.dp)
                    )
                }
            }
            plans.forEach { p ->
                val sel = selected == p.productId
                val yearly = p.productId == PlusManager.PRODUCT_YEARLY
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = if (sel) CoralSoft else CardSurface,
                    border = BorderStroke(if (sel) 2.dp else 1.dp, if (sel) CoralPrimary else CardBorder),
                    modifier = Modifier.fillMaxWidth().clickable { selected = p.productId }.testTag("plan_${p.productId}")
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(t(if (yearly) "Yıllık" else "Aylık"), fontWeight = FontWeight.Bold, color = TextPrimary)
                            if (yearly) Text(t("En avantajlı"), style = MaterialTheme.typography.labelSmall, color = SageDark, fontWeight = FontWeight.Bold)
                        }
                        Text(p.price, fontWeight = FontWeight.ExtraBold, color = CoralDark)
                        if (sel) { Spacer(Modifier.width(8.dp)); Icon(Icons.Filled.Check, contentDescription = null, tint = CoralPrimary) }
                    }
                }
            }
            Button(
                onClick = {
                    val plan = plans.firstOrNull { it.productId == selected }
                    if (plan != null) PlusManager.purchase(plan)
                    else PlusManager.message = "Satın alma şu an kullanılamıyor. Uygulamanın Google Play'den yüklendiğinden emin olun."
                },
                modifier = Modifier.fillMaxWidth().height(54.dp).testTag("paywall_subscribe"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary)
            ) { Text(t("Plus'a abone ol"), fontWeight = FontWeight.Bold, color = OnAccent) }
        }
        TextButton(onClick = { PlusManager.restore() }, modifier = Modifier.fillMaxWidth()) { Text(t("Satın alımları geri yükle"), color = CoralDark) }
        PlusManager.message?.let { Text(t(it), style = MaterialTheme.typography.bodySmall, color = TextSecondary, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) }
        Text(
            t("Abonelik Google Play hesabınızdan ücretlendirilir ve dönem bitiminden en az 24 saat önce iptal edilmezse otomatik yenilenir. Google Play › Abonelikler'den istediğiniz zaman iptal edebilirsiniz."),
            style = MaterialTheme.typography.bodySmall, color = TextTertiary
        )
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(t("Kullanım Koşulları"), color = CoralDark, style = MaterialTheme.typography.labelMedium, modifier = Modifier.clickable { onOpenTerms() })
            Text(t("Gizlilik Politikası"), color = CoralDark, style = MaterialTheme.typography.labelMedium, modifier = Modifier.clickable { onOpenPrivacy() })
        }
        Spacer(Modifier.height(24.dp))
    }
}
