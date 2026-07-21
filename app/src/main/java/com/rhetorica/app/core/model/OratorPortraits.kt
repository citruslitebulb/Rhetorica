package com.rhetorica.app.core.model

import android.content.Context
import androidx.annotation.DrawableRes

/**
 * Local orator portrait library.
 *
 * Drop assets at `res/drawable-nodpi/orator_<slug>.webp` (or .png).
 * Lookup is by orator id → slug → drawable resource name `orator_<slug>`.
 * Missing files fall back to a monogram in the UI.
 */
object OratorPortraits {

    /** Stable slug per dictionary id — keep in sync with `dictionaries.json`. */
    private val slugById: Map<Long, String> = mapOf(
        1L to "demosthenes",
        2L to "cicero",
        3L to "pericles",
        4L to "isocrates",
        5L to "abraham_lincoln",
        6L to "frederick_douglass",
        7L to "william_jennings_bryan",
        8L to "winston_churchill",
        9L to "martin_luther_king_jr",
        10L to "john_f_kennedy",
        11L to "franklin_d_roosevelt",
        12L to "nelson_mandela",
        13L to "mahatma_gandhi",
        14L to "margaret_thatcher",
        15L to "barack_obama",
        16L to "maya_angelou",
        17L to "malala_yousafzai",
        18L to "william_shakespeare",
        19L to "steve_jobs",
        20L to "bill_gates",
        21L to "jeff_bezos",
        22L to "elon_musk",
        23L to "mark_zuckerberg",
        24L to "larry_page",
        25L to "sergey_brin",
        26L to "larry_ellison",
        27L to "uncle_ben",
        28L to "marcus_aurelius",
        29L to "gandalf",
        30L to "yoda",
        31L to "rocky_balboa",
        32L to "mr_miyagi",
        33L to "rudy_ruettiger",
        34L to "herb_brooks",
        35L to "herman_boone",
        36L to "norman_dale",
        37L to "james_j_braddock",
        38L to "tommy_conlon",
        39L to "theodore_roosevelt",
        40L to "ronald_reagan",
        41L to "eleanor_roosevelt",
        42L to "patrick_henry",
        43L to "ruth_bader_ginsburg",
                44L to "john_lewis",
        45L to "sojourner_truth",
        46L to "susan_b_anthony",
        47L to "benjamin_franklin",
        48L to "malcolm_x",
        49L to "desmond_tutu",
        50L to "chief_joseph",
        51L to "atticus_finch",
        52L to "albus_dumbledore",
        53L to "oprah_winfrey",
        54L to "elizabeth_i",
    )

    fun slugFor(oratorId: Long?): String? =
        oratorId?.let { slugById[it] }

    fun allSlugs(): Map<Long, String> = slugById

    /**
     * @return drawable resource id, or 0 if no asset is packaged yet.
     */
    @DrawableRes
    fun drawableRes(context: Context, oratorId: Long?): Int {
        val slug = slugFor(oratorId) ?: return 0
        return context.resources.getIdentifier(
            "orator_$slug",
            "drawable",
            context.packageName,
        )
    }

    /** Initials for monogram fallback (e.g. "MLK", "AL"). */
    fun monogram(oratorName: String?): String {
        if (oratorName.isNullOrBlank()) return "?"
        val parts = oratorName
            .replace(".", " ")
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() && !it.equals("jr", ignoreCase = true) && !it.equals("mr", ignoreCase = true) }
        return when {
            parts.size >= 3 -> parts.take(3).joinToString("") { it.first().uppercaseChar().toString() }
            parts.size == 2 -> parts.joinToString("") { it.first().uppercaseChar().toString() }
            else -> parts.first().take(2).uppercase()
        }
    }
}
