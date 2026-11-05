package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Bi
import com.example.data.PrayerTimeUtils
import com.example.data.PrayerTimes
import com.example.i18n.AppLanguage
import com.example.ui.theme.isAppInDarkTheme
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.sin

private val LanternMetal = Color(0xFFC9A04F)
private val LanternGlass = Color(0xFFE7B85C)
private val LanternLight = Color(0xFFFFF3C9)

/** Ramazan'da ana sayfanın en üstünde, ip üzerinde ışıldayan fanus dizisi (koyu temada yıldızlarla). */
@Composable
fun RamadanLanternGarland(modifier: Modifier = Modifier) {
  val dark = isAppInDarkTheme()
  val starColor = if (dark) Color(0xFFFFF3D0) else MaterialTheme.colorScheme.primary
  Canvas(modifier = modifier.fillMaxWidth().height(64.dp).testTag("ramadan_garland")) {
    val w = size.width
    val rnd = java.util.Random(11)
    repeat(30) {
      drawCircle(
        starColor.copy(alpha = if (dark) 0.3f + rnd.nextFloat() * 0.5f else 0.1f + rnd.nextFloat() * 0.18f),
        radius = 0.7f + rnd.nextFloat() * 1.6f,
        center = Offset(rnd.nextFloat() * w, rnd.nextFloat() * size.height)
      )
    }
    // Sarkık ip
    val sag = size.height * 0.28f
    fun ropeY(x: Float) = 4f + sag * 4f * (x / w) * (1f - x / w)
    val rope = Path().apply { moveTo(0f, ropeY(0f)); var x = 0f; while (x <= w) { lineTo(x, ropeY(x)); x += 6f } }
    drawPath(rope, LanternMetal.copy(alpha = 0.8f), style = Stroke(1.2f))
    // Fanuslar: ortadaki en büyük
    listOf(0.12f to 0.62f, 0.31f to 0.78f, 0.5f to 0.95f, 0.69f to 0.78f, 0.88f to 0.62f).forEach { (fx, s) ->
      val x = w * fx
      drawLantern(Offset(x, ropeY(x)), size.height * 0.16f * s, s, dark)
    }
  }
}

private fun DrawScope.drawLantern(top: Offset, chain: Float, s: Float, dark: Boolean) {
  val u = 1.dp.toPx() * s
  val cx = top.x
  val y = top.y + chain
  drawLine(LanternMetal, top, Offset(cx, y), strokeWidth = 1.dp.toPx())
  drawCircle(
    Brush.radialGradient(listOf(Color(0xFFFFD98A).copy(alpha = if (dark) 0.55f else 0.35f), Color.Transparent), center = Offset(cx, y + 17 * u), radius = 26 * u),
    radius = 26 * u, center = Offset(cx, y + 17 * u)
  )
  drawPath(Path().apply { moveTo(cx - 5 * u, y + 6 * u); quadraticBezierTo(cx, y, cx + 5 * u, y + 6 * u); close() }, LanternMetal)
  val body = Path().apply {
    moveTo(cx - 6 * u, y + 6.5f * u); lineTo(cx + 6 * u, y + 6.5f * u); lineTo(cx + 8 * u, y + 17 * u)
    lineTo(cx + 6 * u, y + 28 * u); lineTo(cx - 6 * u, y + 28 * u); lineTo(cx - 8 * u, y + 17 * u); close()
  }
  drawPath(body, LanternGlass)
  drawPath(body, LanternMetal, style = Stroke(0.8f * u))
  val inner = Path().apply {
    moveTo(cx - 2.7f * u, y + 8 * u); lineTo(cx + 2.7f * u, y + 8 * u); lineTo(cx + 4 * u, y + 17 * u)
    lineTo(cx + 2.7f * u, y + 26.5f * u); lineTo(cx - 2.7f * u, y + 26.5f * u); lineTo(cx - 4 * u, y + 17 * u); close()
  }
  drawPath(inner, LanternLight.copy(alpha = 0.95f))
  drawPath(Path().apply { moveTo(cx - 6.5f * u, y + 28 * u); lineTo(cx + 6.5f * u, y + 28 * u); lineTo(cx + 3 * u, y + 32 * u); lineTo(cx - 3 * u, y + 32 * u); close() }, LanternMetal)
}

