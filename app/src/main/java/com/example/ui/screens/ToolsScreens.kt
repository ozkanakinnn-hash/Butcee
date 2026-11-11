package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.data.AppPreferences
import com.example.data.DiyanetSource
import com.example.data.RamadanCalendar
import com.example.i18n.AppLanguage
import com.example.util.BackupManager
import com.example.util.PrayerCalc
import java.io.File
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private fun L(tr: String, en: String) = if (AppLanguage.isEnglish) en else tr

@Composable
private fun ToolHeader(title: String, subtitle: String, onBack: () -> Unit) {
  Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = L("Geri", "Back")) }
    Column(Modifier.padding(start = 4.dp)) {
      Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
      Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
  }
}

// ═════════════════════════════ Fitre • Fidye • Zekât ═════════════════════════════

/** Dinî ölçüler (Diyanet İşleri Başkanlığı Din İşleri Yüksek Kurulu kararları). */
object ReligiousAmounts {
  /** 2026 Ramazanı → 2027 Ramazanı için açıklanan asgari fitre / günlük fidye (TL). */
  const val FITRE_TL = 240.0
  const val FITRE_YEAR = "2026–2027"
  /** Zekât nisabı: 20 miskal = 80,18 gram altın; oran kırkta bir (%2,5). */
  const val NISAB_GOLD_GRAMS = 80.18
  const val ZAKAT_RATE = 0.025

  fun fitre(people: Int, perPerson: Double = FITRE_TL) = people.coerceAtLeast(0) * perPerson
  fun fidye(days: Int, perDay: Double = FITRE_TL) = days.coerceAtLeast(0) * perDay

  /** @return null: nisaba ulaşmıyor; aksi halde verilecek zekât (TL). */
  fun zakat(netAssetsTl: Double, goldGramPriceTl: Double): Double? {
    if (goldGramPriceTl <= 0) return null
    val nisab = NISAB_GOLD_GRAMS * goldGramPriceTl
    return if (netAssetsTl >= nisab) netAssetsTl * ZAKAT_RATE else null
  }
}

private fun money(v: Double): String = NumberFormat.getNumberInstance(Locale.forLanguageTag("tr")).apply { maximumFractionDigits = 2 }.format(v) + " TL"
private fun parse(s: String): Double = s.replace(".", "").replace(",", ".").trim().toDoubleOrNull() ?: 0.0

