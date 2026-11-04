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
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Locale

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
  var role by mutableStateOf("other")
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
    role = p.getString("role", "other") ?: "other"
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

    types.clear()
    val tj = p.getString("types", null)
    if (tj == null) types.addAll(Builtins.defaultTypes()) else runCatching {
      val a = JSONArray(tj)
      for (i in 0 until a.length()) {
        val o = a.getJSONObject(i)
        types.add(
          ShiftType(
            o.getString("id"), o.getString("nameTr"), o.getString("nameEn"),
            o.getString("letterTr"), o.getString("letterEn"), o.getLong("color"),
            o.getInt("startMin"), o.getInt("endMin"), o.getBoolean("isOff"), o.getBoolean("builtIn"),
          ),
        )
      }
    }
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

  fun save() {
    val p = prefs ?: return
    val ta = JSONArray()
    types.forEach {
      ta.put(
        JSONObject().put("id", it.id).put("nameTr", it.nameTr).put("nameEn", it.nameEn)
          .put("letterTr", it.letterTr).put("letterEn", it.letterEn).put("color", it.color)
          .put("startMin", it.startMin).put("endMin", it.endMin).put("isOff", it.isOff).put("builtIn", it.builtIn),
      )
    }
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
      .putString("name", name).putString("role", role).putInt("sleepGoal", sleepGoalMin)
      .putBoolean("premium", premium).putLong("sleepStart", sleepStart)
      .putBoolean("remShiftStart", remShiftStart).putInt("remShiftLead", remShiftLeadMin)
      .putBoolean("remCheckIn", remCheckIn).putInt("remCheckInMin", remCheckInMin)
      .putBoolean("remForgot", remForgot).putBoolean("remSmart", remSmart).putLong("lastFired", lastFired)
      .putString("types", ta.toString()).putString("shifts", so.toString())
      .putString("sleeps", sa.toString()).putString("energies", ea.toString())
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
