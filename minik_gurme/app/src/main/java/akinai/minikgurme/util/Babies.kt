package akinai.minikgurme.util

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * Birden fazla bebek profili. Her bebeğin verisi kendi veritabanı dosyasında tutulur
 * ("main" → minik_gurme_db, diğerleri → minik_gurme_db_<id>); geçişte etkinlik yeniden başlatılır.
 * İlk bebek ücretsiz; ek bebekler Minik Gurme Plus özelliğidir.
 */
object Babies {
    private const val PREFS = "app_prefs"
    private const val KEY_LIST = "babies"
    private const val KEY_ACTIVE = "active_baby"
    const val MAIN = "main"

    data class Baby(val id: String, val name: String)

    private fun prefs(c: Context) = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun all(c: Context): List<Baby> {
        val raw = prefs(c).getString(KEY_LIST, null) ?: return listOf(Baby(MAIN, ""))
        val arr = JSONArray(raw)
        val list = (0 until arr.length()).map { arr.getJSONObject(it).let { o -> Baby(o.getString("id"), o.optString("name")) } }
        return list.ifEmpty { listOf(Baby(MAIN, "")) }
    }

    private fun save(c: Context, list: List<Baby>) {
        val arr = JSONArray(); list.forEach { arr.put(JSONObject().put("id", it.id).put("name", it.name)) }
        prefs(c).edit().putString(KEY_LIST, arr.toString()).apply()
    }

    fun activeId(c: Context): String = prefs(c).getString(KEY_ACTIVE, MAIN) ?: MAIN

    fun setActive(c: Context, id: String) { prefs(c).edit().putString(KEY_ACTIVE, id).commit() }

    fun dbName(id: String): String = if (id == MAIN) "minik_gurme_db" else "minik_gurme_db_$id"

    fun onboardedKey(id: String): String = if (id == MAIN) "onboarding_done" else "onboarding_done_$id"

    fun setName(c: Context, id: String, name: String) {
        val list = all(c).toMutableList()
        val i = list.indexOfFirst { it.id == id }
        if (i >= 0) list[i] = Baby(id, name) else list.add(Baby(id, name))
        save(c, list)
    }

    /** Yeni bebek oluşturur ve etkin yapar; ilk açılış ekranı onun için gösterilir. */
    fun addNew(c: Context): String {
        val id = "b" + System.currentTimeMillis().toString(36)
        val list = all(c).toMutableList().apply { add(Baby(id, "")) }
        save(c, list); setActive(c, id)
        return id
    }

    /** Bebeği ve tüm verisini siler (ana profil silinemez). */
    fun delete(c: Context, id: String) {
        if (id == MAIN) return
        save(c, all(c).filterNot { it.id == id })
        c.deleteDatabase(dbName(id))
        prefs(c).edit().remove(onboardedKey(id)).remove("weekly_plan_${dbName(id)}").apply()
        if (activeId(c) == id) setActive(c, MAIN)
    }
}
