package com.giftvault.app.ui.claims

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.giftvault.app.domain.model.ClaimStatus
import com.giftvault.app.domain.model.TruzteeClaim
import com.giftvault.app.ui.components.glass.*
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TruzteeClaimsScreen(
    onNavigateBack: () -> Unit,
    viewModel: TruzteeClaimsViewModel = hiltViewModel()
) {
    val tokens = currentGlassTokens()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Truztee Claims & Orders",
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        floatingActionButton = {
            LiquidGlassAddButton(
                onClick = { viewModel.openAddDialog() },
                contentDescription = "Log Truztee Claim",
                tokens = tokens
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF584BF6))
                }
            } else if (uiState.claims.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEEEDFF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.ReceiptLong,
                                contentDescription = null,
                                tint = Color(0xFF584BF6),
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Text(
                            "No Claims or Dispute Logs",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Log your RuPay Select Truztee order IDs & transactions to generate 1-tap escalation emails to support.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.openAddDialog() },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF584BF6))
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("Log New Claim")
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.claims, key = { it.id }) { claim ->
                        ClaimItemCard(
                            claim = claim,
                            onEscalateEmail = { generateEscalationEmail(context, claim) },
                            onDelete = { viewModel.deleteClaim(claim.id) },
                            onStatusChange = { newStatus -> viewModel.updateClaimStatus(claim, newStatus) }
                        )
                    }
                }
            }
        }
    }

    if (uiState.isFormOpen) {
        ClaimFormDialog(uiState = uiState, viewModel = viewModel)
    }
}

