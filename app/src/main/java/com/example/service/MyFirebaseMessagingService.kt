package com.example.service

import android.util.Log
import com.example.data.IftarSoundChoice
import com.example.util.NotificationHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

  override fun onNewToken(token: String) {
    super.onNewToken(token)
    // Güvenlik: cihaz kimliği (FCM token) loglara yazılmaz.
  }

  override fun onMessageReceived(remoteMessage: RemoteMessage) {
    super.onMessageReceived(remoteMessage)

    val title = remoteMessage.notification?.title
      ?: remoteMessage.data["title"]
      ?: "🌙 Ezan & Vakit"

    val body = remoteMessage.notification?.body
      ?: remoteMessage.data["body"]
      ?: "Hayırlı Ramazanlar!"

    NotificationHelper.sendNotification(
      context = applicationContext,
      title = title,
      message = body,
      soundChoice = IftarSoundChoice.RAMAZAN_TOPU
    )
  }
}
