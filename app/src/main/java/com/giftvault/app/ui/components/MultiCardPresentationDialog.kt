package com.giftvault.app.ui.components

import android.app.Activity
import android.os.Build
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
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
import kotlinx.coroutines.launch

@Composable
fun MultiCardPresentationDialog(
    cards: List<GiftCard>,
    onDismiss: () -> Unit,
    onMarkCardRedeemed: (String) -> Unit,
    tokens: LiquidGlassTokens = currentGlassTokens()
) {
    if (cards.isEmpty()) return

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { cards.size })

    // Hardware Display Auto-Boost (v0.9 Module 7)
    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        val originalBrightness = window?.attributes?.screenBrightness
        val layoutParams = window?.attributes
        layoutParams?.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL
        window?.attributes = layoutParams

        onDispose {
            val restoreParams = window?.attributes
            restoreParams?.screenBrightness = originalBrightness ?: WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
            window?.attributes = restoreParams
        }
    }

    // Kalyan Jewellers Showroom Stacking Cap Check
    val isKalyanPool = cards.all { it.merchantName.contains("Kalyan", ignoreCase = true) }
    val exceedsShowroomCap = isKalyanPool && cards.size > 10

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
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
                    .padding(top = 10.dp, bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Checkout Barcode Deck",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF6366F1).copy(alpha = 0.25f)
                            ) {
                                Text(
                                    "${pagerState.currentPage + 1} of ${cards.size}",
                                    color = Color(0xFFA5B4FC),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Swipe left / right to present multiple vouchers",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8)
                        )
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

                // Showroom Stacking Cap Guard Warning Banner
                if (exceedsShowroomCap) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFFEF3C7),
                        border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.Warning, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Showroom billing terminals allow a maximum of 10 vouchers per invoice. Current pool contains ${cards.size} vouchers.",
                                color = Color(0xFF92400E),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Centered Barcode Pass Container (No Excessive Whitespace)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                    ) { page ->
                        val card = cards[page]

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .shadow(24.dp, shape = RoundedCornerShape(26.dp))
                                .clip(RoundedCornerShape(26.dp)),
                            color = Color.White,
                            shape = RoundedCornerShape(26.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight()
                                    .padding(horizontal = 20.dp, vertical = 20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                // Merchant Header
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = card.merchantName,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF0F172A)
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFEEEDFF)
                                    ) {
                                        Text(
                                            "₹${card.balance.toInt()}",
                                            color = Color(0xFF584BF6),
                                            fontWeight = FontWeight.Black,
                                            fontSize = 14.sp,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                // Code 128 Monochrome Barcode Canvas (Crisp 85dp)
                                Code128BarcodeCanvas(
                                    code = card.cardNumber,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(85.dp)
                                )

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

                                // PIN Info with reveal toggle
                                if (!card.pin.isNullOrBlank()) {
                                    var isPinRevealed by remember(card.id) { mutableStateOf(false) }
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFFFEF2F2),
                                        border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                                        modifier = Modifier.clickable { isPinRevealed = !isPinRevealed }
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = if (isPinRevealed) "PIN: ${card.pin}" else "PIN: ••••",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                                                fontWeight = FontWeight.Black,
                                                color = Color(0xFFDC2626)
                                            )
                                            Spacer(Modifier.width(6.dp))
                                            Icon(
                                                imageVector = if (isPinRevealed) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                                contentDescription = "Toggle PIN",
                                                tint = Color(0xFFDC2626),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Action Controls Row (Liquid Glass buttons elevated and fully visible)
                val currentCard = cards[pagerState.currentPage]
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Copy Current Code
                        LiquidGlassButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(currentCard.cardNumber))
                                Toast.makeText(context, "Copied ${currentCard.merchantName} code", Toast.LENGTH_SHORT).show()
                            },
                            text = "Copy Code",
                            icon = Icons.Rounded.ContentCopy,
                            style = GlassButtonStyle.SECONDARY,
                            height = 46.dp,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f),
                            tokens = tokens
                        )

                        // Mark Redeemed & Advance Next
                        LiquidGlassButton(
                            onClick = {
                                onMarkCardRedeemed(currentCard.id)
                                if (pagerState.currentPage < cards.size - 1) {
                                    scope.launch {
                                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                    }
                                } else {
                                    Toast.makeText(context, "All vouchers in deck redeemed!", Toast.LENGTH_SHORT).show()
                                    onDismiss()
                                }
                            },
                            text = "Redeemed & Next",
                            icon = Icons.AutoMirrored.Rounded.ArrowForward,
                            style = GlassButtonStyle.PRIMARY,
                            height = 46.dp,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1.3f),
                            tokens = tokens
                        )
                    }

                    // Done Presenting
                    LiquidGlassButton(
                        onClick = onDismiss,
                        text = "Done Presenting",
                        style = GlassButtonStyle.SUBTLE,
                        height = 46.dp,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                        tokens = tokens
                    )
                }
            }
        }
    }
}
