package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppPreferences
import com.example.data.PrayerTimes
import com.example.data.RamadanMenu
import com.example.data.RamadanMenus
import com.example.ui.components.AdBannerComponent
import com.example.ui.components.PrivacyFooter
import java.util.Calendar

@Composable
fun MenusScreen(
  prefs: AppPreferences,
  prayerTimes: PrayerTimes? = null,
  modifier: Modifier = Modifier
) {
  var selectedDay by remember { mutableIntStateOf(1) }
  val currentMenu: RamadanMenu = remember(selectedDay) {
    RamadanMenus.getMenuForDay(selectedDay)
  }

  // Fasting Duration & Progress Calculation
  val imsakStr = prayerTimes?.imsak ?: "05:45"
  val aksamStr = prayerTimes?.aksam ?: "18:45"

  val (durationHours, durationMinutes, progressFloat, progressPercentStr, statusMessage) = remember(imsakStr, aksamStr) {
    calculateFastingStats(imsakStr, aksamStr)
  }

  val animatedProgress by animateFloatAsState(
    targetValue = progressFloat,
    label = "fasting_progress_bar"
  )

  val dailyMotivationalQuote = remember(selectedDay) {
    getMotivationalQuoteForDay(selectedDay)
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("ramadan_guide_screen")
  ) {
    // 1. Header
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.RestaurantMenu,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Ramazan Rehberi & Günlük Menü",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
          )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "${prefs.province} / ${prefs.district} • Günlük oruç istatistikleri ve dengeli iftar menüsü",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    // 2. DAILY FASTING STATISTICS & PROGRESS CARD
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(38.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Schedule,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(22.dp)
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "Bugünkü Oruç Süresi",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  text = "$durationHours Saat $durationMinutes Dakika",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary
                )
              }
            }

            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF2D5A43))
                .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
              Text(
                text = progressPercentStr,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Live Animated Progress Bar
          LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
              .fillMaxWidth()
              .height(10.dp)
              .clip(RoundedCornerShape(6.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
          )

          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "İmsak: $imsakStr",
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = statusMessage,
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.primary
            )
            Text(
              text = "İftar: $aksamStr",
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }

    // 3. MOTIVATIONAL DAILY SENTENCE (GÜNÜN MOTİVE EDİCİ CÜMLESİ)
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        )
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
          ) {
            Text("✨", fontSize = 18.sp)
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text = "Günün Manevi İlhamı & Motivasyonu",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "“$dailyMotivationalQuote”",
              fontSize = 12.sp,
              fontStyle = FontStyle.Italic,
              lineHeight = 17.sp,
              color = MaterialTheme.colorScheme.onPrimaryContainer
            )
          }
        }
      }
    }

    // 4. DAY SELECTOR & TITLE (ONLY TODAY'S MENU DISPLAYED, PREVENTING 30-DAY DUMP)
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(
            onClick = { if (selectedDay > 1) selectedDay -= 1 },
            enabled = selectedDay > 1
          ) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Önceki Gün")
          }

          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = "${currentMenu.day}. Gün İftar Menüsü",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = if (selectedDay == 1) "Bugünün Özel Menüsü" else "${currentMenu.day} / 30 Ramazan",
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.SemiBold
            )
          }

          IconButton(
            onClick = { if (selectedDay < 30) selectedDay += 1 },
            enabled = selectedDay < 30
          ) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Sonraki Gün")
          }
        }
      }
    }

    // 5. TODAY'S EXCLUSIVE MENU ITEMS (Clean, elegant 4 courses)
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          MenuCourseRow(
            icon = "🍲",
            category = "Çorba / Başlangıç",
            dish = currentMenu.soup
          )

          Spacer(modifier = Modifier.height(12.dp))
          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.5.dp)
          Spacer(modifier = Modifier.height(12.dp))

          MenuCourseRow(
            icon = "🍛",
            category = "Ana Yemek",
            dish = currentMenu.mainDish
          )

          Spacer(modifier = Modifier.height(12.dp))
          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.5.dp)
          Spacer(modifier = Modifier.height(12.dp))

          MenuCourseRow(
            icon = "🥗",
            category = "Yan Lezzet & Salata",
            dish = currentMenu.sideDish
          )

          Spacer(modifier = Modifier.height(12.dp))
          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.5.dp)
          Spacer(modifier = Modifier.height(12.dp))

          MenuCourseRow(
            icon = "🍮",
            category = "Tatlı & İkram",
            dish = currentMenu.dessert
          )
        }
      }
    }

    // 6. HEALTHY IFTAR & SAHUR RECOMMENDATION
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.Lightbulb,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "Şifalı İftar & Sahur Önerisi",
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = getHealthTipForDay(currentMenu.day),
              fontSize = 11.sp,
              lineHeight = 16.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }

    // 7. AdMob Banner
    item {
      AdBannerComponent()
    }

    // 8. Footer
    item {
      PrivacyFooter()
    }
  }
}

