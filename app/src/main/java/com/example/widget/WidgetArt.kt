package com.example.widget

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.res.ResourcesCompat
import com.example.R
import com.example.data.AppColorTheme
import com.example.data.AppPreferences
import com.example.data.AppThemeMode
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Ana ekran widget'larının görselleri. RemoteViews ile mümkün olmayan halka, desen, parıltı ve
 * özel yazı tiplerini çizebilmek için her widget, gerçek boyutunda bir Bitmap olarak üretilir.
 * Canlı akan geri sayımlar bitmap üzerine Chronometer olarak bindirilir (widget düzenlerinde).
 */
object WidgetArt {

  /** Widget renk paleti: uygulamanın renk teması + açık/koyu mod. */
  data class Palette(
    val top: Int, val bottom: Int, val accent: Int, val accentLight: Int,
    val ink: Int, val muted: Int, val line: Int, val dark: Boolean
  )

  fun palette(context: Context, prefs: AppPreferences): Palette {
    val night = when (prefs.themeMode) {
      AppThemeMode.DARK -> true
      AppThemeMode.LIGHT -> false
      AppThemeMode.SYSTEM -> (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    }
    if (!night) return when (prefs.colorTheme) {
      AppColorTheme.SARAY -> Palette(0xFFFFFCF6.toInt(), 0xFFF3EAD8.toInt(), 0xFF9E6B20.toInt(), 0xFFC98A2C.toInt(), 0xFF2B2823.toInt(), 0x9E2B2823.toInt(), 0x669E6B20, false)
      AppColorTheme.IZNIK -> Palette(0xFFFAFBFE.toInt(), 0xFFE3EAF2.toInt(), 0xFF1A3F7A.toInt(), 0xFF2A8C9C.toInt(), 0xFF1B2230.toInt(), 0x9E1B2230.toInt(), 0x661A3F7A, false)
      AppColorTheme.ZUMRUT -> Palette(0xFFF7FBF7.toInt(), 0xFFDFEAE2.toInt(), 0xFF1E6B4F.toInt(), 0xFFA9831B.toInt(), 0xFF1A241F.toInt(), 0x9E1A241F.toInt(), 0x661E6B4F, false)
    }
    return when (prefs.colorTheme) {
      AppColorTheme.SARAY -> Palette(0xFF1D170E.toInt(), 0xFF060504.toInt(), 0xFFE5B869.toInt(), 0xFFF8DEA0.toInt(), 0xFFF5EEDD.toInt(), 0x9EF5EEDD.toInt(), 0x73E5B869, true)
      AppColorTheme.IZNIK -> Palette(0xFF0F1A33.toInt(), 0xFF04070F.toInt(), 0xFF8DB1EA.toInt(), 0xFFC9DBFA.toInt(), 0xFFEEF3FB.toInt(), 0x9EEEF3FB.toInt(), 0x738DB1EA, true)
      AppColorTheme.ZUMRUT -> Palette(0xFF0C2219.toInt(), 0xFF030806.toInt(), 0xFF6FD3A2.toInt(), 0xFFE5C55A.toInt(), 0xFFEEF6F0.toInt(), 0x9EEEF6F0.toInt(), 0x736FD3A2, true)
    }
  }

  // ── Yazı tipleri ──
  private var fonts: Array<Typeface>? = null
  private fun fonts(ctx: Context): Array<Typeface> = fonts ?: run {
    fun load(id: Int, fb: Typeface) = runCatching { ResourcesCompat.getFont(ctx, id) }.getOrNull() ?: fb
    arrayOf(
      load(R.font.inter_regular, Typeface.SANS_SERIF),
      load(R.font.inter_extrabold, Typeface.DEFAULT_BOLD),
      load(R.font.playfair_display_bold, Typeface.create(Typeface.SERIF, Typeface.BOLD)),
      load(R.font.playfair_display_italic, Typeface.create(Typeface.SERIF, Typeface.ITALIC)),
      load(R.font.amiri_bold, Typeface.create(Typeface.SERIF, Typeface.BOLD))
    ).also { fonts = it }
  }
  private const val INTER = 0; private const val INTER_X = 1; private const val PLAYFAIR = 2; private const val PLAYFAIR_I = 3; private const val AMIRI = 4

  /** Bitmap ölçeği: ekran yoğunluğu, bellek sınırı için en fazla 2.5. */
  private fun scale(ctx: Context) = min(ctx.resources.displayMetrics.density, 2.5f)

  private class Art(ctx: Context, wDp: Int, hDp: Int, val p: Palette) {
    val k = scale(ctx)
    val w = wDp * k; val h = hDp * k
    val bmp: Bitmap = Bitmap.createBitmap(w.toInt().coerceAtLeast(1), h.toInt().coerceAtLeast(1), Bitmap.Config.ARGB_8888)
    val c = Canvas(bmp)
    val f = fonts(ctx)
    fun dp(v: Float) = v * k
    fun dp(v: Int) = v * k
    fun paint(color: Int) = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color }
    fun text(size: Float, font: Int, color: Int, align: Paint.Align = Paint.Align.LEFT, spacing: Float = 0f) =
      TextPaint(Paint.ANTI_ALIAS_FLAG).apply { textSize = dp(size); typeface = f[font]; this.color = color; textAlign = align; letterSpacing = spacing }
  }

