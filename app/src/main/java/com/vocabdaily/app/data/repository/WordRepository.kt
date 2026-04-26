package com.vocabdaily.app.data.repository

import com.vocabdaily.app.data.local.WordDao
import com.vocabdaily.app.data.local.WordEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WordRepository @Inject constructor(
    private val wordDao: WordDao,
) {
    fun observeWords(): Flow<List<WordEntity>> = wordDao.observeWords()

    suspend fun getWordOfTheDay(): WordEntity? = wordDao.getWordOfTheDay()

    suspend fun seedWordsIfEmpty() {
        if (wordDao.wordCount() > 0) return

        wordDao.upsertWords(
            listOf(
                WordEntity(
                    id = 1,
                    word = "Serendipity",
                    definition = "The occurrence of events by chance in a happy way.",
                    example = "Finding this cafe was pure serendipity.",
                    partOfSpeech = "noun",
                ),
                WordEntity(
                    id = 2,
                    word = "Ephemeral",
                    definition = "Lasting for a very short time.",
                    example = "Morning mist is beautiful but ephemeral.",
                    partOfSpeech = "adjective",
                ),
                WordEntity(
                    id = 3,
                    word = "Ubiquitous",
                    definition = "Present, appearing, or found everywhere.",
                    example = "Smartphones are ubiquitous in modern life.",
                    partOfSpeech = "adjective",
                ),
            ),
        )
    }
}
