package akinai.minikgurme.platform

import androidx.compose.runtime.Composable
import platform.Foundation.NSBundle
import platform.UIKit.UIPasteboard
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNUserNotificationCenter

/**
 * iOS gerçeklemesi. Şimdilik temel işler; yedekleme, doktor raporu ve kısa bildirim
 * iOS servisleri adımında (5. parça) tamamlanır.
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

    @Composable
    override fun rememberBackupExport(onResult: (Boolean) -> Unit): () -> Unit = { onResult(false) }

    @Composable
    override fun rememberBackupPicker(onPicked: (Any) -> Unit): () -> Unit = { }

    override suspend fun importBackup(file: Any): Result<Unit> = Result.failure(UnsupportedOperationException())

    override suspend fun shareDoctorReport(): Result<Unit> = Result.failure(UnsupportedOperationException())
}
