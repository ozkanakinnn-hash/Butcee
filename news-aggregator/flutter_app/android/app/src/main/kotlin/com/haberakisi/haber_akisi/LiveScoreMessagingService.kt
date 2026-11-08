package com.haberakisi.haber_akisi

import com.google.firebase.messaging.RemoteMessage
import io.flutter.plugins.firebase.messaging.FlutterFirebaseMessagingService

/**
 * FCM mesaj servisi: canlı skor paneli verisini ("type" = "match_live", yalnızca veri) yakalar ve
 * bildirim alanındaki paneli günceller. Diğer tüm mesajlar Flutter eklentisine (super) aynen iletilir.
 * Manifest'te eklentinin kendi servisi yerine bu servis kayıtlıdır.
 */
class LiveScoreMessagingService : FlutterFirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (remoteMessage.data["type"] == LiveScoreNotifier.TYPE) {
            LiveScoreNotifier.show(applicationContext, remoteMessage.data)
            return
        }
        super.onMessageReceived(remoteMessage)
    }
}
