package com.rhetorica.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.rhetorica.app.R

/**
 * Playfair Display (SIL OFL 1.1, see assets/licenses) for headwords, screen titles,
 * and the widget word. Body and label text stay on the system sans so long
 * definitions remain easy to read at small sizes.
 *
 * The bundled files are variable-weight; [Font] derives the `wght` axis from
 * [FontWeight] on API 26+, which covers our minSdk.
 */
val RhetoricaSerif: FontFamily = FontFamily(
    Font(R.font.playfair_display, FontWeight.Normal),
    Font(R.font.playfair_display, FontWeight.Medium),
    Font(R.font.playfair_display, FontWeight.SemiBold),
    Font(R.font.playfair_display, FontWeight.Bold),
    Font(R.font.playfair_display, FontWeight.Black),
    Font(R.font.playfair_display_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.playfair_display_italic, FontWeight.SemiBold, FontStyle.Italic),
    Font(R.font.playfair_display_italic, FontWeight.Bold, FontStyle.Italic),
)

private val Defaults = Typography()

val Typography = Typography(
    displayLarge = Defaults.displayLarge.copy(fontFamily = RhetoricaSerif, letterSpacing = (-0.5).sp),
    displayMedium = Defaults.displayMedium.copy(fontFamily = RhetoricaSerif, letterSpacing = (-0.25).sp),
    displaySmall = Defaults.displaySmall.copy(fontFamily = RhetoricaSerif),
    headlineLarge = Defaults.headlineLarge.copy(fontFamily = RhetoricaSerif),
    headlineMedium = Defaults.headlineMedium.copy(fontFamily = RhetoricaSerif),
    headlineSmall = Defaults.headlineSmall.copy(fontFamily = RhetoricaSerif),
    titleLarge = Defaults.titleLarge.copy(fontFamily = RhetoricaSerif),
    titleMedium = Defaults.titleMedium,
    titleSmall = Defaults.titleSmall,
    bodyLarge = Defaults.bodyLarge,
    bodyMedium = Defaults.bodyMedium,
    bodySmall = Defaults.bodySmall,
    labelLarge = Defaults.labelLarge,
    labelMedium = Defaults.labelMedium,
    labelSmall = Defaults.labelSmall,
)
