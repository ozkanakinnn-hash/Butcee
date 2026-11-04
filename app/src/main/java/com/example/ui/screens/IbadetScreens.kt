package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.Dua
import com.example.data.IslamicDays
import com.example.data.KandilDualari
import com.example.data.KazaStore
import com.example.data.RamadanCalendar
import com.example.i18n.AppLanguage
import java.util.Calendar

private val AmiriFont = FontFamily(Font(R.font.amiri_regular, FontWeight.Normal), Font(R.font.amiri_bold, FontWeight.Bold))
private fun tr(t: String, e: String) = if (AppLanguage.isEnglish) e else t

@Composable
private fun ScreenHeader(title: String, subtitle: String, onBack: () -> Unit) {
  val cs = MaterialTheme.colorScheme
  Row(modifier = Modifier.fillMaxWidth().padding(start = 4.dp, end = 16.dp, top = 8.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = tr("Geri", "Back"), tint = cs.onBackground) }
    Column {
      Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = cs.onBackground)
      Text(subtitle, fontSize = 12.sp, color = cs.onSurfaceVariant)
    }
  }
}

@Composable
private fun Panel(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
  val cs = MaterialTheme.colorScheme
  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 6.dp)
      .clip(RoundedCornerShape(20.dp))
      .background(cs.surface)
      .border(1.dp, cs.outline, RoundedCornerShape(20.dp))
      .padding(16.dp)
  ) { content() }
}

// ───────────────────────────── KAZA NAMAZI TAKİBİ ─────────────────────────────

