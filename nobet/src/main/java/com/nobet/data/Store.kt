package com.nobet.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.nobet.notify.Reminders
import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.io.ByteArrayOutputStream
import java.util.Locale
import java.util.zip.Deflater
import java.util.zip.Inflater

data class ShiftInstance(
  val date: LocalDate,
  val type: ShiftType,
  val start: LocalDateTime,
  val end: LocalDateTime,
)

/**
 * Uygulamanın tüm durumu. Çevrimdışı: her şey SharedPreferences içinde JSON olarak tutulur.
 * Compose state olduğu için arayüz doğrudan okur; değişiklikten sonra [changed] çağrılır.
 */
object Store {
  private var prefs: SharedPreferences? = null
  lateinit var app: Context
    private set

  val zone: ZoneId get() = ZoneId.systemDefault()

  // Ayarlar
  var lang by mutableStateOf("system") // system / tr / en
  var theme by mutableStateOf("system") // system / light / dark / night
  var onboarded by mutableStateOf(false)
  var name by mutableStateOf("")
  var role by mutableStateOf("") // meslek grubu; boş = belirtilmedi
  var jobTitle by mutableStateOf("") // serbest unvan (isteğe bağlı)
  var workType by mutableStateOf("") // çalışma şekli; boş = belirtilmedi
  var sleepGoalMin by mutableIntStateOf(450)
  var premium by mutableStateOf(false)
  var sleepStart by mutableLongStateOf(0L)

  // Hatırlatıcılar
  var remShiftStart by mutableStateOf(true)
  var remShiftLeadMin by mutableIntStateOf(60)
  var remCheckIn by mutableStateOf(true)
  var remCheckInMin by mutableIntStateOf(18 * 60)
  var remForgot by mutableStateOf(true)
  var remSmart by mutableStateOf(true)
  var lastFired by mutableLongStateOf(0L)
  /** Kapatılan akıllı hatırlatıcı kategorileri. */
  val smartOff = mutableStateListOf<String>()

  // Kullanıcının kaydettiği düzen şablonları
  val customPatterns = mutableStateListOf<Pattern>()

  // Aile / partner paylaşımı: yalnızca VARDİYALAR (uyku/enerji asla paylaşılmaz)
  var partnerName by mutableStateOf("")
  var partnerStart by mutableStateOf("") // ISO tarih
  var partnerSeq by mutableStateOf("")
  var partnerUpdated by mutableLongStateOf(0L)
  val partnerTypes = mutableStateListOf<ShiftType>()

  // Veri
  val types = mutableStateListOf<ShiftType>()
  val shifts = mutableStateMapOf<String, String>() // ISO tarih -> tür kimliği
  val sleeps = mutableStateListOf<SleepEntry>()
  val energies = mutableStateListOf<EnergyEntry>()

  val isEn: Boolean
    get() = when (lang) {
      "en" -> true
      "tr" -> false
      else -> Locale.getDefault().language != "tr"
    }

  fun init(ctx: Context) {
    if (prefs != null) return
    app = ctx.applicationContext
    val p = app.getSharedPreferences("nobet", Context.MODE_PRIVATE)
    prefs = p
    load(p)
  }

