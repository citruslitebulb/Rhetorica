package com.rhetorica.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_preferences")
data class UserPreferencesEntity(
    @PrimaryKey val id: Int = 1,
    val favoriteOratorIds: String,
    val rotateThroughAll: Boolean = false,
    val selectedOratorId: Long? = null,
)
