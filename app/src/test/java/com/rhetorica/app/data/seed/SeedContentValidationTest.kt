package com.rhetorica.app.data.seed

import com.rhetorica.app.data.local.DictionaryEntity
import com.rhetorica.app.data.local.QuoteEntity
import com.rhetorica.app.data.local.SpeechEntity
import com.rhetorica.app.data.local.WordEntity
import com.rhetorica.app.feature.speech.findMatchingSpeech
import kotlinx.serialization.json.Json
import org.junit.Assert.fail
import org.junit.Test
import java.io.File

/**
 * Guards the editorial invariants of the bundled seed content: no filler text,
 * no duplicate speeches or ids, valid orator references, and working
 * "Read full speech" links for orators whose catalogues are fully linked.
 */
class SeedContentValidationTest {

    private val json = Json { ignoreUnknownKeys = true }

    private val seedDir: File = listOf(
        File("src/main/assets/data/seed"),
        File("app/src/main/assets/data/seed"),
    ).firstOrNull { it.isDirectory }
        ?: File("src/main/assets/data/seed")

    @Test
    fun `no seed definition contains filler boilerplate`() {
        val failures = wordFiles().flatMap { (fileName, words) ->
            words.filter { it.definition.contains("gains force in public speech") }
                .map { "$fileName: word='${it.word}' (id=${it.id}) still has filler boilerplate" }
        }
        if (failures.isNotEmpty()) {
            fail("Filler definitions found:\n${failures.joinToString("\n")}")
        }
    }

    @Test
    fun `no two speeches share the same fullText`() {
        val speeches = loadSpeeches()
        val dupes = speeches.groupBy { it.fullText }.filterValues { it.size > 1 }
        if (dupes.isNotEmpty()) {
            val detail = dupes.values.joinToString("\n") { group ->
                group.joinToString(" <=> ") { "[orator ${it.oratorId}] id=${it.id} '${it.title}'" }
            }
            fail("Duplicate speech texts found:\n$detail")
        }
    }

    @Test
    fun `word quote and speech ids are unique within each table`() {
        val wordIds = wordFiles().flatMap { (_, words) -> words.map { it.id } }
        val quoteIds = quoteFiles().flatMap { (_, quotes) -> quotes.map { it.id } }
        val speechIds = loadSpeeches().map { it.id }

        val failures = mutableListOf<String>()
        listOf("words" to wordIds, "quotes" to quoteIds, "speeches" to speechIds).forEach { (table, ids) ->
            ids.groupingBy { it }.eachCount().filterValues { it > 1 }.forEach { (id, count) ->
                failures.add("$table: id=$id appears $count times")
            }
        }
        if (failures.isNotEmpty()) {
            fail("Duplicate seed ids (Room upsert would silently drop rows):\n${failures.joinToString("\n")}")
        }
    }

    @Test
    fun `all word and quote oratorIds reference real dictionaries`() {
        val validIds = loadDictionaries().map { it.id }.toSet()
        val failures = mutableListOf<String>()
        wordFiles().forEach { (fileName, words) ->
            words.filter { it.oratorId !in validIds }
                .forEach { failures.add("$fileName: word='${it.word}' has invalid oratorId=${it.oratorId}") }
        }
        quoteFiles().forEach { (fileName, quotes) ->
            quotes.filter { it.oratorId !in validIds }
                .forEach { failures.add("$fileName: quote id=${it.id} has invalid oratorId=${it.oratorId}") }
        }
        if (failures.isNotEmpty()) {
            fail("Invalid orator references:\n${failures.joinToString("\n")}")
        }
    }

    @Test
    fun `fully linked orators resolve every word speech ref`() {
        val speechesByOrator = loadSpeeches().groupBy { it.oratorId }
        val failures = mutableListOf<String>()
        wordFiles().forEach { (fileName, words) ->
            words.filter { it.oratorId in FULLY_LINKED_ORATOR_IDS }.forEach { word ->
                val candidates = speechesByOrator[word.oratorId].orEmpty()
                val ref = word.speech
                if (ref.isNullOrBlank() || findMatchingSpeech(candidates, ref) == null) {
                    failures.add(
                        "$fileName: word='${word.word}' speech='${word.speech}' " +
                            "resolves to no shipped speech for orator ${word.oratorId}",
                    )
                }
            }
        }
        if (failures.isNotEmpty()) {
            fail(
                "Broken 'Read full speech' links for fully-linked orators " +
                    "(fix the ref, or drop the orator from FULLY_LINKED_ORATOR_IDS):\n" +
                    failures.joinToString("\n"),
            )
        }
    }

