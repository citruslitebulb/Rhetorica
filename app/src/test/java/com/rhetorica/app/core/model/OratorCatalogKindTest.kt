package com.rhetorica.app.core.model

import com.rhetorica.app.core.model.OratorCatalogKind.Companion.filterByCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OratorCatalogKindTest {

    @Test
    fun `category strings map to catalog kinds`() {
        assertEquals(OratorCatalogKind.Historical, OratorCatalogKind.fromCategory("20th Century Legends"))
        assertEquals(OratorCatalogKind.Literary, OratorCatalogKind.fromCategory("Literary Classics"))
        assertEquals(OratorCatalogKind.Fictional, OratorCatalogKind.fromCategory("Fictional / Mythic"))
        assertEquals(OratorCatalogKind.Fictional, OratorCatalogKind.fromCategory("Fictional / Modern Inspirational"))
    }

    @Test
    fun `fictional voices are hidden by default`() {
        val orators = listOf(
            profile(1, "Lincoln", "19th Century Powerhouses"),
            profile(2, "Shakespeare", "Literary Classics"),
            profile(3, "Yoda", "Fictional / Mythic"),
        )
        val visible = orators.filterByCatalog(includeLiterary = true, includeFictional = false)
        assertEquals(listOf(1L, 2L), visible.map { it.id })
        assertTrue(orators.filterByCatalog(includeLiterary = false, includeFictional = false).none { it.id == 2L })
    }

    @Test
    fun `isVisible keeps historical voices and honors catalog toggles`() {
        assertTrue(OratorCatalogKind.isVisible("19th Century Powerhouses", includeLiterary = false, includeFictional = false))
        assertTrue(OratorCatalogKind.isVisible("Literary Classics", includeLiterary = true, includeFictional = false))
        assertTrue(!OratorCatalogKind.isVisible("Literary Classics", includeLiterary = false, includeFictional = false))
        assertTrue(!OratorCatalogKind.isVisible("Fictional / Mythic", includeLiterary = true, includeFictional = false))
        assertTrue(OratorCatalogKind.isVisible("Fictional / Mythic", includeLiterary = true, includeFictional = true))
    }

    private fun profile(id: Long, name: String, category: String) = OratorProfile(
        id = id,
        name = name,
        category = category,
        era = "",
        bio = "",
        portraitUrl = "",
        primaryStyle = "",
        voiceStyle = "",
        colorAccent = 0L,
        sampleSpeech = "",
        tags = emptyList(),
    )
}
