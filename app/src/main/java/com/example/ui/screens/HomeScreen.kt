package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppPreferences
import com.example.data.CountdownResult
import com.example.data.HolidayCountdownInfo
import com.example.data.PrayerTimes
import com.example.data.RamadanMenu
import com.example.data.SpiritualItem
import com.example.ui.components.AdBannerComponent
import com.example.ui.components.CountdownCard
import com.example.ui.components.DailySpiritualCard
import com.example.ui.components.HolidayCountdownCard
import com.example.ui.components.IftarMenuCard
import com.example.ui.components.PrayerTimesCard
import com.example.ui.components.PrivacyFooter
import com.example.ui.components.WelcomeBanner

@Composable
fun HomeScreen(
  prefs: AppPreferences,
  prayerTimes: PrayerTimes,
  countdown: CountdownResult,
  holidayInfo: HolidayCountdownInfo,
  menu: RamadanMenu,
  spiritualItem: SpiritualItem,
  onOpenLocationDialog: () -> Unit,
  onOpenSettings: () -> Unit,
  onOpenAllMenus: () -> Unit,
  onOpenQibla: () -> Unit,
  onOpenMosques: () -> Unit,
  onFireCannon: () -> Unit,
  modifier: Modifier = Modifier
) {
  val isRamadanSeason = holidayInfo.title.contains("Bayramı", ignoreCase = true)
  val greetingName = if (prefs.userName.isNotBlank()) prefs.userName else "Değerli Kardeşimiz"

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("home_screen")
  ) {
    // 1. Top Bar Greeting & Location Action
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Merhaba, $greetingName 👋",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
          )
          Spacer(modifier = Modifier.height(2.dp))
          // Clickable location badge
          Row(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .clickable { onOpenLocationDialog() }
              .padding(vertical = 2.dp, horizontal = 2.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.LocationOn,
              contentDescription = "Konum Değiştir",
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "${prefs.province} / ${prefs.district}",
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "• Değiştir",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        IconButton(
          onClick = onOpenSettings,
          modifier = Modifier.testTag("home_settings_button")
        ) {
          Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = "Ayarlar",
            tint = MaterialTheme.colorScheme.onBackground
          )
        }
      }
    }

    // 2. Ramadan Minarets & Welcome Banner
    item {
      WelcomeBanner(
        isRamadanSeason = isRamadanSeason
      )
    }

    // 3. Smart Countdown Card (İftara Kalan / İmsaka Kalan)
    item {
      CountdownCard(
        countdown = countdown,
        city = prefs.province,
        district = prefs.district,
        onFireCannon = onFireCannon
      )
    }

    // 4. SHORTCUT 1: KIBLE BULUCU (Direct single-tap shortcut requested by user)
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 4.dp)
          .clickable { onOpenQibla() }
          .testTag("shortcut_qibla_button"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
              contentAlignment = Alignment.Center
            ) {
              Text("🧭", fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "Kıble Bulucu (Pusula)",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "${prefs.province} için Kâbe yönü ve derece göstergesi",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
          Text("Bul ›", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
        }
      }
    }

    // 5. SHORTCUT 2: YAKINDAKİ CAMİLER (Placed directly below Qibla Finder)
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 4.dp)
          .clickable { onOpenMosques() }
          .testTag("shortcut_mosques_button"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
              contentAlignment = Alignment.Center
            ) {
              Text("🕌", fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "Yakınımdaki Camiler",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "${prefs.province} / ${prefs.district} mescitleri ve canlı navigasyon",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
          Text("Gör ›", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
        }
      }
    }

    // 6. Daily Prayer Times (6 vakit)
    item {
      PrayerTimesCard(
        prayerTimes = prayerTimes
      )
    }

    // 7. Smart Holiday Countdown (Ramazan'a / Ramazan Bayramı'na / Kurban Bayramı'na)
    item {
      HolidayCountdownCard(
        info = holidayInfo
      )
    }

    // 8. Günün İftar Menüsü (Text-only 4 categories)
    item {
      IftarMenuCard(
        menu = menu,
        onViewAllMenus = onOpenAllMenus
      )
    }

    // 9. Günün Ayeti / Hadisi / Duası
    item {
      DailySpiritualCard(
        item = spiritualItem
      )
    }

    // 10. Google AdMob Banner
    item {
      AdBannerComponent()
    }

    // 11. Local Privacy Statement
    item {
      PrivacyFooter()
    }
  }
}
