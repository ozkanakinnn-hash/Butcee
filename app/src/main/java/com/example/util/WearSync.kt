package com.example.util

import android.content.Context
import com.example.data.AppPreferences
import com.example.data.PrayerTimes
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable

/**
 * Namaz vakitlerini, konumu ve dili eşleşmiş akıllı saate (Wear OS) gönderir.
 * Saat yoksa veya Google Play hizmetleri yoksa sessizce hiçbir şey yapmaz.
 */
object WearSync {
  const val PATH = "/ramazan/prayer_times"

  fun send(context: Context, times: PrayerTimes, prefs: AppPreferences) {
    try {
      val req = PutDataMapRequest.create(PATH).apply {
        dataMap.putString("imsak", times.imsak)
        dataMap.putString("gunes", times.gunes)
        dataMap.putString("ogle", times.ogle)
        dataMap.putString("ikindi", times.ikindi)
        dataMap.putString("aksam", times.aksam)
        dataMap.putString("yatsi", times.yatsi)
        dataMap.putString("place", "${prefs.province} / ${prefs.district}")
        dataMap.putString("lang", prefs.language)
        dataMap.putLong("ts", System.currentTimeMillis()) // her gönderimde değişsin
      }.asPutDataRequest().setUrgent()
      Wearable.getDataClient(context).putDataItem(req)
    } catch (_: Exception) {
    }
  }
}
