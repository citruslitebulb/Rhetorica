package com.rhetorica.app.notification

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class NotificationSchedulerTest {

    private val zone = ZoneId.of("UTC")

    @Test
    fun `millisUntilNext is later today when the time has not passed`() {
        val now = ZonedDateTime.of(2026, 8, 13, 7, 0, 0, 0, zone)
        val delay = NotificationScheduler.millisUntilNext(hour = 8, minute = 0, now = now)
        assertEquals(60 * 60 * 1000L, delay)
    }

    @Test
    fun `millisUntilNext rolls to tomorrow when the time has passed`() {
        val now = ZonedDateTime.of(2026, 8, 13, 9, 0, 0, 0, zone)
        val delay = NotificationScheduler.millisUntilNext(hour = 8, minute = 0, now = now)
        assertEquals(23 * 60 * 60 * 1000L, delay)
    }

    @Test
    fun `schedule signature includes enabled flag and clock time`() {
        assertEquals("v2:true:8:30", NotificationScheduler.scheduleSignature(true, 8, 30))
        assertEquals("v2:false:8:30", NotificationScheduler.scheduleSignature(false, 8, 30))
        assertTrue(
            NotificationScheduler.scheduleSignature(true, 8, 0) !=
                NotificationScheduler.scheduleSignature(true, 9, 0),
        )
    }
}
