package com.rhetorica.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SpeechDao {
    @Query("SELECT * FROM speeches WHERE oratorId = :oratorId ORDER BY id ASC")
    fun observeSpeechesByOrator(oratorId: Long): Flow<List<SpeechEntity>>

    @Query("SELECT * FROM speeches ORDER BY oratorId ASC, id ASC")
    fun observeAllSpeeches(): Flow<List<SpeechEntity>>

    @Query("SELECT * FROM speeches WHERE oratorId = :oratorId AND title = :title LIMIT 1")
    suspend fun getSpeechByOratorAndTitle(oratorId: Long, title: String): SpeechEntity?

    @Query("SELECT * FROM speeches WHERE id = :speechId LIMIT 1")
    suspend fun getSpeechById(speechId: Long): SpeechEntity?

    @Query("SELECT COUNT(*) FROM speeches WHERE oratorId = :oratorId")
    suspend fun speechCountByOrator(oratorId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSpeeches(speeches: List<SpeechEntity>)

    @Query("SELECT id FROM speeches")
    suspend fun getAllSpeechIds(): List<Long>

    @Query("DELETE FROM speeches WHERE id IN (:ids)")
    suspend fun deleteSpeechesByIds(ids: List<Long>)

    @Query("SELECT COUNT(*) FROM speeches")
    suspend fun speechCount(): Int
}
