package com.example

import android.graphics.Bitmap
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppPreferences
import com.example.i18n.AppLanguage
import com.example.qibla.CompassReading
import com.example.qibla.QiblaCalculator
import com.example.qibla.QiblaFinderScreen
import com.example.qibla.QiblaPlace
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w400dp-h1250dp-xhdpi")
class QiblaScreenRenderTest {
  @get:Rule val rule = createAndroidComposeRule<androidx.activity.ComponentActivity>()
  private val place = QiblaPlace(38.7333, 27.5667, "Manisa / Saruhanlı", fromGps = false)
  private val qibla = QiblaCalculator.trueBearing(place.lat, place.lon)

  private fun render(name: String, heading: Double, dark: Boolean, en: Boolean = false, extra: CompassReading.() -> CompassReading = { this }) {
    AppLanguage.current = if (en) "en" else "tr"
    val prefs = AppPreferences(ApplicationProvider.getApplicationContext())
    val reading = CompassReading(continuousTrueHeading = heading, ready = true, accuracy = 3, tiltDeg = 3.0).extra()
    rule.mainClock.autoAdvance = false
    rule.setContent {
      MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme()) {
        QiblaFinderScreen(prefs = prefs, onNavigateBack = {}, testReading = reading, testPlace = place)
      }
    }
    rule.mainClock.advanceTimeBy(3000)
    rule.waitForIdle()
    val root = rule.activity.window.decorView
    val bmp = Bitmap.createBitmap(root.width, root.height, Bitmap.Config.ARGB_8888)
    root.draw(android.graphics.Canvas(bmp))
    val dir = File("build/qibla-renders").apply { mkdirs() }
    File(dir, "$name.png").outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
  }

  @Test fun aligned_dark() {
    render("aligned_dark", qibla, dark = true)
    rule.onNodeWithText("🕋 KIBLEYE DÖNDÜNÜZ").assertIsDisplayed()
    rule.onNodeWithTag("qibla_diyanet_card").assertIsDisplayed()
    rule.onNodeWithText("141°").assertIsDisplayed()          // Diyanet Kıble Açısı (Saruhanlı)
  }

  @Test fun turn_right_light() {
    render("turn_right_light", qibla - 40, dark = false)
    rule.onNodeWithText("Sağa doğru 40° dönün  ⟳").assertIsDisplayed()
  }

  @Test fun turn_left_interference_english() {
    render("turn_left_interference_en", qibla + 70, dark = true, en = true) { copy(interference = true, accuracy = 1) }
    rule.onNodeWithText("⟲  Turn left 70°").assertIsDisplayed()
  }
}
