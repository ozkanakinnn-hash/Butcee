package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar

data class IstanbulMosqueDay(
  val dayName: String,
  val mosqueName: String,
  val titleDesc: String,
  val spiritualQuote: String,
  val minaretCount: Int,
  val emoji: String
)

@Composable
fun WelcomeBanner(
  isRamadanSeason: Boolean = false,
  modifier: Modifier = Modifier
) {

  // 7-Day Historic Istanbul Mosques Cycle based on Day of Week
  val todayMosque = remember {
    val dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
    when (dayOfWeek) {
      Calendar.MONDAY -> IstanbulMosqueDay(
        dayName = "Pazartesi",
        mosqueName = "Sultanahmet Camii",
        titleDesc = "Mavi Cami • 6 Minareli İhtişam",
        spiritualQuote = "Sultanahmet'in altı minaresinden yankılanan ezanlar kalbimize ferahlık versin.",
        minaretCount = 6,
        emoji = "🕌"
      )
      Calendar.TUESDAY -> IstanbulMosqueDay(
        dayName = "Salı",
        mosqueName = "Ayasofya-i Kebîr Câmi-i Şerîfi",
        titleDesc = "Fethin Yadigârı • Kadim Mabed",
        spiritualQuote = "Fethin ve vuslatın sembolü Ayasofya'da yükselen dualar arşa ulaşsın.",
        minaretCount = 4,
        emoji = "✨"
      )
      Calendar.WEDNESDAY -> IstanbulMosqueDay(
        dayName = "Çarşamba",
        mosqueName = "Süleymaniye Camii",
        titleDesc = "Mimar Sinan'ın Kalfalık Şaheseri",
        spiritualQuote = "Mimar Sinan'ın zarafetinde, sabır ve tevazu ile Ramazan'ı karşılıyoruz.",
        minaretCount = 4,
        emoji = "🏛️"
      )
      Calendar.THURSDAY -> IstanbulMosqueDay(
        dayName = "Perşembe",
        mosqueName = "Büyük Çamlıca Camii",
        titleDesc = "Boğaziçi'nin Zirvesi • Modern Silüet",
        spiritualQuote = "Yeditepe İstanbul'un zirvesinden göklere uzanan ellerimiz boş çevrilmesin.",
        minaretCount = 6,
        emoji = "🌙"
      )
      Calendar.FRIDAY -> IstanbulMosqueDay(
        dayName = "Cuma",
        mosqueName = "Eyüp Sultan Camii",
        titleDesc = "Maneviyatın ve Cuma'nın Dua Kapısı",
        spiritualQuote = "Eyüp Sultan Hazretleri'nin manevi gölgesinde, Cuma'nın bereketi üzerinize olsun.",
        minaretCount = 2,
        emoji = "🕊️"
      )
      Calendar.SATURDAY -> IstanbulMosqueDay(
        dayName = "Cumartesi",
        mosqueName = "Ortaköy Büyük Mecidiye Camii",
        titleDesc = "Boğaziçi'nin İncisi • Zarif Barok",
        spiritualQuote = "Boğaz'ın serinliğinde, kalbimizi manevi huzur ve sükûnetle dolduruyoruz.",
        minaretCount = 2,
        emoji = "🌊"
      )
      else -> IstanbulMosqueDay(
        dayName = "Pazar",
        mosqueName = "Fatih Camii",
        titleDesc = "Fatih Sultan Mehmed Külliyesi",
        spiritualQuote = "Tarihin köklü mirasında, hayır ve ibadet yolunda adımlarımız bereketlensin.",
        minaretCount = 2,
        emoji = "📖"
      )
    }
  }

  val infiniteTransition = rememberInfiniteTransition(label = "banner_warm_glow")
  val shimmerOffset by infiniteTransition.animateFloat(
    initialValue = 0.85f,
    targetValue = 1.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(2800, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "shimmer_glow"
  )

  Card(
    modifier = modifier.testTag("welcome_ramadan_banner"),
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2440)),
    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      // 1. ARKA PLAN: Günün İstanbul camisi — akşam vakti silüeti
      IstanbulMosqueScene(
        minaretCount = todayMosque.minaretCount,
        glow = shimmerOffset,
        modifier = Modifier.fillMaxSize()
      )

      // 2. Metnin okunması için alttan koyulaşan perde
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(
            Brush.verticalGradient(
              0.0f to Color.Black.copy(alpha = 0.15f),
              0.45f to Color.Transparent,
              1.0f to Color.Black.copy(alpha = 0.82f)
            )
          )
      )

      // 3. ÖN PLAN METİNLERİ
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(12.dp)
      ) {
        Row(
          modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color.Black.copy(alpha = 0.35f))
            .border(1.dp, Color(0xFFE5B869).copy(alpha = 0.6f), RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(todayMosque.emoji, fontSize = 11.sp)
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = todayMosque.dayName,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFE5B869)
          )
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
          text = "Tarihi İstanbul Camileri",
          fontSize = 9.sp,
          fontWeight = FontWeight.SemiBold,
          color = Color(0xFFE5B869).copy(alpha = 0.9f)
        )
        Text(
          text = todayMosque.mosqueName,
          fontSize = 15.sp,
          lineHeight = 18.sp,
          fontWeight = FontWeight.Bold,
          color = Color.White,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis
        )
        Text(
          text = todayMosque.titleDesc,
          fontSize = 10.sp,
          fontWeight = FontWeight.Medium,
          color = Color(0xFFFFD56B),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "“${todayMosque.spiritualQuote}”",
          fontSize = 10.sp,
          fontStyle = FontStyle.Italic,
          lineHeight = 13.sp,
          color = Color.White.copy(alpha = 0.88f),
          maxLines = 4,
          overflow = TextOverflow.Ellipsis
        )
      }
    }
  }
}

