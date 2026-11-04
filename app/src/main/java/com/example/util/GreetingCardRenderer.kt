package com.example.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import com.example.R
import com.example.data.GreetingMessage
import com.example.data.GreetingOccasion
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

/**
 * Özel gün tebrik kartı (Cuma, kandil, Ramazan, bayram...).
 * Her güne özel arka plan sahnesi (res/drawable-nodpi/card_bg_*.webp, uygulama temalarına göre:
 * Saray, İznik Çinisi, Zümrüt) üzerine Arapça hat (Amiri), başlık (Playfair Display),
 * mesaj ve uygulama adı (Inter) çizilir. 1080x1920 (9:16, hikâye boyutu).
 */
object GreetingCardRenderer {

  const val W = 1080
  const val H = 1920
  /** Tasarım ölçeği 540x960; çizimde 2 ile çarpılır. */
  private const val K = 2f

  private var fonts: Array<Typeface>? = null

  /** [Amiri, Playfair, Inter Regular, Inter ExtraBold] — bulunamazsa sistem fontları. */
  private fun fonts(context: Context): Array<Typeface> = fonts ?: run {
    fun load(id: Int, fallback: Typeface) = try { ResourcesCompat.getFont(context, id) ?: fallback } catch (_: Exception) { fallback }
    arrayOf(
      load(R.font.amiri_bold, Typeface.create(Typeface.SERIF, Typeface.BOLD)),
      load(R.font.playfair_display_bold, Typeface.create(Typeface.SERIF, Typeface.BOLD)),
      load(R.font.inter_regular, Typeface.SANS_SERIF),
      load(R.font.inter_extrabold, Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD))
    ).also { fonts = it }
  }

  /** Kartı [scale] çarpanıyla bitmap olarak üretir (önizleme için 0.45, paylaşım için 1). */
  fun render(context: Context, occasion: GreetingOccasion, message: GreetingMessage, scale: Float = 1f): Bitmap {
    val bmp = Bitmap.createBitmap((W * scale).toInt(), (H * scale).toInt(), Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    canvas.scale(scale, scale)
    draw(context, canvas, occasion, message, scale)
    return bmp
  }

  private fun draw(context: Context, canvas: Canvas, occasion: GreetingOccasion, message: GreetingMessage, scale: Float) {
    val st = occasion.style
    val (amiri, playfair, interRegular, interBold) = fonts(context)

    // 1. Arka plan sahnesi
    val bg = decode(context, occasion.bgRes, scale)
    if (bg != null) {
      canvas.drawBitmap(bg, null, RectF(0f, 0f, W.toFloat(), H.toFloat()), Paint(Paint.FILTER_BITMAP_FLAG))
      bg.recycle()
    } else {
      canvas.drawColor(st.footerColor.toInt())
    }

    // 2. Metin bloğu: Arapça hat + başlık + mesaj
    val width = (W - 2 * st.inset * K).toInt()
    val arPaint = TextPaint().apply { isAntiAlias = true; typeface = amiri; color = st.arabicColor.toInt(); textSize = st.arabicSize * K }
    val headPaint = TextPaint().apply { isAntiAlias = true; typeface = playfair; color = st.headColor.toInt(); textSize = st.headSize * K }
    val bodyPaint = TextPaint().apply { isAntiAlias = true; typeface = interRegular; color = st.bodyColor.toInt(); textSize = 15 * K }
    val top = st.top * K
    val limit = (st.footerY - 34) * K
    var ar: StaticLayout; var hd: StaticLayout; var bd: StaticLayout
    while (true) {
      ar = layout(occasion.arabic, arPaint, width, 1.25f)
      hd = layout(message.headline.text, headPaint, width, 1.12f)
      bd = layout(message.body.text, bodyPaint, width, 1.4f)
      val total = ar.height + 6 * K + hd.height + 12 * K + bd.height
      if (top + total <= limit || headPaint.textSize < 18 * K) break
      arPaint.textSize *= 0.93f; headPaint.textSize *= 0.93f; bodyPaint.textSize *= 0.95f
    }
    var y = top
    for ((l, gap) in listOf(ar to 6 * K, hd to 12 * K, bd to 0f)) {
      canvas.save(); canvas.translate(st.inset * K, y); l.draw(canvas); canvas.restore()
      y += l.height + gap
    }

    // 3. Alt bilgi
    val name = context.getString(R.string.app_name).uppercase(Locale.forLanguageTag("tr"))
    canvas.drawText("✦  $name  ✦", W / 2f, (st.footerY + 10) * K, Paint().apply {
      isAntiAlias = true; typeface = interBold; color = st.footerColor.toInt()
      textSize = 12 * K; textAlign = Paint.Align.CENTER; letterSpacing = 0.33f
    })
  }

  private fun layout(text: String, paint: TextPaint, width: Int, mult: Float): StaticLayout =
    StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
      .setAlignment(Layout.Alignment.ALIGN_CENTER)
      .setLineSpacing(0f, mult)
      .setIncludePad(false)
      .setBreakStrategy(Layout.BREAK_STRATEGY_BALANCED)
      .build()

  private fun decode(context: Context, resId: Int, scale: Float): Bitmap? = try {
    val opts = BitmapFactory.Options().apply {
      inSampleSize = if (scale <= 0.5f) 2 else 1
      inScaled = false
    }
    BitmapFactory.decodeResource(context.resources, resId, opts)
  } catch (_: Throwable) {
    null
  }

  private fun saveUri(context: Context, occasion: GreetingOccasion, message: GreetingMessage): Uri {
    val bmp = render(context, occasion, message, 1f)
    val dir = File(context.cacheDir, "shared_images").apply { mkdirs() }
    val file = File(dir, "ozel_gun_karti.png")
    FileOutputStream(file).use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
    bmp.recycle()
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
  }

  /** Android paylaşım menüsü (WhatsApp, Telegram, vb.). */
  fun share(context: Context, occasion: GreetingOccasion, message: GreetingMessage) {
    try {
      val uri = saveUri(context, occasion, message)
      val send = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_TEXT, message.headline.text)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      }
      context.startActivity(Intent.createChooser(send, message.headline.text).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (e: Exception) {
      Toast.makeText(context, e.message ?: "Error", Toast.LENGTH_LONG).show()
    }
  }

  /** Instagram hikâyesi olarak paylaş; Instagram yoksa genel paylaşıma düşer. */
  fun shareToInstagram(context: Context, occasion: GreetingOccasion, message: GreetingMessage) {
    val pkg = "com.instagram.android"
    val uri = try { saveUri(context, occasion, message) } catch (e: Exception) {
      Toast.makeText(context, e.message ?: "Error", Toast.LENGTH_LONG).show(); return
    }
    context.grantUriPermission(pkg, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
    val appId = context.getString(R.string.facebook_app_id).trim()
    if (appId.isNotEmpty()) {
      try {
        context.startActivity(Intent("com.instagram.share.ADD_TO_STORY").apply {
          setPackage(pkg)
          putExtra("source_application", appId)
          setDataAndType(uri, "image/png")
          addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        })
        return
      } catch (_: ActivityNotFoundException) { }
    }
    try {
      context.startActivity(Intent(Intent.ACTION_SEND).apply {
        setPackage(pkg)
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
      })
    } catch (_: ActivityNotFoundException) {
      share(context, occasion, message)
    }
  }
}
