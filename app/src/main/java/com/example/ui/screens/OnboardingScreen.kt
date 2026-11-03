package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.core.content.ContextCompat
import com.example.data.AppPreferences
import com.example.data.IftarSoundChoice
import com.example.ui.dialogs.LocationSelectorDialog

@Composable
fun OnboardingScreen(
  prefs: AppPreferences,
  onComplete: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current

  var step by remember { mutableIntStateOf(1) } // 1: Location, 2: Name, 3: Notifications & Sound
  var showLocationPicker by remember { mutableStateOf(false) }

  var country by remember { mutableStateOf(prefs.country) }
  var city by remember { mutableStateOf(prefs.province) }
  var district by remember { mutableStateOf(prefs.district) }

  var enteredName by remember { mutableStateOf(prefs.userName) }
  var notificationsEnabled by remember { mutableStateOf(false) }
  var selectedSound by remember { mutableStateOf(prefs.iftarSound) }

  // Permission launcher for location
  val locationPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
  ) { permissions ->
    val granted = (permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false) ||
      (permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false)
    if (granted) {
      Toast.makeText(context, "Konum izni verildi!", Toast.LENGTH_SHORT).show()
    }
  }

  // Permission launcher for notifications
  val notificationPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { granted ->
    notificationsEnabled = granted
    prefs.notificationsEnabled = granted
    if (granted) {
      Toast.makeText(context, "Bildirimler aktif edildi!", Toast.LENGTH_SHORT).show()
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("onboarding_screen")
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(24.dp)
        .verticalScroll(rememberScrollState()),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Spacer(modifier = Modifier.height(24.dp))

      // Header App Logo & Title
      Text(
        text = "🌙",
        fontSize = 44.sp
      )
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = "Ramazan Rehberi'ne Hoş Geldiniz",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = TextAlign.Center
      )
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = "Hassas vakitler ve kişiselleştirilmiş bir deneyim için birkaç adımı tamamlayalım.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(20.dp))

      // Step indicator (1 - 2 - 3)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
      ) {
        StepIndicator(num = 1, title = "Konum", isActive = step == 1, isDone = step > 1)
        StepLine(isDone = step > 1)
        StepIndicator(num = 2, title = "Profil", isActive = step == 2, isDone = step > 2)
        StepLine(isDone = step > 2)
        StepIndicator(num = 3, title = "Bildirim", isActive = step == 3, isDone = step > 3)
      }

      Spacer(modifier = Modifier.height(28.dp))

      // Step Contents
      when (step) {
        1 -> {
          // STEP 1: LOCATION
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
          ) {
            Column(modifier = Modifier.padding(20.dp)) {
              Text(
                text = "1. Adım: Konumunuzu Belirleyin",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Diyanet İşleri Başkanlığı'nın desteklediği tüm dünya şehirlerinden vakitler hesaplanır.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )

              Spacer(modifier = Modifier.height(16.dp))

              // Selected Location Display Card
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(12.dp))
                  .background(MaterialTheme.colorScheme.primaryContainer)
                  .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                  Spacer(modifier = Modifier.width(8.dp))
                  Column {
                    Text(text = "$country, $city", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(text = "İlçe / Bölge: $district", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                  }
                }
              }

              Spacer(modifier = Modifier.height(14.dp))

              // Manual Location Picker Button
              Button(
                onClick = { showLocationPicker = true },
                modifier = Modifier.fillMaxWidth().testTag("onboarding_choose_location_button"),
                shape = RoundedCornerShape(12.dp)
              ) {
                Icon(imageVector = Icons.Default.Language, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Şehir / Ülke Listesinden Seç")
              }

              Spacer(modifier = Modifier.height(8.dp))

              // Optional GPS Permission Button
              OutlinedButton(
                onClick = {
                  locationPermissionLauncher.launch(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                  )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
              ) {
                Icon(imageVector = Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cihaz Konumunu Kullan (İsteğe Bağlı GPS)", fontSize = 12.sp)
              }
            }
          }

          Spacer(modifier = Modifier.height(20.dp))

          Button(
            onClick = {
              prefs.country = country
              prefs.province = city
              prefs.district = district
              step = 2
            },
            modifier = Modifier.fillMaxWidth().height(50.dp).testTag("onboarding_step1_next_button"),
            shape = RoundedCornerShape(14.dp)
          ) {
            Text("Devam Et (İsim Belirleme)")
            Spacer(modifier = Modifier.width(6.dp))
            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
          }
        }

        2 -> {
          // STEP 2: NAME ENTRY
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
          ) {
            Column(modifier = Modifier.padding(20.dp)) {
              Text(
                text = "2. Adım: Size Nasıl Hitap Edelim?",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Ana sayfada ve selamlamalarda görünecek isminizi yazabilirsiniz.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )

              Spacer(modifier = Modifier.height(18.dp))

              OutlinedTextField(
                value = enteredName,
                onValueChange = { enteredName = it },
                label = { Text("İsminiz (Örn: Ahmet, Ayşe...)") },
                leadingIcon = {
                  Icon(imageVector = Icons.Default.Person, contentDescription = null)
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("onboarding_name_input"),
                shape = RoundedCornerShape(12.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(20.dp))

          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
              onClick = { step = 1 },
              modifier = Modifier.weight(1f).height(50.dp),
              shape = RoundedCornerShape(14.dp)
            ) {
              Text("Geri")
            }

            Button(
              onClick = {
                prefs.userName = enteredName.trim()
                step = 3
              },
              modifier = Modifier.weight(2f).height(50.dp).testTag("onboarding_step2_next_button"),
              shape = RoundedCornerShape(14.dp)
            ) {
              Text("Devam Et (Bildirimler)")
              Spacer(modifier = Modifier.width(6.dp))
              Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
            }
          }
        }

        3 -> {
          // STEP 3: NOTIFICATIONS & SOUND SELECTION
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
          ) {
            Column(modifier = Modifier.padding(20.dp)) {
              Text(
                text = "3. Adım: Bildirimler & İftar Sesi",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "İftar vakitlerini ve sahur hatırlatıcılarını kaçırmayın.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )

              Spacer(modifier = Modifier.height(16.dp))

              // Notification Switch
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(12.dp))
                  .background(MaterialTheme.colorScheme.surfaceVariant)
                  .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(imageVector = Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                  Spacer(modifier = Modifier.width(10.dp))
                  Column {
                    Text(text = "Ezan & İftar Bildirimleri", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(text = if (notificationsEnabled) "Açık" else "İsteğe bağlı", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                  }
                }
                Switch(
                  checked = notificationsEnabled,
                  onCheckedChange = { isChecked ->
                    if (isChecked && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                      notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                      notificationsEnabled = isChecked
                      prefs.notificationsEnabled = isChecked
                    }
                  },
                  modifier = Modifier.testTag("onboarding_notif_switch")
                )
              }

              Spacer(modifier = Modifier.height(16.dp))

              Text(
                text = "İftar Bildirim Sesi Tercihiniz:",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.primary
              )
              Spacer(modifier = Modifier.height(8.dp))

              // 3 Sound Choices
              IftarSoundChoice.values().forEach { sound ->
                val isSelected = selectedSound == sound
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                    .clickable {
                      selectedSound = sound
                      prefs.iftarSound = sound
                    }
                    .padding(10.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Box(
                    modifier = Modifier
                      .size(18.dp)
                      .clip(CircleShape)
                      .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                  )
                  Spacer(modifier = Modifier.width(10.dp))
                  Column {
                    Text(text = sound.title, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
                    Text(text = sound.subtitle, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                  }
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(20.dp))

          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
              onClick = { step = 2 },
              modifier = Modifier.weight(1f).height(50.dp),
              shape = RoundedCornerShape(14.dp)
            ) {
              Text("Geri")
            }

            Button(
              onClick = {
                prefs.country = country
                prefs.province = city
                prefs.district = district
                prefs.userName = enteredName.trim()
                prefs.notificationsEnabled = notificationsEnabled
                prefs.iftarSound = selectedSound
                prefs.isOnboardingCompleted = true
                onComplete()
              },
              modifier = Modifier.weight(2f).height(50.dp).testTag("onboarding_finish_button"),
              shape = RoundedCornerShape(14.dp),
              colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
              Text("✨ Başlayın", fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }

  // Location selector dialog when clicked
  if (showLocationPicker) {
    LocationSelectorDialog(
      currentCountry = country,
      currentCity = city,
      currentDistrict = district,
      onLocationSelected = { newCountry, newCity, newDistrict ->
        country = newCountry
        city = newCity
        district = newDistrict
        prefs.country = newCountry
        prefs.province = newCity
        prefs.district = newDistrict
        showLocationPicker = false
      },
      onDismiss = { showLocationPicker = false }
    )
  }
}

@Composable
private fun StepIndicator(num: Int, title: String, isActive: Boolean, isDone: Boolean) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Box(
      modifier = Modifier
        .size(30.dp)
        .clip(CircleShape)
        .background(
          when {
            isDone -> MaterialTheme.colorScheme.primary
            isActive -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.surfaceVariant
          }
        ),
      contentAlignment = Alignment.Center
    ) {
      if (isDone) {
        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
      } else {
        Text(text = "$num", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
      }
    }
    Spacer(modifier = Modifier.height(4.dp))
    Text(text = title, fontSize = 11.sp, fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal, color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
  }
}

@Composable
private fun StepLine(isDone: Boolean) {
  Box(
    modifier = Modifier
      .width(40.dp)
      .height(2.dp)
      .padding(horizontal = 6.dp)
      .background(if (isDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
  )
}