@Composable
private fun ClaimItemCard(
    claim: TruzteeClaim,
    onEscalateEmail: () -> Unit,
    onDelete: () -> Unit,
    onStatusChange: (ClaimStatus) -> Unit
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a").withZone(ZoneId.systemDefault()) }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFEBEAFF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
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
                Column {
                    Text(
                        text = "${claim.merchantName} (₹${claim.voucherAmount.toInt()})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1B4B)
                    )
                    Text(
                        text = "Order: ${claim.orderId}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF584BF6)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (claim.status) {
                        ClaimStatus.RESOLVED -> Color(0xFFDCFCE7)
                        ClaimStatus.DISPUTED -> Color(0xFFFEE2E2)
                        ClaimStatus.PROCESSING -> Color(0xFFFEF3C7)
                        ClaimStatus.PENDING -> Color(0xFFEEEDFF)
                    }
                ) {
                    Text(
                        text = claim.status.displayName.uppercase(),
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp,
                        color = when (claim.status) {
                            ClaimStatus.RESOLVED -> Color(0xFF16A34A)
                            ClaimStatus.DISPUTED -> Color(0xFFDC2626)
                            ClaimStatus.PROCESSING -> Color(0xFFD97706)
                            ClaimStatus.PENDING -> Color(0xFF584BF6)
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            if (claim.transactionId.isNotBlank()) {
                Text(
                    text = "Txn Ref: ${claim.transactionId}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF64748B)
                )
            }

            if (!claim.bankCardNickname.isNullOrBlank()) {
                Text(
                    text = "Card Profile: ${claim.bankCardNickname}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF64748B)
                )
            }

            Text(
                text = "Logged: ${dateFormatter.format(claim.createdAt)}",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF94A3B8)
            )

            if (!claim.notes.isNullOrBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = claim.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF334155)
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onEscalateEmail,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Rounded.Mail, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Escalate Email", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                OutlinedButton(
                    onClick = {
                        val nextStatus = when (claim.status) {
                            ClaimStatus.PENDING -> ClaimStatus.PROCESSING
                            ClaimStatus.PROCESSING -> ClaimStatus.RESOLVED
                            ClaimStatus.RESOLVED -> ClaimStatus.DISPUTED
                            ClaimStatus.DISPUTED -> ClaimStatus.RESOLVED
                        }
                        onStatusChange(nextStatus)
                    },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("Status", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                IconButton(onClick = onDelete) {
                    Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

private fun generateEscalationEmail(context: Context, claim: TruzteeClaim) {
    val recipient = "rupay@truztee.com"
    val subject = "Escalation: Truztee RuPay Rewards Order #${claim.orderId} - ${claim.merchantName}"
    val body = """
Dear Truztee RuPay Rewards Support Team,

I am writing to escalate an issue regarding my voucher redemption on the RuPay Select portal (rupayselect.truztee.com).

Order Details:
- Order ID: ${claim.orderId}
- Transaction ID: ${claim.transactionId.ifBlank { "N/A" }}
- Merchant / Brand: ${claim.merchantName}
- Voucher Value: ₹${claim.voucherAmount.toInt()}
- Bank Card Profile: ${claim.bankCardNickname ?: "RuPay Select Card"}
- Status: ${claim.status.displayName}

Issue Description / Notes:
${claim.notes ?: "Voucher code not received / activation failure. Please investigate and resolve this at the earliest."}

Kindly check this transaction and re-issue / activate the voucher.

Thank you.
    """.trimIndent()

    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, body)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    try {
        context.startActivity(Intent.createChooser(intent, "Send Escalation Email"))
    } catch (e: Exception) {
        Toast.makeText(context, "No email client found on device", Toast.LENGTH_SHORT).show()
    }
}

@Composable
private fun ClaimFormDialog(
    uiState: TruzteeClaimsUiState,
    viewModel: TruzteeClaimsViewModel,
    tokens: LiquidGlassTokens = currentGlassTokens()
) {
    LiquidGlassDialog(
        onDismissRequest = { viewModel.closeDialog() },
        title = "Log Truztee Order / Claim",
        tokens = tokens,
        confirmButton = {
            LiquidGlassButton(
                onClick = { viewModel.saveClaim() },
                text = "Save Claim",
                style = GlassButtonStyle.PRIMARY,
                tokens = tokens
            )
        },
        dismissButton = {
            LiquidGlassButton(
                onClick = { viewModel.closeDialog() },
                text = "Cancel",
                style = GlassButtonStyle.SECONDARY,
                tokens = tokens
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (uiState.errorMessage != null) {
                Text(uiState.errorMessage, color = Color(0xFFEF4444), style = MaterialTheme.typography.bodySmall)
            }

            OutlinedTextField(
                value = uiState.orderIdInput,
                onValueChange = { viewModel.onOrderIdChanged(it) },
                label = { Text("Order ID *") },
                placeholder = { Text("e.g. TRZ-8921849", color = tokens.textTertiary) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.transactionIdInput,
                onValueChange = { viewModel.onTransactionIdChanged(it) },
                label = { Text("Transaction ID / Ref") },
                placeholder = { Text("e.g. TXN98492024", color = tokens.textTertiary) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.merchantNameInput,
                onValueChange = { viewModel.onMerchantNameChanged(it) },
                label = { Text("Merchant / Brand *") },
                placeholder = { Text("e.g. Swiggy, Amazon, Myntra", color = tokens.textTertiary) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.amountInput,
                onValueChange = { viewModel.onAmountChanged(it) },
                label = { Text("Voucher Amount (₹)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Linked Bank Card selector
            if (uiState.bankCards.isNotEmpty()) {
                Text("Linked Bank Card", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = tokens.textPrimary)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(uiState.bankCards) { card ->
                        LiquidGlassChip(
                            selected = uiState.selectedBankCard?.id == card.id,
                            onClick = { viewModel.onBankCardSelected(card) },
                            label = card.cardNickname,
                            tokens = tokens
                        )
                    }
                }
            }

            OutlinedTextField(
                value = uiState.notesInput,
                onValueChange = { viewModel.onNotesChanged(it) },
                label = { Text("Notes / Issue Description") },
                placeholder = { Text("e.g. SMS not received after deduction", color = tokens.textTertiary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp),
                shape = RoundedCornerShape(14.dp),
                maxLines = 3
            )
        }
    }
}
