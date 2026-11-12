package akinai.minikgurme

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import akinai.minikgurme.data.local.AppDatabase
import akinai.minikgurme.data.local.closeAll
import akinai.minikgurme.data.local.getDatabase
import akinai.minikgurme.data.model.BabyProfile
import akinai.minikgurme.data.model.GrowthRecord
import akinai.minikgurme.data.model.Meal
import akinai.minikgurme.util.BackupCore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Ortak yedek biçimi: dışa aktar → sil → içe aktar; Android ve iOS aynı dosyayı kullanır. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BackupCoreTest {
    @Test
    fun roundTripRestoresEverything() = runBlocking {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        AppDatabase.closeAll(); ctx.deleteDatabase("backup_test_db")
        val db = AppDatabase.getDatabase(ctx, "backup_test_db")
        db.babyProfileDao().insertOrUpdateProfile(BabyProfile(id = 1, name = "Defne", ageMonths = 7, startDate = "2026-05-01", sex = "girl"))
        db.mealDao().insertMeal(Meal(date = "2026-11-10", mealType = "Öğle", foodItems = "Brokoli, Havuç", texture = "Püre", reaction = "Sevdi", notes = "İlk deneme \"tırnak\" ve ünicode ✓"))
        db.growthRecordDao().insertRecord(GrowthRecord(date = "2026-11-01", weightKg = 7.8f, heightCm = 68.5f, headCircumferenceCm = null, ageMonths = 7))

        val (json, rows) = BackupCore.exportJson(db)
        assertEquals(3, rows)
        assertTrue(json.contains("\"format\":\"minikgurme-backup\""))

        db.mealDao().deleteAllMeals(); db.growthRecordDao().getAllGrowthRecords().first().forEach { db.growthRecordDao().deleteRecordById(it.id) }
        assertEquals(0, db.mealDao().getAllMeals().first().size)

        assertEquals(3, BackupCore.importJson(db, json))
        val meal = db.mealDao().getAllMeals().first().single()
        assertEquals("İlk deneme \"tırnak\" ve ünicode ✓", meal.notes)
        val g = db.growthRecordDao().getAllGrowthRecords().first().single()
        assertEquals(7.8f, g.weightKg); assertEquals(null, g.headCircumferenceCm)
        assertEquals("girl", db.babyProfileDao().getProfile().first()!!.sex)

        // Bozuk dosya hiçbir şeyi değiştirmez
        assertTrue(runCatching { BackupCore.importJson(db, "{\"format\":\"başka\"}") }.isFailure)
        assertEquals(1, db.mealDao().getAllMeals().first().size)
        AppDatabase.closeAll()
    }

    @Test
    fun doctorReportContentIsComplete() = runBlocking {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        akinai.minikgurme.data.local.PlatformContext.init(ctx)
        AppDatabase.closeAll(); ctx.deleteDatabase("minik_gurme_db")
        val db = AppDatabase.getDatabase(ctx)
        db.babyProfileDao().insertOrUpdateProfile(BabyProfile(id = 1, name = "Defne", ageMonths = 7, startDate = "2026-05-01", sex = "girl"))
        db.growthRecordDao().insertRecord(GrowthRecord(date = "2026-11-01", weightKg = 7.8f, heightCm = 68.5f, headCircumferenceCm = 43f, ageMonths = 7))
        val report = akinai.minikgurme.util.DoctorReportContent.build(db)
        val all = report.lines.joinToString("\n") { it.text }
        assertTrue(all.contains("Defne")); assertTrue(all.contains("7.80 kg (WHO")); assertTrue(all.contains("Kız"))
        assertTrue(report.fileName.endsWith(".pdf"))
        // Not: Android PdfDocument Robolectric'te desteklenmiyor; PDF çizimi cihazda denenir.
        AppDatabase.closeAll()
    }
}
