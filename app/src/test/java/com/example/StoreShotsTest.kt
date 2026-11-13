package com.example

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextReplacement
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppPreferences
import com.example.i18n.AppLanguage
import com.example.qibla.CompassReading
import com.example.qibla.QiblaCalculator
import com.example.qibla.QiblaFinderScreen
import com.example.qibla.QiblaPlace
import com.example.ui.screens.CalculatorsScreen
import com.example.ui.screens.ImsakiyeScreen
import com.example.ui.theme.MyApplicationTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/** Play Store tanıtım görselleri için uygulamanın gerçek temasıyla (Saray, koyu) ekran çizimleri. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w412dp-h930dp-xxhdpi")
class StoreShotsTest {
  @get:Rule val rule = createAndroidComposeRule<androidx.activity.ComponentActivity>()
  private val ctx: Context get() = ApplicationProvider.getApplicationContext()

  private fun snap(name: String) {
    rule.mainClock.advanceTimeBy(3000)
    rule.waitForIdle()
    val root = rule.activity.window.decorView
    val bmp = Bitmap.createBitmap(root.width, root.height, Bitmap.Config.ARGB_8888)
    root.draw(android.graphics.Canvas(bmp))
    val dir = File("build/store-shots").apply { mkdirs() }
    File(dir, "$name.png").outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
  }

  private fun seed(): AppPreferences {
    AppLanguage.current = "tr"
    val prefs = AppPreferences(ctx)
    prefs.country = "Türkiye"; prefs.province = "Manisa"; prefs.district = "Saruhanlı"; prefs.useCoordinates = false
    val json = javaClass.classLoader!!.getResource("diyanet_saruhanli_2026_11.json")!!.readText()
    ctx.getSharedPreferences("diyanet_source_v1", Context.MODE_PRIVATE).edit()
      .putString("id|turkiye|manisa|saruhanli", "9719").putString("month|9719", json).commit()
    return prefs
  }

  @Test fun qibla() {
    val prefs = seed()
    val place = QiblaPlace(38.7333, 27.5667, "Manisa / Saruhanlı", fromGps = false)
    val q = QiblaCalculator.trueBearing(place.lat, place.lon)
    rule.mainClock.autoAdvance = false
    rule.setContent {
      MyApplicationTheme(darkTheme = true) {
        QiblaFinderScreen(prefs = prefs, onNavigateBack = {}, testReading = CompassReading(q, true, 3, tiltDeg = 2.0), testPlace = place)
      }
    }
    snap("qibla")
  }

  @Test fun imsakiye() {
    val prefs = seed()
    rule.setContent { MyApplicationTheme(darkTheme = true) { ImsakiyeScreen(prefs = prefs, onNavigateBack = {}) } }
    snap("imsakiye")
  }

  @Test fun zakat() {
    seed()
    rule.setContent { MyApplicationTheme(darkTheme = true) { CalculatorsScreen(onNavigateBack = {}) } }
    rule.onNodeWithTag("fitre_people").performTextReplacement("4")
    snap("fitre")
  }
}
