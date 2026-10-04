package com.rhetorica.app.widget

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WidgetLayoutPolicyTest {

    @Test
    fun `smallest size keeps the word readable and hides the definition`() {
        val layout = WidgetLayoutPolicy.resolve(widthDp = 110, heightDp = 40)

        assertEquals(18f, layout.wordTextSp)
        assertEquals(1, layout.wordMaxLines)
        assertFalse(layout.showDefinition)
        assertFalse(layout.showOrator)
        assertFalse(layout.showBadge)
        assertFalse(layout.showExample)
    }

    @Test
    fun `definition appears once a second line fits`() {
        val layout = WidgetLayoutPolicy.resolve(widthDp = 180, heightDp = 72)

        assertTrue(layout.showDefinition)
        assertEquals(1, layout.definitionMaxLines)
        assertFalse(layout.showOrator)
        assertFalse(layout.showExample)
    }

    @Test
    fun `slightly larger size shows a short definition and the orator`() {
        val layout = WidgetLayoutPolicy.resolve(widthDp = 250, heightDp = 110)

        assertTrue(layout.showDefinition)
        assertEquals(1, layout.definitionMaxLines)
        assertTrue(layout.showOrator)
        assertFalse(layout.showBadge)
        assertFalse(layout.showExample)
        assertFalse(layout.showBottomBar)
    }

    @Test
    fun `tall size keeps the usage example`() {
        val layout = WidgetLayoutPolicy.resolve(widthDp = 250, heightDp = 180)

        assertTrue(layout.showDefinition)
        assertTrue(layout.showOrator)
        assertTrue(layout.showExample)
        assertTrue(layout.showExampleSource)
        assertTrue(layout.showBottomBar)
        assertEquals(3, layout.definitionMaxLines)
    }

    @Test
    fun `missing host bounds fall back to the slightly larger layout`() {
        val layout = WidgetLayoutPolicy.fromHostBounds(0, 0, 0, 0)

        assertEquals(WidgetLayoutPolicy.DEFAULT_WIDTH_DP, layout.widthDp)
        assertEquals(WidgetLayoutPolicy.DEFAULT_HEIGHT_DP, layout.heightDp)
        assertTrue(layout.showDefinition)
        assertTrue(layout.showOrator)
        assertFalse(layout.showExample)
    }

    @Test
    fun `current min height wins over a larger theoretical max`() {
        val layout = WidgetLayoutPolicy.fromHostBounds(
            minWidthDp = 250,
            minHeightDp = 40,
            maxWidthDp = 400,
            maxHeightDp = 300,
        )

        assertEquals(40, layout.heightDp)
        assertFalse(layout.showDefinition)
        assertFalse(layout.showExample)
    }

    @Test
    fun `meta line joins part of speech and orator only when the orator fits`() {
        assertEquals(
            "noun · Cicero",
            WidgetLayoutPolicy.metaLine("noun", "Cicero", showOrator = true),
        )
        assertEquals(
            "Cicero",
            WidgetLayoutPolicy.metaLine(null, "Cicero", showOrator = true),
        )
        assertNull(WidgetLayoutPolicy.metaLine("noun", "Cicero", showOrator = false))
        assertNull(WidgetLayoutPolicy.metaLine("  ", "  ", showOrator = true))
    }
}
