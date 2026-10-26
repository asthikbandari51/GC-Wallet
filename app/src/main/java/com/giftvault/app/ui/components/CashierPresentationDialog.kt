package com.giftvault.app.ui.components

import android.os.Build
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.giftvault.app.domain.model.GiftCard
import com.giftvault.app.ui.components.glass.*

@Composable
fun CashierPresentationDialog(
    card: GiftCard,
    onDismiss: () -> Unit,
    tokens: LiquidGlassTokens = currentGlassTokens()
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val haptic = LocalHapticFeedback.current
    var isCopied by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        val dialogWindow = (LocalView.current.parent as? DialogWindowProvider)?.window
        SideEffect {
            dialogWindow?.let { win ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    win.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                    win.attributes = win.attributes.apply {
                        blurBehindRadius = 60
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xCC050814),
                            Color(0xDD0D1026),
                            Color(0xEE080914)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .padding(horizontal = 20.dp)
                    .padding(top = 10.dp, bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterVertically)
            ) {
                // Top Header with Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(tokens.activeBorderGradient),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.Storefront, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Cashier Presentation",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Spacer(Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF10B981).copy(alpha = 0.2f),
                                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        "READY",
                                        color = Color(0xFF34D399),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "${card.merchantName} • ${card.category.displayName}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFA5B4FC)
                            )
                        }
                    }

                    LiquidGlassIconButton(
                        onClick = onDismiss,
                        icon = Icons.Rounded.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        size = 36.dp,
                        iconSize = 18.dp,
                        tokens = tokens
                    )
                }

                // Premium High-Contrast White Pass Card with Luminous Frame
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(20.dp, shape = RoundedCornerShape(26.dp))
                        .clip(RoundedCornerShape(26.dp))
                        .background(Color.White),
                    color = Color.White,
                    shape = RoundedCornerShape(26.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Scan Label with Laser Icon
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF1F5F9))
                                .padding(vertical = 4.dp)
                        ) {
                            Icon(Icons.Rounded.QrCode2, contentDescription = null, tint = Color(0xFF475569), modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "SCAN CODE AT REGISTER",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.5.sp,
                                color = Color(0xFF334155),
                                fontSize = 10.sp
                            )
                        }

                        Spacer(Modifier.height(10.dp))

                        // High-Contrast Code 128 Barcode Canvas (Crisp Solid Black on White)
                        Code128BarcodeCanvas(
                            code = card.cardNumber,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(85.dp)
                        )

                        Spacer(Modifier.height(8.dp))

                        // Monospaced Voucher Code
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = card.cardNumber.chunked(4).joinToString(" "),
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 2.sp,
                                    fontSize = 18.sp
                                ),
                                color = Color(0xFF0F172A),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp)
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        // Security PIN & Balance Cards Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (!card.pin.isNullOrBlank()) {
                                var isPinRevealed by remember(card.id) { mutableStateOf(false) }
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { isPinRevealed = !isPinRevealed },
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0xFFF8FAFC),
                                    border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("SECURITY PIN", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF64748B), fontSize = 9.sp)
                                            Spacer(Modifier.width(4.dp))
                                            Icon(
                                                imageVector = if (isPinRevealed) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                                contentDescription = "Toggle PIN",
                                                tint = Color(0xFF64748B),
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            if (isPinRevealed) card.pin else "••••",
                                            style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace),
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFF0F172A),
                                            fontSize = 15.sp
                                        )
                                    }
                                }
                            }

                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFFEEEDFF),
                                border = BorderStroke(1.dp, Color(0xFF6366F1).copy(alpha = 0.5f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("AVAILABLE BALANCE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF4F46E5), fontSize = 9.sp)
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        "₹${String.format("%.0f", card.balance)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF312E81),
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Elevated Action Buttons Section (Liquid Glass buttons moved upwards)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LiquidGlassButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(card.cardNumber))
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            isCopied = true
                            Toast.makeText(context, "Voucher code copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        text = if (isCopied) "Voucher Code Copied!" else "Copy Voucher Code",
                        icon = if (isCopied) Icons.Rounded.Check else Icons.Rounded.ContentCopy,
                        style = GlassButtonStyle.SECONDARY,
                        height = 48.dp,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                        tokens = tokens
                    )

                    LiquidGlassButton(
                        onClick = onDismiss,
                        text = "Done Presenting",
                        style = GlassButtonStyle.PRIMARY,
                        height = 48.dp,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                        tokens = tokens
                    )
                }
            }
        }
    }
}
