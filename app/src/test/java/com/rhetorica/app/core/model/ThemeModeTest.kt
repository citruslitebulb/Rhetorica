package com.rhetorica.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeModeTest {
    @Test
    fun `unknown storage values fall back to dark`() {
        assertEquals(ThemeMode.Dark, ThemeMode.fromStorage(null))
        assertEquals(ThemeMode.Dark, ThemeMode.fromStorage("nope"))
        assertEquals(ThemeMode.Dark, ThemeMode.fromStorage("dark"))
        assertEquals(ThemeMode.Light, ThemeMode.fromStorage("light"))
        assertEquals(ThemeMode.System, ThemeMode.fromStorage("system"))
    }
}
