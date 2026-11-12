package akinai.minikgurme.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tried_foods")
data class TriedFood(
    @PrimaryKey val foodId: String,
    val foodName: String,
    val isTried: Boolean = true,
    val reaction: String = "", // "loved", "neutral", "disliked", "allergy"
    val symptoms: String = "", // comma-separated symptoms: "Hapşırma, Döküntü"
    val triedDate: String = "",
    val reactionNotes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
