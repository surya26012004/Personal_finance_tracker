package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = LunarIndigo,
    onPrimary = CosmicVoid,
    primaryContainer = LunarIndigoDark,
    onPrimaryContainer = MoonStarlight,
    secondary = LunarCyan,
    onSecondary = CosmicVoid,
    secondaryContainer = LunarCyanGlow,
    onSecondaryContainer = LunarCyan,
    tertiary = LunarGold,
    onTertiary = CosmicVoid,
    background = CosmicVoid,
    onBackground = MoonStarlight,
    surface = DarkSurface,
    onSurface = MoonStarlight,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = MoonSilver,
    outline = GlassBorderSubtle,
    error = RoseLoss,
    onError = CosmicVoid
)

private val LightColorScheme = DarkColorScheme // Moonlit aesthetic is intrinsically celestial midnight & liquid glass

@Composable
fun WealthWiseTheme(
    darkTheme: Boolean = true, // Default to celestial Moonlit theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}


