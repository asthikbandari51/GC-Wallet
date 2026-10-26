package com.giftvault.app.ui.addcard

import android.app.DatePickerDialog
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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

import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.giftvault.app.core.receipts.CameraUtils
import com.giftvault.app.domain.model.CardCategory
import com.giftvault.app.domain.model.Receipt
import com.giftvault.app.ui.components.AddReceiptBottomSheet
import com.giftvault.app.ui.components.GiftCardVisual
import com.giftvault.app.ui.components.ReceiptsRow
import com.giftvault.app.ui.components.TemplatePicker
import com.giftvault.app.ui.reviewparse.ParseReviewSheet
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCardScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddCardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }

    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    var pastedRawText by remember { mutableStateOf("") }

    // Launcher for Gallery OCR Scan
    val galleryOcrLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { viewModel.parseFromImage(it) }
    }

    // Launcher for Camera OCR Scan
    val cameraOcrLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && tempCameraUri != null) {
            viewModel.parseFromImage(tempCameraUri!!)
        }
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onNavigateBack()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Gift Card") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 🚀 Scan / Import / Paste Quick Action Bar
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .border(
                            width = 2.dp,
                            brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF5D5FEF),
                                    Color(0xFF8E2DE2),
                                    Color(0xFF00C6FF)
                                )
                            ),
                            shape = RoundedCornerShape(20.dp)
                        ),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                    shadowElevation = 6.dp
                ) {

                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Smart Auto-Fill",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = "Extract card details & proof from receipt images or SMS/email text",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Camera OCR button
                            FilledTonalButton(
                                onClick = {
                                    val uri = CameraUtils.createTempCameraUri(context)
                                    tempCameraUri = uri
                                    cameraOcrLauncher.launch(uri)
                                },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Rounded.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Camera", style = MaterialTheme.typography.labelMedium)
                            }

                            // Gallery OCR button
                            FilledTonalButton(
                                onClick = {
                                    galleryOcrLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Rounded.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Gallery", style = MaterialTheme.typography.labelMedium)
                            }

                            // Clipboard / Text Paste button
                            FilledTonalButton(
                                onClick = {
                                    val clipboardText = clipboardManager.getText()?.text
                                    if (!clipboardText.isNullOrBlank()) {
                                        viewModel.parseFromText(clipboardText)
                                    } else {
                                        viewModel.showPasteTextDialog()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Rounded.ContentPaste, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Paste Text", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }

                // Card Visual Preview
                GiftCardVisual(
                    merchantName = uiState.merchantName.ifEmpty { "Merchant Name" },
                    cardNumber = uiState.cardNumber.ifEmpty { "0000 0000 0000 0000" },
                    balance = uiState.balance.toDoubleOrNull() ?: 0.0,
                    currency = uiState.currency,
                    expiryDate = uiState.expiryDate?.format(DateTimeFormatter.ofPattern("MM/yy")),
                    templateKey = uiState.selectedTemplateKey,
                    isRevealed = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Card Details", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

                OutlinedTextField(
                    value = uiState.merchantName,
                    onValueChange = viewModel::onMerchantNameChanged,
                    label = { Text("Merchant Name *") },
                    leadingIcon = { Icon(Icons.Rounded.Store, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (uiState.matchedTemplate != null) {
                    SuggestionChip(
                        onClick = { },
                        label = { Text("Matched: ${uiState.matchedTemplate?.displayName}") },
                        icon = { Icon(Icons.Rounded.CheckCircle, contentDescription = null) }
                    )
                }

                var showTemplates by remember { mutableStateOf(false) }
                OutlinedCard(
                    onClick = { showTemplates = !showTemplates },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Choose Template", modifier = Modifier.weight(1f))
                        Icon(
                            if (showTemplates) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                            contentDescription = null
                        )
                    }
                    AnimatedVisibility(visible = showTemplates) {
                        TemplatePicker(
                            templates = uiState.availableTemplates,
                            selectedTemplate = uiState.selectedTemplateKey,
                            onTemplateSelected = {
                                viewModel.onTemplateSelected(it)
                                showTemplates = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp)
                                .padding(8.dp)
                        )
                    }

                }

                OutlinedTextField(
                    value = uiState.cardNumber,
                    onValueChange = viewModel::onCardNumberChanged,
                    label = { Text("Card Number / Code *") },
                    leadingIcon = { Icon(Icons.Rounded.CreditCard, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                var pinVisible by remember { mutableStateOf(false) }
                OutlinedTextField(
                    value = uiState.pin,
                    onValueChange = viewModel::onPinChanged,
                    label = { Text("PIN / Passcode (Optional)") },
                    leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { pinVisible = !pinVisible }) {
                            Icon(
                                imageVector = if (pinVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                contentDescription = if (pinVisible) "Hide PIN" else "Show PIN"
                            )
                        }
                    },
                    visualTransformation = if (pinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.balance,
                        onValueChange = viewModel::onBalanceChanged,
                        label = { Text("Current Balance *") },
                        leadingIcon = { Icon(Icons.Rounded.AttachMoney, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = uiState.initialValue,
                        onValueChange = viewModel::onInitialValueChanged,
                        label = { Text("Initial Value") },
                        leadingIcon = { Icon(Icons.Rounded.Money, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Expiry Date Selector
                val calendar = Calendar.getInstance()
                val datePickerDialog = DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        viewModel.onExpiryDateChanged(LocalDate.of(year, month + 1, dayOfMonth))
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                )

                OutlinedTextField(
                    value = uiState.expiryDate?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: "",
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Expiry Date (Optional)") },
                    leadingIcon = { Icon(Icons.Rounded.CalendarToday, contentDescription = null) },
                    trailingIcon = {
                        if (uiState.expiryDate != null) {
                            IconButton(onClick = { viewModel.onExpiryDateChanged(null) }) {
                                Icon(Icons.Rounded.Clear, contentDescription = "Clear date")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { datePickerDialog.show() },
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Text("Category", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(CardCategory.values()) { cat ->
                        FilterChip(
                            selected = uiState.category == cat,
                            onClick = { viewModel.onCategoryChanged(cat) },
                            label = { Text(cat.displayName) }
                        )
                    }
                }


                // Notes Field
                OutlinedTextField(
                    value = uiState.notes,
                    onValueChange = viewModel::onNotesChanged,
                    label = { Text("Notes / Service Details") },
                    leadingIcon = { Icon(Icons.Rounded.Notes, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    shape = RoundedCornerShape(12.dp)
                )

                // Receipts section
                Text("Receipts & Image Proofs", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                ReceiptsRow(
                    receipts = uiState.pendingReceiptUris.map {
                        Receipt(
                            id = it.toString(),
                            cardId = "",
                            filePath = it.toString(),
                            createdAt = Instant.now()
                        )
                    },
                    onAddReceiptClick = { viewModel.showAddReceiptSheet() },
                    onReceiptClick = { _, _ -> },
                    onDeleteReceiptClick = { receipt ->
                        viewModel.removePendingReceiptUri(Uri.parse(receipt.filePath))
                    }
                )


                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.saveCard() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !uiState.isLoading && uiState.merchantName.isNotBlank() && uiState.cardNumber.isNotBlank(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Icon(Icons.Rounded.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Gift Card", style = MaterialTheme.typography.titleMedium)
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }

            // Parsing Loading Overlay
            if (uiState.isParsing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                        .clickable(enabled = false) {},
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Running Offline OCR & Parser...",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Extracting code, PIN, merchant & dates",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Add Receipt Bottom Sheet
        if (uiState.showAddReceiptSheet) {
            AddReceiptBottomSheet(
                onDismiss = { viewModel.hideAddReceiptSheet() },
                onImageSelected = { uri ->
                    viewModel.addPendingReceiptUri(uri)
                    viewModel.hideAddReceiptSheet()
                }
            )
        }

        // Raw Text Paste Dialog
        if (uiState.showPasteTextDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.hidePasteTextDialog() },
                title = { Text("Paste Raw Text") },
                text = {
                    Column {
                        Text(
                            text = "Paste text from SMS, email, or digital voucher receipt:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = pastedRawText,
                            onValueChange = { pastedRawText = it },
                            placeholder = { Text("e.g. Coupon Code: GYFRR...\nDate of Issue: 01-Jul-2026...") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 4,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.parseFromText(pastedRawText) },
                        enabled = pastedRawText.isNotBlank()
                    ) {
                        Text("Extract & Auto-Fill")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.hidePasteTextDialog() }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Parse Review Sheet
        if (uiState.showParseReviewSheet && uiState.parseResult != null) {
            ParseReviewSheet(
                parseResult = uiState.parseResult!!,
                onDismiss = { viewModel.hideParseReviewSheet() },
                onConfirm = { merchant, code, pin, amt, expiry, template, category, receiptUri ->
                    viewModel.applyParseResult(merchant, code, pin, amt, expiry, template, category, receiptUri)
                }
            )
        }
    }
}
