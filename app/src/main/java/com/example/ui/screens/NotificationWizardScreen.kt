package com.example.ui.screens

import android.app.AlarmManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.example.i18n.AppLanguage
import com.example.reminder.ReminderScheduler
import com.example.util.NotificationHelper

private fun tx(t: String, e: String) = if (AppLanguage.isEnglish) e else t

/** Cihaz üreticisine göre "otomatik başlatma / arka plan" ayar ekranları. */
private val autostartComponents = listOf(
  ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"),
  ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"),
  ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity"),
  ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity"),
  ComponentName("com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity"),
  ComponentName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity"),
  ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"),
  ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"),
  ComponentName("com.samsung.android.lool", "com.samsung.android.sm.battery.ui.BatteryActivity"),
  ComponentName("com.asus.mobilemanager", "com.asus.mobilemanager.MainActivity"),
  ComponentName("com.transsion.phonemanager", "com.itel.autobootmanager.activity.AutoBootMgrActivity")
)

private fun autostartIntent(context: Context): Intent? =
  autostartComponents.map { Intent().setComponent(it).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
    .firstOrNull { it.resolveActivity(context.packageManager) != null }

private fun appDetails(context: Context) =
  Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

private fun launch(context: Context, intent: Intent?, fallback: Intent? = null) {
  try {
    if (intent != null) { context.startActivity(intent); return }
  } catch (_: Exception) { }
  try { context.startActivity(fallback ?: appDetails(context)) } catch (_: Exception) {
    Toast.makeText(context, tx("Ayar ekranı açılamadı", "Could not open settings"), Toast.LENGTH_SHORT).show()
  }
}

/**
 * Bildirim sihirbazı: ezan/hatırlatıcıların telefonda kesintisiz çalışması için gereken ayarlar
 * (bildirim izni, tam zamanlı alarm, pil optimizasyonu, otomatik başlatma) ve test düğmeleri.
 */
@Composable
fun NotificationWizardScreen(onNavigateBack: () -> Unit) {
  BackHandler { onNavigateBack() }
  val context = LocalContext.current
  val cs = MaterialTheme.colorScheme
  var tick by remember { mutableIntStateOf(0) }
  LifecycleResumeEffect(Unit) {
    tick++ // ayarlardan dönünce durumları yenile
    onPauseOrDispose { }
  }

  @Suppress("UNUSED_VARIABLE") val t = tick
  val notifOk = NotificationManagerCompat.from(context).areNotificationsEnabled()
  val alarmOk = Build.VERSION.SDK_INT < 31 || (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).canScheduleExactAlarms()
  val batteryOk = (context.getSystemService(Context.POWER_SERVICE) as PowerManager).isIgnoringBatteryOptimizations(context.packageName)
  val autostart = remember { autostartIntent(context) }
  val maker = Build.MANUFACTURER.replaceFirstChar { it.uppercase() }

  Column(
    modifier = Modifier.fillMaxSize().background(cs.background).verticalScroll(rememberScrollState()).testTag("notification_wizard")
  ) {
    Row(modifier = Modifier.fillMaxWidth().padding(start = 4.dp, end = 16.dp, top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
      IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = tx("Geri", "Back"), tint = cs.onBackground) }
      Column {
        Text(tx("Bildirim Sihirbazı", "Notification Wizard"), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = cs.onBackground)
        Text(tx("$maker cihazınız için ezan ve hatırlatıcı ayarları", "Adhan & reminder settings for your $maker device"), fontSize = 12.sp, color = cs.onSurfaceVariant)
      }
    }
    val done = listOf(notifOk, alarmOk, batteryOk).count { it }
    Text(
      tx("$done / 3 temel ayar tamam", "$done / 3 essential settings done"),
      fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (done == 3) Color(0xFF2E7D4F) else cs.primary,
      modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
    )

    WizardStep(
      1, notifOk,
      tx("Bildirim izni", "Notification permission"),
      tx("Ezan ve hatırlatıcı bildirimlerinin görünmesi için gereklidir.", "Required for adhan and reminder notifications to appear."),
      tx("Bildirimleri aç", "Enable notifications")
    ) {
      val i = if (Build.VERSION.SDK_INT >= 26)
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      else appDetails(context)
      launch(context, i)
    }
    WizardStep(
      2, alarmOk,
      tx("Tam zamanlı alarm", "Exact alarms"),
      tx("Ezanın vaktinde, dakikası dakikasına çalması için gereklidir.", "Needed so the adhan plays exactly on time."),
      tx("İzin ver", "Allow")
    ) {
      if (Build.VERSION.SDK_INT >= 31) launch(context, Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${context.packageName}")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
    WizardStep(
      3, batteryOk,
      tx("Pil optimizasyonunu kapat", "Turn off battery optimization"),
      tx("Açılan listede \"Ezan & Vakit\" uygulamasını bulup \"Optimize etme\" / \"Kısıtlama yok\" seçin. Aksi hâlde telefon uyurken bildirim gecikebilir.",
        "In the list, find \"Ezan & Vakit\" and choose \"Don't optimize\" / \"Unrestricted\". Otherwise notifications may be delayed while the phone sleeps."),
      tx("Pil ayarlarını aç", "Open battery settings")
    ) {
      launch(context, Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
    WizardStep(
      4, null,
      tx("Otomatik başlatma / arka plan", "Autostart / background"),
      if (autostart != null) tx("$maker cihazlarda uygulamanın arka planda çalışmasına izin verin (Otomatik başlat: Açık).", "On $maker devices allow the app to run in the background (Autostart: On).")
      else tx("Cihazınızda ayrı bir otomatik başlatma ekranı bulunamadı; uygulama ayarlarından \"Pil\" bölümünü \"Kısıtlama yok\" yapın.", "No separate autostart screen found; set \"Battery\" to \"Unrestricted\" in app settings."),
      tx("Ayarı aç", "Open setting")
    ) { launch(context, autostart) }

    // Testler
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 10.dp)
        .clip(RoundedCornerShape(20.dp))
        .background(cs.surface)
        .border(1.dp, cs.outline, RoundedCornerShape(20.dp))
        .padding(16.dp)
    ) {
      Text(tx("🔔 Bildirimi test et", "🔔 Test notifications"), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = cs.onSurface)
      Spacer(modifier = Modifier.height(10.dp))
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TestButton(tx("Ezan sesi", "Adhan sound"), Modifier.weight(1f)) {
          NotificationHelper.sendEzanNotification(context, tx("🕌 Test • Ezan bildirimi", "🕌 Test • Adhan notification"), tx("Ezan bildirimi sorunsuz çalışıyor.", "Adhan notification works."), notificationId = 7801)
        }
        TestButton(tx("Hatırlatıcı", "Reminder"), Modifier.weight(1f)) {
          NotificationHelper.sendNotification(context, tx("🔔 Test • Hatırlatıcı", "🔔 Test • Reminder"), tx("Hatırlatıcı bildirimi sorunsuz çalışıyor.", "Reminder notification works."), notificationId = 7802)
        }
      }
      Spacer(modifier = Modifier.height(8.dp))
      TestButton(tx("⏱️ 1 dakika sonra arka plan testi", "⏱️ Background test in 1 minute"), Modifier.fillMaxWidth(), filled = true) {
        ReminderScheduler.scheduleTest(context)
        Toast.makeText(context, tx("Uygulamayı kapatıp ekranı kilitleyin; 1 dakika içinde bildirim gelmeli.", "Close the app and lock the screen; a notification should arrive within a minute."), Toast.LENGTH_LONG).show()
      }
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        tx("Arka plan testi gelmezse 3. ve 4. adımları tekrar kontrol edin.", "If the background test doesn't arrive, recheck steps 3 and 4."),
        fontSize = 11.sp, color = cs.onSurfaceVariant
      )
      Spacer(modifier = Modifier.height(8.dp))
      // Firebase konsolundan "Test mesajı gönder" için cihazın FCM kimliği (yalnızca kullanıcı isteyince, panoya)
      TestButton(tx("📋 Bildirim kimliğini kopyala (Firebase testi)", "📋 Copy push token (Firebase test)"), Modifier.fillMaxWidth()) {
        try {
          com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            val token = if (task.isSuccessful) task.result else null
            if (token.isNullOrBlank()) {
              Toast.makeText(context, tx("Kimlik alınamadı. İnternet bağlantısını kontrol edin.", "Could not get the token. Check your connection."), Toast.LENGTH_LONG).show()
            } else {
              val cm = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
              cm.setPrimaryClip(android.content.ClipData.newPlainText("FCM", token))
              Toast.makeText(context, tx("Kopyalandı. Firebase → Messaging → \"Test mesajı gönder\" alanına yapıştırın.", "Copied. Paste it into Firebase → Messaging → \"Send test message\"."), Toast.LENGTH_LONG).show()
            }
          }
        } catch (e: Exception) {
          Toast.makeText(context, tx("Firebase yapılandırılmamış.", "Firebase is not configured."), Toast.LENGTH_LONG).show()
        }
      }
    }
    Spacer(modifier = Modifier.height(16.dp))
  }
}

