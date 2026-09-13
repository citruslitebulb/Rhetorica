package com.rhetorica.app.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rhetorica.app.R
import com.rhetorica.app.core.model.OratorPortraits
import com.rhetorica.app.ui.theme.RhetoricaGold

/**
 * Circular orator logo using initials. The full name is shown beside the logo
 * on Profile, so this composable does not repeat it underneath.
 */
@Composable
fun OratorPortrait(
    oratorName: String?,
    modifier: Modifier = Modifier,
    size: Dp = 160.dp,
) {
    val gold = RhetoricaGold
    val goldMuted = Color(0xFFB8973A)
    val ink = Color(0xFF1C2433)
    val description = oratorName?.let {
        stringResource(R.string.orator_initials_cd, it)
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .border(width = 3.dp, color = gold, shape = CircleShape)
            .background(ink)
            // Hide the raw initials from accessibility services; announce the
            // orator instead (or nothing when the name is unknown).
            .clearAndSetSemantics {
                if (description != null) contentDescription = description
            },
        contentAlignment = Alignment.Center,
    ) {
        MonogramFallback(
            name = oratorName,
            gold = gold,
            goldMuted = goldMuted,
            ink = ink,
            compact = size < 96.dp,
        )
    }
}

@Composable
private fun MonogramFallback(
    name: String?,
    gold: Color,
    goldMuted: Color,
    ink: Color,
    compact: Boolean,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(ink.copy(alpha = 0.2f), ink),
                ),
            )
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        gold.copy(alpha = 0.22f),
                        Color.Transparent,
                        goldMuted.copy(alpha = 0.12f),
                    ),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = OratorPortraits.monogram(name),
            style = if (compact) {
                MaterialTheme.typography.titleLarge.copy(
                    fontSize = 18.sp,
                    letterSpacing = 0.4.sp,
                )
            } else {
                MaterialTheme.typography.headlineMedium
            },
            fontWeight = FontWeight.Bold,
            color = gold,
        )
    }
}
