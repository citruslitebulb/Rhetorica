package com.rhetorica.app.feature.home

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class IdiomLabelTest {

    @Test
    fun `user facing idiom label matches todays word capitalization`() {
        val xml = File("src/main/res/values/strings.xml").readText()
        assertTrue(xml.contains("Today\\'s idiom"))
        assertTrue(xml.contains("Today\\'s word"))
        assertFalse(xml.contains("Idiom of the Day"))
    }
}
