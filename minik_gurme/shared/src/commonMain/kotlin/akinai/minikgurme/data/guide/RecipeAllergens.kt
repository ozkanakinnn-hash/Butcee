package akinai.minikgurme.data.guide

import akinai.minikgurme.data.model.BabyRecipe

/**
 * Tarif malzemelerinden başlıca alerjenleri çıkarır (AB 1169/2011 Ek II esas alınarak bebek tariflerinde geçenler).
 * Malzeme adlarına göre otomatik çalışır; ürün etiketinin yerini tutmaz (arayüzde bu uyarı gösterilir).
 */
object RecipeAllergens {
    /** Görünen ad (TR; arayüzde t() ile çevrilir) → malzeme adında aranan kelimeler (küçük harf). */
    private val rules: List<Pair<String, List<String>>> = listOf(
        "Yumurta" to listOf("yumurta"),
        "Süt" to listOf("yoğur", "peynir", "labne", "kaşar", "inek sütü", "tereyağ", "krema", "formül mama", "tarhana"),
        "Gluten" to listOf("buğday", "bulgur", "makarna", "şehriye", "irmik", "ekmek", "lavaş", "tarhana", "yulaf", "unu", "arpa"),
        "Balık" to listOf("somon", "balık", "levrek", "hamsi", "çupra", "mezgit", "ton balığı"),
        "Kuruyemiş" to listOf("ceviz", "fındık", "badem", "antep fıstığı", "kaju"),
        "Yer fıstığı" to listOf("yer fıstığı"),
        "Susam" to listOf("susam", "tahin"),
        "Soya" to listOf("soya"),
        "Kabuklu deniz ürünleri" to listOf("karides", "midye", "kalamar"),
    )

    /** Türkçe büyük İ/I harflerini doğru küçültür ("İnek" → "inek"). */
    private fun lowerTr(s: String) = s.replace('İ', 'i').replace('I', 'ı').lowercase()

    fun of(recipe: BabyRecipe): List<String> = of(recipe.ingredients.map { it.name })

    fun of(ingredientNames: List<String>): List<String> {
        val names = ingredientNames.map(::lowerTr)
        return rules.filter { (_, keys) -> names.any { n -> keys.any { it in n } } }.map { it.first }
    }
}
