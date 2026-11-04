package com.example.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.R
import com.example.data.GreetingMessage
import com.example.data.GreetingOccasion
import java.io.File
import java.io.FileOutputStream

/**
 * Özel gün tebrik kartı (Cuma, kandil, Ramazan, bayram...) çizer.
 * Gece laciverti zemin, altın sivri kemer çerçeve, kemer içinde cami fotoğrafı ve asılı kandil,
 * altta Arapça hat, Türkçe/İngilizce mesaj ve uygulama adı. 1080x1920 (9:16, hikâye boyutu).
 */
object GreetingCardRenderer {

  const val W = 1080
  const val H = 1920

  private const val NAVY_TOP = 0xFF0C1424.toInt()
  private const val NAVY_BOTTOM = 0xFF04070E.toInt()
  private const val GOLD = 0xFFE5B869.toInt()
  private const val GOLD_LIGHT = 0xFFF6DDA0.toInt()
  private const val GOLD_DARK = 0xFF9E6B20.toInt()
  private const val CREAM = 0xFFF3EEE2.toInt()

  /** Kartı [scale] çarpanıyla bitmap olarak üretir (önizleme için 0.5, paylaşım için 1). */
  fun render(context: Context, occasion: GreetingOccasion, message: GreetingMessage, scale: Float = 1f): Bitmap {
    val bmp = Bitmap.createBitmap((W * scale).toInt(), (H * scale).toInt(), Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    canvas.scale(scale, scale)
    draw(context, canvas, occasion, message, scale)
    return bmp
  }

  private fun draw(context: Context, canvas: Canvas, occasion: GreetingOccasion, message: GreetingMessage, scale: Float) {
    val w = W.toFloat()
    val h = H.toFloat()
    val cx = w / 2f

    // 1. Zemin
    canvas.drawRect(0f, 0f, w, h, Paint().apply {
      shader = LinearGradient(0f, 0f, 0f, h, NAVY_TOP, NAVY_BOTTOM, Shader.TileMode.CLAMP)
    })
    // Yumuşak altın ışıltı (alt kısımda)
    canvas.drawRect(0f, 0f, w, h, Paint().apply {
      shader = RadialGradient(cx, h * 0.82f, w * 0.75f, withAlpha(GOLD, 0.16f), Color.TRANSPARENT, Shader.TileMode.CLAMP)
    })
    drawSparkles(canvas, w, h)

    // 2. Dış kemer çerçeve (çift çizgi)
    val goldStroke = Paint().apply {
      isAntiAlias = true; style = Paint.Style.STROKE; strokeWidth = 4f; color = GOLD
    }
    canvas.drawPath(StoryImageGenerator.archPath(56f, w - 56f, 520f, 120f, h - 56f), goldStroke)
    canvas.drawPath(StoryImageGenerator.archPath(78f, w - 78f, 536f, 150f, h - 78f), Paint(goldStroke).apply {
      strokeWidth = 1.6f; color = withAlpha(GOLD, 0.55f)
    })
    // Tepede sekiz köşeli yıldız rozeti
    canvas.drawPath(StoryImageGenerator.eightPointStarPath(cx, 120f, 46f), Paint().apply {
      isAntiAlias = true; color = NAVY_TOP
    })
    canvas.drawPath(StoryImageGenerator.eightPointStarPath(cx, 120f, 46f), Paint(goldStroke).apply { strokeWidth = 3f })
    canvas.drawPath(StoryImageGenerator.eightPointStarPath(cx, 120f, 26f), Paint(goldStroke).apply { strokeWidth = 2f })

    // 3. Fotoğraflı iç kemer
    val photoArch = StoryImageGenerator.archPath(140f, w - 140f, 640f, 300f, 1320f)
    canvas.save()
    canvas.clipPath(photoArch)
    val photo = decodePhoto(context, occasion.photoRes, (820 * scale).toInt().coerceAtLeast(200))
    if (photo != null) {
      val dst = RectF(140f, 300f, w - 140f, 1320f)
      canvas.drawBitmap(photo, centerCrop(photo, dst), dst, Paint(Paint.FILTER_BITMAP_FLAG))
      photo.recycle()
    } else {
      canvas.drawRect(140f, 300f, w - 140f, 1320f, Paint().apply { color = 0xFF1C2A44.toInt() })
    }
    // Üstte hafif, altta koyu geçiş
    canvas.drawRect(140f, 300f, w - 140f, 1320f, Paint().apply {
      shader = LinearGradient(0f, 300f, 0f, 1320f,
        intArrayOf(withAlpha(NAVY_TOP, 0.35f), Color.TRANSPARENT, withAlpha(NAVY_BOTTOM, 0.92f)),
        floatArrayOf(0f, 0.45f, 1f), Shader.TileMode.CLAMP)
    })
    drawLantern(canvas, cx, 300f)
    canvas.restore()
    canvas.drawPath(photoArch, Paint(goldStroke).apply { strokeWidth = 5f })

    // 4. Arapça hat + mesaj (kemerin alt kısmından başlar)
    val arabicPaint = TextPaint().apply {
      isAntiAlias = true; color = GOLD; textSize = 96f; textAlign = Paint.Align.LEFT
      typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
      setShadowLayer(16f, 0f, 0f, withAlpha(GOLD, 0.45f))
    }
    val headPaint = TextPaint().apply {
      isAntiAlias = true; color = CREAM; textSize = 76f
      typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
    }
    val bodyPaint = TextPaint().apply {
      isAntiAlias = true; color = withAlpha(CREAM, 0.82f); textSize = 42f
      typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
    }
    val textWidth = (w - 340f).toInt()
    var arabicL: StaticLayout
    var headL: StaticLayout
    var bodyL: StaticLayout
    val top = 1180f
    val bottomLimit = 1740f
    while (true) {
      arabicL = layout(occasion.arabic, arabicPaint, textWidth)
      headL = layout(message.headline.text, headPaint, textWidth)
      bodyL = layout(message.body.text, bodyPaint, textWidth)
      val total = arabicL.height + 26f + headL.height + 26f + bodyL.height
      if (top + total <= bottomLimit || headPaint.textSize < 46f) break
      arabicPaint.textSize *= 0.92f; headPaint.textSize *= 0.92f; bodyPaint.textSize *= 0.94f
    }
    var y = top
    fun drawLayout(l: StaticLayout, gap: Float) {
      canvas.save(); canvas.translate(170f, y); l.draw(canvas); canvas.restore()
      y += l.height + gap
    }
    drawLayout(arabicL, 26f)
    drawLayout(headL, 26f)
    drawLayout(bodyL, 0f)

    // 5. Alt bilgi: süs çizgisi + uygulama adı
    val divider = Paint().apply { isAntiAlias = true; color = withAlpha(GOLD, 0.7f); strokeWidth = 2.5f }
    canvas.drawLine(cx - 200f, 1780f, cx - 30f, 1780f, divider)
    canvas.drawLine(cx + 30f, 1780f, cx + 200f, 1780f, divider)
    canvas.drawPath(StoryImageGenerator.eightPointStarPath(cx, 1780f, 14f), Paint().apply { isAntiAlias = true; color = GOLD })
    canvas.drawText(context.getString(R.string.app_name).uppercase(), cx, 1836f, Paint().apply {
      isAntiAlias = true; color = GOLD_LIGHT; textSize = 34f; textAlign = Paint.Align.CENTER
      letterSpacing = 0.22f; typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
    })
  }

  /** Kemer tepesinden zincirle sarkan, içi ışıldayan altın kandil. */
  private fun drawLantern(canvas: Canvas, cx: Float, apexY: Float) {
    val gold = Paint().apply { isAntiAlias = true; color = GOLD }
    val chain = Paint().apply { isAntiAlias = true; color = GOLD; strokeWidth = 3f }
    val top = apexY + 10f
    canvas.drawLine(cx, top, cx, top + 120f, chain)
    // Işık halesi
    canvas.drawCircle(cx, top + 215f, 140f, Paint().apply {
      isAntiAlias = true
      shader = RadialGradient(cx, top + 215f, 140f, withAlpha(0xFFFFD27A.toInt(), 0.55f), Color.TRANSPARENT, Shader.TileMode.CLAMP)
    })
    // Üst kubbe
    canvas.drawCircle(cx, top + 122f, 8f, gold)
    val cap = Path().apply {
      moveTo(cx, top + 128f); lineTo(cx + 36f, top + 160f); lineTo(cx - 36f, top + 160f); close()
    }
    canvas.drawPath(cap, gold)
    // Gövde (cam) — ışıklı
    val body = RectF(cx - 34f, top + 160f, cx + 34f, top + 268f)
    canvas.drawRoundRect(body, 10f, 10f, Paint().apply {
      isAntiAlias = true
      shader = LinearGradient(0f, body.top, 0f, body.bottom, 0xFFFFF1C4.toInt(), 0xFFF0B04A.toInt(), Shader.TileMode.CLAMP)
    })
    val frame = Paint().apply { isAntiAlias = true; style = Paint.Style.STROKE; strokeWidth = 4f; color = GOLD_DARK }
    canvas.drawRoundRect(body, 10f, 10f, frame)
    canvas.drawLine(cx, body.top, cx, body.bottom, frame)
    // Alt taban
    val base = Path().apply {
      moveTo(cx - 40f, top + 268f); lineTo(cx + 40f, top + 268f); lineTo(cx + 18f, top + 292f); lineTo(cx - 18f, top + 292f); close()
    }
    canvas.drawPath(base, gold)
    canvas.drawCircle(cx, top + 302f, 7f, gold)
  }

  private fun drawSparkles(canvas: Canvas, w: Float, h: Float) {
    val rnd = java.util.Random(7)
    val p = Paint().apply { isAntiAlias = true }
    repeat(140) {
      val x = rnd.nextFloat() * w
      val y = h * 0.55f + rnd.nextFloat() * h * 0.45f
      p.color = withAlpha(GOLD_LIGHT, 0.15f + rnd.nextFloat() * 0.45f)
      canvas.drawCircle(x, y, 1.2f + rnd.nextFloat() * 2.4f, p)
    }
    repeat(40) {
      p.color = withAlpha(CREAM, 0.15f + rnd.nextFloat() * 0.35f)
      canvas.drawCircle(rnd.nextFloat() * w, rnd.nextFloat() * h * 0.3f, 1f + rnd.nextFloat() * 1.6f, p)
    }
  }

  private fun layout(text: String, paint: TextPaint, width: Int): StaticLayout =
    StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
      .setAlignment(Layout.Alignment.ALIGN_CENTER)
      .setLineSpacing(4f, 1.08f)
      .build()

  private fun decodePhoto(context: Context, resId: Int, targetWidth: Int): Bitmap? = try {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeResource(context.resources, resId, bounds)
    var sample = 1
    while (bounds.outWidth / (sample * 2) >= targetWidth) sample *= 2
    BitmapFactory.decodeResource(context.resources, resId, BitmapFactory.Options().apply { inSampleSize = sample })
  } catch (_: Throwable) {
    null
  }

  private fun centerCrop(src: Bitmap, dst: RectF): Rect {
    val srcRatio = src.width.toFloat() / src.height
    val dstRatio = dst.width() / dst.height()
    return if (srcRatio > dstRatio) {
      val newW = (src.height * dstRatio).toInt()
      val left = (src.width - newW) / 2
      Rect(left, 0, left + newW, src.height)
    } else {
      val newH = (src.width / dstRatio).toInt()
      val topOff = ((src.height - newH) * 0.35f).toInt()
      Rect(0, topOff, src.width, topOff + newH)
    }
  }

  private fun withAlpha(color: Int, alpha: Float): Int =
    (color and 0x00FFFFFF) or ((alpha.coerceIn(0f, 1f) * 255).toInt() shl 24)

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
