package akinai.minikgurme.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import akinai.minikgurme.data.local.AppDatabase
import akinai.minikgurme.data.local.getDatabase
import akinai.minikgurme.util.Babies
import akinai.minikgurme.util.BackupCore
import akinai.minikgurme.util.DoctorReportContent
import kotlinx.coroutines.launch
import platform.Foundation.NSURL
import platform.Foundation.NSBundle
import platform.UIKit.UIPasteboard
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNUserNotificationCenter

/**
 * iOS gerçeklemesi: uygulama içi mesaj, pano, bildirim izni, Dosyalar'a yedek kaydetme/açma,
 * doktor raporu (PDF) ve paylaşım menüsü.
 */
class IosPlatformServices : PlatformServices {
    override val appVersion: String =
        NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String ?: ""
    override val isDebugBuild: Boolean = false

    override fun toast(message: String) = akinai.minikgurme.ui.platform.InAppMessage.show(message)

    override fun copyText(text: String) { UIPasteboard.generalPasteboard.string = text }

    @Composable
    override fun rememberNotificationPermission(onResult: (Boolean) -> Unit): () -> Unit = {
        UNUserNotificationCenter.currentNotificationCenter().requestAuthorizationWithOptions(
            UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge
        ) { granted, _ -> onResult(granted) }
    }

    private fun activeDb() = AppDatabase.getDatabase(Babies.dbName(Babies.activeId()))

    @Composable
    override fun rememberBackupExport(onResult: (Boolean) -> Unit): () -> Unit {
        val scope = rememberCoroutineScope()
        return {
            scope.launch {
                val r = runCatching {
                    val (json, _) = BackupCore.exportJson(activeDb())
                    val url = IosFiles.tempFile("MinikGurme-yedek.json")
                    check(IosFiles.writeText(url, json))
                    url
                }
                r.onSuccess { IosFiles.exportToFiles(it, onResult) }.onFailure { onResult(false) }
            }
        }
    }

    @Composable
    override fun rememberBackupPicker(onPicked: (Any) -> Unit): () -> Unit = { IosFiles.pickBackup { onPicked(it) } }

    override suspend fun importBackup(file: Any): Result<Unit> = runCatching {
        val text = IosFiles.readText(file as NSURL) ?: error("Dosya okunamadı")
        BackupCore.importJson(activeDb(), text)
        Unit
    }

    override suspend fun shareDoctorReport(): Result<Unit> = runCatching {
        val report = DoctorReportContent.build(activeDb())
        IosFiles.share(IosFiles.renderPdf(report))
    }
}
