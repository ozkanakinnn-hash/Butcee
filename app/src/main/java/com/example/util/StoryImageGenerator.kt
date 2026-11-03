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
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.R
import com.example.data.AppColorTheme
import com.example.data.AppPreferences
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
    drawStoryCanvas(context, canvas, width, height, item, selectedTab)

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
        putExtra(Intent.EXTRA_TEXT, "🌙 Günün Manevi İklimi • ${context.getString(R.string.app_name)}")
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

  /** Hikâye görselinin renkleri: kullanıcının seçtiği uygulama temasına göre. */
  private class StoryPalette(
    val bgTop: Int, val bgBottom: Int, val pattern: Int, val panel: Int,
    val accent: Int, val accentLight: Int, val ornament: Int,
    val text: Int, val muted: Int, val arabic: Int
  )

  private fun paletteFor(theme: AppColorTheme): StoryPalette = when (theme) {
    AppColorTheme.SARAY -> StoryPalette(
      bgTop = Color.parseColor("#FBF6EC"), bgBottom = Color.parseColor("#E8D8BB"),
      pattern = Color.parseColor("#B98A3E"), panel = Color.parseColor("#FFFDF8"),
      accent = Color.parseColor("#A8762A"), accentLight = Color.parseColor("#E2C68C"),
      ornament = Color.parseColor("#2F5E46"),
      text = Color.parseColor("#2B241C"), muted = Color.parseColor("#7A6448"),
      arabic = Color.parseColor("#1F4D3A")
    )
    AppColorTheme.IZNIK -> StoryPalette(
      bgTop = Color.parseColor("#F4F7FB"), bgBottom = Color.parseColor("#D3DFEE"),
      pattern = Color.parseColor("#1A3F7A"), panel = Color.parseColor("#FFFFFF"),
      accent = Color.parseColor("#1A3F7A"), accentLight = Color.parseColor("#8FB0DA"),
      ornament = Color.parseColor("#C0392B"),
      text = Color.parseColor("#1B2230"), muted = Color.parseColor("#55627A"),
      arabic = Color.parseColor("#1A3F7A")
    )
    AppColorTheme.ZUMRUT -> StoryPalette(
      bgTop = Color.parseColor("#F3F7F2"), bgBottom = Color.parseColor("#D3E3D8"),
      pattern = Color.parseColor("#1E6B4F"), panel = Color.parseColor("#FFFEFA"),
      accent = Color.parseColor("#1E6B4F"), accentLight = Color.parseColor("#D8BC5E"),
      ornament = Color.parseColor("#B8901F"),
      text = Color.parseColor("#1A241F"), muted = Color.parseColor("#52665A"),
      arabic = Color.parseColor("#1E6B4F")
    )
  }

  private fun withAlpha(color: Int, alpha: Float): Int =
    Color.argb((alpha * 255).toInt().coerceIn(0, 255), Color.red(color), Color.green(color), Color.blue(color))

  private fun staticLayout(text: String, paint: TextPaint, width: Int, spacingAdd: Float, spacingMult: Float): StaticLayout =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
        .setAlignment(Layout.Alignment.ALIGN_CENTER)
        .setLineSpacing(spacingAdd, spacingMult)
        .build()
    } else {
      @Suppress("DEPRECATION")
      StaticLayout(text, paint, width, Layout.Alignment.ALIGN_CENTER, spacingMult, spacingAdd, false)
    }

  /**
   * Mihrap kemerli, temaya uyumlu 1080x1920 hikâye tasarımı:
   * zemin degrade + ince sekiz köşeli yıldız deseni, ortada kemerli kart,
   * üstte yalnızca uygulama ikonu, altta yalnızca uygulama adı (strings.xml'den).
   */
  private fun drawStoryCanvas(
    context: Context,
    canvas: Canvas,
    w: Int,
    h: Int,
    item: SpiritualItem,
    selectedTab: Int
  ) {
    val pal = paletteFor(AppPreferences(context).colorTheme)
    val wf = w.toFloat()
    val hf = h.toFloat()
    val cx = wf / 2f

    // 1. Zemin degradesi
    canvas.drawRect(0f, 0f, wf, hf, Paint().apply {
      shader = LinearGradient(0f, 0f, 0f, hf, pal.bgTop, pal.bgBottom, Shader.TileMode.CLAMP)
    })

    // 2. İnce sekiz köşeli yıldız (Rub-ül Hizb) deseni
    drawStarPattern(canvas, wf, hf, withAlpha(pal.pattern, 0.10f))

    // 3. Kenarlara doğru hafif koyulaşan vinyet
    canvas.drawRect(0f, 0f, wf, hf, Paint().apply {
      shader = RadialGradient(
        cx, hf * 0.45f, hf * 0.75f,
        intArrayOf(Color.TRANSPARENT, Color.TRANSPARENT, withAlpha(pal.pattern, 0.14f)),
        floatArrayOf(0f, 0.6f, 1f), Shader.TileMode.CLAMP
      )
    })

    // 4. Mihrap kemerli kart
    val left = 105f
    val right = wf - 105f
    val bottom = 1650f
    val sideTop = 600f
    val apex = 230f
    val arch = archPath(left, right, sideTop, apex, bottom)

    canvas.drawPath(arch, Paint().apply {
      isAntiAlias = true
      color = pal.panel
      setShadowLayer(36f, 0f, 14f, withAlpha(pal.pattern, 0.22f))
    })
    canvas.drawPath(arch, Paint().apply {
      isAntiAlias = true
      style = Paint.Style.STROKE
      strokeWidth = 5f
      color = pal.accent
    })
    val inset = 20f
    canvas.drawPath(archPath(left + inset, right - inset, sideTop + inset * 0.4f, apex + inset * 1.6f, bottom - inset), Paint().apply {
      isAntiAlias = true
      style = Paint.Style.STROKE
      strokeWidth = 2f
      color = pal.accentLight
    })

    // Kemerin tepesinde küçük sekiz köşeli yıldız (alem yerine sade süs)
    drawEightPointStar(canvas, cx, apex - 42f, 20f, Paint().apply {
      isAntiAlias = true
      color = pal.ornament
    })

    // 5. Uygulama ikonu
    val iconR = 72f
    val iconCy = 395f
    canvas.drawCircle(cx, iconCy, iconR + 10f, Paint().apply {
      isAntiAlias = true
      style = Paint.Style.STROKE
      strokeWidth = 3f
      color = pal.accentLight
    })
    ContextCompat.getDrawable(context, R.mipmap.ic_launcher)?.let { icon ->
      canvas.save()
      canvas.clipPath(Path().apply { addCircle(cx, iconCy, iconR, Path.Direction.CW) })
      icon.setBounds((cx - iconR).toInt(), (iconCy - iconR).toInt(), (cx + iconR).toInt(), (iconCy + iconR).toInt())
      icon.draw(canvas)
      canvas.restore()
    }

    // 6. Başlık ve tür rozeti
    val centered = Paint().apply {
      isAntiAlias = true
      textAlign = Paint.Align.CENTER
    }
    centered.apply {
      textSize = 60f
      typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
      color = pal.text
      letterSpacing = 0.02f
    }
    canvas.drawText(com.example.i18n.t("Günün Manevi İklimi"), cx, 560f, centered)

    val categoryTitle = when (selectedTab) {
      0 -> if (com.example.i18n.AppLanguage.isEnglish) "VERSE OF THE DAY" else "ÂYET-İ KERÎME"
      1 -> if (com.example.i18n.AppLanguage.isEnglish) "HADITH OF THE DAY" else "HADÎS-İ ŞERÎF"
      else -> if (com.example.i18n.AppLanguage.isEnglish) "DUA OF THE DAY" else "GÜNÜN DUASI"
    }
    centered.apply {
      textSize = 30f
      typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      color = pal.accent
      letterSpacing = 0.18f
    }
    val badgeW = centered.measureText(categoryTitle) + 80f
    val badge = RectF(cx - badgeW / 2f, 595f, cx + badgeW / 2f, 655f)
    canvas.drawRoundRect(badge, 30f, 30f, Paint().apply { isAntiAlias = true; color = withAlpha(pal.accent, 0.10f) })
    canvas.drawRoundRect(badge, 30f, 30f, Paint().apply {
      isAntiAlias = true; style = Paint.Style.STROKE; strokeWidth = 2f; color = withAlpha(pal.accent, 0.6f)
    })
    canvas.drawText(categoryTitle, cx, 636f, centered)

    drawIslamicDivider(canvas, cx, 705f, 380f, pal)

    // 7. İçerik (Arapça + Türkçe + kaynak) — kartın kalan alanında dikey ortalanır
    val arabicText = if (selectedTab == 0) item.verseArabic else ""
    val turkishBody = when (selectedTab) {
      0 -> item.verseTurkish
      1 -> item.hadithTurkish
      else -> item.prayerTurkish
    }
    val turkishText = if (turkishBody.isBlank()) "" else "“$turkishBody”"
    val sourceText = when (selectedTab) {
      0 -> item.verseSource
      1 -> item.hadithSource
      else -> item.prayerSource
    }

    val contentTop = 750f
    val contentBottom = bottom - 70f
    val contentWidth = (right - left - 150f).toInt()

    val arabicPaint = TextPaint().apply {
      isAntiAlias = true
      color = pal.arabic
      typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
    }
    val bodyPaint = TextPaint().apply {
      isAntiAlias = true
      color = pal.text
      typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
    }
    val sourcePaint = TextPaint().apply {
      isAntiAlias = true
      color = pal.accent
      typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
      letterSpacing = 0.03f
    }

    // Uzun metinlerde yazıyı sığana kadar küçült
    var scale = 1f
    lateinit var arabicLayout: StaticLayout
    lateinit var bodyLayout: StaticLayout
    lateinit var sourceLayout: StaticLayout
    var gapArabic = 0f
    var total = 0f
    while (true) {
      arabicPaint.textSize = 54f * scale
      bodyPaint.textSize = 50f * scale
      sourcePaint.textSize = 36f * scale
      arabicLayout = staticLayout(arabicText, arabicPaint, contentWidth, 18f * scale, 1.3f)
      bodyLayout = staticLayout(turkishText, bodyPaint, contentWidth, 16f * scale, 1.3f)
      sourceLayout = staticLayout("— $sourceText —", sourcePaint, contentWidth, 0f, 1.1f)
      gapArabic = if (arabicText.isNotBlank()) 90f * scale else 0f
      total = (if (arabicText.isNotBlank()) arabicLayout.height.toFloat() else 0f) +
        gapArabic + bodyLayout.height + 56f * scale + sourceLayout.height
      if (total <= contentBottom - contentTop || scale <= 0.6f) break
      scale -= 0.05f
    }

    var y = contentTop + ((contentBottom - contentTop) - total).coerceAtLeast(0f) / 2f
    val layoutLeft = cx - contentWidth / 2f
    if (arabicText.isNotBlank()) {
      canvas.save(); canvas.translate(layoutLeft, y); arabicLayout.draw(canvas); canvas.restore()
      y += arabicLayout.height
      drawEightPointStar(canvas, cx, y + gapArabic / 2f, 12f, Paint().apply { isAntiAlias = true; color = pal.accentLight })
      y += gapArabic
    }
    canvas.save(); canvas.translate(layoutLeft, y); bodyLayout.draw(canvas); canvas.restore()
    y += bodyLayout.height + 56f * scale
    canvas.save(); canvas.translate(layoutLeft, y); sourceLayout.draw(canvas); canvas.restore()

    // 8. Alt bilgi: yalnızca uygulama adı (isim değişirse otomatik güncellenir)
    drawIslamicDivider(canvas, cx, 1730f, 300f, pal)
    centered.apply {
      textSize = 40f
      typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
      color = pal.accent
      letterSpacing = 0.12f
    }
    canvas.drawText(context.getString(R.string.app_name), cx, 1800f, centered)
  }

  /** Sivri (mihrap) kemerli kart yolu; alt köşeler yuvarlatılmış. */
  private fun archPath(left: Float, right: Float, sideTop: Float, apex: Float, bottom: Float): Path {
    val cx = (left + right) / 2f
    val r = 36f
    return Path().apply {
      moveTo(left, bottom - r)
      lineTo(left, sideTop)
      cubicTo(left, sideTop - (sideTop - apex) * 0.55f, cx - (right - left) * 0.16f, apex + (sideTop - apex) * 0.22f, cx, apex)
      cubicTo(cx + (right - left) * 0.16f, apex + (sideTop - apex) * 0.22f, right, sideTop - (sideTop - apex) * 0.55f, right, sideTop)
      lineTo(right, bottom - r)
      quadTo(right, bottom, right - r, bottom)
      lineTo(left + r, bottom)
      quadTo(left, bottom, left, bottom - r)
      close()
    }
  }

  /** İki karenin 45° döndürülmüş birleşimi: sekiz köşeli yıldız. */
  private fun eightPointStarPath(cx: Float, cy: Float, r: Float): Path {
    val path = Path()
    for (k in 0 until 16) {
      val angle = Math.PI / 8 * k - Math.PI / 2
      val rad = if (k % 2 == 0) r else r * 0.62f
      val x = cx + (rad * Math.cos(angle)).toFloat()
      val y = cy + (rad * Math.sin(angle)).toFloat()
      if (k == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return path
  }

  private fun drawEightPointStar(canvas: Canvas, cx: Float, cy: Float, r: Float, paint: Paint) {
    canvas.drawPath(eightPointStarPath(cx, cy, r), paint)
  }

  private fun drawStarPattern(canvas: Canvas, w: Float, h: Float, color: Int) {
    val stroke = Paint().apply {
      isAntiAlias = true
      style = Paint.Style.STROKE
      strokeWidth = 2f
      this.color = color
    }
    val step = 150f
    var row = 0
    var y = -step / 2f
    while (y < h + step) {
      val offset = if (row % 2 == 0) 0f else step / 2f
      var x = -step / 2f + offset
      while (x < w + step) {
        canvas.drawPath(eightPointStarPath(x, y, 34f), stroke)
        // Yıldızları birbirine bağlayan ince kafes
        canvas.drawLine(x + 34f, y, x + step / 2f - 20f, y + step / 2f - 20f, stroke)
        canvas.drawLine(x - 34f, y, x - step / 2f + 20f, y + step / 2f - 20f, stroke)
        x += step
      }
      y += step / 2f
      row++
    }
  }

  private fun drawIslamicDivider(canvas: Canvas, cx: Float, cy: Float, width: Float, pal: StoryPalette) {
    val linePaint = Paint().apply {
      isAntiAlias = true
      color = pal.accentLight
      strokeWidth = 3f
      style = Paint.Style.STROKE
    }
    val half = width / 2f
    canvas.drawLine(cx - half, cy, cx - 34f, cy, linePaint)
    canvas.drawLine(cx + 34f, cy, cx + half, cy, linePaint)
    drawEightPointStar(canvas, cx, cy, 16f, Paint().apply {
      isAntiAlias = true
      color = pal.accent
    })
  }
}
