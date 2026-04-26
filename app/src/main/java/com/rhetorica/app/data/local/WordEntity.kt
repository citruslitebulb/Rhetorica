package com.rhetorica.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "words", indices = [androidx.room.Index(value = ["oratorId"])])
@Serializable
data class WordEntity(
    @PrimaryKey val id: Long,
    val word: String,
    val definition: String,
    val example: String,
    val partOfSpeech: String,
    val oratorId: Long?,
)
