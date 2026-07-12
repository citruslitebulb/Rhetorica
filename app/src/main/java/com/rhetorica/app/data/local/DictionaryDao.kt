package com.rhetorica.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DictionaryDao {
    @Query("SELECT * FROM dictionaries")
    fun observeDictionaries(): Flow<List<DictionaryEntity>>

    @Query("SELECT * FROM dictionaries WHERE isActive = 1")
    fun observeActiveDictionaries(): Flow<List<DictionaryEntity>>

    @Query("SELECT * FROM dictionaries WHERE id = :dictionaryId LIMIT 1")
    suspend fun getDictionaryById(dictionaryId: Long): DictionaryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDictionaries(dictionaries: List<DictionaryEntity>)

    @Query("SELECT id FROM dictionaries")
    suspend fun getAllDictionaryIds(): List<Long>

    @Query("DELETE FROM dictionaries WHERE id IN (:ids)")
    suspend fun deleteDictionariesByIds(ids: List<Long>)

    @Query("UPDATE dictionaries SET isActive = :isActive WHERE id = :dictionaryId")
    suspend fun updateDictionaryActiveStatus(dictionaryId: Long, isActive: Boolean)

    @Query("SELECT COUNT(*) FROM dictionaries")
    suspend fun dictionaryCount(): Int

    @Query("SELECT * FROM dictionaries")
    suspend fun getAllDictionaries(): List<DictionaryEntity>
}
