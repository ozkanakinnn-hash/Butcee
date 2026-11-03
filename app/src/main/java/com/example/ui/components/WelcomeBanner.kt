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
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
  val isDark = isSystemInDarkTheme()

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

  // Cream, beige and gold luxury palette
  val cardContainerColor = if (isDark) Color(0xFF22201D) else Color(0xFFF9F4EB)
  val goldAccent = if (isDark) Color(0xFFD4AF37) else Color(0xFF9E6B20)
  val deepTextColor = if (isDark) Color(0xFFEDE5D8) else Color(0xFF382A1B)

  Card(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 6.dp)
      .testTag("welcome_ramadan_banner"),
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(containerColor = cardContainerColor),
    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2D6C0).copy(alpha = 0.7f)),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Box(modifier = Modifier.fillMaxWidth()) {
      // 1. SOFT BLUR BACKGROUND MOSQUE LAYER (Hafif arka plan blur efekti)
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(145.dp)
          .blur(5.dp)
      ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          val w = size.width
          val h = size.height

          // Soft diffused background radial light
          drawCircle(
            brush = Brush.radialGradient(
              colors = listOf(
                Color(0xFFEEDCBB).copy(alpha = 0.45f * shimmerOffset),
                Color.Transparent
              ),
              center = Offset(w * 0.5f, h * 0.4f),
              radius = w * 0.45f
            )
          )

          // Soft Mosque Dome & Minaret Silhouettes in warm gold/beige
          val silhouetteColor = if (isDark) Color(0xFF332D24).copy(alpha = 0.5f) else Color(0xFFDACBB4).copy(alpha = 0.6f)

          // Central Grand Dome
          val domeRadius = w * 0.22f
          val domeCenter = Offset(w * 0.5f, h * 0.85f)
          drawCircle(
            color = silhouetteColor,
            radius = domeRadius,
            center = domeCenter
          )

          // Side Half Domes
          drawCircle(
            color = silhouetteColor.copy(alpha = 0.4f),
            radius = domeRadius * 0.65f,
            center = Offset(w * 0.32f, h * 0.90f)
          )
          drawCircle(
            color = silhouetteColor.copy(alpha = 0.4f),
            radius = domeRadius * 0.65f,
            center = Offset(w * 0.68f, h * 0.90f)
          )

          // Left Minaret
          val minaretWidth = 8f
          drawRect(
            color = silhouetteColor,
            topLeft = Offset(w * 0.18f - minaretWidth / 2, h * 0.15f),
            size = androidx.compose.ui.geometry.Size(minaretWidth, h * 0.85f)
          )
          val leftSpire = Path().apply {
            moveTo(w * 0.18f - minaretWidth, h * 0.15f)
            lineTo(w * 0.18f, h * 0.04f)
            lineTo(w * 0.18f + minaretWidth, h * 0.15f)
            close()
          }
          drawPath(leftSpire, color = silhouetteColor)

          // Right Minaret
          drawRect(
            color = silhouetteColor,
            topLeft = Offset(w * 0.82f - minaretWidth / 2, h * 0.15f),
            size = androidx.compose.ui.geometry.Size(minaretWidth, h * 0.85f)
          )
          val rightSpire = Path().apply {
            moveTo(w * 0.82f - minaretWidth, h * 0.15f)
            lineTo(w * 0.82f, h * 0.04f)
            lineTo(w * 0.82f + minaretWidth, h * 0.15f)
            close()
          }
          drawPath(rightSpire, color = silhouetteColor)
        }
      }

      // 2. FOREGROUND CONTENT WITH LUXURY CREAM/BEIGE/GOLD TYPOGRAPHY
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 18.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Daily Mosque Badge
        val badgeBg = if (isDark) Color(0xFF2E2A24) else Color(0xFFEFE6D5).copy(alpha = 0.95f)
        val badgeBorder = if (isDark) Color(0xFF7A684C) else Color(0xFFD8C7AA)
        Row(
          modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(badgeBg)
            .border(1.dp, badgeBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(todayMosque.emoji, fontSize = 13.sp)
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "${todayMosque.dayName} • Tarihi İstanbul Camileri",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = goldAccent
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Mosque Name
        Text(
          text = todayMosque.mosqueName,
          fontSize = 17.sp,
          fontWeight = FontWeight.Bold,
          color = deepTextColor,
          textAlign = TextAlign.Center
        )

        Text(
          text = todayMosque.titleDesc,
          fontSize = 11.sp,
          fontWeight = FontWeight.Medium,
          color = goldAccent,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Spiritual Quote
        Text(
          text = "“${todayMosque.spiritualQuote}”",
          fontSize = 12.sp,
          fontStyle = FontStyle.Italic,
          lineHeight = 17.sp,
          color = deepTextColor.copy(alpha = 0.85f),
          textAlign = TextAlign.Center,
          modifier = Modifier.padding(horizontal = 8.dp)
        )
      }
    }
  }
}
