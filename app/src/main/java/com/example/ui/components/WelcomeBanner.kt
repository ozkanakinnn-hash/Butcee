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
import com.example.R
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.Image
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
  mahya: List<String>? = null,
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
      // Günün camisinin fotoğrafı (varsa); yoksa çizim sahnesi
      val photo = when (todayMosque.dayName) {
        "Pazartesi" -> R.drawable.mosque_sultanahmet
        "Salı" -> R.drawable.mosque_ayasofya
        "Çarşamba" -> R.drawable.mosque_suleymaniye
        "Cuma" -> R.drawable.mosque_eyup
        "Cumartesi" -> R.drawable.mosque_ortakoy
        "Pazar" -> R.drawable.mosque_fatih
        else -> null
      }
      if (photo != null) {
        Image(
          painter = painterResource(photo),
          contentDescription = todayMosque.mosqueName,
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )
      } else {
        IstanbulMosqueScene(
          spec = when (todayMosque.dayName) {
            "Pazartesi" -> IstanbulMosques.sultanahmet
            "Salı" -> IstanbulMosques.ayasofya
            "Çarşamba" -> IstanbulMosques.suleymaniye
            "Perşembe" -> IstanbulMosques.camlica
            "Cuma" -> IstanbulMosques.eyup
            "Cumartesi" -> IstanbulMosques.ortakoy
            else -> IstanbulMosques.fatih
          },
          glow = shimmerOffset,
          mahya = mahya,
          modifier = Modifier.fillMaxSize()
        )
      }

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
