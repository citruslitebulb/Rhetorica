package com.rhetorica.app.widget

import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class WidgetRefreshSchedulerTest {

    private val zone = ZoneId.of("America/Los_Angeles")

    @Test
    fun `next refresh lands two minutes after local midnight`() {
        val now = ZonedDateTime.of(2026, 9, 11, 22, 0, 0, 0, zone)
        val delay = WidgetRefreshScheduler.millisUntilNextMidnight(now)
        assertEquals((2 * 60 + 2) * 60 * 1000L, delay)
    }

    @Test
    fun `just after midnight schedules for the following night`() {
        val now = ZonedDateTime.of(2026, 9, 12, 0, 1, 0, 0, zone)
        val delay = WidgetRefreshScheduler.millisUntilNextMidnight(now)
        // 23h59m to midnight + 2m grace
        assertEquals((24 * 60 + 1) * 60 * 1000L, delay)
    }

    @Test
    fun `handles a daylight saving transition day`() {
        // 2026-11-01 02:00 PDT -> 01:00 PST: the local day is 25 hours long.
        val now = ZonedDateTime.of(2026, 11, 1, 0, 0, 0, 0, zone)
        val delay = WidgetRefreshScheduler.millisUntilNextMidnight(now)
        assertEquals((25 * 60 + 2) * 60 * 1000L, delay)
    }
}
