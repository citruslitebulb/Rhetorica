package com.rhetorica.app.core.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Outlined gold theme pill used on feed cards and word detail.
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
        modifier = modifier
            .border(1.dp, MaterialTheme.colorScheme.primary, shape)
            .padding(
                horizontal = if (compact) 8.dp else 10.dp,
                vertical = if (compact) 3.dp else 4.dp,
            ),
    )
}
