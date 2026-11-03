package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppPreferences
import com.example.ui.components.AdBannerComponent

data class MosqueItem(
  val name: String,
  val address: String,
  val distanceStr: String,
  val features: String
)

@Composable
fun NearbyMosquesScreen(
  prefs: AppPreferences,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  BackHandler { onNavigateBack() }
  val context = LocalContext.current

  val mosques = remember(prefs.province, prefs.district) {
    getMosquesForLocation(prefs.province, prefs.district)
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("nearby_mosques_screen")
  ) {
    // Header
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(start = 8.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(onClick = onNavigateBack) {
          Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
        }
        Spacer(modifier = Modifier.width(4.dp))
        Column {
          Text(
            text = "Yakınımdaki Camiler",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
          )
          Text(
            text = "${prefs.province} / ${prefs.district} civarındaki camiler ve mescitler",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }

    // Google Maps Quick Action Banner
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
              contentAlignment = Alignment.Center
            ) {
              Icon(imageVector = Icons.Default.Map, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "Canlı Harita & Navigasyon",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer
              )
              Text(
                text = "Konumunuza en yakın camileri Google Haritalar üzerinde açın",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          Button(
            onClick = {
              try {
                val query = Uri.encode("cami ${prefs.province} ${prefs.district}")
                val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=$query")).apply {
                  setPackage("com.google.android.apps.maps")
                }
                if (mapIntent.resolveActivity(context.packageManager) != null) {
                  context.startActivity(mapIntent)
                } else {
                  val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/$query"))
                  context.startActivity(webIntent)
                }
              } catch (e: Exception) {
                Toast.makeText(context, "Harita açılamadı", Toast.LENGTH_SHORT).show()
              }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(imageVector = Icons.Default.NearMe, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("🗺️ Google Haritalar'da Tüm Camileri Gör")
          }
        }
      }
    }

    // List header
    item {
      Text(
        text = "Öne Çıkan Çevre Camileri",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 6.dp)
      )
    }

    // Mosque list items
    items(mosques) { mosque ->
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
          ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
              ) {
                Text("🕌", fontSize = 18.sp)
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = mosque.name,
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = mosque.address,
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.DirectionsWalk,
                  contentDescription = null,
                  modifier = Modifier.size(12.dp),
                  tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                  text = mosque.distanceStr,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = "Özellikler: ${mosque.features}",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(12.dp))
          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
          Spacer(modifier = Modifier.height(8.dp))

          // Direction Button
          OutlinedButton(
            onClick = {
              try {
                val q = Uri.encode("${mosque.name} ${prefs.province}")
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=$q"))
                context.startActivity(intent)
              } catch (e: Exception) {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/${Uri.encode(mosque.name)}"))
                context.startActivity(webIntent)
              }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
          ) {
            Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Yol Tarifi Al (Haritada Göster)", fontSize = 12.sp)
          }
        }
      }
    }

    item {
      AdBannerComponent()
    }
  }
}

private fun getMosquesForLocation(province: String, district: String): List<MosqueItem> {
  return when (district.lowercase()) {
    "saruhanlı" -> listOf(
      MosqueItem("Saruhanlı Merkez Çarşı Camii", "Cumhuriyet Mah. Çarşı Meydanı, Saruhanlı", "280 m", "Şadırvan, Kadınlar Mescidi, Klimalı"),
      MosqueItem("Saruhanlı Hacı Rahmanlı Camii", "Hacı Rahmanlı Mah. Atatürk Cad., Saruhanlı", "1.1 km", "Geniş Avlu, Şadırvan, Otopark"),
      MosqueItem("Saruhanlı İstasyon Camii", "İstasyon Mah. Demiryolu Cad., Saruhanlı", "850 m", "Tarihi Mimari, Şadırvan"),
      MosqueItem("Saruhanlı Cengiz Topel Camii", "Cengiz Topel Mah. Park Sok., Saruhanlı", "1.4 km", "Kur'an Kursu, Geniş Cemaat Alanı"),
      MosqueItem("Koldere Köyü Camii", "Koldere Mah., Saruhanlı", "4.2 km", "Köy İçi, Sakin İbadet Alanı")
    )
    "şehzadeler" -> listOf(
      MosqueItem("Manisa Hatuniye Camii", "Hükümet Konağı Arkası, Şehzadeler", "320 m", "Tarihi Sultan II. Bayezid Dönemi"),
      MosqueItem("Tarihi Ulu Camii", "Spil Dağı Eteği, Şehzadeler", "1.1 km", "1376 Saruhanoğulları Yadigarı"),
      MosqueItem("Sultan Camii (Mesir Camii)", "Sultan Mah., Şehzadeler", "750 m", "Hafsa Sultan Külliyesi, Tarihi Çeşme")
    )
    "yunusemre" -> listOf(
      MosqueItem("Muradiye Camii", "Muradiye Mah., Yunusemre", "500 m", "Mimar Sinan Eseri, Çini İşlemeli"),
      MosqueItem("Yunusemre Yeni Camii", "Güzelyurt Mah., Yunusemre", "900 m", "Modern Mimari, Geniş Otopark")
    )
    else -> listOf(
      MosqueItem("$district Merkez Camii", "$district Çarşı Meydanı, $province", "350 m", "Şadırvan, Klimalı, Engelli Rampası"),
      MosqueItem("$district Fatih Camii", "$district Fatih Mah., $province", "720 m", "Geniş Cemaat Kapasitesi, Kur'an Kursu"),
      MosqueItem("$district Yeni Camii", "$district Yeni Mah., $province", "1.1 km", "Modern Tasarım, Çocuk Oyun Parkı Yanı"),
      MosqueItem("$district Sanayi Camii", "$district Sanayi Sitesi, $province", "1.8 km", "Geniş Otopark, Şadırvan")
    )
  }
}