@Composable
fun KazaNamaziScreen(onNavigateBack: () -> Unit) {
  BackHandler { onNavigateBack() }
  val context = LocalContext.current
  val store = remember { KazaStore(context) }
  var version by remember { mutableIntStateOf(0) }
  var showCalc by remember { mutableStateOf(false) }
  var showReset by remember { mutableStateOf(false) }
  val cs = MaterialTheme.colorScheme
  val en = AppLanguage.isEnglish
  // Kayıtlar her değişiklikte (version) yeniden okunur
  val rows = remember(version) { KazaStore.Vakit.entries.map { Triple(it, store.remaining(it), store.prayed(it)) } }
  val remaining = rows.sumOf { it.second }
  val prayed = rows.sumOf { it.third }
  val all = remaining + prayed

  LazyColumn(modifier = Modifier.fillMaxSize().background(cs.background).testTag("kaza_screen")) {
    item { ScreenHeader(tr("Kaza Namazı Takibi", "Missed Prayer Tracker"), tr("Borcunu gir, kıldıkça işaretle", "Enter your debt, mark as you pray"), onNavigateBack) }
    item {
      Panel {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Column(modifier = Modifier.weight(1f)) {
            Text(tr("Kalan kaza", "Remaining"), fontSize = 12.sp, color = cs.onSurfaceVariant)
            Text("$remaining", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = cs.primary)
          }
          Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
            Text(tr("Kılınan", "Prayed"), fontSize = 12.sp, color = cs.onSurfaceVariant)
            Text("$prayed", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = cs.onSurface)
          }
        }
        Spacer(modifier = Modifier.height(10.dp))
        LinearProgressIndicator(
          progress = { if (all == 0) 0f else prayed / all.toFloat() },
          modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
          color = cs.primary, trackColor = cs.surfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          ActionChip(tr("➕ Borç hesapla / ekle", "➕ Calculate / add"), Modifier.weight(1f), filled = true) { showCalc = true }
          ActionChip(tr("↻ Sıfırla", "↻ Reset"), Modifier.weight(0.6f)) { showReset = true }
        }
      }
    }
    items(rows) { (v, r, p) ->
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 4.dp)
          .clip(RoundedCornerShape(16.dp))
          .background(cs.surface)
          .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(if (en) v.en else v.tr, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = cs.onSurface)
          Text(
            (if (en) "Remaining $r • Prayed $p" else "Kalan $r • Kılınan $p"),
            fontSize = 11.sp, color = cs.onSurfaceVariant
          )
        }
        Text(v.arabic, fontFamily = AmiriFont, fontSize = 16.sp, color = cs.primary, modifier = Modifier.padding(end = 10.dp))
        RoundBtn("−", enabled = r > 0) { store.add(v, -1); version++ }
        Spacer(modifier = Modifier.width(6.dp))
        RoundBtn("+") { store.add(v, 1); version++ }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          tr("✓ Kıldım", "✓ Prayed"),
          fontSize = 12.sp, fontWeight = FontWeight.Bold,
          color = if (r > 0) cs.onPrimary else cs.onSurfaceVariant,
          modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (r > 0) cs.primary else cs.surfaceVariant)
            .clickable(enabled = r > 0) {
              store.markPrayed(v); version++
              Toast.makeText(context, if (en) "May God accept it 🤲" else "Allah kabul etsin 🤲", Toast.LENGTH_SHORT).show()
            }
            .padding(horizontal = 10.dp, vertical = 8.dp)
        )
      }
    }
    item {
      Text(
        tr(
          "ⓘ Kaza borcu, ergenlik çağından itibaren kılınmayan namazlar için yaklaşık hesaplanır. Vitir, Hanefî mezhebine göre vaciptir. Kesin bilgi için müftülüklere danışabilirsiniz.",
          "ⓘ Missed prayers are estimated from the age of maturity. Witr is wajib in the Hanafi school. Consult a local mufti for certainty."
        ),
        fontSize = 11.sp, lineHeight = 15.sp, color = cs.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
      )
    }
  }

  if (showCalc) {
    var years by remember { mutableStateOf("") }
    var days by remember { mutableStateOf("") }
    var vitir by remember { mutableStateOf(true) }
    AlertDialog(
      onDismissRequest = { showCalc = false },
      title = { Text(tr("Kaza borcu ekle", "Add missed prayers")) },
      text = {
        Column {
          Text(tr("Kılınmayan süreyi yazın; her vakte o kadar gün eklenir.", "Enter the missed period; that many days are added to each prayer."), fontSize = 13.sp)
          Spacer(modifier = Modifier.height(10.dp))
          OutlinedTextField(years, { years = it.filter(Char::isDigit).take(3) }, label = { Text(tr("Yıl", "Years")) }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
          OutlinedTextField(days, { days = it.filter(Char::isDigit).take(5) }, label = { Text(tr("Gün", "Days")) }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
          Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = vitir, onCheckedChange = { vitir = it })
            Text(tr("Vitir namazını da ekle", "Include witr"), fontSize = 13.sp)
          }
        }
      },
      confirmButton = {
        TextButton(onClick = {
          val total = (years.toIntOrNull() ?: 0) * 365 + (days.toIntOrNull() ?: 0)
          if (total > 0) { store.addDays(total, vitir); version++ }
          showCalc = false
        }) { Text(tr("Ekle", "Add"), fontWeight = FontWeight.Bold) }
      },
      dismissButton = { TextButton(onClick = { showCalc = false }) { Text(tr("Vazgeç", "Cancel")) } }
    )
  }
  if (showReset) {
    AlertDialog(
      onDismissRequest = { showReset = false },
      title = { Text(tr("Sıfırla", "Reset")) },
      text = { Text(tr("Tüm kaza kayıtları silinecek.", "All missed-prayer records will be deleted.")) },
      confirmButton = { TextButton(onClick = { store.resetAll(); version++; showReset = false }) { Text(tr("Sil", "Delete"), fontWeight = FontWeight.Bold) } },
      dismissButton = { TextButton(onClick = { showReset = false }) { Text(tr("Vazgeç", "Cancel")) } }
    )
  }
}

@Composable
private fun RoundBtn(label: String, enabled: Boolean = true, onClick: () -> Unit) {
  val cs = MaterialTheme.colorScheme
  Box(
    modifier = Modifier.size(32.dp).clip(CircleShape).background(cs.primaryContainer).clickable(enabled = enabled) { onClick() },
    contentAlignment = Alignment.Center
  ) { Text(label, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = if (enabled) cs.primary else cs.onSurfaceVariant) }
}

@Composable
private fun ActionChip(label: String, modifier: Modifier = Modifier, filled: Boolean = false, onClick: () -> Unit) {
  val cs = MaterialTheme.colorScheme
  Text(
    label, fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center,
    color = if (filled) cs.onPrimary else cs.primary,
    modifier = modifier
      .clip(RoundedCornerShape(14.dp))
      .background(if (filled) cs.primary else cs.primaryContainer)
      .clickable { onClick() }
      .padding(vertical = 11.dp)
  )
}

// ───────────────────────────── KANDİL DUALARI ─────────────────────────────

