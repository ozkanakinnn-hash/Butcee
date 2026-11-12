package akinai.app1

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.SystemClock
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.util.Locale

/**
 * Canlı skor paneli: maç boyunca bildirim alanında SABİT duran, yerinde güncellenen bildirim.
 *  - Aynı maç için tek bildirim (kimlik = fixture_id); skor değişince yeni bildirim açılmaz, güncellenir.
 *  - Dakika telefonda kendi sayar (Chronometer) → sunucu her dakika veri göndermek zorunda değil.
 *  - Sessiz kanal (`live_score`); gol uyarısı ayrı "goals" kanalından sesli gelir.
 *  - Maç bitince "MS" gösterir, kaydırılıp kapatılabilir ve 2 saat sonra kendiliğinden kalkar.
 *  - Ayarlar'daki "Canlı skor paneli" kapalıysa gösterilmez (Flutter SharedPreferences: flutter.liveScorePanel).
 */
object LiveScoreNotifier {
    const val TYPE = "match_live"
    private const val CHANNEL = "live_score"
    private val LIVE = setOf("1H", "2H", "ET", "LIVE")

    fun show(context: Context, d: Map<String, String>) {
        val prefs = context.getSharedPreferences("FlutterSharedPreferences", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("flutter.liveScorePanel", true)) return
        val fixtureId = d["fixture_id"] ?: return
        val lang = prefs.getString("flutter.language", null)
        val en = (if (lang == null || lang == "system") Locale.getDefault().language else lang).startsWith("en")
        ensureChannel(context, en)

        val status = d["status"] ?: "NS"
        val finished = d["finished"] == "1"
        val homeShort = d["home_short"].orEmpty()
        val awayShort = d["away_short"].orEmpty()
        val gh = d["gh"] ?: "0"
        val ga = d["ga"] ?: "0"

        val rv = RemoteViews(context.packageName, R.layout.notification_live_score)
        rv.setTextViewText(R.id.live_league, d["league"] ?: "Süper Lig")
        rv.setTextViewText(R.id.live_home_short, homeShort)
        rv.setTextViewText(R.id.live_away_short, awayShort)
        rv.setTextViewText(R.id.live_home_name, d["home_name"].orEmpty())
        rv.setTextViewText(R.id.live_away_name, d["away_name"].orEmpty())
        rv.setInt(R.id.live_home_dot, "setColorFilter", parseColor(d["home_color"]))
        rv.setInt(R.id.live_away_dot, "setColorFilter", parseColor(d["away_color"]))
        // Milli maç: renk noktası yerine bayrak (sunucu bayrak emojisi gönderir)
        for ((flag, dot, value) in listOf(
            Triple(R.id.live_home_flag, R.id.live_home_dot, d["home_flag"]),
            Triple(R.id.live_away_flag, R.id.live_away_dot, d["away_flag"]),
        )) {
            val hasFlag = !value.isNullOrEmpty()
            rv.setTextViewText(flag, value.orEmpty())
            rv.setViewVisibility(flag, if (hasFlag) View.VISIBLE else View.GONE)
            rv.setViewVisibility(dot, if (hasFlag) View.GONE else View.VISIBLE)
        }

        if (status == "NS") {
            rv.setTextViewText(R.id.live_score, d["kickoff_text"] ?: "– : –")
        } else {
            rv.setTextViewText(R.id.live_score, "$gh – $ga")
        }

        // Dakika: canlıysa Chronometer, değilse durum yazısı (İY, MS, …)
        if (status in LIVE) {
            val elapsedMin = (d["elapsed"] ?: "0").toLongOrNull() ?: 0L
            val sentMs = (d["sent_ms"] ?: "0").toLongOrNull() ?: System.currentTimeMillis()
            val lag = (System.currentTimeMillis() - sentMs).coerceIn(0L, 10 * 60_000L)
            val base = SystemClock.elapsedRealtime() - (elapsedMin * 60_000L + lag)
            rv.setViewVisibility(R.id.live_minute, View.VISIBLE)
            rv.setViewVisibility(R.id.live_status, View.GONE)
            rv.setChronometer(R.id.live_minute, base, null, true)
        } else {
            rv.setViewVisibility(R.id.live_minute, View.GONE)
            rv.setViewVisibility(R.id.live_status, View.VISIBLE)
            rv.setTextViewText(R.id.live_status, statusLabel(status, en))
        }

        val open = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(NewsNotifier.EXTRA_PAYLOAD, org.json.JSONObject(mapOf("type" to "match", "fixture_id" to fixtureId)).toString())
        }
        val pi = open?.let {
            PendingIntent.getActivity(
                context, fixtureId.hashCode(), it,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }

        val builder = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(R.drawable.ic_stat_news)
            .setColor(Color.parseColor("#0B1F3A"))
            .setCustomContentView(rv)
            .setCustomBigContentView(rv)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setContentTitle("$homeShort $gh – $ga $awayShort")
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setShowWhen(false)
            .setOngoing(!finished)
            .setAutoCancel(finished)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        if (pi != null) builder.setContentIntent(pi)
        if (finished) builder.setTimeoutAfter(2 * 60 * 60 * 1000L)

        try {
            NotificationManagerCompat.from(context).notify("live_$fixtureId", 7100, builder.build())
        } catch (_: SecurityException) {
            // Bildirim izni yok → sessizce geç
        }
    }

    private fun statusLabel(status: String, en: Boolean): String = when (status) {
        "NS" -> if (en) "Kick-off" else "Başlamadı"
        "HT" -> if (en) "HT" else "İY"
        "BT" -> if (en) "Break" else "Ara"
        "P" -> if (en) "Penalties" else "Penaltılar"
        "FT" -> if (en) "FT" else "MS"
        "AET" -> if (en) "AET" else "UZ. MS"
        "PEN" -> if (en) "Pens" else "PEN. MS"
        "PST" -> if (en) "Postponed" else "Ertelendi"
        "CANC" -> if (en) "Cancelled" else "İptal"
        "SUSP", "INT" -> if (en) "Suspended" else "Durduruldu"
        else -> status
    }

    private fun parseColor(hex: String?): Int = try {
        Color.parseColor(if (hex.isNullOrBlank()) "#8E8E93" else hex)
    } catch (_: IllegalArgumentException) {
        Color.parseColor("#8E8E93")
    }

    private fun ensureChannel(context: Context, en: Boolean) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = context.getSystemService(NotificationManager::class.java) ?: return
        if (nm.getNotificationChannel(CHANNEL) != null) return
        val ch = NotificationChannel(
            CHANNEL,
            if (en) "Live score" else "Canlı skor",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = if (en) "Live score panel for your teams' matches" else "Takip ettiğin takımların maçlarında canlı skor paneli"
            setSound(null, null)
            enableVibration(false)
            setShowBadge(false)
        }
        nm.createNotificationChannel(ch)
    }
}
