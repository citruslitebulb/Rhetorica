package com.rhetorica.app.data.seed

import com.rhetorica.app.core.model.OnboardingAnswers
import com.rhetorica.app.core.model.OratorProfile
import com.rhetorica.app.core.model.OratorVoiceFamily
import com.rhetorica.app.data.local.DictionaryEntity
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.File

class OratorVoiceFamilySeedTest {

    private val json = Json { ignoreUnknownKeys = true }

    private val seedDir: File = listOf(
        File("src/main/assets/data/seed"),
        File("app/src/main/assets/data/seed"),
    ).firstOrNull { it.isDirectory }
        ?: File("src/main/assets/data/seed")

    @Test
    fun `every seed orator maps to the expected voice family`() {
        val orators = loadOrators()
        val byFamily = orators.groupBy { OratorVoiceFamily.from(it) }
            .mapValues { (_, members) -> members.map { it.name }.toSet() }

        assertEquals(orators.size, byFamily.values.sumOf { it.size })

        assertEquals(
            setOf("Demosthenes", "Cicero", "Pericles", "Isocrates", "Marcus Aurelius"),
            byFamily[OratorVoiceFamily.Classical].orEmpty(),
        )
        assertEquals(
            setOf("William Shakespeare", "Elizabeth I", "Maya Angelou"),
            byFamily[OratorVoiceFamily.Literary].orEmpty(),
        )
        assertEquals(
            setOf(
                "Steve Jobs",
                "Bill Gates",
                "Jeff Bezos",
                "Elon Musk",
                "Mark Zuckerberg",
                "Larry Page",
                "Sergey Brin",
                "Larry Ellison",
            ),
            byFamily[OratorVoiceFamily.Technology].orEmpty(),
        )
        assertEquals(
            setOf(
                "Uncle Ben",
                "Gandalf",
                "Yoda",
                "Rocky Balboa",
                "Mr. Miyagi",
                "Rudy Ruettiger",
                "Herb Brooks",
                "Herman Boone",
                "Norman Dale",
                "James J. Braddock",
                "Tommy Conlon",
                "Atticus Finch",
                "Albus Dumbledore",
            ),
            byFamily[OratorVoiceFamily.Fictional].orEmpty(),
        )
        assertEquals(
            setOf(
                "Frederick Douglass",
                "Martin Luther King Jr.",
                "Nelson Mandela",
                "Mahatma Gandhi",
                "Malala Yousafzai",
                "Eleanor Roosevelt",
                "Ruth Bader Ginsburg",
                "John Lewis",
                "Sojourner Truth",
                "Susan B. Anthony",
                "Malcolm X",
                "Desmond Tutu",
            ),
            byFamily[OratorVoiceFamily.Justice].orEmpty(),
        )
        assertEquals(
            setOf(
                "Abraham Lincoln",
                "William Jennings Bryan",
                "Winston Churchill",
                "John F. Kennedy",
                "Franklin D. Roosevelt",
                "Margaret Thatcher",
                "Barack Obama",
                "Theodore Roosevelt",
                "Ronald Reagan",
                "Patrick Henry",
                "Benjamin Franklin",
                "Chief Joseph",
                "Oprah Winfrey",
            ),
            byFamily[OratorVoiceFamily.Statesmen].orEmpty(),
        )
    }

    @Test
    fun `default onboarding answers do not include every orator`() {
        val orators = loadOrators()
        val patch = OnboardingAnswers.resolve(
            orators = orators,
            families = OratorVoiceFamily.defaultSelected,
            themes = emptySet(),
            selectedOratorIds = emptySet(),
        )
        val names = orators.filter { it.id in patch.favoriteOratorIds.toSet() }.map { it.name }.toSet()

        assertEquals(
            setOf("Demosthenes", "Cicero", "Pericles", "Isocrates", "Marcus Aurelius"),
            names,
        )
        assertTrue(patch.rotateThroughAll)
        assertFalse(patch.includeLiteraryOrators)
        assertFalse(patch.includeFictionalOrators)
        assertTrue("Elon Musk" !in names)
        assertTrue("Yoda" !in names)
        assertTrue("William Shakespeare" !in names)
        assertTrue(patch.favoriteOratorIds.size < orators.size)
    }

    private fun loadOrators(): List<OratorProfile> {
        if (!seedDir.exists()) {
            fail("Seed directory not found at ${seedDir.absolutePath}")
        }
        val dictFile = File(seedDir, "dictionaries.json")
        if (!dictFile.exists()) {
            fail("dictionaries.json not found at ${dictFile.absolutePath}")
        }
        return json.decodeFromString<List<DictionaryEntity>>(dictFile.readText()).map { entity ->
            OratorProfile(
                id = entity.id,
                name = entity.name,
                category = entity.category,
                era = entity.era,
                bio = entity.bio,
                portraitUrl = entity.portraitUrl,
                primaryStyle = entity.primaryStyle,
                voiceStyle = entity.voiceStyle,
                colorAccent = entity.colorAccent,
                sampleSpeech = entity.sampleSpeech,
                tags = entity.tags,
                themeCategories = entity.themeCategories,
                isActive = entity.isActive,
            )
        }
    }
}
