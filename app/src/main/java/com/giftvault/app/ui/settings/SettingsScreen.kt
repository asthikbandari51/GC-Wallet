package com.giftvault.app.ui.settings

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.giftvault.app.core.rupay.RuPayRedeemManager
import com.giftvault.app.domain.model.CardCategory
import com.giftvault.app.domain.model.GiftCard
import com.giftvault.app.ui.components.glass.*
import com.giftvault.app.ui.theme.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToBackup: () -> Unit,
    onNavigateToReminders: () -> Unit,
    onNavigateToManageCards: () -> Unit = {},
    onNavigateToRuPayRedeem: () -> Unit = {},
    onNavigateToClaims: () -> Unit = {},
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val tokens = currentGlassTokens()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val ruPayRedeemManager = remember { RuPayRedeemManager(context) }

    var showRecycleBinDialog by remember { mutableStateOf(false) }
    var showCategoryExclusionDialog by remember { mutableStateOf(false) }
    var showExpiryWindowDialog by remember { mutableStateOf(false) }
    var showEditNameDialog by remember { mutableStateOf(false) }
    var editNameText by remember(uiState.userName) { mutableStateOf(uiState.userName ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = tokens.textPrimary
                    )
                },
                navigationIcon = {
                    LiquidGlassIconButton(
                        onClick = onNavigateBack,
                        icon = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Navigate back",
                        size = 38.dp,
                        iconSize = 20.dp,
                        tokens = tokens,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 💳 RuPay & Concierge Services
            SettingsGlassCard(title = "RuPay & Concierge Services", icon = Icons.Rounded.WorkspacePremium) {
                SettingsClickableRow(
                    title = "RuPay Rewards Assistant",
                    subtitle = "In-app browser with 1-tap card autofill & voucher capture",
                    icon = Icons.Rounded.OpenInBrowser,
                    onClick = onNavigateToRuPayRedeem
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = tokens.border.copy(alpha = 0.4f))
                SettingsClickableRow(
                    title = "Truztee Claims & Escalation",
                    subtitle = "Order logs & 1-tap escalation emails to support",
                    icon = Icons.Rounded.ReceiptLong,
                    onClick = onNavigateToClaims
                )
            }

            // 👤 Profile & Personalization
            SettingsGlassCard(title = "Profile & Personalization", icon = Icons.Rounded.Person) {
                SettingsClickableRow(
                    title = "Your Display Name",
                    subtitle = if (!uiState.userName.isNullOrBlank()) uiState.userName!! else "Tap to set your name (used for greetings & sharing)",
                    icon = Icons.Rounded.Badge,
                    onClick = {
                        editNameText = uiState.userName ?: ""
                        showEditNameDialog = true
                    }
                )
            }

            // 🎨 Appearance Section
            SettingsGlassCard(title = "Appearance", icon = Icons.Rounded.Palette) {
                ThemeSelection(
                    selectedTheme = uiState.themeMode,
                    onThemeSelected = { viewModel.setThemeMode(it) }
                )
            }

            // 🔒 Privacy & Security Section
            SettingsGlassCard(title = "Privacy & Security", icon = Icons.Rounded.Security) {
                SettingsSwitchRow(
                    title = "Hide Balances (Privacy Mode)",
                    subtitle = "Mask balances (₹ ••••) on Home, Vault & Bank Cards",
                    checked = uiState.isPrivacyModeEnabled,
                    onCheckedChange = { viewModel.togglePrivacyMode(it) }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = tokens.border.copy(alpha = 0.4f))
                SettingsSwitchRow(
                    title = "Require Biometrics / PIN",
                    subtitle = "App lock to secure your gift vault upon launch",
                    checked = uiState.isAppLockEnabled,
                    onCheckedChange = { viewModel.toggleAppLock(it) }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = tokens.border.copy(alpha = 0.4f))
                SettingsSwitchRow(
                    title = "Mask Bank Card CVV by Default",
                    subtitle = "Hide CVV (•••) until tapped in bank card vault",
                    checked = uiState.hideBankCardCvv,
                    onCheckedChange = { viewModel.toggleHideBankCardCvv(it) }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = tokens.border.copy(alpha = 0.4f))
                SettingsSwitchRow(
                    title = "Require Biometrics / PIN for CVV",
                    subtitle = "Prompt authentication before revealing or copying CVV",
                    checked = uiState.requireAuthForCvv,
                    onCheckedChange = { viewModel.toggleRequireAuthForCvv(it) }
                )
            }

            // 💰 Balance & Expiry Rules
            SettingsGlassCard(title = "Balance & Expiry Rules", icon = Icons.Rounded.AccountBalance) {
                val activeExcludedCount = CardCategory.entries.count { cat ->
                    uiState.excludedCategories.any { it.equals(cat.name, true) || it.equals(cat.displayName, true) }
                }
                SettingsClickableRow(
                    title = "Excluded Balance Categories",
                    subtitle = "$activeExcludedCount excluded • Exclude non-monetary cards (e.g. OTT) from total balance",
                    icon = Icons.Rounded.FilterAlt,
                    onClick = { showCategoryExclusionDialog = true }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = tokens.border.copy(alpha = 0.4f))
                SettingsClickableRow(
                    title = "Expiring Soon Alert Window",
                    subtitle = "Highlight vouchers expiring within ${uiState.expiryAlertDays} days",
                    icon = Icons.Rounded.DateRange,
                    onClick = { showExpiryWindowDialog = true }
                )
            }

            // 💾 Data & Offline Backups Section
            SettingsGlassCard(title = "Data & Backups", icon = Icons.Rounded.Storage) {
                SettingsClickableRow(
                    title = "Offline Backup & Restore",
                    subtitle = "Export or import your encrypted vault data (.giftvault)",
                    icon = Icons.Rounded.FolderZip,
                    onClick = onNavigateToBackup
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = tokens.border.copy(alpha = 0.4f))
                SettingsClickableRow(
                    title = "Recycle Bin / Archived Vouchers",
                    subtitle = "${uiState.archivedCards.size} vouchers in bin • View, restore, or permanently delete",
                    icon = Icons.Rounded.DeleteSweep,
                    onClick = { showRecycleBinDialog = true }
                )
            }

            // 🔔 Notifications Section
            SettingsGlassCard(title = "Notifications", icon = Icons.Rounded.Notifications) {
                val notifPermLauncher = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    rememberLauncherForActivityResult(
                        ActivityResultContracts.RequestPermission()
                    ) { }
                } else null

                SettingsClickableRow(
                    title = "Manage Reminders",
                    subtitle = "Set expiration alerts for active cards",
                    icon = Icons.Rounded.Alarm,
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notifPermLauncher?.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        onNavigateToReminders()
                    }
                )
            }

            // ℹ️ About Section
            SettingsGlassCard(title = "About", icon = Icons.Rounded.Info) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF584BF6), Color(0xFF7928CA))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.AccountBalanceWallet,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "GiftVault Pro V9.00.00",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        "Sovereign Offline Sanctuary for Bank Cards & Gift Vouchers",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "100% Offline • AES-256-GCM Hardware Encrypted",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF10B981),
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(14.dp))

                    // Asthik Bandari's Digital Signature Seal
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFF0F172A),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF334155)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Rounded.Verified,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "DIGITAL SIGNATURE",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.5.sp
                                )
                            }

                            Spacer(Modifier.height(8.dp))

                            // Stylized Signature Display with perfected alignment
                            Text(
                                text = "Asthik Bandari",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Cursive,
                                    fontWeight = FontWeight.Normal,
                                    letterSpacing = 1.2.sp,
                                    lineHeight = 38.sp
                                ),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                color = Color(0xFFF8FAFC),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            )

                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Architected & Designed with Precision by Asthik Bandari",
                                style = MaterialTheme.typography.labelSmall,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                color = Color(0xFFA5B4FC),
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    // ── Edit Display Name Dialog ──
    if (showEditNameDialog) {
        LiquidGlassDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = "Set Your Display Name",
            tokens = tokens,
            confirmButton = {
                LiquidGlassButton(
                    onClick = {
                        viewModel.setUserName(editNameText.trim())
                        showEditNameDialog = false
                    },
                    text = "Save",
                    style = GlassButtonStyle.PRIMARY,
                    tokens = tokens
                )
            },
            dismissButton = {
                LiquidGlassButton(
                    onClick = { showEditNameDialog = false },
                    text = "Cancel",
                    style = GlassButtonStyle.SECONDARY,
                    tokens = tokens
                )
            }
        ) {
            Text(
                "Your name is used for the app greeting and when sharing gift cards. The developer signature in About remains Asthik Bandari.",
                style = MaterialTheme.typography.bodySmall,
                color = tokens.textSecondary
            )
            Spacer(Modifier.height(14.dp))
            OutlinedTextField(
                value = editNameText,
                onValueChange = { editNameText = it },
                label = { Text("Your Name") },
                placeholder = { Text("Enter your name", color = tokens.textTertiary) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    // ── Recycle Bin Dialog ──
    if (showRecycleBinDialog) {
        LiquidGlassDialog(
            onDismissRequest = { showRecycleBinDialog = false },
            title = "Recycle Bin (${uiState.archivedCards.size})",
            tokens = tokens,
            confirmButton = if (uiState.archivedCards.isNotEmpty()) {
                {
                    LiquidGlassButton(
                        onClick = {
                            viewModel.emptyRecycleBin()
                            showRecycleBinDialog = false
                        },
                        text = "Empty Bin",
                        style = GlassButtonStyle.PRIMARY,
                        tokens = tokens
                    )
                }
            } else null,
            dismissButton = {
                LiquidGlassButton(
                    onClick = { showRecycleBinDialog = false },
                    text = "Close",
                    style = GlassButtonStyle.SECONDARY,
                    tokens = tokens
                )
            }
        ) {
            if (uiState.archivedCards.isEmpty()) {
                Text(
                    "The recycle bin is empty. Archived vouchers will appear here for safe recovery or permanent deletion.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = tokens.textSecondary
                )
            } else {
                Text(
                    "Archived vouchers are excluded from your active vault but can be restored anytime.",
                    style = MaterialTheme.typography.bodySmall,
                    color = tokens.textSecondary
                )
                Spacer(Modifier.height(12.dp))
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp)
                ) {
                    items(uiState.archivedCards, key = { it.id }) { card ->
                        LiquidGlassSurface(
                            shape = RoundedCornerShape(14.dp),
                            surfaceBrush = tokens.surfaceGradient,
                            borderBrush = tokens.borderGradient,
                            borderWidth = 1.dp,
                            tokens = tokens,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        card.merchantName,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = tokens.textPrimary
                                    )
                                    Text(
                                        "INR ${card.balance.toInt()} • Code: •• ${card.cardNumber.takeLast(4)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = tokens.textSecondary
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    LiquidGlassIconButton(
                                        onClick = { viewModel.restoreArchivedCard(card.id) },
                                        icon = Icons.Rounded.Restore,
                                        contentDescription = "Restore",
                                        size = 32.dp,
                                        iconSize = 16.dp,
                                        tint = tokens.primaryAccent,
                                        tokens = tokens
                                    )
                                    LiquidGlassIconButton(
                                        onClick = { viewModel.deleteCardPermanently(card.id) },
                                        icon = Icons.Rounded.DeleteForever,
                                        contentDescription = "Delete Permanently",
                                        size = 32.dp,
                                        iconSize = 16.dp,
                                        tint = Color(0xFFEF4444),
                                        tokens = tokens
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ── Excluded Categories Dialog ──
    if (showCategoryExclusionDialog) {
        LiquidGlassDialog(
            onDismissRequest = { showCategoryExclusionDialog = false },
            title = "Exclude Categories from Balance",
            tokens = tokens,
            confirmButton = {
                LiquidGlassButton(
                    onClick = { showCategoryExclusionDialog = false },
                    text = "Done",
                    style = GlassButtonStyle.PRIMARY,
                    tokens = tokens
                )
            }
        ) {
            Text(
                "Selected categories will be excluded from the Total Vault Balance calculation on Home and Vault screens:",
                style = MaterialTheme.typography.bodySmall,
                color = tokens.textSecondary
            )
            Spacer(Modifier.height(12.dp))
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 280.dp)
            ) {
                items(CardCategory.entries.toTypedArray()) { cat ->
                    val isExcluded = uiState.excludedCategories.any {
                        it.equals(cat.name, ignoreCase = true) || it.equals(cat.displayName, ignoreCase = true)
                    }
                    LiquidGlassSurface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleExcludedCategory(cat.name) },
                        shape = RoundedCornerShape(12.dp),
                        surfaceBrush = if (isExcluded) tokens.elevatedGradient else tokens.surfaceGradient,
                        borderBrush = if (isExcluded) tokens.activeBorderGradient else tokens.borderGradient,
                        borderWidth = 1.dp,
                        tokens = tokens
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isExcluded,
                                onCheckedChange = { viewModel.toggleExcludedCategory(cat.name) }
                            )
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(
                                    cat.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isExcluded) FontWeight.Bold else FontWeight.Normal,
                                    color = tokens.textPrimary
                                )
                                if (cat == CardCategory.OTT_STREAMING) {
                                    Text(
                                        "Recommended (Subscription / Non-monetary)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = tokens.textTertiary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ── Expiry Alert Window Dialog ──
    if (showExpiryWindowDialog) {
        val windowOptions = listOf(7, 15, 30, 60)
        LiquidGlassDialog(
            onDismissRequest = { showExpiryWindowDialog = false },
            title = "Expiry Alert Window",
            tokens = tokens,
            dismissButton = {
                LiquidGlassButton(
                    onClick = { showExpiryWindowDialog = false },
                    text = "Close",
                    style = GlassButtonStyle.SECONDARY,
                    tokens = tokens
                )
            }
        ) {
            Text(
                "Vouchers expiring within this timeframe will appear in the Expiring Cards carousel on Home:",
                style = MaterialTheme.typography.bodySmall,
                color = tokens.textSecondary
            )
            Spacer(Modifier.height(12.dp))
            windowOptions.forEach { days ->
                val isSelected = uiState.expiryAlertDays == days
                LiquidGlassSurface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable {
                            viewModel.setExpiryAlertDays(days)
                            showExpiryWindowDialog = false
                        },
                    shape = RoundedCornerShape(12.dp),
                    surfaceBrush = if (isSelected) tokens.elevatedGradient else tokens.surfaceGradient,
                    borderBrush = if (isSelected) tokens.activeBorderGradient else tokens.borderGradient,
                    borderWidth = 1.dp,
                    tokens = tokens
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isSelected) "✓ $days Days" else "$days Days",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) tokens.primaryAccent else tokens.textPrimary
                        )
                        if (isSelected) {
                            Icon(
                                Icons.Rounded.Check,
                                contentDescription = null,
                                tint = tokens.primaryAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsGlassCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    val tokens = currentGlassTokens()
    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = LiquidGlassTokens.ShapeLarge,
        tokens = tokens
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(tokens.primaryAccent.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = tokens.primaryAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = tokens.textPrimary
                )
            }
            content()
        }
    }
}

@Composable
fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val tokens = currentGlassTokens()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = tokens.textPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = tokens.textSecondary
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = tokens.primaryAccent
            )
        )
    }
}

@Composable
fun SettingsClickableRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val tokens = currentGlassTokens()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tokens.iconSecondary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = tokens.textPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = tokens.textSecondary
                )
            }
        }
        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = tokens.iconSecondary,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun ThemeSelection(
    selectedTheme: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit
) {
    val tokens = currentGlassTokens()
    val themes = listOf(
        Pair(ThemeMode.LIGHT, "Light (Clean Classic)"),
        Pair(ThemeMode.LIQUID_GLASS, "iOS Liquid Glass")
    )

    Column(modifier = Modifier.selectableGroup()) {
        themes.forEach { (mode, label) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .selectable(
                        selected = (mode == selectedTheme),
                        onClick = { onThemeSelected(mode) },
                        role = Role.RadioButton
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (mode == selectedTheme),
                    onClick = null,
                    colors = RadioButtonDefaults.colors(selectedColor = tokens.primaryAccent)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (mode == selectedTheme) FontWeight.Bold else FontWeight.Normal,
                    color = tokens.textPrimary
                )
            }
        }
    }
}
