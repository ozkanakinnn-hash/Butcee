package akinai.minikgurme.util

import akinai.minikgurme.data.local.AppDatabase
import akinai.minikgurme.data.local.getDatabase
import android.content.Context
import android.net.Uri
import kotlinx.coroutines.runBlocking

/**
 * Yedekle / geri yükle (Android): dosyayı kullanıcı seçer (sistem dosya seçicisi); uygulama hiçbir yere göndermez.
 * JSON biçimi ortak [BackupCore]'dadır (iOS ile aynı dosya).
 */
object Backup {
    fun export(context: Context, uri: Uri): Int = runBlocking {
        val (json, rows) = BackupCore.exportJson(AppDatabase.getDatabase(context))
        context.contentResolver.openOutputStream(uri, "wt")?.use { it.write(json.toByteArray(Charsets.UTF_8)) }
            ?: error("Dosyaya yazılamadı")
        rows
    }

    /** Yedeği etkin bebeğin verilerinin YERİNE yükler. Ekranlar, çağıranın etkinliği yeniden başlatmasıyla yenilenir. */
    fun import(context: Context, uri: Uri): Int = runBlocking {
        val text = context.contentResolver.openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) }
            ?: error("Dosya okunamadı")
        BackupCore.importJson(AppDatabase.getDatabase(context), text)
    }
}
