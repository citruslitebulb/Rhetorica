package com.vocabdaily.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_words")
data class SavedWordEntity(
    @PrimaryKey val wordId: Long,
    val savedAtEpochMillis: Long,
)
