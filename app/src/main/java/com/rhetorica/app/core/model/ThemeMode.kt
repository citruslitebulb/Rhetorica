package com.rhetorica.app.core.model

enum class ThemeMode(val storageValue: String) {
    System("system"),
    Dark("dark"),
    Light("light"),
    ;

    companion object {
        fun fromStorage(value: String?): ThemeMode {
            return entries.firstOrNull { it.storageValue == value } ?: System
        }
    }
}
