@file:OptIn(ExperimentalMaterial3Api::class)

package com.nobet.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nobet.data.Store
import com.nobet.i18n.fmtClock
import com.nobet.i18n.fmtDate
import com.nobet.i18n.relDay
import com.nobet.i18n.t
import com.nobet.i18n.typeName
import java.time.Instant

/** Paylaşım mesajı: okunabilir 14 günlük özet + uygulamada içe aktarılabilir kod. Uyku/enerji içermez. */
fun buildShareMessage(days: Int): String {
  val sender = Store.name.trim().ifEmpty { t("Bir Nöbet kullanıcısı", "A Nöbet user") }
  val code = Store.makeShareCode(days, sender)
  val sb = StringBuilder()
  sb.append(t("$sender vardiya takvimini seninle paylaştı 📅\n\n", "$sender shared their shift schedule with you 📅\n\n"))
  val today = Store.today()
  for (i in 0 until 14) {
    val d = today.plusDays(i.toLong())
    val ty = Store.shiftOn(d)
    val line = when {
      ty == null -> "–"
      ty.isOff -> typeName(ty)
      else -> "${typeName(ty)} ${fmtClock(ty.startMin)}–${fmtClock(ty.endMin)}"
    }
    sb.append("${fmtDate(d, "d MMM EEE")}: $line\n")
  }
  sb.append(
    t(
      "\nNöbet uygulamasında görmek için: Ayarlar → Aile paylaşımı → Kodu yapıştır.\n",
      "\nTo view it in the Nöbet app: Settings → Family sharing → Paste code.\n",
    ),
  )
  sb.append("nobet://share?c=$code\n")
  return sb.toString()
}

fun copyToClipboard(ctx: Context, text: String) {
  val cm = ctx.getSystemService(ClipboardManager::class.java) ?: return
  cm.setPrimaryClip(ClipData.newPlainText("Nöbet", text))
}

fun readClipboard(ctx: Context): String =
  ctx.getSystemService(ClipboardManager::class.java)?.primaryClip?.takeIf { it.itemCount > 0 }?.getItemAt(0)?.coerceToText(ctx)?.toString() ?: ""

