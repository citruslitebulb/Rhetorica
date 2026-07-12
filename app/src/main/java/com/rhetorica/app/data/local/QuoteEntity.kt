package com.rhetorica.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(
    tableName = "quotes",
    indices = [Index(value = ["oratorId"])]
)
@Serializable
data class QuoteEntity(
    @PrimaryKey val id: Long,
    val oratorId: Long,
    val text: String,
    val source: String? = null,
    val speech: String? = null,
    val year: Int? = null,
    val context: String? = null,
)
