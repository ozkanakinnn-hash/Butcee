package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.billing.PremiumManager
import com.example.i18n.AppLanguage
import com.example.util.AppLinks
import kotlin.math.cos
import kotlin.math.sin

private fun pt(t: String, e: String) = if (AppLanguage.isEnglish) e else t

private fun Context.findActivity(): Activity? {
  var c: Context? = this
  while (c is ContextWrapper) { if (c is Activity) return c; c = c.baseContext }
  return null
}

private fun periodLabel(period: String): String = when (period) {
  "P1M" -> pt("Aylık", "Monthly")
  "P1Y" -> pt("Yıllık", "Yearly")
  "P1W" -> pt("Haftalık", "Weekly")
  "" -> pt("Ömür boyu", "Lifetime")
  else -> period
}

private fun trialLabel(p: String?): String? = when {
  p == null -> null
  p.endsWith("D") -> p.removePrefix("P").removeSuffix("D").let { pt("$it gün ücretsiz dene", "$it-day free trial") }
  p.endsWith("W") -> p.removePrefix("P").removeSuffix("W").let { pt("$it hafta ücretsiz dene", "$it-week free trial") }
  else -> pt("Ücretsiz deneme", "Free trial")
}

/** Reklamsız Premium üyelik ekranı (Google Play Faturalandırma). */
@Composable
fun PremiumScreen(onNavigateBack: () -> Unit) {
  BackHandler { onNavigateBack() }
  val context = LocalContext.current
  val cs = MaterialTheme.colorScheme
  val isPremium by PremiumManager.isPremium.collectAsState()
  val plans by PremiumManager.plans.collectAsState()
  val status by PremiumManager.status.collectAsState()
  val message by PremiumManager.message.collectAsState()
  var selected by remember { mutableStateOf<String?>(null) }

  LaunchedEffect(Unit) { PremiumManager.refresh() }
  LaunchedEffect(message) {
    val m = message ?: return@LaunchedEffect
    val text = when (m) {
      "thanks" -> pt("Teşekkürler! Premium etkin, reklamlar kaldırıldı 🤲", "Thank you! Premium is active, ads removed 🤲")
      "pending" -> pt("Ödeme onay bekliyor; tamamlanınca Premium açılacak.", "Payment pending; Premium will activate when complete.")
      "restored" -> pt("Satın alımınız geri yüklendi.", "Your purchase was restored.")
      "store_unavailable" -> pt("Google Play'e bağlanılamadı. Biraz sonra tekrar deneyin.", "Couldn't reach Google Play. Try again later.")
      else -> pt("Satın alma tamamlanamadı.", "Purchase could not be completed.")
    }
    Toast.makeText(context, text, Toast.LENGTH_LONG).show()
    PremiumManager.consumeMessage()
  }
  val chosen = plans.firstOrNull { "${it.productId}:${it.period}" == selected }
    ?: plans.firstOrNull { it.period == "P1Y" } ?: plans.firstOrNull()

  Column(modifier = Modifier.fillMaxSize().background(cs.background).verticalScroll(rememberScrollState()).testTag("premium_screen")) {
    Row(modifier = Modifier.padding(start = 4.dp, top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
      IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = pt("Geri", "Back"), tint = cs.onBackground) }
    }

    // Başlık madalyonu
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
      val prim = cs.primary; val pc = cs.primaryContainer
      Canvas(modifier = Modifier.size(110.dp)) {
        val path = Path().apply {
          for (k in 0 until 16) {
            val a = Math.PI / 8 * k - Math.PI / 2
            val r = size.minDimension / 2f * (if (k % 2 == 0) 0.97f else 0.8f)
            val x = center.x + r * cos(a).toFloat(); val y = center.y + r * sin(a).toFloat()
            if (k == 0) moveTo(x, y) else lineTo(x, y)
          }
          close()
        }
        drawPath(path, pc); drawPath(path, prim, style = Stroke(2.dp.toPx()))
        drawCircle(prim, radius = size.minDimension * 0.27f, center = center, style = Stroke(1.dp.toPx()))
      }
      Text("⭐", fontSize = 38.sp)
    }
    Text(
      pt("Ramazan Rehberi Premium", "Ramazan Rehberi Premium"),
      fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 26.sp, color = cs.onBackground,
      textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
    )
    Text(
      if (isPremium) pt("Premium üyeliğiniz etkin. Desteğiniz için teşekkürler! 🤲", "Your Premium is active. Thank you for your support! 🤲")
      else pt("Reklamsız, sade ve huzurlu bir ibadet arkadaşı", "Ad-free, calm and focused companion for worship"),
      fontSize = 14.sp, color = if (isPremium) cs.primary else cs.onSurfaceVariant, fontWeight = if (isPremium) FontWeight.Bold else FontWeight.Normal,
      textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 6.dp)
    )

    // Avantajlar
    Column(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)
        .clip(RoundedCornerShape(20.dp)).background(cs.surface).border(1.dp, cs.outline, RoundedCornerShape(20.dp)).padding(16.dp)
    ) {
      listOf(
        pt("🚫  Tüm reklamlar kaldırılır", "🚫  All ads removed"),
        pt("🕌  Ezan, Kur'an ve dua ekranları tamamen reklamsız", "🕌  Adhan, Quran and dua screens fully ad-free"),
        pt("⚡  Daha hızlı ve pil dostu kullanım", "⚡  Faster, battery-friendly experience"),
        pt("🤲  Uygulamanın gelişmesine destek olursunuz", "🤲  You support the app's development")
      ).forEach { Text(it, fontSize = 14.sp, color = cs.onSurface, modifier = Modifier.padding(vertical = 5.dp)) }
    }

    if (!isPremium) {
      when {
        plans.isEmpty() && status != PremiumManager.Status.UNAVAILABLE -> Box(Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
          CircularProgressIndicator()
        }
        plans.isEmpty() -> Text(
          pt("Mağaza bilgisi alınamadı. Uygulama Google Play'den yüklendiyse internet bağlantınızı kontrol edin.",
            "Store info unavailable. If installed from Google Play, check your internet connection."),
          fontSize = 13.sp, color = cs.onSurfaceVariant, textAlign = TextAlign.Center,
          modifier = Modifier.fillMaxWidth().padding(20.dp)
        )
        else -> plans.forEach { plan ->
          val key = "${plan.productId}:${plan.period}"
          val sel = chosen?.let { "${it.productId}:${it.period}" } == key
          Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp)
              .clip(RoundedCornerShape(16.dp))
              .background(if (sel) cs.primaryContainer else cs.surface)
              .border(if (sel) 2.dp else 1.dp, if (sel) cs.primary else cs.outline, RoundedCornerShape(16.dp))
              .clickable { selected = key }
              .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(periodLabel(plan.period), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = cs.onSurface)
                if (plan.period == "P1Y") {
                  Spacer(Modifier.width(8.dp))
                  Text(pt("En avantajlı", "Best value"), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = cs.onPrimary,
                    modifier = Modifier.clip(RoundedCornerShape(50)).background(cs.primary).padding(horizontal = 8.dp, vertical = 2.dp))
                }
              }
              trialLabel(plan.freeTrial)?.let { Text(it, fontSize = 12.sp, color = cs.primary, fontWeight = FontWeight.SemiBold) }
              if (plan.period == "") Text(pt("Tek seferlik ödeme", "One-time payment"), fontSize = 12.sp, color = cs.onSurfaceVariant)
            }
            Text(plan.price, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = cs.primary)
          }
        }
      }

      Text(
        if (chosen?.freeTrial != null) pt("Ücretsiz denemeyi başlat", "Start free trial") else pt("Premium'a geç", "Go Premium"),
        fontSize = 16.sp, fontWeight = FontWeight.Bold, color = cs.onPrimary, textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)
          .clip(RoundedCornerShape(16.dp)).background(if (chosen != null) cs.primary else cs.surfaceVariant)
          .clickable(enabled = chosen != null) {
            val act = context.findActivity()
            if (act != null && chosen != null) PremiumManager.launch(act, chosen)
          }
          .padding(vertical = 15.dp)
          .testTag("premium_buy")
      )
    }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
      Text(
        pt("Satın alımları geri yükle", "Restore purchases"), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = cs.primary,
        modifier = Modifier.clip(RoundedCornerShape(10.dp)).clickable {
          PremiumManager.restore { ok ->
            (context as? Activity ?: context.findActivity())?.runOnUiThread {
              Toast.makeText(context, if (ok) pt("Premium geri yüklendi.", "Premium restored.") else pt("Etkin bir satın alma bulunamadı.", "No active purchase found."), Toast.LENGTH_SHORT).show()
            }
          }
        }.padding(10.dp)
      )
      if (isPremium) Text(
        pt("Aboneliği yönet", "Manage subscription"), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = cs.primary,
        modifier = Modifier.clip(RoundedCornerShape(10.dp)).clickable {
          AppLinks.open(context, AppLinks.subscriptionsUrl(context.packageName, PremiumManager.SUB_ID))
        }.padding(10.dp)
      )
    }

    // Google Play abonelik politikası gereği açık bilgilendirme
    Text(
      pt(
        "Abonelikler, dönem bitmeden en az 24 saat önce iptal edilmedikçe aynı ücretle otomatik yenilenir. Ödeme Google Play hesabınızdan alınır. Ücretsiz deneme bittiğinde abonelik ücretli devam eder. Aboneliğinizi Google Play Store → Profil → Ödemeler ve abonelikler bölümünden istediğiniz zaman yönetebilir veya iptal edebilirsiniz. Ömür boyu seçeneği tek seferlik ödemedir.",
        "Subscriptions renew automatically at the same price unless cancelled at least 24 hours before the period ends. Payment is charged to your Google Play account. After a free trial the subscription continues as paid. Manage or cancel anytime in Google Play Store → Profile → Payments & subscriptions. Lifetime is a one-time payment."
      ),
      fontSize = 11.sp, lineHeight = 15.sp, color = cs.onSurfaceVariant, textAlign = TextAlign.Center,
      modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp)
    )
    Text(
      pt("Gizlilik Politikası", "Privacy Policy"), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = cs.primary, textAlign = TextAlign.Center,
      modifier = Modifier.fillMaxWidth().clickable { AppLinks.open(context, AppLinks.PRIVACY_URL) }.padding(12.dp)
    )
    Spacer(Modifier.height(16.dp))
  }
}

@Suppress("unused")
private val unusedOffset = Offset.Zero
