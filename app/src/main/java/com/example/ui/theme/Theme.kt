package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = Redline,
    onPrimary = SurfaceBlack,
    secondary = RedlineDark,
    onSecondary = OnDark,
    background = SurfaceDarker,
    onBackground = OnDark,
    surface = SurfaceDark,
    onSurface = OnDark,
    surfaceVariant = SurfaceDarker,
    onSurfaceVariant = OnDarkVariant,
    outline = OutlineDark
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force dark theme for the cybernetic SNOW-X aesthetic
    dynamicColor: Boolean = false, // Disable dynamic content colors to retain the signature "Redline" accent
    content: @Composable () -> Unit
) {
    // We strictly use our custom DarkColorScheme to match the professional brand design
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
