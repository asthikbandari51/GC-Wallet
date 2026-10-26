package com.giftvault.app.ui.components.glass

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Design tokens for the Liquid Glass visual material system.
 * Simulates layered refraction, specular edge highlights, environmental tinting,
 * and physical tactile feedback while remaining ultra-fast for 120Hz displays.
 */
@Immutable
data class LiquidGlassTokens(
    val isDark: Boolean,
    // Surfaces
    val surfaceBase: Color,
    val surfaceElevated: Color,
    val surfaceSubtle: Color,
    val surfaceActive: Color,
    // Brushes
    val surfaceGradient: Brush,
    val elevatedGradient: Brush,
    val borderGradient: Brush,
    val activeBorderGradient: Brush,
    val specularHighlightGradient: Brush,
    // Borders
    val borderDefault: Color,
    val borderSubtle: Color,
    val borderActive: Color,
    // Accents
    val primaryAccent: Color,
    val secondaryAccent: Color,
    val glowColor: Color,
    // Text on Glass
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val textOnAccent: Color,
    // Shadows & Depth
    val shadowColor: Color,
    val defaultElevation: Dp = 4.dp,
    val elevatedElevation: Dp = 10.dp
) {
    // Semantic Convenience Tokens
    val iconPrimary: Color get() = textPrimary
    val iconSecondary: Color get() = textSecondary
    val surface: Color get() = surfaceBase
    val surfaceStrong: Color get() = surfaceElevated
    val border: Color get() = borderDefault
    val divider: Color get() = borderSubtle
    val accent: Color get() = primaryAccent
    val accentMuted: Color get() = glowColor
    val success: Color get() = Color(0xFF10B981)
    val warning: Color get() = Color(0xFFF59E0B)
    val error: Color get() = Color(0xFFEF4444)

    companion object {
        val ShapeSmall = RoundedCornerShape(10.dp)
        val ShapeMedium = RoundedCornerShape(16.dp)
        val ShapeLarge = RoundedCornerShape(24.dp)
        val ShapePill = RoundedCornerShape(999.dp)
        val ShapeModal = RoundedCornerShape(32.dp)

        val SpringTactile = spring<Float>(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        )

        val SpringSmooth = spring<Float>(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        )

        fun light(): LiquidGlassTokens = LiquidGlassTokens(
            isDark = false,
            surfaceBase = Color(0xF2FFFFFF), // 95% translucent crisp white
            surfaceElevated = Color(0xFAFFFFFF),
            surfaceSubtle = Color(0x66EEF2FF),
            surfaceActive = Color(0xFFEEEDFF),
            surfaceGradient = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFFFFFFF).copy(alpha = 0.96f),
                    Color(0xFFF8FAFC).copy(alpha = 0.88f)
                )
            ),
            elevatedGradient = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFFFFFFF),
                    Color(0xFFF1F5F9).copy(alpha = 0.95f)
                )
            ),
            borderGradient = Brush.linearGradient(
                colors = listOf(
                    Color(0xFFFFFFFF).copy(alpha = 0.95f),
                    Color(0xFFE2E8F0).copy(alpha = 0.60f),
                    Color(0xFFCBD5E1).copy(alpha = 0.35f)
                )
            ),
            activeBorderGradient = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF818CF8),
                    Color(0xFF584BF6),
                    Color(0xFFC084FC)
                )
            ),
            specularHighlightGradient = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFFFFFFF).copy(alpha = 0.50f),
                    Color(0xFFFFFFFF).copy(alpha = 0.0f)
                )
            ),
            borderDefault = Color(0xFFE2E8F0).copy(alpha = 0.85f),
            borderSubtle = Color(0xFFE2E8F0).copy(alpha = 0.45f),
            borderActive = Color(0xFF584BF6),
            primaryAccent = Color(0xFF584BF6),
            secondaryAccent = Color(0xFF06B6D4),
            glowColor = Color(0x33584BF6),
            textPrimary = Color(0xFF0F172A),
            textSecondary = Color(0xFF475569),
            textTertiary = Color(0xFF94A3B8),
            textOnAccent = Color(0xFFFFFFFF),
            shadowColor = Color(0x140F172A)
        )

        fun dark(): LiquidGlassTokens = LiquidGlassTokens(
            isDark = true,
            surfaceBase = Color(0xCC141936), // Translucent deep obsidian
            surfaceElevated = Color(0xE61E244A),
            surfaceSubtle = Color(0x551E244A),
            surfaceActive = Color(0xCC2A2D5C),
            surfaceGradient = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF1E244A).copy(alpha = 0.92f),
                    Color(0xFF141936).copy(alpha = 0.85f)
                )
            ),
            elevatedGradient = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF262D5A).copy(alpha = 0.95f),
                    Color(0xFF181D3E).copy(alpha = 0.90f)
                )
            ),
            borderGradient = Brush.linearGradient(
                colors = listOf(
                    Color(0xFFFFFFFF).copy(alpha = 0.35f),
                    Color(0xFF8B80F9).copy(alpha = 0.25f),
                    Color(0xFF000000).copy(alpha = 0.40f)
                )
            ),
            activeBorderGradient = Brush.linearGradient(
                colors = listOf(
                    Color(0xFFA5B4FC),
                    Color(0xFF8B80F9),
                    Color(0xFFE879F9)
                )
            ),
            specularHighlightGradient = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFFFFFFF).copy(alpha = 0.20f),
                    Color(0xFFFFFFFF).copy(alpha = 0.0f)
                )
            ),
            borderDefault = Color(0xFF2A3366).copy(alpha = 0.70f),
            borderSubtle = Color(0xFF2A3366).copy(alpha = 0.35f),
            borderActive = Color(0xFF8B80F9),
            primaryAccent = Color(0xFF8B80F9),
            secondaryAccent = Color(0xFFFFD700),
            glowColor = Color(0x408B80F9),
            textPrimary = Color(0xFFF8FAFC),
            textSecondary = Color(0xFFCBD5E1),
            textTertiary = Color(0xFF64748B),
            textOnAccent = Color(0xFFFFFFFF),
            shadowColor = Color(0x66000000)
        )
    }
}

val LocalLiquidGlassTokens = compositionLocalOf { LiquidGlassTokens.light() }

@Composable
fun currentGlassTokens(): LiquidGlassTokens {
    val isDark = isSystemInDarkTheme()
    return if (isDark) LiquidGlassTokens.dark() else LiquidGlassTokens.light()
}