@Composable
private fun MenuCourseRow(
  icon: String,
  category: String,
  dish: String
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(40.dp)
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.primaryContainer),
      contentAlignment = Alignment.Center
    ) {
      Text(icon, fontSize = 20.sp)
    }
    Spacer(modifier = Modifier.width(14.dp))
    Column {
      Text(
        text = category,
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = dish,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )
    }
  }
}

private data class FastingStats(
  val durationHours: Int,
  val durationMinutes: Int,
  val progressFloat: Float,
  val progressPercentStr: String,
  val statusMessage: String
)

private fun calculateFastingStats(imsakStr: String, aksamStr: String): FastingStats {
  val imsakParts = imsakStr.split(":").mapNotNull { it.toIntOrNull() }
  val aksamParts = aksamStr.split(":").mapNotNull { it.toIntOrNull() }

  val imsakMinutes = if (imsakParts.size == 2) imsakParts[0] * 60 + imsakParts[1] else 5 * 60 + 45
  val aksamMinutes = if (aksamParts.size == 2) aksamParts[0] * 60 + aksamParts[1] else 18 * 60 + 45

  val totalDurationMin = (aksamMinutes - imsakMinutes).coerceAtLeast(60)
  val hours = totalDurationMin / 60
  val mins = totalDurationMin % 60

  val cal = Calendar.getInstance()
  val nowMinutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)

  return when {
    nowMinutes < imsakMinutes -> {
      val minToImsak = imsakMinutes - nowMinutes
      FastingStats(
        durationHours = hours,
        durationMinutes = mins,
        progressFloat = 0f,
        progressPercentStr = "%0",
        statusMessage = "Sahur Vakti • İmsaka ${minToImsak / 60} sa ${minToImsak % 60} dk"
      )
    }
    nowMinutes >= aksamMinutes -> {
      FastingStats(
        durationHours = hours,
        durationMinutes = mins,
        progressFloat = 1.0f,
        progressPercentStr = "%100",
        statusMessage = "Bugünkü Oruç Tamamlandı 🤲"
      )
    }
    else -> {
      val passed = (nowMinutes - imsakMinutes).toFloat()
      val fraction = (passed / totalDurationMin).coerceIn(0f, 1f)
      val percent = (fraction * 100).toInt()
      val remaining = aksamMinutes - nowMinutes
      FastingStats(
        durationHours = hours,
        durationMinutes = mins,
        progressFloat = fraction,
        progressPercentStr = "%$percent",
        statusMessage = "İftara ${remaining / 60} sa ${remaining % 60} dk kaldı"
      )
    }
  }
}

private fun getMotivationalQuoteForDay(day: Int): String {
  val quotes = listOf(
    "Oruç, bedenin sabrı, ruhun huzurudur. Sabrettiğin her an cennet kapılarından biri olan Reyyân'a bir adımdır.",
    "Açlık nefsin terbiyesi, tokluk ise şükrün kapısıdır. Bugün tuttuğun oruç yarın kalbine nur olsun.",
    "Sabır ile kılınan namaz ve tutulan oruç, kul ile Rabbi arasındaki en samimi sırdır.",
    "Oruç sadece mideyi değil; gözü, dili, kulağı ve kalbi de kötülüklerden koruma kalkanıdır.",
    "Bir tas çorbanın, bir yudum suyun kıymetini öğreten oruç; zengini fakirle, kalbi merhametle buluşturur.",
    "Göz açıp kapayıncaya kadar geçen ömürde, Ramazan'ın her anı ebedi bir kazanç mevsimidir.",
    "İftar vaktinde gökyüzüne açılan eller boş çevrilmez; dualarınız kabul, orucunuz makbul olsun."
  )
  return quotes[(day - 1) % quotes.size]
}

private fun getHealthTipForDay(day: Int): String {
  val tips = listOf(
    "İftarda orucunuzu hurma ve ılık suyla açıp 10-15 dakika bekledikten sonra ana yemeğe geçmeniz sindirimi kolaylaştırır.",
    "Sahurda bol protein (yumurta, peynir) ve lifli gıdalar tüketerek gün boyu tokluk sağlayabilirsiniz.",
    "İftar ile sahur arasında en az 2-2.5 litre su içmeyi ve çay-kahve tüketimini sınırlandırmayı ihmal etmeyiniz.",
    "Sahurda aşırı tuzlu ve baharatlı yiyeceklerden kaçınmak, gün içinde susuzluk hissini önemli ölçüde azaltır.",
    "Yemekleri yavaş yiyip iyice çiğnemek mide kramplarını ve ani tansiyon değişimlerini önler.",
    "İftardan 1-2 saat sonra yapacağınız hafif tempolu 20 dakikalık bir yürüyüş metabolizmayı canlandırır.",
    "Tatlı ihtiyacınızı ağır şerbetliler yerine sütlü tatlılar veya taze meyveyle karşılamak kan şekeri dengesini korur."
  )
  return tips[(day - 1) % tips.size]
}
