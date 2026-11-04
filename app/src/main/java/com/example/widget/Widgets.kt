package com.example.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import android.widget.RemoteViews
import android.widget.Toast
import com.example.MainActivity
import com.example.R
import com.example.data.AppPreferences
import com.example.data.PrayerTimeUtils
import com.example.data.SpiritualQuotes
import com.example.i18n.AppLanguage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** Sonraki namaz vakti + canlı geri sayım widget'ı. */
class NextPrayerWidget : AppWidgetProvider() {
  override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) = WidgetUpdater.updateAll(context)
}

/** Günün 5 vakti (sıradaki vurgulu) widget'ı. */
class PrayerTimesWidget : AppWidgetProvider() {
  override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) = WidgetUpdater.updateAll(context)
}

/** Günün Manevi İklimi (ayet / hadis) widget'ı. */
class SpiritualWidget : AppWidgetProvider() {
  override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) = WidgetUpdater.updateAll(context)
}

/** Vakit değişiminde ve gece yarısında widget'ları yeniler. */
class WidgetRefreshReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) = WidgetUpdater.updateAll(context)
}

object WidgetUpdater {

  private const val REFRESH_REQUEST = 7401

  private data class Slot(val trName: String, val enName: String, val arabic: String, val time: String)

  private fun slots(prefs: AppPreferences) = listOf(
    Slot("İmsak", "Fajr", "الفجر", prefs.lastImsak),
    Slot("Öğle", "Dhuhr", "الظهر", prefs.lastOgle),
    Slot("İkindi", "Asr", "العصر", prefs.lastIkindi),
    Slot("Akşam", "Maghrib", "المغرب", prefs.lastAksam),
    Slot("Yatsı", "Isha", "العشاء", prefs.lastYatsi)
  ).map { it.copy(time = PrayerTimeUtils.cleanTimeString(it.time)) }

  /** Sıradaki vaktin sırası ve zamanı (ms). Yatsıdan sonra ertesi günün imsakı. */
  private fun next(slots: List<Slot>, now: Calendar = Calendar.getInstance()): Pair<Int, Long> {
    val nowMin = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
    val idx = slots.indexOfFirst { PrayerTimeUtils.parseTimeToMinutes(it.time) > nowMin }
    val target = (now.clone() as Calendar).apply {
      val i = if (idx == -1) 0 else idx
      val m = PrayerTimeUtils.parseTimeToMinutes(slots[i].time)
      if (idx == -1) add(Calendar.DAY_OF_MONTH, 1)
      set(Calendar.HOUR_OF_DAY, m / 60); set(Calendar.MINUTE, m % 60)
      set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }
    return (if (idx == -1) 0 else idx) to target.timeInMillis
  }

