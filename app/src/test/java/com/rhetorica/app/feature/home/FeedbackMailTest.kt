package com.rhetorica.app.feature.home

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

class FeedbackMailTest {

    @Test
    fun `blank empty and whitespace are not ready`() {
        assertFalse(FeedbackMail.hasMessage(""))
        assertFalse(FeedbackMail.hasMessage("   "))
        assertFalse(FeedbackMail.hasMessage("\n\t"))
    }

    @Test
    fun `typed text is ready`() {
        assertTrue(FeedbackMail.hasMessage("The widget is too dim."))
        assertTrue(FeedbackMail.hasMessage("  still counts  "))
    }

    @Test
    fun `payload contains only the typed body plus address and subject`() {
        val body = "The widget is too dim."
        val uri = FeedbackMail.mailtoUri(
            address = "derrick5199@gmail.com",
            subject = "Rhetorica feedback",
            body = body,
        )
        assertTrue(uri.startsWith("mailto:derrick5199@gmail.com?"))
        val decoded = URLDecoder.decode(uri, StandardCharsets.UTF_8)
        assertTrue(decoded.contains(body))
        assertTrue(decoded.contains("Rhetorica feedback"))
        assertFalse(uri.contains("version=", ignoreCase = true))
        assertFalse(uri.contains("device=", ignoreCase = true))
        assertEquals(body, queryValue(uri, "body"))
        assertEquals("Rhetorica feedback", queryValue(uri, "subject"))
    }

    @Test
    fun `special characters in the body stay in the decoded payload`() {
        val body = "Line 1 & line 2?\n100%"
        val uri = FeedbackMail.mailtoUri("a@b.com", "subj", body)
        assertEquals(body, queryValue(uri, "body"))
        assertTrue(uri.contains("%26"))
        assertTrue(uri.contains("%3F") || uri.contains("%3f"))
    }

    private fun queryValue(uri: String, key: String): String {
        val query = uri.substringAfter('?', "")
        val pair = query.split('&').first { it.startsWith("$key=") }
        return URLDecoder.decode(pair.substringAfter('='), StandardCharsets.UTF_8)
    }
}
