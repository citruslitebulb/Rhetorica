package com.rhetorica.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "opened_words")
data class OpenedWordEntity(
    @PrimaryKey val wordId: Long,
    val firstOpenedAtEpochMillis: Long,
)
