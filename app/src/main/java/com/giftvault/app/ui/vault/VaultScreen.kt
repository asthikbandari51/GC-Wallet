package com.giftvault.app.ui.vault

import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.giftvault.app.domain.model.CardCategory
import com.giftvault.app.domain.model.CardStatus
import com.giftvault.app.domain.model.GiftCard
import com.giftvault.app.ui.components.EmptyVaultState
import com.giftvault.app.ui.components.SkeletonCardItem
import com.giftvault.app.ui.components.glass.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun VaultScreen(
    onNavigateToCardDetails: (String) -> Unit,
    onNavigateToAddCard: () -> Unit,
    viewModel: VaultViewModel = hiltViewModel(),
    tokens: LiquidGlassTokens = currentGlassTokens()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var showSortPopup by remember { mutableStateOf(false) }
    var multiDeckCards by remember { mutableStateOf<List<GiftCard>?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LiquidGlassSurface(
                            modifier = Modifier.size(38.dp),
                            shape = RoundedCornerShape(12.dp),
                            elevation = 2.dp,
                            surfaceBrush = tokens.activeBorderGradient,
                            borderWidth = 0.dp,
                            tokens = tokens
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Gift Card Vault",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = tokens.textPrimary
                        )
                    }
                },
                actions = {
                    LiquidGlassIconButton(
                        onClick = onNavigateToAddCard,
                        icon = Icons.Rounded.Add,
                        contentDescription = "Add Card",
                        size = 38.dp,
                        iconSize = 22.dp,
                        tint = tokens.primaryAccent,
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // 1. Total Spendable Balance Liquid Glass Banner (Top Focal Point)
                item {
                    LiquidGlassSurface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        elevation = 8.dp,
                        surfaceBrush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF6B38FB),
                                Color(0xFF584BF6),
                                Color(0xFF483BF0)
                            )
                        ),
                        borderBrush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.5f),
                                Color(0xFFA5B4FC).copy(alpha = 0.3f),
                                Color(0xFF584BF6).copy(alpha = 0.2f)
                            )
                        ),
                        borderWidth = 1.2.dp,
                        tokens = tokens
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "TOTAL SPENDABLE BALANCE",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White.copy(alpha = 0.85f),
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = if (uiState.isPrivacyMode) "INR ••••••" else "INR ${String.format("%.2f", uiState.spendableBalance)}",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(Color.White.copy(alpha = 0.20f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Lock,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. Controls Row Below Spendable Amount: [ Search vouchers (75%, 3f) ]  [ ↕ Sort (25%, 1f) ]
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        LiquidGlassSearchBar(
                            query = uiState.searchQuery,
                            onQueryChange = { viewModel.onSearchQueryChanged(it) },
                            placeholder = "Search vouchers...",
                            tokens = tokens,
                            modifier = Modifier.weight(3f)
                        )

                        // Compact Pill-Sized Sort Control (25% width, 3:1 ratio)
                        Box(modifier = Modifier.weight(1f)) {
                            LiquidGlassButton(
                                onClick = { showSortPopup = true },
                                text = "Sort",
                                icon = Icons.Rounded.SwapVert,
                                style = if (uiState.sortOption != SortOption.RECENTLY_ADDED) GlassButtonStyle.PRIMARY else GlassButtonStyle.SECONDARY,
                                height = 48.dp,
                                fontSize = 12.sp,
                                contentPadding = PaddingValues(horizontal = 6.dp),
                                shape = RoundedCornerShape(999.dp),
                                modifier = Modifier.fillMaxWidth(),
                                tokens = tokens
                            )

                            // Compact Right-Aligned Floating Sort Popover
                            LiquidGlassSortPopup(
                                expanded = showSortPopup,
                                onDismissRequest = { showSortPopup = false },
                                selectedOption = uiState.sortOption,
                                onOptionSelected = { option ->
                                    viewModel.onSortOptionChanged(option)
                                    showSortPopup = false
                                },
                                tokens = tokens
                            )
                        }
                    }
                }

                // Kalyan Gold Stacking Target Calculator
                val kalyanCards = uiState.cards.filter { it.merchantName.contains("Kalyan", ignoreCase = true) }
                if (kalyanCards.isNotEmpty()) {
                    val kalyanTotal = kalyanCards.sumOf { it.balance }
                    val kalyanTargetGoal = 50000.0
                    val progress = (kalyanTotal / kalyanTargetGoal).toFloat().coerceIn(0f, 1f)
                    val percent = (progress * 100).toInt()
                    val exceedsShowroomCap = kalyanCards.size > 10

                    item {
                        LiquidGlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(22.dp),
                            elevation = 4.dp,
                            surfaceBrush = Brush.verticalGradient(
                                colors = listOf(Color(0xFFFFFBEB), Color(0xFFFEF3C7))
                            ),
                            borderBrush = Brush.linearGradient(
                                listOf(Color(0xFFFDE68A), Color(0xFFF59E0B))
                            ),
                            tokens = tokens
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Rounded.Savings, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(22.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                "Kalyan Gold Stacking Goal",
                                                fontWeight = FontWeight.Black,
                                                color = Color(0xFF92400E),
                                                fontSize = 14.sp
                                            )
                                            Text(
                                                "${kalyanCards.size} vouchers stacked • ₹${kalyanTotal.toInt()} / ₹${kalyanTargetGoal.toInt()}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color(0xFFB45309)
                                            )
                                        }
                                    }
                                    Text(
                                        "$percent%",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 16.sp,
                                        color = Color(0xFFD97706)
                                    )
                                }

                                Spacer(Modifier.height(10.dp))
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(CircleShape),
                                    color = Color(0xFFD97706),
                                    trackColor = Color(0xFFFDE68A)
                                )

                                if (exceedsShowroomCap) {
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "⚠️ Stacking Limit Reached: Showroom billing terminals allow a maximum of 10 vouchers per invoice. Current pool contains ${kalyanCards.size} vouchers.",
                                        color = Color(0xFFDC2626),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(Modifier.height(12.dp))
                                LiquidGlassButton(
                                    onClick = { multiDeckCards = kalyanCards },
                                    text = "Present Kalyan Deck (${kalyanCards.size} Vouchers)",
                                    icon = Icons.Rounded.Style,
                                    style = GlassButtonStyle.ACCENT_GOLD,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                    tokens = tokens
                                )
                            }
                        }
                    }
                }

                // Quick Category Scroll Row
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            LiquidGlassChip(
                                selected = uiState.selectedCategory == null && !uiState.showArchivedOnly,
                                onClick = {
                                    if (uiState.showArchivedOnly) viewModel.onArchivedToggled()
                                    viewModel.onCategorySelected(null)
                                },
                                label = "All (${uiState.cards.count { it.status != CardStatus.ARCHIVED }})",
                                tokens = tokens
                            )
                        }
                        val archivedCount = uiState.cards.count { it.status == CardStatus.ARCHIVED }
                        if (archivedCount > 0) {
                            item {
                                LiquidGlassChip(
                                    selected = uiState.showArchivedOnly,
                                    onClick = { viewModel.onArchivedToggled() },
                                    icon = Icons.Rounded.Archive,
                                    label = "Archived ($archivedCount)",
                                    tokens = tokens
                                )
                            }
                        }
                        items(CardCategory.entries.toTypedArray()) { cat ->
                            val count = uiState.cards.count { it.category == cat && it.status != CardStatus.ARCHIVED }
                            if (count > 0) {
                                LiquidGlassChip(
                                    selected = uiState.selectedCategory == cat && !uiState.showArchivedOnly,
                                    onClick = {
                                        if (uiState.showArchivedOnly) viewModel.onArchivedToggled()
                                        viewModel.onCategorySelected(if (uiState.selectedCategory == cat) null else cat)
                                    },
                                    label = "${cat.displayName} ($count)",
                                    tokens = tokens
                                )
                            }
                        }
                    }
                }

                // 3. Vault Card List Rows
                if (uiState.isLoading) {
                    items(4) {
                        SkeletonCardItem()
                    }
                } else if (uiState.cards.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            EmptyVaultState(onAddCardClick = onNavigateToAddCard)
                        }
                    }
                } else if (uiState.filteredCards.isEmpty()) {
                    // Empty Filter/Search Result State
                    item {
                        LiquidGlassCard(
                            shape = RoundedCornerShape(26.dp),
                            elevation = 2.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            tokens = tokens
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.SearchOff,
                                    contentDescription = null,
                                    tint = tokens.textTertiary,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    text = "No such cards available",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = tokens.textPrimary,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "No gift card matched your search query or selected category filter.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = tokens.textSecondary,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(Modifier.height(16.dp))
                                LiquidGlassButton(
                                    onClick = {
                                        viewModel.onSearchQueryChanged("")
                                        viewModel.onCategorySelected(null)
                                    },
                                    text = "Reset Search & Filter",
                                    icon = Icons.Rounded.Refresh,
                                    style = GlassButtonStyle.PRIMARY,
                                    tokens = tokens
                                )
                            }
                        }
                    }
                } else {
                    items(uiState.filteredCards, key = { it.id }) { card ->
                        VaultRowCardItem(
                            card = card,
                            isPrivacyMode = uiState.isPrivacyMode,
                            onClick = { onNavigateToCardDetails(card.id) },
                            onDeleteCard = { viewModel.deleteCard(card.id) },
                            onArchiveCard = { viewModel.archiveCard(card.id) },
                            onUnarchiveCard = { viewModel.unarchiveCard(card.id) },
                            onCopyCode = {
                                clipboardManager.setText(AnnotatedString(card.cardNumber))
                                Toast.makeText(context, "Card code copied!", Toast.LENGTH_SHORT).show()
                            },
                            tokens = tokens
                        )
                    }
                }
            }
        }
    }

    if (multiDeckCards != null) {
        com.giftvault.app.ui.components.MultiCardPresentationDialog(
            cards = multiDeckCards!!,
            onDismiss = { multiDeckCards = null },
            onMarkCardRedeemed = { cardId ->
                viewModel.deleteCard(cardId)
            }
        )
    }
}

