package com.rhetorica.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface IdiomDao {
    @Query("SELECT * FROM idioms ORDER BY id ASC")
    fun observeIdioms(): Flow<List<IdiomEntity>>

    @Query("SELECT * FROM idioms ORDER BY id ASC")
    suspend fun getAll(): List<IdiomEntity>

    @Query("SELECT * FROM idioms WHERE id = :idiomId LIMIT 1")
    fun observeById(idiomId: Long): Flow<IdiomEntity?>

    @Query("SELECT COUNT(*) FROM idioms")
    suspend fun idiomCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertIdioms(idioms: List<IdiomEntity>)

    @Query("SELECT id FROM idioms")
    suspend fun getAllIds(): List<Long>

    @Query("DELETE FROM idioms WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)
}
