package com.rhetorica.app.core.model

import java.time.LocalDate
import java.time.ZoneId

object HabitProgress {
    fun isTodaysWordOpened(
        todaysWotdId: Long?,
        todaysWotdDate: String,
        isOpened: Boolean,
        today: String = LocalDate.now(ZoneId.systemDefault()).toString(),
    ): Boolean {
        return todaysWotdId != null && todaysWotdDate == today && isOpened
    }
}
