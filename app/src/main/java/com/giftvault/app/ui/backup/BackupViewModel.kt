package com.giftvault.app.ui.backup

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.giftvault.app.core.backup.BackupManager
import com.giftvault.app.core.backup.RestoreManager
import com.giftvault.app.core.security.SteganographyManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BackupUiState(
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val message: String? = null,
    val error: String? = null
)

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val backupManager: BackupManager,
    private val restoreManager: RestoreManager,
    private val steganographyManager: SteganographyManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    fun performExport(password: String, uri: Uri) {
        if (password.isBlank()) {
            _uiState.update { it.copy(error = "Password cannot be empty") }
            return
        }
        _uiState.update { it.copy(isExporting = true, error = null, message = null) }
        viewModelScope.launch {
            val result = backupManager.exportBackup(password, uri)
            _uiState.update { state ->
                state.copy(
                    isExporting = false,
                    message = if (result.isSuccess) "Backup successfully exported" else null,
                    error = result.exceptionOrNull()?.message
                )
            }
        }
    }

    fun performImport(password: String, uri: Uri) {
        if (password.isBlank()) {
            _uiState.update { it.copy(error = "Password cannot be empty") }
            return
        }
        _uiState.update { it.copy(isImporting = true, error = null, message = null) }
        viewModelScope.launch {
            val result = restoreManager.importBackup(password, uri)
            _uiState.update { state ->
                state.copy(
                    isImporting = false,
                    message = if (result.isSuccess) "Successfully restored ${result.getOrNull()} cards" else null,
                    error = result.exceptionOrNull()?.message
                )
            }
        }
    }

    fun performStegoExport(password: String) {
        if (password.isBlank()) {
            _uiState.update { it.copy(error = "Password cannot be empty") }
            return
        }
        _uiState.update { it.copy(isExporting = true, error = null, message = null) }
        viewModelScope.launch {
            try {
                val backupBytes = backupManager.createBackupDataJsonBytes()
                val fullPayload = steganographyManager.encryptPayload(backupBytes, password)
                val carrier = steganographyManager.createScenicCarrierBitmap()
                val stegoBitmap = steganographyManager.embedPayloadIntoBitmap(carrier, fullPayload)
                val uri = steganographyManager.saveStegoImageToGallery(stegoBitmap)
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        message = if (uri != null) "Steganographic vault hidden in Photo Gallery (Pictures/GiftVault)!" else "Failed to save steganographic photo"
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isExporting = false, error = e.message) }
            }
        }
    }

    fun performStegoImport(password: String, uri: Uri, context: Context) {
        if (password.isBlank()) {
            _uiState.update { it.copy(error = "Password cannot be empty") }
            return
        }
        _uiState.update { it.copy(isImporting = true, error = null, message = null) }
        viewModelScope.launch {
            try {
                val ins = context.contentResolver.openInputStream(uri)
                    ?: throw IllegalArgumentException("Cannot open image")
                val options = BitmapFactory.Options().apply {
                    inPremultiplied = false
                    inScaled = false
                    inPreferredConfig = android.graphics.Bitmap.Config.ARGB_8888
                }
                val bitmap = BitmapFactory.decodeStream(ins, null, options)
                    ?: throw IllegalArgumentException("Not a valid image bitmap")
                val decrypted = steganographyManager.extractAndDecryptPayload(bitmap, password)
                val count = restoreManager.restoreFromJsonBytes(decrypted)
                _uiState.update {
                    it.copy(
                        isImporting = false,
                        message = "Successfully extracted and restored $count items from Steganographic image!"
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isImporting = false, error = e.message) }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(message = null, error = null) }
    }
}
