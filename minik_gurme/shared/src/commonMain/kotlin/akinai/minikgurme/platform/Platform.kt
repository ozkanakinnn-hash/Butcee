package akinai.minikgurme.platform

import androidx.compose.runtime.Composable

/**
 * Ekranların ihtiyaç duyduğu telefona özel işler. Android ve iOS kendi gerçeklemesini [Platform.services]'e kurar.
 * Composable olanlar "başlatıcı" döndürür: ekran açılırken hazırlanır, düğmeye basılınca çağrılır.
 */
interface PlatformServices {
    val appVersion: String
    val isDebugBuild: Boolean

    /** Kısa bilgi mesajı (Türkçe kaynak metin; gösterirken çevrilir). */
    fun toast(message: String)
    fun copyText(text: String)

    /** Bildirim izni ister (gerekmiyorsa hemen true ile döner). */
    @Composable
    fun rememberNotificationPermission(onResult: (Boolean) -> Unit): () -> Unit

    /** Yedek dosyası kaydetme penceresi. */
    @Composable
    fun rememberBackupExport(onResult: (Boolean) -> Unit): () -> Unit

    /** Yedek dosyası seçme penceresi; seçilen dosya [importBackup]'a verilir. */
    @Composable
    fun rememberBackupPicker(onPicked: (Any) -> Unit): () -> Unit

    suspend fun importBackup(file: Any): Result<Unit>

    /** Doktor raporunu (PDF) oluşturup paylaşma penceresini açar. */
    suspend fun shareDoctorReport(): Result<Unit>
}

object Platform {
    lateinit var services: PlatformServices
}
