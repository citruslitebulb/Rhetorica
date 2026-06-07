package com.rhetorica.app.data.seed

import com.rhetorica.app.data.local.WordEntity
import kotlinx.serialization.json.Json
import org.junit.Assert.fail
import org.junit.Test
import java.io.File

/**
 * Unit test to enforce data quality invariants on the bundled word seed data.
 *
 * This prevents regressions like examples that do not actually contain the target word
 * (a problem that previously affected many speech-attributed entries).
 */
class WordExampleValidationTest {

    private val json = Json { ignoreUnknownKeys = true }

    private val seedDir = File("app/src/main/assets/data/seed")

    @Test
    fun `every word example must contain the word itself`() {
        if (!seedDir.exists()) {
            fail("Seed directory not found at ${seedDir.absolutePath}. " +
                 "Run tests from the project root (e.g. ./gradlew :app:test).")
            return
        }

        val wordFiles = seedDir.listFiles { _, name ->
            name.startsWith("words_") && name.endsWith(".json")
        }?.map { it.name }?.sorted() ?: emptyList()

        if (wordFiles.isEmpty()) {
            fail("No words_*.json files found in seed directory.")
            return
        }

        val failures = mutableListOf<String>()

        wordFiles.forEach { fileName ->
            val file = File(seedDir, fileName)
            val content = file.readText()
            val words = try {
                json.decodeFromString<List<WordEntity>>(content)
            } catch (e: Exception) {
                failures.add("$fileName: Failed to parse JSON - ${e.message}")
                return@forEach
            }

            words.forEach { entry ->
                val word = entry.word
                val example = entry.example ?: ""

                if (word.isBlank()) {
                    failures.add("$fileName: id=${entry.id} has blank word")
                    return@forEach
                }
                if (example.isBlank()) {
                    failures.add("$fileName: word='$word' has blank example")
                    return@forEach
                }

                val lowerWord = word.lowercase()
                val lowerExample = example.lowercase()

                // Require the word (or common inflections) to appear in the example.
                // This ensures the example actually demonstrates usage of the vocabulary word.
                val containsWord = lowerExample.contains(lowerWord) ||
                                   lowerExample.contains(lowerWord + "s") ||
                                   lowerExample.contains(lowerWord + "d") ||
                                   lowerExample.contains(lowerWord + "ing") ||
                                   lowerExample.contains(lowerWord + "ed")

                if (!containsWord) {
                    val shortExample = if (example.length > 90) example.take(87) + "..." else example
                    val attribution = buildString {
                        if (entry.speech != null) append(entry.speech)
                        if (entry.source != null && entry.source != entry.speech) {
                            if (isNotEmpty()) append(" / ")
                            append(entry.source)
                        }
                    }.ifBlank { "n/a" }

                    failures.add(
                        "$fileName: word='$word' (id=${entry.id})\n" +
                        "  Example does not contain the word: \"$shortExample\"\n" +
                        "  Attribution: $attribution"
                    )
                }
            }
        }

        if (failures.isNotEmpty()) {
            val summary = buildString {
                appendLine("Found ${failures.size} word example(s) that do not contain their target word.")
                appendLine("This breaks the expectation that the example demonstrates the vocabulary word in context.")
                appendLine()
                failures.forEachIndexed { index, msg ->
                    appendLine("${index + 1}. $msg")
                    appendLine()
                }
            }
            fail(summary)
        }
    }
}