  private fun load(p: SharedPreferences) {
    lang = p.getString("lang", "system") ?: "system"
    theme = p.getString("theme", "system") ?: "system"
    onboarded = p.getBoolean("onboarded", false)
    name = p.getString("name", "") ?: ""
    role = p.getString("role", "") ?: ""
    jobTitle = p.getString("jobTitle", "") ?: ""
    workType = p.getString("workType", "") ?: ""
    sleepGoalMin = p.getInt("sleepGoal", 450)
    premium = p.getBoolean("premium", false)
    sleepStart = p.getLong("sleepStart", 0L)
    remShiftStart = p.getBoolean("remShiftStart", true)
    remShiftLeadMin = p.getInt("remShiftLead", 60)
    remCheckIn = p.getBoolean("remCheckIn", true)
    remCheckInMin = p.getInt("remCheckInMin", 18 * 60)
    remForgot = p.getBoolean("remForgot", true)
    remSmart = p.getBoolean("remSmart", true)
    lastFired = p.getLong("lastFired", 0L)

    smartOff.clear()
    (p.getString("smartOff", "") ?: "").split(",").filter { it.isNotBlank() }.forEach { smartOff.add(it) }

    customPatterns.clear()
    runCatching {
      val a = JSONArray(p.getString("patterns", "[]") ?: "[]")
      for (i in 0 until a.length()) {
        val o = a.getJSONObject(i)
        val sq = o.getJSONArray("seq")
        val nm = o.getString("name")
        customPatterns.add(Pattern(o.getString("id"), nm, nm, "", "", List(sq.length()) { sq.getString(it) }, premium = false, custom = true))
      }
    }

    partnerName = p.getString("partnerName", "") ?: ""
    partnerStart = p.getString("partnerStart", "") ?: ""
    partnerSeq = p.getString("partnerSeq", "") ?: ""
    partnerUpdated = p.getLong("partnerUpdated", 0L)
    partnerTypes.clear()
    runCatching { partnerTypes.addAll(typesFromJson(JSONArray(p.getString("partnerTypes", "[]") ?: "[]"))) }

    types.clear()
    val tj = p.getString("types", null)
    if (tj == null) types.addAll(Builtins.defaultTypes()) else runCatching { types.addAll(typesFromJson(JSONArray(tj))) }
    if (types.none { it.id == Builtins.OFF }) types.addAll(Builtins.defaultTypes().filter { d -> types.none { it.id == d.id } })

    shifts.clear()
    runCatching {
      val o = JSONObject(p.getString("shifts", "{}") ?: "{}")
      o.keys().forEach { k -> shifts[k] = o.getString(k) }
    }

    sleeps.clear()
    runCatching {
      val a = JSONArray(p.getString("sleeps", "[]") ?: "[]")
      for (i in 0 until a.length()) {
        val o = a.getJSONObject(i)
        sleeps.add(SleepEntry(o.getLong("id"), o.getLong("start"), o.getLong("end"), o.getInt("q"), o.getBoolean("nap")))
      }
    }

    energies.clear()
    runCatching {
      val a = JSONArray(p.getString("energies", "[]") ?: "[]")
      for (i in 0 until a.length()) {
        val o = a.getJSONObject(i)
        val tg = o.optJSONArray("tags") ?: JSONArray()
        energies.add(
          EnergyEntry(o.getLong("id"), o.getLong("time"), o.getInt("level"), List(tg.length()) { tg.getString(it) }, o.optString("note", "")),
        )
      }
    }
  }

  private fun typesFromJson(a: JSONArray): List<ShiftType> = List(a.length()) { i ->
    val o = a.getJSONObject(i)
    ShiftType(
      o.getString("id"), o.getString("nameTr"), o.getString("nameEn"),
      o.getString("letterTr"), o.getString("letterEn"), o.getLong("color"),
      o.getInt("startMin"), o.getInt("endMin"), o.getBoolean("isOff"), o.optBoolean("builtIn", false),
    )
  }

  private fun typesToJson(l: List<ShiftType>): JSONArray {
    val ta = JSONArray()
    l.forEach {
      ta.put(
        JSONObject().put("id", it.id).put("nameTr", it.nameTr).put("nameEn", it.nameEn)
          .put("letterTr", it.letterTr).put("letterEn", it.letterEn).put("color", it.color)
          .put("startMin", it.startMin).put("endMin", it.endMin).put("isOff", it.isOff).put("builtIn", it.builtIn),
      )
    }
    return ta
  }

  fun save() {
    val p = prefs ?: return
    val ta = typesToJson(types)
    val pa = JSONArray()
    customPatterns.forEach { pa.put(JSONObject().put("id", it.id).put("name", it.nameTr).put("seq", JSONArray(it.seq))) }
    val so = JSONObject()
    shifts.forEach { (k, v) -> so.put(k, v) }
    val sa = JSONArray()
    sleeps.forEach { sa.put(JSONObject().put("id", it.id).put("start", it.start).put("end", it.end).put("q", it.quality).put("nap", it.nap)) }
    val ea = JSONArray()
    energies.forEach {
      ea.put(JSONObject().put("id", it.id).put("time", it.time).put("level", it.level).put("tags", JSONArray(it.tags)).put("note", it.note))
    }
    p.edit()
      .putString("lang", lang).putString("theme", theme).putBoolean("onboarded", onboarded)
      .putString("name", name).putString("role", role).putString("jobTitle", jobTitle).putString("workType", workType).putInt("sleepGoal", sleepGoalMin)
      .putBoolean("premium", premium).putLong("sleepStart", sleepStart)
      .putBoolean("remShiftStart", remShiftStart).putInt("remShiftLead", remShiftLeadMin)
      .putBoolean("remCheckIn", remCheckIn).putInt("remCheckInMin", remCheckInMin)
      .putBoolean("remForgot", remForgot).putBoolean("remSmart", remSmart).putLong("lastFired", lastFired)
      .putString("types", ta.toString()).putString("shifts", so.toString())
      .putString("sleeps", sa.toString()).putString("energies", ea.toString())
      .putString("smartOff", smartOff.joinToString(",")).putString("patterns", pa.toString())
      .putString("partnerName", partnerName).putString("partnerStart", partnerStart)
      .putString("partnerSeq", partnerSeq).putLong("partnerUpdated", partnerUpdated)
      .putString("partnerTypes", typesToJson(partnerTypes).toString())
      .apply()
  }

