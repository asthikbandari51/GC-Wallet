package com.giftvault.app.ui.rupay

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.giftvault.app.core.rupay.RuPayRedeemManager
import com.giftvault.app.domain.model.BankCard

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuPayRedeemScreen(
    onNavigateBack: () -> Unit,
    onOpenAddCard: (String?) -> Unit = {},
    viewModel: RuPayRedeemViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val tokens = com.giftvault.app.ui.components.glass.currentGlassTokens()
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var pageTitle by remember { mutableStateOf("RuPay Select Rewards") }
    var isWebLoading by remember { mutableStateOf(true) }
    var showAssistantSheet by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            pageTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = tokens.textPrimary,
                            maxLines = 1
                        )
                        Text(
                            "rupayselect.truztee.com",
                            style = MaterialTheme.typography.labelSmall,
                            color = tokens.primaryAccent
                        )
                    }
                },
                navigationIcon = {
                    com.giftvault.app.ui.components.glass.LiquidGlassIconButton(
                        onClick = onNavigateBack,
                        icon = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        size = 38.dp,
                        iconSize = 20.dp,
                        tokens = tokens,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                },
                actions = {
                    com.giftvault.app.ui.components.glass.LiquidGlassIconButton(
                        onClick = { webViewInstance?.reload() },
                        icon = Icons.Rounded.Refresh,
                        contentDescription = "Reload",
                        size = 36.dp,
                        iconSize = 18.dp,
                        tokens = tokens
                    )
                    Spacer(Modifier.width(6.dp))
                    com.giftvault.app.ui.components.glass.LiquidGlassIconButton(
                        onClick = { showAssistantSheet = !showAssistantSheet },
                        icon = if (showAssistantSheet) Icons.Rounded.CreditCardOff else Icons.Rounded.CreditCard,
                        contentDescription = "Toggle Card Helper",
                        tint = tokens.primaryAccent,
                        size = 36.dp,
                        iconSize = 18.dp,
                        tokens = tokens,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Sandboxed In-App WebView pointing to RuPay Rewards Portal
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.loadWithOverviewMode = true
                        settings.useWideViewPort = true
                        settings.setSupportZoom(true)
                        settings.builtInZoomControls = true
                        settings.displayZoomControls = false

                        addJavascriptInterface(
                            object {
                                @android.webkit.JavascriptInterface
                                fun onVoucherScraped(code: String, pin: String?, merchant: String?) {
                                    (ctx as? android.app.Activity)?.runOnUiThread {
                                        clipboardManager.setText(AnnotatedString(code))
                                        viewModel.saveClaimedVoucher(code, pin, merchant ?: "RuPay Rewards Voucher")
                                        Toast.makeText(ctx, "🎉 RuPay Voucher Claimed & Saved: $code", Toast.LENGTH_LONG).show()
                                    }
                                }

                                @android.webkit.JavascriptInterface
                                fun notifyStatus(message: String) {
                                    (ctx as? android.app.Activity)?.runOnUiThread {
                                        Toast.makeText(ctx, "🤖 $message", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            "GiftVaultBridge"
                        )

                        webChromeClient = object : WebChromeClient() {
                            override fun onReceivedTitle(view: WebView?, title: String?) {
                                if (!title.isNullOrBlank()) pageTitle = title
                            }
                        }

                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                isWebLoading = true
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                isWebLoading = false
                                val observerJs = """
                                    (function() {
                                        if (window._gvObserverInstalled) return;
                                        window._gvObserverInstalled = true;
                                        function check() {
                                            const text = document.body ? document.body.innerText : '';
                                            const v = text.match(/(?:voucher|code|gift\s*code)[\s:]*([A-Z0-9]{8,20})/i);
                                            const p = text.match(/(?:pin|voucher\s*pin)[\s:]*([0-9]{3,8})/i);
                                            if (v && v[1] && window.GiftVaultBridge) {
                                                window.GiftVaultBridge.onVoucherScraped(v[1], p ? p[1] : null, "RuPay Rewards Voucher");
                                            }
                                        }
                                        setInterval(check, 3000);
                                    })();
                                """.trimIndent()
                                view?.evaluateJavascript(observerJs, null)
                            }

                            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                val url = request?.url?.toString() ?: return false
                                // Stay inside RuPay/Truztee session
                                return false
                            }
                        }

                        loadUrl(RuPayRedeemManager.RUPAY_PORTAL_URL)
                        webViewInstance = this
                    }
                }
            )

            if (isWebLoading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter),
                    color = Color(0xFF584BF6),
                    trackColor = Color(0xFFEEEDFF)
                )
            }

            // Floating Card Helper Card at the bottom for instant 1-tap copy
            AnimatedVisibility(
                visible = showAssistantSheet && uiState.selectedCard != null,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(14.dp)
            ) {
                uiState.selectedCard?.let { card ->
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        border = BorderStroke(1.dp, Color(0xFF584BF6)),
                        elevation = CardDefaults.cardElevation(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF584BF6)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Rounded.WorkspacePremium, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            card.cardNickname,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            "${card.network.displayName} ${card.tier.displayName}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFFFFD700)
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { showAssistantSheet = false },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Rounded.Close, contentDescription = "Close", tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                                }
                            }

                            Spacer(Modifier.height(10.dp))

                            // Quick Copy Actions Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Copy Card Number
                                OutlinedButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(card.cardNumber))
                                        Toast.makeText(context, "Card Number Copied!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1.3f),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Rounded.ContentCopy, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        "•• ${card.lastFourDigits}",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                // Copy Expiry
                                if (card.expiryMonth.isNotBlank() && card.expiryYear.isNotBlank()) {
                                    OutlinedButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(card.formattedExpiry))
                                            Toast.makeText(context, "Expiry Copied!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.weight(0.9f),
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            card.formattedExpiry,
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }

                                // Copy CVV
                                if (card.cvv.isNotBlank()) {
                                    OutlinedButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(card.cvv))
                                            Toast.makeText(context, "CVV Copied!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.weight(0.8f),
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, Color(0xFFFFD700)),
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            "CVV",
                                            color = Color(0xFFFFD700),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(8.dp))

                            // RuPay Concierge Desk
                            Button(
                                onClick = {
                                    try {
                                        context.startActivity(
                                            android.content.Intent(android.content.Intent.ACTION_DIAL, android.net.Uri.parse("tel:18002667077")).apply {
                                                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                            }
                                        )
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "RuPay Concierge Desk: 1800 266 7077", Toast.LENGTH_LONG).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                                contentPadding = PaddingValues(vertical = 6.dp)
                            ) {
                                Icon(Icons.Rounded.Phone, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("RuPay Concierge Desk: 1800 266 7077", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
