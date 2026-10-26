package com.giftvault.app.ui.components

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.giftvault.app.core.templates.ColorEngine
import com.giftvault.app.core.templates.TemplateRegistry
import kotlin.math.abs
import kotlin.math.sin

@Composable
fun GiftCardVisual(
    merchantName: String,
    cardNumber: String,
    balance: Double,
    currency: String,
    expiryDate: String?,
    templateKey: String,
    modifier: Modifier = Modifier,
    isRevealed: Boolean = false,
    isThumbnail: Boolean = false,
    compactMode: Boolean = false,
    isPrivacyMode: Boolean = false,
    enableCopyOnTap: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val haptic = LocalHapticFeedback.current

    val resId = remember(templateKey) {
        TemplateRegistry.getDrawableResId(context, templateKey)
    }

    val isLightTemplate = remember(templateKey) {
        TemplateRegistry.getTemplate(templateKey)?.isLightTemplate ?: false
    }

    // High contrast text tokens
    val textColor = if (isLightTemplate) Color(0xFF111111) else Color(0xFFF8F9FA)
    val subTextColor = if (isLightTemplate) Color(0xFF444444) else Color(0xFFE2E8F0)

    val isGenericTemplate = resId == 0 || templateKey == "other" || templateKey.isBlank()

    val formattedCardNumber = remember(cardNumber, isRevealed) {
        if (isRevealed) {
            cardNumber.chunked(4).joinToString(" ")
        } else {
            if (cardNumber.length > 4) {
                "•••• •••• •••• ${cardNumber.takeLast(4)}"
            } else {
                "•••• •••• •••• $cardNumber"
            }
        }
    }

    Surface(
        modifier = modifier
            .aspectRatio(1.586f)
            .clip(RoundedCornerShape(20.dp))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.25f),
                shape = RoundedCornerShape(20.dp)
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else if (enableCopyOnTap) {
                    Modifier.clickable {
                        clipboardManager.setText(AnnotatedString(cardNumber))
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        Toast.makeText(context, "$merchantName code copied", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(20.dp),
        shadowElevation = if (isThumbnail) 2.dp else 8.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (!isGenericTemplate) {
                // High-performance asynchronous background rendering with memory caching
                coil.compose.AsyncImage(
                    model = resId,
                    contentDescription = "$merchantName Card Background",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Procedural Gradient with optimized memoized seed
                val seed = remember(merchantName, cardNumber) { merchantName + cardNumber }
                val proceduralBrush = remember(seed) { ColorEngine.getProceduralGradient(seed) }
                val seedHash = remember(seed) { abs(seed.hashCode()) }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(proceduralBrush)
                )

                val wavePath = remember { Path() }
                // High-performance lightweight wave overlay
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val waveCount = 3
                    for (i in 0 until waveCount) {
                        wavePath.reset()
                        val yOffset = (h / waveCount) * i + (seedHash % 25)
                        wavePath.moveTo(0f, yOffset)
                        var x = 0f
                        while (x <= w) {
                            val waveY = yOffset + sin((x / w * 4 * Math.PI) + (i * 0.8)).toFloat() * 20f
                            wavePath.lineTo(x, waveY)
                            x += 25f
                        }
                        drawPath(
                            path = wavePath,
                            color = Color.White.copy(alpha = 0.08f + (i * 0.03f)),
                            style = Stroke(width = 2f)
                        )
                    }
                }
            }

            // GIFT CARD badge
            Surface(
                modifier = Modifier
                    .align(if (isThumbnail) Alignment.BottomEnd else Alignment.TopEnd)
                    .padding(if (isThumbnail) 4.dp else if (compactMode) 8.dp else 16.dp),
                color = if (isThumbnail) Color.Black.copy(alpha = 0.45f) else Color.Transparent,
                shape = RoundedCornerShape(6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(
                        horizontal = if (isThumbnail) 4.dp else if (compactMode) 4.dp else 0.dp,
                        vertical = if (isThumbnail) 2.dp else if (compactMode) 2.dp else 0.dp
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "GIFT CARD ",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = if (isThumbnail) 7.sp else if (compactMode) 8.sp else 11.sp,
                            letterSpacing = 1.sp
                        ),
                        fontWeight = FontWeight.Bold,
                        color = if (isThumbnail) Color.White else textColor
                    )
                    Icon(
                        imageVector = Icons.Rounded.CardGiftcard,
                        contentDescription = null,
                        tint = if (isThumbnail) Color.White else textColor,
                        modifier = Modifier.size(if (isThumbnail) 9.dp else if (compactMode) 11.dp else 16.dp)
                    )
                }
            }

            if (!isThumbnail) {
                if (isGenericTemplate) {
                    Text(
                        text = merchantName.ifBlank { "GIFT CARD" }.uppercase(),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = if (compactMode) 12.sp else 20.sp
                        ),
                        fontWeight = FontWeight.Black,
                        color = textColor,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(
                                start = if (compactMode) 12.dp else 24.dp,
                                top = if (compactMode) 10.dp else 20.dp
                            )
                    )
                }

                // Card Number (Center)
                Text(
                    text = formattedCardNumber,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = if (compactMode) 1.2.sp else 2.5.sp,
                        fontSize = if (compactMode) 11.sp else 18.sp
                    ),
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = if (compactMode) 10.dp else 24.dp)
                )

                // Balance (Bottom Left)
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(
                            start = if (compactMode) 12.dp else 24.dp,
                            bottom = if (compactMode) 8.dp else 18.dp
                        )
                ) {
                    Text(
                        text = "BALANCE",
                        style = MaterialTheme.typography.labelSmall,
                        color = subTextColor,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontSize = if (compactMode) 7.sp else 9.sp
                    )
                    Text(
                        text = if (isPrivacyMode) "₹ ••••" else "₹${String.format("%,.2f", balance)}",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = if (compactMode) 12.sp else 20.sp
                        ),
                        fontWeight = FontWeight.Black,
                        color = textColor
                    )
                }

                // Expiry (Bottom Right)
                if (expiryDate != null) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(
                                end = if (compactMode) 12.dp else 24.dp,
                                bottom = if (compactMode) 8.dp else 18.dp
                            ),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "VALID THRU",
                            style = MaterialTheme.typography.labelSmall,
                            color = subTextColor,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            fontSize = if (compactMode) 7.sp else 9.sp
                        )
                        Text(
                            text = expiryDate,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = if (compactMode) 10.sp else 14.sp
                            ),
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    }
                }
            }
        }
    }
}