@Composable
fun CalculatorsScreen(onNavigateBack: () -> Unit) {
  BackHandler { onNavigateBack() }
  var tab by remember { mutableIntStateOf(0) }
  androidx.compose.runtime.CompositionLocalProvider(androidx.compose.material3.LocalContentColor provides MaterialTheme.colorScheme.onBackground) { Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).testTag("calculators_screen")) {
    ToolHeader(L("Fitre • Fidye • Zekât", "Fitrah • Fidyah • Zakat"), L("Dinî ölçülerle hesaplayıcı", "Calculator based on religious measures"), onNavigateBack)
    TabRow(selectedTabIndex = tab) {
      listOf(L("Fitre", "Fitrah"), L("Fidye", "Fidyah"), L("Zekât", "Zakat")).forEachIndexed { i, s ->
        Tab(selected = tab == i, onClick = { tab = i }, text = { Text(s) })
      }
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
      when (tab) {
        0 -> FitreTab()
        1 -> FidyeTab()
        else -> ZakatTab()
      }
      Text(
        L("Tutarlar Diyanet İşleri Başkanlığı Din İşleri Yüksek Kurulu'nun açıkladığı asgari ölçülere göredir; imkânı olanların daha fazla vermesi tavsiye edilir. Kesin bilgi için müftülüğe danışınız.",
          "Amounts follow the minimums announced by Türkiye's High Board of Religious Affairs; those who can are encouraged to give more."),
        fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
  }
}

@Composable
private fun ResultCard(label: String, value: String, note: String? = null) {
  Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
    Column(Modifier.padding(16.dp)) {
      Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
      Text(value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.testTag("calc_result"))
      if (note != null) Text(note, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
  }
}

@Composable
private fun NumberField(label: String, value: String, onChange: (String) -> Unit, tag: String) {
  OutlinedTextField(
    value = value, onValueChange = { v -> onChange(v.filter { it.isDigit() || it == ',' || it == '.' }.take(15)) },
    label = { Text(label) }, singleLine = true,
    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
    modifier = Modifier.fillMaxWidth().testTag(tag)
  )
}

@Composable
private fun FitreTab() {
  var people by remember { mutableStateOf("1") }
  var perPerson by remember { mutableStateOf("240") }
  Text(L("Fitre (fıtır sadakası), Ramazan bayramına ulaşan ve temel ihtiyaçları dışında nisap miktarı malı olan Müslümanın kendisi ve bakmakla yükümlü olduğu kişiler için verdiği sadakadır. Bayram namazından önce verilmesi uygundur.",
    "Fitrah is the charity paid before Eid al-Fitr for oneself and one's dependants."), fontSize = 13.sp)
  NumberField(L("Kişi sayısı (siz + bakmakla yükümlü olduklarınız)", "Number of people"), people, { people = it }, "fitre_people")
  NumberField(L("Kişi başı tutar (TL) — asgari ${ReligiousAmounts.FITRE_YEAR}: 240 TL", "Per person (TL) — minimum ${ReligiousAmounts.FITRE_YEAR}: 240 TL"), perPerson, { perPerson = it }, "fitre_per")
  ResultCard(L("Toplam fitre", "Total fitrah"), money(ReligiousAmounts.fitre(parse(people).toInt(), parse(perPerson))))
}

@Composable
private fun FidyeTab() {
  var days by remember { mutableStateOf("30") }
  var perDay by remember { mutableStateOf("240") }
  Text(L("Fidye, yaşlılık veya iyileşme ümidi olmayan bir hastalık nedeniyle oruç tutamayan kişinin, tutamadığı her gün için bir fakiri doyuracak miktarda verdiği bedeldir. Günlük fidye, fitre miktarı kadardır.",
    "Fidyah is paid for each fasting day missed due to old age or a permanent illness; the daily amount equals the fitrah."), fontSize = 13.sp)
  NumberField(L("Tutulamayan gün sayısı", "Days missed"), days, { days = it }, "fidye_days")
  NumberField(L("Günlük tutar (TL)", "Per day (TL)"), perDay, { perDay = it }, "fidye_per")
  ResultCard(L("Toplam fidye", "Total fidyah"), money(ReligiousAmounts.fidye(parse(days).toInt(), parse(perDay))))
}

@Composable
private fun ZakatTab() {
  var cash by remember { mutableStateOf("") }
  var gold by remember { mutableStateOf("") }
  var other by remember { mutableStateOf("") }
  var debts by remember { mutableStateOf("") }
  var goldPrice by remember { mutableStateOf("") }
  Text(L("Zekât, temel ihtiyaçları ve borçları dışında nisap miktarı (80,18 gram altın değerinde) mala sahip olan ve bu mal üzerinden bir kamerî yıl geçen Müslümanın, malının kırkta birini (%2,5) vermesidir.",
    "Zakat is 2.5% of net wealth above the nisab (value of 80.18 g of gold) held for one lunar year."), fontSize = 13.sp)
  NumberField(L("Gram altın fiyatı (TL) — bugünkü değeri girin", "Gold price per gram (TL)"), goldPrice, { goldPrice = it }, "zakat_gold_price")
  NumberField(L("Nakit, banka, döviz (TL karşılığı)", "Cash, bank, foreign currency (TL)"), cash, { cash = it }, "zakat_cash")
  NumberField(L("Altın / gümüş (TL karşılığı)", "Gold / silver (TL value)"), gold, { gold = it }, "zakat_gold")
  NumberField(L("Ticari mal, alacak, diğer (TL)", "Trade goods, receivables, other (TL)"), other, { other = it }, "zakat_other")
  NumberField(L("Borçlar (TL)", "Debts (TL)"), debts, { debts = it }, "zakat_debts")
  val net = parse(cash) + parse(gold) + parse(other) - parse(debts)
  val price = parse(goldPrice)
  val z = ReligiousAmounts.zakat(net, price)
  when {
    price <= 0 -> ResultCard(L("Zekât", "Zakat"), "—", L("Nisabı hesaplamak için gram altın fiyatını girin.", "Enter the gold price to calculate the nisab."))
    z == null -> ResultCard(L("Zekât düşmüyor", "No zakat due"), money(0.0),
      L("Net varlık ${money(net)}, nisap ${money(ReligiousAmounts.NISAB_GOLD_GRAMS * price)} altında.", "Net wealth is below the nisab of ${money(ReligiousAmounts.NISAB_GOLD_GRAMS * price)}."))
    else -> ResultCard(L("Verilecek zekât (%2,5)", "Zakat due (2.5%)"), money(z),
      L("Net varlık ${money(net)} • Nisap ${money(ReligiousAmounts.NISAB_GOLD_GRAMS * price)}", "Net wealth ${money(net)} • Nisab ${money(ReligiousAmounts.NISAB_GOLD_GRAMS * price)}"))
  }
}

// ═════════════════════════════ İmsakiye ═════════════════════════════

data class ImsakiyeRow(val date: String, val imsak: String, val gunes: String, val ogle: String, val ikindi: String, val aksam: String, val yatsi: String)

/** 30 günlük vakit tablosu: önce Diyanet tablosu (önbellek), yoksa koordinata göre hesap. */
fun buildImsakiye(context: Context, prefs: AppPreferences): List<ImsakiyeRow> {
  val df = SimpleDateFormat("dd.MM.yyyy", Locale.ROOT)
  val today = df.format(java.util.Date())
  if (!prefs.useCoordinates) {
    val days = DiyanetSource.cachedMonth(context, prefs.country, prefs.province, prefs.district, today)
    if (days.isNotEmpty()) return days.map { ImsakiyeRow(it.date, it.imsak, it.gunes, it.ogle, it.ikindi, it.aksam, it.yatsi) }
  }
  val coords = if (prefs.useCoordinates && prefs.latitude != 0.0) prefs.latitude to prefs.longitude else PrayerCalc.provinceCoords[prefs.province]
    ?: return emptyList()
  val cal = Calendar.getInstance()
  return (0 until 30).map {
    val t = PrayerCalc.times(coords.first, coords.second, cal)
    ImsakiyeRow(df.format(cal.time), t[0], t[1], t[2], t[3], t[4], t[5]).also { cal.add(Calendar.DAY_OF_MONTH, 1) }
  }
}

@Composable
fun ImsakiyeScreen(prefs: AppPreferences, onNavigateBack: () -> Unit) {
  BackHandler { onNavigateBack() }
  val context = LocalContext.current
  val rows = remember { buildImsakiye(context, prefs) }
  androidx.compose.runtime.CompositionLocalProvider(androidx.compose.material3.LocalContentColor provides MaterialTheme.colorScheme.onBackground) { Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).testTag("imsakiye_screen")) {
    ToolHeader(L("İmsakiye", "Prayer Timetable"), "${prefs.province} / ${prefs.district} • " + L("${rows.size} gün", "${rows.size} days"), onNavigateBack)
    if (rows.isEmpty()) {
      Text(L("Vakit tablosu henüz indirilmedi. İnternete bağlanıp ana sayfayı açın.", "Timetable not downloaded yet. Connect and open the home screen."),
        Modifier.padding(16.dp), textAlign = TextAlign.Center)
      return@Column
    }
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      Button(onClick = { shareImsakiyePdf(context, prefs, rows) }, modifier = Modifier.weight(1f).testTag("imsakiye_share")) {
        Text(L("📄 PDF olarak paylaş", "📄 Share as PDF"))
      }
    }
    Spacer(Modifier.height(8.dp))
    val header = listOf(L("Tarih", "Date"), L("İmsak", "Fajr"), L("Güneş", "Sunrise"), L("Öğle", "Dhuhr"), L("İkindi", "Asr"), L("Akşam", "Maghrib"), L("Yatsı", "Isha"))
    TableRow(header, bold = true, highlight = false)
    LazyColumn(Modifier.fillMaxSize()) {
      items(rows) { r ->
        val iso = r.date.split(".").let { "${it[2]}-${it[1]}-${it[0]}" }
        TableRow(listOf(r.date.substring(0, 5), r.imsak, r.gunes, r.ogle, r.ikindi, r.aksam, r.yatsi), bold = false,
          highlight = RamadanCalendar.isRamadanDay(iso))
      }
    }
  }
  }
}

