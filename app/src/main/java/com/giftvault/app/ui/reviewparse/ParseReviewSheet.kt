package com.giftvault.app.ui.reviewparse

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.giftvault.app.domain.model.CardCategory
import com.giftvault.app.domain.model.GiftCardParseResult
import com.giftvault.app.domain.model.ParseSourceType
import com.giftvault.app.ui.components.TemplatePicker
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParseReviewSheet(
    parseResult: GiftCardParseResult,
    onDismiss: () -> Unit,
    onConfirm: (
        merchantName: String,
        cardNumber: String,
        pin: String,
        amount: String,
        expiryDate: LocalDate?,
        templateKey: String,
        category: CardCategory,
        receiptUri: Uri?
    ) -> Unit
) {
    var merchantName by remember(parseResult) { mutableStateOf(parseResult.merchantName ?: "") }
    var cardNumber by remember(parseResult) { mutableStateOf(parseResult.cardCode ?: "") }
    var pin by remember(parseResult) { mutableStateOf(parseResult.pin ?: "") }
    var amount by remember(parseResult) { mutableStateOf(parseResult.amount?.toString() ?: "") }
    var expiryDate by remember(parseResult) { mutableStateOf(parseResult.expiryDate) }
    var selectedTemplateKey by remember(parseResult) { mutableStateOf(parseResult.templateKey ?: "other") }
    var attachReceipt by remember(parseResult) { mutableStateOf(parseResult.imageUri != null) }
    var category by remember(parseResult) { mutableStateOf(CardCategory.OTHERS) }
    var showTemplatePicker by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Title & Source Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Review Extracted Data",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Verify and correct pre-filled fields",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Confidence badge
                val confidenceColor = when {
                    parseResult.confidenceScore >= 0.8f -> Color(0xFF2E7D32) // Green
                    parseResult.confidenceScore >= 0.5f -> Color(0xFFED6C02) // Amber
                    else -> Color(0xFFD32F2F) // Red
                }

                val confidenceText = when {
                    parseResult.confidenceScore >= 0.8f -> "High Match"
                    parseResult.confidenceScore >= 0.5f -> "Medium Match"
                    else -> "Low Match"
                }

                Surface(
                    color = confidenceColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = confidenceText,
                        color = confidenceColor,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Duplicate Detection Banner
            if (parseResult.isDuplicate) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Warning: A gift card with code '${parseResult.cardCode}' already exists in your vault!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Image Proof Preview (if OCR image source)
            if (parseResult.imageUri != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        AsyncImage(
                            model = parseResult.imageUri,
                            contentDescription = "Scanned Receipt",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Scanned Receipt Image",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Attach as image proof to card",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Checkbox(
                        checked = attachReceipt,
                        onCheckedChange = { attachReceipt = it }
                    )
                }
            }

            // Editable Fields Form
            OutlinedTextField(
                value = merchantName,
                onValueChange = { merchantName = it },
                label = { Text("Merchant / Brand Name *") },
                leadingIcon = { Icon(Icons.Default.Store, contentDescription = null) },
                isError = merchantName.isBlank(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = cardNumber,
                onValueChange = { cardNumber = it },
                label = { Text("Card / Coupon Code *") },
                leadingIcon = { Icon(Icons.Default.CreditCard, contentDescription = null) },
                isError = cardNumber.isBlank(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = pin,
                    onValueChange = { pin = it },
                    label = { Text("PIN / Passcode") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Balance / Value") },
                    leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Expiry Date Field
            OutlinedTextField(
                value = expiryDate?.format(DateTimeFormatter.ofPattern("dd MMM yyyy")) ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Expiry Date") },
                leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Details / Notes preview if parsed
            if (!parseResult.details.isNullOrBlank() || !parseResult.service.isNullOrBlank()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Detected Service Details:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        parseResult.service?.let {
                            Text(text = "• Service: $it", style = MaterialTheme.typography.bodySmall)
                        }
                        parseResult.details?.let {
                            Text(text = "• Details: $it", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            // Template Selection Button
            OutlinedButton(
                onClick = { showTemplatePicker = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Label, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Selected Template: ${selectedTemplateKey.capitalize()}")
            }

            // Template Picker Dialog
            if (showTemplatePicker) {
                AlertDialog(
                    onDismissRequest = { showTemplatePicker = false },
                    title = { Text("Choose Card Template") },
                    text = {
                        TemplatePicker(
                            templates = com.giftvault.app.core.templates.TemplateRegistry.getAllTemplates(),
                            selectedTemplate = selectedTemplateKey,
                            onTemplateSelected = {
                                selectedTemplateKey = it.templateKey
                                showTemplatePicker = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 280.dp)
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = { showTemplatePicker = false }) {
                            Text("Done")
                        }
                    }
                )
            }


            // Confirm & Cancel Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        onConfirm(
                            merchantName,
                            cardNumber,
                            pin,
                            amount,
                            expiryDate,
                            selectedTemplateKey,
                            category,
                            if (attachReceipt) parseResult.imageUri else null
                        )
                    },
                    enabled = merchantName.isNotBlank() && cardNumber.isNotBlank(),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Auto-Fill Form")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
