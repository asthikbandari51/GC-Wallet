package com.giftvault.app.ui.carddetails

import android.net.Uri
import androidx.compose.runtime.Immutable
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.giftvault.app.core.receipts.ReceiptStorageManager
import com.giftvault.app.core.security.BiometricAuthManager
import com.giftvault.app.data.repository.BankCardRepository
import com.giftvault.app.data.repository.CardRepository
import com.giftvault.app.data.repository.ReceiptRepository
import com.giftvault.app.data.repository.TransactionRepository
import com.giftvault.app.domain.model.BankCard
import com.giftvault.app.domain.model.GiftCard
import com.giftvault.app.domain.model.Receipt
import com.giftvault.app.domain.model.Transaction
import com.giftvault.app.domain.model.TransactionType
import com.giftvault.app.core.theme.ThemeManager
import com.giftvault.app.domain.usecase.UpdateBalanceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class CardDetailsUiState(
    val card: GiftCard? = null,
    val linkedBankCard: BankCard? = null,
    val transactions: List<Transaction> = emptyList(),
    val receipts: List<Receipt> = emptyList(),
    val isRevealed: Boolean = false,
    val isPrivacyMode: Boolean = false,
    val isLoading: Boolean = true,
    val isProcessingReceipt: Boolean = false,
    val showDeleteConfirmation: Boolean = false,
    val showArchiveConfirmation: Boolean = false,
    val showBalanceDialog: Boolean = false,
    val showAddReceiptSheet: Boolean = false,
    val showBarcodeDialog: Boolean = false,
    val showCashierDialog: Boolean = false,
    val viewerInitialIndex: Int? = null,
    val balanceDialogType: TransactionType = TransactionType.REDEEM,
    val isDeleted: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CardDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val cardRepository: CardRepository,
    private val bankCardRepository: BankCardRepository,
    private val transactionRepository: TransactionRepository,
    private val receiptRepository: ReceiptRepository,
    private val receiptStorageManager: ReceiptStorageManager,
    private val updateBalanceUseCase: UpdateBalanceUseCase,
    private val biometricAuthManager: BiometricAuthManager,
    val themeManager: ThemeManager
) : ViewModel() {

    val cardId: String = savedStateHandle.get<String>("cardId") ?: ""

    private val _uiState = MutableStateFlow(CardDetailsUiState())
    val uiState: StateFlow<CardDetailsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                cardRepository.getCardById(cardId),
                transactionRepository.getTransactionsForCard(cardId),
                receiptRepository.getReceiptsForCard(cardId),
                themeManager.isPrivacyModeEnabled
            ) { card, transactions, receipts, isPrivacy ->
                val linkedBank = if (card?.linkedCardId != null) {
                    bankCardRepository.getBankCardById(card.linkedCardId)
                } else null

                CardDetailsUiState(
                    card = card,
                    linkedBankCard = linkedBank,
                    transactions = transactions,
                    receipts = receipts,
                    isPrivacyMode = isPrivacy,
                    isLoading = false
                )
            }.catch { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }.collect { stateUpdate ->
                _uiState.update { currentState ->
                    currentState.copy(
                        card = stateUpdate.card,
                        linkedBankCard = stateUpdate.linkedBankCard,
                        transactions = stateUpdate.transactions,
                        receipts = stateUpdate.receipts,
                        isPrivacyMode = stateUpdate.isPrivacyMode,
                        isLoading = stateUpdate.isLoading,
                        error = stateUpdate.error
                    )
                }
            }
        }
    }

    fun showBarcodeDialog() {
        _uiState.update { it.copy(showBarcodeDialog = true) }
    }

    fun hideBarcodeDialog() {
        _uiState.update { it.copy(showBarcodeDialog = false) }
    }

    fun showCashierDialog() {
        _uiState.update { it.copy(showCashierDialog = true) }
    }

    fun hideCashierDialog() {
        _uiState.update { it.copy(showCashierDialog = false) }
    }

    fun toggleReveal(activity: FragmentActivity?) {
        if (_uiState.value.isRevealed) {
            _uiState.update { it.copy(isRevealed = false) }
        } else {
            if (activity != null) {
                requestReveal(activity)
            } else {
                _uiState.update { it.copy(isRevealed = true) }
            }
        }
    }

    fun requestReveal(activity: FragmentActivity) {
        biometricAuthManager.authenticate(
            activity = activity,
            onSuccess = {
                _uiState.update { it.copy(isRevealed = true) }
            },
            onError = { err ->
                _uiState.update { it.copy(error = err) }
            }
        )
    }

    fun onReceiptButtonClicked() {
        if (_uiState.value.receipts.isNotEmpty()) {
            openViewerAtIndex(0)
        } else {
            showAddReceiptSheet()
        }
    }

    fun openViewerAtIndex(index: Int) {
        _uiState.update { it.copy(viewerInitialIndex = index) }
    }

    fun closeViewer() {
        _uiState.update { it.copy(viewerInitialIndex = null) }
    }

    fun addReceipt(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isProcessingReceipt = true) }
            try {
                receiptStorageManager.saveReceiptImage(cardId, uri)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            } finally {
                _uiState.update { it.copy(isProcessingReceipt = false, showAddReceiptSheet = false) }
            }
        }
    }

    fun deleteReceipt(receipt: Receipt) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                receiptStorageManager.deleteReceipt(receipt.id, receipt.filePath)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun toggleFavorite() {
        val currentCard = _uiState.value.card ?: return
        viewModelScope.launch(Dispatchers.IO) {
            cardRepository.updateCard(currentCard.copy(isFavorite = !currentCard.isFavorite))
        }
    }

    fun showBalanceDialog(type: TransactionType) {
        _uiState.update {
            it.copy(showBalanceDialog = true, balanceDialogType = type)
        }
    }

    fun hideBalanceDialog() {
        _uiState.update { it.copy(showBalanceDialog = false) }
    }

    fun updateBalance(amount: Double, note: String?) {
        val type = _uiState.value.balanceDialogType
        viewModelScope.launch(Dispatchers.IO) {
            try {
                updateBalanceUseCase(cardId, amount, type, note)
                _uiState.update { it.copy(showBalanceDialog = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun showDeleteConfirmation() {
        _uiState.update { it.copy(showDeleteConfirmation = true) }
    }

    fun hideDeleteConfirmation() {
        _uiState.update { it.copy(showDeleteConfirmation = false) }
    }

    fun showArchiveConfirmation() {
        _uiState.update { it.copy(showArchiveConfirmation = true) }
    }

    fun hideArchiveConfirmation() {
        _uiState.update { it.copy(showArchiveConfirmation = false) }
    }

    fun archiveCard() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                cardRepository.archiveCard(cardId)
                _uiState.update { it.copy(showArchiveConfirmation = false, isDeleted = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun unarchiveCard() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                cardRepository.unarchiveCard(cardId)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun deleteCard() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                cardRepository.deleteCard(cardId)
                _uiState.update { it.copy(showDeleteConfirmation = false, isDeleted = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun showAddReceiptSheet() {
        _uiState.update { it.copy(showAddReceiptSheet = true) }
    }

    fun hideAddReceiptSheet() {
        _uiState.update { it.copy(showAddReceiptSheet = false) }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
}
