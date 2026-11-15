package com.giftvault.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.giftvault.app.ui.components.glass.LiquidGlassTokens
import com.giftvault.app.ui.components.glass.currentGlassTokens

@Composable
fun SpendProgressRing(
    currentSpend: Double,
    targetSpend: Double,
    size: Dp = 90.dp,
    strokeWidth: Dp = 8.dp,
    label: String? = null,
    tokens: LiquidGlassTokens = currentGlassTokens()
) {
    val progress = if (targetSpend > 0) (currentSpend / targetSpend).toFloat().coerceIn(0f, 1f) else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 800),
        label = "spend_progress"
    )

    val gradientBrush = Brush.sweepGradient(
        colors = listOf(
            Color(0xFF8B5CF6),
            Color(0xFF3B82F6),
            Color(0xFF10B981),
            Color(0xFF8B5CF6)
        )
    )

    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidthPx = strokeWidth.toPx()
            val arcSize = Size(size.toPx() - strokeWidthPx, size.toPx() - strokeWidthPx)
            val topLeft = Offset(strokeWidthPx / 2f, strokeWidthPx / 2f)

            // Background Track
            drawArc(
                color = if (tokens.isDark) Color(0x332A2D5C) else Color(0xFFE2E8F0),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )

            // Progress Arc
            drawArc(
                brush = gradientBrush,
                startAngle = -90f,
                sweepAngle = animatedProgress * 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${(animatedProgress * 100).toInt()}%",
                fontWeight = FontWeight.Black,
                fontSize = (size.value * 0.22f).sp,
                color = tokens.textPrimary
            )
            if (label != null) {
                Text(
                    text = label,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = tokens.textTertiary
                )
            }
        }
    }
}
