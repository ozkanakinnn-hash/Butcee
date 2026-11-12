package akinai.app1

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

/**
 * Haber bildirimleri (sunucu: kind = "news_alert", yalnızca veri). Bildirimi uygulama kendisi kurar ki üzerine
 * "Daha az bildirim" düğmesi eklenebilsin (FCM'in kendi gösterdiği bildirimlere düğme eklenemez).
 *  - Kanal, başlık, metin ve görsel sunucudan gelir (n_channel, n_title, n_body, n_image).
 *  - Dokununca uygulama açılır ve haber gösterilir (MainActivity → Flutter "akinai/launch" kanalı).
 *  - Kullanıcı zaten "Sadece önemli" seviyesindeyse düğme gösterilmez.
 */
object NewsNotifier {
    const val KIND = "news_alert"
    const val EXTRA_PAYLOAD = "akinai_payload"
    private const val NOTIF_ID = 7200
    private val BRAND = Color.parseColor("#0B1F3A")

    fun show(context: Context, d: Map<String, String>) {
        val title = d["n_title"] ?: return
        val body = d["n_body"].orEmpty()
        val channel = d["n_channel"]?.takeIf { it.isNotEmpty() } ?: "news"
        val tag = d["n_tag"]?.takeIf { it.isNotEmpty() } ?: "news_${d["article_id"].orEmpty()}"
        val prefs = context.getSharedPreferences("FlutterSharedPreferences", Context.MODE_PRIVATE)
        val lang = prefs.getString("flutter.language", null)
        val en = (if (lang == null || lang == "system") Locale.getDefault().language else lang).startsWith("en")
        val level = prefs.getString("flutter.notif_level", "balanced")

        val payload = JSONObject().apply {
            for (k in listOf("type", "url", "title", "source", "category_id", "article_id", "fixture_id")) {
                d[k]?.let { put(k, it) }
            }
        }.toString()
        val open = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_PAYLOAD, payload)
        }
        val firstLine = body.lineSequence().firstOrNull().orEmpty()
        val builder = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_stat_news)
            .setColor(BRAND)
            .setContentTitle(title)
            .setContentText(firstLine)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
            .setPriority(if (channel == "breaking_news") NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
        open?.let {
            builder.setContentIntent(
                PendingIntent.getActivity(context, tag.hashCode(), it, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE),
            )
        }

        // Görsel: tek satırlık haberde büyük resim; çok satırlı özet/bültende küçük resim (metin okunur kalsın).
        loadBitmap(d["n_image"])?.let { bmp ->
            builder.setLargeIcon(bmp)
            if (body.lines().size <= 2) {
                builder.setStyle(NotificationCompat.BigPictureStyle().bigPicture(bmp).bigLargeIcon(null as Bitmap?).setSummaryText(firstLine))
            }
        }

        if (level != "important") {
            val less = Intent(context, LessNotificationsReceiver::class.java).apply {
                action = LessNotificationsReceiver.ACTION
                putExtra(LessNotificationsReceiver.EXTRA_TAG, tag)
                putExtra(LessNotificationsReceiver.EXTRA_ID, NOTIF_ID)
            }
            builder.addAction(
                0,
                if (en) "Fewer notifications" else "Daha az bildirim",
                PendingIntent.getBroadcast(context, tag.hashCode(), less, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE),
            )
        }

        try {
            NotificationManagerCompat.from(context).notify(tag, NOTIF_ID, builder.build())
        } catch (_: SecurityException) {
            // Bildirim izni yok
        }
    }

    /** Görseli en fazla ~5 sn'de indirir; büyükse küçültür. Hata → görselsiz bildirim. */
    private fun loadBitmap(url: String?): Bitmap? {
        if (url.isNullOrEmpty() || !url.startsWith("https://")) return null
        return try {
            val c = URL(url).openConnection() as HttpURLConnection
            c.connectTimeout = 4000
            c.readTimeout = 5000
            c.setRequestProperty("User-Agent", "HaberAkisi/1.0")
            val bytes = c.inputStream.use { it.readBytes() }
            c.disconnect()
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
            var sample = 1
            while (bounds.outWidth / sample > 1200) sample *= 2
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, BitmapFactory.Options().apply { inSampleSize = sample })
        } catch (_: Exception) {
            null
        }
    }
}
