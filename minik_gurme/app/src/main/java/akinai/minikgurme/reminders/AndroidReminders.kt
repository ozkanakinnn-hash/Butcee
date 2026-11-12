package akinai.minikgurme.reminders

import akinai.minikgurme.data.local.getDatabase
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import akinai.minikgurme.MainActivity
import akinai.minikgurme.R
import akinai.minikgurme.data.local.AppDatabase
import akinai.minikgurme.i18n.AppLanguage
import akinai.minikgurme.i18n.Translator
import akinai.minikgurme.i18n.t
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * Yerel hatırlatıcılar (internet gerekmez). Günlük tekrar eden, pil dostu (tam zamanlı olmayan) alarmlar.
 *  - Alerjen kontrolü (ücretsiz): her akşam 19:00, devam eden bir 3 gün denemesi varsa.
 *  - Öğün hatırlatıcıları (Plus): 08:30, 12:30, 18:00.
 */
object AndroidReminders {
    const val CHANNEL = "reminders"

    data class Slot(val code: Int, val hour: Int, val minute: Int, val kind: String)
    val ALLERGEN = Slot(100, 19, 0, "allergen")
    val MEALS = listOf(Slot(101, 8, 30, "meal_breakfast"), Slot(102, 12, 30, "meal_lunch"), Slot(103, 18, 0, "meal_dinner"))

    /** Ortak [Reminders] ayarları değişince alarmları yeniden kurar. */
    fun install(c: Context) {
        val app = c.applicationContext
        Reminders.scheduler = Reminders.Scheduler { reschedule(app) }
    }

    fun ensureChannel(c: Context) {
        if (Build.VERSION.SDK_INT >= 26) {
            val nm = c.getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(NotificationChannel(CHANNEL, t("Hatırlatıcılar"), NotificationManager.IMPORTANCE_DEFAULT))
        }
    }

    private fun pending(c: Context, s: Slot): PendingIntent =
        PendingIntent.getBroadcast(c, s.code, Intent(c, ReminderReceiver::class.java).putExtra("kind", s.kind),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

    /** Tüm alarmları tercihlere göre yeniden kurar (açılışta, ayar değişince, yeniden başlatmada). */
    fun reschedule(c: Context, plus: Boolean = akinai.minikgurme.billing.PlusManager.isPlus) {
        val am = c.getSystemService(AlarmManager::class.java) ?: return
        val wanted = buildList {
            if (Reminders.allergenOn()) add(ALLERGEN)
            if (Reminders.mealsOn() && plus) addAll(MEALS)
        }
        (MEALS + ALLERGEN).forEach { am.cancel(pending(c, it)) }
        wanted.forEach { s ->
            val next = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, s.hour); set(Calendar.MINUTE, s.minute); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
            }
            am.setInexactRepeating(AlarmManager.RTC_WAKEUP, next.timeInMillis, AlarmManager.INTERVAL_DAY, pending(c, s))
        }
    }

    fun notify(c: Context, id: Int, title: String, text: String) {
        ensureChannel(c)
        val open = PendingIntent.getActivity(c, 0, Intent(c, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val n = NotificationCompat.Builder(c, CHANNEL)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title).setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(open).setAutoCancel(true).build()
        try { NotificationManagerCompat.from(c).notify(id, n) } catch (_: SecurityException) { /* izin yok */ }
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val kind = intent.getStringExtra("kind") ?: return
        akinai.minikgurme.data.local.PlatformContext.init(context); AppLanguage.init()
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Translator.load(akinai.minikgurme.i18n.composeFileReader(context))
                when (kind) {
                    "allergen" -> {
                        val ongoing = AppDatabase.getDatabase(context).allergenTestDao().getAllTests().first().filter { it.status == "Devam Ediyor" }
                        if (ongoing.isNotEmpty()) {
                            val names = ongoing.joinToString(", ") { t(it.food) }
                            AndroidReminders.notify(context, 100, t("3 gün kuralı: belirtileri kontrol edin"),
                                t("Devam eden deneme") + ": $names. " + t("Kızarıklık, kusma veya ishal oldu mu? Günü kaydetmeyi unutmayın."))
                        }
                    }
                    "meal_breakfast" -> AndroidReminders.notify(context, 101, t("Kahvaltı zamanı"), t("Bugünün kahvaltısını kaydetmeyi unutmayın."))
                    "meal_lunch" -> AndroidReminders.notify(context, 102, t("Öğle yemeği zamanı"), t("Öğle öğününü kaydetmeyi unutmayın."))
                    "meal_dinner" -> AndroidReminders.notify(context, 103, t("Akşam yemeği zamanı"), t("Akşam öğününü kaydetmeyi unutmayın."))
                }
            } finally { pending.finish() }
        }
    }
}

/** Telefon yeniden başlayınca veya uygulama güncellenince hatırlatıcıları yeniden kurar. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        akinai.minikgurme.data.local.PlatformContext.init(context)
        val p = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        AndroidReminders.reschedule(context, plus = p.getBoolean("plus_active_cache", false))
    }
}