@Composable
fun ShareScreen() {
  val ctx = LocalContext.current
  var days by remember { mutableIntStateOf(60) }
  var code by remember { mutableStateOf("") }
  var confirmRemove by remember { mutableStateOf(false) }
  var pendingImport by remember { mutableStateOf<String?>(null) }

  fun doImport(text: String) {
    val who = Store.importShareCode(text)
    if (who == null) {
      Toast.makeText(ctx, t("Geçerli bir paylaşım kodu bulunamadı", "No valid share code found"), Toast.LENGTH_LONG).show()
    } else {
      code = ""
      Toast.makeText(ctx, t("$who takvimi eklendi", "$who's schedule added"), Toast.LENGTH_SHORT).show()
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(t("Aile / partner paylaşımı", "Family / partner sharing")) },
        navigationIcon = { IconButton(onClick = { Nav.pop() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, t("Geri", "Back")) } },
      )
    },
  ) { pad ->
    Column(
      Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      NCard(Modifier.fillMaxWidth(), container = MaterialTheme.colorScheme.secondaryContainer) {
        Row {
          Icon(Icons.Filled.Shield, null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
          Spacer(Modifier.width(10.dp))
          Text(
            t("Yalnızca vardiyalarınız paylaşılır. Uyku, enerji ve notlarınız asla paylaşılmaz. Sunucu yok — paylaşım bir mesajla, doğrudan sizden karşı tarafa gider.",
              "Only your shifts are shared. Your sleep, energy and notes are never shared. No server — the share goes directly from you to them in a message."),
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            style = MaterialTheme.typography.bodySmall,
          )
        }
      }

      // Benim takvimim
      NCard(Modifier.fillMaxWidth()) {
        Text(t("Takvimimi paylaş", "Share my schedule"), style = MaterialTheme.typography.titleMedium)
        Text(
          t("Partnerinize veya bir aile üyenize gönderin. Takviminiz değişirse yeniden paylaşın.", "Send it to your partner or a family member. Share again if your schedule changes."),
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Gap(8.dp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          listOf(30, 60, 90).forEach { d ->
            FilterChip(selected = days == d, onClick = { days = d }, label = { Text(t("$d gün", "$d days")) })
          }
        }
        Gap(8.dp)
        val hasShifts = (0 until days).any { Store.shifts.containsKey(Store.key(Store.today().plusDays(it.toLong()))) }
        BigButton(t("Paylaş", "Share"), enabled = hasShifts) {
          val i = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, buildShareMessage(days))
          ctx.startActivity(Intent.createChooser(i, t("Takvimi paylaş", "Share schedule")))
        }
        TextButton(enabled = hasShifts, onClick = {
          copyToClipboard(ctx, buildShareMessage(days))
          Toast.makeText(ctx, t("Kopyalandı", "Copied"), Toast.LENGTH_SHORT).show()
        }) { Text(t("Mesajı kopyala", "Copy message")) }
        if (!hasShifts) Text(t("Önce takviminize vardiya ekleyin.", "Add shifts to your calendar first."), style = MaterialTheme.typography.bodySmall)
      }

      // Benimle paylaşılan
      NCard(Modifier.fillMaxWidth()) {
        Text(t("Benimle paylaşılan takvim", "Schedule shared with me"), style = MaterialTheme.typography.titleMedium)
        if (Store.hasPartner) {
          Text(Store.partnerName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
          val upd = Instant.ofEpochMilli(Store.partnerUpdated).atZone(Store.zone).toLocalDate()
          Text(
            t("Güncellendi: ", "Updated: ") + fmtDate(upd, "d MMM") +
              (Store.partnerLastDay()?.let { t(" · ${fmtDate(it, "d MMM")} tarihine kadar", " · until ${fmtDate(it, "d MMM")}") } ?: ""),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Gap(8.dp)
          val today = Store.today()
          for (i in 0 until 14) {
            val d = today.plusDays(i.toLong())
            val mine = Store.shiftOn(d)
            val theirs = Store.partnerShiftOn(d)
            Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
              Text(relDay(d), Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
              Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1.4f)) {
                Text(
                  theirs?.let { typeName(it) + if (!it.isOff) " ${fmtClock(it.startMin)}–${fmtClock(it.endMin)}" else "" } ?: "–",
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.SemiBold,
                )
              }
              Spacer(Modifier.width(8.dp))
              if (theirs != null) ShiftBadge(theirs, 26.dp) else Spacer(Modifier.size(26.dp))
              Spacer(Modifier.width(6.dp))
              if (mine != null) ShiftBadge(mine, 20.dp) else Spacer(Modifier.size(20.dp))
            }
          }
          Text(
            t("Büyük rozet: ${Store.partnerName} · küçük rozet: siz. İkinizin de izinli olduğu günler: ", "Big badge: ${Store.partnerName} · small badge: you. Days you're both off: ") +
              (0 until 30).count { k ->
                val d = today.plusDays(k.toLong())
                val a = Store.shiftOn(d)
                val b = Store.partnerShiftOn(d)
                (a == null || a.isOff) && b != null && b.isOff
              } + t(" (30 gün)", " (30 days)"),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Gap(8.dp)
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = {
              val clip = readClipboard(ctx)
              if (clip.contains("NB1")) pendingImport = clip else Toast.makeText(ctx, t("Panoda kod yok", "No code in clipboard"), Toast.LENGTH_SHORT).show()
            }) { Text(t("Panodan güncelle", "Update from clipboard")) }
            TextButton(onClick = { confirmRemove = true }) { Text(t("Kaldır", "Remove"), color = MaterialTheme.colorScheme.error) }
          }
        } else {
          Text(
            t("Size gönderilen paylaşım mesajını yapıştırın. Yalnızca bir kişinin takvimi tutulur.", "Paste the share message you received. Only one person's schedule is kept."),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Gap(8.dp)
          OutlinedTextField(
            value = code,
            onValueChange = { code = it },
            label = { Text(t("Paylaşım mesajı veya kodu", "Share message or code")) },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 4,
          )
          Gap(8.dp)
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { code = readClipboard(ctx) }) { Text(t("Panodan yapıştır", "Paste")) }
            OutlinedButton(enabled = code.isNotBlank(), onClick = { doImport(code) }) { Text(t("Ekle", "Add")) }
          }
        }
      }
      Spacer(Modifier.height(24.dp))
    }
  }

  if (confirmRemove) {
    ConfirmDialog(
      t("Paylaşılan takvim kaldırılsın mı?", "Remove shared schedule?"),
      t("${Store.partnerName} takvimi bu cihazdan silinecek.", "${Store.partnerName}'s schedule will be removed from this device."),
      t("Kaldır", "Remove"),
      { confirmRemove = false },
    ) {
      Store.removePartner()
      confirmRemove = false
    }
  }
  pendingImport?.let { txt ->
    ConfirmDialog(
      t("Takvim güncellensin mi?", "Update schedule?"),
      t("Mevcut paylaşılan takvim, panodaki kodla değiştirilecek.", "The current shared schedule will be replaced with the code in your clipboard."),
      t("Güncelle", "Update"),
      { pendingImport = null },
    ) {
      doImport(txt)
      pendingImport = null
    }
  }
}

/** Bugün ekranında partnerin bugünkü ve yarınki vardiyası. */
@Composable
fun PartnerTodayCard() {
  if (!Store.hasPartner) return
  val today = Store.today()
  NCard(Modifier.fillMaxWidth(), onClick = { Nav.open("share") }) {
    Text(Store.partnerName, style = MaterialTheme.typography.labelLarge)
    Gap(6.dp)
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
      listOf(0, 1).forEach { k ->
        val d = today.plusDays(k.toLong())
        val ty = Store.partnerShiftOn(d)
        Row(verticalAlignment = Alignment.CenterVertically) {
          if (ty != null) ShiftBadge(ty, 28.dp)
          Spacer(Modifier.width(8.dp))
          Column {
            Text(relDay(d), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(ty?.let { typeName(it) } ?: "–", fontWeight = FontWeight.SemiBold)
          }
        }
      }
    }
  }
}
