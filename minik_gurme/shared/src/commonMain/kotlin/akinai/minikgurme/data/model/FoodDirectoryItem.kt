package akinai.minikgurme.data.model

data class FoodDirectoryItem(
    val id: String,
    val name: String,
    val category: String, // "Sebzeler", "Meyveler", "Tahıllar & Baklagiller", "Protein & Et", "Süt Ürünleri", "Tohumlar & Sağlıklı Yağlar"
    val minAgeMonths: Int,
    val ageDisplay: String,
    val isAllergen: Boolean,
    val isChokingHazard: Boolean,
    val isGutFriendly: Boolean,
    val nutrients: List<String>,
    val summary: String,
    val benefits: String,
    val allergenNotes: String,
    val serving6m: String,
    val serving9m: String,
    val serving12m: String,
    val storageTips: String,
    val safetyAlert: String,
    val emoji: String
)