  /** Kaydet ve hatırlatıcıları yeniden kur. */
  fun changed() {
    save()
    runCatching { Reminders.reschedule(app) }
  }

  // ---------- Yardımcılar ----------
  fun key(d: LocalDate) = d.toString()
  fun today(): LocalDate = LocalDate.now(zone)
  fun toDate(ms: Long): LocalDate = Instant.ofEpochMilli(ms).atZone(zone).toLocalDate()
  fun toLdt(ms: Long): LocalDateTime = Instant.ofEpochMilli(ms).atZone(zone).toLocalDateTime()
  fun toMs(t: LocalDateTime): Long = t.atZone(zone).toInstant().toEpochMilli()

  fun typeById(id: String?): ShiftType? = if (id == null) null else types.firstOrNull { it.id == id }
  fun shiftOn(d: LocalDate): ShiftType? = typeById(shifts[key(d)])

  fun window(d: LocalDate, t: ShiftType): ShiftInstance {
    val s = d.atStartOfDay().plusMinutes(t.startMin.toLong())
    return ShiftInstance(d, t, s, s.plusMinutes(t.durationMin.toLong()))
  }

  fun workOn(d: LocalDate): ShiftInstance? = shiftOn(d)?.takeIf { !it.isOff }?.let { window(d, it) }

  /** Şu an süren ya da sıradaki çalışma vardiyası. */
  fun nextShift(now: LocalDateTime = LocalDateTime.now(zone)): ShiftInstance? {
    val t = now.toLocalDate()
    for (i in -1..120) {
      val w = workOn(t.plusDays(i.toLong())) ?: continue
      if (w.end.isAfter(now)) return w
    }
    return null
  }

  fun lastEndedShift(now: LocalDateTime = LocalDateTime.now(zone)): ShiftInstance? {
    val t = now.toLocalDate()
    for (i in 0..3) {
      val w = workOn(t.minusDays(i.toLong())) ?: continue
      if (!w.end.isAfter(now)) return w
    }
    return null
  }

  fun setShift(d: LocalDate, typeId: String?) {
    if (typeId == null) shifts.remove(key(d)) else shifts[key(d)] = typeId
  }

  fun applyPattern(p: Pattern, start: LocalDate, offset: Int, days: Int) {
    for (i in 0 until days) {
      shifts[key(start.plusDays(i.toLong()))] = p.seq[(i + offset) % p.seq.size]
    }
    changed()
  }

  fun upsertType(t: ShiftType) {
    val i = types.indexOfFirst { it.id == t.id }
    if (i >= 0) types[i] = t else types.add(t)
    changed()
  }

  fun deleteType(id: String) {
    types.removeAll { it.id == id && !it.builtIn }
    shifts.keys.filter { shifts[it] == id }.forEach { shifts.remove(it) }
    changed()
  }

  fun customCount() = types.count { !it.builtIn }

  // ---------- Düzen şablonları ----------
  fun allPatterns(): List<Pattern> = customPatterns.toList() + Builtins.patterns

  fun savePattern(name: String, seq: List<String>) {
    customPatterns.add(0, Pattern("P" + System.currentTimeMillis(), name, name, "", "", seq, premium = false, custom = true))
    changed()
  }

  fun deletePattern(id: String) {
    customPatterns.removeAll { it.id == id }
    changed()
  }

  fun smartOn(kind: String) = kind !in smartOff

  fun setSmart(kind: String, on: Boolean) {
    if (on) smartOff.remove(kind) else if (kind !in smartOff) smartOff.add(kind)
    changed()
  }

