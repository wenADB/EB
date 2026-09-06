package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SleekColorScheme = darkColorScheme(
    primary = SleekEmerald,
    onPrimary = Color(0xFF0A0B0E),
    primaryContainer = SleekEmeraldContainer,
    onPrimaryContainer = SleekEmerald,
    secondary = SleekAmber,
    onSecondary = Color(0xFF0A0B0E),
    secondaryContainer = Color(0xFF332005),
    onSecondaryContainer = SleekAmber,
    tertiary = SleekBlue,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF1E293B),
    onTertiaryContainer = SleekBlue,
    background = SleekBackground,
    onBackground = SleekTextWhite,
    surface = SleekSurface,
    onSurface = SleekTextWhite,
    surfaceVariant = SleekSurfaceDark,
    onSurfaceVariant = SleekTextBody,
    outline = SleekCardBorder,
    error = SleekCrimson,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SleekColorScheme,
        typography = Typography,
        content = content
    )
}