@Composable
private fun VaultRowCardItem(
    card: GiftCard,
    isPrivacyMode: Boolean,
    onClick: () -> Unit,
    onDeleteCard: () -> Unit,
    onArchiveCard: () -> Unit,
    onUnarchiveCard: () -> Unit,
    onCopyCode: () -> Unit,
    tokens: LiquidGlassTokens
) {
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }

    val resId = remember(card.templateKey) {
        com.giftvault.app.core.templates.TemplateRegistry.getDrawableResId(context, card.templateKey)
    }

    val formattedCode = remember(card.cardNumber) {
        if (card.cardNumber.length > 4) {
            val last4 = card.cardNumber.takeLast(4)
            val prefix = card.cardNumber.dropLast(4)
            val maskedPrefix = prefix.chunked(4).joinToString(" ")
            "$maskedPrefix $last4"
        } else {
            card.cardNumber
        }
    }

    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        elevation = 2.dp,
        tokens = tokens
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(96.dp)
                    .height(62.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFE2E8F0)),
                contentAlignment = Alignment.Center
            ) {
                if (resId != 0) {
                    AsyncImage(
                        model = resId,
                        contentDescription = card.merchantName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        card.merchantName.take(2).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = tokens.primaryAccent
                    )
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = card.merchantName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = tokens.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (card.status == CardStatus.ARCHIVED) {
                        Spacer(Modifier.width(6.dp))
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF94A3B8)
                        ) {
                            Text(
                                text = "ARCHIVED",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(2.dp))

                Text(
                    text = formattedCode,
                    style = MaterialTheme.typography.bodySmall,
                    color = tokens.textSecondary,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )

                Spacer(Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isPrivacyMode) "₹ ••••" else "INR ${card.balance.toInt()}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black,
                        color = if (card.status == CardStatus.ARCHIVED) tokens.textTertiary else tokens.primaryAccent
                    )

                    if (card.expiryDate != null) {
                        Text(
                            text = card.expiryDate.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = tokens.textTertiary
                        )
                    }
                }
            }

            Box {
                LiquidGlassIconButton(
                    onClick = { showMenu = true },
                    icon = Icons.Rounded.MoreVert,
                    contentDescription = "Options",
                    size = 32.dp,
                    iconSize = 18.dp,
                    tint = tokens.textSecondary,
                    elevation = 0.dp,
                    tokens = tokens
                )

                LiquidGlassCardMenuPopup(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    card = card,
                    onViewDetails = onClick,
                    onCopyCode = onCopyCode,
                    onArchiveCard = onArchiveCard,
                    onUnarchiveCard = onUnarchiveCard,
                    onDeleteCard = onDeleteCard,
                    tokens = tokens
                )
            }
        }
    }
}

