package com.giftvault.app.ui.carddetails

import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.giftvault.app.core.sharing.ShareManager
import com.giftvault.app.core.sharing.ShareMode
import com.giftvault.app.domain.model.BankCard
import com.giftvault.app.domain.model.CardStatus
import com.giftvault.app.domain.model.GiftCard
import com.giftvault.app.domain.model.Receipt
import com.giftvault.app.domain.model.Transaction
import com.giftvault.app.domain.model.TransactionType
import com.giftvault.app.ui.components.AddReceiptBottomSheet
import com.giftvault.app.ui.components.BarcodeDialog
import com.giftvault.app.ui.components.GiftCardVisual
import com.giftvault.app.ui.components.ReceiptsRow
import com.giftvault.app.ui.components.glass.*
import com.giftvault.app.ui.receipts.FullScreenReceiptViewer
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardDetailsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEditCard: (String) -> Unit,
    viewModel: CardDetailsViewModel = hiltViewModel()
) {
    val tokens = currentGlassTokens()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val shareManager = remember(viewModel.themeManager) { ShareManager(context, viewModel.themeManager) }

    var showShareSheet by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) {
            onNavigateBack()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.dismissError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.card?.merchantName ?: "Card Details",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = tokens.textPrimary
                    )
                },
                navigationIcon = {
                    LiquidGlassIconButton(
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
                    uiState.card?.let { card ->
                        LiquidGlassIconButton(
                            onClick = { onNavigateToEditCard(card.id) },
                            icon = Icons.Rounded.Edit,
                            contentDescription = "Edit Card",
                            size = 38.dp,
                            iconSize = 18.dp,
                            tokens = tokens
                        )
                        Spacer(Modifier.width(6.dp))
                        LiquidGlassIconButton(
                            onClick = { viewModel.toggleFavorite() },
                            icon = if (card.isFavorite) Icons.Rounded.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (card.isFavorite) Color(0xFFEF4444) else tokens.iconSecondary,
                            size = 38.dp,
                            iconSize = 18.dp,
                            tokens = tokens
                        )
                        Spacer(Modifier.width(6.dp))
                        if (card.status == CardStatus.ARCHIVED) {
                            LiquidGlassIconButton(
                                onClick = {
                                    viewModel.unarchiveCard()
                                    Toast.makeText(context, "Card restored to active vault", Toast.LENGTH_SHORT).show()
                                },
                                icon = Icons.Rounded.Unarchive,
                                contentDescription = "Restore Card",
                                tint = tokens.primaryAccent,
                                size = 38.dp,
                                iconSize = 18.dp,
                                tokens = tokens
                            )
                            Spacer(Modifier.width(6.dp))
                            LiquidGlassIconButton(
                                onClick = { viewModel.showDeleteConfirmation() },
                                icon = Icons.Rounded.DeleteForever,
                                contentDescription = "Delete Permanently",
                                tint = Color(0xFFEF4444),
                                size = 38.dp,
                                iconSize = 18.dp,
                                tokens = tokens,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        } else {
                            LiquidGlassIconButton(
                                onClick = { viewModel.showArchiveConfirmation() },
                                icon = Icons.Rounded.Archive,
                                contentDescription = "Archive Card",
                                tint = Color(0xFFD97706),
                                size = 38.dp,
                                iconSize = 18.dp,
                                tokens = tokens,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = tokens.primaryAccent)
            }
        } else if (uiState.card != null) {
            val card = uiState.card!!
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    HeroCardSection(
                        card = card,
                        receiptCount = uiState.receipts.size,
                        isRevealed = uiState.isRevealed,
                        isPrivacyMode = uiState.isPrivacyMode,
                        onRequestReveal = {
                            viewModel.toggleReveal(context as? FragmentActivity)
                        },
                        onShowCashier = { viewModel.showCashierDialog() },
                        onReceiptClick = { viewModel.onReceiptButtonClicked() },
                        onShare = { showShareSheet = true },
                        onCopy = {
                            clipboardManager.setText(AnnotatedString(card.cardNumber))
                            Toast.makeText(context, "Card number copied to clipboard", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                item {
                    BalanceSection(
                        card = card,
                        isPrivacyMode = uiState.isPrivacyMode,
                        onRedeemClick = { viewModel.showBalanceDialog(TransactionType.REDEEM) },
                        onAddFundsClick = { viewModel.showBalanceDialog(TransactionType.ADD) }
                    )
                }

                item {
                    ReceiptsRow(
                        receipts = uiState.receipts,
                        onAddReceiptClick = { viewModel.showAddReceiptSheet() },
                        onReceiptClick = { _, index -> viewModel.openViewerAtIndex(index) },
                        onDeleteReceiptClick = { receipt -> viewModel.deleteReceipt(receipt) },
                        isProcessing = uiState.isProcessingReceipt
                    )
                }

                item {
                    CardInfoSection(
                        card = card,
                        linkedBankCard = uiState.linkedBankCard,
                        isRevealed = uiState.isRevealed,
                        onRequestReveal = {
                            if (context is FragmentActivity) {
                                viewModel.requestReveal(context)
                            }
                        }
                    )
                }

                item {
                    Text(
                        text = "Transaction History",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = tokens.textPrimary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                if (uiState.transactions.isEmpty()) {
                    item {
                        Text(
                            text = "No transactions yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = tokens.textTertiary,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }
                } else {
                    items(uiState.transactions) { transaction ->
                        TransactionItem(transaction = transaction, currency = card.currency, tokens = tokens)
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Card not found", color = tokens.textSecondary)
            }
        }

        // Cashier Presentation Mode Dialog
        if (uiState.showCashierDialog && uiState.card != null) {
            com.giftvault.app.ui.components.CashierPresentationDialog(
                card = uiState.card!!,
                onDismiss = { viewModel.hideCashierDialog() }
            )
        }

        // Barcode Scanner Display Dialog
        if (uiState.showBarcodeDialog && uiState.card != null) {
            BarcodeDialog(
                merchantName = uiState.card!!.merchantName,
                cardNumber = uiState.card!!.cardNumber,
                onDismiss = { viewModel.hideBarcodeDialog() }
            )
        }

        // Balance Dialog
        if (uiState.showBalanceDialog) {
            BalanceDialog(
                type = uiState.balanceDialogType,
                onDismiss = { viewModel.hideBalanceDialog() },
                onConfirm = { amount, note -> viewModel.updateBalance(amount, note) },
                tokens = tokens
            )
        }

        // Delete Card Confirmation Dialog
        if (uiState.showDeleteConfirmation) {
            LiquidGlassDialog(
                onDismissRequest = { viewModel.hideDeleteConfirmation() },
                title = "Delete Card",
                tokens = tokens,
                confirmButton = {
                    LiquidGlassButton(
                        onClick = { viewModel.deleteCard() },
                        text = "Delete",
                        style = GlassButtonStyle.PRIMARY,
                        tokens = tokens
                    )
                },
                dismissButton = {
                    LiquidGlassButton(
                        onClick = { viewModel.hideDeleteConfirmation() },
                        text = "Cancel",
                        style = GlassButtonStyle.SECONDARY,
                        tokens = tokens
                    )
                }
            ) {
                Text(
                    text = "Are you sure you want to delete this card? All attached receipts and balance transactions will also be permanently removed.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = tokens.textSecondary
                )
            }
        }

        // Add Receipt Bottom Sheet
        if (uiState.showAddReceiptSheet) {
            AddReceiptBottomSheet(
                onDismiss = { viewModel.hideAddReceiptSheet() },
                onImageSelected = { uri -> viewModel.addReceipt(uri) }
            )
        }

        // Full Screen Receipt Viewer
        uiState.viewerInitialIndex?.let { initialIdx ->
            FullScreenReceiptViewer(
                receipts = uiState.receipts,
                initialIndex = initialIdx,
                onDismiss = { viewModel.closeViewer() },
                onDeleteReceipt = { receipt -> viewModel.deleteReceipt(receipt) }
            )
        }

        // Quick Share Sheet Modal
        if (showShareSheet && uiState.card != null) {
            com.giftvault.app.ui.components.QuickShareSheet(
                card = uiState.card!!,
                onDismiss = { showShareSheet = false },
                onSelectShareMode = { mode ->
                    shareManager.shareCard(uiState.card!!, mode)
                }
            )
        }

        // Archive Card Confirmation Dialog
        if (uiState.showArchiveConfirmation) {
            LiquidGlassDialog(
                onDismissRequest = { viewModel.hideArchiveConfirmation() },
                title = "Archive Card",
                tokens = tokens,
                confirmButton = {
                    LiquidGlassButton(
                        onClick = { viewModel.archiveCard() },
                        text = "Archive",
                        style = GlassButtonStyle.ACCENT_GOLD,
                        tokens = tokens
                    )
                },
                dismissButton = {
                    LiquidGlassButton(
                        onClick = { viewModel.hideArchiveConfirmation() },
                        text = "Cancel",
                        style = GlassButtonStyle.SECONDARY,
                        tokens = tokens
                    )
                }
            ) {
                Text(
                    text = "Archive this voucher? It will be hidden from the active vault and home screen. You can access, restore, or permanently delete it from the Recycle Bin in Settings.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = tokens.textSecondary
                )
            }
        }
    }
}

@Composable
fun HeroCardSection(
    card: GiftCard,
    receiptCount: Int,
    isRevealed: Boolean,
    isPrivacyMode: Boolean = false,
    onRequestReveal: () -> Unit,
    onShowCashier: () -> Unit,
    onReceiptClick: () -> Unit,
    onShare: () -> Unit,
    onCopy: () -> Unit,
    tokens: LiquidGlassTokens = currentGlassTokens()
) {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxWidth()) {

        GiftCardVisual(
            merchantName = card.merchantName,
            cardNumber = card.cardNumber,
            balance = card.balance,
            currency = card.currency,
            expiryDate = card.expiryDate?.format(DateTimeFormatter.ofPattern("MM/yy")),
            templateKey = card.templateKey,
            isRevealed = isRevealed,
            isPrivacyMode = isPrivacyMode,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ActionButton(
                icon = Icons.Default.Storefront,
                label = "Cashier",
                onClick = onShowCashier,
                tokens = tokens
            )
            ActionButton(
                icon = if (isRevealed) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                label = if (isRevealed) "Hide" else "Reveal",
                onClick = onRequestReveal,
                tokens = tokens
            )
            ActionButton(
                icon = Icons.Default.Share,
                label = "Share",
                onClick = onShare,
                tokens = tokens
            )
            ActionButton(
                icon = Icons.Default.ContentCopy,
                label = "Copy",
                onClick = onCopy,
                tokens = tokens
            )
            val merchantLower = card.merchantName.lowercase().replace(Regex("[^a-z0-9]"), "")
            val isPhysicalStoreBrand = merchantLower.contains("apollo") ||
                    merchantLower.contains("lakme") ||
                    merchantLower.contains("reliancedigital") ||
                    merchantLower.contains("kalyan") ||
                    merchantLower.contains("croma") ||
                    merchantLower.contains("trends")

            if (isPhysicalStoreBrand) {
                ActionButton(
                    icon = Icons.Default.LocationOn,
                    label = "Find Store",
                    tokens = tokens,
                    onClick = {
                        val storeQuery = when {
                            merchantLower.contains("croma") -> "Croma Electronics Store near me"
                            merchantLower.contains("trends") -> "Reliance Trends Store near me"
                            merchantLower.contains("apollo") -> "Apollo Pharmacy near me"
                            merchantLower.contains("lakme") -> "Lakme Salon near me"
                            merchantLower.contains("reliancedigital") -> "Reliance Digital Store near me"
                            merchantLower.contains("kalyan") -> "Kalyan Jewellers near me"
                            else -> "${card.merchantName} store near me"
                        }
                        val query = android.net.Uri.encode(storeQuery)
                        val mapIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("geo:0,0?q=$query")).apply {
                            setPackage("com.google.android.apps.maps")
                            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        val fallbackIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://www.google.com/maps/search/?api=1&query=$query")).apply {
                            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        try {
                            context.startActivity(mapIntent)
                        } catch (e: Exception) {
                            context.startActivity(fallbackIntent)
                        }
                    }
                )
            } else {
                ActionButton(
                    icon = Icons.Default.OpenInNew,
                    label = "Redeem App",
                    tokens = tokens,
                    onClick = {
                        val webUrl = when {
                            merchantLower.contains("swiggy") -> "https://www.swiggy.com"
                            merchantLower.contains("zomato") -> "https://www.zomato.com"
                            merchantLower.contains("blinkit") -> "https://www.blinkit.com"
                            merchantLower.contains("zepto") -> "https://www.zepto.com"
                            merchantLower.contains("ajio") -> "https://www.ajio.com"
                            merchantLower.contains("makemytrip") || merchantLower.contains("mmt") -> "https://www.makemytrip.com"
                            merchantLower.contains("flipkart") -> "https://www.flipkart.com"
                            merchantLower.contains("hppay") -> "https://www.hppay.in"
                            merchantLower.contains("domino") -> "https://www.dominos.co.in"
                            merchantLower.contains("amazon") -> "https://www.amazon.in/gvc"
                            merchantLower.contains("myntra") -> "https://www.myntra.com"
                            merchantLower.contains("bigbasket") -> "https://www.bigbasket.com"
                            merchantLower.contains("bookmyshow") -> "https://in.bookmyshow.com"
                            merchantLower.contains("decathlon") -> "https://www.decathlon.in"
                            merchantLower.contains("cultfit") -> "https://www.cult.fit"
                            merchantLower.contains("gaana") -> "https://www.gaana.com"
                            merchantLower.contains("polarbear") -> "https://polarbear.co.in"
                            else -> "https://www.google.com/search?q=" + android.net.Uri.encode("${card.merchantName} redeem gift card")
                        }
                        val webIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(webUrl)).apply {
                            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        try {
                            context.startActivity(webIntent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Unable to open browser for ${card.merchantName}", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    tokens: LiquidGlassTokens = currentGlassTokens()
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        LiquidGlassSurface(
            shape = RoundedCornerShape(14.dp),
            elevation = 2.dp,
            surfaceBrush = tokens.surfaceGradient,
            borderBrush = tokens.borderGradient,
            borderWidth = 1.dp,
            tokens = tokens,
            modifier = Modifier.size(46.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = tokens.primaryAccent,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            color = tokens.textPrimary
        )
    }
}

@Composable
fun BalanceSection(
    card: GiftCard,
    isPrivacyMode: Boolean = false,
    onRedeemClick: () -> Unit,
    onAddFundsClick: () -> Unit,
    tokens: LiquidGlassTokens = currentGlassTokens()
) {
    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        tokens = tokens
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Text(
                text = "Current Balance",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = tokens.textSecondary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isPrivacyMode) "${card.currency} ••••" else "${card.currency} ${card.balance}",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    color = tokens.textPrimary
                )

                LiquidGlassSurface(
                    shape = RoundedCornerShape(8.dp),
                    surfaceBrush = tokens.surfaceSubtle.let { Brush.linearGradient(listOf(it, it)) },
                    borderBrush = tokens.borderGradient,
                    borderWidth = 1.dp,
                    tokens = tokens
                ) {
                    val statusColor = when (card.status) {
                        CardStatus.ACTIVE -> Color(0xFF10B981)
                        CardStatus.USED -> tokens.textTertiary
                        CardStatus.EXPIRED -> Color(0xFFEF4444)
                        CardStatus.ARCHIVED -> tokens.primaryAccent
                    }
                    Text(
                        text = card.status.value.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { (100f - card.usagePercentage) / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = tokens.primaryAccent,
                trackColor = tokens.surfaceSubtle
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Initial: ${card.currency}${if (isPrivacyMode) "••••" else card.initialValue}",
                    style = MaterialTheme.typography.labelSmall,
                    color = tokens.textTertiary
                )
                Text(
                    text = "Used: ${card.usagePercentage.toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = tokens.textTertiary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LiquidGlassButton(
                    onClick = onRedeemClick,
                    text = "Redeem",
                    icon = Icons.Rounded.Remove,
                    modifier = Modifier.weight(1f),
                    height = 44.dp,
                    style = GlassButtonStyle.PRIMARY,
                    tokens = tokens
                )

                LiquidGlassButton(
                    onClick = onAddFundsClick,
                    text = "Add Funds",
                    icon = Icons.Rounded.Add,
                    modifier = Modifier.weight(1f),
                    height = 44.dp,
                    style = GlassButtonStyle.SECONDARY,
                    tokens = tokens
                )
            }
        }
    }
}

@Composable
fun CardInfoSection(
    card: GiftCard,
    linkedBankCard: BankCard? = null,
    isRevealed: Boolean,
    onRequestReveal: () -> Unit,
    tokens: LiquidGlassTokens = currentGlassTokens()
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val haptic = LocalHapticFeedback.current

    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        tokens = tokens
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Card Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = tokens.textPrimary
            )

            if (linkedBankCard != null) {
                InfoRow(
                    label = "Linked Bank Card",
                    value = "${linkedBankCard.cardNickname} (${linkedBankCard.network.displayName} •• ${linkedBankCard.lastFourDigits})",
                    icon = Icons.Rounded.AccountBalance,
                    tokens = tokens
                )
            }

            InfoRow(
                label = "Card Number",
                value = if (isRevealed) card.cardNumber else card.maskedCardNumber,
                icon = Icons.Rounded.CreditCard,
                onClick = if (!isRevealed) onRequestReveal else null,
                onCopy = {
                    clipboardManager.setText(AnnotatedString(card.cardNumber))
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    Toast.makeText(context, "${card.merchantName} card number copied", Toast.LENGTH_SHORT).show()
                },
                tokens = tokens
            )

            card.pin?.let { pin ->
                InfoRow(
                    label = "PIN / CVV",
                    value = if (isRevealed) pin else "••••",
                    icon = Icons.Rounded.Lock,
                    onClick = if (!isRevealed) onRequestReveal else null,
                    onCopy = {
                        clipboardManager.setText(AnnotatedString(pin))
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        Toast.makeText(context, "PIN / CVV copied", Toast.LENGTH_SHORT).show()
                    },
                    tokens = tokens
                )
            }

            InfoRow(
                label = "Category",
                value = card.category.displayName,
                icon = Icons.Rounded.Category,
                tokens = tokens
            )

            card.expiryDate?.let { expiry ->
                InfoRow(
                    label = "Expiry Date",
                    value = expiry.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")),
                    icon = Icons.Rounded.Event,
                    isWarning = card.isExpired,
                    tokens = tokens
                )
            }

            if (!card.notes.isNull_or_blank()) {
                InfoRow(
                    label = "Notes",
                    value = card.notes!!,
                    icon = Icons.Rounded.Notes,
                    tokens = tokens
                )
            }
        }
    }
}

private fun String?.isNull_or_blank(): Boolean = this == null || this.trim().isEmpty()

@Composable
fun InfoRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isWarning: Boolean = false,
    onClick: (() -> Unit)? = null,
    onCopy: (() -> Unit)? = null,
    tokens: LiquidGlassTokens = currentGlassTokens()
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LiquidGlassSurface(
            shape = RoundedCornerShape(10.dp),
            surfaceBrush = tokens.surfaceGradient,
            borderBrush = tokens.borderGradient,
            borderWidth = 1.dp,
            tokens = tokens,
            modifier = Modifier.size(34.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isWarning) Color(0xFFEF4444) else tokens.primaryAccent,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = tokens.textSecondary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (isWarning) Color(0xFFEF4444) else tokens.textPrimary
            )
        }
        if (onClick != null) {
            Text(
                text = "Tap to reveal",
                style = MaterialTheme.typography.labelSmall,
                color = tokens.primaryAccent,
                modifier = Modifier.padding(end = 4.dp)
            )
        }
        if (onCopy != null) {
            LiquidGlassIconButton(
                onClick = onCopy,
                icon = Icons.Rounded.ContentCopy,
                contentDescription = "Copy $label",
                size = 32.dp,
                iconSize = 16.dp,
                tokens = tokens
            )
        }
    }
}

@Composable
fun TransactionItem(
    transaction: Transaction,
    currency: String,
    tokens: LiquidGlassTokens = currentGlassTokens()
) {
    LiquidGlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        surfaceBrush = tokens.surfaceGradient,
        borderBrush = tokens.borderGradient,
        borderWidth = 1.dp,
        tokens = tokens
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val iconBg = when (transaction.type) {
                    TransactionType.REDEEM -> Color(0xFFEF4444).copy(alpha = 0.15f)
                    TransactionType.ADD -> Color(0xFF10B981).copy(alpha = 0.15f)
                    TransactionType.ADJUSTMENT -> tokens.primaryAccent.copy(alpha = 0.15f)
                }
                val iconTint = when (transaction.type) {
                    TransactionType.REDEEM -> Color(0xFFEF4444)
                    TransactionType.ADD -> Color(0xFF10B981)
                    TransactionType.ADJUSTMENT -> tokens.primaryAccent
                }
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (transaction.type) {
                            TransactionType.REDEEM -> Icons.Rounded.ArrowDownward
                            TransactionType.ADD -> Icons.Rounded.ArrowUpward
                            TransactionType.ADJUSTMENT -> Icons.Rounded.Sync
                        },
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = transaction.type.value.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = tokens.textPrimary
                    )
                    transaction.note?.let { note ->
                        if (note.isNotBlank()) {
                            Text(
                                text = note,
                                style = MaterialTheme.typography.bodySmall,
                                color = tokens.textSecondary
                            )
                        }
                    }
                }
            }

            Text(
                text = "${if (transaction.type == TransactionType.REDEEM) "-" else "+"}$currency${transaction.amount}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = when (transaction.type) {
                    TransactionType.REDEEM -> Color(0xFFEF4444)
                    TransactionType.ADD -> Color(0xFF10B981)
                    TransactionType.ADJUSTMENT -> tokens.textPrimary
                }
            )
        }
    }
}

@Composable
fun BalanceDialog(
    type: TransactionType,
    onDismiss: () -> Unit,
    onConfirm: (Double, String?) -> Unit,
    tokens: LiquidGlassTokens = currentGlassTokens()
) {
    var amountText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    LiquidGlassDialog(
        onDismissRequest = onDismiss,
        title = if (type == TransactionType.REDEEM) "Redeem Balance" else "Add Funds",
        tokens = tokens,
        confirmButton = {
            LiquidGlassButton(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    if (amount != null && amount > 0) {
                        onConfirm(amount, noteText.ifBlank { null })
                    } else {
                        isError = true
                    }
                },
                text = "Confirm",
                style = GlassButtonStyle.PRIMARY,
                tokens = tokens
            )
        },
        dismissButton = {
            LiquidGlassButton(
                onClick = onDismiss,
                text = "Cancel",
                style = GlassButtonStyle.SECONDARY,
                tokens = tokens
            )
        }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = amountText,
                onValueChange = {
                    amountText = it
                    isError = false
                },
                label = { Text("Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = isError,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                label = { Text("Note (Optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
