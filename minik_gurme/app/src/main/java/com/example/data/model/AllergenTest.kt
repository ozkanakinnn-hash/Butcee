package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "allergen_tests")
data class AllergenTest(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val food: String,
    val startDate: String,
    val day: Int = 1, // 1, 2, 3
    val status: String = "Devam Ediyor", // "Güvenli", "Devam Ediyor", "Reaksiyon", "Denenecek"
    val notes: String = ""
)