/** "🌙 Ramazan'ın 12. günü" rozeti. */
@Composable
fun RamadanDayBadge(day: Int, modifier: Modifier = Modifier) {
  val cs = MaterialTheme.colorScheme
  Text(
    text = if (AppLanguage.isEnglish) "🌙 Day $day of Ramadan" else "🌙 Ramazan'ın $day. günü",
    fontSize = 12.sp, fontWeight = FontWeight.Bold, color = cs.onPrimary,
    modifier = modifier.clip(RoundedCornerShape(50)).background(cs.primary).padding(horizontal = 12.dp, vertical = 4.dp)
  )
}

/** İki minare arasında ampul ışığıyla yazılmış mahya. Yazı Ramazan'ın gününe göre değişir. */
@Composable
fun RamadanMahyaBar(day: Int, totalDays: Int = 30, modifier: Modifier = Modifier) {
  val cs = MaterialTheme.colorScheme
  val dark = isAppInDarkTheme()
  val text = remember(day) {
    val en = AppLanguage.isEnglish
    when {
      day <= 3 -> if (en) "WELCOME, O MONTH OF RAMADAN" else "HOŞ GELDİN YA ŞEHR-İ RAMAZAN"
      day >= totalDays - 2 -> if (en) "FAREWELL, O MONTH OF RAMADAN" else "ELVEDA YA ŞEHR-İ RAMAZAN"
      else -> {
        val list = if (en) listOf("SULTAN OF ELEVEN MONTHS", "RAMADAN IS BLESSING", "FASTING IS PATIENCE", "THE MONTH OF THE QURAN", "SHARE AND BE BLESSED")
        else listOf("ON BİR AYIN SULTANI", "RAMAZAN BEREKETTİR", "ORUÇ SABIRDIR", "KUR'AN AYI RAMAZAN", "PAYLAŞTIKÇA BEREKET")
        list[day % list.size]
      }
    }
  }
  val bulb = Color(0xFFFFE29A)
  Box(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 6.dp)
      .height(46.dp)
      .clip(RoundedCornerShape(14.dp))
      .background(if (dark) Color(0xFF0E0B06) else cs.primary)
      .border(1.dp, LanternMetal.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
      .testTag("ramadan_mahya"),
    contentAlignment = Alignment.Center
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val h = size.height
      fun minaret(x: Float) {
        drawRect(LanternMetal.copy(alpha = 0.65f), topLeft = Offset(x - 2.5f, h * 0.18f), size = Size(5f, h * 0.82f))
        drawPath(Path().apply { moveTo(x - 3.5f, h * 0.18f); lineTo(x, 0f); lineTo(x + 3.5f, h * 0.18f); close() }, LanternMetal.copy(alpha = 0.65f))
        drawRect(LanternMetal.copy(alpha = 0.65f), topLeft = Offset(x - 5f, h * 0.45f), size = Size(10f, 2.5f))
      }
      minaret(18f); minaret(size.width - 18f)
      val rope = Path().apply { moveTo(18f, h * 0.24f); quadraticBezierTo(size.width / 2f, h * 0.42f, size.width - 18f, h * 0.24f) }
      drawPath(rope, LanternMetal.copy(alpha = 0.6f), style = Stroke(1f))
    }
    Text(
      text = text,
      style = TextStyle(shadow = Shadow(bulb.copy(alpha = 0.9f), Offset.Zero, 12f)),
      fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp, color = bulb,
      textAlign = TextAlign.Center, maxLines = 1, modifier = Modifier.padding(horizontal = 30.dp)
    )
  }
}

