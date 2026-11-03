package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.R
import androidx.compose.ui.unit.sp
import com.example.data.AppPreferences
import com.example.data.CountdownResult
import com.example.data.HolidayCountdownInfo
import com.example.data.PrayerTimes
import com.example.data.RamadanMenu
import com.example.data.SpiritualItem
import com.example.ui.components.AdBannerComponent
import com.example.ui.components.CompactCountdownCard
import com.example.ui.components.DailySpiritualCard
import com.example.ui.components.DiyanetRadioCard
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

        Row(verticalAlignment = Alignment.CenterVertically) {
          // Kıble Bulucu kısayolu (Kâbe ikonu)
          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primaryContainer)
              .clickable { onOpenQibla() }
              .testTag("shortcut_qibla_button"),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              painter = painterResource(R.drawable.ic_kaaba),
              contentDescription = "Kıble Bulucu",
              tint = Color.Unspecified,
              modifier = Modifier.size(24.dp)
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
          // Yakınımdaki Camiler kısayolu (cami ikonu)
          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primaryContainer)
              .clickable { onOpenMosques() }
              .testTag("shortcut_mosques_button"),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Mosque,
              contentDescription = "Yakınımdaki Camiler",
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(22.dp)
            )
          }
          Spacer(modifier = Modifier.width(4.dp))
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
    }

    // 2. Üst satır: iki eşit pano — (1) Günün İstanbul camisi, (2) İftara/İmsaka kalan vakit
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .height(250.dp)
          .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        WelcomeBanner(
          isRamadanSeason = isRamadanSeason,
          modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
        )
        CompactCountdownCard(
          countdown = countdown,
          onFireCannon = onFireCannon,
          modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
        )
      }
    }

    // 3. Günlük Vakit Namazları tablosu (eski geri sayım panosunun yeri)
    item {
      PrayerTimesCard(
        prayerTimes = prayerTimes
      )
    }

    // 3a. Diyanet Radyo (canlı yayın, arka planda çalar)
    item {
      DiyanetRadioCard()
    }

    // 4. Smart Holiday Countdown (Ramazan'a / Ramazan Bayramı'na / Kurban Bayramı'na)
    item {
      HolidayCountdownCard(
        info = holidayInfo
      )
    }

    // 5. Günün İftar Menüsü (Text-only 4 categories)
    item {
      IftarMenuCard(
        menu = menu,
        onViewAllMenus = onOpenAllMenus
      )
    }

    // 6. Günün Ayeti / Hadisi / Duası
    item {
      DailySpiritualCard(
        item = spiritualItem
      )
    }

    // 7. Google AdMob Banner
    item {
      AdBannerComponent()
    }

    // 8. Local Privacy Statement
    item {
      PrivacyFooter()
    }
  }
}
