package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = QuickDropDarkPrimary,
    onPrimary = QuickDropDarkOnPrimary,
    primaryContainer = QuickDropDarkPrimaryContainer,
    onPrimaryContainer = QuickDropDarkOnPrimaryContainer,
    secondary = QuickDropDarkSecondary,
    onSecondary = QuickDropDarkOnSecondary,
    secondaryContainer = QuickDropDarkSecondaryContainer,
    onSecondaryContainer = QuickDropDarkOnSecondaryContainer,
    tertiary = QuickDropTertiary,
    onTertiary = QuickDropOnTertiary,
    tertiaryContainer = QuickDropTertiaryContainer,
    onTertiaryContainer = QuickDropOnTertiaryContainer,
    background = QuickDropDarkSurface,
    onBackground = QuickDropDarkOnSurface,
    surface = QuickDropDarkSurface,
    onSurface = QuickDropDarkOnSurface,
    surfaceVariant = QuickDropDarkSurfaceContainerHigh,
    onSurfaceVariant = QuickDropDarkOnSurfaceVariant,
    surfaceContainerLowest = QuickDropDarkSurfaceContainerLowest,
    surfaceContainerLow = QuickDropDarkSurfaceContainerLow,
    surfaceContainer = QuickDropDarkSurfaceContainer,
    surfaceContainerHigh = QuickDropDarkSurfaceContainerHigh,
    surfaceContainerHighest = QuickDropDarkSurfaceContainerHighest,
    outlineVariant = QuickDropDarkOutlineVariant,
    error = QuickDropError,
    onError = QuickDropOnError,
    errorContainer = QuickDropErrorContainer,
    onErrorContainer = QuickDropOnErrorContainer
)

private val LightColorScheme = lightColorScheme(
    primary = QuickDropPrimary,
    onPrimary = QuickDropOnPrimary,
    primaryContainer = QuickDropPrimaryContainer,
    onPrimaryContainer = QuickDropOnPrimaryContainer,
    secondary = QuickDropSecondary,
    onSecondary = QuickDropOnSecondary,
    secondaryContainer = QuickDropSecondaryContainer,
    onSecondaryContainer = QuickDropOnSecondaryContainer,
    tertiary = QuickDropTertiary,
    onTertiary = QuickDropOnTertiary,
    tertiaryContainer = QuickDropTertiaryContainer,
    onTertiaryContainer = QuickDropOnTertiaryContainer,
    background = QuickDropSurface,
    onBackground = QuickDropOnSurface,
    surface = QuickDropSurface,
    onSurface = QuickDropOnSurface,
    surfaceVariant = QuickDropSurfaceContainerHighest,
    onSurfaceVariant = QuickDropOnSurfaceVariant,
    surfaceContainerLowest = QuickDropSurfaceContainerLowest,
    surfaceContainerLow = QuickDropSurfaceContainerLow,
    surfaceContainer = QuickDropSurfaceContainer,
    surfaceContainerHigh = QuickDropSurfaceContainerHigh,
    surfaceContainerHighest = QuickDropSurfaceContainerHighest,
    outline = QuickDropOutline,
    outlineVariant = QuickDropOutlineVariant,
    error = QuickDropError,
    onError = QuickDropOnError,
    errorContainer = QuickDropErrorContainer,
    onErrorContainer = QuickDropOnErrorContainer
)

val QuickDropShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun QuickDropTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = QuickDropTypography,
        shapes = QuickDropShapes,
        content = content
    )
}
