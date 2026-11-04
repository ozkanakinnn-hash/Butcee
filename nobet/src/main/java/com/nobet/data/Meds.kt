package com.nobet.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalDateTime

/** İlaç: adı, dozu, günlük saatleri (gün içi dakika), haftanın günleri (1 = Pzt … 7 = Paz), zorunlu mu. */
data class Med(
  val id: String,
  val name: String,
  val dose: String,
  val times: List<Int>,
  val days: List<Int>,
  val critical: Boolean,
)

/** Belirli bir gündeki tek doz. */
data class Dose(val date: LocalDate, val med: Med, val minute: Int) {
  val key: String get() = "${date}|${med.id}|$minute"
  val at: LocalDateTime get() = date.atStartOfDay().plusMinutes(minute.toLong())
}

/**
 * İlaç takibi + isteğe bağlı sağlık profili. Tüm veriler yalnızca bu cihazda tutulur,
 * partner paylaşımına ve dışa aktarmaya girmez.
 */
object Meds {
  /** Bildirim onaylanmazsa kaç dakikada bir ertelenir ve kaç ertelemeden sonra kritik uyarı verilir. */
  const val SNOOZE_MIN = 5
  const val MAX_SNOOZES = 3

  private var prefs: SharedPreferences? = null

  // Sağlık profili (hepsi isteğe bağlı)
  var forWhom by mutableStateOf("") // "" · self · family · patient
  var personName by mutableStateOf("")
  var chronic by mutableStateOf("")
  var age by mutableStateOf("")
  var height by mutableStateOf("")
  var weight by mutableStateOf("")

  val meds = mutableStateListOf<Med>()
  val taken = mutableStateMapOf<String, Long>() // doz anahtarı -> alınma zamanı
  val dismissed = mutableStateListOf<String>() // kapatılan kritik uyarılar
  var lastFired by mutableLongStateOf(0L)

  fun init(ctx: Context) {
    if (prefs != null) return
    val p = ctx.applicationContext.getSharedPreferences("nobet_meds", Context.MODE_PRIVATE)
    prefs = p
    load(p)
  }

  private fun load(p: SharedPreferences) {
    forWhom = p.getString("forWhom", "") ?: ""
    personName = p.getString("personName", "") ?: ""
    chronic = p.getString("chronic", "") ?: ""
    age = p.getString("age", "") ?: ""
    height = p.getString("height", "") ?: ""
    weight = p.getString("weight", "") ?: ""
    lastFired = p.getLong("lastFired", 0L)
    meds.clear()
    runCatching {
      val a = JSONArray(p.getString("meds", "[]") ?: "[]")
      for (i in 0 until a.length()) {
        val o = a.getJSONObject(i)
        val ts = o.getJSONArray("times")
        val ds = o.getJSONArray("days")
        meds.add(
          Med(
            o.getString("id"), o.getString("name"), o.optString("dose", ""),
            List(ts.length()) { ts.getInt(it) }, List(ds.length()) { ds.getInt(it) }, o.optBoolean("critical", false),
          ),
        )
      }
    }
    taken.clear()
    val cutoff = LocalDate.now().minusDays(60).toString()
    runCatching {
      val o = JSONObject(p.getString("taken", "{}") ?: "{}")
      o.keys().forEach { k -> if (k.substringBefore('|') >= cutoff) taken[k] = o.getLong(k) }
    }
    dismissed.clear()
    (p.getString("dismissed", "") ?: "").split(",").filter { it.isNotBlank() && it.substringBefore('|') >= cutoff }.forEach { dismissed.add(it) }
  }

  fun save() {
    val p = prefs ?: return
    val a = JSONArray()
    meds.forEach {
      a.put(
        JSONObject().put("id", it.id).put("name", it.name).put("dose", it.dose)
          .put("times", JSONArray(it.times)).put("days", JSONArray(it.days)).put("critical", it.critical),
      )
    }
    val tk = JSONObject()
    taken.forEach { (k, v) -> tk.put(k, v) }
    p.edit()
      .putString("forWhom", forWhom).putString("personName", personName).putString("chronic", chronic)
      .putString("age", age).putString("height", height).putString("weight", weight)
      .putLong("lastFired", lastFired)
      .putString("meds", a.toString()).putString("taken", tk.toString())
      .putString("dismissed", dismissed.joinToString(","))
      .apply()
  }

  fun clearAll() {
    prefs?.edit()?.clear()?.apply()
    prefs?.let { load(it) }
  }

  // ---------- İlaç yönetimi ----------
  fun upsert(m: Med) {
    val i = meds.indexOfFirst { it.id == m.id }
    if (i >= 0) meds[i] = m else meds.add(m)
    save()
  }

  fun delete(id: String) {
    meds.removeAll { it.id == id }
    save()
  }

  fun dosesOn(d: LocalDate): List<Dose> =
    meds.filter { d.dayOfWeek.value in it.days }
      .flatMap { m -> m.times.map { Dose(d, m, it) } }
      .sortedBy { it.minute }

  fun isTaken(d: Dose) = taken.containsKey(d.key)

  fun markTaken(key: String) {
    taken[key] = System.currentTimeMillis()
    save()
  }

  fun unmarkTaken(key: String) {
    taken.remove(key)
    save()
  }

  fun dismiss(key: String) {
    if (key !in dismissed) dismissed.add(key)
    save()
  }

  /**
   * Kritik uyarı gereken dozlar: zorunlu, alınmamış, 3 erteleme süresi dolmuş (saat + 15 dk),
   * son 24 saat içinde ve kullanıcı uyarıyı kapatmamış.
   */
  fun criticalMissed(now: LocalDateTime = LocalDateTime.now()): List<Dose> {
    val out = mutableListOf<Dose>()
    for (i in -1..0) {
      dosesOn(now.toLocalDate().plusDays(i.toLong())).forEach { d ->
        val due = d.at.plusMinutes((SNOOZE_MIN * MAX_SNOOZES).toLong())
        if (d.med.critical && !isTaken(d) && d.key !in dismissed && !due.isAfter(now) &&
          java.time.Duration.between(due, now).toHours() < 24
        ) out += d
      }
    }
    return out
  }

  /** "Kimin için" bilgisine göre ilaç paneli başlığı. */
  fun ownerLabel(t: (String, String) -> String): String = when {
    forWhom != "self" && forWhom.isNotEmpty() && personName.isNotBlank() -> t("${personName.trim()} için ilaçlar", "Medicines for ${personName.trim()}")
    else -> t("İlaçlarım", "My medicines")
  }
}
