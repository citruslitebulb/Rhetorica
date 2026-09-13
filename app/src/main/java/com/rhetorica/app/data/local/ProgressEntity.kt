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
    val quizAttemptCount: Int = 0,
    val quizStreak: Int = 0,
    val bestQuizStreak: Int = 0,
    /** Consecutive local days with any learning activity (open or quiz). */
    val dailyStreak: Int = 0,
    val bestDailyStreak: Int = 0,
    /** ISO local date of the most recent activity, or "" if none. */
    val lastActiveDate: String = "",
    val updatedAtEpochMillis: Long,
)
