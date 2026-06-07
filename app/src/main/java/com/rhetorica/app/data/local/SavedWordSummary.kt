package com.rhetorica.app.data.local

data class SavedWordSummary(
    val id: Long,
    val word: String,
    val definition: String,
    val example: String,
    val partOfSpeech: String,
    val oratorId: Long?,
    val savedAtEpochMillis: Long,
    val source: String? = null,
    val speech: String? = null,
)
