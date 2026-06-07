package com.rhetorica.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface QuoteDao {
    @Query("SELECT * FROM quotes WHERE oratorId = :oratorId ORDER BY id ASC")
    fun observeQuotesByOrator(oratorId: Long): Flow<List<QuoteEntity>>

    @Query("SELECT * FROM quotes WHERE oratorId = :oratorId ORDER BY id ASC")
    suspend fun getQuotesByOrator(oratorId: Long): List<QuoteEntity>

    @Query("SELECT * FROM quotes ORDER BY id ASC")
    fun observeAllQuotes(): Flow<List<QuoteEntity>>

    @Query("SELECT * FROM quotes WHERE id = :quoteId LIMIT 1")
    suspend fun getQuoteById(quoteId: Long): QuoteEntity?

    @Query("SELECT COUNT(*) FROM quotes WHERE oratorId = :oratorId")
    suspend fun quoteCountByOrator(oratorId: Long): Int

    @Query("SELECT COUNT(*) FROM quotes")
    suspend fun quoteCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertQuotes(quotes: List<QuoteEntity>)
}
