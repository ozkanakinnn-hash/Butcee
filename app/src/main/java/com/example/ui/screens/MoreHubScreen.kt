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
          text = "Dahası & Faydalı Araçlar",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onBackground
        )
        Text(
          text = "Kur'an-ı Kerim, Kıble bulucu, camiler ve zengin İslami modüller",
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
          title = "Kur'an-ı Kerîm",
          subtitle = "114 Sure • 30 Cüz\nArapça & Meâl",
          emoji = "📖",
          onClick = onOpenQuran,
          modifier = Modifier.weight(1f)
        )
        HubCard(
          title = "Kıble Bulucu",
          subtitle = "Pusula Sensörü\nKâbe İstikameti",
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
          title = "Yakındaki Camiler",
          subtitle = "${prefs.province} / ${prefs.district}\nHarita & Navigasyon",
          emoji = "🕌",
          onClick = onOpenMosques,
          modifier = Modifier.weight(1f)
        )
        HubCard(
          title = "Oruç Takibi",
          subtitle = "Bugünkü Durum\nKaza & Nafile Sayacı",
          emoji = "🤲",
          onClick = onOpenFastingTracker,
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
          title = "Manevi Notlar",
          subtitle = "Kişisel Dualar\nRamazan Günlüğü",
          emoji = "📝",
          onClick = onOpenNotes,
          modifier = Modifier.weight(1f)
        )
        HubCard(
          title = "Uygulama Ayarları",
          subtitle = "Top & Ney Sesi\nDakika Barları & Vakit",
          emoji = "⚙️",
          onClick = onOpenSettings,
          modifier = Modifier.weight(1f)
        )
      }
    }

    // Direct Notification Test Card
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp)
          .clickable { onOpenSettings() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f))
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
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
              contentAlignment = Alignment.Center
            ) {
              Text("🔔", fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "Bildirim Test Paneli",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer
              )
              Text(
                text = "Ramazan topu ve Ney sesli bildirimleri hemen test edin",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
              )
            }
          }
          Text("Test Et ›", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
        }
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
