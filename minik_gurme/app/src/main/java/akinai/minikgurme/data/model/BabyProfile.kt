package akinai.minikgurme.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "baby_profile")
data class BabyProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "",
    val ageMonths: Int = 6,
    val startDate: String = "",
    val notes: String = "",
    /** "girl" | "boy" | "" — WHO karşılaştırmaları için */
    val sex: String = ""
)
