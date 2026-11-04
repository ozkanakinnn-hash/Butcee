@file:OptIn(ExperimentalMaterial3Api::class)

package com.nobet.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nobet.data.Store
import com.nobet.notify.Reminders
import com.nobet.i18n.fmtClock
import com.nobet.i18n.fmtDur
import com.nobet.i18n.t

@Composable
private fun SwitchRow(title: String, sub: String = "", checked: Boolean, locked: Boolean = false, onChange: (Boolean) -> Unit) {
  Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
    Column(Modifier.weight(1f)) {
      Text(title)
      if (sub.isNotEmpty()) Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    if (locked) Icon(Icons.Filled.Lock, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(end = 8.dp))
    Switch(checked = checked && !locked, onCheckedChange = onChange)
  }
}

@Composable
private fun RadioRow(title: String, selected: Boolean, locked: Boolean = false, onClick: () -> Unit) {
  Surface(onClick = onClick, color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(12.dp)) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
      RadioButton(selected = selected, onClick = onClick)
      Text(title, Modifier.weight(1f))
      if (locked) Icon(Icons.Filled.Lock, null, tint = MaterialTheme.colorScheme.primary)
    }
  }
}

@Composable
fun SettingsScreen() {
  val ctx = LocalContext.current
  var pickCheckIn by remember { mutableStateOf(false) }
  var confirmDelete by remember { mutableStateOf(false) }
  var editLeave by remember { mutableStateOf(false) }
  val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
    if (!ok) Toast.makeText(ctx, t("Bildirim izni verilmedi", "Notification permission denied"), Toast.LENGTH_SHORT).show()
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(t("Ayarlar", "Settings")) },
        navigationIcon = { IconButton(onClick = { Nav.pop() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, t("Geri", "Back")) } },
      )
    },
  ) { pad ->
    Column(
      Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      // Premium
      NCard(
        Modifier.fillMaxWidth(),
        onClick = { if (!Store.premium) Nav.paywall(t("Nöbet Premium", "Nöbet Premium")) },
        container = MaterialTheme.colorScheme.primaryContainer,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Filled.WorkspacePremium, null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
          Spacer(Modifier.width(12.dp))
          Column(Modifier.weight(1f)) {
            Text(
              if (Store.premium) t("Premium etkin (test)", "Premium active (test)") else t("Premium'a geç", "Go Premium"),
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
              if (Store.premium) t("Tüm özellikler açık.", "All features unlocked.") else t("Akıllı hatırlatıcılar, detaylı istatistikler ve daha fazlası", "Smart reminders, detailed stats and more"),
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
          }
        }
        if (Store.premium) {
          TextButton(onClick = {
            Store.premium = false
            if (Store.theme == "night" || Store.theme == "amber") Store.theme = "dark"
            Store.changed()
          }) { Text(t("Test Premium'u kapat", "Turn off test Premium")) }
        }
      }

      // Profil
      NCard(Modifier.fillMaxWidth()) {
        SectionTitle(t("Profil", "Profile"))
        Text(
          t("Tüm alanlar isteğe bağlıdır. Seçili bir öğeye tekrar dokunarak kaldırabilirsiniz.", "All fields are optional. Tap a selected item again to clear it."),
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Gap(8.dp)
        ProfileFields()
        Gap(8.dp)
        Text(t("Genellikle ihtiyaç duyduğunuz uyku: ", "Usual sleep need: ") + fmtDur(Store.sleepGoalMin), fontWeight = FontWeight.SemiBold)
        Text(
          t("İsteğe bağlı. Yalnızca toparlanma puanında kullanılır; hedef olarak gösterilmez.", "Optional. Only used for your recovery score; never shown as a target."),
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Slider(
          value = Store.sleepGoalMin.toFloat(),
          onValueChange = { Store.sleepGoalMin = (it / 15f).toInt() * 15 },
          onValueChangeFinished = { Store.changed() },
          valueRange = 300f..600f,
        )
      }

      // Vardiyalar
      NCard(Modifier.fillMaxWidth(), onClick = { Nav.open("templates") }) {
        Text(t("Vardiya türleri ve saatleri", "Shift types & times"), fontWeight = FontWeight.SemiBold)
        Text(t("Saatleri düzenleyin, özel vardiya ekleyin", "Edit times, add custom shifts"), style = MaterialTheme.typography.bodySmall)
      }
      NCard(Modifier.fillMaxWidth(), onClick = { Nav.open("patterns") }) {
        Text(t("Şablon kütüphanesi", "Template library"), fontWeight = FontWeight.SemiBold)
        Text(t("Hazır düzenler, mesleğinize göre öneriler ve kendi şablonlarınız", "Ready-made patterns, suggestions for your role and your own templates"), style = MaterialTheme.typography.bodySmall)
      }
      NCard(Modifier.fillMaxWidth(), onClick = { Nav.open("share") }) {
        Text(t("Aile / partner paylaşımı", "Family / partner sharing"), fontWeight = FontWeight.SemiBold)
        Text(
          if (Store.hasPartner) t("${Store.partnerName} takvimi ekli", "${Store.partnerName}'s schedule added")
          else t("Vardiyalarınızı bir kişiyle paylaşın (uyku verisi paylaşılmaz)", "Share your shifts with one person (no sleep data)"),
          style = MaterialTheme.typography.bodySmall,
        )
      }

      // Hatırlatıcılar
      NCard(Modifier.fillMaxWidth()) {
        SectionTitle(t("Hatırlatıcılar", "Reminders"))
        if (!Reminders.canNotify(ctx)) {
          TextButton(onClick = { if (Build.VERSION.SDK_INT >= 33) permLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }) {
            Text(t("⚠ Bildirim izni ver", "⚠ Allow notifications"))
          }
        }
        SwitchRow(t("Vardiya başlangıcı", "Shift start"), t("Vardiyadan önce haber ver", "Heads-up before each shift"), Store.remShiftStart) {
          Store.remShiftStart = it
          Store.changed()
        }
        if (Store.remShiftStart) {
          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(30, 60, 90, 120).forEach { m ->
              FilterChip(selected = Store.remShiftLeadMin == m, onClick = {
                Store.remShiftLeadMin = m
                Store.changed()
              }, label = { Text(fmtDur(m)) })
            }
          }
        }
        SwitchRow(t("Günlük enerji kaydı", "Daily energy check-in"), fmtClock(Store.remCheckInMin), Store.remCheckIn) {
          Store.remCheckIn = it
          Store.changed()
        }
        if (Store.remCheckIn) TextButton(onClick = { pickCheckIn = true }) { Text(t("Saati değiştir", "Change time")) }
        SwitchRow(t("Vardiya sonrası toparlanma kontrolü", "Post-shift recovery check-in"), t("Vardiya bitince ve dinlendikten sonra 4 kısa soru", "4 quick questions after your shift and after you've rested"), Store.remRecovery) {
          Store.remRecovery = it
          Store.changed()
        }
        SwitchRow(
          t("Akıllı vardiya hatırlatıcıları", "Smart shift-aware reminders"),
          t("Gece öncesi uyarı, kestirme, yatma vakti, kafein, vardiya sonrası", "Night-before alert, nap, wind-down, caffeine, after-shift"),
          Store.remSmart,
          locked = !Store.premium,
        ) {
          if (!Store.premium) Nav.paywall(t("Akıllı hatırlatıcılar", "Smart reminders")) else {
            Store.remSmart = it
            Store.changed()
          }
        }
        if (Store.premium && Store.remSmart) {
          com.nobet.data.Builtins.smartKinds.forEach { k ->
            val (title, sub) = Reminders.kindName(k)
            Row(Modifier.padding(start = 12.dp)) {
              SwitchRow(title, sub, Store.smartOn(k)) { Store.setSmart(k, it) }
            }
          }
        }
        Gap(4.dp)
        SectionTitle(t("Yaklaşan hatırlatıcılar", "Upcoming reminders"))
        val up = Reminders.upcoming().take(5)
        if (up.isEmpty()) {
          Text(t("Planlanmış hatırlatıcı yok", "No reminders scheduled"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        up.forEach { e ->
          Text("• " + Reminders.describe(e), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row {
          TextButton(onClick = {
            Reminders.show(ctx, t("Test bildirimi", "Test notification"), t("Hatırlatıcılar çalışıyor 👍", "Reminders are working 👍"))
            if (!Reminders.canNotify(ctx)) Toast.makeText(ctx, t("Önce bildirim izni verin", "Allow notifications first"), Toast.LENGTH_SHORT).show()
          }) { Text(t("Test bildirimi gönder", "Send test notification")) }
          if (Build.VERSION.SDK_INT >= 31) {
            TextButton(onClick = {
              runCatching {
                ctx.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:" + ctx.packageName)))
              }
            }) { Text(t("Tam zamanlı alarm", "Exact alarms")) }
          }
        }
      }

      // Görünüm
      NCard(Modifier.fillMaxWidth()) {
        SectionTitle(t("Görünüm", "Appearance"))
        listOf(
          "system" to t("Sistem", "System"),
          "light" to t("Açık", "Light"),
          "dark" to t("Koyu", "Dark"),
          "night" to t("Gece (kırmızı, çok loş)", "Night (red, ultra-dim)"),
          "amber" to t("Gece (amber, göz yormayan)", "Night (amber, easy on the eyes)"),
        ).forEach { (k, l) ->
          val locked = (k == "night" || k == "amber") && !Store.premium
          RadioRow(l, Store.theme == k, locked) {
            if (locked) Nav.paywall(t("Gece teması", "Night theme")) else {
              Store.theme = k
              Store.changed()
            }
          }
        }
      }

      // Tatiller ve izin
      NCard(Modifier.fillMaxWidth()) {
        SectionTitle(t("Resmî tatiller ve izin", "Holidays & leave"))
        SwitchRow(t("Resmî tatilleri göster (Türkiye)", "Show public holidays (Turkey)"), t("Takvimde kırmızı gün ve tatil adı", "Red date and holiday name in the calendar"), Store.showHolidays) {
          Store.showHolidays = it
          Store.changed()
        }
        Text(
          t("Yıllık izin hakkı: ${fmtDays(Store.leaveAllowance)} gün · devreden ${fmtDays(Store.leaveCarry)} · kalan ${fmtDays(Store.leaveRemaining())}",
            "Annual leave: ${fmtDays(Store.leaveAllowance)} days · carried over ${fmtDays(Store.leaveCarry)} · left ${fmtDays(Store.leaveRemaining())}"),
          style = MaterialTheme.typography.bodySmall,
        )
        TextButton(onClick = { editLeave = true }) { Text(t("İzin hakkını ayarla", "Set leave allowance")) }
      }

      // Dil
      NCard(Modifier.fillMaxWidth()) {
        SectionTitle(t("Dil / Language", "Language / Dil"))
        listOf("system" to t("Cihaz dili", "Device language"), "tr" to "Türkçe", "en" to "English").forEach { (k, l) ->
          RadioRow(l, Store.lang == k) {
            Store.lang = k
            Reminders.ensureChannel(ctx)
            Store.changed()
          }
        }
      }

      // Veri
      NCard(Modifier.fillMaxWidth()) {
        SectionTitle(t("Veri ve gizlilik", "Data & privacy"))
        Text(
          t("Tüm verileriniz yalnızca bu cihazda saklanır. Hesap gerekmez.", "All your data stays on this device. No account required."),
          style = MaterialTheme.typography.bodySmall,
        )
        TextButton(onClick = { if (Store.premium) shareCsv(ctx) else Nav.paywall(t("Veri dışa aktarma", "Data export")) }) {
          Text(t("Verileri dışa aktar (CSV)", "Export data (CSV)") + if (Store.premium) "" else " 🔒")
        }
        TextButton(onClick = { confirmDelete = true }) { Text(t("Tüm verileri sil", "Delete all data"), color = MaterialTheme.colorScheme.error) }
      }

      // Hakkında
      NCard(Modifier.fillMaxWidth()) {
        SectionTitle(t("Hakkında", "About"))
        Text("Nöbet 0.6.0 (test)", fontWeight = FontWeight.SemiBold)
        Text(
          t("Nöbet genel sağlıklı yaşam bilgisi sunar, tıbbi tavsiye değildir. Sağlık sorunlarınız için bir hekime danışın.",
            "Nöbet provides general wellness information, not medical advice. Consult a doctor about health concerns."),
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
      Spacer(Modifier.height(24.dp))
    }
  }

  if (pickCheckIn) {
    TimePickDialog(t("Enerji kaydı saati", "Check-in time"), Store.remCheckInMin, { pickCheckIn = false }) {
      Store.remCheckInMin = it
      Store.changed()
      pickCheckIn = false
    }
  }
  if (editLeave) LeaveSettingsDialog { editLeave = false }
  if (confirmDelete) {
    ConfirmDialog(
      t("Tüm veriler silinsin mi?", "Delete all data?"),
      t("Vardiyalar, uyku ve enerji kayıtları kalıcı olarak silinir. Bu işlem geri alınamaz.", "Shifts, sleep and energy logs will be permanently deleted. This can't be undone."),
      t("Hepsini sil", "Delete all"),
      { confirmDelete = false },
    ) {
      confirmDelete = false
      Nav.stack.clear()
      Nav.tab = 0
      Store.clearAll()
    }
  }
}

@Composable
fun PaywallScreen() {
  val ctx = LocalContext.current
  var plan by remember { mutableStateOf("annual") }
  val tr = !Store.isEn
  val prices = mapOf(
    "monthly" to if (tr) "₺79,99 / ay" else "$3.99 / month",
    "annual" to if (tr) "₺499,99 / yıl" else "$24.99 / year",
    "lifetime" to if (tr) "₺1.199,99 tek sefer" else "$59.99 one-time",
  )
  val subs = mapOf(
    "monthly" to t("İstediğiniz zaman iptal", "Cancel anytime"),
    "annual" to t("≈ ₺41,67/ay · %48 tasarruf · 7 gün ücretsiz", "≈ $2.08/mo · save 48% · 7-day free trial"),
    "lifetime" to t("Abonelik yok, bir kez öde", "No subscription, pay once"),
  )
  Column(Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState()).padding(20.dp)) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
      IconButton(onClick = { Nav.pop() }) { Icon(Icons.Filled.Close, t("Kapat", "Close")) }
    }
    Icon(Icons.Filled.WorkspacePremium, null, Modifier.align(Alignment.CenterHorizontally).padding(8.dp), tint = MaterialTheme.colorScheme.primary)
    Text(
      "Nöbet Premium",
      style = MaterialTheme.typography.headlineMedium,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.fillMaxWidth(),
      textAlign = TextAlign.Center,
    )
    if (Nav.paywallReason.isNotEmpty()) {
      Text(
        Nav.paywallReason + t(" Premium ile açılır.", " is a Premium feature."),
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
    Gap(16.dp)
    listOf(
      t("Akıllı, vardiyaya duyarlı hatırlatıcılar", "Smart, shift-aware reminders"),
      t("Toparlanma örüntüleri ve nazik öneriler", "Recovery patterns & gentle suggestions"),
      t("Kafein ve öğün zamanlaması, sınırsız geçmiş", "Caffeine & meal timing, unlimited history"),
      t("Tüm vardiya düzenleri, sınırsız şablon", "Full pattern library, unlimited templates"),
      t("Veri dışa aktarma, kırmızı ve amber gece temaları", "Data export, red & amber night themes"),
    ).forEach {
      Row(Modifier.padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Filled.CheckCircle, null, tint = Success)
        Spacer(Modifier.width(10.dp))
        Text(it)
      }
    }
    Gap(16.dp)
    listOf("annual", "monthly", "lifetime").forEach { k ->
      val name = when (k) {
        "annual" -> t("Yıllık", "Annual")
        "monthly" -> t("Aylık", "Monthly")
        else -> t("Ömür boyu", "Lifetime")
      }
      ChoiceCard("$name · ${prices[k]}", subs[k] ?: "", selected = plan == k) { plan = k }
    }
    Gap(16.dp)
    BigButton(if (plan == "annual") t("7 gün ücretsiz dene", "Start 7-day free trial") else t("Devam et", "Continue")) {
      Store.premium = true
      Store.changed()
      Toast.makeText(ctx, t("Premium açıldı (test sürümü)", "Premium unlocked (test build)"), Toast.LENGTH_SHORT).show()
      Nav.pop()
    }
    Text(
      t("Test sürümü: ödeme alınmaz, Premium hemen açılır. Ayarlar'dan kapatabilirsiniz.",
        "Test build: no payment is taken, Premium unlocks immediately. You can turn it off in Settings."),
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center,
      modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
    )
    TextButton(onClick = {
      Toast.makeText(ctx, t("Geri yüklenecek satın alma yok", "No purchases to restore"), Toast.LENGTH_SHORT).show()
    }, modifier = Modifier.fillMaxWidth()) { Text(t("Satın alımları geri yükle", "Restore purchases")) }
  }
}