/** Sayaç kartının köşesinde soluk hilal ve yıldız filigranı (Ramazan'da). */
@Composable
fun CrescentWatermark(modifier: Modifier = Modifier) {
  val c = MaterialTheme.colorScheme.primary
  Canvas(modifier = modifier) {
    // Gerçek hilal biçimi (iki dairenin farkı) — yarı saydam, alttaki yazıları örtmez
    val r = size.minDimension * 0.42f
    val center = Offset(size.width - r * 0.55f, size.height - r * 0.55f)
    val outer = androidx.compose.ui.graphics.Path().apply {
      addOval(androidx.compose.ui.geometry.Rect(center, r))
    }
    val innerC = Offset(center.x + r * 0.42f, center.y - r * 0.24f)
    val inner = androidx.compose.ui.graphics.Path().apply {
      addOval(androidx.compose.ui.geometry.Rect(innerC, r * 0.86f))
    }
    val crescent = androidx.compose.ui.graphics.Path().apply {
      op(outer, inner, androidx.compose.ui.graphics.PathOperation.Difference)
    }
    drawPath(crescent, c.copy(alpha = 0.08f))
  }
}

// ───────────────────────────── ORUÇ İLERLEME ÇUBUĞU ─────────────────────────────

private val FastingQuotes = listOf(
  Bi("Oruç bir kalkandır.", "Fasting is a shield.") to "Buhârî, Savm 2",
  Bi("Oruçlu için iki sevinç vardır: iftar ettiği an ve Rabbine kavuştuğu an.", "The fasting person has two joys: when he breaks his fast and when he meets his Lord.") to "Buhârî, Savm 9",
  Bi("Cennette Reyyân denilen bir kapı vardır; oradan yalnızca oruçlular girer.", "In Paradise there is a gate called Rayyan; only those who fast enter through it.") to "Buhârî, Savm 4",
  Bi("Oruç sizden öncekilere farz kılındığı gibi size de farz kılındı; umulur ki korunursunuz.", "Fasting is prescribed for you as it was for those before you, so that you may be mindful of God.") to "Bakara, 2/183",
  Bi("Oruçlunun ağız kokusu Allah katında misk kokusundan daha hoştur.", "The breath of the fasting person is sweeter to God than the scent of musk.") to "Buhârî, Savm 2",
  Bi("Kim inanarak ve karşılığını Allah'tan bekleyerek Ramazan orucunu tutarsa geçmiş günahları bağışlanır.", "Whoever fasts Ramadan with faith and hope of reward, his past sins are forgiven.") to "Buhârî, Îmân 28",
  Bi("Sabredenlere mükâfatları hesapsız ödenecektir.", "Those who are patient will be given their reward without measure.") to "Zümer, 39/10",
  Bi("Allah size kolaylık diler, zorluk dilemez.", "God wants ease for you, not hardship.") to "Bakara, 2/185",
  Bi("Kullarım sana beni sorduklarında, bilsinler ki ben onlara çok yakınım.", "When My servants ask you about Me, I am indeed near.") to "Bakara, 2/186",
  Bi("Şüphesiz zorlukla beraber bir kolaylık vardır.", "Indeed, with hardship comes ease.") to "İnşirâh, 94/6",
  Bi("Kim bir oruçluya iftar ettirirse, onun sevabı kadar sevap kazanır.", "Whoever gives iftar to a fasting person earns the same reward.") to "Tirmizî, Savm 82"
)

/**
 * Oruç tutulan günde (Ramazan veya planlı oruç), imsak ile iftar arasında orucun yüzde ilerlemesi,
 * iftara kalan süre ve günün saatine göre değişen dinî motivasyon cümlesi.
 * [tick] her saniye değişen bir değerdir (yeniden hesap için).
 */
