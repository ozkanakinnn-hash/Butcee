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
import com.example.data.quote
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
      load(R.font.inter_extrabold, Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)),
      load(R.font.playfair_display_italic, Typeface.create(Typeface.SERIF, Typeface.ITALIC))
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

  // Yerleşim (540x960 tasarım ölçeği): kemerli fotoğraf görselde; metin 500'den, ayet paneli 728'den başlar
  private const val TEXT_TOP = 500f
  private const val PANEL_TOP = 728f
  private const val FOOTER_Y = 912f
  private const val SIDE = 60f
  private val GOLD = 0xFFA8762A.toInt()
  private val GOLD_LIGHT = 0xFFF5D98F.toInt()

  private fun draw(context: Context, canvas: Canvas, occasion: GreetingOccasion, message: GreetingMessage, scale: Float) {
    val st = occasion.style
    val f = fonts(context)
    val amiri = f[0]; val playfair = f[1]; val inter = f[2]; val interBold = f[3]; val playfairItalic = f[4]
    val accent = if (st.dark) GOLD_LIGHT else GOLD

    // 1. Arka plan (fotoğraf, kemer, çerçeve, süslemeler)
    val bg = decode(context, occasion.bgRes, scale)
    if (bg != null) {
      canvas.drawBitmap(bg, null, RectF(0f, 0f, W.toFloat(), H.toFloat()), Paint(Paint.FILTER_BITMAP_FLAG))
      bg.recycle()
    } else {
      canvas.drawColor(if (st.dark) 0xFF0B0910.toInt() else 0xFFFBF6EC.toInt())
    }

    // 2. Arapça hat + başlık + mesaj (panelin üstüne taşmayana kadar küçültülür)
    val width = (W - 2 * SIDE * K).toInt()
    val arPaint = TextPaint().apply { isAntiAlias = true; typeface = amiri; color = st.arabic.toInt(); textSize = 40 * K }
    val headPaint = TextPaint().apply { isAntiAlias = true; typeface = playfair; color = st.ink.toInt(); textSize = 28 * K }
    val bodyPaint = TextPaint().apply { isAntiAlias = true; typeface = inter; color = st.muted.toInt(); textSize = 15 * K }
    var ar: StaticLayout; var hd: StaticLayout; var bd: StaticLayout
    val top = TEXT_TOP * K
    val limit = (PANEL_TOP - 22) * K
    while (true) {
      ar = layout(occasion.arabic, arPaint, width, 1.2f)
      hd = layout(message.headline.text, headPaint, width, 1.12f)
      bd = layout(message.body.text, bodyPaint, width, 1.4f)
      val total = ar.height + 2 * K + hd.height + 8 * K + bd.height
      if (top + total <= limit || headPaint.textSize < 18 * K) break
      arPaint.textSize *= 0.93f; headPaint.textSize *= 0.93f; bodyPaint.textSize *= 0.95f
    }
    var y = top
    for ((l, gap) in listOf(ar to 2 * K, hd to 8 * K, bd to 0f)) {
      canvas.save(); canvas.translate(SIDE * K, y); l.draw(canvas); canvas.restore()
      y += l.height + gap
    }

    // 3. Ayet / hadis paneli
    val q = occasion.quote
    val pLeft = 58 * K; val pRight = W - 58 * K
    val inner = (pRight - pLeft - 44 * K).toInt()
    val quotePaint = TextPaint().apply { isAntiAlias = true; typeface = playfairItalic; color = st.ink.toInt(); textSize = 16 * K }
    val srcPaint = TextPaint().apply {
      isAntiAlias = true; typeface = interBold; color = accent; textSize = 10.5f * K; letterSpacing = 0.19f
    }
    var ql: StaticLayout
    var sl: StaticLayout
    val maxPanelBottom = (FOOTER_Y - 30) * K
    while (true) {
      ql = layout("“" + q.text.text + "”", quotePaint, inner, 1.35f)
      sl = layout(q.source.uppercase(Locale.forLanguageTag("tr")), srcPaint, inner, 1f)
      val h = 20 * K + ql.height + 8 * K + sl.height + 14 * K
      if (PANEL_TOP * K + h <= maxPanelBottom || quotePaint.textSize < 11 * K) break
      quotePaint.textSize *= 0.93f
    }
    val panelH = 20 * K + ql.height + 8 * K + sl.height + 14 * K
    val rect = RectF(pLeft, PANEL_TOP * K, pRight, PANEL_TOP * K + panelH)
    canvas.drawRoundRect(rect, 16 * K, 16 * K, Paint().apply { isAntiAlias = true; color = st.panel.toInt() })
    canvas.drawRoundRect(rect, 16 * K, 16 * K, Paint().apply { isAntiAlias = true; style = Paint.Style.STROKE; strokeWidth = 1.2f * K; color = GOLD })
    // Panelin üstünde sekiz köşeli yıldız rozeti
    val badge = StoryImageGenerator.eightPointStarPath(W / 2f, PANEL_TOP * K, 13 * K)
    canvas.drawPath(badge, Paint().apply { isAntiAlias = true; color = if (st.dark) 0xFF141018.toInt() else 0xFFFBF6EC.toInt() })
    canvas.drawPath(badge, Paint().apply { isAntiAlias = true; style = Paint.Style.STROKE; strokeWidth = 1.6f * K; color = GOLD })
    var py = PANEL_TOP * K + 20 * K
    canvas.save(); canvas.translate(pLeft + 22 * K, py); ql.draw(canvas); canvas.restore()
    py += ql.height + 8 * K
    canvas.save(); canvas.translate(pLeft + 22 * K, py); sl.draw(canvas); canvas.restore()

    // 4. Alt bilgi
    val name = context.getString(R.string.app_name).uppercase(Locale.forLanguageTag("tr"))
    canvas.drawText("✦  $name  ✦", W / 2f, FOOTER_Y * K + 10 * K, Paint().apply {
      isAntiAlias = true; typeface = interBold; color = accent
      textSize = 11 * K; textAlign = Paint.Align.CENTER; letterSpacing = 0.36f
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
