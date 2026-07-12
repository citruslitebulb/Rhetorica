package com.rhetorica.app.core.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rhetorica.app.R
import com.rhetorica.app.core.model.OratorPortraits

/**
 * Circular orator portrait from the local image library, with monogram fallback.
 */
@Composable
fun OratorPortrait(
    oratorId: Long?,
    oratorName: String?,
    modifier: Modifier = Modifier,
    size: Dp = 160.dp,
) {
    val context = LocalContext.current
    val resId = remember(oratorId) { OratorPortraits.drawableRes(context, oratorId) }
    val gold = Color(0xFFD4AF37)
    val goldMuted = Color(0xFFB8973A)
    val ink = Color(0xFF1C2433)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .border(width = 3.dp, color = gold, shape = CircleShape)
                .background(ink),
            contentAlignment = Alignment.Center,
        ) {
            if (resId != 0) {
                Image(
                    painter = painterResource(resId),
                    contentDescription = oratorName?.let {
                        stringResource(R.string.orator_portrait_cd, it)
                    },
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            } else {
                MonogramFallback(
                    name = oratorName,
                    gold = gold,
                    goldMuted = goldMuted,
                    ink = ink,
                )
            }
        }

        if (!oratorName.isNullOrBlank()) {
            Text(
                text = oratorName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
    }
}

@Composable
private fun MonogramFallback(
    name: String?,
    gold: Color,
    goldMuted: Color,
    ink: Color,
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
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = gold,
        )
    }
}