  private fun withAlpha(color: Int, a: Int) = (color and 0x00FFFFFF) or (a shl 24)

  /** Zemin: köşeleri yuvarlak gradyan + parıltı + sekiz köşeli yıldız deseni + ince kenar. */
  private fun Art.background(glowX: Float, glowY: Float) {
    val r = dp(24)
    val rect = RectF(0f, 0f, w, h)
    val clip = Path().apply { addRoundRect(rect, r, r, Path.Direction.CW) }
    c.save(); c.clipPath(clip)
    c.drawRect(rect, Paint().apply { shader = LinearGradient(0f, 0f, w * .35f, h, p.top, p.bottom, Shader.TileMode.CLAMP) })
    c.drawCircle(w * glowX, h * glowY, min(w, h) * .9f, Paint().apply {
      shader = RadialGradient(w * glowX, h * glowY, min(w, h) * .9f, withAlpha(p.accent, if (p.dark) 0x38 else 0x22), 0, Shader.TileMode.CLAMP)
    })
    val pat = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = dp(.8f); color = withAlpha(p.accent, if (p.dark) 0x16 else 0x14) }
    val step = dp(52)
    var y = step / 2
    while (y < h + step) { var x = step / 2; while (x < w + step) { c.drawPath(star(x, y, step * .38f, .72f), pat); x += step }; y += step }
    c.restore()
    c.drawRoundRect(RectF(dp(.6f), dp(.6f), w - dp(.6f), h - dp(.6f)), r, r,
      Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = dp(1.1f); color = p.line })
  }

  private fun star(cx: Float, cy: Float, r: Float, inner: Float) = Path().apply {
    for (i in 0 until 16) {
      val a = Math.PI / 8 * i - Math.PI / 2
      val rr = if (i % 2 == 0) r else r * inner
      val x = cx + rr * cos(a).toFloat(); val y = cy + rr * sin(a).toFloat()
      if (i == 0) moveTo(x, y) else lineTo(x, y)
    }
    close()
  }

  private fun Art.crescent(cx: Float, cy: Float, r: Float) {
    c.drawCircle(cx, cy, r * 1.45f, paint(withAlpha(p.accent, 0x22)))
    val moon = Path().apply {
      addCircle(cx, cy, r, Path.Direction.CW)
      op(Path().apply { addCircle(cx + r * .45f, cy - r * .2f, r * .85f, Path.Direction.CW) }, Path.Op.DIFFERENCE)
    }
    c.drawPath(moon, paint(p.accentLight))
    c.drawPath(star(cx + r * 1.05f, cy - r * 1.0f, r * .32f, .45f), paint(p.accentLight))
  }

  /** İnce çizgili cami silüeti (alt kenarda). */
  private fun Art.mosqueLine(alpha: Int) {
    val pt = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = dp(1.1f); color = withAlpha(p.accent, alpha) }
    val base = h; val s = h / dp(160)
    fun minaret(x: Float) = c.drawPath(Path().apply {
      moveTo(x, base); lineTo(x, base - dp(52) * s); lineTo(x + dp(3), base - dp(64) * s); lineTo(x + dp(6), base - dp(52) * s); lineTo(x + dp(6), base)
    }, pt)
    minaret(w * .18f); minaret(w * .82f - dp(6))
    c.drawPath(Path().apply {
      moveTo(w * .3f, base); lineTo(w * .3f, base - dp(24) * s)
      quadTo(w * .3f, base - dp(52) * s, w * .5f, base - dp(58) * s)
      quadTo(w * .7f, base - dp(52) * s, w * .7f, base - dp(24) * s); lineTo(w * .7f, base)
      moveTo(w * .5f, base - dp(58) * s); lineTo(w * .5f, base - dp(66) * s)
      moveTo(w * .44f, base); lineTo(w * .44f, base - dp(12) * s)
      quadTo(w * .5f, base - dp(24) * s, w * .56f, base - dp(12) * s); lineTo(w * .56f, base)
    }, pt)
  }

  private fun Art.textCenter(s: String, cx: Float, baseline: Float, tp: TextPaint) { tp.textAlign = Paint.Align.CENTER; c.drawText(s, cx, baseline, tp) }

  // ─────────────── 1) Sıradaki Namaz: ilerleme halkası (geri sayım Chronometer ile ortada) ───────────────
  fun nextPrayer(ctx: Context, p: Palette, wDp: Int, hDp: Int, label: String, name: String, time: String, progress: Float): Bitmap {
    val a = Art(ctx, wDp, hDp, p)
    with(a) {
      background(.85f, 0f)
      mosqueLine(if (p.dark) 0x55 else 0x60)
      crescent(w - dp(26), dp(26), dp(9))
      val cx = w / 2; val cy = h / 2
      val r = min(w, h) / 2 - dp(16)
      val track = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = dp(5); color = p.line }
      c.drawCircle(cx, cy, r, track)
      val prog = progress.coerceIn(0.02f, 1f)
      val arcRect = RectF(cx - r, cy - r, cx + r, cy + r)
      val glow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; strokeWidth = dp(9); strokeCap = Paint.Cap.ROUND; color = withAlpha(p.accent, 0x55)
        maskFilter = BlurMaskFilter(dp(6), BlurMaskFilter.Blur.NORMAL)
      }
      c.drawArc(arcRect, -90f, 360f * prog, false, glow)
      val arc = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; strokeWidth = dp(5); strokeCap = Paint.Cap.ROUND
        shader = LinearGradient(cx - r, cy - r, cx + r, cy + r, p.accentLight, p.accent, Shader.TileMode.CLAMP)
      }
      c.drawArc(arcRect, -90f, 360f * prog, false, arc)
      val ang = Math.toRadians(360.0 * prog - 90)
      val kx = cx + r * cos(ang).toFloat(); val ky = cy + r * sin(ang).toFloat()
      c.drawCircle(kx, ky, dp(5.5f), paint(p.bottom)); c.drawCircle(kx, ky, dp(4.2f), paint(p.accentLight))
      textCenter(label.uppercase(java.util.Locale.forLanguageTag("tr")), cx, cy - dp(36), text(8.5f, INTER, p.muted, spacing = .25f))
      textCenter(name, cx, cy - dp(16), text(17f, PLAYFAIR, p.ink))
      // Saat rozeti
      val tp = text(10.5f, INTER_X, p.accent)
      val tw = tp.measureText(time) + dp(16)
      val pill = RectF(cx - tw / 2, cy + dp(16), cx + tw / 2, cy + dp(32))
      c.drawRoundRect(pill, dp(8), dp(8), Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = dp(1.1f); color = p.accent })
      textCenter(time, cx, pill.bottom - dp(4.5f), tp)
    }
    return a.bmp
  }

  // ─────────────── 2) Oruç & İftar ───────────────
  /**
   * [countdownLabel] varsa geri sayım Chronometer ile sol üstte (58dp) gösterilir; yoksa [bigText] çizilir.
   */
  fun fasting(
    ctx: Context, p: Palette, wDp: Int, hDp: Int, title: String, countdownLabel: String,
    bigText: String?, progress: Float?, leftLabel: String, rightLabel: String, centerLabel: String
  ): Bitmap {
    val a = Art(ctx, wDp, hDp, p)
    with(a) {
      background(.15f, 0f)
      // Fener
      val lx = w - dp(34); val ly = dp(8)
      c.drawLine(lx, 0f, lx, ly + dp(10), Paint(Paint.ANTI_ALIAS_FLAG).apply { strokeWidth = dp(1.2f); color = p.accent })
      c.drawCircle(lx, ly + dp(38), dp(30), paint(withAlpha(p.accentLight, 0x24)))
      c.drawRoundRect(RectF(lx - dp(7), ly + dp(10), lx + dp(7), ly + dp(14)), dp(1.5f), dp(1.5f), paint(p.accent))
      val body = Path().apply {
        moveTo(lx - dp(10), ly + dp(14)); quadTo(lx - dp(14), ly + dp(34), lx - dp(10), ly + dp(56))
        lineTo(lx + dp(10), ly + dp(56)); quadTo(lx + dp(14), ly + dp(34), lx + dp(10), ly + dp(14)); close()
      }
      c.drawPath(body, Paint(Paint.ANTI_ALIAS_FLAG).apply { shader = LinearGradient(0f, ly + dp(14), 0f, ly + dp(56), 0xFFFFE9A8.toInt(), 0xFFF0B04A.toInt(), Shader.TileMode.CLAMP) })
      c.drawPath(body, Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = dp(1.1f); color = p.accent })
      c.drawRoundRect(RectF(lx - dp(6), ly + dp(56), lx + dp(6), ly + dp(60)), dp(1.5f), dp(1.5f), paint(p.accent))
      // Başlık + etiket
      val left = dp(16)
      val titlePaint = text(9f, INTER_X, p.accent, spacing = .06f)
      c.drawText(title.uppercase(java.util.Locale.forLanguageTag("tr")), left, dp(24), titlePaint)
      c.drawText(countdownLabel, left, dp(46), text(11f, INTER, p.muted))
      if (bigText != null) {
        val tp = text(20f, INTER_X, p.ink)
        val sl = StaticLayout.Builder.obtain(bigText, 0, bigText.length, tp, (w - left - dp(60)).toInt())
          .setAlignment(Layout.Alignment.ALIGN_NORMAL).setLineSpacing(0f, 1.05f).setIncludePad(false).build()
        c.save(); c.translate(left, dp(54)); sl.draw(c); c.restore()
      }
      // İlerleme çubuğu
      val barY = h - dp(44)
      val bar = RectF(left, barY, w - left, barY + dp(6))
      c.drawRoundRect(bar, dp(3), dp(3), paint(p.line))
      if (progress != null) {
        val px = bar.left + bar.width() * progress.coerceIn(0f, 1f)
        c.drawRoundRect(RectF(bar.left, bar.top, px, bar.bottom), dp(3), dp(3),
          Paint(Paint.ANTI_ALIAS_FLAG).apply { shader = LinearGradient(bar.left, 0f, px.coerceAtLeast(bar.left + 1), 0f, p.accentLight, p.accent, Shader.TileMode.CLAMP) })
        c.drawCircle(px, bar.centerY(), dp(9), paint(p.accent))
        val mr = dp(4.6f)
        val moon = Path().apply {
          addCircle(px, bar.centerY(), mr, Path.Direction.CW)
          op(Path().apply { addCircle(px + mr * .5f, bar.centerY() - mr * .25f, mr * .85f, Path.Direction.CW) }, Path.Op.DIFFERENCE)
        }
        c.drawPath(moon, paint(p.bottom))
      }
      val small = text(9.5f, INTER, p.muted)
      c.drawText(leftLabel, left, h - dp(16), small)
      textCenter(centerLabel, w / 2, h - dp(16), text(10f, INTER_X, p.accent))
      small.textAlign = Paint.Align.RIGHT
      c.drawText(rightLabel, w - left, h - dp(16), small)
    }
    return a.bmp
  }

  // ─────────────── 3) Günün Vakitleri ───────────────
  fun prayerTimes(
    ctx: Context, p: Palette, wDp: Int, hDp: Int, place: String, hijri: String,
    names: List<String>, times: List<String>, current: Int, dayProgress: Float, footer: String
  ): Bitmap {
    val a = Art(ctx, wDp, hDp, p)
    with(a) {
      background(.5f, 0f)
      val side = dp(14)
      c.drawText(place, side + dp(2), dp(24), text(11f, INTER_X, p.ink))
      val hp = text(10.5f, PLAYFAIR, p.accent, Paint.Align.RIGHT)
      c.drawText(hijri, w - side - dp(2), dp(24), hp)
      val n = names.size
      val gap = dp(5)
      val top = dp(34); val bottom = h - dp(30)
      val cw = (w - 2 * side - gap * (n - 1)) / n
      for (i in 0 until n) {
        val x = side + i * (cw + gap)
        val cell = RectF(x, top, x + cw, bottom)
        val sel = i == current
        if (sel) {
          c.drawRoundRect(RectF(cell.left, cell.top + dp(2), cell.right, cell.bottom + dp(2)), dp(12), dp(12),
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = withAlpha(p.accent, 0x66); maskFilter = BlurMaskFilter(dp(8), BlurMaskFilter.Blur.NORMAL) })
          c.drawRoundRect(cell, dp(12), dp(12), Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(0f, cell.top, 0f, cell.bottom, p.accentLight, p.accent, Shader.TileMode.CLAMP)
          })
        } else {
          c.drawRoundRect(cell, dp(12), dp(12), paint(withAlpha(p.ink, 0x0D)))
          c.drawRoundRect(cell, dp(12), dp(12), Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = dp(.8f); color = p.line })
        }
        val fg = if (sel) p.bottom else p.ink
        val icColor = if (sel) p.bottom else p.accent
        val cx = cell.centerX()
        val hgt = cell.height()
        drawIcon(i, cx, cell.top + hgt * .27f, dp(8), icColor)
        val nameSize = min(10.5f, cw / k / 5.2f)
        textCenter(names[i], cx, cell.top + hgt * .62f, text(nameSize, if (sel) INTER_X else INTER, fg))
        textCenter(times[i], cx, cell.top + hgt * .88f, text(min(14f, cw / k / 3.6f), INTER_X, fg))
      }
      // Günün akışı çizgisi + alt bilgi
      val fp = text(10f, INTER, p.muted, Paint.Align.RIGHT)
      val fw = fp.measureText(footer)
      val ly = h - dp(14)
      val lr = w - side - fw - dp(12)
      c.drawRoundRect(RectF(side, ly - dp(1.5f), lr, ly + dp(1.5f)), dp(1.5f), dp(1.5f), paint(p.line))
      val px = side + (lr - side) * dayProgress.coerceIn(0f, 1f)
      c.drawRoundRect(RectF(side, ly - dp(1.5f), px, ly + dp(1.5f)), dp(1.5f), dp(1.5f), paint(p.accent))
      c.drawCircle(px, ly, dp(4.5f), paint(p.accentLight))
      c.drawText(footer, w - side, ly + dp(3.5f), fp)
    }
    return a.bmp
  }

  /** Vakit ikonları: 0 imsak (hilal), 1 güneş doğuşu, 2 öğle (güneş), 3 ikindi (güneş+bulut), 4 akşam (batış), 5 yatsı (hilal). */
  private fun Art.drawIcon(i: Int, cx: Float, cy: Float, r: Float, col: Int) {
    val pt = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = dp(1.3f); color = col; strokeCap = Paint.Cap.ROUND }
    fun moon() {
      val path = Path().apply {
        addCircle(cx, cy, r, Path.Direction.CW)
        op(Path().apply { addCircle(cx + r * .5f, cy - r * .3f, r * .85f, Path.Direction.CW) }, Path.Op.DIFFERENCE)
      }
      c.drawPath(path, pt)
    }
    fun sun(rr: Float, ox: Float = 0f, oy: Float = 0f) {
      c.drawCircle(cx + ox, cy + oy, rr * .5f, pt)
      for (k2 in 0 until 8) {
        val an = Math.PI / 4 * k2
        c.drawLine(cx + ox + rr * .75f * cos(an).toFloat(), cy + oy + rr * .75f * sin(an).toFloat(),
          cx + ox + rr * cos(an).toFloat(), cy + oy + rr * sin(an).toFloat(), pt)
      }
    }
    fun horizon(up: Boolean) {
      c.drawArc(RectF(cx - r * .7f, cy - r * .3f, cx + r * .7f, cy + r * 1.1f), 180f, 180f, false, pt)
      c.drawLine(cx - r * 1.1f, cy + r * .45f, cx + r * 1.1f, cy + r * .45f, pt)
      val ay = cy - r * .75f
      c.drawLine(cx, ay - r * .45f, cx, ay + r * .25f, pt)
      if (up) { c.drawLine(cx - r * .3f, ay - r * .15f, cx, ay - r * .45f, pt); c.drawLine(cx + r * .3f, ay - r * .15f, cx, ay - r * .45f, pt) }
      else { c.drawLine(cx - r * .3f, ay - r * .05f, cx, ay + r * .25f, pt); c.drawLine(cx + r * .3f, ay - r * .05f, cx, ay + r * .25f, pt) }
    }
    when (i) {
      0, 5 -> moon()
      1 -> horizon(true)
      2 -> sun(r)
      3 -> { sun(r * .8f, -r * .25f, -r * .2f); c.drawRoundRect(RectF(cx - r * .55f, cy + r * .05f, cx + r * 1.05f, cy + r * .75f), r * .35f, r * .35f, pt) }
      4 -> horizon(false)
    }
  }

  // ─────────────── 4) Günün Manevi İklimi: mihrap kemerli çerçeve ───────────────
  fun spiritual(ctx: Context, p: Palette, wDp: Int, hDp: Int, kind: String, arabic: String, text: String, source: String): Bitmap {
    val a = Art(ctx, wDp, hDp, p)
    with(a) {
      background(.5f, 1f)
      val m = dp(12)
      fun arch(inset: Float) = Path().apply {
        val l = m + inset; val r = w - m - inset; val b = h - m - inset; val t0 = dp(12) + inset
        moveTo(l, b); lineTo(l, t0 + dp(26))
        quadTo(l, t0 + dp(12), w / 2 - dp(60), t0 + dp(8)); quadTo(w / 2 - dp(16), t0 + dp(4), w / 2, t0 - dp(2))
        quadTo(w / 2 + dp(16), t0 + dp(4), w / 2 + dp(60), t0 + dp(8)); quadTo(r, t0 + dp(12), r, t0 + dp(26))
        lineTo(r, b); close()
      }
      c.drawPath(arch(0f), Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = dp(1.1f); color = withAlpha(p.accent, 0xC0) })
      c.drawPath(arch(dp(4)), Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = dp(.6f); color = withAlpha(p.accent, 0x66) })
      val sx = w / 2; val sy = dp(10)
      c.drawPath(star(sx, sy, dp(9), .7f), paint(p.bottom))
      c.drawPath(star(sx, sy, dp(9), .7f), Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = dp(1.1f); color = p.accent })

      val inner = (w - 2 * dp(40)).toInt()
      val kindP = text(8.5f, INTER, p.accent, Paint.Align.CENTER, .25f)
      val arP = text(18f, AMIRI, p.accentLight)
      val bodyP = text(12.5f, PLAYFAIR_I, p.ink)
      val srcP = text(8f, INTER, p.muted, Paint.Align.CENTER, .2f)
      fun lay(s: String, tp: TextPaint, mult: Float) = StaticLayout.Builder.obtain(s, 0, s.length, tp, inner)
        .setAlignment(Layout.Alignment.ALIGN_CENTER).setLineSpacing(0f, mult).setIncludePad(false).setMaxLines(3)
        .setEllipsize(android.text.TextUtils.TruncateAt.END).build()
      // Sığana kadar küçült
      var ar: StaticLayout; var body: StaticLayout
      val avail = h - dp(40) - dp(26)
      while (true) {
        ar = lay(arabic, arP, 1.1f); body = lay("“$text”", bodyP, 1.15f)
        val total = (if (arabic.isBlank()) 0 else ar.height) + body.height + dp(8)
        if (total <= avail || bodyP.textSize < dp(9f)) break
        arP.textSize *= .92f; bodyP.textSize *= .93f
      }
      val total = (if (arabic.isBlank()) 0 else ar.height) + body.height + dp(8)
      var y = dp(40) + (avail - total) / 2 + dp(4)
      c.drawText(kind.uppercase(java.util.Locale.forLanguageTag("tr")), w / 2, dp(36), kindP)
      if (arabic.isNotBlank()) { c.save(); c.translate(dp(40), y); ar.draw(c); c.restore(); y += ar.height + dp(4) }
      c.save(); c.translate(dp(40), y); body.draw(c); c.restore()
      c.drawText(source.uppercase(java.util.Locale.forLanguageTag("tr")), w / 2, h - dp(20), srcP)
    }
    return a.bmp
  }

  @Suppress("unused") private val keep = Color.TRANSPARENT
}
