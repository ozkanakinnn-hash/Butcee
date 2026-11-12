package akinai.minikgurme.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meals")
data class Meal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // YYYY-MM-DD
    val mealType: String, // "Sabah", "Öğle", "İkindi", "Akşam"
    val foodItems: String,
    val texture: String, // "Pürüzsüz Püre", "Çatalla Ezilmiş", "Pütürlü Ezme", "Parmak Gıda"
    val reaction: String, // "Sevdi", "Nötr", "İstemedi", "Alerji Sinyali"
    val notes: String = "",
    val timestamp: Long = akinai.minikgurme.shared.currentTimeMillis()
)
