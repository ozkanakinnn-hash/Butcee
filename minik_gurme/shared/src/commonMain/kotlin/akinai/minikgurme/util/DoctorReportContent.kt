package akinai.minikgurme.util

import akinai.minikgurme.data.local.AppDatabase
import akinai.minikgurme.i18n.t
import akinai.minikgurme.ui.components.WhoStandardsData
import kotlinx.coroutines.flow.first

/**
 * Doktor raporunun içeriği (Android ve iOS ortak). Platformlar bu satırları PDF sayfalarına çizer.
 * Profil, büyüme ölçümleri (WHO medyanıyla), alerjen denemeleri, denenen besinler ve son 30 gün.
 */
object DoctorReportContent {
    enum class Style { TITLE, SECTION, BODY, BULLET, MUTED }
    data class Line(val style: Style, val text: String)
    data class Report(val fileName: String, val lines: List<Line>)

    suspend fun build(db: AppDatabase): Report {
        val profile = db.babyProfileDao().getProfile().first()
        val growth = db.growthRecordDao().getAllGrowthRecords().first()
        val tests = db.allergenTestDao().getAllTests().first()
        val tried = db.triedFoodDao().getAllTriedFoods().first().filter { it.isTried }
        val meals = db.mealDao().getAllMeals().first()
        val today = DateKit.todayIso()
        val monthAgo = DateKit.isoPlusDays(today, -30)
        val sex = profile?.sex.orEmpty()
        val out = mutableListOf<Line>()
        fun add(s: Style, text: String) { out += Line(s, text) }

        add(Style.TITLE, t("Minik Gurme · Beslenme ve Gelişim Raporu"))
        add(Style.MUTED, "${t("Rapor tarihi")}: ${prettyDate(today)}")
        add(Style.SECTION, t("Bebek"))
        add(Style.BODY, "${t("Ad")}: ${profile?.name.orEmpty()}   ·   ${t("Yaş")}: ${profile?.ageMonths ?: "-"} ${t("ay")}   ·   ${t("Cinsiyet")}: ${t(when (sex) { "girl" -> "Kız"; "boy" -> "Erkek"; else -> "Belirtilmedi" })}")
        if (!profile?.startDate.isNullOrBlank()) add(Style.BODY, "${t("Ek gıdaya başlama")}: ${prettyDate(profile!!.startDate)}")

        add(Style.SECTION, t("Büyüme ölçümleri (WHO medyanı ile)"))
        if (growth.isEmpty()) add(Style.BODY, t("Kayıt yok."))
        growth.sortedBy { it.date }.forEach { g ->
            val ww = WhoStandardsData.getWhoWeight(g.ageMonths, sex); val wh = WhoStandardsData.getWhoHeight(g.ageMonths, sex)
            add(Style.BODY, "${prettyDate(g.date)} · ${g.ageMonths}. ${t("ay")} · ${g.weightKg.fmt(2)} kg (WHO ${ww.fmt(1)}) · ${g.heightCm.fmt(1)} cm (WHO ${wh.fmt(1)})" +
                (g.headCircumferenceCm?.let { " · ${t("baş çevresi")} ${it.fmt(1)} cm" } ?: ""))
        }

        add(Style.SECTION, t("Alerjen denemeleri"))
        if (tests.isEmpty()) add(Style.BODY, t("Kayıt yok."))
        tests.forEach { a -> add(Style.BODY, "${t(a.food)} · ${t(a.status)} · ${a.day}. ${t("gün")} · ${prettyDate(a.startDate)}" + (if (a.notes.isNotBlank()) " · ${t(a.notes)}" else "")) }

        add(Style.SECTION, t("Denenen besinler ve tepkiler"))
        add(Style.BODY, "${t("Toplam")}: ${tried.size} / 100")
        val reactions = mapOf("loved" to "Sevdi", "neutral" to "Nötr", "disliked" to "Sevmedi", "allergy" to "Alerji şüphesi")
        tried.sortedBy { it.triedDate }.forEach { f ->
            add(Style.BULLET, "• ${t(f.foodName)}" + (reactions[f.reaction]?.let { " — ${t(it)}" } ?: "") +
                (if (f.symptoms.isNotBlank()) " (${t(f.symptoms)})" else "") + (if (f.triedDate.isNotBlank()) " · ${prettyDate(f.triedDate)}" else ""))
        }

        add(Style.SECTION, t("Son 30 gün"))
        val recent = meals.filter { it.date >= monthAgo }
        add(Style.BODY, "${t("Kaydedilen öğün")}: ${recent.size}   ·   ${t("Alerji sinyali")}: ${recent.count { it.reaction == "Alerji Sinyali" }}")
        add(Style.MUTED, "")
        add(Style.MUTED, t("Bu rapor ebeveyn tarafından girilen kayıtlardan otomatik oluşturulmuştur; tıbbi değerlendirme yerine geçmez."))
        return Report("MinikGurme-rapor-$today.pdf", out)
    }
}
