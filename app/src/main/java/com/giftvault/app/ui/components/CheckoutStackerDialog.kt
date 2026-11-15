package com.giftvault.app.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.giftvault.app.domain.model.BankCard
import com.giftvault.app.domain.model.GiftCard
import com.giftvault.app.ui.components.glass.*

data class StackedVoucherItem(
    val voucher: GiftCard,
    val amountToDeduct: Double,
    val remainingVoucherBalance: Double
)

@Composable
fun CheckoutStackerDialog(
    allCards: List<GiftCard>,
    bankCards: List<BankCard>,
    initialMerchantName: String? = null,
    onDismissRequest: () -> Unit,
    onApplyRedemptions: (deductions: List<Pair<GiftCard, Double>>) -> Unit,
    tokens: LiquidGlassTokens = currentGlassTokens()
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val merchantsWithVouchers = remember(allCards) {
        allCards.filter { it.balance > 0 }
            .map { it.merchantName }
            .distinct()
            .sorted()
    }

    var selectedMerchant by remember {
        mutableStateOf(initialMerchantName ?: merchantsWithVouchers.firstOrNull() ?: "")
    }
    var cartAmountInput by remember { mutableStateOf("") }
    var selectedVoucherIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var autoSelectMode by remember { mutableStateOf(true) }

    val merchantVouchers = remember(allCards, selectedMerchant) {
        allCards.filter {
            it.merchantName.equals(selectedMerchant, ignoreCase = true) &&
            it.balance > 0 &&
            it.status != com.giftvault.app.domain.model.CardStatus.ARCHIVED
        }.sortedWith(
            compareBy<GiftCard> { it.expiryDate ?: java.time.LocalDate.MAX }
                .thenByDescending { it.balance }
        )
    }

    val cartAmount = cartAmountInput.toDoubleOrNull() ?: 0.0

    // Automatic calculation: selects optimal combination prioritizing expiring cards first
    val stackedItems = remember(merchantVouchers, cartAmount, autoSelectMode, selectedVoucherIds) {
        if (cartAmount <= 0.0) return@remember emptyList<StackedVoucherItem>()

        val candidateCards = if (autoSelectMode) merchantVouchers else merchantVouchers.filter { selectedVoucherIds.contains(it.id) }
        var currentNeeded = cartAmount
        val result = mutableListOf<StackedVoucherItem>()

        for (v in candidateCards) {
            if (currentNeeded <= 0.0) break
            val deduct = minOf(v.balance, currentNeeded)
            result.add(
                StackedVoucherItem(
                    voucher = v,
                    amountToDeduct = deduct,
                    remainingVoucherBalance = v.balance - deduct
                )
            )
            currentNeeded -= deduct
        }
        result
    }

    val totalVouchersApplied = stackedItems.sumOf { it.amountToDeduct }
    val remainingToPay = maxOf(0.0, cartAmount - totalVouchersApplied)

    // Suggest best bank card to pay remainder (prefer cards with quarterly spend targets or primary)
    val suggestedBankCard = remember(bankCards, stackedItems) {
        val linkedIds = stackedItems.mapNotNull { it.voucher.linkedCardId }.toSet()
        val linkedCard = bankCards.find { it.id in linkedIds }
        linkedCard ?: bankCards.find { it.hasSpendTarget && it.currentQuarterSpend < it.quarterlySpendTarget }
        ?: bankCards.find { it.isPrimary } ?: bankCards.firstOrNull()
    }

    LiquidGlassDialog(
        onDismissRequest = onDismissRequest,
        title = "Checkout Stacker",
        tokens = tokens,
        confirmButton = if (stackedItems.isNotEmpty()) {
            {
                LiquidGlassButton(
                    onClick = {
                        val deductions = stackedItems.map { Pair(it.voucher, it.amountToDeduct) }
                        onApplyRedemptions(deductions)
                        Toast.makeText(context, "Applied ₹${String.format("%,.0f", totalVouchersApplied)} from ${stackedItems.size} vouchers!", Toast.LENGTH_SHORT).show()
                        onDismissRequest()
                    },
                    text = "Apply & Deduct",
                    icon = Icons.Rounded.DoneAll,
                    style = GlassButtonStyle.PRIMARY,
                    tokens = tokens
                )
            }
        } else null,
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
            Text(
                text = "Combine multiple expiring vouchers on a single bill & calculate the remaining balance to pay via bank card.",
                style = MaterialTheme.typography.bodySmall,
                color = tokens.textSecondary
            )

            // Merchant Selection Pills
            if (merchantsWithVouchers.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(merchantsWithVouchers) { merchant ->
                        val isSelected = merchant.equals(selectedMerchant, ignoreCase = true)
                        LiquidGlassChip(
                            selected = isSelected,
                            onClick = { selectedMerchant = merchant },
                            label = merchant,
                            tokens = tokens
                        )
                    }
                }
            }

            // Cart Total Input
            OutlinedTextField(
                value = cartAmountInput,
                onValueChange = { cartAmountInput = it.filter { ch -> ch.isDigit() || ch == '.' } },
                label = { Text("Cart / Order Total (₹)") },
                placeholder = { Text("e.g. 2450", color = tokens.textTertiary) },
                leadingIcon = { Text("₹", fontWeight = FontWeight.Bold, color = tokens.primaryAccent, fontSize = 18.sp, modifier = Modifier.padding(start = 12.dp)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Split Summary Card
            LiquidGlassSurface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                surfaceBrush = tokens.surfaceGradient,
                borderBrush = tokens.activeBorderGradient,
                borderWidth = 1.dp,
                tokens = tokens
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Cart Total:", color = tokens.textSecondary, style = MaterialTheme.typography.bodyMedium)
                        Text("₹${String.format("%,.2f", cartAmount)}", fontWeight = FontWeight.Bold, color = tokens.textPrimary)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Vouchers Applied:", color = Color(0xFF10B981), style = MaterialTheme.typography.bodyMedium)
                        Text("- ₹${String.format("%,.2f", totalVouchersApplied)}", fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = tokens.border.copy(alpha = 0.3f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Remainder to Pay:", fontWeight = FontWeight.Bold, color = tokens.textPrimary, style = MaterialTheme.typography.titleSmall)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (remainingToPay > 0) Color(0x22F59E0B) else Color(0x2210B981)
                        ) {
                            Text(
                                text = "₹${String.format("%,.2f", remainingToPay)}",
                                color = if (remainingToPay > 0) Color(0xFFF59E0B) else Color(0xFF10B981),
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    if (remainingToPay > 0 && suggestedBankCard != null) {
                        Spacer(Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Rounded.CreditCard, contentDescription = null, tint = tokens.primaryAccent, modifier = Modifier.size(16.dp))
                            Text(
                                text = "Recommended: Pay remainder with ${suggestedBankCard.bankName} ${suggestedBankCard.cardNickname} (••${suggestedBankCard.lastFourDigits})",
                                style = MaterialTheme.typography.labelSmall,
                                color = tokens.textTertiary
                            )
                        }
                    }
                }
            }

            // Stacked Vouchers List
            if (stackedItems.isNotEmpty()) {
                Text(
                    text = "Vouchers to Scan / Redeem (${stackedItems.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = tokens.textPrimary
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 260.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(stackedItems, key = { it.voucher.id }) { item ->
                        val v = item.voucher
                        LiquidGlassSurface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            surfaceBrush = tokens.elevatedGradient,
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
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = v.merchantName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = tokens.textPrimary
                                    )
                                    Text(
                                        text = "Code: ${v.cardNumber.chunked(4).joinToString(" ")}",
                                        fontFamily = FontFamily.Monospace,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = tokens.primaryAccent
                                    )
                                    v.pin?.let { p ->
                                        Text(
                                            text = "PIN: $p",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = tokens.textTertiary
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Apply ₹${String.format("%,.0f", item.amountToDeduct)}",
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF10B981),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "Rem: ₹${String.format("%,.0f", item.remainingVoucherBalance)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = tokens.textTertiary
                                    )
                                    IconButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(v.cardNumber))
                                            Toast.makeText(context, "Voucher code copied!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Rounded.ContentCopy, contentDescription = "Copy", tint = tokens.textSecondary, modifier = Modifier.size(16.dp))
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
