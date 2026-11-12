package akinai.minikgurme.util

import akinai.minikgurme.data.local.AppDatabase
import akinai.minikgurme.shared.currentTimeMillis
import androidx.room.execSQL
import androidx.room.immediateTransaction
import androidx.room.useReaderConnection
import androidx.room.useWriterConnection
import androidx.sqlite.SQLITE_DATA_FLOAT
import androidx.sqlite.SQLITE_DATA_INTEGER
import androidx.sqlite.SQLITE_DATA_NULL
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.longOrNull

/**
 * Yedek dosyası biçimi (Android ve iOS ortak): etkin bebeğin tüm tabloları tek JSON'da.
 * { "format": "minikgurme-backup", "version": 1, "createdAt": ms, "tables": { "<tablo>": [ {sütun: değer} ] } }
 * Bir telefonda alınan yedek diğerinde açılabilir.
 */
object BackupCore {
    val TABLES = listOf(
        "baby_profile", "meals", "feeding_diary", "growth_records", "allergen_tests",
        "tried_foods", "favorite_recipes", "shopping_items"
    )
    const val FORMAT = "minikgurme-backup"

    suspend fun exportJson(db: AppDatabase): Pair<String, Int> {
        var rows = 0
        val tables = LinkedHashMap<String, JsonArray>()
        db.useReaderConnection { conn ->
            for (t in TABLES) {
                val list = conn.usePrepared("SELECT * FROM `$t`") { st ->
                    buildList {
                        while (st.step()) {
                            val o = LinkedHashMap<String, kotlinx.serialization.json.JsonElement>()
                            for (i in 0 until st.getColumnCount()) {
                                o[st.getColumnName(i)] = when (st.getColumnType(i)) {
                                    SQLITE_DATA_NULL -> JsonNull
                                    SQLITE_DATA_INTEGER -> JsonPrimitive(st.getLong(i))
                                    SQLITE_DATA_FLOAT -> JsonPrimitive(st.getDouble(i))
                                    else -> JsonPrimitive(st.getText(i))
                                }
                            }
                            add(JsonObject(o))
                        }
                    }
                }
                rows += list.size
                tables[t] = JsonArray(list)
            }
        }
        val root = JsonObject(mapOf(
            "format" to JsonPrimitive(FORMAT), "version" to JsonPrimitive(1),
            "createdAt" to JsonPrimitive(currentTimeMillis()), "tables" to JsonObject(tables)
        ))
        return root.toString() to rows
    }

    /** Yedeği etkin bebeğin verilerinin YERİNE yükler; hatalı dosyada hiçbir şey değişmez (tek işlem). */
    suspend fun importJson(db: AppDatabase, text: String): Int {
        val root = runCatching { Json.parseToJsonElement(text).jsonObject }.getOrNull()
        require(root != null && (root["format"] as? JsonPrimitive)?.contentOrNull == FORMAT) { "Bu dosya bir Minik Gurme yedeği değil." }
        val tables = root["tables"]!!.jsonObject
        var rows = 0
        db.useWriterConnection { conn ->
            conn.immediateTransaction {
                for (t in TABLES) {
                    val arr = tables[t] as? JsonArray ?: continue
                    execSQL("DELETE FROM `$t`")
                    val columns = usePrepared("SELECT * FROM `$t` LIMIT 0") { it.getColumnNames().toSet() }
                    for (e in arr) {
                        val o = e.jsonObject.filterKeys { it in columns }
                        if (o.isEmpty()) continue
                        val keys = o.keys.toList()
                        val sql = "INSERT OR REPLACE INTO `$t` (${keys.joinToString { "`$it`" }}) VALUES (${keys.joinToString { "?" }})"
                        usePrepared(sql) { st ->
                            keys.forEachIndexed { i, k ->
                                val idx = i + 1
                                when (val v = o[k]) {
                                    null, is JsonNull -> st.bindNull(idx)
                                    is JsonPrimitive -> when {
                                        v.isString -> st.bindText(idx, v.content)
                                        v.booleanOrNull != null -> st.bindLong(idx, if (v.booleanOrNull == true) 1L else 0L)
                                        v.longOrNull != null -> st.bindLong(idx, v.longOrNull!!)
                                        v.doubleOrNull != null -> st.bindDouble(idx, v.doubleOrNull!!)
                                        else -> st.bindText(idx, v.content)
                                    }
                                    else -> st.bindText(idx, v.toString())
                                }
                            }
                            st.step()
                        }
                        rows++
                    }
                }
            }
        }
        return rows
    }
}
