package com.giftvault.app.ui.components.glass

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Tactile interactive Liquid Glass card with responsive iOS-style spring compression.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    isSelected: Boolean = false,
    shape: Shape = LiquidGlassTokens.ShapeLarge,
    elevation: Dp = 3.dp,
    surfaceBrush: Brush? = null,
    borderBrush: Brush? = null,
    borderWidth: Dp? = null,
    tokens: LiquidGlassTokens = currentGlassTokens(),
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled && onClick != null) 0.985f else 1.0f,
        animationSpec = LiquidGlassTokens.SpringTactile,
        label = "glass_card_press"
    )

    val activeBorder = borderBrush ?: if (isSelected) tokens.activeBorderGradient else tokens.borderGradient
    val activeSurface = surfaceBrush ?: if (isSelected) tokens.elevatedGradient else tokens.surfaceGradient
    val activeBorderWidth = borderWidth ?: if (isSelected) 1.5.dp else 1.dp

    LiquidGlassSurface(
        modifier = modifier
            .scale(scale)
            .then(
                if (onClick != null) {
                    Modifier.combinedClickable(
                        interactionSource = interactionSource,
                        indication = null,
                        enabled = enabled,
                        onClick = onClick,
                        onLongClick = onLongClick
                    )
                } else Modifier
            ),
        shape = shape,
        elevation = if (isSelected) elevation + 2.dp else elevation,
        surfaceBrush = activeSurface,
        borderBrush = activeBorder,
        borderWidth = activeBorderWidth,
        tokens = tokens,
        content = content
    )
}
