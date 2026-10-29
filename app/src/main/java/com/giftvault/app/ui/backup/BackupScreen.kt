package com.giftvault.app.ui.backup

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.giftvault.app.ui.components.glass.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    onNavigateBack: () -> Unit,
    viewModel: BackupViewModel = hiltViewModel()
) {
    val tokens = currentGlassTokens()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    // Export state
    var exportPassword by remember { mutableStateOf("") }
    var confirmExportPassword by remember { mutableStateOf("") }
    var showExportPassword by remember { mutableStateOf(false) }
    var exportPasswordError by remember { mutableStateOf<String?>(null) }
    
    // Restore state
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }
    var showRestorePasswordDialog by remember { mutableStateOf(false) }
    var restorePasswordInput by remember { mutableStateOf("") }
    var showRestorePassword by remember { mutableStateOf(false) }
    
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri: Uri? ->
        uri?.let { viewModel.performExport(exportPassword, it) }
    }
    
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            pendingImportUri = uri
            restorePasswordInput = ""
            showRestorePasswordDialog = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Backup & Restore",
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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 18.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Info Card
            LiquidGlassSurface(
                shape = RoundedCornerShape(18.dp),
                surfaceBrush = tokens.surfaceGradient,
                borderBrush = tokens.borderGradient,
                borderWidth = 1.dp,
                tokens = tokens
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    LiquidGlassIconButton(
                        onClick = {},
                        icon = Icons.Rounded.Shield,
                        contentDescription = "Security",
                        size = 40.dp,
                        iconSize = 22.dp,
                        tint = tokens.primaryAccent,
                        tokens = tokens
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Private Offline Storage",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = tokens.textPrimary
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "Your backups are encrypted exclusively with your personal password. Safe to store anywhere or export to any newer device.",
                            style = MaterialTheme.typography.bodySmall,
                            color = tokens.textSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
            
            // Status messages
            AnimatedVisibility(visible = uiState.message != null) {
                uiState.message?.let { msg ->
                    LiquidGlassSurface(
                        shape = RoundedCornerShape(14.dp),
                        surfaceBrush = tokens.surfaceGradient,
                        borderBrush = tokens.borderGradient,
                        borderWidth = 1.dp,
                        tokens = tokens
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                            Text(
                                text = msg,
                                color = Color(0xFF10B981),
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { viewModel.clearMessages() }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Rounded.Close, contentDescription = "Dismiss", tint = tokens.textTertiary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
            
            AnimatedVisibility(visible = uiState.error != null) {
                uiState.error?.let { err ->
                    LiquidGlassSurface(
                        shape = RoundedCornerShape(14.dp),
                        surfaceBrush = tokens.surfaceGradient,
                        borderBrush = tokens.borderGradient,
                        borderWidth = 1.dp,
                        tokens = tokens
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Rounded.Error, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
                            Text(
                                text = err,
                                color = Color(0xFFEF4444),
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { viewModel.clearMessages() }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Rounded.Close, contentDescription = "Dismiss", tint = tokens.textTertiary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // Restore Backup Card (Moved Up for quick access on new devices)
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                tokens = tokens
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.CloudDownload, contentDescription = null, tint = tokens.primaryAccent, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                "Restore from File",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = tokens.textPrimary
                            )
                            Text(
                                "Select a .giftvault backup file from your storage",
                                style = MaterialTheme.typography.bodySmall,
                                color = tokens.textTertiary
                            )
                        }
                    }
                    
                    LiquidGlassButton(
                        onClick = {
                            importLauncher.launch(arrayOf("*/*"))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isExporting && !uiState.isImporting,
                        style = GlassButtonStyle.PRIMARY,
                        tokens = tokens
                    ) {
                        if (uiState.isImporting) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(Modifier.width(10.dp))
                            Text("Restoring Data...", fontWeight = FontWeight.Bold, color = Color.White)
                        } else {
                            Icon(Icons.Rounded.FolderOpen, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Choose Backup File", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            // Export Backup Card
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                tokens = tokens
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Lock, contentDescription = null, tint = tokens.primaryAccent, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                "Export Secured Backup",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = tokens.textPrimary
                            )
                            Text(
                                "Save all gift cards, bank cards, and vouchers to a secured file",
                                style = MaterialTheme.typography.bodySmall,
                                color = tokens.textTertiary
                            )
                        }
                    }
                    
                    OutlinedTextField(
                        value = exportPassword,
                        onValueChange = { 
                            exportPassword = it
                            exportPasswordError = null
                        },
                        label = { Text("Backup Password") },
                        placeholder = { Text("Enter a password for this backup", color = tokens.textTertiary) },
                        visualTransformation = if (showExportPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        leadingIcon = { Icon(Icons.Rounded.Key, contentDescription = null, tint = tokens.iconSecondary) },
                        trailingIcon = {
                            IconButton(onClick = { showExportPassword = !showExportPassword }) {
                                Icon(
                                    imageVector = if (showExportPassword) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                    contentDescription = if (showExportPassword) "Hide password" else "Show password",
                                    tint = tokens.iconSecondary
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = confirmExportPassword,
                        onValueChange = { 
                            confirmExportPassword = it
                            exportPasswordError = null
                        },
                        label = { Text("Confirm Password") },
                        placeholder = { Text("Re-enter backup password", color = tokens.textTertiary) },
                        visualTransformation = if (showExportPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        leadingIcon = { Icon(Icons.Rounded.LockReset, contentDescription = null, tint = tokens.iconSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        isError = exportPasswordError != null
                    )

                    if (exportPasswordError != null) {
                        Text(
                            text = exportPasswordError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                    
                    LiquidGlassButton(
                        onClick = { 
                            if (exportPassword.isBlank()) {
                                exportPasswordError = "Password cannot be empty"
                                return@LiquidGlassButton
                            }
                            if (exportPassword != confirmExportPassword) {
                                exportPasswordError = "Passwords do not match"
                                return@LiquidGlassButton
                            }
                            exportPasswordError = null
                            exportLauncher.launch("giftvault_backup.giftvault")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isExporting && !uiState.isImporting,
                        style = GlassButtonStyle.PRIMARY,
                        tokens = tokens
                    ) {
                        if (uiState.isExporting) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(Modifier.width(10.dp))
                            Text("Exporting Backup...", fontWeight = FontWeight.Bold, color = Color.White)
                        } else {
                            Icon(Icons.Rounded.FileDownload, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Export & Save Backup", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            // Steganographic Photo Hide Section
            var stegoPassword by remember { mutableStateOf("") }
            val stegoImportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                uri?.let { viewModel.performStegoImport(stegoPassword, it, context) }
            }

            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                tokens = tokens
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Image, contentDescription = null, tint = tokens.primaryAccent, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Steganographic Photo Hide",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = tokens.textPrimary
                        )
                    }
                    Text(
                        "Conceals your encrypted wallet inside an ordinary scenic photo in your device photo gallery for plausible deniability.",
                        style = MaterialTheme.typography.bodySmall,
                        color = tokens.textSecondary
                    )

                    OutlinedTextField(
                        value = stegoPassword,
                        onValueChange = { stegoPassword = it },
                        label = { Text("Steganography Password") },
                        placeholder = { Text("Password for photo encoding", color = tokens.textTertiary) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null, tint = tokens.iconSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        LiquidGlassButton(
                            onClick = { viewModel.performStegoExport(stegoPassword) },
                            modifier = Modifier.weight(1f),
                            style = GlassButtonStyle.PRIMARY,
                            enabled = !uiState.isExporting && !uiState.isImporting,
                            tokens = tokens,
                            height = 44.dp
                        ) {
                            Text("Hide to Photo", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        LiquidGlassButton(
                            onClick = { stegoImportLauncher.launch("image/png") },
                            modifier = Modifier.weight(1f),
                            style = GlassButtonStyle.SECONDARY,
                            enabled = !uiState.isExporting && !uiState.isImporting,
                            tokens = tokens,
                            height = 44.dp
                        ) {
                            Text("Extract Photo", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = tokens.primaryAccent)
                        }
                    }
                }
            }
        }
    }

    // Modal Dialog: Password Prompt After File Selection
    if (showRestorePasswordDialog && pendingImportUri != null) {
        LiquidGlassDialog(
            onDismissRequest = { 
                showRestorePasswordDialog = false 
                pendingImportUri = null
            },
            title = "Enter Backup Password",
            tokens = tokens,
            confirmButton = {
                LiquidGlassButton(
                    onClick = {
                        val uri = pendingImportUri
                        if (uri != null && restorePasswordInput.isNotBlank()) {
                            showRestorePasswordDialog = false
                            viewModel.performImport(restorePasswordInput, uri)
                            pendingImportUri = null
                        }
                    },
                    text = "Restore Vault",
                    icon = Icons.Rounded.Key,
                    style = GlassButtonStyle.PRIMARY,
                    tokens = tokens,
                    enabled = restorePasswordInput.isNotBlank()
                )
            },
            dismissButton = {
                LiquidGlassButton(
                    onClick = { 
                        showRestorePasswordDialog = false 
                        pendingImportUri = null
                    },
                    text = "Cancel",
                    style = GlassButtonStyle.SUBTLE,
                    tokens = tokens
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "File selected. Please enter the password used to protect this backup file to decrypt and restore your cards.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = tokens.textSecondary
                )

                OutlinedTextField(
                    value = restorePasswordInput,
                    onValueChange = { restorePasswordInput = it },
                    label = { Text("Backup Password") },
                    placeholder = { Text("Enter password (e.g. 1234)", color = tokens.textTertiary) },
                    visualTransformation = if (showRestorePassword) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null, tint = tokens.iconSecondary) },
                    trailingIcon = {
                        IconButton(onClick = { showRestorePassword = !showRestorePassword }) {
                            Icon(
                                imageVector = if (showRestorePassword) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                contentDescription = if (showRestorePassword) "Hide password" else "Show password",
                                tint = tokens.iconSecondary
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )
            }
        }
    }
}
