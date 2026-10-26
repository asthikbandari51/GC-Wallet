package com.giftvault.app.ui.addcard

import android.app.DatePickerDialog
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.giftvault.app.core.receipts.CameraUtils
import com.giftvault.app.core.templates.TemplateManifest
import com.giftvault.app.core.templates.TemplateRegistry
import com.giftvault.app.domain.model.CardCategory
import com.giftvault.app.ui.components.AddReceiptBottomSheet
import com.giftvault.app.ui.components.GiftCardVisual
import com.giftvault.app.ui.components.glass.*
import com.giftvault.app.ui.reviewparse.ParseReviewSheet
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCardStepperScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddCardViewModel = hiltViewModel()
) {
    val tokens = currentGlassTokens()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    var isPinVisible by remember { mutableStateOf(false) }
    var showTemplateSheet by remember { mutableStateOf(false) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            viewModel.parseFromImage(tempCameraUri!!)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.parseFromImage(uri)
        }
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            Toast.makeText(
                context,
                if (uiState.isEditMode) "Gift Card updated!" else "Gift Card saved!",
                Toast.LENGTH_SHORT
            ).show()
            onNavigateBack()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { err ->
            Toast.makeText(context, err, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (uiState.isEditMode) "Edit Gift Card" else "Add Gift Card",
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
        bottomBar = {
            LiquidGlassSurface(
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                elevation = 8.dp,
                surfaceBrush = tokens.elevatedGradient,
                borderBrush = tokens.borderGradient,
                borderWidth = 1.dp,
                tokens = tokens
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    val isEnabled = !uiState.isLoading && uiState.cardNumber.isNotBlank()
                    LiquidGlassButton(
                        onClick = { viewModel.saveCard() },
                        enabled = isEnabled,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        style = GlassButtonStyle.PRIMARY,
                        tokens = tokens
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            if (isEnabled) {
                                Icon(Icons.Rounded.Check, contentDescription = null, tint = Color.White)
                                Spacer(Modifier.width(8.dp))
                            }
                            Text(
                                text = if (uiState.isEditMode) "Save Changes" else if (isEnabled) "✓ Save Gift Card" else "Save Gift Card",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. SMART AUTO-FILL COMPACT LIQUID GLASS ACTION GROUP
            LiquidGlassSurface(
                shape = RoundedCornerShape(20.dp),
                elevation = 2.dp,
                surfaceBrush = tokens.surfaceGradient,
                borderBrush = tokens.activeBorderGradient,
                borderWidth = 1.dp,
                modifier = Modifier.fillMaxWidth(),
                tokens = tokens
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = tokens.primaryAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Smart Auto-Fill",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = tokens.textPrimary
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Scan image, upload receipt, or paste SMS to auto-detect details.",
                        style = MaterialTheme.typography.labelSmall,
                        color = tokens.textSecondary
                    )
                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LiquidGlassButton(
                            onClick = {
                                val uri = CameraUtils.createTempCameraUri(context)
                                tempCameraUri = uri
                                cameraLauncher.launch(uri)
                            },
                            modifier = Modifier.weight(1f),
                            height = 38.dp,
                            text = "Camera",
                            icon = Icons.Rounded.PhotoCamera,
                            style = GlassButtonStyle.PRIMARY,
                            shape = RoundedCornerShape(12.dp),
                            tokens = tokens
                        )

                        LiquidGlassButton(
                            onClick = {
                                galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                            modifier = Modifier.weight(1f),
                            height = 38.dp,
                            text = "Gallery",
                            icon = Icons.Rounded.Image,
                            style = GlassButtonStyle.PRIMARY,
                            shape = RoundedCornerShape(12.dp),
                            tokens = tokens
                        )

                        LiquidGlassButton(
                            onClick = {
                                val text = clipboardManager.getText()?.text
                                if (!text.isNullOrBlank()) {
                                    viewModel.parseFromText(text)
                                } else {
                                    viewModel.showPasteTextDialog()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            height = 38.dp,
                            text = "Paste",
                            icon = Icons.Rounded.ContentPaste,
                            style = GlassButtonStyle.SECONDARY,
                            shape = RoundedCornerShape(12.dp),
                            tokens = tokens
                        )
                    }
                }
            }

            // 2. LIVE CARD PREVIEW
            GiftCardVisual(
                merchantName = uiState.merchantName.ifBlank { uiState.matchedTemplate?.displayName ?: "Brand Name" },
                cardNumber = uiState.cardNumber.ifBlank { "0000 0000 0000 0000" },
                balance = uiState.balance.toDoubleOrNull() ?: 0.0,
                currency = uiState.currency,
                expiryDate = uiState.expiryDate?.toString(),
                templateKey = uiState.selectedTemplateKey,
                modifier = Modifier.fillMaxWidth()
            )

            // Reusable iOS Frosted Field Colors
            val glassFieldColors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = if (tokens.isDark) Color(0x18FFFFFF) else Color(0x0C000000),
                unfocusedContainerColor = if (tokens.isDark) Color(0x0DFFFFFF) else Color(0x05000000),
                disabledContainerColor = if (tokens.isDark) Color(0x0AFFFFFF) else Color(0x03000000),
                focusedBorderColor = tokens.primaryAccent,
                unfocusedBorderColor = tokens.borderDefault,
                disabledBorderColor = tokens.borderSubtle,
                focusedLabelColor = tokens.primaryAccent,
                unfocusedLabelColor = tokens.textSecondary,
                disabledLabelColor = tokens.textTertiary,
                focusedTextColor = tokens.textPrimary,
                unfocusedTextColor = tokens.textPrimary,
                disabledTextColor = tokens.textPrimary,
                focusedLeadingIconColor = tokens.primaryAccent,
                unfocusedLeadingIconColor = tokens.iconSecondary,
                disabledLeadingIconColor = tokens.iconSecondary.copy(alpha = 0.6f),
                focusedTrailingIconColor = tokens.primaryAccent,
                unfocusedTrailingIconColor = tokens.iconSecondary,
                disabledTrailingIconColor = tokens.iconSecondary.copy(alpha = 0.6f),
                cursorColor = tokens.primaryAccent
            )

            // 3. BRAND & TEMPLATE SECTION (Flush, No Empty Space)
            LiquidGlassSurface(
                shape = RoundedCornerShape(20.dp),
                elevation = 2.dp,
                surfaceBrush = tokens.surfaceGradient,
                borderBrush = tokens.borderGradient,
                borderWidth = 1.dp,
                modifier = Modifier.fillMaxWidth(),
                tokens = tokens
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.merchantName,
                        onValueChange = { name ->
                            viewModel.onMerchantNameChanged(name)
                            // Real-time auto-matching merchant name to template & category
                            val matched = TemplateRegistry.getTemplateForMerchant(name)
                            if (matched != null) {
                                viewModel.onTemplateSelected(matched)
                            }
                        },
                        label = { Text("Merchant / Brand Name") },
                        placeholder = { Text("e.g. Swiggy, Amazon, Croma") },
                        leadingIcon = { Icon(Icons.Rounded.Store, contentDescription = null, tint = tokens.primaryAccent) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = glassFieldColors,
                        singleLine = true
                    )

                    LiquidGlassButton(
                        onClick = { showTemplateSheet = true },
                        text = "Template",
                        icon = Icons.Rounded.Style,
                        style = GlassButtonStyle.SECONDARY,
                        shape = RoundedCornerShape(14.dp),
                        height = 54.dp,
                        fontSize = 12.sp,
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        tokens = tokens
                    )
                }
            }

            // 4. VOUCHER CREDENTIALS SECTION CARD
            LiquidGlassSurface(
                shape = RoundedCornerShape(20.dp),
                elevation = 2.dp,
                surfaceBrush = tokens.surfaceGradient,
                borderBrush = tokens.borderGradient,
                borderWidth = 1.dp,
                modifier = Modifier.fillMaxWidth(),
                tokens = tokens
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "VOUCHER CREDENTIALS",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = tokens.primaryAccent
                    )

                    OutlinedTextField(
                        value = uiState.cardNumber,
                        onValueChange = { viewModel.onCardNumberChanged(it) },
                        label = { Text("Card Number / Voucher Code *") },
                        placeholder = { Text("Enter or paste code", color = tokens.textTertiary) },
                        leadingIcon = { Icon(Icons.Rounded.CreditCard, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = glassFieldColors,
                        singleLine = true,
                        isError = uiState.cardNumber.isBlank()
                    )

                    OutlinedTextField(
                        value = uiState.pin,
                        onValueChange = { viewModel.onPinChanged(it) },
                        label = { Text("PIN / Security Code (Optional)") },
                        placeholder = { Text("••••", color = tokens.textTertiary) },
                        leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { isPinVisible = !isPinVisible }) {
                                Icon(
                                    if (isPinVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                    contentDescription = "Toggle PIN Visibility"
                                )
                            }
                        },
                        visualTransformation = if (isPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = glassFieldColors,
                        singleLine = true
                    )
                }
            }

            // 5. VALUE & VALIDITY SECTION CARD
            LiquidGlassSurface(
                shape = RoundedCornerShape(20.dp),
                elevation = 2.dp,
                surfaceBrush = tokens.surfaceGradient,
                borderBrush = tokens.borderGradient,
                borderWidth = 1.dp,
                modifier = Modifier.fillMaxWidth(),
                tokens = tokens
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "VALUE & VALIDITY",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = tokens.primaryAccent
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.balance,
                            onValueChange = { viewModel.onBalanceChanged(it) },
                            label = { Text("Current Balance *") },
                            leadingIcon = { Text("₹", fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = glassFieldColors,
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = uiState.initialValue,
                            onValueChange = { viewModel.onInitialValueChanged(it) },
                            label = { Text("Initial Value") },
                            leadingIcon = { Text("₹", fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = glassFieldColors,
                            singleLine = true
                        )
                    }

                    val calendar = Calendar.getInstance()
                    val datePickerDialog = DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            viewModel.onExpiryDateChanged(LocalDate.of(year, month + 1, dayOfMonth))
                        },
                        uiState.expiryDate?.year ?: calendar.get(Calendar.YEAR),
                        (uiState.expiryDate?.monthValue?.minus(1)) ?: calendar.get(Calendar.MONTH),
                        uiState.expiryDate?.dayOfMonth ?: calendar.get(Calendar.DAY_OF_MONTH)
                    )

                    OutlinedTextField(
                        value = uiState.expiryDate?.format(DateTimeFormatter.ofPattern("dd MMM yyyy")) ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Expiry Date (Optional)") },
                        placeholder = { Text("Select Date") },
                        leadingIcon = { Icon(Icons.Rounded.CalendarToday, contentDescription = null) },
                        trailingIcon = {
                            if (uiState.expiryDate != null) {
                                IconButton(onClick = { viewModel.onExpiryDateChanged(null) }) {
                                    Icon(Icons.Rounded.Clear, contentDescription = "Clear date")
                                }
                            } else {
                                IconButton(onClick = { datePickerDialog.show() }) {
                                    Icon(Icons.Rounded.ArrowDropDown, contentDescription = "Pick date")
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { datePickerDialog.show() },
                        shape = RoundedCornerShape(14.dp),
                        enabled = false,
                        colors = glassFieldColors
                    )
                }
            }

            // 6. CLASSIFICATION & NOTES SECTION CARD
            LiquidGlassSurface(
                shape = RoundedCornerShape(20.dp),
                elevation = 2.dp,
                surfaceBrush = tokens.surfaceGradient,
                borderBrush = tokens.borderGradient,
                borderWidth = 1.dp,
                modifier = Modifier.fillMaxWidth(),
                tokens = tokens
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "CLASSIFICATION & NOTES",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = tokens.primaryAccent
                    )

                    Text(
                        "Category",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = tokens.textSecondary
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(CardCategory.entries.toTypedArray()) { cat ->
                            LiquidGlassChip(
                                selected = uiState.category == cat,
                                onClick = { viewModel.onCategoryChanged(cat) },
                                label = cat.displayName,
                                shape = RoundedCornerShape(12.dp),
                                tokens = tokens
                            )
                        }
                    }

                    if (uiState.bankCards.isNotEmpty()) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "Linked Bank Card Profile",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = tokens.textSecondary
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item {
                                LiquidGlassChip(
                                    selected = uiState.selectedBankCardId == null,
                                    onClick = { viewModel.onBankCardSelected(null) },
                                    label = "None / Unlinked",
                                    shape = RoundedCornerShape(12.dp),
                                    tokens = tokens
                                )
                            }
                            items(uiState.bankCards) { bankCard ->
                                LiquidGlassChip(
                                    selected = uiState.selectedBankCardId == bankCard.id,
                                    onClick = { viewModel.onBankCardSelected(bankCard.id) },
                                    label = "${bankCard.cardNickname} (•• ${bankCard.lastFourDigits})",
                                    shape = RoundedCornerShape(12.dp),
                                    tokens = tokens
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = uiState.notes,
                        onValueChange = { viewModel.onNotesChanged(it) },
                        label = { Text("Notes & Terms") },
                        placeholder = { Text("Customer support, terms, store restrictions...") },
                        leadingIcon = { Icon(Icons.Rounded.Notes, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = glassFieldColors,
                        maxLines = 4
                    )
                }
            }

            // 7. RECEIPT PROOFS SECTION CARD
            LiquidGlassSurface(
                shape = RoundedCornerShape(20.dp),
                elevation = 2.dp,
                surfaceBrush = tokens.surfaceGradient,
                borderBrush = tokens.borderGradient,
                borderWidth = 1.dp,
                modifier = Modifier.fillMaxWidth(),
                tokens = tokens
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "RECEIPT & BILL PROOFS",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = tokens.primaryAccent
                        )
                        LiquidGlassButton(
                            onClick = { viewModel.showAddReceiptSheet() },
                            text = "Add Receipt",
                            icon = Icons.Rounded.Add,
                            style = GlassButtonStyle.SECONDARY,
                            height = 36.dp,
                            shape = RoundedCornerShape(12.dp),
                            tokens = tokens
                        )
                    }

                    if (uiState.pendingReceiptUris.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            items(uiState.pendingReceiptUris) { uri ->
                                Box(
                                    modifier = Modifier
                                        .size(70.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(1.dp, tokens.border, RoundedCornerShape(12.dp))
                                ) {
                                    AsyncImage(
                                        model = uri,
                                        contentDescription = "Receipt thumbnail",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    IconButton(
                                        onClick = { viewModel.removePendingReceiptUri(uri) },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .size(22.dp)
                                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                    ) {
                                        Icon(
                                            Icons.Rounded.Close,
                                            contentDescription = "Remove receipt",
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Text(
                            "No receipts attached yet. Attach purchase invoices or gift receipts.",
                            style = MaterialTheme.typography.bodySmall,
                            color = tokens.textTertiary
                        )
                    }
                }
            }
        }
    }

    // Modal Bottom Sheets
    if (showTemplateSheet) {
        ModalBottomSheet(
            onDismissRequest = { showTemplateSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color.Transparent,
            scrimColor = if (tokens.isDark) Color(0xAA050814) else Color(0x660F172A),
            dragHandle = null
        ) {
            LiquidGlassSurface(
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                elevation = 16.dp,
                borderBrush = tokens.activeBorderGradient,
                tokens = tokens,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp)
                        .padding(top = 12.dp, bottom = 24.dp)
                ) {
                    // iOS Frosted Drag Handle
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .width(40.dp)
                            .height(5.dp)
                            .clip(CircleShape)
                            .background(if (tokens.isDark) Color(0x44FFFFFF) else Color(0x33000000))
                    )
                    Spacer(Modifier.height(14.dp))

                    Text(
                        "Select Brand Template",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = tokens.textPrimary
                    )
                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = uiState.brandSearchQuery,
                        onValueChange = { viewModel.onBrandSearchChanged(it) },
                        placeholder = { Text("Search 26+ Brands...", color = tokens.textTertiary) },
                        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = tokens.iconSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = CircleShape,
                        singleLine = true
                    )

                    Spacer(Modifier.height(12.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(350.dp)
                    ) {
                        items(uiState.filteredTemplates, key = { it.templateKey }) { template ->
                            val resId = remember(template.templateKey) {
                                context.resources.getIdentifier(template.templateKey, "drawable", context.packageName)
                            }

                            LiquidGlassSurface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.onTemplateSelected(template)
                                        showTemplateSheet = false
                                    },
                                shape = RoundedCornerShape(16.dp),
                                surfaceBrush = tokens.surfaceGradient,
                                borderBrush = tokens.borderGradient,
                                borderWidth = 1.dp,
                                tokens = tokens
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(70.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(tokens.surfaceSubtle),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (resId != 0) {
                                            AsyncImage(
                                                model = resId,
                                                contentDescription = template.displayName,
                                                contentScale = ContentScale.Crop,
                                                filterQuality = FilterQuality.High,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            Text(
                                                template.displayName.take(2).uppercase(),
                                                fontWeight = FontWeight.Bold,
                                                color = tokens.textPrimary
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        template.displayName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        color = tokens.textPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (uiState.showAddReceiptSheet) {
        AddReceiptBottomSheet(
            onDismiss = { viewModel.hideAddReceiptSheet() },
            onImageSelected = { uri ->
                viewModel.addPendingReceiptUri(uri)
                viewModel.hideAddReceiptSheet()
            }
        )
    }

    if (uiState.showParseReviewSheet && uiState.parseResult != null) {
        ParseReviewSheet(
            parseResult = uiState.parseResult!!,
            onDismiss = { viewModel.hideParseReviewSheet() },
            onConfirm = { merchant, number, pin, balance, expiry, tKey, cat, receiptUri ->
                viewModel.applyParseResult(merchant, number, pin, balance, expiry, tKey, cat, receiptUri)
            }
        )
    }

    if (uiState.showPasteTextDialog) {
        var pastedText by remember { mutableStateOf("") }
        LiquidGlassDialog(
            onDismissRequest = { viewModel.hidePasteTextDialog() },
            title = "Paste Card Text / SMS",
            tokens = tokens,
            confirmButton = {
                LiquidGlassButton(
                    onClick = { viewModel.parseFromText(pastedText) },
                    text = "Parse Text",
                    style = GlassButtonStyle.PRIMARY,
                    tokens = tokens
                )
            },
            dismissButton = {
                LiquidGlassButton(
                    onClick = { viewModel.hidePasteTextDialog() },
                    text = "Cancel",
                    style = GlassButtonStyle.SECONDARY,
                    tokens = tokens
                )
            }
        ) {
            OutlinedTextField(
                value = pastedText,
                onValueChange = { pastedText = it },
                placeholder = { Text("Paste SMS, Email or Voucher text here...", color = tokens.textTertiary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                maxLines = 6
            )
        }
    }
}
