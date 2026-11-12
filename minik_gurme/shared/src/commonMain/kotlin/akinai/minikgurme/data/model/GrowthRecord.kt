package akinai.minikgurme.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Bebeğin boy, kilo, baş çevresi ve ölçüm tarihlerini saklayan
 * Gelişim Takibi (Growth Record) veri modeli.
 */
@Entity(tableName = "growth_records")
data class GrowthRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // YYYY-MM-DD
    val weightKg: Float, // örn: 8.4 kg
    val heightCm: Float, // örn: 69.5 cm
    val headCircumferenceCm: Float? = null, // örn: 43.5 cm (isteğe bağlı baş çevresi)
    val ageMonths: Int, // Bebeğin ölçüm yapıldığı andaki ayı (örn: 7)
    val notes: String = "", // Doktor kontrolü notu, aşı günü vb.
    val timestamp: Long = akinai.minikgurme.shared.currentTimeMillis()
)
