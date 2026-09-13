package com.rhetorica.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Per-word spaced-repetition state. Rows exist only for words that have been
 * quizzed at least once; see `LeitnerScheduler` for the box semantics.
 */
@Entity(
    tableName = "word_progress",
    indices = [Index(value = ["nextDueAtEpochMillis"])],
)
data class WordProgressEntity(
    @PrimaryKey val wordId: Long,
    val box: Int,
    val correctCount: Int,
    val incorrectCount: Int,
    val lastReviewedAtEpochMillis: Long,
    val nextDueAtEpochMillis: Long,
)
