package com.example.ui.dialogs

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.AppPreferences
import com.example.data.AppColorTheme
import com.example.data.AppThemeMode
import com.example.data.IftarSoundChoice
import com.example.util.NotificationHelper
import com.example.util.SoundManager

@Composable
fun SettingsDialog(
  prefs: AppPreferences,
  onThemeChanged: (AppThemeMode) -> Unit,
  onColorThemeChanged: (AppColorTheme) -> Unit,
  onChangeLocation: () -> Unit,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current

  var userName by remember { mutableStateOf(prefs.userName) }
  var isEditingName by remember { mutableStateOf(false) }

  var currentTheme by remember { mutableStateOf(prefs.themeMode) }
  var currentColorTheme by remember { mutableStateOf(prefs.colorTheme) }
  var notificationsEnabled by remember { mutableStateOf(prefs.notificationsEnabled) }

  // 5 Vakit Namaz Onayları
  var notifyImsak by remember { mutableStateOf(prefs.notifyImsak) }
  var notifyOgle by remember { mutableStateOf(prefs.notifyOgle) }
  var notifyIkindi by remember { mutableStateOf(prefs.notifyIkindi) }
  var notifyIftar by remember { mutableStateOf(prefs.notifyIftar) }
  var notifyYatsi by remember { mutableStateOf(prefs.notifyYatsi) }

  // Vaktinden Önce Hatırlatıcılar & Dinamik Dakika Barları
  var remindBeforeImsak by remember { mutableStateOf(prefs.remindBeforeImsak) }
  var remindBeforeImsakMin by remember { mutableIntStateOf(prefs.remindBeforeImsakMinutes) }

  var remindBeforeOgle by remember { mutableStateOf(prefs.remindBeforeOgle) }
  var remindBeforeOgleMin by remember { mutableIntStateOf(prefs.remindBeforeOgleMinutes) }

  var remindBeforeIkindi by remember { mutableStateOf(prefs.remindBeforeIkindi) }
  var remindBeforeIkindiMin by remember { mutableIntStateOf(prefs.remindBeforeIkindiMinutes) }

  var remindBeforeIftar by remember { mutableStateOf(prefs.remindBeforeIftar) }
  var remindBeforeIftarMin by remember { mutableIntStateOf(prefs.remindBeforeIftarMinutes) }

  var remindBeforeYatsi by remember { mutableStateOf(prefs.remindBeforeYatsi) }
  var remindBeforeYatsiMin by remember { mutableIntStateOf(prefs.remindBeforeYatsiMinutes) }

  var selectedSound by remember { mutableStateOf(prefs.iftarSound) }
  var currentlyPlayingSound by remember { mutableStateOf<IftarSoundChoice?>(null) }

  DisposableEffect(Unit) {
    onDispose {
      SoundManager.stopSound()
    }
  }

  Dialog(
    onDismissRequest = {
      SoundManager.stopSound()
      onDismiss()
    },
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .fillMaxHeight(0.90f)
        .clip(RoundedCornerShape(24.dp))
        .testTag("settings_dialog"),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 6.dp
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp)
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Ayarlar & Özelleştirme",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          IconButton(
            onClick = {
              SoundManager.stopSound()
              onDismiss()
            }
          ) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Kapat")
          }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)

        // Content
        Column(
          modifier = Modifier
            .weight(1f)
            .verticalScroll(rememberScrollState())
        ) {
          // 1. Profile / Name Section
          SettingsSectionHeader(title = "Kullanıcı Profili")
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              if (isEditingName) {
                OutlinedTextField(
                  value = userName,
                  onValueChange = { userName = it },
                  label = { Text("Hitap Edilecek İsim") },
                  singleLine = true,
                  modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.align(Alignment.End)) {
                  Button(
                    onClick = {
                      prefs.userName = userName.trim()
                      isEditingName = false
                      Toast.makeText(context, "İsminiz güncellendi", Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(8.dp)
                  ) {
                    Text("Kaydet")
                  }
                }
              } else {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                      Text(
                        text = if (userName.isNotBlank()) userName else "İsim Belirlenmedi",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                      )
                      Text(
                        text = "Ana sayfa selamlama hitabı",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                      )
                    }
                  }
                  IconButton(onClick = { isEditingName = true }) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Düzenle", tint = MaterialTheme.colorScheme.primary)
                  }
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // 2. Location Setting
          SettingsSectionHeader(title = "Konum")
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onChangeLocation() },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(
                    text = "${prefs.province} / ${prefs.district}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                  )
                  Text(
                    text = "${prefs.country} (Diyanet Uyumlu Vakitler)",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
              Text("Değiştir ›", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // 3. VAKİT BİLDİRİMLERİ & DAKİKA BARLARI (Requirement 6)
          SettingsSectionHeader(title = "Namaz Vakitleri & Hatırlatıcı Barları")
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              // Master Switch
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text("Tüm Vakit Bildirimleri", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                  Text("Ezan ve iftar bildirimlerini aç / kapat", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                  checked = notificationsEnabled,
                  onCheckedChange = {
                    notificationsEnabled = it
                    prefs.notificationsEnabled = it
                  }
                )
              }

              if (notificationsEnabled) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant)

                // 1. İmsak / Sahur
                PrayerReminderItem(
                  title = "Sahur / İmsak Vakti",
                  isNotifyChecked = notifyImsak,
                  onNotifyChanged = { notifyImsak = it; prefs.notifyImsak = it },
                  isRemindBeforeChecked = remindBeforeImsak,
                  onRemindBeforeChanged = { remindBeforeImsak = it; prefs.remindBeforeImsak = it },
                  minutes = remindBeforeImsakMin,
                  onMinutesChanged = { remindBeforeImsakMin = it; prefs.remindBeforeImsakMinutes = it }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)

                // 2. Öğle
                PrayerReminderItem(
                  title = "Öğle Namazı",
                  isNotifyChecked = notifyOgle,
                  onNotifyChanged = { notifyOgle = it; prefs.notifyOgle = it },
                  isRemindBeforeChecked = remindBeforeOgle,
                  onRemindBeforeChanged = { remindBeforeOgle = it; prefs.remindBeforeOgle = it },
                  minutes = remindBeforeOgleMin,
                  onMinutesChanged = { remindBeforeOgleMin = it; prefs.remindBeforeOgleMinutes = it }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)

                // 3. İkindi
                PrayerReminderItem(
                  title = "İkindi Namazı",
                  isNotifyChecked = notifyIkindi,
                  onNotifyChanged = { notifyIkindi = it; prefs.notifyIkindi = it },
                  isRemindBeforeChecked = remindBeforeIkindi,
                  onRemindBeforeChanged = { remindBeforeIkindi = it; prefs.remindBeforeIkindi = it },
                  minutes = remindBeforeIkindiMin,
                  onMinutesChanged = { remindBeforeIkindiMin = it; prefs.remindBeforeIkindiMinutes = it }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)

                // 4. İftar / Akşam
                PrayerReminderItem(
                  title = "İftar / Akşam Ezanı",
                  isNotifyChecked = notifyIftar,
                  onNotifyChanged = { notifyIftar = it; prefs.notifyIftar = it },
                  isRemindBeforeChecked = remindBeforeIftar,
                  onRemindBeforeChanged = { remindBeforeIftar = it; prefs.remindBeforeIftar = it },
                  minutes = remindBeforeIftarMin,
                  onMinutesChanged = { remindBeforeIftarMin = it; prefs.remindBeforeIftarMinutes = it }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)

                // 5. Yatsı / Teravih
                PrayerReminderItem(
                  title = "Yatsı / Teravih Vakti",
                  isNotifyChecked = notifyYatsi,
                  onNotifyChanged = { notifyYatsi = it; prefs.notifyYatsi = it },
                  isRemindBeforeChecked = remindBeforeYatsi,
                  onRemindBeforeChanged = { remindBeforeYatsi = it; prefs.remindBeforeYatsi = it },
                  minutes = remindBeforeYatsiMin,
                  onMinutesChanged = { remindBeforeYatsiMin = it; prefs.remindBeforeYatsiMinutes = it }
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // 4. ÖZEL İSLAMİ SESLER KÜTÜPHANESİ (Requirement 4)
          SettingsSectionHeader(title = "Özel İftar & Bildirim Sesleri")
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Text(
                text = "Sadeleştirilmiş iki özel ses (Ramazan Topu ve Ney):",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Spacer(modifier = Modifier.height(8.dp))

              IftarSoundChoice.entries.forEach { sound ->
                val isSelected = selectedSound == sound
                val isPlayingThis = currentlyPlayingSound == sound

                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                    .clickable {
                      selectedSound = sound
                      prefs.iftarSound = sound
                      Toast.makeText(context, "${sound.title} seçildi", Toast.LENGTH_SHORT).show()
                    }
                    .padding(10.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                      modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
                      contentAlignment = Alignment.Center
                    ) {
                      if (isSelected) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                      }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(sound.iconEmoji, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                      Text(text = sound.title, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
                      Text(text = sound.subtitle, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                  }

                  // Preview Listen Button
                  OutlinedButton(
                    onClick = {
                      if (isPlayingThis) {
                        SoundManager.stopSound()
                        currentlyPlayingSound = null
                      } else {
                        SoundManager.playSound(context, sound) {
                          currentlyPlayingSound = null
                        }
                        currentlyPlayingSound = sound
                      }
                    },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(34.dp)
                  ) {
                    Icon(
                      imageVector = if (isPlayingThis) Icons.Default.Stop else Icons.Default.PlayArrow,
                      contentDescription = null,
                      modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isPlayingThis) "Durdur" else "Dinle", fontSize = 11.sp)
                  }
                }
              }

              Spacer(modifier = Modifier.height(14.dp))
              HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.5.dp)
              Spacer(modifier = Modifier.height(10.dp))

              // BİLDİRİM TEST PANELİ
              Text(
                text = "🔔 BİLDİRİM TEST PANELİ",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Cihazınızda bildirim seslerini ve titreşimleri hemen test edin:",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Spacer(modifier = Modifier.height(8.dp))

              // Test 1: Ramazan Topu
              Button(
                onClick = {
                  NotificationHelper.sendNotification(
                    context = context,
                    title = "💥 Ramazan Topu Patladı • İftar Vakti!",
                    message = "${prefs.province} için iftar vakti girdi. Hayırlı iftarlar! Allah kabul etsin 🤲",
                    soundChoice = IftarSoundChoice.RAMAZAN_TOPU
                  )
                  Toast.makeText(context, "💥 Ramazan Topu bildirimi gönderildi!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
              ) {
                Text("💥 İftar Topu Bildirimini Test Et", fontSize = 12.sp)
              }

              Spacer(modifier = Modifier.height(6.dp))

              // Test 2: Ney Sesi Vakit Hatırlatıcısı
              OutlinedButton(
                onClick = {
                  NotificationHelper.sendNotification(
                    context = context,
                    title = "🕌 Vakit Hatırlatıcısı",
                    message = "Akşam namazı vakti girdi. Huzur ve sükûnetle namaza durabilirsiniz.",
                    soundChoice = IftarSoundChoice.SUFI_NEY
                  )
                  Toast.makeText(context, "🎶 Ney sesi vakit bildirimi gönderildi!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
              ) {
                Text("🎶 Ney Sesi Vakit Bildirimini Test Et", fontSize = 12.sp)
              }

              Spacer(modifier = Modifier.height(6.dp))

              // Test 3: 15 Dakika Önce Hatırlatıcı
              OutlinedButton(
                onClick = {
                  NotificationHelper.sendNotification(
                    context = context,
                    title = "⏳ Vaktinden Önce Hatırlatıcı (15 Dk)",
                    message = "İftar vaktine 15 dakika kaldı. Sofranız bereketli, dualarınız kabul olsun.",
                    soundChoice = IftarSoundChoice.SUFI_NEY
                  )
                  Toast.makeText(context, "⏳ 15 Dk Önce bildirimi test edildi!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
              ) {
                Text("⏳ 15 Dk Önce Hatırlatıcıyı Test Et", fontSize = 12.sp)
              }
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // 5. Theme Settings
          SettingsSectionHeader(title = "Görünüm & Tema")
          // 5a. Renk teması (3 seçenek)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            AppColorTheme.entries.forEach { theme ->
              ColorThemeCard(
                theme = theme,
                isSelected = currentColorTheme == theme,
                onClick = {
                  currentColorTheme = theme
                  prefs.colorTheme = theme
                  onColorThemeChanged(theme)
                },
                modifier = Modifier.weight(1f)
              )
            }
          }
          Spacer(modifier = Modifier.height(10.dp))
          // 5b. Aydınlık / Koyu / Sistem
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              ThemeOptionRow(
                title = "☀️ Aydınlık",
                isSelected = currentTheme == AppThemeMode.LIGHT,
                onClick = {
                  currentTheme = AppThemeMode.LIGHT
                  prefs.themeMode = AppThemeMode.LIGHT
                  onThemeChanged(AppThemeMode.LIGHT)
                }
              )
              ThemeOptionRow(
                title = "🌙 Koyu (Gece Siyahı)",
                isSelected = currentTheme == AppThemeMode.DARK,
                onClick = {
                  currentTheme = AppThemeMode.DARK
                  prefs.themeMode = AppThemeMode.DARK
                  onThemeChanged(AppThemeMode.DARK)
                }
              )
              ThemeOptionRow(
                title = "📱 Sistem Teması",
                isSelected = currentTheme == AppThemeMode.SYSTEM,
                onClick = {
                  currentTheme = AppThemeMode.SYSTEM
                  prefs.themeMode = AppThemeMode.SYSTEM
                  onThemeChanged(AppThemeMode.SYSTEM)
                }
              )
            }
          }

          Spacer(modifier = Modifier.height(18.dp))

          // Privacy note
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "🔒 Verileriniz yalnızca cihazınızda yerel olarak saklanmaktadır.",
              fontSize = 10.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }
  }
}

@Composable
private fun PrayerReminderItem(
  title: String,
  isNotifyChecked: Boolean,
  onNotifyChanged: (Boolean) -> Unit,
  isRemindBeforeChecked: Boolean,
  onRemindBeforeChanged: (Boolean) -> Unit,
  minutes: Int,
  onMinutesChanged: (Int) -> Unit
) {
  Column(modifier = Modifier.fillMaxWidth()) {
    // Exact time switch
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
      Switch(checked = isNotifyChecked, onCheckedChange = onNotifyChanged)
    }

    // Remind before toggle & Minute Slider Bar
    if (isNotifyChecked) {
      Spacer(modifier = Modifier.height(4.dp))
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(MaterialTheme.colorScheme.surface)
          .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Switch(
            checked = isRemindBeforeChecked,
            onCheckedChange = onRemindBeforeChanged,
            modifier = Modifier.size(36.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Vaktinden Önce Hatırlat",
            fontSize = 11.sp,
            color = if (isRemindBeforeChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        if (isRemindBeforeChecked) {
          // Dynamic Minute Bar [-] X dk [+]
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            IconButton(
              onClick = { if (minutes > 5) onMinutesChanged(minutes - 5) },
              modifier = Modifier.size(26.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
            ) {
              Icon(imageVector = Icons.Default.Remove, contentDescription = "Azalt", modifier = Modifier.size(12.dp))
            }

            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.primary)
                .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
              Text(
                text = "$minutes dk",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            }

            IconButton(
              onClick = { if (minutes < 60) onMinutesChanged(minutes + 5) },
              modifier = Modifier.size(26.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
            ) {
              Icon(imageVector = Icons.Default.Add, contentDescription = "Arttır", modifier = Modifier.size(12.dp))
            }
          }
        }
      }
    }
  }
}

@Composable
private fun SettingsSectionHeader(title: String) {
  Text(
    text = title,
    style = MaterialTheme.typography.labelLarge,
    fontWeight = FontWeight.Bold,
    color = MaterialTheme.colorScheme.primary,
    modifier = Modifier.padding(vertical = 4.dp)
  )
}

@Composable
private fun ThemeOptionRow(
  title: String,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(8.dp))
      .clickable { onClick() }
      .padding(vertical = 8.dp, horizontal = 4.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(text = title, fontSize = 13.sp)
    if (isSelected) {
      Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
    }
  }
}


@Composable
private fun ColorThemeCard(
  theme: AppColorTheme,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
  Column(
    modifier = modifier
      .clip(RoundedCornerShape(14.dp))
      .border(if (isSelected) 2.dp else 1.dp, borderColor, RoundedCornerShape(14.dp))
      .background(MaterialTheme.colorScheme.surfaceVariant)
      .clickable(onClick = onClick)
      .padding(vertical = 10.dp, horizontal = 6.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // Renk örnekleri: arka plan zemini üzerinde temanın vurgu renkleri
    Row(
      modifier = Modifier
        .clip(RoundedCornerShape(10.dp))
        .background(Color(theme.swatches.first()))
        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
        .padding(horizontal = 8.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      theme.swatches.drop(1).forEach { c ->
        Box(
          modifier = Modifier
            .size(14.dp)
            .clip(CircleShape)
            .background(Color(c))
        )
      }
    }
    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = (if (isSelected) "✓ " else "") + theme.title,
      fontSize = 12.sp,
      fontWeight = FontWeight.Bold,
      color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
      maxLines = 1
    )
    Text(
      text = theme.subtitle,
      fontSize = 9.sp,
      lineHeight = 11.sp,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center,
      maxLines = 3
    )
  }
}
