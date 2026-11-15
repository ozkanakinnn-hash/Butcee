package akinai.minikgurme.util

import akinai.minikgurme.data.model.AllergenTest
import akinai.minikgurme.data.model.FeedingDiaryEntry
import akinai.minikgurme.data.model.Meal
import akinai.minikgurme.shared.KeyValueStore
import akinai.minikgurme.ui.components.MAJOR_ALLERGENS
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Yüksek alerji riski (şiddetli egzama veya tanı konmuş gıda alerjisi) — bebek başına saklanır.
 * Kılavuzlar bu bebeklerde alerjen tanıtımının doktorla planlanmasını önerir; arayüz uyarı gösterir.
 */
object AllergyRisk {
    private val prefs by lazy { KeyValueStore("app_prefs") }
    private fun key(babyId: String) = if (babyId == Babies.MAIN) "allergy_high_risk" else "allergy_high_risk_$babyId"

    /** Etkin bebeğin durumu (Compose durumu; değişince ekranlar yeniden çizilir). */
    var high by mutableStateOf(false)
        private set

    fun load(babyId: String = Babies.activeId()) { high = prefs.getBoolean(key(babyId), false) }

    fun set(value: Boolean, babyId: String = Babies.activeId()) {
        prefs.putBoolean(key(babyId), value)
        if (babyId == Babies.activeId()) high = value
    }
}

/**
 * Alerjen sürdürme: güvenli bulunan alerjenler tolerans için düzenli (haftada birkaç kez) verilmeye devam etmeli.
 * Son verilme tarihi öğün kayıtları, beslenme günlüğü ve 3 gün denemesinin bitişinden hesaplanır.
 */
object AllergenMaintenance {
    const val OVERDUE_DAYS = 7

    data class Item(val name: String, val emoji: String, val lastGiven: String, val daysSince: Int) {
        val overdue get() = daysSince > OVERDUE_DAYS
    }

    fun compute(
        tests: List<AllergenTest>,
        meals: List<Meal>,
        diary: List<FeedingDiaryEntry>,
        today: String = DateKit.todayIso()
    ): List<Item> {
        val todayDate = DateKit.parseIso(today) ?: return emptyList()
        return MAJOR_ALLERGENS.mapNotNull { a ->
            val matches = tests.filter { t -> val f = t.food.lowercaseTr(); a.keywords.any { f.contains(it) } }
            if (matches.none { it.status == "Güvenli" } || matches.any { it.status == "Reaksiyon" }) return@mapNotNull null
            fun has(text: String) = text.lowercaseTr().let { s -> a.keywords.any { k -> s.contains(k) && !(k == "süt" && s.contains("anne sütü") && s.split("süt").size == 2) } }
            val dates = meals.filter { has(it.foodItems) }.map { it.date } +
                diary.filter { has(it.foodItems) }.map { it.date } +
                matches.filter { it.status == "Güvenli" }.map { DateKit.isoPlusDays(it.startDate, 2) }
            val last = dates.mapNotNull { d -> DateKit.parseIso(d)?.let { it to d } }.filter { it.first <= todayDate }.maxByOrNull { it.first }
                ?: return@mapNotNull null
            val days = (todayDate.toEpochDays() - last.first.toEpochDays()).toInt()
            Item(a.name, a.emoji, last.second, days)
        }.sortedByDescending { it.daysSince }
    }
}
