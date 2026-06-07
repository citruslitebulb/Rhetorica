package com.rhetorica.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "dictionaries")
@Serializable
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
    val tags: List<String>,
    val themeCategories: List<String> = emptyList(),
    val isActive: Boolean = true,
)
