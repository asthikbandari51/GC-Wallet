package com.giftvault.app.ui.bankcards

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.giftvault.app.domain.model.BankCard
import com.giftvault.app.domain.model.BankCardType
import com.giftvault.app.domain.model.CardNetwork
import com.giftvault.app.domain.model.CardTier
import com.giftvault.app.ui.components.glass.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageBankCardsScreen(
    onNavigateBack: () -> Unit,
    viewModel: ManageBankCardsViewModel = hiltViewModel(),
    tokens: LiquidGlassTokens = currentGlassTokens()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Bank Cards",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = tokens.textPrimary
                        )
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = tokens.primaryAccent.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "${uiState.bankCards.size}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = tokens.primaryAccent,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
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
                    LiquidGlassIconButton(
                        onClick = { viewModel.openAddDialog() },
                        icon = Icons.Rounded.Add,
                        contentDescription = "Add Card",
                        size = 38.dp,
                        iconSize = 22.dp,
                        tint = tokens.primaryAccent,
                        tokens = tokens,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        floatingActionButton = {
            LiquidGlassAddButton(
                onClick = { viewModel.openAddDialog() },
                contentDescription = "Add Bank Card",
                tokens = tokens
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 18.dp)
        ) {
            // Floating Liquid Glass Search Field
            LiquidGlassSearchBar(
                query = uiState.searchQuery,
                onQueryChange = { viewModel.onSearchQueryChanged(it) },
                placeholder = "Search by bank, nickname, or digits...",
                tokens = tokens,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Hierarchical Filter Pills: Level 1 (Card Type: All, Credit, Debit)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = if (uiState.typeFilter != BankCardTypeFilter.ALL) 8.dp else 14.dp)
            ) {
                BankCardTypeFilter.entries.forEach { filter ->
                    val isSelected = uiState.typeFilter == filter
                    LiquidGlassChip(
                        selected = isSelected,
                        onClick = { viewModel.onTypeFilterChanged(filter) },
                        label = filter.displayName,
                        tokens = tokens
                    )
                }
            }

            // Level 2 Sub-Filter: Appears only when Credit or Debit is selected (All Networks, RuPay, Visa, Mastercard, Amex)
            AnimatedVisibility(visible = uiState.typeFilter != BankCardTypeFilter.ALL) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                ) {
                    items(BankCardNetworkFilter.entries.toTypedArray()) { netFilter ->
                        val isNetSelected = uiState.networkFilter == netFilter
                        LiquidGlassChip(
                            selected = isNetSelected,
                            onClick = { viewModel.onNetworkFilterChanged(netFilter) },
                            label = netFilter.displayName,
                            tokens = tokens
                        )
                    }
                }
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = tokens.primaryAccent)
                }
            } else if (uiState.filteredCards.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LiquidGlassCard(
                        shape = RoundedCornerShape(28.dp),
                        elevation = 2.dp,
                        modifier = Modifier.fillMaxWidth(),
                        tokens = tokens
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(tokens.primaryAccent.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Rounded.CreditCard,
                                    contentDescription = null,
                                    tint = tokens.primaryAccent,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Text(
                                text = if (uiState.bankCards.isEmpty()) "No Bank Cards Added" else "No Matching Cards",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = tokens.textPrimary
                            )
                            Text(
                                text = if (uiState.bankCards.isEmpty())
                                    "Add your RuPay Select, Visa Infinite, Mastercard World or PNB Imperial cards with hardware-backed encryption."
                                else
                                    "Try clearing your search query or reset the filter chip.",
                                style = MaterialTheme.typography.bodySmall,
                                color = tokens.textSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            if (uiState.bankCards.isEmpty()) {
                                Spacer(Modifier.height(6.dp))
                                LiquidGlassButton(
                                    onClick = { viewModel.openAddDialog() },
                                    text = "Add Bank Card",
                                    icon = Icons.Rounded.Add,
                                    style = GlassButtonStyle.PRIMARY,
                                    tokens = tokens
                                )
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    items(uiState.filteredCards, key = { it.id }) { card ->
                        val isRevealed = uiState.revealedCardIds.contains(card.id)
                        val isCvvRevealed = uiState.revealedCvvCardIds.contains(card.id)
                        LiquidBankCardItem(
                            card = card,
                            isRevealed = isRevealed,
                            isCvvRevealed = isCvvRevealed,
                            isPrivacyMode = uiState.isPrivacyMode,
                            hideBankCardCvv = uiState.hideBankCardCvv,
                            requireAuthForCvv = uiState.requireAuthForCvv,
                            onToggleReveal = { viewModel.toggleCardDetailsReveal(card.id) },
                            onToggleCvvReveal = { viewModel.toggleCvvReveal(card.id) },
                            onEdit = { viewModel.openEditDialog(card) },
                            onDelete = { viewModel.deleteBankCard(card.id) },
                            onSetPrimary = { viewModel.setAsPrimary(card.id) },
                            tokens = tokens
                        )
                    }
                }
            }
        }
    }

    if (uiState.isFormOpen) {
        BankCardFormDialog(
            uiState = uiState,
            viewModel = viewModel,
            tokens = tokens
        )
    }
}

