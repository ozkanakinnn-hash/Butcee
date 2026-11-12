package akinai.minikgurme.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import akinai.minikgurme.data.model.GrowthRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface GrowthRecordDao {
    @Query("SELECT * FROM growth_records ORDER BY date ASC, timestamp ASC")
    fun getAllGrowthRecords(): Flow<List<GrowthRecord>>

    @Query("SELECT * FROM growth_records ORDER BY date DESC, timestamp DESC LIMIT 1")
    fun getLatestGrowthRecord(): Flow<GrowthRecord?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: GrowthRecord): Long

    @Update
    suspend fun updateRecord(record: GrowthRecord)

    @Delete
    suspend fun deleteRecord(record: GrowthRecord)

    @Query("DELETE FROM growth_records WHERE id = :id")
    suspend fun deleteRecordById(id: Long)

    @Query("SELECT COUNT(*) FROM growth_records")
    fun getRecordCount(): Flow<Int>
}
