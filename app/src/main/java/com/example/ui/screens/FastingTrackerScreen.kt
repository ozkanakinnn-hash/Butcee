package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppPreferences
import com.example.ui.components.AdBannerComponent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FastingTrackerScreen(
  prefs: AppPreferences,
  onNavigateBack: () -> Unit,
  onOpenCalendar: () -> Unit,
  modifier: Modifier = Modifier
) {
  BackHandler { onNavigateBack() }
  val context = LocalContext.current

  val todayKey = remember {
    SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(Date())
  }
  val todayFormatted = remember {
    SimpleDateFormat("dd MMMM yyyy, EEEE", Locale("tr", "TR")).format(Date())
  }

  var todayStatus by remember { mutableStateOf(prefs.getFastingStatus(todayKey)) }
  var kazaCount by remember { mutableIntStateOf(prefs.kazaDebtCount) }
  var nafileCount by remember { mutableIntStateOf(prefs.nafileCount) }
  var reminderEnabled by remember { mutableStateOf(prefs.kazaReminderEnabled) }

  val totalFastedDays = remember(todayStatus) {
    prefs.getFastingDates().size
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("fasting_tracker_screen")
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
            text = "Oruç Takibi & Sayaç",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
          )
          Text(
            text = "Günlük oruç niyetleri, kaza ve nafile sayaçları",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }

    // 1. TODAY'S FASTING ACTION CARD
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Bugün Oruç Durumu",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = todayFormatted,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(
                  when (todayStatus) {
                    "fasted" -> Color(0xFF2D5A43)
                    "broken" -> Color(0xFFC77700)
                    "unfasted" -> Color(0xFF8A3B2B)
                    else -> MaterialTheme.colorScheme.surfaceVariant
                  }
                )
                .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
              Text(
                text = when (todayStatus) {
                  "fasted" -> "Oruçlusunuz 🟢"
                  "broken" -> "Bozuldu 🟡"
                  "unfasted" -> "Tutulmadı 🔴"
                  else -> "Belirlenmedi"
                },
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (todayStatus != null) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))
          Text(
            text = "Bugünkü oruç durumunuzu belirlemek için aşağıdaki butonlara dokunun:",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(10.dp))

          // 3 Action Buttons requested by user
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            // Button 1: Oruç Tuttum
            Button(
              onClick = {
                prefs.setFastingStatus(todayKey, "fasted")
                todayStatus = "fasted"
                Toast.makeText(context, "Bugün oruçlu olarak kaydedildi! Allah kabul eylesin.", Toast.LENGTH_SHORT).show()
              },
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = if (todayStatus == "fasted") Color(0xFF2D5A43) else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (todayStatus == "fasted") Color.White else MaterialTheme.colorScheme.onSurface
              )
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🟢", fontSize = 16.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text("Oruç Tuttum", fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
              }
            }

            // Button 2: Tutamadım
            Button(
              onClick = {
                prefs.setFastingStatus(todayKey, "unfasted")
                todayStatus = "unfasted"
                kazaCount++
                prefs.kazaDebtCount = kazaCount
                Toast.makeText(context, "Tutulmadı olarak işaretlendi (1 kaza eklendi).", Toast.LENGTH_SHORT).show()
              },
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = if (todayStatus == "unfasted") Color(0xFF8A3B2B) else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (todayStatus == "unfasted") Color.White else MaterialTheme.colorScheme.onSurface
              )
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🔴", fontSize = 16.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text("Tutamadım", fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
              }
            }

            // Button 3: Niyetliydim Ama Bozuldu
            Button(
              onClick = {
                prefs.setFastingStatus(todayKey, "broken")
                todayStatus = "broken"
                kazaCount++
                prefs.kazaDebtCount = kazaCount
                Toast.makeText(context, "Niyetliydim ama bozuldu olarak kaydedildi (1 kaza eklendi).", Toast.LENGTH_SHORT).show()
              },
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = if (todayStatus == "broken") Color(0xFFC77700) else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (todayStatus == "broken") Color.White else MaterialTheme.colorScheme.onSurface
              )
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🟡", fontSize = 16.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text("Niyetliydim Bozuldu", fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
              }
            }
          }
        }
      }
    }

    // 2. COUNTERS: Kaza & Nafile Oruç Sayaçları
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Kaza Orucu Sayacı
        Card(
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text("Kaza Borcu", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "$kazaCount Gün",
              fontSize = 22.sp,
              fontWeight = FontWeight.Bold,
              color = if (kazaCount > 0) Color(0xFF8A3B2B) else Color(0xFF2D5A43)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              IconButton(
                onClick = {
                  if (kazaCount > 0) {
                    kazaCount--
                    prefs.kazaDebtCount = kazaCount
                  }
                },
                modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
              ) {
                Icon(imageVector = Icons.Default.Remove, contentDescription = "Azalt", modifier = Modifier.size(16.dp))
              }
              IconButton(
                onClick = {
                  kazaCount++
                  prefs.kazaDebtCount = kazaCount
                },
                modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
              ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Arttır", modifier = Modifier.size(16.dp))
              }
            }
          }
        }

        // Nafile Orucu Sayacı
        Card(
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text("Nafile Orucu", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "$nafileCount Gün",
              fontSize = 22.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              IconButton(
                onClick = {
                  if (nafileCount > 0) {
                    nafileCount--
                    prefs.nafileCount = nafileCount
                  }
                },
                modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
              ) {
                Icon(imageVector = Icons.Default.Remove, contentDescription = "Azalt", modifier = Modifier.size(16.dp))
              }
              IconButton(
                onClick = {
                  nafileCount++
                  prefs.nafileCount = nafileCount
                },
                modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
              ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Arttır", modifier = Modifier.size(16.dp))
              }
            }
          }
        }
      }
    }

    // 3. Kaza Borcu Hatırlatıcısı Card
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.NotificationsActive,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text("Kaza Borcu Hatırlatıcısı", fontWeight = FontWeight.Bold, fontSize = 13.sp)
              Text("Pazartesi & Perşembe günleri nafile ve kaza orucu için nazik hatırlatma", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
          Switch(
            checked = reminderEnabled,
            onCheckedChange = {
              reminderEnabled = it
              prefs.kazaReminderEnabled = it
              Toast.makeText(context, if (it) "Kaza hatırlatıcısı aktif" else "Kapatıldı", Toast.LENGTH_SHORT).show()
            }
          )
        }
      }
    }

    // 4. Calendar Shortcut Button
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenCalendar() }
            .padding(16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text("📅 Tam Oruç Takvimini Görüntüle", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
            Text("Bu yıl toplam $totalFastedDays gün oruç tuttunuz. Dini günleri inceleyin.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
          Text("Aç ›", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
      }
    }

    item {
      AdBannerComponent()
    }
  }
}
