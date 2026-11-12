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
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.RemoteViews
import android.widget.Toast
import com.example.MainActivity
import com.example.R
import com.example.data.AppPreferences
import com.example.data.IslamicDays
import com.example.data.PrayerTimeUtils
import com.example.data.RamadanCalendar
import com.example.data.SpiritualQuotes
import com.example.i18n.AppLanguage
import com.example.i18n.t
import java.util.Calendar
import java.util.Locale

/** Sıradaki namaz: ilerleme halkası + canlı geri sayım. */
class NextPrayerWidget : AppWidgetProvider() {
  override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) = WidgetUpdater.updateAll(context)
  override fun onAppWidgetOptionsChanged(context: Context, manager: AppWidgetManager, id: Int, o: Bundle) = WidgetUpdater.updateAll(context)
}

/** Günün 6 vakti, hicri tarih ve günün akışı. */
class PrayerTimesWidget : AppWidgetProvider() {
  override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) = WidgetUpdater.updateAll(context)
  override fun onAppWidgetOptionsChanged(context: Context, manager: AppWidgetManager, id: Int, o: Bundle) = WidgetUpdater.updateAll(context)
}

/** Günün Manevi İklimi (ayet / hadis). */
class SpiritualWidget : AppWidgetProvider() {
  override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) = WidgetUpdater.updateAll(context)
  override fun onAppWidgetOptionsChanged(context: Context, manager: AppWidgetManager, id: Int, o: Bundle) = WidgetUpdater.updateAll(context)
}

/** Oruç & İftar: oruç ilerlemesi ve iftara kalan süre; diğer günlerde sıradaki kandil / bayram. */
class FastingWidget : AppWidgetProvider() {
  override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) = WidgetUpdater.updateAll(context)
  override fun onAppWidgetOptionsChanged(context: Context, manager: AppWidgetManager, id: Int, o: Bundle) = WidgetUpdater.updateAll(context)
}

/** Vakit değişiminde, gece yarısında ve 15 dakikada bir (ekran açıkken) widget'ları yeniler. */
class WidgetRefreshReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) = WidgetUpdater.updateAll(context)
}

object WidgetUpdater {

  private const val REFRESH_REQUEST = 7401
  private val PROVIDERS = listOf(NextPrayerWidget::class.java, PrayerTimesWidget::class.java, SpiritualWidget::class.java, FastingWidget::class.java)

  private val TR_NAMES = listOf("İmsak", "Güneş", "Öğle", "İkindi", "Akşam", "Yatsı")
  private val EN_NAMES = listOf("Fajr", "Sunrise", "Dhuhr", "Asr", "Maghrib", "Isha")

  private fun times(prefs: AppPreferences) =
    listOf(prefs.lastImsak, prefs.lastGunes, prefs.lastOgle, prefs.lastIkindi, prefs.lastAksam, prefs.lastYatsi)
      .map { PrayerTimeUtils.cleanTimeString(it) }

