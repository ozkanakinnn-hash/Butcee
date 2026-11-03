package com.example.ui.components

import com.example.i18n.t
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
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.blur
import androidx.compose.foundation.layout.BoxWithConstraints
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
        "Perşembe" -> R.drawable.mosque_camlica
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

      // 2. Alt kısımda isim okunsun diye hafif koyulaşma
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(
            Brush.verticalGradient(
              0.0f to Color.Transparent,
              0.6f to Color.Transparent,
              1.0f to Color.Black.copy(alpha = 0.65f)
            )
          )
      )

      // 3. Orta-altta buzlu cam (şeffaf, bulanık) panel üzerinde günün mesajı
      val message = remember(com.example.i18n.AppLanguage.current) {
        com.example.data.DailyMessages.today(com.example.i18n.AppLanguage.isEnglish)
      }
      BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val panelTop = maxHeight * 0.50f
        val panelBottom = maxHeight * 0.84f
        val side = 10.dp
        // Fotoğrafın bulanık kopyası yalnızca panel alanında çizilir (Android 12+ gerçek bulanıklık)
        if (photo != null) {
          Image(
            painter = painterResource(photo),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
              .fillMaxSize()
              .drawWithContent {
                val l = side.toPx(); val r = size.width - side.toPx()
                val tp = panelTop.toPx(); val bt = panelBottom.toPx()
                val path = androidx.compose.ui.graphics.Path().apply {
                  addRoundRect(androidx.compose.ui.geometry.RoundRect(l, tp, r, bt, androidx.compose.ui.geometry.CornerRadius(16.dp.toPx())))
                }
                clipPath(path) { this@drawWithContent.drawContent() }
              }
              .blur(18.dp)
          )
        }
        Box(
          modifier = Modifier
            .padding(start = side, end = side, top = panelTop)
            .fillMaxWidth()
            .height(panelBottom - panelTop)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.16f))
            .border(1.dp, Color.White.copy(alpha = 0.28f), RoundedCornerShape(16.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "“$message”",
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.SemiBold,
            fontStyle = FontStyle.Italic,
            color = Color.White,
            textAlign = TextAlign.Center,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis
          )
        }
        // 4. En altta küçük yazıyla cami adı
        Text(
          text = todayMosque.mosqueName,
          fontSize = 9.sp,
          fontWeight = FontWeight.Medium,
          color = Color.White.copy(alpha = 0.85f),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          textAlign = TextAlign.Center,
          modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 7.dp)
        )
      }
    }
  }
}
