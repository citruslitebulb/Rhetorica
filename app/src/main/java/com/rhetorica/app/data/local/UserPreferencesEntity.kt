package com.rhetorica.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "user_preferences")
@Serializable
data class UserPreferencesEntity(
    @PrimaryKey val id: Int = 1,
    val favoriteOratorIds: List<Long>,
    val rotateThroughAll: Boolean = false,
    val selectedOratorId: Long? = null,
    val widgetBackgroundColor: Int = 0xFF2C3E50.toInt(),
    val widgetBackgroundOpacityPercent: Int = 80,
)
