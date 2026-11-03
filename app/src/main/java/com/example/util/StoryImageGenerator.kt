package com.example.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.R
import com.example.data.SpiritualItem
import java.io.File
import java.io.FileOutputStream

object StoryImageGenerator {

  private const val INSTAGRAM_PACKAGE = "com.instagram.android"

  /** 9:16 (1080x1920) hikâye görselini üretir, önbelleğe kaydeder ve paylaşılabilir Uri döndürür. */
  private fun createStoryUri(context: Context, item: SpiritualItem, selectedTab: Int): Uri {
    val width = 1080
    val height = 1920
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawStoryCanvas(canvas, width, height, item, selectedTab)

    val sharedDir = File(context.cacheDir, "shared_images")
    if (!sharedDir.exists()) sharedDir.mkdirs()
    val imageFile = File(sharedDir, "manevi_iklim_story.png")
    FileOutputStream(imageFile).use { out -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, out) }
    bitmap.recycle()

    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", imageFile)
  }

  /** Genel paylaşım: Android paylaşım menüsü (WhatsApp, Telegram, vb.). */
  fun shareSpiritualStory(
    context: Context,
    item: SpiritualItem,
    selectedTab: Int // 0: Ayet, 1: Hadis, 2: Dua
  ) {
    try {
      val uri = createStoryUri(context, item, selectedTab)
      val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_TEXT, "🌙 Günün Manevi İklimi • Ramazan Rehberi 2026")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      }
      val chooser = Intent.createChooser(shareIntent, "Manevi İklim Hikayesini Paylaş")
      chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      context.startActivity(chooser)
    } catch (e: Exception) {
      Toast.makeText(context, "Görsel oluşturulamadı: ${e.message}", Toast.LENGTH_LONG).show()
    }
  }

  /**
   * Doğrudan Instagram Hikâyesi.
   * - strings.xml'de facebook_app_id tanımlıysa Instagram'ın resmi "ADD_TO_STORY" akışı ile
   *   görsel doğrudan hikâye düzenleyicisinde açılır.
   * - Tanımlı değilse (ya da akış reddedilirse) görsel Instagram'a gönderilir; Instagram kendi
   *   menüsünde "Hikaye" seçeneğini sunar.
   */
  fun shareToInstagramStory(context: Context, item: SpiritualItem, selectedTab: Int) {
    val uri = try {
      createStoryUri(context, item, selectedTab)
    } catch (e: Exception) {
      Toast.makeText(context, "Görsel oluşturulamadı: ${e.message}", Toast.LENGTH_LONG).show()
      return
    }
    context.grantUriPermission(INSTAGRAM_PACKAGE, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)

    val appId = context.getString(R.string.facebook_app_id).trim()
    if (appId.isNotEmpty()) {
      val storyIntent = Intent("com.instagram.share.ADD_TO_STORY").apply {
        setPackage(INSTAGRAM_PACKAGE)
        putExtra("source_application", appId)
        setDataAndType(uri, "image/png")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      try {
        context.startActivity(storyIntent)
        return
      } catch (_: ActivityNotFoundException) {
        // Aşağıdaki genel Instagram paylaşımına düş
      }
    }

    val sendIntent = Intent(Intent.ACTION_SEND).apply {
      setPackage(INSTAGRAM_PACKAGE)
      type = "image/png"
      putExtra(Intent.EXTRA_STREAM, uri)
      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
      context.startActivity(sendIntent)
    } catch (_: ActivityNotFoundException) {
      Toast.makeText(context, "Instagram yüklü değil. Diğer uygulamalarla paylaşabilirsiniz.", Toast.LENGTH_LONG).show()
      shareSpiritualStory(context, item, selectedTab)
    }
  }

  private fun drawStoryCanvas(
    canvas: Canvas,
    w: Int,
    h: Int,
    item: SpiritualItem,
    selectedTab: Int
  ) {
    // 1. Soft Luxury Cream/Gold Gradient Background
    val bgPaint = Paint().apply {
      isAntiAlias = true
      shader = LinearGradient(
        0f, 0f, 0f, h.toFloat(),
        intArrayOf(
          Color.parseColor("#FFFDF9"), // Pure soft cream
          Color.parseColor("#F9F3E8"), // Warm parchment
          Color.parseColor("#EFE4D0"), // Rich golden cream
          Color.parseColor("#F7F1E6")
        ),
        floatArrayOf(0f, 0.35f, 0.75f, 1f),
        Shader.TileMode.CLAMP
      )
    }
    canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), bgPaint)

    // 2. Ornate Double Gold Borders
    val outerGoldBorder = Paint().apply {
      isAntiAlias = true
      style = Paint.Style.STROKE
      strokeWidth = 6f
      color = Color.parseColor("#C99A45") // Warm Islamic Gold
    }
    val innerGoldBorder = Paint().apply {
      isAntiAlias = true
      style = Paint.Style.STROKE
      strokeWidth = 2f
      color = Color.parseColor("#E0C58A")
    }

    val marginOuter = 50f
    val marginInner = 68f
    canvas.drawRoundRect(RectF(marginOuter, marginOuter, w - marginOuter, h - marginOuter), 40f, 40f, outerGoldBorder)
    canvas.drawRoundRect(RectF(marginInner, marginInner, w - marginInner, h - marginInner), 32f, 32f, innerGoldBorder)

    // Corner Ornaments
    drawCornerOrnaments(canvas, marginInner, w.toFloat() - marginInner, marginInner, h.toFloat() - marginInner)

    // 3. Top Decorative Header (Crescent, Stars & Lantern Motif)
    val goldPaint = Paint().apply {
      isAntiAlias = true
      color = Color.parseColor("#B8860B")
      style = Paint.Style.FILL
    }
    val textPaint = Paint().apply {
      isAntiAlias = true
      textAlign = Paint.Align.CENTER
    }

    // Top Emblem Icon
    textPaint.textSize = 80f
    canvas.drawText("🌙 ۞ 🕋", w / 2f, 180f, textPaint)

    // App Branding & Title
    textPaint.textSize = 34f
    textPaint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
    textPaint.color = Color.parseColor("#8C621D")
    textPaint.letterSpacing = 0.15f
    canvas.drawText("RAMAZAN REHBERİ 2026", w / 2f, 250f, textPaint)

    textPaint.textSize = 56f
    textPaint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
    textPaint.color = Color.parseColor("#2C241E")
    textPaint.letterSpacing = 0.05f
    canvas.drawText("Günün Manevi İklimi", w / 2f, 330f, textPaint)

    // Category Badge
    val categoryTitle = when (selectedTab) {
      0 -> "• ÂYET-İ KERÎME •"
      1 -> "• HADÎS-İ ŞERÎF •"
      else -> "• GÜNÜN DUASI •"
    }
    val badgePaint = Paint().apply {
      isAntiAlias = true
      color = Color.parseColor("#E5D4B3")
      style = Paint.Style.FILL
    }
    val badgeRect = RectF(w / 2f - 240f, 375f, w / 2f + 240f, 440f)
    canvas.drawRoundRect(badgeRect, 20f, 20f, badgePaint)

    val badgeStroke = Paint().apply {
      isAntiAlias = true
      color = Color.parseColor("#B8860B")
      style = Paint.Style.STROKE
      strokeWidth = 2f
    }
    canvas.drawRoundRect(badgeRect, 20f, 20f, badgeStroke)

    textPaint.textSize = 32f
    textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    textPaint.color = Color.parseColor("#704C14")
    textPaint.letterSpacing = 0.08f
    canvas.drawText(categoryTitle, w / 2f, 420f, textPaint)

    // Decorative Floral Divider Under Header
    drawIslamicDivider(canvas, w / 2f, 490f, 360f)

    // Content Variables
    val arabicText = if (selectedTab == 0) item.verseArabic else ""
    val turkishText = when (selectedTab) {
      0 -> item.verseTurkish
      1 -> item.hadithTurkish
      else -> item.prayerTurkish
    }
    val sourceText = when (selectedTab) {
      0 -> item.verseSource
      1 -> item.hadithSource
      else -> item.prayerSource
    }

    var currentY = 560f

    // 4. Arabic Text (if present)
    if (arabicText.isNotBlank()) {
      val arabicPaint = TextPaint().apply {
        isAntiAlias = true
        color = Color.parseColor("#1B4332") // Deep Islamic Emerald
        textSize = 50f
        typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
      }

      val contentWidth = (w - 220).coerceAtLeast(400)
      val arabicLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        StaticLayout.Builder.obtain(arabicText, 0, arabicText.length, arabicPaint, contentWidth)
          .setAlignment(Layout.Alignment.ALIGN_CENTER)
          .setLineSpacing(16f, 1.25f)
          .build()
      } else {
        @Suppress("DEPRECATION")
        StaticLayout(arabicText, arabicPaint, contentWidth, Layout.Alignment.ALIGN_CENTER, 1.25f, 16f, false)
      }

      canvas.save()
      canvas.translate(w / 2f - contentWidth / 2f, currentY)
      arabicLayout.draw(canvas)
      canvas.restore()

      currentY += arabicLayout.height + 70f

      // Small secondary divider between Arabic and Turkish
      drawIslamicDivider(canvas, w / 2f, currentY, 200f)
      currentY += 60f
    } else {
      currentY += 40f
    }

    // 5. Large Quote Mark “
    val quotePaint = Paint().apply {
      isAntiAlias = true
      color = Color.parseColor("#C99A45")
      textSize = 140f
      typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
      textAlign = Paint.Align.CENTER
      alpha = 180
    }
    canvas.drawText("“", w / 2f, currentY, quotePaint)
    currentY += 50f

    // 6. Turkish Text
    val bodyPaint = TextPaint().apply {
      isAntiAlias = true
      color = Color.parseColor("#26221D") // Deep charcoal
      textSize = if (turkishText.length > 200) 44f else 50f
      typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
    }

    val bodyWidth = (w - 240).coerceAtLeast(400)
    val bodyLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      StaticLayout.Builder.obtain(turkishText, 0, turkishText.length, bodyPaint, bodyWidth)
        .setAlignment(Layout.Alignment.ALIGN_CENTER)
        .setLineSpacing(18f, 1.35f)
        .build()
    } else {
      @Suppress("DEPRECATION")
      StaticLayout(turkishText, bodyPaint, bodyWidth, Layout.Alignment.ALIGN_CENTER, 1.35f, 18f, false)
    }

    canvas.save()
    canvas.translate(w / 2f - bodyWidth / 2f, currentY)
    bodyLayout.draw(canvas)
    canvas.restore()

    currentY += bodyLayout.height + 50f

    // Closing Quote Mark ”
    canvas.drawText("”", w / 2f, currentY, quotePaint)
    currentY += 60f

    // 7. Source Citation Box
    val sourcePaint = Paint().apply {
      isAntiAlias = true
      color = Color.parseColor("#8C621D")
      textSize = 36f
      typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
      textAlign = Paint.Align.CENTER
      letterSpacing = 0.04f
    }
    canvas.drawText("— $sourceText —", w / 2f, currentY, sourcePaint)

    // 8. Bottom Footer with Islamic Watermark & App Stamp
    val footerDividerY = h - 260f
    drawIslamicDivider(canvas, w / 2f, footerDividerY, 500f)

    val footerPaint = Paint().apply {
      isAntiAlias = true
      textAlign = Paint.Align.CENTER
      color = Color.parseColor("#7A551E")
      textSize = 34f
      typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      letterSpacing = 0.06f
    }
    canvas.drawText("✨ Ramazan Rehberi 2026 • Manisa / Saruhanlı", w / 2f, h - 190f, footerPaint)

    footerPaint.textSize = 28f
    footerPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    footerPaint.color = Color.parseColor("#9C7F58")
    canvas.drawText("Huzurlu İbadet, Namaz Vakitleri & Oruç Takibi", w / 2f, h - 135f, footerPaint)
  }

  private fun drawIslamicDivider(canvas: Canvas, cx: Float, cy: Float, width: Float) {
    val linePaint = Paint().apply {
      isAntiAlias = true
      color = Color.parseColor("#C99A45")
      strokeWidth = 3f
      style = Paint.Style.STROKE
    }

    val half = width / 2f
    canvas.drawLine(cx - half, cy, cx - 30f, cy, linePaint)
    canvas.drawLine(cx + 30f, cy, cx + half, cy, linePaint)

    // Central 8-pointed star / diamond
    val starPaint = Paint().apply {
      isAntiAlias = true
      color = Color.parseColor("#B8860B")
      style = Paint.Style.FILL
    }
    val diamond = Path().apply {
      moveTo(cx, cy - 14f)
      lineTo(cx + 14f, cy)
      lineTo(cx, cy + 14f)
      lineTo(cx - 14f, cy)
      close()
    }
    canvas.drawPath(diamond, starPaint)
  }

  private fun drawCornerOrnaments(canvas: Canvas, left: Float, right: Float, top: Float, bottom: Float) {
    val goldPaint = Paint().apply {
      isAntiAlias = true
      color = Color.parseColor("#C99A45")
      style = Paint.Style.STROKE
      strokeWidth = 3f
    }
    val len = 40f

    // Top Left
    canvas.drawLine(left, top + len, left + len, top, goldPaint)
    // Top Right
    canvas.drawLine(right - len, top, right, top + len, goldPaint)
    // Bottom Left
    canvas.drawLine(left, bottom - len, left + len, bottom, goldPaint)
    // Bottom Right
    canvas.drawLine(right - len, bottom, right, bottom - len, goldPaint)
  }
}
