package com.example.gymlogapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkBackground,
    primaryContainer = DarkSurface,
    onPrimaryContainer = DarkPrimary,
    inversePrimary = LightPrimary,
    secondary = DarkSecondary,
    onSecondary = DarkBackground,
    secondaryContainer = DarkSurface,
    onSecondaryContainer = DarkSecondary,
    tertiary = DarkSecondary,
    onTertiary = DarkBackground,
    tertiaryContainer = DarkSurface,
    onTertiaryContainer = DarkSecondary,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurface,
    onSurfaceVariant = DarkTextSecondary,
    surfaceTint = DarkSurface,
    inverseSurface = LightSurface,
    inverseOnSurface = LightTextPrimary,
    outline = DarkBorder,
    outlineVariant = DarkBorder,
    scrim = DarkBackground,
    surfaceBright = DarkSurface,
    surfaceDim = DarkBackground,
    surfaceContainerLowest = DarkBackground,
    surfaceContainerLow = DarkSurface,
    surfaceContainer = DarkSurface,
    surfaceContainerHigh = DarkSurface,
    surfaceContainerHighest = DarkSurface,
    error = DarkError,
    onError = DarkOnError,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightSurface,
    primaryContainer = LightSurface,
    onPrimaryContainer = LightPrimary,
    inversePrimary = DarkPrimary,
    secondary = LightSecondary,
    onSecondary = LightSurface,
    secondaryContainer = LightSurface,
    onSecondaryContainer = LightSecondary,
    tertiary = LightSecondary,
    onTertiary = LightSurface,
    tertiaryContainer = LightSurface,
    onTertiaryContainer = LightSecondary,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurface,
    onSurfaceVariant = LightTextSecondary,
    surfaceTint = LightSurface,
    inverseSurface = DarkSurface,
    inverseOnSurface = DarkTextPrimary,
    outline = LightBorder,
    outlineVariant = LightBorder,
    scrim = DarkBackground,
    surfaceBright = LightSurface,
    surfaceDim = LightBackground,
    surfaceContainerLowest = LightSurface,
    surfaceContainerLow = LightSurface,
    surfaceContainer = LightSurface,
    surfaceContainerHigh = LightSurface,
    surfaceContainerHighest = LightSurface,
    error = LightError,
    onError = LightOnError,
    errorContainer = LightErrorContainer,
    onErrorContainer = LightOnErrorContainer
)

@Composable
fun GymLogAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Keep the GymLog palette even on devices with dynamic system colors.
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
