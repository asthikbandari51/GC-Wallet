package com.giftvault.app.ui.components.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Fundamental layered Liquid Glass surface component.
 * Provides physical depth, dual-tone luminous hairline border,
 * environmental glass gradient, and subtle specular top sheen.
 */
@Composable
fun LiquidGlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = LiquidGlassTokens.ShapeLarge,
    elevation: Dp = 2.dp,
    surfaceBrush: Brush? = null,
    borderBrush: Brush? = null,
    borderWidth: Dp = 1.dp,
    specularHighlight: Boolean = true,
    tokens: LiquidGlassTokens = currentGlassTokens(),
    content: @Composable BoxScope.() -> Unit
) {
    val activeSurfaceBrush = surfaceBrush ?: tokens.surfaceGradient
    val activeBorderBrush = borderBrush ?: tokens.borderGradient

    Box(
        modifier = modifier
            .then(
                if (elevation > 0.dp) {
                    Modifier.shadow(
                        elevation = elevation,
                        shape = shape,
                        ambientColor = tokens.shadowColor,
                        spotColor = tokens.shadowColor
                    )
                } else Modifier
            )
            .clip(shape)
            .background(activeSurfaceBrush)
            .then(
                if (specularHighlight) {
                    Modifier.drawWithContent {
                        drawContent()
                        // Draw soft specular reflection across top edge
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    if (tokens.isDark) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.35f),
                                    Color.Transparent
                                ),
                                startY = 0f,
                                endY = size.height * 0.35f
                            ),
                            topLeft = Offset.Zero,
                            size = Size(size.width, size.height * 0.35f)
                        )
                    }
                } else Modifier
            )
            .border(
                width = borderWidth,
                brush = activeBorderBrush,
                shape = shape
            ),
        content = content
    )
}
