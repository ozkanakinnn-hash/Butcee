package com.example

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppPreferences
import com.example.i18n.AppLanguage
import com.example.reminder.ReminderScheduler
import com.example.ui.screens.CalculatorsScreen
import com.example.ui.screens.ImsakiyeScreen
import com.example.ui.screens.ReligiousAmounts
import com.example.ui.screens.buildImsakiye
import com.example.util.BackupManager
import com.example.util.NotificationHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.util.Calendar

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w400dp-h900dp-xhdpi")
class NewFeaturesTest {
  @get:Rule val rule = createAndroidComposeRule<androidx.activity.ComponentActivity>()
  private val ctx: Context get() = ApplicationProvider.getApplicationContext()

  @Before fun setup() { AppLanguage.current = "tr" }

  private fun seedDiyanet(prefs: AppPreferences) {
    prefs.country = "Türkiye"; prefs.province = "Manisa"; prefs.district = "Saruhanlı"; prefs.useCoordinates = false
    val json = javaClass.classLoader!!.getResource("diyanet_saruhanli_2026_11.json")!!.readText()
    val sp = ctx.getSharedPreferences("diyanet_source_v1", Context.MODE_PRIVATE)
    sp.edit().putString("id|turkiye|manisa|saruhanli", "9719").putString("month|9719", json).commit()
  }

  private fun snap(name: String) {
    rule.waitForIdle()
    val root = rule.activity.window.decorView
    val bmp = Bitmap.createBitmap(root.width, root.height, Bitmap.Config.ARGB_8888)
    root.draw(android.graphics.Canvas(bmp))
    val dir = File("build/feature-renders").apply { mkdirs() }
    File(dir, "$name.png").outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
  }

  // ── Hesaplayıcılar ──
  @Test fun religious_amounts() {
    assertEquals(240.0, ReligiousAmounts.FITRE_TL, 0.0)
    assertEquals(960.0, ReligiousAmounts.fitre(4), 0.0)
    assertEquals(7200.0, ReligiousAmounts.fidye(30), 0.0)
    // nisap 80,18 g × 4.000 TL = 320.720 TL
    assertNull(ReligiousAmounts.zakat(300_000.0, 4000.0))
    assertEquals(10_000.0, ReligiousAmounts.zakat(400_000.0, 4000.0)!!, 1e-6)
    assertNull(ReligiousAmounts.zakat(1e9, 0.0))
  }

  @Test fun calculators_screen_fitre_and_zakat() {
    rule.setContent { MaterialTheme(colorScheme = darkColorScheme()) { CalculatorsScreen(onNavigateBack = {}) } }
    rule.onNodeWithTag("fitre_people").performTextReplacement("5")
    rule.onNodeWithText("1.200 TL").assertIsDisplayed()
    snap("calc_fitre")
    rule.onNodeWithText("Zekât").performClick()
    rule.onNodeWithTag("zakat_gold_price").performTextReplacement("4000")
    rule.onNodeWithTag("zakat_cash").performTextReplacement("400000")
    rule.onNodeWithText("10.000 TL").assertIsDisplayed()
    snap("calc_zakat")
  }


  // ── İmsakiye ──
  @Test fun imsakiye_from_diyanet_cache_and_pdf() {
    val prefs = AppPreferences(ctx); seedDiyanet(prefs)
    val rows = buildImsakiye(ctx, prefs)
    assertTrue(rows.isNotEmpty())
    // Tablo bugünden başlar; her satır Diyanet tablosundaki değerle birebir aynı
    val json = javaClass.classLoader!!.getResource("diyanet_saruhanli_2026_11.json")!!.readText()
    for (r in rows) {
      val d = com.example.data.DiyanetSource.findDay(json, r.date)!!
      assertEquals(listOf(d.imsak, d.gunes, d.ogle, d.ikindi, d.aksam, d.yatsi), listOf(r.imsak, r.gunes, r.ogle, r.ikindi, r.aksam, r.yatsi))
    }
    // Kullanıcının ekran görüntüsündeki gün (07.11.2026)
    val d7 = com.example.data.DiyanetSource.findDay(json, "07.11.2026")!!
    assertEquals(listOf("06:12", "07:36", "12:58", "15:46", "18:11", "19:30"), listOf(d7.imsak, d7.gunes, d7.ogle, d7.ikindi, d7.aksam, d7.yatsi))
    // PDF sayfasının çizimi (Robolectric'te PdfDocument yerel kodu yok: aynı çizim bitmap'e)
    val bmp = Bitmap.createBitmap(595 * 2, 842 * 2, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bmp).apply { scale(2f, 2f) }
    com.example.ui.screens.drawImsakiyePage(canvas, prefs.province, prefs.district, rows)
    val dir = File("build/feature-renders").apply { mkdirs() }
    File(dir, "imsakiye_pdf_page.png").outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
  }

