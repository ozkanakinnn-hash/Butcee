package akinai.minikgurme.util

import akinai.minikgurme.data.local.deleteDatabaseFile
import akinai.minikgurme.shared.KeyValueStore
import akinai.minikgurme.shared.currentTimeMillis
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Birden fazla bebek profili. Her bebeğin verisi kendi veritabanı dosyasında tutulur
 * ("main" → minik_gurme_db, diğerleri → minik_gurme_db_<id>); geçişte ekran yeniden kurulur.
 * İlk bebek ücretsiz; ek bebekler Minik Gurme Plus özelliğidir.
 */
object Babies {
    private const val KEY_LIST = "babies"
    private const val KEY_ACTIVE = "active_baby"
    const val MAIN = "main"

    data class Baby(val id: String, val name: String)

    private val prefs by lazy { KeyValueStore("app_prefs") }

    fun all(): List<Baby> {
        val raw = prefs.getString(KEY_LIST, null) ?: return listOf(Baby(MAIN, ""))
        val list = runCatching {
            Json.parseToJsonElement(raw).jsonArray.map { e ->
                val o = e.jsonObject
                Baby(o["id"]!!.jsonPrimitive.content, o["name"]?.jsonPrimitive?.content ?: "")
            }
        }.getOrDefault(emptyList())
        return list.ifEmpty { listOf(Baby(MAIN, "")) }
    }

    private fun save(list: List<Baby>) {
        val arr = JsonArray(list.map { JsonObject(mapOf("id" to JsonPrimitive(it.id), "name" to JsonPrimitive(it.name))) })
        prefs.putString(KEY_LIST, arr.toString())
    }

    fun activeId(): String = prefs.getString(KEY_ACTIVE, MAIN) ?: MAIN

    fun setActive(id: String) { prefs.putString(KEY_ACTIVE, id) }

    fun dbName(id: String): String = if (id == MAIN) "minik_gurme_db" else "minik_gurme_db_$id"

    fun onboardedKey(id: String): String = if (id == MAIN) "onboarding_done" else "onboarding_done_$id"

    fun isOnboarded(id: String = activeId()) = prefs.getBoolean(onboardedKey(id), false)
    fun setOnboarded(id: String = activeId()) = prefs.putBoolean(onboardedKey(id), true)

    fun setName(id: String, name: String) {
        val list = all().toMutableList()
        val i = list.indexOfFirst { it.id == id }
        if (i >= 0) list[i] = Baby(id, name) else list.add(Baby(id, name))
        save(list)
    }

    /** Yeni bebek oluşturur ve etkin yapar; ilk açılış ekranı onun için gösterilir. */
    fun addNew(): String {
        val id = "b" + currentTimeMillis().toString(36)
        save(all() + Baby(id, ""))
        setActive(id)
        return id
    }

    /** Bebeği ve tüm verisini siler (ana profil silinemez). */
    fun delete(id: String) {
        if (id == MAIN) return
        save(all().filterNot { it.id == id })
        deleteDatabaseFile(dbName(id))
        prefs.remove(onboardedKey(id)); prefs.remove("weekly_plan_${dbName(id)}")
        if (activeId() == id) setActive(MAIN)
    }
}
