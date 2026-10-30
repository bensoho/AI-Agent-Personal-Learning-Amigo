package com.agentlearning.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = Color(0xFF4F46E5),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E7FF),
    onPrimaryContainer = Color(0xFF1E1B6B),
    secondary = Color(0xFF0D9488),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCCFBF1),
    onSecondaryContainer = Color(0xFF064E48),
    tertiary = Color(0xFFD97706),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFEF3C7),
    onTertiaryContainer = Color(0xFF5C3300),
    background = Color(0xFFF6F7FB),
    onBackground = Color(0xFF1B1E2E),
    surface = Color(0xFFF6F7FB),
    onSurface = Color(0xFF1B1E2E),
    surfaceVariant = Color(0xFFE9ECF5),
    onSurfaceVariant = Color(0xFF4B5066),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color.White,
    surfaceContainer = Color(0xFFFFFFFF),
    surfaceContainerHigh = Color(0xFFF0F2F9),
    surfaceContainerHighest = Color(0xFFE9ECF5),
    outline = Color(0xFF8A90A8),
    outlineVariant = Color(0xFFDDE1EE),
    error = Color(0xFFDC2626),
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF7F1D1D),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFA5B4FC),
    onPrimary = Color(0xFF1E1B4B),
    primaryContainer = Color(0xFF3730A3),
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = Color(0xFF5EEAD4),
    onSecondary = Color(0xFF042F2E),
    secondaryContainer = Color(0xFF115E59),
    onSecondaryContainer = Color(0xFFCCFBF1),
    tertiary = Color(0xFFFCD34D),
    onTertiary = Color(0xFF451A03),
    tertiaryContainer = Color(0xFF78350F),
    onTertiaryContainer = Color(0xFFFEF3C7),
    background = Color(0xFF0F1220),
    onBackground = Color(0xFFE6E8F4),
    surface = Color(0xFF0F1220),
    onSurface = Color(0xFFE6E8F4),
    surfaceVariant = Color(0xFF252A40),
    onSurfaceVariant = Color(0xFFB4B9D0),
    surfaceContainerLowest = Color(0xFF0B0D19),
    surfaceContainerLow = Color(0xFF171B2D),
    surfaceContainer = Color(0xFF1B2034),
    surfaceContainerHigh = Color(0xFF22273D),
    surfaceContainerHighest = Color(0xFF2A3049),
    outline = Color(0xFF7B8199),
    outlineVariant = Color(0xFF2F354D),
    error = Color(0xFFF87171),
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFEE2E2),
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
)

/** [fontScale] multiplies the system font scale, so the in-app setting stacks with the phone's setting. */
@Composable
fun AgentLearningTheme(fontScale: Float = 1f, content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) DarkColors else LightColors
    val density = LocalDensity.current
    CompositionLocalProvider(
        LocalDensity provides Density(density.density, density.fontScale * fontScale)
    ) {
        MaterialTheme(
            colorScheme = colors,
            typography = AppTypography,
            shapes = AppShapes,
            content = content,
        )
    }
}