  // ---------- Partner paylaşımı ----------
  /**
   * Paylaşım kodu: yalnızca vardiya türleri + bugünden itibaren [days] günlük vardiya dizisi.
   * JSON → Deflate → Base64 (URL güvenli). Uyku/enerji verisi KESİNLİKLE eklenmez.
   */
  fun makeShareCode(days: Int, sender: String): String {
    val start = today()
    val used = (0 until days).mapNotNull { shifts[key(start.plusDays(it.toLong()))] }.distinct()
    val ts = types.filter { it.id in used }
    val seq = StringBuilder()
    for (i in 0 until days) {
      val id = shifts[key(start.plusDays(i.toLong()))]
      val idx = ts.indexOfFirst { it.id == id }
      seq.append(if (idx < 0) '.' else ('a' + idx))
    }
    val o = JSONObject().put("v", 1).put("n", sender).put("s", start.toString()).put("t", typesToJson(ts)).put("d", seq.toString())
    val raw = o.toString().toByteArray(Charsets.UTF_8)
    val def = Deflater(Deflater.BEST_COMPRESSION)
    def.setInput(raw)
    def.finish()
    val out = ByteArrayOutputStream()
    val buf = ByteArray(1024)
    while (!def.finished()) out.write(buf, 0, def.deflate(buf))
    def.end()
    return "NB1" + Base64.encodeToString(out.toByteArray(), Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
  }

  /** Metnin içinden kodu bulup içe aktarır. Başarılıysa paylaşan kişinin adı döner. */
  fun importShareCode(text: String): String? = runCatching {
    val m = Regex("NB1[A-Za-z0-9_-]{8,}").find(text) ?: return null
    val bytes = Base64.decode(m.value.removePrefix("NB1"), Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    val inf = Inflater()
    inf.setInput(bytes)
    val out = ByteArrayOutputStream()
    val buf = ByteArray(1024)
    var guard = 0
    while (!inf.finished() && guard++ < 1000) {
      val n = inf.inflate(buf)
      if (n == 0 && (inf.needsInput() || inf.needsDictionary())) break
      out.write(buf, 0, n)
    }
    inf.end()
    val o = JSONObject(out.toString("UTF-8"))
    LocalDate.parse(o.getString("s"))
    partnerTypes.clear()
    partnerTypes.addAll(typesFromJson(o.getJSONArray("t")))
    partnerName = o.optString("n", "").ifBlank { "Partner" }
    partnerStart = o.getString("s")
    partnerSeq = o.getString("d")
    partnerUpdated = System.currentTimeMillis()
    save()
    partnerName
  }.getOrNull()

  fun removePartner() {
    partnerName = ""
    partnerStart = ""
    partnerSeq = ""
    partnerUpdated = 0L
    partnerTypes.clear()
    save()
  }

  val hasPartner: Boolean get() = partnerSeq.isNotEmpty()

  fun partnerShiftOn(d: LocalDate): ShiftType? {
    if (!hasPartner) return null
    val st = runCatching { LocalDate.parse(partnerStart) }.getOrNull() ?: return null
    val i = java.time.temporal.ChronoUnit.DAYS.between(st, d)
    if (i < 0 || i >= partnerSeq.length) return null
    val c = partnerSeq[i.toInt()]
    if (c == '.') return null
    return partnerTypes.getOrNull(c - 'a')
  }

  fun partnerLastDay(): LocalDate? =
    runCatching { LocalDate.parse(partnerStart).plusDays((partnerSeq.length - 1).toLong()) }.getOrNull()

  // ---------- Uyku ----------
  fun startSleep() {
    sleepStart = System.currentTimeMillis()
    changed()
  }

  fun stopSleep(quality: Int, nap: Boolean) {
    val s = sleepStart
    if (s == 0L) return
    val e = System.currentTimeMillis()
    if (e - s >= 5 * 60_000L) sleeps.add(SleepEntry(e, s, e, quality, nap))
    sleepStart = 0L
    changed()
  }

  fun cancelSleep() {
    sleepStart = 0L
    changed()
  }

  fun addSleep(start: Long, end: Long, quality: Int, nap: Boolean) {
    sleeps.add(SleepEntry(System.currentTimeMillis(), start, end, quality, nap))
    changed()
  }

  fun deleteSleep(id: Long) {
    sleeps.removeAll { it.id == id }
    changed()
  }

  fun addEnergy(level: Int, tags: List<String>, note: String) {
    val now = System.currentTimeMillis()
    energies.add(EnergyEntry(now, now, level, tags, note))
    changed()
  }

  /** Uyku, bittiği güne yazılır (gece vardiyasından sonraki gündüz uykusu doğru güne düşer). */
  fun sleepMinOn(d: LocalDate): Int = sleeps.filter { toDate(it.end) == d }.sumOf { it.minutes }
  fun hasSleep(d: LocalDate): Boolean = sleeps.any { toDate(it.end) == d }

  /** Temel uyku borcu: son 7 gün, veri olmayan günler sayılmaz. (borç dk, veri olan gün) */
  fun debt7(today: LocalDate = today()): Pair<Int, Int> {
    var debt = 0
    var days = 0
    for (i in 0..6) {
      val d = today.minusDays(i.toLong())
      if (!hasSleep(d)) continue
      days++
      debt += (sleepMinOn(d) - sleepGoalMin).let { if (it < 0) -it else 0 }
    }
    return debt to days
  }

  /** Gelişmiş borç: 14 gün, fazla uyku günde en çok 2 saat borç kapatır. */
  fun debt14(today: LocalDate = today()): Int {
    var debt = 0
    for (i in 13 downTo 0) {
      val d = today.minusDays(i.toLong())
      if (!hasSleep(d)) continue
      val diff = sleepGoalMin - sleepMinOn(d)
      debt = if (diff > 0) debt + diff else (debt - minOf(-diff, 120)).coerceAtLeast(0)
    }
    return debt
  }

  fun energyOn(d: LocalDate): Float? {
    val l = energies.filter { toDate(it.time) == d }
    return if (l.isEmpty()) null else l.map { it.level }.average().toFloat()
  }

  /** Gece vardiyasının bittiği günler. */
  fun isPostNight(d: LocalDate): Boolean {
    val a = workOn(d.minusDays(1))
    val b = workOn(d)
    return (a != null && a.type.isNight && a.end.toLocalDate() == d) ||
      (b != null && b.type.isNight && b.end.toLocalDate() == d)
  }

  /** Uyku günü kategorisi: vardiyanın uykuyu nasıl etkilediğini karşılaştırmak için. */
  fun sleepCategory(d: LocalDate): String {
    val w = workOn(d)
    return when {
      isPostNight(d) -> "postNight"
      w != null && w.type.isNight -> "preNight"
      w != null -> "work"
      else -> "off"
    }
  }

  data class SleepStat(val days: Int, val avgMin: Int, val avgQuality: Float)

  /** Son [range] gün için kategori bazında ortalama uyku süresi ve kalitesi. */
  fun sleepByCategory(range: Int = 60, today: LocalDate = today()): Map<String, SleepStat> {
    val m = mutableMapOf<String, MutableList<LocalDate>>()
    for (i in 0 until range) {
      val d = today.minusDays(i.toLong())
      if (!hasSleep(d)) continue
      m.getOrPut(sleepCategory(d)) { mutableListOf() }.add(d)
    }
    return m.mapValues { (_, ds) ->
      val q = sleeps.filter { toDate(it.end) in ds }.map { it.quality }
      SleepStat(ds.size, ds.map { sleepMinOn(it) }.average().toInt(), if (q.isEmpty()) 0f else q.average().toFloat())
    }
  }

  fun exportCsv(): String {
    val sb = StringBuilder()
    sb.append("type,date,start,end,value,extra\n")
    shifts.keys.sorted().forEach { k ->
      val t = typeById(shifts[k]) ?: return@forEach
      sb.append("shift,$k,,,${t.nameEn},\n")
    }
    sleeps.sortedBy { it.start }.forEach {
      sb.append("sleep,${toDate(it.end)},${toLdt(it.start)},${toLdt(it.end)},${it.minutes},q=${it.quality}${if (it.nap) ";nap" else ""}\n")
    }
    energies.sortedBy { it.time }.forEach {
      val note = it.note.replace("\"", "'").replace("\n", " ")
      sb.append("energy,${toDate(it.time)},${toLdt(it.time)},,${it.level},\"${it.tags.joinToString(";")} $note\"\n")
    }
    return sb.toString()
  }

  fun clearAll() {
    prefs?.edit()?.clear()?.apply()
    prefs?.let { load(it) }
    runCatching { Reminders.reschedule(app) }
  }
}
