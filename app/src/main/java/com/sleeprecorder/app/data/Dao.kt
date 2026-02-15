package com.sleeprecorder.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SleepRecordDao {
    @Query("SELECT * FROM sleep_records ORDER BY date DESC")
    fun getAllRecords(): Flow<List<SleepRecord>>
    
    @Query("SELECT * FROM sleep_records ORDER BY date DESC LIMIT 1")
    suspend fun getLatestRecord(): SleepRecord?
    
    @Insert
    suspend fun insert(record: SleepRecord)
    
    @Delete
    suspend fun delete(record: SleepRecord)
    
    @Query("DELETE FROM sleep_records WHERE date < :date")
    suspend fun deleteBefore(date: Long)
    
    @Query("SELECT COUNT(*) FROM sleep_records")
    suspend fun getRecordCount(): Int
    
    @Query("SELECT AVG(duration) FROM sleep_records")
    suspend fun getAverageDuration(): Long?
}

@Dao
interface AudioSegmentDao {
    @Query("SELECT * FROM audio_segments WHERE recordId = :recordId ORDER BY startTime")
    suspend fun getSegmentsForRecord(recordId: String): List<AudioSegment>
    
    @Insert
    suspend fun insert(segment: AudioSegment)
    
    @Insert
    suspend fun insertAll(segments: List<AudioSegment>)
    
    @Query("SELECT COUNT(*) FROM audio_segments WHERE recordId = :recordId")
    suspend fun getSegmentCount(recordId: String): Int
    
    @Query("SELECT SUM(duration) FROM audio_segments WHERE recordId = :recordId")
    suspend fun getTotalRecordedDuration(recordId: String): Long?
}

@Dao
interface SettingsDao {
    @Query("SELECT * FROM app_settings WHERE id = 1")
    suspend fun getSettings(): AppSettings?
    
    @Query("SELECT * FROM app_settings WHERE id = 1")
    fun getSettingsFlow(): Flow<AppSettings?>
    
    @Insert
    suspend fun insert(settings: AppSettings)
    
    @Update
    suspend fun update(settings: AppSettings)
    
    @Query("INSERT OR REPLACE INTO app_settings (id, sensitivity, minRecordDuration) VALUES (1, :sensitivity, :duration)")
    suspend fun updateRecordingSettings(sensitivity: Float, duration: Int)
}