@Composable
private fun LiquidBankCardItem(
    card: BankCard,
    isRevealed: Boolean,
    isCvvRevealed: Boolean,
    isPrivacyMode: Boolean,
    hideBankCardCvv: Boolean,
    requireAuthForCvv: Boolean,
    onToggleReveal: () -> Unit,
    onToggleCvvReveal: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSetPrimary: () -> Unit,
    tokens: LiquidGlassTokens
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val displayedNumber = if (isRevealed) {
        card.cardNumber.chunked(4).joinToString(" ")
    } else {
        card.maskedDisplayNumber
    }

    val isCvvShown = if (hideBankCardCvv) isCvvRevealed else isRevealed
    val displayedCvv = if (isCvvShown) card.cvv else "•••"

    val handleCvvAction: (() -> Unit) -> Unit = { action ->
        if (!isCvvShown && requireAuthForCvv) {
            val activity = context as? androidx.fragment.app.FragmentActivity
            if (activity != null) {
                com.giftvault.app.core.security.BiometricAuthManager().authenticate(
                    activity = activity,
                    title = "Authenticate to reveal CVV",
                    subtitle = "Verify credential to unlock card security code",
                    onSuccess = { action() },
                    onError = { Toast.makeText(context, "Authentication required for CVV", Toast.LENGTH_SHORT).show() }
                )
            } else {
                action()
            }
        } else {
            action()
        }
    }

    LiquidGlassCard(
        shape = RoundedCornerShape(24.dp),
        elevation = if (card.isPrimary) 6.dp else 2.dp,
        borderBrush = if (card.isPrimary) tokens.activeBorderGradient else tokens.borderGradient,
        borderWidth = if (card.isPrimary) 1.5.dp else 1.dp,
        modifier = Modifier.fillMaxWidth(),
        tokens = tokens
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header Row: Bank Badge, Title, Debit/Credit pill, Primary Pill, Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(tokens.primaryAccent, Color(0xFF7C3AED))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = card.bankName.take(3).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = card.cardNickname,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = tokens.textPrimary
                            )
                            Spacer(Modifier.width(6.dp))
                            // Card Type Pill: CREDIT vs DEBIT
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (card.cardType == BankCardType.CREDIT) tokens.primaryAccent.copy(alpha = 0.12f) else Color(0x1F10B981)
                            ) {
                                Text(
                                    card.cardType.displayName.uppercase(),
                                    color = if (card.cardType == BankCardType.CREDIT) tokens.primaryAccent else Color(0xFF059669),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 9.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            if (card.isPrimary) {
                                Spacer(Modifier.width(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = tokens.primaryAccent.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        "PRIMARY",
                                        color = tokens.primaryAccent,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 9.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = "${card.network.displayName} • ${card.tier.displayName}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = tokens.primaryAccent
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    LiquidGlassIconButton(
                        onClick = onToggleReveal,
                        icon = if (isRevealed) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                        contentDescription = "Toggle Reveal",
                        size = 32.dp,
                        iconSize = 16.dp,
                        tint = tokens.primaryAccent,
                        tokens = tokens
                    )
                    Spacer(Modifier.width(6.dp))
                    LiquidGlassIconButton(
                        onClick = onEdit,
                        icon = Icons.Rounded.Edit,
                        contentDescription = "Edit",
                        size = 32.dp,
                        iconSize = 16.dp,
                        tint = tokens.textSecondary,
                        tokens = tokens
                    )
                    Spacer(Modifier.width(6.dp))
                    LiquidGlassIconButton(
                        onClick = onDelete,
                        icon = Icons.Rounded.Delete,
                        contentDescription = "Delete",
                        size = 32.dp,
                        iconSize = 16.dp,
                        tint = Color(0xFFEF4444),
                        tokens = tokens
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // Realistic Physical Card Visual Panel
            LiquidGlassSurface(
                shape = RoundedCornerShape(18.dp),
                elevation = 4.dp,
                surfaceBrush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF1E293B),
                        Color(0xFF0F172A)
                    )
                ),
                borderBrush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.25f),
                        Color.White.copy(alpha = 0.05f)
                    )
                ),
                modifier = Modifier.fillMaxWidth(),
                tokens = tokens
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Card Number & 1-Tap Copy
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = displayedNumber,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            ),
                            color = Color.White
                        )
                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(card.cardNumber))
                                Toast.makeText(context, "Card number copied", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Rounded.ContentCopy,
                                contentDescription = "Copy Card Number",
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Cardholder & Expiry / CVV Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "CARDHOLDER",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 9.sp
                            )
                            Text(
                                card.cardholderName.ifBlank { "VALUED CARDHOLDER" }.uppercase(),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "EXPIRES",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 9.sp
                                )
                                Text(
                                    "${card.expiryMonth.padStart(2, '0')}/${card.expiryYear.takeLast(2)}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            if (card.cvv.isNotBlank()) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.clickable {
                                        handleCvvAction { onToggleCvvReveal() }
                                    }
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            "CVV",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.5f),
                                            fontSize = 9.sp
                                        )
                                        Spacer(Modifier.width(2.dp))
                                        Icon(
                                            if (isCvvShown) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                            contentDescription = "Toggle CVV",
                                            tint = Color.White.copy(alpha = 0.5f),
                                            modifier = Modifier.size(10.dp)
                                        )
                                    }
                                    Text(
                                        displayedCvv,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFFD700)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Spend Milestone Tracker: ONLY SHOW IF CARD HAS SPEND TARGET > 0
            if (card.hasSpendTarget) {
                Spacer(Modifier.height(14.dp))
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Quarterly Milestone (${card.quarterlyPercentage}%)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = tokens.textSecondary
                        )
                        Text(
                            if (isPrivacyMode) "₹•••• / ₹••••" else "₹${card.currentQuarterSpend.toInt()} / ₹${card.quarterlySpendTarget.toInt()}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = tokens.primaryAccent
                        )
                    }

                    Spacer(Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { card.quarterlyPercentage / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = if (card.quarterlyPercentage >= 100f) Color(0xFF10B981) else tokens.primaryAccent,
                        trackColor = if (tokens.isDark) Color(0x332A2D5C) else Color(0xFFEEEDFF)
                    )
                }
            }

            // Set as Primary Action if not already
            if (!card.isPrimary) {
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .clickable(onClick = onSetPrimary)
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Star, contentDescription = null, modifier = Modifier.size(16.dp), tint = tokens.primaryAccent)
                    Spacer(Modifier.width(4.dp))
                    Text("Set as Primary Card Profile", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = tokens.primaryAccent)
                }
            }
        }
    }
}

