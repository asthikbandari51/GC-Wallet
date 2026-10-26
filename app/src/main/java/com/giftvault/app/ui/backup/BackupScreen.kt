package com.giftvault.app.ui.backup

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
    
    var exportPassword by remember { mutableStateOf("") }
    var importPassword by remember { mutableStateOf("") }
    
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri: Uri? ->
        uri?.let { viewModel.performExport(exportPassword, it) }
    }
    
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let { viewModel.performImport(importPassword, it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Offline Backup & Restore",
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
            LiquidGlassSurface(
                shape = RoundedCornerShape(16.dp),
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
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Info,
                        contentDescription = "Info",
                        tint = tokens.primaryAccent,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "All backups are 100% offline, AES-256 encrypted with your password, and protect all your gift cards, bank cards, claims & perk quotas without any cloud servers.",
                        style = MaterialTheme.typography.bodySmall,
                        color = tokens.textSecondary
                    )
                }
            }
            
            uiState.message?.let { msg ->
                Text(
                    text = msg,
                    color = Color(0xFF10B981),
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            
            uiState.error?.let { err ->
                Text(
                    text = err,
                    color = Color(0xFFEF4444),
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
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
                    Text(
                        "Export Backup",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = tokens.textPrimary
                    )
                    
                    OutlinedTextField(
                        value = exportPassword,
                        onValueChange = { exportPassword = it },
                        label = { Text("Backup Password") },
                        placeholder = { Text("Enter a strong password", color = tokens.textTertiary) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null, tint = tokens.iconSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )
                    
                    LiquidGlassButton(
                        onClick = { 
                            if (exportPassword.isNotEmpty()) {
                                exportLauncher.launch("giftvault_backup.giftvault")
                            } else {
                                viewModel.performExport("", Uri.EMPTY)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isExporting && !uiState.isImporting,
                        style = GlassButtonStyle.PRIMARY,
                        tokens = tokens
                    ) {
                        if (uiState.isExporting) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Rounded.FileDownload, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Save .giftvault File", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            // Restore Backup Card
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
                    Text(
                        "Restore Backup",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = tokens.textPrimary
                    )
                    
                    OutlinedTextField(
                        value = importPassword,
                        onValueChange = { importPassword = it },
                        label = { Text("Backup Password") },
                        placeholder = { Text("Password used during export", color = tokens.textTertiary) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null, tint = tokens.iconSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )
                    
                    LiquidGlassButton(
                        onClick = {
                            if (importPassword.isNotEmpty()) {
                                importLauncher.launch(arrayOf("application/octet-stream"))
                            } else {
                                viewModel.performImport("", Uri.EMPTY)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isExporting && !uiState.isImporting,
                        style = GlassButtonStyle.SECONDARY,
                        tokens = tokens
                    ) {
                        if (uiState.isImporting) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = tokens.primaryAccent, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Rounded.FileUpload, contentDescription = null, tint = tokens.primaryAccent, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Select .giftvault File", fontWeight = FontWeight.Bold, color = tokens.primaryAccent)
                        }
                    }
                }
            }

            // Steganographic Gallery Hide (v0.9 Module 1)
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
                            "Steganographic Gallery Hide",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = tokens.textPrimary
                        )
                    }
                    Text(
                        "Conceals your encrypted vault inside an ordinary photo in your public photo gallery (Pictures/GiftVault) using 3-bit ARGB LSB pixel encoding. Mathematically imperceptible (PSNR > 51dB) for plausible deniability.",
                        style = MaterialTheme.typography.bodySmall,
                        color = tokens.textSecondary
                    )

                    OutlinedTextField(
                        value = stegoPassword,
                        onValueChange = { stegoPassword = it },
                        label = { Text("Steganography Password") },
                        placeholder = { Text("Password for pixel encoding", color = tokens.textTertiary) },
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
}
