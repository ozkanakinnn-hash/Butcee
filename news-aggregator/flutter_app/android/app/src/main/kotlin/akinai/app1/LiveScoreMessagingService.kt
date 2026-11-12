package akinai.app1

import com.google.firebase.messaging.RemoteMessage
import io.flutter.plugins.firebase.messaging.FlutterFirebaseMessagingService

/**
 * FCM mesaj servisi: canlı skor paneli verisini ("type" = "match_live") ve haber bildirimlerini
 * ("kind" = "news_alert") yakalayıp bildirimi kendisi kurar. Diğer tüm mesajlar Flutter eklentisine (super) aynen iletilir.
 * Manifest'te eklentinin kendi servisi yerine bu servis kayıtlıdır.
 */
class LiveScoreMessagingService : FlutterFirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (remoteMessage.data["type"] == LiveScoreNotifier.TYPE) {
            LiveScoreNotifier.show(applicationContext, remoteMessage.data)
            return
        }
        // Haber bildirimi: uygulama kendisi kurar ("Daha az bildirim" düğmesiyle); ön planda da aynı görünür.
        if (remoteMessage.data["kind"] == NewsNotifier.KIND) {
            NewsNotifier.show(applicationContext, remoteMessage.data)
            return
        }
        super.onMessageReceived(remoteMessage)
    }
}