    @Test
    fun `every orator has enough words for narrow filters`() {
        val failures = wordFiles()
            .filter { (_, words) -> words.size < MIN_WORDS_PER_ORATOR }
            .map { (fileName, words) -> "$fileName: only ${words.size} words (min $MIN_WORDS_PER_ORATOR)" }
        if (failures.isNotEmpty()) {
            fail("Orators too thin to browse single-orator:\n${failures.joinToString("\n")}")
        }
    }

    @Test
    fun `every theme has enough words for theme filters`() {
        val totals = mutableMapOf<String, Int>()
        wordFiles().forEach { (_, words) ->
            words.forEach { word ->
                word.categories.forEach { theme ->
                    totals[theme] = (totals[theme] ?: 0) + 1
                }
            }
        }
        val failures = MIN_WORDS_PER_THEME
            .filter { (theme, min) -> (totals[theme] ?: 0) < min }
            .map { (theme, min) -> "$theme: ${totals[theme] ?: 0} attributions (min $min)" }
        if (failures.isNotEmpty()) {
            fail("Themes too thin to browse single-theme:\n${failures.joinToString("\n")}")
        }
    }

    private fun wordFiles(): List<Pair<String, List<WordEntity>>> {
        val files = seedDir.listFiles { _, name ->
            name.startsWith("words_") && name.endsWith(".json")
        }?.sortedBy { it.name } ?: emptyList()
        if (files.isEmpty()) fail("No words_*.json files found in seed directory.")
        return files.map { it.name to json.decodeFromString<List<WordEntity>>(it.readText()) }
    }

    private fun quoteFiles(): List<Pair<String, List<QuoteEntity>>> {
        val files = seedDir.listFiles { _, name ->
            name.startsWith("quotes_") && name.endsWith(".json")
        }?.sortedBy { it.name } ?: emptyList()
        if (files.isEmpty()) fail("No quotes_*.json files found in seed directory.")
        return files.map { it.name to json.decodeFromString<List<QuoteEntity>>(it.readText()) }
    }

    private fun loadSpeeches(): List<SpeechEntity> {
        val file = File(seedDir, "speeches.json")
        if (!file.exists()) fail("speeches.json not found at ${file.absolutePath}")
        return json.decodeFromString<List<SpeechEntity>>(file.readText())
    }

    private fun loadDictionaries(): List<DictionaryEntity> {
        val file = File(seedDir, "dictionaries.json")
        if (!file.exists()) fail("dictionaries.json not found at ${file.absolutePath}")
        return json.decodeFromString<List<DictionaryEntity>>(file.readText())
    }

    companion object {
        /**
         * Orators whose every word.speech resolves to a shipped speech via
         * [findMatchingSpeech]. Expand this set as more catalogues get linked;
         * never shrink it without fixing the underlying refs.
         *
         * 1 Demosthenes, 3 Pericles, 5 Lincoln, 7 Bryan, 8 Churchill, 11 FDR,
         * 18 Shakespeare, 27 Uncle Ben, 42 Henry, 50 Chief Joseph, 51 Atticus,
         * 54 Elizabeth I.
         */
        private val FULLY_LINKED_ORATOR_IDS = setOf(1L, 3L, 5L, 7L, 8L, 11L, 18L, 27L, 42L, 50L, 51L, 54L)

        /** Narrowest browse surface is one orator; keep every catalogue above quiz minimums. */
        private const val MIN_WORDS_PER_ORATOR = 16

        /** Per-theme floors so single-theme filters stay rich (arts/tech are small by nature). */
        private val MIN_WORDS_PER_THEME = mapOf(
            "inspirational" to 400,
            "tech" to 80,
            "humanities" to 450,
            "arts" to 90,
            "leadership" to 320,
            "democracy" to 220,
            "courage" to 360,
            "legacy" to 360,
        )
    }
}
