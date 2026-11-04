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
  /** Genellikle ihtiyaç duyulan uyku (isteğe bağlı) — yalnızca toparlanma puanında Q1 için kullanılır. */
  var sleepGoalMin by mutableIntStateOf(450)
  var premium by mutableStateOf(false)
  var showHolidays by mutableStateOf(true)
  var leaveAllowance by mutableStateOf(14f) // yıllık izin hakkı (gün)
  var leaveCarry by mutableStateOf(0f) // önceki yıldan devreden (gün)
  // Araçlar → Saatlik Ücret Hesaplayıcı (isteğe bağlı; yalnızca cihazda, paylaşılmaz/dışa aktarılmaz)
  var toolSalary by mutableStateOf("")
  var toolHours by mutableStateOf("")

  // Hatırlatıcılar
  var remShiftStart by mutableStateOf(true)
  var remShiftLeadMin by mutableIntStateOf(60)
  var remCheckIn by mutableStateOf(true)
  var remCheckInMin by mutableIntStateOf(18 * 60)
  var remRecovery by mutableStateOf(true) // vardiya sonrası toparlanma kontrolü hatırlatması
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
  val checkins = mutableStateMapOf<String, CheckIn>() // vardiya günü -> toparlanma kontrolü
  val notes = mutableStateMapOf<String, JournalNote>() // vardiya günü -> günlük notu
  val leaves = mutableStateMapOf<String, String>() // gün -> annual / annualHalf / sick / unpaid / other
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
    showHolidays = p.getBoolean("showHolidays", true)
    leaveAllowance = p.getFloat("leaveAllowance", 14f)
    leaveCarry = p.getFloat("leaveCarry", 0f)
    toolSalary = p.getString("toolSalary", "") ?: ""
    toolHours = p.getString("toolHours", "") ?: ""
    remShiftStart = p.getBoolean("remShiftStart", true)
    remShiftLeadMin = p.getInt("remShiftLead", 60)
    remCheckIn = p.getBoolean("remCheckIn", true)
    remCheckInMin = p.getInt("remCheckInMin", 18 * 60)
    remRecovery = p.getBoolean("remRecovery", true)
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

    checkins.clear()
    runCatching {
      val o = JSONObject(p.getString("checkins", "{}") ?: "{}")
      o.keys().forEach { k ->
        val c = o.getJSONObject(k)
        checkins[k] = CheckIn(k, c.getInt("smooth"), c.optString("ss", ""), c.optInt("sm", 0), c.optInt("q", 0), c.optInt("e", 0), c.optLong("at", 0L))
      }
    }
    notes.clear()
    runCatching {
      val o = JSONObject(p.getString("notes", "{}") ?: "{}")
      o.keys().forEach { k ->
        val n = o.getJSONObject(k)
        val tg = n.optJSONArray("tags") ?: JSONArray()
        notes[k] = JournalNote(n.optString("text", ""), List(tg.length()) { tg.getString(it) })
      }
    }
    leaves.clear()
    runCatching {
      val o = JSONObject(p.getString("leaves", "{}") ?: "{}")
      o.keys().forEach { k -> leaves[k] = o.getString(k) }
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
    val co = JSONObject()
    checkins.forEach { (k, c) ->
      co.put(k, JSONObject().put("smooth", c.smooth).put("ss", c.sleepStatus).put("sm", c.sleepMin).put("q", c.quality).put("e", c.energy).put("at", c.at))
    }
    val no = JSONObject()
    notes.forEach { (k, n) -> no.put(k, JSONObject().put("text", n.text).put("tags", JSONArray(n.tags))) }
    val lo = JSONObject()
    leaves.forEach { (k, v) -> lo.put(k, v) }
    val ea = JSONArray()
    energies.forEach {
      ea.put(JSONObject().put("id", it.id).put("time", it.time).put("level", it.level).put("tags", JSONArray(it.tags)).put("note", it.note))
    }
    p.edit()
      .putString("lang", lang).putString("theme", theme).putBoolean("onboarded", onboarded)
      .putString("name", name).putString("role", role).putString("jobTitle", jobTitle).putString("workType", workType).putInt("sleepGoal", sleepGoalMin)
      .putBoolean("premium", premium).putBoolean("showHolidays", showHolidays)
      .putFloat("leaveAllowance", leaveAllowance).putFloat("leaveCarry", leaveCarry)
      .putString("toolSalary", toolSalary).putString("toolHours", toolHours)
      .putBoolean("remShiftStart", remShiftStart).putInt("remShiftLead", remShiftLeadMin)
      .putBoolean("remCheckIn", remCheckIn).putInt("remCheckInMin", remCheckInMin)
      .putBoolean("remRecovery", remRecovery).putBoolean("remSmart", remSmart).putLong("lastFired", lastFired)
      .putString("types", ta.toString()).putString("shifts", so.toString())
      .putString("checkins", co.toString()).putString("notes", no.toString()).putString("leaves", lo.toString())
      .putString("energies", ea.toString())
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

  /** Çalışma vardiyası (izin türü ya da tam gün izinli günler hariç). */
  fun workOn(d: LocalDate): ShiftInstance? {
    val l = leaves[key(d)]
    if (l != null && l != "annualHalf") return null
    return shiftOn(d)?.takeIf { !it.isOff }?.let { window(d, it) }
  }

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

  // ---------- Toparlanma kontrolü ----------
  fun saveCheckIn(c: CheckIn) {
    checkins[c.shiftDate] = c
    changed()
  }

  fun deleteCheckIn(d: LocalDate) {
    checkins.remove(key(d))
    changed()
  }

  // ---------- Vardiya günlüğü ----------
  fun saveNote(d: LocalDate, text: String, tags: List<String>) {
    if (text.isBlank() && tags.isEmpty()) notes.remove(key(d)) else notes[key(d)] = JournalNote(text.trim().take(280), tags)
    save()
  }

  // ---------- İzin ----------
  fun setLeave(d: LocalDate, kind: String?) {
    if (kind == null) leaves.remove(key(d)) else leaves[key(d)] = kind
    changed()
  }

  /** Bu yıl kullanılan yıllık izin (yarım gün 0,5). İzin günü, düzen izni ya da resmî tatile denk geliyorsa düşülmez. */
  fun leaveUsed(year: Int = today().year): Float = leaves.entries.sumOf { (k, v) ->
    val d = runCatching { LocalDate.parse(k) }.getOrNull() ?: return@sumOf 0.0
    if (d.year != year) return@sumOf 0.0
    val off = shiftOn(d)?.isOff == true
    val hol = Holidays.on(d)?.let { !it.half } == true
    when {
      v == "annual" && !off && !hol -> 1.0
      v == "annualHalf" && !off && !hol -> 0.5
      else -> 0.0
    }
  }.toFloat()

  fun leaveRemaining(): Float = leaveAllowance + leaveCarry - leaveUsed()

  fun addEnergy(level: Int, tags: List<String>, note: String) {
    val now = System.currentTimeMillis()
    energies.add(EnergyEntry(now, now, level, tags, note))
    changed()
  }

  fun energyOn(d: LocalDate): Float? {
    val l = energies.filter { toDate(it.time) == d }
    return if (l.isEmpty()) null else l.map { it.level }.average().toFloat()
  }

  fun exportCsv(): String {
    val sb = StringBuilder()
    sb.append("type,date,start,end,value,extra\n")
    shifts.keys.sorted().forEach { k ->
      val t = typeById(shifts[k]) ?: return@forEach
      sb.append("shift,$k,,,${t.nameEn},\n")
    }
    checkins.keys.sorted().forEach { k ->
      val c = checkins[k] ?: return@forEach
      sb.append("recovery,$k,,,${Recovery.score(c)},smooth=${c.smooth};sleep=${c.sleepStatus}:${c.sleepMin};q=${c.quality};energy=${c.energy}\n")
    }
    leaves.keys.sorted().forEach { k -> sb.append("leave,$k,,,${leaves[k]},\n") }
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
