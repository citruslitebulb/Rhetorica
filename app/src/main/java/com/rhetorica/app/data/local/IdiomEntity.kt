package com.rhetorica.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "idioms")
@Serializable
data class IdiomEntity(
    @PrimaryKey val id: Long,
    val idiom: String,
    val meaning: String,
    val example: String,
    val complexity: String,
    val origin: String? = null,
    val oratorId: Long? = null,
    val quoteId: Long? = null,
)
