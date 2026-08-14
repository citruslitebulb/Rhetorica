package com.rhetorica.app.widget

/** Power-of-two subsample so gallery photos are decoded near the widget size. */
object BitmapSampling {
    fun inSampleSize(
        srcWidth: Int,
        srcHeight: Int,
        reqWidth: Int,
        reqHeight: Int,
    ): Int {
        val reqW = reqWidth.coerceAtLeast(1)
        val reqH = reqHeight.coerceAtLeast(1)
        if (srcWidth <= 0 || srcHeight <= 0) return 1
        var sample = 1
        if (srcHeight > reqH || srcWidth > reqW) {
            val halfHeight = srcHeight / 2
            val halfWidth = srcWidth / 2
            while (halfHeight / sample >= reqH && halfWidth / sample >= reqW) {
                sample *= 2
            }
        }
        return sample
    }
}
