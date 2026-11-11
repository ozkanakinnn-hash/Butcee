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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.border
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
  isRamadan: Boolean = false,
  onOpenFastingGuide: () -> Unit = {},
  onOpenGreetingCards: () -> Unit = {},
  onOpenHatim: () -> Unit = {},
  onOpenKaza: () -> Unit = {},
  onOpenKandil: () -> Unit = {},
  onOpenWizard: () -> Unit = {},
  onOpenPremium: () -> Unit = {},
  onAddWidget: () -> Unit = {},
  onOpenQibla: () -> Unit,
  onOpenMosques: () -> Unit,
  onOpenFastingTracker: () -> Unit,
  onOpenNotes: () -> Unit,
  onOpenSettings: () -> Unit,
  onOpenZikirmatik: () -> Unit = {},
  onOpenEsma: () -> Unit = {},
  onOpenCalculators: () -> Unit = {},
  onOpenImsakiye: () -> Unit = {},
  onOpenBackup: () -> Unit = {},
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
        // Ramazan dışında Kur'an alt sekmede; burada Oruç Rehberi gösterilir
        if (isRamadan) HubCard(
          title = t("Kur'an-ı Kerîm"),
          subtitle = t("114 Sure • 30 Cüz\nArapça & Meâl"),
          iconRes = R.drawable.ic_hub_quran,
          onClick = onOpenQuran,
          modifier = Modifier.weight(1f)
        ) else HubCard(
          title = t("Ramazan Rehberi"),
          subtitle = t("Orucun Faydaları\nİftar & Sahur"),
          iconRes = R.drawable.ic_hub_ramadan,
          onClick = onOpenFastingGuide,
          modifier = Modifier.weight(1f)
        )
        HubCard(
          title = t("Kıble Bulucu"),
          subtitle = t("Pusula Sensörü\nKâbe İstikameti"),
          iconRes = R.drawable.ic_hub_qibla,
          onClick = onOpenQibla,
          modifier = Modifier.weight(1f)
        )
      }
    }

    // Premium (reklamsız) kartı
    item {
      val isPremium by com.example.billing.PremiumManager.isPremium.collectAsState()
      val cs = MaterialTheme.colorScheme
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp)
          .clip(RoundedCornerShape(20.dp))
          .background(androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(cs.primaryContainer, cs.surface)))
          .border(1.dp, cs.primary.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
          .clickable { onOpenPremium() }
          .padding(16.dp)
          .testTag("hub_premium"),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("⭐", fontSize = 26.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text(
            if (isPremium) t("Premium etkin") else t("Reklamsız Premium'a geç"),
            fontWeight = FontWeight.Bold, fontSize = 16.sp, color = cs.onSurface
          )
          Text(
            if (isPremium) t("Desteğiniz için teşekkürler") else t("Reklamları kaldır, uygulamaya destek ol"),
            fontSize = 12.sp, color = cs.onSurfaceVariant
          )
        }
        Text("›", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = cs.primary)
      }
    }

    // Kaza namazı • Kandil duaları • Hatim takibi • Bildirim sihirbazı
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        HubCard(
          title = t("Kaza Namazı"),
          subtitle = t("Borç hesapla\nKıldıkça işaretle"),
          iconRes = R.drawable.ic_hub_kaza,
          onClick = onOpenKaza,
          modifier = Modifier.weight(1f)
        )
        HubCard(
          title = t("Kandil Duaları"),
          subtitle = t("Mübarek geceler\nDua & ibadetler"),
          iconRes = R.drawable.ic_hub_kandil,
          onClick = onOpenKandil,
          modifier = Modifier.weight(1f)
        )
      }
    }
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        HubCard(
          title = t("Hatim Takibi"),
          subtitle = t("Cüz & Sure\nİlerleme"),
          iconRes = R.drawable.ic_hub_hatim,
          onClick = onOpenHatim,
          modifier = Modifier.weight(1f)
        )
        HubCard(
          title = t("Bildirim Sihirbazı"),
          subtitle = t("Ezan gelmiyor mu?\nAyarla & test et"),
          iconRes = R.drawable.ic_hub_bell,
          onClick = onOpenWizard,
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
          iconRes = R.drawable.ic_hub_mosque,
          onClick = onOpenMosques,
          modifier = Modifier.weight(1f)
        )
        HubCard(
          title = t("Oruç Takibi"),
          subtitle = t("Bugünkü Durum\nKaza & Nafile Sayacı"),
          iconRes = R.drawable.ic_hub_fasting,
          onClick = onOpenFastingTracker,
          modifier = Modifier.weight(1f)
        )
      }
    }

    // Zikirmatik & Esmâü'l-Hüsnâ
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        HubCard(
          title = t("Zikirmatik"),
          subtitle = t("Serbest • 33 • 99\nTesbihat"),
          iconRes = R.drawable.ic_hub_tasbih,
          onClick = onOpenZikirmatik,
          modifier = Modifier.weight(1f)
        )
        HubCard(
          title = t("Esmâü'l-Hüsnâ"),
          subtitle = t("Allah'ın 99 ismi\nAnlamlarıyla"),
          iconRes = R.drawable.ic_hub_esma,
          onClick = onOpenEsma,
          modifier = Modifier.weight(1f)
        )
      }
    }

    // İmsakiye & Hesaplayıcılar
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        HubCard(
          title = t("İmsakiye"),
          subtitle = t("30 günlük vakitler\nPDF olarak paylaş"),
          iconRes = R.drawable.ic_hub_imsakiye,
          onClick = onOpenImsakiye,
          modifier = Modifier.weight(1f)
        )
        HubCard(
          title = t("Fitre • Zekât"),
          subtitle = t("Fitre, fidye ve\nzekât hesaplayıcı"),
          iconRes = R.drawable.ic_hub_calc,
          onClick = onOpenCalculators,
          modifier = Modifier.weight(1f)
        )
      }
    }

    // Özel Gün Kartları & Widget
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        HubCard(
          title = t("Özel Gün Kartları"),
          subtitle = t("Cuma • Kandil • Bayram\nSevdiklerinizle paylaşın"),
          iconRes = R.drawable.ic_hub_card,
          onClick = onOpenGreetingCards,
          modifier = Modifier.weight(1f)
        )
        HubCard(
          title = t("Ana Ekran Widget'ı"),
          subtitle = t("Namaz Vakitleri\nManevi İklim"),
          iconRes = R.drawable.ic_hub_widget,
          onClick = onAddWidget,
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
          iconRes = R.drawable.ic_hub_notes,
          onClick = onOpenNotes,
          modifier = Modifier.weight(1f)
        )
        HubCard(
          title = t("Uygulama Ayarları"),
          subtitle = t("Tema, Dil\nBildirim & Hatırlatıcı"),
          iconRes = R.drawable.ic_hub_settings,
          onClick = onOpenSettings,
          modifier = Modifier.weight(1f)
        )
      }
    }

    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        HubCard(
          title = t("Yedekle & Geri Yükle"),
          subtitle = t("Zikir, kaza, hatim\nve notlarınızı koruyun"),
          iconRes = R.drawable.ic_hub_backup,
          onClick = onOpenBackup,
          modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.weight(1f))
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
  iconRes: Int,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val cs = MaterialTheme.colorScheme
  Card(
    modifier = modifier
      .clickable { onClick() }
      .testTag("hub_card_${title.lowercase()}"),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = cs.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 16.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Sekiz köşeli yıldız madalyon içinde tema renginde çizgi ikon
      Box(modifier = Modifier.size(58.dp), contentAlignment = Alignment.Center) {
        val fill = cs.primaryContainer
        val line = cs.primary.copy(alpha = 0.55f)
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
          val c = center
          val r = size.minDimension / 2f - 1.dp.toPx()
          val path = androidx.compose.ui.graphics.Path().apply {
            for (k in 0 until 16) {
              val a = Math.PI / 8 * k - Math.PI / 2
              val rr = if (k % 2 == 0) r else r * 0.82f
              val x = c.x + rr * kotlin.math.cos(a).toFloat()
              val y = c.y + rr * kotlin.math.sin(a).toFloat()
              if (k == 0) moveTo(x, y) else lineTo(x, y)
            }
            close()
          }
          drawPath(path, fill)
          drawPath(path, line, style = androidx.compose.ui.graphics.drawscope.Stroke(1.dp.toPx()))
        }
        Icon(
          painter = painterResource(iconRes),
          contentDescription = null,
          tint = cs.primary,
          modifier = Modifier.size(28.dp)
        )
      }
      Spacer(modifier = Modifier.height(10.dp))
      Text(
        text = title,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        color = cs.onSurface,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        maxLines = 1
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = subtitle,
        fontSize = 11.sp,
        color = cs.onSurfaceVariant,
        lineHeight = 15.sp,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center
      )
    }
  }
}
