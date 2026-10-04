package com.rhetorica.app.widget

/**
 * Decides which Word of the Day fields fit in the host-reported widget size.
 *
 * Priority, from the smallest cell upward: the word, then a short definition,
 * then the orator. Taller placements also keep the usage example.
 *
 * Launchers sometimes report a theoretical max that is larger than the cell
 * the user actually placed. Content follows the current minimum bounds.
 */
object WidgetLayoutPolicy {
    const val DEFAULT_WIDTH_DP = 250
    const val DEFAULT_HEIGHT_DP = 110

    /** Below this height only the headword fits. */
    const val DEFINITION_MIN_HEIGHT_DP = 64

    /** Around two cells tall: the orator line fits under a short definition. */
    const val ORATOR_MIN_HEIGHT_DP = 100

    /** Taller than the default placement: usage example. */
    const val EXAMPLE_MIN_HEIGHT_DP = 150

    const val TALL_HEIGHT_DP = 180

    const val BADGE_MIN_HEIGHT_DP = 130

    fun fromHostBounds(
        minWidthDp: Int,
        minHeightDp: Int,
        maxWidthDp: Int,
        maxHeightDp: Int,
    ): WidgetLayoutSize {
        val heightDp = when {
            minHeightDp > 0 -> minHeightDp
            maxHeightDp > 0 -> maxHeightDp
            else -> DEFAULT_HEIGHT_DP
        }
        val widthDp = when {
            minWidthDp > 0 -> minWidthDp
            maxWidthDp > 0 -> maxWidthDp
            else -> DEFAULT_WIDTH_DP
        }
        return resolve(widthDp, heightDp)
    }

    fun resolve(widthDp: Int, heightDp: Int): WidgetLayoutSize {
        val showDefinition = heightDp >= DEFINITION_MIN_HEIGHT_DP
        val showOrator = heightDp >= ORATOR_MIN_HEIGHT_DP
        val showExample = heightDp >= EXAMPLE_MIN_HEIGHT_DP
        val tall = heightDp >= TALL_HEIGHT_DP
        val compact = !showDefinition

        val definitionMaxLines = when {
            !showDefinition -> 0
            showOrator && heightDp < BADGE_MIN_HEIGHT_DP -> 1
            heightDp < 80 -> 1
            heightDp < EXAMPLE_MIN_HEIGHT_DP -> 2
            else -> 3
        }

        return WidgetLayoutSize(
            widthDp = widthDp,
            heightDp = heightDp,
            showBadge = heightDp >= BADGE_MIN_HEIGHT_DP && widthDp >= 180,
            showDefinition = showDefinition,
            definitionMaxLines = definitionMaxLines,
            showOrator = showOrator,
            showExample = showExample,
            exampleMaxLines = when {
                !showExample -> 0
                tall -> 4
                else -> 2
            },
            exampleMaxChars = when {
                !showExample -> 0
                tall -> 220
                else -> 120
            },
            showExampleSource = tall,
            showBottomBar = tall,
            wordTextSp = when {
                compact -> 18f
                heightDp < 120 -> 22f
                else -> 26f
            },
            wordMaxLines = if (compact || heightDp < 90) 1 else 2,
            definitionTextSp = if (heightDp < 120) 12f else 13f,
            rootPaddingDp = if (compact) 2 else 4,
            contentPaddingDp = when {
                compact -> 4
                heightDp < EXAMPLE_MIN_HEIGHT_DP -> 8
                else -> 12
            },
        )
    }

    /**
     * Home-screen meta line, matching the in-app card: part of speech, then orator.
     * The orator is omitted when the cell is too short to keep the word readable.
     */
    fun metaLine(partOfSpeech: String?, oratorName: String?, showOrator: Boolean): String? {
        if (!showOrator) return null
        val pos = partOfSpeech?.trim()?.takeIf { it.isNotEmpty() }
        val orator = oratorName?.trim()?.takeIf { it.isNotEmpty() }
        val line = listOfNotNull(pos, orator).joinToString(" · ")
        return line.ifEmpty { null }
    }
}

data class WidgetLayoutSize(
    val widthDp: Int,
    val heightDp: Int,
    val showBadge: Boolean,
    val showDefinition: Boolean,
    val definitionMaxLines: Int,
    val showOrator: Boolean,
    val showExample: Boolean,
    val exampleMaxLines: Int,
    val exampleMaxChars: Int,
    val showExampleSource: Boolean,
    val showBottomBar: Boolean,
    val wordTextSp: Float,
    val wordMaxLines: Int,
    val definitionTextSp: Float,
    val rootPaddingDp: Int,
    val contentPaddingDp: Int,
)
