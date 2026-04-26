package com.vocabdaily.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "words")
data class WordEntity(
    @PrimaryKey val id: Long,
    val word: String,
    val definition: String,
    val example: String,
    val partOfSpeech: String,
)
