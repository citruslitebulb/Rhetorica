package com.rhetorica.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rhetorica.app.core.model.ThemeMode
import com.rhetorica.app.widget.WidgetImagePreset
import kotlinx.serialization.Serializable

@Entity(tableName = "user_preferences")
@Serializable
data class UserPreferencesEntity(
    @PrimaryKey val id: Int = 1,
    val favoriteOratorIds: List<Long> = emptyList(),
    val rotateThroughAll: Boolean = false,
    val selectedOratorId: Long? = null,
    val selectedThemeCategories: List<String> = emptyList(),
    val widgetBackgroundColor: Int = 0xFF2C3E50.toInt(),
    val widgetBackgroundOpacityPercent: Int = 80,
    val widgetBackgroundImageKey: String = WidgetImagePreset.None.key,
    val widgetGalleryUri: String = "",
    val onboardingCompleted: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val notificationHour: Int = 8,
    val notificationMinute: Int = 0,
    val themeMode: String = ThemeMode.System.storageValue,
    val includeFictionalOrators: Boolean = false,
    val includeLiteraryOrators: Boolean = true,
    val shownWotdIds: List<Long> = emptyList(),
    val shownWotdPoolKey: String = "",
    val todaysWotdId: Long? = null,
    val todaysWotdDate: String = "",
) {
    companion object {
        fun defaults(): UserPreferencesEntity = UserPreferencesEntity()
    }
}

fun UserPreferencesEntity?.orDefault(): UserPreferencesEntity = this ?: UserPreferencesEntity.defaults()
