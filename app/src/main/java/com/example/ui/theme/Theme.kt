package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = KachehriGreenPrimary,
    onPrimary = Color(0xFF00382B),
    primaryContainer = KachehriGreenDark,
    onPrimaryContainer = Color(0xFFE7FFF3),
    secondary = KachehriTealAccent,
    onSecondary = Color.White,
    background = DarkBgMain,
    onBackground = Color(0xFFE9EDEF),
    surface = DarkSurfaceHeader,
    onSurface = Color(0xFFE9EDEF),
    surfaceVariant = DarkSurfaceCard,
    onSurfaceVariant = Color(0xFF8696A0),
    outline = DarkBorder
)

private val LightColorScheme = lightColorScheme(
    primary = KachehriGreenDark,
    onPrimary = Color.White,
    primaryContainer = KachehriGreenLight,
    onPrimaryContainer = Color(0xFF00382B),
    secondary = KachehriTealAccent,
    onSecondary = Color.White,
    background = LightBgMain,
    onBackground = Color(0xFF111B21),
    surface = LightSurfaceHeader,
    onSurface = Color.White,
    surfaceVariant = LightSurfaceCard,
    onSurfaceVariant = Color(0xFF667781),
    outline = LightBorder
)

@Composable
fun KachehriTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep branded WhatsApp/Kachehri aesthetic consistent
    content: @Composable () -> Unit
) {
    MyApplicationTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep branded WhatsApp/Kachehri aesthetic consistent
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
