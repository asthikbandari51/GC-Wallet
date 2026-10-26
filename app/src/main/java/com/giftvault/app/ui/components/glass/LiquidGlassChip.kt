package com.giftvault.app.ui.components.glass

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LiquidGlassChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    shape: Shape = CircleShape,
    tokens: LiquidGlassTokens = currentGlassTokens()
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = LiquidGlassTokens.SpringTactile,
        label = "glass_chip_scale"
    )

    val surfaceBrush = if (selected) {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF6366F1),
                Color(0xFF4F46E5)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                tokens.surfaceBase,
                tokens.surfaceSubtle
            )
        )
    }

    val borderBrush = if (selected) {
        tokens.activeBorderGradient
    } else {
        tokens.borderGradient
    }

    val textColor by animateColorAsState(
        targetValue = if (selected) Color.White else tokens.textSecondary,
        label = "chip_text_color"
    )

    LiquidGlassSurface(
        modifier = modifier
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = shape,
        elevation = if (selected) 3.dp else 1.dp,
        surfaceBrush = surfaceBrush,
        borderBrush = borderBrush,
        borderWidth = if (selected) 1.2.dp else 1.dp,
        tokens = tokens
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(6.dp))
            }
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = textColor
            )
        }
    }
}
