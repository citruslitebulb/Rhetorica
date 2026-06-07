package com.rhetorica.app.data.local

import org.junit.Assert.assertEquals
import org.junit.Test

class ConvertersTest {

    private val converters = Converters()

    @Test
    fun `fromStringList and toStringList roundtrip`() {
        val original = listOf("inspirational", "leadership", "courage")

        val json = converters.fromStringList(original)
        val restored = converters.toStringList(json)

        assertEquals(original, restored)
    }

    @Test
    fun `toStringList returns emptyList on invalid JSON`() {
        val badJson = "[not valid json"

        val result = converters.toStringList(badJson)

        assertEquals(emptyList<String>(), result)
    }

    @Test
    fun `fromLongList and toLongList roundtrip`() {
        val original = listOf(1L, 42L, 100L)

        val json = converters.fromLongList(original)
        val restored = converters.toLongList(json)

        assertEquals(original, restored)
    }

    @Test
    fun `toLongList returns emptyList on invalid JSON`() {
        val result = converters.toLongList("not-a-list")

        assertEquals(emptyList<Long>(), result)
    }

    @Test
    fun `empty lists roundtrip correctly`() {
        assertEquals(emptyList<String>(), converters.toStringList(converters.fromStringList(emptyList())))
        assertEquals(emptyList<Long>(), converters.toLongList(converters.fromLongList(emptyList())))
    }
}
