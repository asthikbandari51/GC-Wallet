package com.giftvault.app.ui.components.glass

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class GlassButtonStyle {
    PRIMARY,
    SECONDARY,
    SUBTLE,
    ACCENT_GOLD
}

@Composable
fun LiquidGlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String? = null,
    icon: ImageVector? = null,
    style: GlassButtonStyle = GlassButtonStyle.PRIMARY,
    enabled: Boolean = true,
    shape: Shape = LiquidGlassTokens.ShapeMedium,
    fontSize: TextUnit = 13.sp,
    height: Dp = 44.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 8.dp),
    tokens: LiquidGlassTokens = currentGlassTokens(),
    content: (@Composable RowScope.() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.96f else 1.0f,
        animationSpec = LiquidGlassTokens.SpringTactile,
        label = "glass_btn_scale"
    )

    val surfaceBrush: Brush = when (style) {
        GlassButtonStyle.PRIMARY -> Brush.linearGradient(
            colors = listOf(
                Color(0xFF6366F1),
                Color(0xFF4F46E5)
            )
        )
        GlassButtonStyle.SECONDARY -> Brush.verticalGradient(
            colors = listOf(
                tokens.surfaceBase,
                tokens.surfaceSubtle
            )
        )
        GlassButtonStyle.SUBTLE -> Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = if (tokens.isDark) 0.10f else 0.40f),
                Color.White.copy(alpha = if (tokens.isDark) 0.05f else 0.20f)
            )
        )
        GlassButtonStyle.ACCENT_GOLD -> Brush.linearGradient(
            colors = listOf(
                Color(0xFFD97706),
                Color(0xFFB45309)
            )
        )
    }

    val borderBrush: Brush = when (style) {
        GlassButtonStyle.PRIMARY -> Brush.linearGradient(
            colors = listOf(
                Color(0xFFA5B4FC),
                Color(0xFF818CF8).copy(alpha = 0.5f)
            )
        )
        GlassButtonStyle.SECONDARY -> tokens.borderGradient
        GlassButtonStyle.SUBTLE -> Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.5f),
                Color.Transparent
            )
        )
        GlassButtonStyle.ACCENT_GOLD -> Brush.linearGradient(
            colors = listOf(
                Color(0xFFFDE68A),
                Color(0xFFF59E0B)
            )
        )
    }

    val contentColor: Color = when (style) {
        GlassButtonStyle.PRIMARY -> Color.White
        GlassButtonStyle.SECONDARY -> tokens.primaryAccent
        GlassButtonStyle.SUBTLE -> tokens.textPrimary
        GlassButtonStyle.ACCENT_GOLD -> Color.White
    }

    LiquidGlassSurface(
        modifier = modifier
            .scale(scale)
            .height(height)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        shape = shape,
        elevation = if (style == GlassButtonStyle.PRIMARY) 4.dp else 1.dp,
        surfaceBrush = surfaceBrush,
        borderBrush = borderBrush,
        borderWidth = 1.dp,
        tokens = tokens
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (content != null) {
                content()
            } else {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(17.dp)
                    )
                    if (!text.isNullOrBlank()) {
                        Spacer(Modifier.width(4.dp))
                    }
                }
                if (!text.isNullOrBlank()) {
                    Text(
                        text = text,
                        color = contentColor,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = fontSize,
                        maxLines = 1,
                        softWrap = false,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun LiquidGlassIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: Dp = 40.dp,
    iconSize: Dp = 20.dp,
    tint: Color? = null,
    shape: Shape = CircleShape,
    elevation: Dp = 2.dp,
    tokens: LiquidGlassTokens = currentGlassTokens()
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.92f else 1.0f,
        animationSpec = LiquidGlassTokens.SpringTactile,
        label = "glass_icon_btn_scale"
    )

    LiquidGlassSurface(
        modifier = modifier
            .size(size)
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        shape = shape,
        elevation = elevation,
        tokens = tokens
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = tint ?: tokens.primaryAccent,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}

/**
 * Standardized Liquid Glass floating Add button shared across Home and Bank Cards.
 * Features tactile spring compression, luminous border, and glowing gradient fill.
 */
@Composable
fun LiquidGlassAddButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String = "Add",
    size: Dp = 56.dp,
    tokens: LiquidGlassTokens = currentGlassTokens()
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1.0f,
        animationSpec = LiquidGlassTokens.SpringTactile,
        label = "glass_add_btn_scale"
    )

    LiquidGlassSurface(
        modifier = modifier
            .size(size)
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = CircleShape,
        elevation = 8.dp,
        surfaceBrush = Brush.linearGradient(
            colors = listOf(
                tokens.primaryAccent,
                Color(0xFF7C3AED)
            )
        ),
        borderBrush = tokens.activeBorderGradient,
        borderWidth = 1.5.dp,
        tokens = tokens
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            androidx.compose.material3.Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = contentDescription,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
