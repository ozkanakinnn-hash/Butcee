package com.example

import android.content.Context
import android.graphics.Bitmap
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppColorTheme
import com.example.data.AppPreferences
import com.example.data.AppThemeMode
import com.example.widget.WidgetArt
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/** Widget görsellerini gerçekten çizer (Robolectric yerel grafik) ve build/widget-renders altına kaydeder. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "xxhdpi")
class WidgetRenderTest {
  private val ctx: Context get() = ApplicationProvider.getApplicationContext()

  private fun save(b: Bitmap, name: String) {
    val dir = File("build/widget-renders").apply { mkdirs() }
    File(dir, "$name.png").outputStream().use { b.compress(Bitmap.CompressFormat.PNG, 100, it) }
    assertTrue(b.width > 100 && b.height > 100)
  }

  @Test fun renderAll() {
    val prefs = AppPreferences(ctx)
    val names = listOf("İmsak", "Güneş", "Öğle", "İkindi", "Akşam", "Yatsı")
    val times = listOf("06:11", "07:35", "12:58", "15:47", "18:12", "19:30")
    for ((theme, dark) in listOf(AppColorTheme.SARAY to true, AppColorTheme.IZNIK to true, AppColorTheme.ZUMRUT to true, AppColorTheme.SARAY to false)) {
      prefs.colorTheme = theme
      prefs.themeMode = if (dark) AppThemeMode.DARK else AppThemeMode.LIGHT
      val p = WidgetArt.palette(ctx, prefs)
      val tag = theme.name.lowercase() + if (dark) "" else "_light"
      save(WidgetArt.nextPrayer(ctx, p, 170, 170, "Sıradaki", "İkindi", "15:47", .62f), "next_$tag")
      save(WidgetArt.fasting(ctx, p, 170, 170, "Ramazan'ın 12. günü", "İftara kalan", null, .68f, "İmsak 06:11", "İftar 18:12", "%68"), "fasting_$tag")
      save(WidgetArt.fasting(ctx, p, 170, 170, "Mübarek günler", "Sıradaki mübarek gün", "Regaip Kandili\n33 gün kaldı", null, "10.12.2026", "Akşam 18:12", ""), "fasting_holy_$tag")
      save(WidgetArt.prayerTimes(ctx, p, 320, 150, "Manisa / Saruhanlı", "26 Cemaziyelevvel 1448", names, times, 3, .72f, "Sıradaki: Akşam 18:12"), "times_$tag")
      save(WidgetArt.spiritual(ctx, p, 320, 150, "Günün Ayeti", "أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ",
        "Bilesiniz ki kalpler ancak Allah'ı anmakla huzur bulur.", "Ra'd Suresi, 28. Ayet"), "spiritual_$tag")
    }
  }
}