/** Akşam ezanı vaktinde İstanbul camisi: gökyüzü, hilal, kubbeler, minareler ve deniz yansıması. */
@Composable
private fun IstanbulMosqueScene(
  minaretCount: Int,
  glow: Float,
  modifier: Modifier = Modifier
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height
    val baseY = h * 0.58f
    val silhouette = Color(0xFF16110E)

    // Gökyüzü (lacivert → mor → kehribar)
    drawRect(
      brush = Brush.verticalGradient(
        colors = listOf(Color(0xFF1B2440), Color(0xFF473A5E), Color(0xFFB9744A), Color(0xFFE8B66A)),
        startY = 0f,
        endY = baseY
      ),
      size = Size(w, baseY)
    )
    // Ufuktaki güneş ışıltısı
    drawCircle(
      brush = Brush.radialGradient(
        colors = listOf(Color(0xFFFFD56B).copy(alpha = 0.55f * glow), Color.Transparent),
        center = Offset(w * 0.5f, baseY),
        radius = w * 0.6f
      ),
      center = Offset(w * 0.5f, baseY),
      radius = w * 0.6f
    )

    // Yıldızlar
    listOf(0.12f to 0.08f, 0.30f to 0.15f, 0.55f to 0.06f, 0.64f to 0.20f, 0.92f to 0.30f, 0.20f to 0.27f)
      .forEach { (x, y) ->
        drawCircle(Color.White.copy(alpha = 0.75f), radius = w * 0.008f, center = Offset(w * x, h * y))
      }

    // Hilal
    val moonC = Offset(w * 0.80f, h * 0.12f)
    val moonR = w * 0.065f
    drawCircle(Color(0xFFFFE3A3), radius = moonR, center = moonC)
    drawCircle(Color(0xFF1E2643), radius = moonR * 0.85f, center = Offset(moonC.x + moonR * 0.4f, moonC.y - moonR * 0.15f))

    // Deniz (Boğaz) ve yansıma
    drawRect(
      brush = Brush.verticalGradient(
        colors = listOf(Color(0xFF6B4A3A), Color(0xFF1E2238)),
        startY = baseY,
        endY = h
      ),
      topLeft = Offset(0f, baseY),
      size = Size(w, h - baseY)
    )
    for (k in 1..5) {
      val y = baseY + (h - baseY) * k / 7f
      drawLine(
        color = Color(0xFFFFD56B).copy(alpha = 0.18f - k * 0.025f),
        start = Offset(w * (0.25f + k * 0.03f), y),
        end = Offset(w * (0.75f - k * 0.03f), y),
        strokeWidth = 2f
      )
    }

    // Cami gövdesi
    val bodyTop = baseY - h * 0.10f
    drawRect(silhouette, topLeft = Offset(w * 0.18f, bodyTop), size = Size(w * 0.64f, baseY - bodyTop))

    // Yan yarım kubbeler
    val sideR = w * 0.12f
    listOf(0.30f, 0.70f).forEach { x ->
      drawArc(
        color = silhouette, startAngle = 180f, sweepAngle = 180f, useCenter = true,
        topLeft = Offset(w * x - sideR, bodyTop - sideR * 0.55f), size = Size(sideR * 2, sideR * 2)
      )
    }
    // Ana kubbe + kasnak + alem
    val domeR = w * 0.19f
    val drumTop = bodyTop - h * 0.05f
    drawRect(silhouette, topLeft = Offset(w * 0.5f - domeR * 0.92f, drumTop), size = Size(domeR * 1.84f, bodyTop - drumTop + 2f))
    drawArc(
      color = silhouette, startAngle = 180f, sweepAngle = 180f, useCenter = true,
      topLeft = Offset(w * 0.5f - domeR, drumTop - domeR), size = Size(domeR * 2, domeR * 2)
    )
    drawLine(silhouette, Offset(w * 0.5f, drumTop - domeR), Offset(w * 0.5f, drumTop - domeR - h * 0.05f), strokeWidth = w * 0.012f)

    // Minareler (günün camisinin minare sayısı kadar)
    val xs = when {
      minaretCount >= 6 -> listOf(0.05f, 0.13f, 0.21f, 0.79f, 0.87f, 0.95f)
      minaretCount >= 4 -> listOf(0.06f, 0.16f, 0.84f, 0.94f)
      else -> listOf(0.09f, 0.91f)
    }
    val mw = w * 0.026f
    xs.forEach { fx ->
      val outer = fx < 0.08f || fx > 0.92f
      val top = if (outer) h * 0.14f else h * 0.20f
      val cx = w * fx
      drawRect(silhouette, topLeft = Offset(cx - mw / 2, top), size = Size(mw, baseY - top))
      // Şerefeler
      listOf(0.35f, 0.62f).forEach { t ->
        val by = top + (baseY - top) * t
        drawRect(silhouette, topLeft = Offset(cx - mw, by), size = Size(mw * 2, h * 0.012f))
      }
      // Külah
      val spire = Path().apply {
        moveTo(cx - mw * 0.75f, top)
        lineTo(cx, top - h * 0.07f)
        lineTo(cx + mw * 0.75f, top)
        close()
      }
      drawPath(spire, silhouette)
    }
  }
}
