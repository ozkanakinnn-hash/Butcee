package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "baby_profile")
data class BabyProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "Alp",
    val ageMonths: Int = 7,
    val startDate: String = "2024-01-15",
    val notes: String = ""
)
