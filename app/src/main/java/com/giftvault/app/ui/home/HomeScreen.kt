package com.giftvault.app.ui.home

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.giftvault.app.domain.model.BankCard
import com.giftvault.app.ui.components.GiftCardVisual
import com.giftvault.app.ui.components.glass.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToAddCard: () -> Unit,
    onNavigateToCardDetails: (String) -> Unit,
    onNavigateToVault: () -> Unit,
    onNavigateToManageCards: () -> Unit = {},
    onNavigateToRuPayRedeem: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
    tokens: LiquidGlassTokens = currentGlassTokens()
) {
    val uiState by viewModel.uiState.collectAsState()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.onResume()
    }

    Scaffold(
        floatingActionButton = {
            LiquidGlassAddButton(
                onClick = onNavigateToAddCard,
                contentDescription = "Add Card",
                tokens = tokens
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = tokens.primaryAccent)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // 1. Premium Compact Header
                item {
                    val greetingName = uiState.userName?.trim()?.ifBlank { "there" } ?: "there"
                    val initialLetter = greetingName.firstOrNull()?.uppercaseChar()?.toString() ?: "U"

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Avatar Icon with soft glass halo
                            LiquidGlassSurface(
                                modifier = Modifier.size(44.dp),
                                shape = CircleShape,
                                elevation = 2.dp,
                                surfaceBrush = Brush.linearGradient(
                                    colors = listOf(tokens.primaryAccent, Color(0xFF7C3AED))
                                ),
                                borderWidth = 1.dp,
                                tokens = tokens
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = initialLetter,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                }
                            }

                            Spacer(Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = "Hey, $greetingName! 👋",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = tokens.textPrimary
                                )
                                Text(
                                    text = "Vault Protected • AES-256",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = tokens.textTertiary
                                )
                            }
                        }

                        // Privacy Masking Toggle Action
                        LiquidGlassIconButton(
                            onClick = { viewModel.togglePrivacyMode() },
                            icon = if (uiState.isPrivacyMode) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                            contentDescription = "Toggle Privacy Masking",
                            size = 38.dp,
                            iconSize = 18.dp,
                            tint = if (uiState.isPrivacyMode) tokens.primaryAccent else tokens.textSecondary,
                            tokens = tokens
                        )
                    }
                }

                // 2. Smart Ingestion Detection Pill (Compact Search-Bar-Sized Liquid Glass Action Bar)
                if (uiState.detectedVoucher != null) {
                    item {
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(animationSpec = tween(200)) + expandVertically(animationSpec = tween(200)),
                            exit = fadeOut(animationSpec = tween(150)) + shrinkVertically(animationSpec = tween(150))
                        ) {
                            uiState.detectedVoucher?.let { detected ->
                                LiquidGlassSurface(
                                    shape = RoundedCornerShape(999.dp),
                                    elevation = 4.dp,
                                    borderBrush = tokens.activeBorderGradient,
                                    borderWidth = 1.dp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(46.dp),
                                    tokens = tokens
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.AutoAwesome,
                                                contentDescription = null,
                                                tint = tokens.primaryAccent,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                text = "Coupon detected",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                color = tokens.primaryAccent
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                text = detected.displayText,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = tokens.textPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            LiquidGlassIconButton(
                                                onClick = { viewModel.ingestDetectedVoucher() },
                                                icon = Icons.Rounded.Add,
                                                contentDescription = "Save Coupon",
                                                size = 32.dp,
                                                iconSize = 16.dp,
                                                tint = Color.White,
                                                tokens = tokens
                                            )
                                            LiquidGlassIconButton(
                                                onClick = { viewModel.dismissDetectedVoucher() },
                                                icon = Icons.Rounded.Close,
                                                contentDescription = "Dismiss",
                                                size = 32.dp,
                                                iconSize = 14.dp,
                                                tint = tokens.textTertiary,
                                                tokens = tokens
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. Bank Card Profile Carousel
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Bank Card Profiles",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = tokens.textPrimary
                            )
                            TextButton(onClick = onNavigateToManageCards) {
                                Icon(Icons.Rounded.Tune, contentDescription = null, modifier = Modifier.size(16.dp), tint = tokens.primaryAccent)
                                Spacer(Modifier.width(4.dp))
                                Text("Manage", color = tokens.primaryAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // "All Profiles" Pill
                            item {
                                val isAllSelected = uiState.activeBankCardId == null
                                LiquidGlassCard(
                                    onClick = { viewModel.selectBankCard(null) },
                                    isSelected = isAllSelected,
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = if (isAllSelected) 4.dp else 1.dp,
                                    modifier = Modifier.height(72.dp),
                                    tokens = tokens
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(if (isAllSelected) tokens.primaryAccent else tokens.surfaceSubtle),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Rounded.CreditCard,
                                                contentDescription = null,
                                                tint = if (isAllSelected) Color.White else tokens.textSecondary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "All Profiles",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = if (isAllSelected) tokens.primaryAccent else tokens.textPrimary
                                            )
                                            Text(
                                                text = "${uiState.bankCards.size} cards linked",
                                                fontSize = 11.sp,
                                                color = tokens.textTertiary
                                            )
                                        }
                                    }
                                }
                            }

                            // Dynamic Bank Card Items
                            items(uiState.bankCards, key = { it.id }) { card ->
                                val isSelected = uiState.activeBankCardId == card.id

                                LiquidGlassCard(
                                    onClick = { viewModel.selectBankCard(card.id) },
                                    isSelected = isSelected,
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = if (isSelected) 4.dp else 1.dp,
                                    modifier = Modifier.height(72.dp),
                                    tokens = tokens
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (isSelected) {
                                                        Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF4F46E5)))
                                                    } else {
                                                        Brush.linearGradient(listOf(Color(0xFF1E293B), Color(0xFF0F172A)))
                                                    }
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = card.bankName.take(2).uppercase(),
                                                color = Color.White,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 11.sp
                                            )
                                        }

                                        Spacer(Modifier.width(10.dp))

                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = card.cardNickname,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = if (isSelected) tokens.primaryAccent else tokens.textPrimary,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Spacer(Modifier.width(6.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = if (isSelected) tokens.primaryAccent.copy(alpha = 0.15f) else Color(0x1F0F172A)
                                                ) {
                                                    Text(
                                                        text = card.cardType.name,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = if (isSelected) tokens.primaryAccent else tokens.textTertiary,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }

                                            Text(
                                                text = "${card.network.displayName} • ${card.lastFourDigits}",
                                                fontSize = 11.sp,
                                                color = tokens.textTertiary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. Main Balance Hero (Liquid Glass Focal Point)
                item {
                    LiquidGlassSurface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToVault() },
                        shape = RoundedCornerShape(30.dp),
                        elevation = 8.dp,
                        surfaceBrush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF6366F1),
                                Color(0xFF4F46E5),
                                Color(0xFF4338CA)
                            )
                        ),
                        borderBrush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFFFFFFF).copy(alpha = 0.5f),
                                Color(0xFFA5B4FC).copy(alpha = 0.3f),
                                Color(0xFF6366F1).copy(alpha = 0.2f)
                            )
                        ),
                        borderWidth = 1.2.dp,
                        tokens = tokens
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(22.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (uiState.activeBankCard != null) "${uiState.activeBankCard?.cardNickname} Balance" else "Total Spendable Vault",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White.copy(alpha = 0.90f),
                                    fontWeight = FontWeight.SemiBold
                                )

                                if (uiState.activeBankCard != null) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color.White.copy(alpha = 0.20f)
                                    ) {
                                        Text(
                                            text = "${uiState.activeBankCard?.network?.displayName} ${uiState.activeBankCard?.tier?.displayName}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(8.dp))

                            Text(
                                text = if (uiState.isPrivacyMode) "₹ ••••••" else "₹${String.format("%,.2f", uiState.totalBalance)}",
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )

                            Spacer(Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White.copy(alpha = 0.22f)
                                ) {
                                    Text(
                                        text = "${uiState.totalCards} Active Vouchers",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "View Vault",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Icon(
                                        Icons.Rounded.ChevronRight,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 5. Tactile Quick Actions (RuPay Rewards, + Voucher, Vault)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LiquidGlassButton(
                            onClick = onNavigateToRuPayRedeem,
                            modifier = Modifier.weight(1f),
                            text = "RuPay",
                            icon = Icons.Rounded.WorkspacePremium,
                            style = GlassButtonStyle.ACCENT_GOLD,
                            height = 46.dp,
                            fontSize = 12.sp,
                            contentPadding = PaddingValues(horizontal = 6.dp),
                            shape = RoundedCornerShape(16.dp),
                            tokens = tokens
                        )

                        LiquidGlassButton(
                            onClick = onNavigateToAddCard,
                            modifier = Modifier.weight(1f),
                            text = "Cards",
                            icon = Icons.Rounded.CreditCard,
                            style = GlassButtonStyle.PRIMARY,
                            height = 46.dp,
                            fontSize = 12.sp,
                            contentPadding = PaddingValues(horizontal = 6.dp),
                            shape = RoundedCornerShape(16.dp),
                            tokens = tokens
                        )

                        LiquidGlassButton(
                            onClick = onNavigateToVault,
                            modifier = Modifier.weight(1f),
                            text = "Vault",
                            icon = Icons.Rounded.AccountBalanceWallet,
                            style = GlassButtonStyle.SECONDARY,
                            height = 46.dp,
                            fontSize = 12.sp,
                            contentPadding = PaddingValues(horizontal = 6.dp),
                            shape = RoundedCornerShape(16.dp),
                            tokens = tokens
                        )
                    }
                }

                // 6. Quarterly Spend Milestone Tracker (Conditionally Shown)
                if (uiState.hasSpendTarget && uiState.quarterlySpendTarget > 0) {
                    item {
                        LiquidGlassCard(
                            shape = RoundedCornerShape(22.dp),
                            elevation = 3.dp,
                            modifier = Modifier.fillMaxWidth(),
                            tokens = tokens
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                val spendPct = (uiState.currentQuarterSpend / uiState.quarterlySpendTarget).toFloat().coerceIn(0f, 1f)
                                val pctInt = (spendPct * 100).toInt()

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
                                                .background(tokens.primaryAccent.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Rounded.TrendingUp,
                                                contentDescription = null,
                                                tint = tokens.primaryAccent,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = if (uiState.activeBankCard != null) "${uiState.activeBankCard?.cardNickname} Spend" else "Quarterly Milestone Tracker",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = tokens.textPrimary
                                            )
                                            Text(
                                                text = if (uiState.isPrivacyMode) "Spend Target: ₹ ••••" else "Spend Target: ₹${uiState.quarterlySpendTarget.toInt()}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = tokens.textTertiary
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (pctInt >= 100) Color(0xFFECFDF5) else tokens.primaryAccent.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = if (pctInt >= 100) "GOAL MET 🎉" else "$pctInt% Completed",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (pctInt >= 100) Color(0xFF059669) else tokens.primaryAccent,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(Modifier.height(14.dp))

                                LinearProgressIndicator(
                                    progress = { spendPct },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(CircleShape),
                                    color = if (pctInt >= 100) Color(0xFF10B981) else tokens.primaryAccent,
                                    trackColor = if (tokens.isDark) Color(0x332A2D5C) else Color(0xFFEEEDFF)
                                )

                                Spacer(Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = if (uiState.isPrivacyMode) "Current: ₹ ••••" else "Current: ₹${uiState.currentQuarterSpend.toInt()}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = tokens.textTertiary
                                    )
                                    val remaining = (uiState.quarterlySpendTarget - uiState.currentQuarterSpend).coerceAtLeast(0.0)
                                    Text(
                                        text = if (uiState.isPrivacyMode) "Target Active" else if (remaining > 0) "₹${remaining.toInt()} to target" else "Target achieved",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (pctInt >= 100) Color(0xFF059669) else tokens.primaryAccent
                                    )
                                }
                            }
                        }
                    }
                }

                // 7. Wallet Insights Section Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = tokens.primaryAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Wallet Insights",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = tokens.textPrimary
                            )
                        }

                        Text(
                            text = "View Vault",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = tokens.primaryAccent,
                            modifier = Modifier.clickable { onNavigateToVault() }
                        )
                    }
                }

                // 8. Insights 2x2 Grid (Liquid Glass Cards)
                item {
                    val topCatShare = remember(uiState.totalBalance, uiState.categoryBalances, uiState.topCategory) {
                        if (uiState.topCategory != null && uiState.totalBalance > 0) {
                            val topBal = uiState.categoryBalances.find { it.category == uiState.topCategory }?.totalBalance ?: 0.0
                            ((topBal / uiState.totalBalance) * 100).toInt().coerceIn(0, 100)
                        } else 0
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            GlassInsightCard(
                                title = "Active Cards",
                                value = "${uiState.totalCards}",
                                subtitle = "₹${String.format("%,.0f", uiState.totalBalance)} total",
                                icon = Icons.Rounded.Style,
                                iconTint = tokens.primaryAccent,
                                modifier = Modifier.weight(1f),
                                tokens = tokens
                            )

                            GlassInsightCard(
                                title = "Expiring Soon",
                                value = "${uiState.expiringCards.size}",
                                subtitle = if (uiState.expiringCards.isNotEmpty()) "Action needed" else "None expiring",
                                icon = Icons.Rounded.Timer,
                                iconTint = if (uiState.expiringCards.isNotEmpty()) Color(0xFFEF4444) else tokens.textTertiary,
                                modifier = Modifier.weight(1f),
                                tokens = tokens
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            GlassInsightCard(
                                title = "Top Category",
                                value = uiState.topCategory?.displayName ?: "None",
                                subtitle = if (uiState.topCategory != null) "$topCatShare% of balance" else "No cards yet",
                                icon = Icons.Rounded.Category,
                                iconTint = tokens.primaryAccent,
                                modifier = Modifier.weight(1f),
                                tokens = tokens
                            )

                            GlassInsightCard(
                                title = "Top Brand",
                                value = uiState.favoriteBrand ?: "None",
                                subtitle = if (uiState.favoriteBrand != null) "Most frequent" else "No cards yet",
                                icon = Icons.Rounded.Star,
                                iconTint = Color(0xFFE11D48),
                                modifier = Modifier.weight(1f),
                                tokens = tokens
                            )
                        }
                    }
                }

                // 9. Expiring Cards Alert Carousel
                if (uiState.expiringCards.isNotEmpty()) {
                    item {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Warning, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "Expiring Cards Alert",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFEF4444)
                                )
                            }
                            Spacer(Modifier.height(10.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(uiState.expiringCards, key = { it.id }) { card ->
                                    GiftCardVisual(
                                        merchantName = card.merchantName,
                                        cardNumber = card.cardNumber,
                                        balance = card.balance,
                                        currency = card.currency,
                                        expiryDate = card.expiryDate?.toString(),
                                        templateKey = card.templateKey,
                                        compactMode = true,
                                        isPrivacyMode = uiState.isPrivacyMode,
                                        modifier = Modifier.width(220.dp),
                                        onClick = { onNavigateToCardDetails(card.id) }
                                    )
                                }
                            }
                        }
                    }
                }

                // 10. Balance Allocation Progress Bars
                item {
                    Column {
                        Text(
                            text = "Balance Allocation",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = tokens.textPrimary
                        )
                        Spacer(Modifier.height(10.dp))

                        if (uiState.categoryBalances.isNotEmpty()) {
                            LiquidGlassCard(
                                shape = RoundedCornerShape(24.dp),
                                elevation = 2.dp,
                                modifier = Modifier.fillMaxWidth(),
                                tokens = tokens
                            ) {
                                Column(
                                    modifier = Modifier.padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    uiState.categoryBalances.take(5).forEach { catBal ->
                                        val percentage = if (uiState.totalBalance > 0) {
                                            (catBal.totalBalance / uiState.totalBalance).toFloat()
                                        } else 0f

                                        Column {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    catBal.category.displayName,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = tokens.textPrimary
                                                )
                                                Text(
                                                    if (uiState.isPrivacyMode) "₹ •••• (${(percentage * 100).toInt()}%)" else "₹${String.format("%,.2f", catBal.totalBalance)} (${(percentage * 100).toInt()}%)",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = tokens.primaryAccent
                                                )
                                            }
                                            Spacer(Modifier.height(6.dp))
                                            LinearProgressIndicator(
                                                progress = { percentage },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(8.dp)
                                                    .clip(CircleShape),
                                                color = tokens.primaryAccent,
                                                trackColor = if (tokens.isDark) Color(0x332A2D5C) else Color(0xFFEEEDFF)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GlassInsightCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier,
    tokens: LiquidGlassTokens = currentGlassTokens()
) {
    LiquidGlassCard(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        elevation = 2.dp,
        tokens = tokens
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = tokens.textSecondary,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(Modifier.height(10.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = tokens.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = tokens.textTertiary
            )
        }
    }
}
