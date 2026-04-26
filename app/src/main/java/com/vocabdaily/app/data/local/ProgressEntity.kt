package com.vocabdaily.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "progress")
data class ProgressEntity(
    @PrimaryKey val id: Int = 1,
    val viewedCount: Int,
    val savedCount: Int,
    val quizCorrectCount: Int,
    val updatedAtEpochMillis: Long,
)
