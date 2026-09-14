package com.rhetorica.app.core.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rhetorica.app.core.model.WordThemes

/**
 * Outlined gold theme pill used on feed cards and word detail.
 *
 * [softWrap] is off so a squeezed max-width cannot stack letters vertically;
 * [ThemeChipRow] then wraps whole chips onto the next line instead.
 */
@Composable
fun ThemeChip(
    label: String,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val shape = RoundedCornerShape(percent = 50)
    Text(
        text = label,
        style = if (compact) {
            MaterialTheme.typography.labelSmall
        } else {
            MaterialTheme.typography.labelMedium
        },
        color = MaterialTheme.colorScheme.primary,
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
            .border(1.dp, MaterialTheme.colorScheme.primary, shape)
            .padding(
                horizontal = if (compact) 8.dp else 10.dp,
                vertical = if (compact) 3.dp else 4.dp,
            ),
    )
}

/**
 * Lays theme pills out in wrapping rows so a word with many categories
 * cannot overflow the card or detail column.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ThemeChipRow(
    categories: List<String>,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    if (categories.isEmpty()) return
    val gap = if (compact) 6.dp else 8.dp
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(gap),
        verticalArrangement = Arrangement.spacedBy(gap),
    ) {
        categories.forEach { cat ->
            ThemeChip(
                label = WordThemes.displayName(cat),
                compact = compact,
            )
        }
    }
}
