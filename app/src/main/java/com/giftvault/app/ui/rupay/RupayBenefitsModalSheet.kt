package com.giftvault.app.ui.rupay

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.giftvault.app.core.rupay.BenefitClaimStatus
import com.giftvault.app.core.rupay.PnbCardBenefitSummary
import com.giftvault.app.domain.model.GiftCard
import com.giftvault.app.ui.components.glass.*

@Composable
fun RupayBenefitsModalSheet(
    summaries: List<PnbCardBenefitSummary>,
    unlinkedVouchers: List<GiftCard>,
    onDismissRequest: () -> Unit,
    onViewVoucherDetails: (String) -> Unit,
    onLinkVoucherToCard: (voucherId: String, cardId: Long) -> Unit,
    tokens: LiquidGlassTokens = currentGlassTokens()
) {
    var selectedCardIndex by remember { mutableStateOf(0) }
    var voucherPickerForBenefit by remember { mutableStateOf<BenefitClaimStatus?>(null) }
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val currentSummary = summaries.getOrNull(selectedCardIndex) ?: summaries.firstOrNull()

    LiquidGlassDialog(
        onDismissRequest = onDismissRequest,
        title = "RuPay Benefits & Perks",
        tokens = tokens,
        dismissButton = {
            LiquidGlassButton(
                onClick = onDismissRequest,
                text = "Close",
                style = GlassButtonStyle.SECONDARY,
                tokens = tokens
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (summaries.isEmpty()) {
                Text(
                    text = "No RuPay or PNB cards found. Add a PNB Imperial or RuPay card in the Cards tab to track quarterly benefits.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = tokens.textSecondary
                )
            } else {
                // Card Selector Tabs
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(summaries.indices.toList()) { index ->
                        val summary = summaries[index]
                        val isSelected = index == selectedCardIndex
                        LiquidGlassChip(
                            selected = isSelected,
                            onClick = { selectedCardIndex = index },
                            label = "${summary.card.cardNickname} (••${summary.card.lastFourDigits})",
                            tokens = tokens
                        )
                    }
                }

                currentSummary?.let { summary ->
                    // Progress Banner
                    LiquidGlassSurface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        surfaceBrush = tokens.surfaceGradient,
                        borderBrush = tokens.activeBorderGradient,
                        borderWidth = 1.dp,
                        tokens = tokens
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${summary.card.cardNickname} • ${summary.quarterLabel}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = tokens.textPrimary
                                    )
                                    Text(
                                        text = "${summary.claimedCount} of ${summary.totalCount} benefits claimed",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF10B981),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0x2210B981)
                                ) {
                                    Text(
                                        text = "₹${String.format("%,.0f", summary.totalClaimedValue)} Secured",
                                        color = Color(0xFF10B981),
                                        fontWeight = FontWeight.Black,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(Modifier.height(8.dp))
                            val progress = if (summary.totalCount > 0) summary.claimedCount.toFloat() / summary.totalCount else 0f
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape),
                                color = Color(0xFF10B981),
                                trackColor = if (tokens.isDark) Color(0x332A2D5C) else Color(0xFFE2E8F0)
                            )
                        }
                    }

                    // Benefit Items List
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 380.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(summary.benefits, key = { it.category.id }) { benefit ->
                            LiquidGlassSurface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                surfaceBrush = if (benefit.isClaimed) tokens.elevatedGradient else tokens.surfaceGradient,
                                borderBrush = if (benefit.isClaimed) tokens.activeBorderGradient else tokens.borderGradient,
                                borderWidth = 1.dp,
                                tokens = tokens
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (benefit.isClaimed) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                                                contentDescription = null,
                                                tint = if (benefit.isClaimed) Color(0xFF10B981) else Color(0xFFF59E0B),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Column {
                                                Text(
                                                    text = benefit.category.title,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = tokens.textPrimary
                                                )
                                                Text(
                                                    text = "Allowance: ₹${String.format("%,.0f", benefit.category.quarterlyValue)}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = tokens.textTertiary
                                                )
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (benefit.isClaimed) Color(0x2210B981) else Color(0x22F59E0B)
                                        ) {
                                            Text(
                                                text = if (benefit.isClaimed) "CLAIMED" else "UNCLAIMED",
                                                color = if (benefit.isClaimed) Color(0xFF10B981) else Color(0xFFF59E0B),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Black,
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                            )
                                        }
                                    }

                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = benefit.category.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = tokens.textSecondary,
                                        fontSize = 11.sp
                                    )

                                    Spacer(Modifier.height(8.dp))

                                    if (benefit.isClaimed && benefit.matchingVoucher != null) {
                                        val voucher = benefit.matchingVoucher
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = "Voucher: ${voucher.merchantName} (₹${String.format("%,.0f", voucher.balance)})",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = tokens.primaryAccent
                                                )
                                                benefit.claimDateText?.let {
                                                    Text(
                                                        text = "Added on $it",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = tokens.textTertiary
                                                    )
                                                }
                                            }

                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                LiquidGlassIconButton(
                                                    onClick = {
                                                        clipboardManager.setText(AnnotatedString(voucher.cardNumber))
                                                        Toast.makeText(context, "Code copied!", Toast.LENGTH_SHORT).show()
                                                    },
                                                    icon = Icons.Rounded.ContentCopy,
                                                    contentDescription = "Copy code",
                                                    size = 32.dp,
                                                    iconSize = 16.dp,
                                                    tokens = tokens
                                                )
                                                LiquidGlassButton(
                                                    onClick = {
                                                        onDismissRequest()
                                                        onViewVoucherDetails(voucher.id)
                                                    },
                                                    text = "View",
                                                    icon = Icons.Rounded.Visibility,
                                                    height = 32.dp,
                                                    style = GlassButtonStyle.PRIMARY,
                                                    tokens = tokens
                                                )
                                            }
                                        }
                                    } else {
                                        // Unclaimed actions
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            LiquidGlassButton(
                                                onClick = {
                                                    try {
                                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(benefit.category.defaultPortalUrl)).apply {
                                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                        }
                                                        context.startActivity(intent)
                                                    } catch (_: Exception) {
                                                        Toast.makeText(context, "Could not open browser", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                modifier = Modifier.weight(1f),
                                                text = "Claim on Truztee",
                                                icon = Icons.Rounded.OpenInNew,
                                                height = 32.dp,
                                                style = GlassButtonStyle.PRIMARY,
                                                tokens = tokens
                                            )

                                            LiquidGlassButton(
                                                onClick = { voucherPickerForBenefit = benefit },
                                                modifier = Modifier.weight(1f),
                                                text = "Link Voucher",
                                                icon = Icons.Rounded.Link,
                                                height = 32.dp,
                                                style = GlassButtonStyle.SECONDARY,
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
        }
    }

    // Modal Dialog to pick an existing unlinked voucher
    if (voucherPickerForBenefit != null && currentSummary != null) {
        val targetBenefit = voucherPickerForBenefit!!
        LiquidGlassDialog(
            onDismissRequest = { voucherPickerForBenefit = null },
            title = "Link Existing Voucher",
            tokens = tokens,
            dismissButton = {
                LiquidGlassButton(
                    onClick = { voucherPickerForBenefit = null },
                    text = "Cancel",
                    style = GlassButtonStyle.SECONDARY,
                    tokens = tokens
                )
            }
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Select a voucher from your vault to assign to ${currentSummary.card.cardNickname} for the ${targetBenefit.category.title} benefit:",
                    style = MaterialTheme.typography.bodySmall,
                    color = tokens.textSecondary
                )

                if (unlinkedVouchers.isEmpty()) {
                    Text(
                        text = "No unlinked vouchers found in vault. Add a new voucher in the Add Card section and select this card.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = tokens.textTertiary,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(unlinkedVouchers, key = { it.id }) { v ->
                            LiquidGlassSurface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onLinkVoucherToCard(v.id, currentSummary.card.id)
                                        voucherPickerForBenefit = null
                                        Toast.makeText(context, "${v.merchantName} linked to ${currentSummary.card.cardNickname}!", Toast.LENGTH_SHORT).show()
                                    },
                                shape = RoundedCornerShape(12.dp),
                                surfaceBrush = tokens.surfaceGradient,
                                borderBrush = tokens.borderGradient,
                                borderWidth = 1.dp,
                                tokens = tokens
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = v.merchantName,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = tokens.textPrimary
                                        )
                                        Text(
                                            text = "Balance: ₹${String.format("%,.0f", v.balance)} • ${v.category.displayName}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = tokens.textSecondary
                                        )
                                    }
                                    Icon(
                                        Icons.Rounded.AddLink,
                                        contentDescription = null,
                                        tint = tokens.primaryAccent
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