@Composable
fun FastingProgressCard(prayerTimes: PrayerTimes, @Suppress("UNUSED_PARAMETER") tick: Long, modifier: Modifier = Modifier) {
  val cs = MaterialTheme.colorScheme
  val en = AppLanguage.isEnglish
  val now = Calendar.getInstance()
  val nowSec = now.get(Calendar.HOUR_OF_DAY) * 3600 + now.get(Calendar.MINUTE) * 60 + now.get(Calendar.SECOND)
  val imsakStr = PrayerTimeUtils.cleanTimeString(prayerTimes.imsak)
  val aksamStr = PrayerTimeUtils.cleanTimeString(prayerTimes.aksam)
  val start = PrayerTimeUtils.parseTimeToMinutes(imsakStr) * 60
  val end = PrayerTimeUtils.parseTimeToMinutes(aksamStr) * 60
  if (end <= start || nowSec < start || nowSec >= end) return
  val progress = (nowSec - start).toFloat() / (end - start)
  val remain = end - nowSec
  val pct = (progress * 100).toInt()
  // Cümle günde 4 kez değişir (ilerleme çeyreklerine göre)
  val idx = (now.get(Calendar.DAY_OF_YEAR) * 4 + (progress * 4).toInt().coerceAtMost(3)) % FastingQuotes.size
  val (quote, source) = FastingQuotes[idx]
  val track = cs.surfaceVariant
  val fillStart = lerp(cs.primary, Color.White, 0.25f)
  val fillEnd = cs.primary

  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 6.dp)
      .clip(RoundedCornerShape(20.dp))
      .background(cs.surface)
      .border(1.dp, cs.outline, RoundedCornerShape(20.dp))
      .padding(horizontal = 16.dp, vertical = 14.dp)
      .testTag("fasting_progress_card")
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Text(
        if (en) "Fast completed: $pct%" else "Orucun tamamlanan kısmı: %$pct",
        fontSize = 15.sp, fontWeight = FontWeight.Bold, color = cs.onSurface, modifier = Modifier.weight(1f)
      )
      Text(
        (if (en) "Iftar in " else "İftara ") + "${remain / 3600} ${if (en) "h" else "sa"} ${(remain % 3600) / 60} ${if (en) "min" else "dk"}",
        fontSize = 12.sp, fontWeight = FontWeight.Bold, color = cs.primary
      )
    }
    Spacer(modifier = Modifier.height(12.dp))
    // Çubuk: altın dolgu, ilerleme noktasında hilal, sonda fanus
    Canvas(modifier = Modifier.fillMaxWidth().height(26.dp)) {
      val h = 10.dp.toPx()
      val y = size.height / 2f - h / 2f
      val endPad = 14.dp.toPx()
      val w = size.width - endPad
      drawRoundRect(track, topLeft = Offset(0f, y), size = Size(w, h), cornerRadius = androidx.compose.ui.geometry.CornerRadius(h / 2f))
      val fw = (w * progress).coerceAtLeast(h)
      drawRoundRect(
        Brush.horizontalGradient(listOf(fillStart, fillEnd), startX = 0f, endX = fw),
        topLeft = Offset(0f, y), size = Size(fw, h), cornerRadius = androidx.compose.ui.geometry.CornerRadius(h / 2f)
      )
      // Hilal işaretçi
      val cx = fw.coerceIn(h, w - h / 2f)
      val cy = size.height / 2f
      val r = 9.dp.toPx()
      drawCircle(Color(0xFFFFD98A).copy(alpha = 0.35f), radius = r * 1.7f, center = Offset(cx, cy))
      drawCircle(Color(0xFFE9C77E), radius = r, center = Offset(cx, cy))
      drawCircle(cs.surface, radius = r * 0.82f, center = Offset(cx + r * 0.38f, cy - r * 0.18f))
      // Sonda iftar (fanus) işareti
      drawLantern(Offset(size.width - endPad / 2f, 0f), 0f, 0.7f, false)
    }
    Row(modifier = Modifier.fillMaxWidth()) {
      Text((if (en) "Imsak " else "İmsak ") + imsakStr, fontSize = 11.sp, color = cs.onSurfaceVariant, modifier = Modifier.weight(1f))
      Text((if (en) "Iftar " else "İftar ") + aksamStr, fontSize = 11.sp, color = cs.onSurfaceVariant)
    }
    Spacer(modifier = Modifier.height(10.dp))
    Text(
      "“" + quote.text + "”",
      fontSize = 13.sp, lineHeight = 19.sp, color = cs.onSurface, textAlign = TextAlign.Center,
      fontWeight = FontWeight.Medium, modifier = Modifier.fillMaxWidth()
    )
    Text(
      "— $source", fontSize = 11.sp, color = cs.onSurfaceVariant, textAlign = TextAlign.Center,
      fontWeight = FontWeight.SemiBold, modifier = Modifier.fillMaxWidth().padding(top = 2.dp)
    )
  }
}

@Suppress("unused")
private fun unusedTrig() = cos(0.0) + sin(0.0)