@Composable
private fun WizardStep(n: Int, ok: Boolean?, title: String, desc: String, action: String, onAction: () -> Unit) {
  val cs = MaterialTheme.colorScheme
  val green = Color(0xFF2E7D4F)
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 5.dp)
      .clip(RoundedCornerShape(18.dp))
      .background(cs.surface)
      .border(1.dp, if (ok == true) green.copy(alpha = 0.5f) else cs.outline, RoundedCornerShape(18.dp))
      .padding(14.dp),
    verticalAlignment = Alignment.Top
  ) {
    Box(
      modifier = Modifier.size(30.dp).clip(CircleShape).background(if (ok == true) green else cs.primaryContainer),
      contentAlignment = Alignment.Center
    ) {
      Text(if (ok == true) "✓" else "$n", fontWeight = FontWeight.Bold, color = if (ok == true) Color.White else cs.primary)
    }
    Spacer(modifier = Modifier.width(12.dp))
    Column(modifier = Modifier.weight(1f)) {
      Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = cs.onSurface)
      Text(
        when (ok) { true -> tx("Tamam", "Done"); false -> tx("Gerekli", "Needed"); null -> tx("Cihaza özel", "Device specific") },
        fontSize = 11.sp, fontWeight = FontWeight.Bold, color = when (ok) { true -> green; false -> Color(0xFFC0392B); null -> cs.primary }
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(desc, fontSize = 12.sp, lineHeight = 17.sp, color = cs.onSurfaceVariant)
      if (ok != true) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          action, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = cs.onPrimary,
          modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(cs.primary).clickable { onAction() }.padding(horizontal = 14.dp, vertical = 8.dp)
        )
      }
    }
  }
}

@Composable
private fun TestButton(label: String, modifier: Modifier = Modifier, filled: Boolean = false, onClick: () -> Unit) {
  val cs = MaterialTheme.colorScheme
  Text(
    label, fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center,
    color = if (filled) cs.onPrimary else cs.primary,
    modifier = modifier.clip(RoundedCornerShape(14.dp)).background(if (filled) cs.primary else cs.primaryContainer)
      .clickable { onClick() }.padding(vertical = 11.dp)
  )
}
