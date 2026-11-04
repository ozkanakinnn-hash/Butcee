package com.example.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.AppPreferences
import com.example.data.PrayerTimeUtils
import com.example.data.PrayerTimes
import java.util.Calendar

/** İftar ve sahur öncesi hatırlatıcılarını AlarmManager ile kurar. */
object ReminderScheduler {

  enum class Type(val requestCode: Int, val label: String) {
    SAHUR(7101, "Sahur"), IFTAR(7102, "İftar"),
    // Vakit bildirimleri (Ayarlar > Namaz Vakitleri anahtarları)
    PRAYER_IMSAK(7201, "İmsak"), PRAYER_OGLE(7202, "Öğle"), PRAYER_IKINDI(7203, "İkindi"),
    PRAYER_AKSAM(7204, "Akşam"), PRAYER_YATSI(7205, "Yatsı"),
    // Ramazan boyunca her gün farklı faydalı oruç bilgisi
    FASTING_TIP(7301, "Oruç Bilgisi"),
    // Bildirim sihirbazı: 1 dakika sonra arka plan testi
    TEST(7901, "Test")
  }

  const val EXTRA_TYPE = "reminder_type"
  const val EXTRA_TARGET_MILLIS = "reminder_target_millis"
  private const val FASTING_TIP_TIME = "13:00"

  /** Namaz vakitleri yüklendiğinde çağrılır: vakitleri saklar ve alarmları yeniler. */
  fun onPrayerTimesLoaded(context: Context, prayerTimes: PrayerTimes) {
    val prefs = AppPreferences(context)
    prefs.lastImsak = PrayerTimeUtils.cleanTimeString(prayerTimes.imsak, prefs.lastImsak)
    prefs.lastAksam = PrayerTimeUtils.cleanTimeString(prayerTimes.aksam, prefs.lastAksam)
    prefs.lastOgle = PrayerTimeUtils.cleanTimeString(prayerTimes.ogle, prefs.lastOgle)
    prefs.lastIkindi = PrayerTimeUtils.cleanTimeString(prayerTimes.ikindi, prefs.lastIkindi)
    prefs.lastYatsi = PrayerTimeUtils.cleanTimeString(prayerTimes.yatsi, prefs.lastYatsi)
    rescheduleAll(context)
  }

  fun rescheduleAll(context: Context) {
    val prefs = AppPreferences(context)
    schedule(context, Type.SAHUR, prefs.remindBeforeImsak, prefs.lastImsak, prefs.remindBeforeImsakMinutes)
    schedule(context, Type.IFTAR, prefs.remindBeforeIftar, prefs.lastAksam, prefs.remindBeforeIftarMinutes)
    val on = prefs.notificationsEnabled
    schedule(context, Type.PRAYER_IMSAK, on && prefs.notifyImsak, prefs.lastImsak, 0)
    schedule(context, Type.PRAYER_OGLE, on && prefs.notifyOgle, prefs.lastOgle, 0)
    schedule(context, Type.PRAYER_IKINDI, on && prefs.notifyIkindi, prefs.lastIkindi, 0)
    schedule(context, Type.PRAYER_AKSAM, on && prefs.notifyIftar, prefs.lastAksam, 0)
    schedule(context, Type.PRAYER_YATSI, on && prefs.notifyYatsi, prefs.lastYatsi, 0)
    // Her gün 13:00'te kurulur; yalnızca Ramazan günlerinde bildirim gösterilir
    schedule(context, Type.FASTING_TIP, on, FASTING_TIP_TIME, 0)
    // Ana ekran widget'larını da güncel tut
    com.example.widget.WidgetUpdater.updateAll(context)
  }

  private fun schedule(context: Context, type: Type, enabled: Boolean, targetTime: String, minutesBefore: Int) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    if (!enabled) {
      alarmManager.cancel(pendingIntent(context, type, 0L))
      return
    }

    // Hedef vakit (bugün), hatırlatma zamanı geçmişse yarına kaydır
    val totalMin = PrayerTimeUtils.parseTimeToMinutes(targetTime)
    val target = Calendar.getInstance().apply {
      set(Calendar.HOUR_OF_DAY, totalMin / 60)
      set(Calendar.MINUTE, totalMin % 60)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }
    val now = System.currentTimeMillis()
    var triggerAt = target.timeInMillis - minutesBefore * 60_000L
    if (triggerAt <= now + 30_000L) {
      target.add(Calendar.DAY_OF_MONTH, 1)
      triggerAt = target.timeInMillis - minutesBefore * 60_000L
    }

    val pi = pendingIntent(context, type, target.timeInMillis)
    if (type == Type.FASTING_TIP) {
      // Günlük bilgi kritik değil: esnek pencere (pil dostu, ezan alarmlarının kotasını kullanmaz)
      alarmManager.setWindow(AlarmManager.RTC_WAKEUP, triggerAt, 15 * 60_000L, pi)
    } else {
      setPrecise(context, alarmManager, triggerAt, pi)
    }
  }

  /**
   * Dakikası dakikasına alarm: setAlarmClock() Doze (uyku) modundan ve "9 dakikada bir" kotasından
   * muaftır, cihazı uyandırır. Ezan ve sahur/iftar hatırlatıcıları bununla kurulur.
   * Tam zamanlı alarm izni yoksa en iyi ihtimalle setAndAllowWhileIdle'a düşer.
   */
  private fun setPrecise(context: Context, alarmManager: AlarmManager, triggerAt: Long, pi: PendingIntent) {
    val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
    try {
      if (canExact) {
        val show = PendingIntent.getActivity(
          context, 7999,
          Intent(context, com.example.MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
          PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(triggerAt, show), pi)
      } else {
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
      }
    } catch (e: SecurityException) {
      alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
    }
  }

  /** Bildirim sihirbazı: 1 dakika sonra arka planda test bildirimi. */
  fun scheduleTest(context: Context) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val at = System.currentTimeMillis() + 60_000L
    setPrecise(context, alarmManager, at, pendingIntent(context, Type.TEST, at))
  }

  private fun pendingIntent(context: Context, type: Type, targetMillis: Long): PendingIntent {
    val intent = Intent(context, ReminderReceiver::class.java).apply {
      action = "com.example.reminder.${type.name}"
      putExtra(EXTRA_TYPE, type.name)
      putExtra(EXTRA_TARGET_MILLIS, targetMillis)
    }
    return PendingIntent.getBroadcast(
      context, type.requestCode, intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
  }
}
