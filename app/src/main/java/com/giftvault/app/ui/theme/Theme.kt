package com.giftvault.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import com.giftvault.app.ui.components.glass.LiquidGlassTokens
import com.giftvault.app.ui.components.glass.LocalLiquidGlassTokens

enum class ThemeMode { LIGHT, LIQUID_GLASS }

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
    error = LightError
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = Color.White,
    secondary = DarkSecondary,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    error = DarkError
)

private val LiquidGlassColorScheme = lightColorScheme(
    primary = GlassPrimary,
    onPrimary = GlassOnPrimary,
    primaryContainer = GlassPrimaryContainer,
    secondary = GlassSecondary,
    background = GlassBackground,
    onBackground = LightOnBackground,
    surface = GlassSurface,
    onSurface = LightOnSurface,
    surfaceVariant = GlassSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    error = GlassError
)

private val LiquidGlassDarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = Color.White,
    primaryContainer = DarkPrimaryContainer,
    secondary = DarkSecondary,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    error = DarkError
)

@Composable
fun GiftVaultTheme(
    themeMode: ThemeMode = ThemeMode.LIQUID_GLASS,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()

    val colorScheme = when (themeMode) {
        ThemeMode.LIGHT -> if (isSystemDark) DarkColorScheme else LightColorScheme
        ThemeMode.LIQUID_GLASS -> if (isSystemDark) LiquidGlassDarkColorScheme else LiquidGlassColorScheme
    }

    val glassTokens = if (isSystemDark) {
        LiquidGlassTokens.dark()
    } else {
        LiquidGlassTokens.light()
    }

    CompositionLocalProvider(LocalLiquidGlassTokens provides glassTokens) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}
