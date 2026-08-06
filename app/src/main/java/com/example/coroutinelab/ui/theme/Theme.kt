package com.example.coroutinelab.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF315DA8),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD9E2FF),
    onPrimaryContainer = Color(0xFF001A42),
    secondary = Color(0xFF006B5F),
    secondaryContainer = Color(0xFF9EF2E1),
    surface = Color(0xFFF8FAFF),
    surfaceVariant = Color(0xFFE1E7F0),
    error = Color(0xFFBA1A1A),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFADC6FF),
    primaryContainer = Color(0xFF16458E),
    secondary = Color(0xFF82D5C5),
    surface = Color(0xFF101318),
    surfaceVariant = Color(0xFF42474E),
)

@Composable
fun CoroutineLabTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content,
    )
}
