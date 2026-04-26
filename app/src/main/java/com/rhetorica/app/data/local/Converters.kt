package com.rhetorica.app.data.local

import android.util.Log
import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    private val json = Json { ignoreUnknownKeys = true }
    private val TAG = "Converters"

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return json.encodeToString(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return try {
            json.decodeFromString(value)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse string list: $value", e)
            emptyList()
        }
    }

    @TypeConverter
    fun fromLongList(value: List<Long>): String {
        return json.encodeToString(value)
    }

    @TypeConverter
    fun toLongList(value: String): List<Long> {
        return try {
            json.decodeFromString(value)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse long list: $value", e)
            emptyList()
        }
    }
}
