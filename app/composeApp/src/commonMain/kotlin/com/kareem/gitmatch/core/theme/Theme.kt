package com.kareem.gitmatch.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = Emerald,
    onPrimary = ZincTextPrimary,
    background = ZincBackground,
    onBackground = ZincTextPrimary,
    surface = ZincSurface,
    onSurface = ZincTextPrimary,
    surfaceVariant = ZincBorder,
    onSurfaceVariant = ZincText,
    secondary = Amber,
    tertiary = Indigo,
    error = Rose,
    onError = ZincTextPrimary,
    outline = ZincBorder,
    outlineVariant = ZincMuted,
    primaryContainer = ZincMuted,
    onPrimaryContainer = ZincTextPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = Emerald,
    onPrimary = ZincLightSurface,
    background = ZincLightBg,
    onBackground = ZincLightText,
    surface = ZincLightSurface,
    onSurface = ZincLightText,
    surfaceVariant = ZincLightBorder,
    onSurfaceVariant = ZincLightTextSec,
    secondary = Amber,
    tertiary = Indigo,
    error = Rose,
    onError = ZincLightSurface,
    outline = ZincLightBorder,
    outlineVariant = ZincLightBorder,
    primaryContainer = Emerald.copy(alpha = 0.12f),
    onPrimaryContainer = Emerald
)

@Composable
fun GitMatchTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = GitMatchTypography,
        content = content
    )
}
