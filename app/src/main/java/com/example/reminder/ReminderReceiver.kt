package com.example.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.AppPreferences
import com.example.data.RamadanCalendar
import com.example.util.NotificationHelper
import java.util.Calendar

/** Alarm çaldığında: hedef gün oruç günüyse motive edici bildirim gönderir, ertesi günü kurar. */
class ReminderReceiver : BroadcastReceiver() {

  override fun onReceive(context: Context, intent: Intent) {
    val prefs = AppPreferences(context)
    val type = runCatching { ReminderScheduler.Type.valueOf(intent.getStringExtra(ReminderScheduler.EXTRA_TYPE) ?: "") }.getOrNull()
    val targetMillis = intent.getLongExtra(ReminderScheduler.EXTRA_TARGET_MILLIS, 0L)

    if (type != null && targetMillis > 0L) {
      val targetDay = Calendar.getInstance().apply { timeInMillis = targetMillis }
      val dayKey = RamadanCalendar.dateKey(targetDay)
      val isFastingDay = RamadanCalendar.isRamadanDay(dayKey) || prefs.getFastingDates().contains(dayKey)

      if (type == ReminderScheduler.Type.TEST) {
        val en = prefs.language == "en"
        NotificationHelper.sendEzanNotification(
          context,
          if (en) "✅ Background test successful" else "✅ Arka plan testi başarılı",
          if (en) "Adhan and reminders will arrive on time on this phone." else "Ezan ve hatırlatıcılar bu telefonda vaktinde gelecek.",
          notificationId = type.requestCode
        )
      } else if (type == ReminderScheduler.Type.FASTING_TIP) {
        val day = RamadanCalendar.dayOfRamadan(targetDay)
        if (day != null) {
          com.example.i18n.AppLanguage.current = prefs.language
          val en = prefs.language == "en"
          val title = if (en) "🌙 Ramadan Day $day • Daily Tip" else "🌙 Ramazan'ın $day. Günü • Günün Bilgisi"
          NotificationHelper.sendNotification(context, title, com.example.data.FastingKnowledge.tipForDay(day).text, notificationId = type.requestCode)
        }
      } else if (type.name.startsWith("PRAYER_")) {
        val time = String.format(java.util.Locale.ROOT, "%02d:%02d", targetDay.get(Calendar.HOUR_OF_DAY), targetDay.get(Calendar.MINUTE))
        val place = "${prefs.province} / ${prefs.district}"
        // Şimdilik tüm vakitlerde aynı ezan sesi çalar
        val (title, message) = when {
          type == ReminderScheduler.Type.PRAYER_AKSAM && isFastingDay ->
            "🕌 Akşam Ezanı • İftar Vakti" to "$place için iftar vakti ($time). Allah orucunuzu kabul etsin! 🤲"
          type == ReminderScheduler.Type.PRAYER_IMSAK ->
            "🌙 İmsak Vakti" to "$place için imsak vakti ($time)."
          else ->
            "🕌 ${type.label} Ezanı" to "$place için ${type.label.lowercase()} namazı vakti girdi ($time)."
        }
        NotificationHelper.sendEzanNotification(context, title, message, notificationId = type.requestCode)
      } else if (isFastingDay) {
        val (title, message) = when (type) {
          ReminderScheduler.Type.SAHUR ->
            "🌙 Sahura ${prefs.remindBeforeImsakMinutes} dakika kaldı" to MotivationMessages.randomSahur()
          else ->
            "🍽️ İftara ${prefs.remindBeforeIftarMinutes} dakika kaldı" to MotivationMessages.randomIftar()
        }
        // Sahur: davul sesi + titreşim; iftar ve diğerleri: ney sesi
        if (type == ReminderScheduler.Type.SAHUR) {
          NotificationHelper.sendDavulNotification(context, title, message, notificationId = type.requestCode)
        } else {
          NotificationHelper.sendNotification(context, title, message, notificationId = type.requestCode)
        }
      }
    }

    // Bir sonraki günün hatırlatıcısını kur
    ReminderScheduler.rescheduleAll(context)
  }
}

/** Cihaz yeniden başladığında veya uygulama güncellendiğinde alarmları yeniden kurar. */
class ReminderBootReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    when (intent.action) {
      Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED,
      Intent.ACTION_TIME_CHANGED, Intent.ACTION_TIMEZONE_CHANGED,
      "android.app.action.SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED" ->
        ReminderScheduler.rescheduleAll(context)
    }
  }
}
