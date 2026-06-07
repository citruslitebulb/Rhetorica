package com.rhetorica.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(
    tableName = "speeches",
    indices = [Index(value = ["oratorId"])]
)
@Serializable
data class SpeechEntity(
    @PrimaryKey val id: Long,
    val oratorId: Long,
    val title: String,
    val fullText: String,
    val year: Int? = null,
    val description: String? = null,
)