@Composable
private fun TableRow(cells: List<String>, bold: Boolean, highlight: Boolean) {
  Row(
    Modifier.fillMaxWidth()
      .background(if (highlight) MaterialTheme.colorScheme.primaryContainer else if (bold) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.background)
      .padding(horizontal = 8.dp, vertical = 7.dp)
  ) {
    cells.forEachIndexed { i, c ->
      Text(c, Modifier.weight(if (i == 0) 1.1f else 1f), fontSize = 12.sp, textAlign = TextAlign.Center,
        fontWeight = if (bold || i == 1 || i == 5) FontWeight.Bold else FontWeight.Normal,
        color = if (!bold && (i == 1 || i == 5)) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
    }
  }
}

/** A4 PDF imsakiye oluşturur ve paylaşım menüsünü açar. */
fun createImsakiyePdf(context: Context, prefs: AppPreferences, rows: List<ImsakiyeRow>): File {
  val doc = PdfDocument()
  val page = doc.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
  drawImsakiyePage(page.canvas, prefs.province, prefs.district, rows)
  doc.finishPage(page)
  val dir = File(context.cacheDir, "shared_images").apply { mkdirs() }
  val file = File(dir, "imsakiye.pdf")
  file.outputStream().use { doc.writeTo(it) }
  doc.close()
  return file
}

/** A4 (595×842 pt) imsakiye sayfası; PDF'te ve testte aynı çizim kullanılır. */
fun drawImsakiyePage(c: android.graphics.Canvas, province: String, district: String, rows: List<ImsakiyeRow>) {
  c.drawColor(0xFFFFFFFF.toInt())
  val title = Paint().apply { isAntiAlias = true; textSize = 20f; typeface = Typeface.DEFAULT_BOLD; color = 0xFF3A2A0A.toInt() }
  val head = Paint().apply { isAntiAlias = true; textSize = 10f; typeface = Typeface.DEFAULT_BOLD; color = 0xFFFFFFFF.toInt() }
  val cell = Paint().apply { isAntiAlias = true; textSize = 10f; color = 0xFF222222.toInt() }
  val gold = Paint().apply { color = 0xFFC59B3C.toInt() }
  val stripe = Paint().apply { color = 0xFFF6F0E1.toInt() }
  val ram = Paint().apply { color = 0xFFEFE0B5.toInt() }
  c.drawText(L("İmsakiye • $province / $district", "Prayer Timetable • $province / $district"), 40f, 52f, title)
  val sub = Paint(cell).apply { textSize = 9f; color = 0xFF777777.toInt() }
  c.drawText(L("Ramazan Rehberi • Vakitler resmî ilçe takviminden alınmıştır.", "Ramazan Rehberi • Times from the official district calendar."), 40f, 68f, sub)
  val cols = listOf(L("Tarih", "Date"), L("İmsak", "Fajr"), L("Güneş", "Sunrise"), L("Öğle", "Dhuhr"), L("İkindi", "Asr"), L("Akşam", "Maghrib"), L("Yatsı", "Isha"))
  val x0 = 40f; val w = 515f; val colW = w / cols.size; var y = 84f; val rh = 22f
  c.drawRect(x0, y, x0 + w, y + rh, gold)
  cols.forEachIndexed { i, s -> c.drawText(s, x0 + i * colW + (colW - head.measureText(s)) / 2, y + 15f, head) }
  y += rh
  rows.take(31).forEachIndexed { n, r ->
    val iso = r.date.split(".").let { "${it[2]}-${it[1]}-${it[0]}" }
    if (RamadanCalendar.isRamadanDay(iso)) c.drawRect(x0, y, x0 + w, y + rh, ram) else if (n % 2 == 1) c.drawRect(x0, y, x0 + w, y + rh, stripe)
    listOf(r.date, r.imsak, r.gunes, r.ogle, r.ikindi, r.aksam, r.yatsi).forEachIndexed { i, s ->
      val p = if (i == 1 || i == 5) Paint(cell).apply { typeface = Typeface.DEFAULT_BOLD } else cell
      c.drawText(s, x0 + i * colW + (colW - p.measureText(s)) / 2, y + 15f, p)
    }
    y += rh
  }
}

private fun shareImsakiyePdf(context: Context, prefs: AppPreferences, rows: List<ImsakiyeRow>) {
  runCatching {
    val file = createImsakiyePdf(context, prefs, rows)
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val send = Intent(Intent.ACTION_SEND).apply {
      type = "application/pdf"; putExtra(Intent.EXTRA_STREAM, uri); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(send, L("İmsakiyeyi paylaş", "Share timetable")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
  }
}

// ═════════════════════════════ Yedekleme ═════════════════════════════

@Composable
fun BackupScreen(onNavigateBack: () -> Unit, onRestored: () -> Unit) {
  BackHandler { onNavigateBack() }
  val context = LocalContext.current
  var status by remember { mutableStateOf<String?>(null) }
  val createLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri: Uri? ->
    if (uri != null) status = if (BackupManager.writeTo(context, uri)) L("✅ Yedek kaydedildi.", "✅ Backup saved.") else L("❌ Yedek kaydedilemedi.", "❌ Could not save backup.")
  }
  val openLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
    if (uri != null) {
      val n = BackupManager.readFrom(context, uri)
      status = if (n >= 0) L("✅ $n kayıt geri yüklendi. Uygulama yenileniyor…", "✅ Restored $n entries. Reloading…") else L("❌ Geçerli bir Ramazan Rehberi yedeği değil.", "❌ Not a valid backup file.")
      if (n >= 0) onRestored()
    }
  }
  androidx.compose.runtime.CompositionLocalProvider(androidx.compose.material3.LocalContentColor provides MaterialTheme.colorScheme.onBackground) { Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState()).testTag("backup_screen")) {
    ToolHeader(L("Yedekle & Geri Yükle", "Backup & Restore"), L("Telefon değiştirince verileriniz kaybolmasın", "Keep your data when you change phones"), onNavigateBack)
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Text(L("Yedeğe dahil olanlar: zikirmatik sayaçları, kaza namazı borçları, hatim ilerlemesi, oruç günleri, manevi notlar ve ayarlar. Dosyayı Google Drive'a veya telefonunuza kaydedebilirsiniz; uygulama hiçbir sunucuya veri göndermez.",
        "Included: dhikr counters, missed prayers, khatm progress, fasting days, notes and settings. Save the file to Google Drive or your phone; nothing is sent to our servers."), fontSize = 13.sp)
      Button(onClick = {
        val name = "ramazan-rehberi-yedek-" + SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(java.util.Date()) + ".json"
        createLauncher.launch(name)
      }, modifier = Modifier.fillMaxWidth().testTag("backup_export")) { Text(L("💾 Yedek oluştur", "💾 Create backup")) }
      OutlinedButton(onClick = { openLauncher.launch(arrayOf("application/json", "text/plain", "application/octet-stream")) },
        modifier = Modifier.fillMaxWidth().testTag("backup_import")) { Text(L("📂 Yedekten geri yükle", "📂 Restore from backup")) }
      Text(L("⚠️ Geri yükleme, bu telefondaki mevcut kayıtların yerine yedekteki kayıtları koyar.", "⚠️ Restoring replaces the records on this phone with those in the backup."),
        fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
      status?.let { Text(it, fontWeight = FontWeight.Bold) }
      Spacer(Modifier.width(1.dp))
    }
  }
  }
}
