package com.rhetorica.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "dictionaries")
@TypeConverters(Converters::class)
data class DictionaryEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val description: String,
    val oratorName: String,
    val wordCount: Int,
    val category: String,
    val era: String,
    val bio: String,
    val portraitUrl: String,
    val primaryStyle: String,
    val voiceStyle: String,
    val colorAccent: Long,
    val sampleSpeech: String,
    val tags: String,
    val isActive: Boolean = true,
)
