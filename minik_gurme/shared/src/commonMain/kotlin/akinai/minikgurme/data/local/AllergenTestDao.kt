package akinai.minikgurme.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import akinai.minikgurme.data.model.AllergenTest
import kotlinx.coroutines.flow.Flow

@Dao
interface AllergenTestDao {
    @Query("SELECT * FROM allergen_tests ORDER BY id DESC")
    fun getAllTests(): Flow<List<AllergenTest>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTest(test: AllergenTest): Long

    @Update
    suspend fun updateTest(test: AllergenTest)

    @Delete
    suspend fun deleteTest(test: AllergenTest)

    @Query("DELETE FROM allergen_tests")
    suspend fun deleteAllTests()

    @Query("DELETE FROM allergen_tests WHERE id = :id")
    suspend fun deleteTestById(id: Long)
}
