package akinai.minikgurme.util

import akinai.minikgurme.data.local.getDatabase
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import akinai.minikgurme.data.local.AppDatabase
import akinai.minikgurme.i18n.t
import akinai.minikgurme.ui.components.WhoStandardsData
import kotlinx.coroutines.flow.first
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Doktor raporu (PDF) — Minik Gurme Plus. Profil, büyüme ölçümleri (WHO medyanıyla), alerjen denemeleri,
 * denenen besinler ve tepkiler, son 30 günün öğün sayısı. Dosya cihazda üretilir, kullanıcı paylaşır.
 */
object DoctorReport {
    private const val W = 595; private const val H = 842; private const val M = 40f

    suspend fun build(context: Context): File {
        val db = AppDatabase.getDatabase(context)
        val profile = db.babyProfileDao().getProfile().first()
        val growth = db.growthRecordDao().getAllGrowthRecords().first()
        val tests = db.allergenTestDao().getAllTests().first()
        val tried = db.triedFoodDao().getAllTriedFoods().first().filter { it.isTried }
        val meals = db.mealDao().getAllMeals().first()
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val monthAgo = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(System.currentTimeMillis() - 30L * 86_400_000))
        val sex = profile?.sex.orEmpty()

        val doc = PdfDocument()
        var page = doc.startPage(PdfDocument.PageInfo.Builder(W, H, 1).create())
        var canvas = page.canvas
        var y = M
        val title = Paint().apply { color = Color.rgb(52, 70, 176); textSize = 20f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); isAntiAlias = true }
        val h2 = Paint().apply { color = Color.rgb(31, 34, 56); textSize = 13f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); isAntiAlias = true }
        val body = Paint().apply { color = Color.rgb(60, 64, 90); textSize = 10.5f; isAntiAlias = true }
        val muted = Paint().apply { color = Color.rgb(120, 124, 150); textSize = 9f; isAntiAlias = true }
        val line = Paint().apply { color = Color.rgb(220, 222, 236); strokeWidth = 1f }
        var pageNo = 1
        fun newPageIfNeeded(need: Float) {
            if (y + need < H - M) return
            doc.finishPage(page); pageNo++
            page = doc.startPage(PdfDocument.PageInfo.Builder(W, H, pageNo).create()); canvas = page.canvas; y = M
        }
        fun text(s: String, p: Paint, x: Float = M, gap: Float = p.textSize + 5f) {
            // Uzun satırları kelime kelime böl
            val maxW = W - x - M
            var cur = ""
            for (word in s.split(" ")) {
                val next = if (cur.isEmpty()) word else "$cur $word"
                if (p.measureText(next) > maxW && cur.isNotEmpty()) { newPageIfNeeded(gap); canvas.drawText(cur, x, y, p); y += gap; cur = word } else cur = next
            }
            if (cur.isNotEmpty()) { newPageIfNeeded(gap); canvas.drawText(cur, x, y, p); y += gap }
        }
        fun section(s: String) { y += 8f; newPageIfNeeded(30f); canvas.drawLine(M, y, W - M, y, line); y += 18f; text(t(s), h2) }

        text(t("Minik Gurme · Beslenme ve Gelişim Raporu"), title, gap = 26f)
        text("${t("Rapor tarihi")}: ${prettyDate(today)}", muted)
        section("Bebek")
        text("${t("Ad")}: ${profile?.name.orEmpty()}   ·   ${t("Yaş")}: ${profile?.ageMonths ?: "-"} ${t("ay")}   ·   ${t("Cinsiyet")}: ${t(when (sex) { "girl" -> "Kız"; "boy" -> "Erkek"; else -> "Belirtilmedi" })}", body)
        if (!profile?.startDate.isNullOrBlank()) text("${t("Ek gıdaya başlama")}: ${prettyDate(profile!!.startDate)}", body)

        section("Büyüme ölçümleri (WHO medyanı ile)")
        if (growth.isEmpty()) text(t("Kayıt yok."), body)
        growth.sortedBy { it.date }.forEach { g ->
            val ww = WhoStandardsData.getWhoWeight(g.ageMonths, sex); val wh = WhoStandardsData.getWhoHeight(g.ageMonths, sex)
            text("${prettyDate(g.date)} · ${g.ageMonths}. ${t("ay")} · ${(g.weightKg).fmt(2)} kg (WHO ${(ww).fmt(1)}) · ${(g.heightCm).fmt(1)} cm (WHO ${(wh).fmt(1)})" +
                (g.headCircumferenceCm?.let { " · ${t("baş çevresi")} ${(it).fmt(1)} cm" } ?: ""), body)
        }

        section("Alerjen denemeleri")
        if (tests.isEmpty()) text(t("Kayıt yok."), body)
        tests.forEach { a -> text("${t(a.food)} · ${t(a.status)} · ${a.day}. ${t("gün")} · ${prettyDate(a.startDate)}" + (if (a.notes.isNotBlank()) " · ${t(a.notes)}" else ""), body) }

        section("Denenen besinler ve tepkiler")
        text("${t("Toplam")}: ${tried.size} / 100", body)
        val reactions = mapOf("loved" to "Sevdi", "neutral" to "Nötr", "disliked" to "Sevmedi", "allergy" to "Alerji şüphesi")
        tried.sortedBy { it.triedDate }.forEach { f ->
            text("• ${t(f.foodName)}" + (reactions[f.reaction]?.let { " — ${t(it)}" } ?: "") +
                (if (f.symptoms.isNotBlank()) " (${t(f.symptoms)})" else "") + (if (f.triedDate.isNotBlank()) " · ${prettyDate(f.triedDate)}" else ""), body, x = M + 6f)
        }

        section("Son 30 gün")
        val recent = meals.filter { it.date >= monthAgo }
        text("${t("Kaydedilen öğün")}: ${recent.size}   ·   ${t("Alerji sinyali")}: ${recent.count { it.reaction == "Alerji Sinyali" }}", body)

        y += 14f
        text(t("Bu rapor ebeveyn tarafından girilen kayıtlardan otomatik oluşturulmuştur; tıbbi değerlendirme yerine geçmez."), muted)
        doc.finishPage(page)

        val dir = File(context.cacheDir, "reports").apply { mkdirs() }
        val file = File(dir, "MinikGurme-rapor-$today.pdf")
        file.outputStream().use { doc.writeTo(it) }
        doc.close()
        return file
    }

    fun share(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"; putExtra(Intent.EXTRA_STREAM, uri); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(send, t("Raporu paylaş")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}