  fun updateAll(context: Context) {
    try {
      val prefs = AppPreferences(context)
      AppLanguage.current = prefs.language
      val en = prefs.language == "en"
      val manager = AppWidgetManager.getInstance(context)
      val slots = slots(prefs)
      val (nextIdx, nextMillis) = next(slots)
      val openApp = PendingIntent.getActivity(
        context, 0, Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )

      // 1) Sonraki namaz
      manager.getAppWidgetIds(ComponentName(context, NextPrayerWidget::class.java)).forEach { id ->
        val v = RemoteViews(context.packageName, R.layout.widget_next_prayer)
        val s = slots[nextIdx]
        v.setTextViewText(R.id.label, if (en) "Next Prayer" else "Sonraki Namaz")
        v.setTextViewText(R.id.prayer_name, if (en) s.enName else s.trName)
        v.setTextViewText(R.id.prayer_time, s.time)
        val base = SystemClock.elapsedRealtime() + (nextMillis - System.currentTimeMillis())
        v.setChronometer(R.id.countdown, base, null, true)
        v.setChronometerCountDown(R.id.countdown, true)
        v.setOnClickPendingIntent(R.id.widget_root, openApp)
        manager.updateAppWidget(id, v)
      }

      // 2) 5 vakit listesi
      val rowIds = intArrayOf(R.id.row_0, R.id.row_1, R.id.row_2, R.id.row_3, R.id.row_4)
      val arabicIds = intArrayOf(R.id.arabic_0, R.id.arabic_1, R.id.arabic_2, R.id.arabic_3, R.id.arabic_4)
      val nameIds = intArrayOf(R.id.name_0, R.id.name_1, R.id.name_2, R.id.name_3, R.id.name_4)
      val timeIds = intArrayOf(R.id.time_0, R.id.time_1, R.id.time_2, R.id.time_3, R.id.time_4)
      val dateText = SimpleDateFormat("d MMMM yyyy", if (en) Locale.ENGLISH else Locale.forLanguageTag("tr")).format(Date())
      manager.getAppWidgetIds(ComponentName(context, PrayerTimesWidget::class.java)).forEach { id ->
        val v = RemoteViews(context.packageName, R.layout.widget_prayer_times)
        v.setTextViewText(R.id.date, dateText)
        slots.forEachIndexed { i, s ->
          val active = i == nextIdx
          v.setTextViewText(arabicIds[i], s.arabic)
          v.setTextViewText(nameIds[i], if (en) s.enName else s.trName)
          v.setTextViewText(timeIds[i], s.time)
          v.setInt(rowIds[i], "setBackgroundResource", if (active) R.drawable.widget_row_highlight else R.drawable.widget_row_normal)
          v.setTextColor(nameIds[i], if (active) 0xFFE5B869.toInt() else 0xFFEDE9DF.toInt())
          v.setTextColor(timeIds[i], if (active) 0xFFE5B869.toInt() else 0xFFF3EEE2.toInt())
        }
        v.setOnClickPendingIntent(R.id.widget_root, openApp)
        manager.updateAppWidget(id, v)
      }

      // 3) Günün Manevi İklimi
      val spiritualIds = manager.getAppWidgetIds(ComponentName(context, SpiritualWidget::class.java))
      if (spiritualIds.isNotEmpty()) {
        val item = SpiritualQuotes.forToday()
        val showVerse = item.verseTurkish.isNotBlank()
        spiritualIds.forEach { id ->
          val v = RemoteViews(context.packageName, R.layout.widget_spiritual)
          v.setTextViewText(R.id.title, if (en) "Daily Inspiration" else "Günün Manevi İklimi")
          v.setTextViewText(R.id.kind, if (showVerse) (if (en) "Verse" else "Ayet") else (if (en) "Hadith" else "Hadis"))
          v.setTextViewText(R.id.arabic, if (showVerse) item.verseArabic else "")
          v.setTextViewText(R.id.body, "“" + (if (showVerse) item.verseTurkish else item.hadithTurkish) + "”")
          v.setTextViewText(R.id.source, "— " + (if (showVerse) item.verseSource else item.hadithSource))
          v.setOnClickPendingIntent(R.id.widget_root, openApp)
          manager.updateAppWidget(id, v)
        }
      }

      scheduleRefresh(context, nextMillis)
    } catch (_: Exception) {
      // Widget güncellemesi uygulamayı asla çökertmemeli
    }
  }

  /** Bir sonraki vakit (veya gece yarısı, hangisi önceyse) için yenileme alarmı kurar. */
  private fun scheduleRefresh(context: Context, nextMillis: Long) {
    val manager = AppWidgetManager.getInstance(context)
    val any = listOf(NextPrayerWidget::class.java, PrayerTimesWidget::class.java, SpiritualWidget::class.java)
      .any { manager.getAppWidgetIds(ComponentName(context, it)).isNotEmpty() }
    val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val pi = PendingIntent.getBroadcast(
      context, REFRESH_REQUEST, Intent(context, WidgetRefreshReceiver::class.java),
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    if (!any) { alarm.cancel(pi); return }
    val midnight = Calendar.getInstance().apply {
      add(Calendar.DAY_OF_MONTH, 1)
      set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 5); set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    val at = minOf(nextMillis + 2_000L, midnight)
    val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarm.canScheduleExactAlarms()
    try {
      if (canExact) alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
      else alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
    } catch (_: SecurityException) {
      alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
    }
  }

  enum class Kind { NEXT_PRAYER, PRAYER_TIMES, SPIRITUAL }

  /** Android 8+ : widget'ı doğrudan ana ekrana ekleme isteği. Desteklenmiyorsa yönlendirme gösterir. */
  fun requestPin(context: Context, kind: Kind) {
    val cls = when (kind) {
      Kind.NEXT_PRAYER -> NextPrayerWidget::class.java
      Kind.PRAYER_TIMES -> PrayerTimesWidget::class.java
      Kind.SPIRITUAL -> SpiritualWidget::class.java
    }
    val en = AppLanguage.isEnglish
    val manager = AppWidgetManager.getInstance(context)
    val ok = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && manager.isRequestPinAppWidgetSupported &&
      runCatching { manager.requestPinAppWidget(ComponentName(context, cls), null, null) }.getOrDefault(false)
    if (!ok) {
      Toast.makeText(
        context,
        if (en) "Long-press an empty spot on your home screen → Widgets → Ramazan Rehberi"
        else "Ana ekranda boş bir yere basılı tutun → Widget'lar → Ramazan Rehberi",
        Toast.LENGTH_LONG
      ).show()
    }
  }
}
