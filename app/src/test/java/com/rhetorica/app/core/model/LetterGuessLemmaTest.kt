package com.rhetorica.app.core.model

import com.rhetorica.app.data.local.WordEntity
import com.rhetorica.app.feature.quiz.WordGuessDifficulty
import kotlinx.serialization.json.Json
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class LetterGuessLemmaTest {

    @Test
    fun `advertise stays and its inflected forms do not`() {
        assertTrue(LetterGuessLemma.isBaseForm("advertise", "verb"))
        assertFalse(LetterGuessLemma.isBaseForm("advertising", "noun"))
        assertFalse(LetterGuessLemma.isBaseForm("advertised", "verb"))
        assertFalse(LetterGuessLemma.isBaseForm("advertised", "adjective"))
        assertFalse(LetterGuessLemma.isBaseForm("self-limiting", "adjective"))
    }

    @Test
    fun `roots that only look inflected stay eligible`() {
        assertTrue(LetterGuessLemma.isBaseForm("bring", "verb"))
        assertTrue(LetterGuessLemma.isBaseForm("need", "verb"))
        assertTrue(LetterGuessLemma.isBaseForm("need", "noun"))
        assertTrue(LetterGuessLemma.isBaseForm("bless", "verb"))
        assertTrue(LetterGuessLemma.isBaseForm("kindly", "adverb"))
        assertTrue(LetterGuessLemma.isBaseForm("kindly", "adjective"))
        assertTrue(LetterGuessLemma.isBaseForm("king", "noun"))
        assertTrue(LetterGuessLemma.isBaseForm("morning", "noun"))
        assertTrue(LetterGuessLemma.isBaseForm("sacred", "adjective"))
        assertTrue(LetterGuessLemma.isBaseForm("canvas", "noun"))
        assertTrue(LetterGuessLemma.isBaseForm("focus", "noun"))
        assertTrue(LetterGuessLemma.isBaseForm("creed", "noun"))
        assertTrue(LetterGuessLemma.isBaseForm("means", "noun"))
        assertTrue(LetterGuessLemma.isBaseForm("physics", "noun"))
        assertTrue(LetterGuessLemma.isBaseForm("economics", "noun"))
        assertTrue(LetterGuessLemma.isBaseForm("shortcoming", "noun"))
        assertTrue(LetterGuessLemma.isBaseForm("notwithstanding", "adverb"))
        assertTrue(LetterGuessLemma.isBaseForm("worldly", "adjective"))
        assertTrue(LetterGuessLemma.isBaseForm("pass", "verb"))
        assertTrue(LetterGuessLemma.isBaseForm("oppress", "verb"))
        assertTrue(LetterGuessLemma.isBaseForm("surrender", "verb"))
        assertFalse(LetterGuessLemma.isBaseForm("used", "verb"))
    }

    @Test
    fun `plurals ly adverbs and superlatives with a library base are not citation forms`() {
        assertFalse(LetterGuessLemma.isBaseForm("ancestors", "noun"))
        assertFalse(LetterGuessLemma.isBaseForm("impulses", "noun"))
        assertFalse(LetterGuessLemma.isBaseForm("inequities", "noun"))
        assertFalse(LetterGuessLemma.isBaseForm("multitudes", "noun"))
        assertFalse(LetterGuessLemma.isBaseForm("courageously", "adverb"))
        assertFalse(LetterGuessLemma.isBaseForm("outstanding", "adjective"))
        assertTrue(LetterGuessLemma.isBaseForm("chiefest", "adjective"))
        assertFalse(LetterGuessLemma.isBaseForm("chiefest", "adjective", setOf("chief")))
        assertTrue(LetterGuessLemma.isBaseForm("protest", "noun", setOf("prot")))
        assertTrue(LetterGuessLemma.isBaseForm("honest", "adjective", setOf("hon")))
    }

    @Test
    fun `difficulty length and complexity still gate letter guess`() {
        val lexicon = setOf("chief")
        assertTrue(
            LetterGuessLemma.isCandidate(
                word = "advertise",
                partOfSpeech = "verb",
                complexity = "intermediate",
                minLetters = 7,
                maxLetters = 10,
                allowedComplexity = WordComplexity.Intermediate,
                lexicon = lexicon,
            ),
        )
        assertFalse(
            LetterGuessLemma.isCandidate(
                word = "advertise",
                partOfSpeech = "verb",
                complexity = "intermediate",
                minLetters = 4,
                maxLetters = 5,
                allowedComplexity = WordComplexity.All,
            ),
        )
        assertFalse(
            LetterGuessLemma.isCandidate(
                word = "advertise",
                partOfSpeech = "verb",
                complexity = "advanced",
                minLetters = 7,
                maxLetters = 10,
                allowedComplexity = WordComplexity.Basic,
            ),
        )
        assertFalse(
            LetterGuessLemma.isCandidate(
                word = "advertising",
                partOfSpeech = "noun",
                complexity = "intermediate",
                minLetters = 4,
                maxLetters = 14,
                allowedComplexity = WordComplexity.All,
            ),
        )
        assertTrue(
            LetterGuessLemma.isCandidate(
                word = "lamp",
                partOfSpeech = "noun",
                complexity = "beginner",
                minLetters = 4,
                maxLetters = 5,
                allowedComplexity = WordComplexity.Basic,
            ),
        )
    }

    @Test
    fun `every difficulty still has a base-form pool after the seed is filtered`() {
        val words = loadSeedWords()
        val lexicon = words.mapTo(HashSet()) { LetterGuessLemma.normalize(it.word) }
        val tiers = listOf(
            "all" to WordComplexity.All,
            "basic" to WordComplexity.Basic,
            "intermediate" to WordComplexity.Intermediate,
            "advanced" to WordComplexity.Advanced,
        )
        val floors = mapOf(
            WordGuessDifficulty.Easy to mapOf("all" to 150, "basic" to 40, "intermediate" to 50, "advanced" to 8),
            WordGuessDifficulty.Medium to mapOf("all" to 200, "basic" to 40, "intermediate" to 80, "advanced" to 20),
            WordGuessDifficulty.Hard to mapOf("all" to 600, "basic" to 20, "intermediate" to 300, "advanced" to 150),
            WordGuessDifficulty.Hardcore to mapOf("all" to 1000, "basic" to 80, "intermediate" to 500, "advanced" to 250),
        )
        val report = StringBuilder()
        for (difficulty in WordGuessDifficulty.entries) {
            for ((tierName, tier) in tiers) {
                val before = words.count { word ->
                    inBand(word, difficulty) && tier.matches(word.complexity)
                }
                val after = words.count { word ->
                    LetterGuessLemma.isCandidate(
                        word = word.word,
                        partOfSpeech = word.partOfSpeech,
                        complexity = word.complexity,
                        minLetters = difficulty.minLetters,
                        maxLetters = difficulty.maxLetters,
                        allowedComplexity = tier,
                        lexicon = lexicon,
                    )
                }
                report.append("${difficulty.name} $tierName $before -> $after\n")
                val floor = floors.getValue(difficulty).getValue(tierName)
                assertTrue(
                    "${difficulty.name} $tierName pool $after is below $floor (before $before)",
                    after >= floor,
                )
                assertTrue(after <= before)
            }
        }
        val advertising = words.filter { LetterGuessLemma.normalize(it.word) == "advertising" }
        assertTrue(advertising.isNotEmpty())
        assertTrue(advertising.none { LetterGuessLemma.isBaseForm(it.word, it.partOfSpeech, lexicon) })
        println(report.toString())
    }

    private fun inBand(word: WordEntity, difficulty: WordGuessDifficulty): Boolean {
        val letters = word.word.count { it.isLetter() }
        return letters in difficulty.minLetters..difficulty.maxLetters
    }

    private fun loadSeedWords(): List<WordEntity> {
        val seedDir = listOf(
            File("src/main/assets/data/seed"),
            File("app/src/main/assets/data/seed"),
        ).first { it.isDirectory }
        val json = Json { ignoreUnknownKeys = true }
        return seedDir.listFiles { file ->
            file.name.startsWith("words_") && file.name.endsWith(".json")
        }.orEmpty().flatMap { file ->
            json.decodeFromString<List<WordEntity>>(file.readText())
        }
    }
}