@Composable
private fun BankCardFormDialog(
    uiState: ManageBankCardsUiState,
    viewModel: ManageBankCardsViewModel,
    tokens: LiquidGlassTokens
) {
    val banks = listOf("PNB", "HDFC", "SBI", "ICICI", "Axis", "Bank of Baroda", "Kotak", "Other")
    val availableTiers = remember(uiState.networkInput) {
        CardTier.getTiersForNetwork(uiState.networkInput)
    }

    LiquidGlassDialog(
        onDismissRequest = { viewModel.closeDialog() },
        title = if (uiState.editingCard != null) "Edit Bank Card" else "Add Bank Card Details",
        tokens = tokens,
        confirmButton = {
            LiquidGlassButton(
                onClick = { viewModel.saveBankCard() },
                text = "Save Card",
                icon = Icons.Rounded.Check,
                style = GlassButtonStyle.PRIMARY,
                tokens = tokens
            )
        },
        dismissButton = {
            LiquidGlassButton(
                onClick = { viewModel.closeDialog() },
                text = "Cancel",
                style = GlassButtonStyle.SUBTLE,
                tokens = tokens
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (uiState.errorMessage != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0x22EF4444),
                    border = BorderStroke(1.dp, Color(0xFFEF4444)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = uiState.errorMessage,
                        color = Color(0xFFEF4444),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            // Live Updating Physical Card Preview
            LiquidGlassSurface(
                shape = RoundedCornerShape(18.dp),
                elevation = 6.dp,
                surfaceBrush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF1E293B),
                        Color(0xFF0F172A)
                    )
                ),
                borderBrush = tokens.activeBorderGradient,
                borderWidth = 1.2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                tokens = tokens
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = (uiState.bankNameInput.ifBlank { "BANK" }).uppercase(),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color.White.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = uiState.cardTypeInput.displayName.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = tokens.primaryAccent.copy(alpha = 0.35f)
                            ) {
                                Text(
                                    text = "${uiState.networkInput.displayName} ${uiState.tierInput.displayName}".trim(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    val formattedLiveNumber = remember(uiState.cardNumberInput) {
                        val raw = uiState.cardNumberInput.filter { it.isDigit() }
                        if (raw.isBlank()) "•••• •••• •••• ••••"
                        else raw.chunked(4).joinToString(" ").padEnd(19, '•')
                    }
                    Text(
                        text = formattedLiveNumber,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        ),
                        color = Color.White
                    )

                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "CARDHOLDER",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 9.sp
                            )
                            Text(
                                uiState.cardholderNameInput.ifBlank { "VALUED CARDHOLDER" }.uppercase(),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "EXPIRES",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 9.sp
                                )
                                Text(
                                    "${uiState.expiryMonthInput.ifBlank { "MM" }.padStart(2, '0')}/${uiState.expiryYearInput.ifBlank { "YY" }.takeLast(2)}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            if (uiState.cvvInput.isNotBlank()) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        "CVV",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.5f),
                                        fontSize = 9.sp
                                    )
                                    Text(
                                        "•••",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFFD700)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            val glassTextFieldColors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = if (tokens.isDark) Color(0x1AFFFFFF) else Color(0x0D000000),
                unfocusedContainerColor = if (tokens.isDark) Color(0x0DFFFFFF) else Color(0x05000000),
                focusedBorderColor = tokens.primaryAccent,
                unfocusedBorderColor = tokens.borderDefault,
                focusedLabelColor = tokens.primaryAccent,
                unfocusedLabelColor = tokens.textSecondary,
                focusedTextColor = tokens.textPrimary,
                unfocusedTextColor = tokens.textPrimary
            )

            // GROUP 1: CARD IDENTITY
            Text("Card Identity", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = tokens.textPrimary)

            // Nickname
            OutlinedTextField(
                value = uiState.nicknameInput,
                onValueChange = { viewModel.onNicknameChanged(it) },
                label = { Text("Card Nickname *") },
                placeholder = { Text("e.g. PNB Select Imperial", color = tokens.textTertiary) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = glassTextFieldColors,
                modifier = Modifier.fillMaxWidth()
            )

            // Card Type Selector: CREDIT vs DEBIT
            Text("Card Type", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = tokens.textPrimary)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BankCardType.entries.forEach { type ->
                    LiquidGlassChip(
                        selected = uiState.cardTypeInput == type,
                        onClick = { viewModel.onCardTypeChanged(type) },
                        label = type.displayName,
                        tokens = tokens
                    )
                }
            }

            // Bank Name Selector
            Text("Bank Name", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = tokens.textPrimary)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(banks) { bank ->
                    LiquidGlassChip(
                        selected = uiState.bankNameInput == bank,
                        onClick = { viewModel.onBankNameChanged(bank) },
                        label = bank,
                        tokens = tokens
                    )
                }
            }

            // Network Selector
            Text("Card Network", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = tokens.textPrimary)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(CardNetwork.entries.toTypedArray()) { net ->
                    LiquidGlassChip(
                        selected = uiState.networkInput == net,
                        onClick = { viewModel.onNetworkChanged(net) },
                        label = net.displayName,
                        tokens = tokens
                    )
                }
            }

            // Network Variants / Tiers
            Text(
                "${uiState.networkInput.displayName} Variant / Tier",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = tokens.textPrimary
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(availableTiers) { tier ->
                    LiquidGlassChip(
                        selected = uiState.tierInput == tier,
                        onClick = { viewModel.onTierChanged(tier) },
                        label = tier.displayName,
                        tokens = tokens
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            // GROUP 2: CARD DETAILS
            Text("Card Details", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = tokens.textPrimary)

            // Full 16-digit Card Number
            OutlinedTextField(
                value = uiState.cardNumberInput,
                onValueChange = { viewModel.onCardNumberChanged(it) },
                label = { Text("Full Card Number (16 Digits) *") },
                placeholder = { Text("•••• •••• •••• ••••", color = tokens.textTertiary) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = glassTextFieldColors,
                modifier = Modifier.fillMaxWidth()
            )

            // Expiry Month/Year & CVV Row
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = uiState.expiryMonthInput,
                    onValueChange = { viewModel.onExpiryMonthChanged(it) },
                    label = { Text("MM") },
                    placeholder = { Text("MM", color = tokens.textTertiary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = glassTextFieldColors,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = uiState.expiryYearInput,
                    onValueChange = { viewModel.onExpiryYearChanged(it) },
                    label = { Text("YY") },
                    placeholder = { Text("YY", color = tokens.textTertiary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = glassTextFieldColors,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = uiState.cvvInput,
                    onValueChange = { viewModel.onCvvChanged(it) },
                    label = { Text("CVV") },
                    placeholder = { Text("CVV", color = tokens.textTertiary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = glassTextFieldColors,
                    modifier = Modifier.weight(1f)
                )
            }

            // Cardholder Name
            OutlinedTextField(
                value = uiState.cardholderNameInput,
                onValueChange = { viewModel.onCardholderNameChanged(it) },
                label = { Text("Cardholder Name") },
                placeholder = { Text("Name on Card", color = tokens.textTertiary) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = glassTextFieldColors,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(4.dp))

            // GROUP 3: OPTIONAL SPEND LIMIT & PREFERENCES
            Text("Spending Target & Preferences", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = tokens.textPrimary)

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = uiState.currentSpendInput,
                    onValueChange = { viewModel.onCurrentSpendChanged(it) },
                    label = { Text("Current Spend (₹)") },
                    placeholder = { Text("0", color = tokens.textTertiary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = glassTextFieldColors,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = uiState.spendTargetInput,
                    onValueChange = { viewModel.onSpendTargetChanged(it) },
                    label = { Text("Quarterly Spend Limit (₹)") },
                    placeholder = { Text("0 (No Target)", color = tokens.textTertiary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = glassTextFieldColors,
                    modifier = Modifier.weight(1.3f)
                )
            }

            // Primary Checkbox
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { viewModel.onIsPrimaryChanged(!uiState.isPrimaryInput) }
                    .padding(vertical = 4.dp)
            ) {
                Checkbox(
                    checked = uiState.isPrimaryInput,
                    onCheckedChange = { viewModel.onIsPrimaryChanged(it) }
                )
                Text("Set as Primary Card Profile", style = MaterialTheme.typography.bodyMedium, color = tokens.textPrimary)
            }
        }
    }
}
