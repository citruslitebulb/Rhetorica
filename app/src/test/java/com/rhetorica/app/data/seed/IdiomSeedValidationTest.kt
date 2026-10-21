package com.rhetorica.app.data.seed

import com.rhetorica.app.data.local.DictionaryEntity
import com.rhetorica.app.data.local.IdiomEntity
import com.rhetorica.app.data.local.QuoteEntity
import kotlinx.serialization.json.Json
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.File

/**
 * Editorial checks for idioms.json: required fields, unique ids, and orator
 * links that point at a real orator and a real seed quote containing the idiom.
 */
class IdiomSeedValidationTest {
    private val json = Json { ignoreUnknownKeys = true }
    private val allowedComplexity = setOf("beginner", "basic", "intermediate", "advanced")

    private val seedDir: File = listOf(
        File("src/main/assets/data/seed"),
        File("app/src/main/assets/data/seed"),
    ).firstOrNull { it.isDirectory } ?: File("src/main/assets/data/seed")

    @Test
    fun `idioms parse with required fields and unique ids`() {
        val idioms = loadIdioms()
        assertTrue("expected about 150 idioms, found ${idioms.size}", idioms.size in 140..180)
        val failures = mutableListOf<String>()
        idioms.groupingBy { it.id }.eachCount().filterValues { it > 1 }.forEach { (id, count) ->
            failures += "id=$id appears $count times"
        }
        idioms.groupingBy { it.idiom.trim().lowercase() }.eachCount().filterValues { it > 1 }.forEach { (idiom, count) ->
            failures += "idiom '$idiom' appears $count times"
        }
        idioms.forEach { idiom ->
            if (idiom.idiom.isBlank()) failures += "id=${idiom.id} missing idiom"
            if (idiom.meaning.isBlank()) failures += "id=${idiom.id} missing meaning"
            if (idiom.example.isBlank()) failures += "id=${idiom.id} missing example"
            if (idiom.complexity !in allowedComplexity) {
                failures += "id=${idiom.id} complexity='${idiom.complexity}'"
            }
            if (idiom.origin != null && idiom.origin.isBlank()) {
                failures += "id=${idiom.id} has a blank origin"
            }
        }
        allowedComplexity.forEach { tier ->
            if (idioms.none { it.complexity == tier }) failures += "no idioms at complexity $tier"
        }
        if (failures.isNotEmpty()) fail(failures.joinToString("\n"))
    }

    @Test
    fun `orator links point at a real orator and a quote that uses the idiom`() {
        val orators = loadDictionaries().associateBy { it.id }
        val quotes = loadQuotes().associateBy { it.id }
        val failures = mutableListOf<String>()
        val linked = loadIdioms().filter { it.oratorId != null || it.quoteId != null }
        if (linked.isEmpty()) fail("expected at least one verified orator link")
        linked.forEach { idiom ->
            val oratorId = idiom.oratorId
            if (oratorId == null || oratorId !in orators) {
                failures += "'${idiom.idiom}' oratorId=$oratorId is not a seed orator"
            }
            val quoteId = idiom.quoteId
            if (quoteId == null) {
                failures += "'${idiom.idiom}' has an orator link without a quote id"
                return@forEach
            }
            val quote = quotes[quoteId]
            if (quote == null) {
                failures += "'${idiom.idiom}' quoteId=$quoteId is not in the seed"
                return@forEach
            }
            if (quote.oratorId != oratorId) {
                failures += "'${idiom.idiom}' quote ${quote.id} belongs to orator ${quote.oratorId}, not $oratorId"
            }
            if (!normalize(quote.text).contains(normalize(idiom.idiom))) {
                failures += "'${idiom.idiom}' is not in quote ${quote.id}: ${quote.text}"
            }
        }
        if (failures.isNotEmpty()) fail(failures.joinToString("\n"))
    }

    private fun normalize(value: String): String {
        return value.lowercase()
            .replace('’', '\'')
            .replace('‘', '\'')
            .replace(Regex("[^a-z0-9' ]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun loadIdioms(): List<IdiomEntity> {
        val file = File(seedDir, "idioms.json")
        if (!file.isFile) fail("idioms.json not found in $seedDir")
        return json.decodeFromString(file.readText())
    }

    private fun loadDictionaries(): List<DictionaryEntity> {
        return json.decodeFromString(File(seedDir, "dictionaries.json").readText())
    }

    private fun loadQuotes(): List<QuoteEntity> {
        val files = seedDir.listFiles { _, name ->
            name.startsWith("quotes_") && name.endsWith(".json")
        }?.sortedBy { it.name } ?: emptyList()
        if (files.isEmpty()) fail("No quotes_*.json files found")
        return files.flatMap { json.decodeFromString<List<QuoteEntity>>(it.readText()) }
    }
}
