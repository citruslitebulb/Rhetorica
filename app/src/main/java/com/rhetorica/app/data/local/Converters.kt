package com.rhetorica.app.data.local

import android.util.Log
import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class Converters @Inject constructor(
    private val json: Json
) {
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
            Log.e(TAG, "Failed to parse tags: $value", e)
            emptyList()
        }
    }
}
