package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.TriedFood
import kotlinx.coroutines.flow.Flow

@Dao
interface TriedFoodDao {
    @Query("SELECT * FROM tried_foods ORDER BY timestamp DESC")
    fun getAllTriedFoods(): Flow<List<TriedFood>>

    @Query("SELECT COUNT(*) FROM tried_foods WHERE isTried = 1")
    fun getTriedCount(): Flow<Int>

    @Query("SELECT * FROM tried_foods WHERE foodId = :foodId LIMIT 1")
    suspend fun getTriedFoodById(foodId: String): TriedFood?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateTriedFood(triedFood: TriedFood)

    @Query("DELETE FROM tried_foods WHERE foodId = :foodId")
    suspend fun deleteTriedFood(foodId: String)
}
