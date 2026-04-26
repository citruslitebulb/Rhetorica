// app/src/main/java/com/rhetorica/app/ui/theme/Theme.kt
package com.rhetorica.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val RhetoricaDarkScheme = darkColorScheme(
    primary = Color(0xFFD4AF37),        // Gold
    secondary = Color(0xFF8C5A2D),      // Deep burgundy
    tertiary = Color(0xFF5C2D2D),       // Rich burgundy
    background = Color(0xFF1A1A1A),
    surface = Color(0xFF121212),
    onPrimary = Color.Black,
    onBackground = Color(0xFFEAEAEA),
    primaryContainer = Color(0xFF4A3B18),
    onPrimaryContainer = Color(0xFFFFEBA4)
)

private val RhetoricaLightScheme = lightColorScheme(
    primary = Color(0xFF6B4423),        // Darker brown for better contrast with white
    secondary = Color(0xFF5C2D2D),
    tertiary = Color(0xFFD4AF37),
    background = Color(0xFFF8F4ED),     // Cream paper feel
    surface = Color.White,
    onPrimary = Color.White,
    onBackground = Color(0xFF1A1A1A),
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onSurface = Color(0xFF1A1A1A),
    primaryContainer = Color(0xFFFFEBA4),
    onPrimaryContainer = Color(0xFF2A1B00)
)

@Composable
fun RhetoricaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) RhetoricaDarkScheme else RhetoricaLightScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