  private fun at(base: Calendar, hhmm: String, addDays: Int = 0): Long {
    val m = PrayerTimeUtils.parseTimeToMinutes(hhmm)
    return (base.clone() as Calendar).apply {
      add(Calendar.DAY_OF_MONTH, addDays)
      set(Calendar.HOUR_OF_DAY, m / 60); set(Calendar.MINUTE, m % 60); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis
  }

  /** Widget'ın dp cinsinden boyutu (dikey ekran: en küçük genişlik × en büyük yükseklik). */
  private fun sizeDp(manager: AppWidgetManager, id: Int, defW: Int, defH: Int): Pair<Int, Int> {
    val o = manager.getAppWidgetOptions(id)
    val w = o.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 0).takeIf { it > 40 } ?: defW
    val h = o.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, 0).takeIf { it > 40 } ?: defH
    return w.coerceIn(90, 520) to h.coerceIn(90, 420)
  }

  private val HIJRI_TR = listOf("Muharrem", "Safer", "Rebiülevvel", "Rebiülahir", "Cemaziyelevvel", "Cemaziyelahir", "Recep", "Şaban", "Ramazan", "Şevval", "Zilkade", "Zilhicce")
  private val HIJRI_EN = listOf("Muharram", "Safar", "Rabi al-Awwal", "Rabi al-Thani", "Jumada al-Awwal", "Jumada al-Thani", "Rajab", "Sha'ban", "Ramadan", "Shawwal", "Dhu al-Qa'dah", "Dhu al-Hijjah")

  /** Hicri tarih (Ümmü'l-Kura); Ramazan günlerinde Diyanet tablosuyla uyumlu gün. */
  fun hijriText(cal: Calendar, en: Boolean): String = try {
    RamadanCalendar.dayOfRamadan(cal)?.let { d ->
      val ic = android.icu.util.IslamicCalendar().apply {
        calculationType = android.icu.util.IslamicCalendar.CalculationType.ISLAMIC_UMALQURA; timeInMillis = cal.timeInMillis
      }
      return "$d ${if (en) HIJRI_EN[8] else HIJRI_TR[8]} ${ic.get(android.icu.util.IslamicCalendar.YEAR)}"
    }
    val ic = android.icu.util.IslamicCalendar().apply {
      calculationType = android.icu.util.IslamicCalendar.CalculationType.ISLAMIC_UMALQURA; timeInMillis = cal.timeInMillis
    }
    val m = ic.get(android.icu.util.IslamicCalendar.MONTH)
    "${ic.get(android.icu.util.IslamicCalendar.DAY_OF_MONTH)} ${(if (en) HIJRI_EN else HIJRI_TR)[m]} ${ic.get(android.icu.util.IslamicCalendar.YEAR)}"
  } catch (_: Exception) { "" }

  fun updateAll(context: Context) {
    try {
      val prefs = AppPreferences(context)
      AppLanguage.current = prefs.language
      val en = prefs.language == "en"
      val manager = AppWidgetManager.getInstance(context)
      val palette = WidgetArt.palette(context, prefs)
      val now = Calendar.getInstance()
      val nowMs = now.timeInMillis
      val ts = times(prefs)
      val names = if (en) EN_NAMES else TR_NAMES
      val mins = ts.map { PrayerTimeUtils.parseTimeToMinutes(it) }
      val nowMin = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
      // Şu anki vakit (imsaktan önce: dünkü yatsı) ve sıradaki vakit
      val current = mins.indexOfLast { nowMin >= it }.let { if (it == -1) 5 else it }
      val nextIdx = mins.indexOfFirst { it > nowMin }
      val nextMs = if (nextIdx == -1) at(now, ts[0], 1) else at(now, ts[nextIdx])
      val startMs = when {
        nextIdx == -1 -> at(now, ts[5])
        nextIdx == 0 -> at(now, ts[5], -1)
        else -> at(now, ts[nextIdx - 1])
      }
      val periodProgress = ((nowMs - startMs).toFloat() / (nextMs - startMs).coerceAtLeast(1)).coerceIn(0f, 1f)
      val nextName = names[if (nextIdx == -1) 0 else nextIdx]
      val nextTime = ts[if (nextIdx == -1) 0 else nextIdx]
      val openApp = PendingIntent.getActivity(
        context, 0, Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )
      fun chrono(v: RemoteViews, targetMs: Long) {
        v.setChronometer(R.id.countdown, SystemClock.elapsedRealtime() + (targetMs - System.currentTimeMillis()), null, true)
        v.setChronometerCountDown(R.id.countdown, true)
        v.setTextColor(R.id.countdown, palette.ink)
      }

      // 1) Sıradaki namaz
      manager.getAppWidgetIds(ComponentName(context, NextPrayerWidget::class.java)).forEach { id ->
        val (w, h) = sizeDp(manager, id, 170, 170)
        val v = RemoteViews(context.packageName, R.layout.widget_art_next)
        v.setImageViewBitmap(R.id.art, WidgetArt.nextPrayer(context, palette, w, h, if (en) "Next" else "Sıradaki", nextName, nextTime, periodProgress))
        chrono(v, nextMs)
        v.setOnClickPendingIntent(R.id.widget_root, openApp)
        manager.updateAppWidget(id, v)
      }

      // 2) Günün vakitleri
      val place = "${prefs.province} / ${prefs.district}"
      val hijri = hijriText(now, en)
      val dayStart = at(now, ts[0]); val dayEnd = at(now, ts[5])
      val dayProgress = ((nowMs - dayStart).toFloat() / (dayEnd - dayStart).coerceAtLeast(1)).coerceIn(0f, 1f)
      val footer = (if (en) "Next: " else "Sıradaki: ") + "$nextName $nextTime"
      manager.getAppWidgetIds(ComponentName(context, PrayerTimesWidget::class.java)).forEach { id ->
        val (w, h) = sizeDp(manager, id, 320, 170)
        val v = RemoteViews(context.packageName, R.layout.widget_art_image)
        v.setImageViewBitmap(R.id.art, WidgetArt.prayerTimes(context, palette, w, h, place, hijri, names, ts, current, dayProgress, footer))
        v.setOnClickPendingIntent(R.id.widget_root, openApp)
        manager.updateAppWidget(id, v)
      }

      // 3) Günün Manevi İklimi
      val spiritualIds = manager.getAppWidgetIds(ComponentName(context, SpiritualWidget::class.java))
      if (spiritualIds.isNotEmpty()) {
        val item = SpiritualQuotes.forToday()
        val showVerse = item.verseTurkish.isNotBlank()
        spiritualIds.forEach { id ->
          val (w, h) = sizeDp(manager, id, 320, 170)
          val v = RemoteViews(context.packageName, R.layout.widget_art_image)
          v.setImageViewBitmap(R.id.art, WidgetArt.spiritual(
            context, palette, w, h,
            if (showVerse) (if (en) "Verse of the Day" else "Günün Ayeti") else (if (en) "Hadith of the Day" else "Günün Hadisi"),
            if (showVerse) item.verseArabic else "",
            if (showVerse) item.verseTurkish else item.hadithTurkish,
            if (showVerse) item.verseSource else item.hadithSource
          ))
          v.setOnClickPendingIntent(R.id.widget_root, openApp)
          manager.updateAppWidget(id, v)
        }
      }

      // 4) Oruç & İftar
      val fastIds = manager.getAppWidgetIds(ComponentName(context, FastingWidget::class.java))
      if (fastIds.isNotEmpty()) {
        val todayKey = RamadanCalendar.dateKey(now)
        val ramadanDay = RamadanCalendar.dayOfRamadan(now)
        val fastToday = ramadanDay != null || prefs.getFastingDates().contains(todayKey)
        val imsakMs = at(now, ts[0]); val iftarMs = at(now, ts[4])
        val title = when {
          ramadanDay != null -> if (en) "Ramadan • Day $ramadanDay" else "Ramazan'ın $ramadanDay. günü"
          fastToday -> if (en) "Fasting day" else "Oruç günü"
          else -> if (en) "Holy days" else "Mübarek günler"
        }
        var label = ""; var big: String? = null; var progress: Float? = null; var target = 0L
        var centerLabel = ""; var leftLabel = ""; var rightLabel = ""
        when {
          fastToday && nowMs < imsakMs -> {
            label = if (en) "Until suhoor ends" else "İmsaka kalan"; target = imsakMs; progress = 0f
            leftLabel = (if (en) "Imsak " else "İmsak ") + ts[0]; rightLabel = (if (en) "Iftar " else "İftar ") + ts[4]; centerLabel = "%0"
          }
          fastToday && nowMs < iftarMs -> {
            val pr = ((nowMs - imsakMs).toFloat() / (iftarMs - imsakMs).coerceAtLeast(1)).coerceIn(0f, 1f)
            label = if (en) "Until iftar" else "İftara kalan"; target = iftarMs; progress = pr
            leftLabel = (if (en) "Imsak " else "İmsak ") + ts[0]; rightLabel = (if (en) "Iftar " else "İftar ") + ts[4]
            centerLabel = if (en) "${(pr * 100).toInt()}%" else "%${(pr * 100).toInt()}"
          }
          else -> {
            // Sıradaki kandil / bayram
            val y = now.get(Calendar.YEAR)
            val upcoming = (IslamicDays.forYear(y).values + IslamicDays.forYear(y + 1).values)
              .filter { (it.category == "Mübarek Gece" || (it.category == "Dini Bayram" && it.title.contains("1. Gün"))) && it.dateKey >= todayKey }
              .minByOrNull { it.dateKey }
            if (upcoming != null) {
              val p = upcoming.dateKey.split("-").map { it.toInt() }
              val target2 = Calendar.getInstance().apply { clear(); set(p[0], p[1] - 1, p[2]) }
              val today0 = Calendar.getInstance().apply { clear(); set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)) }
              val days = ((target2.timeInMillis - today0.timeInMillis) / 86_400_000L).toInt()
              val name = t(upcoming.title.split(" • ").first().replace(" 1. Gün", ""))
              label = if (en) "Next holy day" else "Sıradaki mübarek gün"
              big = if (days == 0) (if (en) "Tonight:\n$name" else "Bu gece:\n$name")
                    else "$name\n" + (if (en) "in $days days" else "$days gün kaldı")
              leftLabel = "${p[2]}.${p[1]}.${p[0]}"; rightLabel = (if (en) "Iftar " else "Akşam ") + ts[4]
            } else {
              label = ""; big = if (en) "Blessed days" else "Hayırlı günler"
            }
          }
        }
        fastIds.forEach { id ->
          val (w, h) = sizeDp(manager, id, 170, 170)
          val v = RemoteViews(context.packageName, R.layout.widget_art_fasting)
          v.setImageViewBitmap(R.id.art, WidgetArt.fasting(context, palette, w, h, title, label, big, progress, leftLabel, rightLabel, centerLabel))
          if (big == null && target > 0) { chrono(v, target); v.setViewVisibility(R.id.countdown, View.VISIBLE) }
          else v.setViewVisibility(R.id.countdown, View.GONE)
          v.setOnClickPendingIntent(R.id.widget_root, openApp)
          manager.updateAppWidget(id, v)
        }
      }

      scheduleRefresh(context, nextMs)
    } catch (_: Exception) {
      // Widget güncellemesi uygulamayı asla çökertmemeli
    }
  }

  /** Sonraki vakit, gece yarısı veya 15 dk sonrası (halka/çubuk ilerlemesi için) — hangisi önceyse. */
  private fun scheduleRefresh(context: Context, nextMillis: Long) {
    val manager = AppWidgetManager.getInstance(context)
    val any = PROVIDERS.any { manager.getAppWidgetIds(ComponentName(context, it)).isNotEmpty() }
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
    val at = minOf(nextMillis + 2_000L, midnight, System.currentTimeMillis() + 15 * 60_000L)
    // Uyandırmayan alarm: ezan alarmlarının Doze kotasını tüketmez (ekran açıldığında güncellenir)
    alarm.set(AlarmManager.RTC, at, pi)
  }

  enum class Kind { NEXT_PRAYER, PRAYER_TIMES, SPIRITUAL, FASTING }

  /** Android 8+ : widget'ı doğrudan ana ekrana ekleme isteği. Desteklenmiyorsa yönlendirme gösterir. */
  fun requestPin(context: Context, kind: Kind) {
    val cls = when (kind) {
      Kind.NEXT_PRAYER -> NextPrayerWidget::class.java
      Kind.PRAYER_TIMES -> PrayerTimesWidget::class.java
      Kind.SPIRITUAL -> SpiritualWidget::class.java
      Kind.FASTING -> FastingWidget::class.java
    }
    val en = AppLanguage.isEnglish
    val manager = AppWidgetManager.getInstance(context)
    val ok = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && manager.isRequestPinAppWidgetSupported &&
      runCatching { manager.requestPinAppWidget(ComponentName(context, cls), null, null) }.getOrDefault(false)
    if (!ok) {
      Toast.makeText(
        context,
        if (en) "Long-press an empty spot on your home screen → Widgets → Ezan & Vakit"
        else "Ana ekranda boş bir yere basılı tutun → Widget'lar → Ezan & Vakit",
        Toast.LENGTH_LONG
      ).show()
    }
  }

  @Suppress("unused") private val keepLocale = Locale.ROOT
}
