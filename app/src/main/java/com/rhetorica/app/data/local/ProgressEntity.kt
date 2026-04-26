package com.rhetorica.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "progress")
@Serializable
data class ProgressEntity(
    @PrimaryKey val id: Int = 1,
    val viewedCount: Int,
    val savedCount: Int,
    val quizCorrectCount: Int,
    val updatedAtEpochMillis: Long,
)
