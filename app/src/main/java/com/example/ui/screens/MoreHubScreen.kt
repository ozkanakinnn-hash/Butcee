package com.example.ui.screens

import com.example.i18n.t
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.AppPreferences
import com.example.ui.components.AdBannerComponent
import com.example.ui.components.PrivacyFooter

@Composable
fun MoreHubScreen(
  prefs: AppPreferences,
  onOpenQuran: () -> Unit,
  onOpenQibla: () -> Unit,
  onOpenMosques: () -> Unit,
  onOpenFastingTracker: () -> Unit,
  onOpenNotes: () -> Unit,
  onOpenSettings: () -> Unit,
  onOpenRadioTv: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("more_hub_screen")
  ) {
    // Header
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
      ) {
        Text(
          text = t("Dahası & Faydalı Araçlar"),
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onBackground
        )
        Text(
          text = t("Kur'an-ı Kerim, Kıble bulucu, camiler ve zengin İslami modüller"),
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    // Grid row 1: Kur'an & Kıble
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        HubCard(
          title = t("Kur'an-ı Kerîm"),
          subtitle = t("114 Sure • 30 Cüz\nArapça & Meâl"),
          emoji = "📖",
          onClick = onOpenQuran,
          modifier = Modifier.weight(1f)
        )
        HubCard(
          title = t("Kıble Bulucu"),
          subtitle = t("Pusula Sensörü\nKâbe İstikameti"),
          emoji = "🧭",
          onClick = onOpenQibla,
          modifier = Modifier.weight(1f)
        )
      }
    }

    // Grid row 2: Camiler & Oruç Takibi
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        HubCard(
          title = t("Yakındaki Camiler"),
          subtitle = "${prefs.province} / ${prefs.district}\nHarita & Navigasyon",
          emoji = "🕌",
          onClick = onOpenMosques,
          modifier = Modifier.weight(1f)
        )
        HubCard(
          title = t("Oruç Takibi"),
          subtitle = t("Bugünkü Durum\nKaza & Nafile Sayacı"),
          emoji = "🤲",
          onClick = onOpenFastingTracker,
          modifier = Modifier.weight(1f)
        )
      }
    }

    // Radyo & TV
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        HubCard(
          title = t("Radyo"),
          subtitle = t("Diyanet • Kur'an\nRisalet (resmî)"),
          emoji = "📻",
          onClick = onOpenRadioTv,
          modifier = Modifier.weight(1f)
        )
        HubCard(
          title = t("Kâbe TV"),
          subtitle = t("Mescid-i Haram\nCanlı Yayın"),
          emoji = "🕋",
          onClick = onOpenRadioTv,
          modifier = Modifier.weight(1f)
        )
      }
    }

    // Grid row 3: Manevi Notlar & Ayarlar
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        HubCard(
          title = t("Manevi Notlar"),
          subtitle = t("Kişisel Dualar\nRamazan Günlüğü"),
          emoji = "📝",
          onClick = onOpenNotes,
          modifier = Modifier.weight(1f)
        )
        HubCard(
          title = t("Uygulama Ayarları"),
          subtitle = t("Tema, Dil\nBildirim & Hatırlatıcı"),
          emoji = "⚙️",
          onClick = onOpenSettings,
          modifier = Modifier.weight(1f)
        )
      }
    }

    // Google AdMob Banner
    item {
      AdBannerComponent()
    }

    // Footer
    item {
      PrivacyFooter()
    }
  }
}

@Composable
private fun HubCard(
  title: String,
  subtitle: String,
  emoji: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .clickable { onClick() }
      .testTag("hub_card_${title.lowercase()}"),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      Box(
        modifier = Modifier
          .size(44.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
      ) {
        Text(emoji, fontSize = 22.sp)
      }
      Spacer(modifier = Modifier.height(12.dp))
      Text(
        text = title,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = subtitle,
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        lineHeight = 15.sp
      )
    }
  }
}
