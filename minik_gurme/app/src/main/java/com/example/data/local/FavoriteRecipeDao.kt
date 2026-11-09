package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.FavoriteRecipe
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteRecipeDao {
    @Query("SELECT recipeId FROM favorite_recipes")
    fun getAllFavoriteIds(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteRecipe)

    @Query("DELETE FROM favorite_recipes WHERE recipeId = :recipeId")
    suspend fun removeFavorite(recipeId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_recipes WHERE recipeId = :recipeId)")
    suspend fun isFavorite(recipeId: String): Boolean
}
