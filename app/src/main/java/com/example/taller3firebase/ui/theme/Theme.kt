package com.example.taller3firebase.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF2457D6),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCE2FF),
    secondary = Color(0xFF006A65),
    background = Color(0xFFF9F9FF),
    surface = Color(0xFFF9F9FF),
    error = Color(0xFFBA1A1A),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFB7C4FF),
    onPrimary = Color(0xFF002A78),
    primaryContainer = Color(0xFF003EA8),
    secondary = Color(0xFF4EDAD1),
    background = Color(0xFF111318),
    surface = Color(0xFF111318),
    error = Color(0xFFFFB4AB),
)

@Composable
fun Taller3Theme(
    modifier: Modifier = Modifier,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = {
            Surface(modifier = modifier, content = content)
        },
    )
}

