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

    @Query("SELECT * FROM quotes WHERE oratorId = :oratorId ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomQuoteByOrator(oratorId: Long): QuoteEntity?

    /**
     * Prefer a quote tied to the same speech/source as the word the user is viewing.
     */
    @Query(
        """
        SELECT * FROM quotes
        WHERE oratorId = :oratorId
          AND (
            (:speech IS NOT NULL AND speech = :speech)
            OR (:source IS NOT NULL AND (source = :source OR speech = :source))
            OR (:speech IS NOT NULL AND source = :speech)
          )
        ORDER BY RANDOM()
        LIMIT 1
        """,
    )
    suspend fun getQuoteMatchingSpeechOrSource(
        oratorId: Long,
        speech: String?,
        source: String?,
    ): QuoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertQuotes(quotes: List<QuoteEntity>)

    @Query("SELECT id FROM quotes")
    suspend fun getAllQuoteIds(): List<Long>

    @Query("DELETE FROM quotes WHERE id IN (:ids)")
    suspend fun deleteQuotesByIds(ids: List<Long>)
}
