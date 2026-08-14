package com.rhetorica.app.widget

import org.junit.Assert.assertEquals
import org.junit.Test

class BitmapSamplingTest {

    @Test
    fun `small source stays at sample 1`() {
        assertEquals(1, BitmapSampling.inSampleSize(200, 100, 400, 200))
    }

    @Test
    fun `large photo is power-of-two subsampled`() {
        assertEquals(8, BitmapSampling.inSampleSize(4032, 3024, 400, 300))
    }

    @Test
    fun `invalid source size is sample 1`() {
        assertEquals(1, BitmapSampling.inSampleSize(0, 0, 100, 100))
    }
}
