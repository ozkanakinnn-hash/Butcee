package akinai.minikgurme.util

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import akinai.minikgurme.data.local.AppDatabase
import org.json.JSONArray
import org.json.JSONObject

/**
 * Yedekle / geri yükle: etkin bebeğin tüm tablolarını tek bir JSON dosyasına yazar ve geri okur.
 * Dosyayı kullanıcı seçer (Android dosya seçicisi); uygulama hiçbir yere göndermez.
 */
object Backup {
    private val TABLES = listOf(
        "baby_profile", "meals", "feeding_diary", "growth_records", "allergen_tests",
        "tried_foods", "favorite_recipes", "shopping_items"
    )
    private const val FORMAT = "minikgurme-backup"

    fun export(context: Context, uri: Uri): Int {
        val db = AppDatabase.getDatabase(context).openHelper.readableDatabase
        val root = JSONObject().put("format", FORMAT).put("version", 1).put("createdAt", System.currentTimeMillis())
        val tables = JSONObject(); var rows = 0
        for (t in TABLES) {
            val arr = JSONArray()
            db.query("SELECT * FROM `$t`").use { c ->
                while (c.moveToNext()) {
                    val o = JSONObject()
                    for (i in 0 until c.columnCount) {
                        val name = c.getColumnName(i)
                        when (c.getType(i)) {
                            Cursor.FIELD_TYPE_NULL -> o.put(name, JSONObject.NULL)
                            Cursor.FIELD_TYPE_INTEGER -> o.put(name, c.getLong(i))
                            Cursor.FIELD_TYPE_FLOAT -> o.put(name, c.getDouble(i))
                            else -> o.put(name, c.getString(i))
                        }
                    }
                    arr.put(o); rows++
                }
            }
            tables.put(t, arr)
        }
        root.put("tables", tables)
        context.contentResolver.openOutputStream(uri, "wt")?.use { it.write(root.toString().toByteArray(Charsets.UTF_8)) }
            ?: error("Dosyaya yazılamadı")
        return rows
    }

    /** Yedeği etkin bebeğin verilerinin YERİNE yükler. Hatalı dosyada hiçbir şey değişmez. */
    fun import(context: Context, uri: Uri): Int {
        val text = context.contentResolver.openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) }
            ?: error("Dosya okunamadı")
        val root = JSONObject(text)
        require(root.optString("format") == FORMAT) { "Bu dosya bir Minik Gurme yedeği değil." }
        val tables = root.getJSONObject("tables")
        val db = AppDatabase.getDatabase(context).openHelper.writableDatabase
        var rows = 0
        db.beginTransaction()
        try {
            for (t in TABLES) {
                val arr = tables.optJSONArray(t) ?: continue
                db.execSQL("DELETE FROM `$t`")
                val columns = db.query("SELECT * FROM `$t` LIMIT 0").use { c -> c.columnNames.toSet() }
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i); val cv = ContentValues()
                    for (k in o.keys()) {
                        if (k !in columns) continue
                        when (val v = o.get(k)) {
                            JSONObject.NULL -> cv.putNull(k)
                            is Int -> cv.put(k, v.toLong())
                            is Long -> cv.put(k, v)
                            is Double -> cv.put(k, v)
                            is Boolean -> cv.put(k, if (v) 1L else 0L)
                            else -> cv.put(k, v.toString())
                        }
                    }
                    db.insert(t, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE, cv); rows++
                }
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        // Ekranlar, çağıran tarafın etkinliği yeniden başlatmasıyla yeni verileri okur.
        return rows
    }
}
