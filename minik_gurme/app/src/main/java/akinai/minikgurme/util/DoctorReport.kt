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
import java.io.File
import akinai.minikgurme.i18n.t

/**
 * Doktor raporu (PDF) — Minik Gurme Plus. Profil, büyüme ölçümleri (WHO medyanıyla), alerjen denemeleri,
 * denenen besinler ve tepkiler, son 30 günün öğün sayısı. Dosya cihazda üretilir, kullanıcı paylaşır.
 */
object DoctorReport {
    private const val W = 595; private const val H = 842; private const val M = 40f

    suspend fun build(context: Context): File {
        val report = DoctorReportContent.build(AppDatabase.getDatabase(context))
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
        for (l in report.lines) when (l.style) {
            DoctorReportContent.Style.TITLE -> text(l.text, title, gap = 26f)
            DoctorReportContent.Style.SECTION -> { y += 8f; newPageIfNeeded(30f); canvas.drawLine(M, y, W - M, y, line); y += 18f; text(l.text, h2) }
            DoctorReportContent.Style.BODY -> text(l.text, body)
            DoctorReportContent.Style.BULLET -> text(l.text, body, x = M + 6f)
            DoctorReportContent.Style.MUTED -> if (l.text.isEmpty()) y += 14f else text(l.text, muted)
        }
        doc.finishPage(page)

        val dir = File(context.cacheDir, "reports").apply { mkdirs() }
        val file = File(dir, report.fileName)
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
