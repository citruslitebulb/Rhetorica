package com.rhetorica.app.core.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/** Icons drawn for Rhetorica rather than taken from Material. */
object RhetoricaIcons {

    /**
     * Doric column matching the launcher icon's silhouette: abacus, capital,
     * fluted shaft, base and plinth. The launcher's Ionic volutes are dropped
     * because they collapse into noise at the 24dp nav-bar size.
     */
    val Column: ImageVector by lazy {
        ImageVector.Builder(
            name = "RhetoricaColumn",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        )
            .stroke { moveTo(5.4f, 5.2f); horizontalLineTo(18.6f) }
            .stroke { moveTo(7.6f, 7.3f); horizontalLineTo(16.4f) }
            .stroke { moveTo(9f, 7.3f); verticalLineTo(16.7f) }
            .stroke { moveTo(12f, 7.3f); verticalLineTo(16.7f) }
            .stroke { moveTo(15f, 7.3f); verticalLineTo(16.7f) }
            .stroke { moveTo(7.6f, 16.7f); horizontalLineTo(16.4f) }
            .stroke { moveTo(5.4f, 18.8f); horizontalLineTo(18.6f) }
            .build()
    }
}

/**
 * Stroked subpath. The colour is a placeholder — [androidx.compose.material3.Icon]
 * tints the whole vector, strokes included.
 */
private fun ImageVector.Builder.stroke(block: PathBuilder.() -> Unit): ImageVector.Builder =
    path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.7f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
        pathBuilder = block,
    )
