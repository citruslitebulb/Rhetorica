package com.rhetorica.app.core.model

data class OratorProfile(
    val id: Long,
    val name: String,
    val category: String,
    val era: String,
    val bio: String,
    val portraitUrl: String,
    val primaryStyle: String,
    val voiceStyle: String,
    val colorAccent: Long,
    val sampleSpeech: String,
    val tags: List<String>,
    val isActive: Boolean = true,
)