@Composable
fun KandilDualariScreen(onNavigateBack: () -> Unit) {
  BackHandler { onNavigateBack() }
  val cs = MaterialTheme.colorScheme
  // Yaklaşan ilk kandil seçili gelir
  val upcoming = remember {
    val cal = Calendar.getInstance()
    val today = RamadanCalendar.dateKey(cal)
    (IslamicDays.forYear(cal.get(Calendar.YEAR)) + IslamicDays.forYear(cal.get(Calendar.YEAR) + 1))
      .toSortedMap().filterKeys { it >= today }.entries
      .firstNotNullOfOrNull { (date, d) -> KandilDualari.keyFor(d.title)?.let { it to date } }
  }
  var selected by remember { mutableStateOf(upcoming?.first ?: "regaib") }
  val info = KandilDualari.kandiller.first { it.key == selected }

  LazyColumn(modifier = Modifier.fillMaxSize().background(cs.background).testTag("kandil_dualari")) {
    item {
      ScreenHeader(
        tr("Kandil Duaları", "Holy Night Duas"),
        upcoming?.let { (k, date) ->
          val n = KandilDualari.kandiller.first { it.key == k }.name.text
          tr("Yaklaşan: $n • ${date.substring(8, 10)}.${date.substring(5, 7)}.${date.substring(0, 4)}", "Upcoming: $n • $date")
        } ?: tr("Kandil gecelerine özel dualar", "Duas for the holy nights"),
        onNavigateBack
      )
    }
    item {
      LazyRow(modifier = Modifier.padding(vertical = 6.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(KandilDualari.kandiller) { k ->
          val sel = k.key == selected
          Text(
            k.name.text, fontSize = 13.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium,
            color = if (sel) cs.onPrimary else cs.primary,
            modifier = Modifier.clip(RoundedCornerShape(50)).background(if (sel) cs.primary else cs.primaryContainer)
              .clickable { selected = k.key }.padding(horizontal = 14.dp, vertical = 8.dp)
          )
        }
      }
    }
    item { Panel { Text(info.about.text, fontSize = 13.sp, lineHeight = 19.sp, color = cs.onSurface) } }
    items(info.duas + KandilDualari.general) { d -> DuaCard(d) }
    item {
      Panel {
        Text(tr("🌙 Bu gece yapılabilecekler", "🌙 What to do tonight"), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = cs.onSurface)
        Spacer(modifier = Modifier.height(8.dp))
        KandilDualari.worship.forEach { w ->
          Row(modifier = Modifier.padding(vertical = 3.dp)) {
            Text("•", color = cs.primary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 8.dp))
            Text(w.text, fontSize = 13.sp, color = cs.onSurface)
          }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(KandilDualari.note.text, fontSize = 11.sp, lineHeight = 15.sp, color = cs.onSurfaceVariant)
      }
    }
    item { Spacer(modifier = Modifier.height(16.dp)) }
  }
}

@Composable
private fun DuaCard(d: Dua) {
  val cs = MaterialTheme.colorScheme
  val clipboard = LocalClipboardManager.current
  val context = LocalContext.current
  Panel {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Text(d.title.text, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = cs.primary, modifier = Modifier.weight(1f))
      IconButton(onClick = {
        clipboard.setText(AnnotatedString("${d.arabic}\n\n${d.okunus}\n\n${d.meaning.text}\n— ${d.source}"))
        Toast.makeText(context, tr("Dua kopyalandı", "Dua copied"), Toast.LENGTH_SHORT).show()
      }) { Icon(Icons.Default.ContentCopy, contentDescription = tr("Kopyala", "Copy"), tint = cs.primary, modifier = Modifier.size(18.dp)) }
    }
    Text(
      d.arabic, fontFamily = AmiriFont, fontSize = 21.sp, lineHeight = 38.sp, color = cs.onSurface,
      textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge.copy(textDirection = TextDirection.Rtl),
      modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
    )
    Text(d.okunus, fontSize = 13.sp, fontStyle = FontStyle.Italic, lineHeight = 19.sp, color = cs.onSurfaceVariant)
    Spacer(modifier = Modifier.height(8.dp))
    Text(d.meaning.text, fontSize = 13.sp, lineHeight = 19.sp, color = cs.onSurface)
    Spacer(modifier = Modifier.height(6.dp))
    Text("— ${d.source}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = cs.onSurfaceVariant)
  }
}

@Suppress("unused")
private val unusedColor = Color.Transparent