@Composable
private fun LiquidGlassCardMenuPopup(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    card: GiftCard,
    onViewDetails: () -> Unit,
    onCopyCode: () -> Unit,
    onArchiveCard: () -> Unit,
    onUnarchiveCard: () -> Unit,
    onDeleteCard: () -> Unit,
    tokens: LiquidGlassTokens
) {
    if (!expanded) return

    Popup(
        alignment = Alignment.TopEnd,
        offset = IntOffset(x = 0, y = 36),
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(
            focusable = true,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        LiquidGlassSurface(
            modifier = Modifier
                .width(200.dp)
                .clip(RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp),
            elevation = 14.dp,
            surfaceBrush = if (tokens.isDark) tokens.elevatedGradient else tokens.surfaceGradient,
            borderBrush = tokens.activeBorderGradient,
            borderWidth = 1.2.dp,
            tokens = tokens
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                MenuGlassItem(
                    text = "View Details",
                    icon = Icons.Rounded.Visibility,
                    tint = tokens.primaryAccent,
                    tokens = tokens,
                    onClick = {
                        onDismissRequest()
                        onViewDetails()
                    }
                )

                MenuGlassItem(
                    text = "Copy Card Code",
                    icon = Icons.Rounded.ContentCopy,
                    tint = tokens.textPrimary,
                    tokens = tokens,
                    onClick = {
                        onDismissRequest()
                        onCopyCode()
                    }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    color = tokens.border.copy(alpha = 0.35f)
                )

                if (card.status == CardStatus.ARCHIVED) {
                    MenuGlassItem(
                        text = "Restore Card",
                        icon = Icons.Rounded.Unarchive,
                        tint = tokens.primaryAccent,
                        tokens = tokens,
                        onClick = {
                            onDismissRequest()
                            onUnarchiveCard()
                        }
                    )
                    MenuGlassItem(
                        text = "Delete Forever",
                        icon = Icons.Rounded.DeleteForever,
                        tint = Color(0xFFEF4444),
                        tokens = tokens,
                        onClick = {
                            onDismissRequest()
                            onDeleteCard()
                        }
                    )
                } else {
                    MenuGlassItem(
                        text = "Archive Card",
                        icon = Icons.Rounded.Archive,
                        tint = tokens.textSecondary,
                        tokens = tokens,
                        onClick = {
                            onDismissRequest()
                            onArchiveCard()
                        }
                    )
                    MenuGlassItem(
                        text = "Delete Card",
                        icon = Icons.Rounded.Delete,
                        tint = Color(0xFFEF4444),
                        tokens = tokens,
                        onClick = {
                            onDismissRequest()
                            onDeleteCard()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MenuGlassItem(
    text: String,
    icon: ImageVector,
    tint: Color,
    tokens: LiquidGlassTokens,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = if (tint == Color(0xFFEF4444)) tint else tokens.textPrimary
        )
    }
}