  @Test fun imsakiye_screen_render() {
    val prefs = AppPreferences(ctx); seedDiyanet(prefs)
    rule.setContent { MaterialTheme(colorScheme = darkColorScheme()) { ImsakiyeScreen(prefs = prefs, onNavigateBack = {}) } }
    rule.onNodeWithTag("imsakiye_share").assertIsDisplayed()
    snap("imsakiye")
  }

  // ── Yedekleme ──
  @Test fun backup_roundtrip_excludes_premium() {
    val prefs = AppPreferences(ctx)
    prefs.notifyCuma = false; prefs.cumaMinutesBefore = 30
    ctx.getSharedPreferences("zikirmatik", Context.MODE_PRIVATE).edit().putInt("count", 99).putStringSet("set", setOf("a", "b")).commit()
    ctx.getSharedPreferences("premium_state", Context.MODE_PRIVATE).edit().putBoolean("active", true).commit()
    val json = BackupManager.export(ctx)
    assertTrue(!json.contains("premium_state"))
    // telefon sıfırlandı
    BackupManager.FILES.forEach { ctx.getSharedPreferences(it, Context.MODE_PRIVATE).edit().clear().commit() }
    assertTrue(BackupManager.import(ctx, json) > 0)
    assertEquals(99, ctx.getSharedPreferences("zikirmatik", Context.MODE_PRIVATE).getInt("count", 0))
    assertEquals(setOf("a", "b"), ctx.getSharedPreferences("zikirmatik", Context.MODE_PRIVATE).getStringSet("set", null))
    assertEquals(false, AppPreferences(ctx).notifyCuma)
    assertEquals(30, AppPreferences(ctx).cumaMinutesBefore)
    assertEquals(-1, BackupManager.import(ctx, "{\"format\":\"baska\"}"))
    assertEquals(-1, BackupManager.import(ctx, "bozuk"))
  }

  // ── Cuma hatırlatıcısı ve canlı iftar sayacı ──
  @Test fun cuma_alarm_scheduled_before_friday_dhuhr() {
    val prefs = AppPreferences(ctx); seedDiyanet(prefs)
    prefs.notificationsEnabled = true; prefs.notifyCuma = true; prefs.cumaMinutesBefore = 45
    ReminderScheduler.rescheduleAll(ctx)
    val am = shadowOf(ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager)
    val cuma = am.scheduledAlarms.firstOrNull { it.operation?.let { op -> shadowOf(op).savedIntent.getStringExtra(ReminderScheduler.EXTRA_TYPE) } == "CUMA" }
    assertNotNull("Cuma alarmı kurulmalı", cuma)
    val c = Calendar.getInstance().apply { timeInMillis = cuma!!.triggerAtTime + 45 * 60_000L }
    assertEquals(Calendar.FRIDAY, c.get(Calendar.DAY_OF_WEEK))
  }

  @Test fun live_countdown_notification() {
    val target = System.currentTimeMillis() + 30 * 60_000L
    NotificationHelper.sendLiveCountdown(ctx, "🍽️ İftara kalan süre", "Manisa • İftar 18:11", target, 7303)
    val nm = shadowOf(ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
    val n = nm.getNotification(7303)
    assertNotNull(n)
    assertEquals(target, n.`when`)
    assertTrue(n.extras.getBoolean("android.chronometerCountDown"))
    assertTrue((n.flags and android.app.Notification.FLAG_ONGOING_EVENT) != 0)
    // geçmiş hedef: gönderilmez
    NotificationHelper.sendLiveCountdown(ctx, "x", "y", System.currentTimeMillis() - 1000, 7304)
    assertNull(nm.getNotification(7304))
  }
}
