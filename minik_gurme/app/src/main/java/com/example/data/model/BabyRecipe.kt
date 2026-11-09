package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

data class RecipeIngredient(
    val name: String,
    val amount: String,
    val emoji: String = "🥄"
)

data class BabyRecipe(
    val id: String,
    val title: String,
    val ageMonths: Int,
    val ageLabel: String,
    val prepTime: String,
    val servings: String,
    val mealType: String, // "Kahvaltı", "Öğle", "Akşam", "Ara Öğün"
    val textureStyle: String, // "Püre", "BLW (Parmak Besin)", "Pütürlü Ezme"
    val tags: List<String>,
    val ingredients: List<RecipeIngredient>,
    val instructions: List<String>,
    val doctorTip: String,
    val emoji: String
)

@Entity(tableName = "favorite_recipes")
data class FavoriteRecipe(
    @PrimaryKey val recipeId: String,
    val timestamp: Long = System.currentTimeMillis()
)
