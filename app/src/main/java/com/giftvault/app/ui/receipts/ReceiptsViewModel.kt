package com.giftvault.app.ui.receipts

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.giftvault.app.core.database.dao.ReceiptDao
import com.giftvault.app.core.receipts.ReceiptStorageManager
import com.giftvault.app.data.mappers.toDomain
import com.giftvault.app.data.mappers.toEntity
import com.giftvault.app.data.repository.CardRepository
import com.giftvault.app.domain.model.Receipt
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReceiptUiItem(
    val receipt: Receipt,
    val merchantName: String
)

data class ReceiptsUiState(
    val receipts: List<ReceiptUiItem> = emptyList(),
    val selectedReceiptPath: String? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class ReceiptsViewModel @Inject constructor(
    private val receiptDao: ReceiptDao,
    private val cardRepository: CardRepository,
    private val storageManager: ReceiptStorageManager,
    private val receiptOcrAnalyzer: com.giftvault.app.core.ocr.ReceiptOcrAnalyzer
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReceiptsUiState())
    val uiState: StateFlow<ReceiptsUiState> = _uiState.asStateFlow()

    fun scanAndReconcileReceipt(sourceBitmap: android.graphics.Bitmap, cardId: String) {
        viewModelScope.launch {
            val card = cardRepository.getCardById(cardId).firstOrNull()
            val parsed = receiptOcrAnalyzer.analyzeReceipt(sourceBitmap, card?.balance)
            if (parsed.voucherDebit != null && card != null) {
                val newBalance = (card.balance - parsed.voucherDebit).coerceAtLeast(0.0)
                cardRepository.updateCard(card.copy(balance = newBalance))
            }
            if (parsed.receiptImagePath != null) {
                val receipt = Receipt(
                    id = java.util.UUID.randomUUID().toString(),
                    cardId = cardId,
                    filePath = parsed.receiptImagePath,
                    createdAt = java.time.Instant.now()
                )
                receiptDao.insertReceipt(receipt.toEntity())
            }
        }
    }

    init {
        loadReceipts()
    }

    private fun loadReceipts() {
        combine(
            receiptDao.getAllReceipts(),
            cardRepository.getAllCards()
        ) { receiptEntities, cards ->
            val items = receiptEntities.map { entity ->
                val card = cards.find { it.id == entity.cardId }
                ReceiptUiItem(
                    receipt = entity.toDomain(),
                    merchantName = card?.merchantName ?: "Unknown Merchant"
                )
            }.sortedByDescending { it.receipt.createdAt }
            
            _uiState.value = _uiState.value.copy(
                receipts = items,
                isLoading = false
            )
        }.launchIn(viewModelScope)
    }

    fun addReceipt(cardId: String, uri: Uri) {
        viewModelScope.launch {
            try {
                storageManager.saveReceiptImage(cardId, uri)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun deleteReceipt(receiptUiItem: ReceiptUiItem) {
        viewModelScope.launch {
            try {
                storageManager.deleteReceipt(
                    receiptId = receiptUiItem.receipt.id,
                    filePath = receiptUiItem.receipt.filePath
                )
                if (_uiState.value.selectedReceiptPath == receiptUiItem.receipt.filePath) {
                    selectReceiptForViewer(null)
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun selectReceiptForViewer(path: String?) {
        _uiState.value = _uiState.value.copy(selectedReceiptPath = path)
    }
}
