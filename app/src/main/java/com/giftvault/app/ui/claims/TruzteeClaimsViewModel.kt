package com.giftvault.app.ui.claims

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.giftvault.app.data.repository.BankCardRepository
import com.giftvault.app.data.repository.TruzteeClaimRepository
import com.giftvault.app.domain.model.BankCard
import com.giftvault.app.domain.model.ClaimStatus
import com.giftvault.app.domain.model.TruzteeClaim
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@Immutable
data class TruzteeClaimsUiState(
    val claims: List<TruzteeClaim> = emptyList(),
    val bankCards: List<BankCard> = emptyList(),
    val isLoading: Boolean = true,
    val isFormOpen: Boolean = false,
    val orderIdInput: String = "",
    val transactionIdInput: String = "",
    val merchantNameInput: String = "",
    val amountInput: String = "",
    val statusInput: ClaimStatus = ClaimStatus.PENDING,
    val selectedBankCard: BankCard? = null,
    val notesInput: String = "",
    val errorMessage: String? = null
)

@HiltViewModel
class TruzteeClaimsViewModel @Inject constructor(
    private val claimRepository: TruzteeClaimRepository,
    private val bankCardRepository: BankCardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TruzteeClaimsUiState())
    val uiState: StateFlow<TruzteeClaimsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                claimRepository.getAllClaims(),
                bankCardRepository.getAllBankCards()
            ) { claims, bankCards ->
                TruzteeClaimsUiState(
                    claims = claims,
                    bankCards = bankCards,
                    selectedBankCard = bankCards.firstOrNull { it.isPrimary } ?: bankCards.firstOrNull(),
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun openAddDialog() {
        _uiState.update {
            it.copy(
                isFormOpen = true,
                orderIdInput = "",
                transactionIdInput = "",
                merchantNameInput = "",
                amountInput = "",
                statusInput = ClaimStatus.PENDING,
                notesInput = "",
                errorMessage = null
            )
        }
    }

    fun closeDialog() {
        _uiState.update { it.copy(isFormOpen = false, errorMessage = null) }
    }

    fun onOrderIdChanged(v: String) = _uiState.update { it.copy(orderIdInput = v) }
    fun onTransactionIdChanged(v: String) = _uiState.update { it.copy(transactionIdInput = v) }
    fun onMerchantNameChanged(v: String) = _uiState.update { it.copy(merchantNameInput = v) }
    fun onAmountChanged(v: String) = _uiState.update { it.copy(amountInput = v.filter { c -> c.isDigit() || c == '.' }) }
    fun onStatusChanged(s: ClaimStatus) = _uiState.update { it.copy(statusInput = s) }
    fun onBankCardSelected(b: BankCard?) = _uiState.update { it.copy(selectedBankCard = b) }
    fun onNotesChanged(n: String) = _uiState.update { it.copy(notesInput = n) }

    fun saveClaim() {
        val state = _uiState.value
        if (state.orderIdInput.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter an Order ID") }
            return
        }
        if (state.merchantNameInput.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter Merchant Name") }
            return
        }

        val amt = state.amountInput.toDoubleOrNull() ?: 0.0

        val claim = TruzteeClaim(
            orderId = state.orderIdInput.trim(),
            transactionId = state.transactionIdInput.trim(),
            merchantName = state.merchantNameInput.trim(),
            voucherAmount = amt,
            status = state.statusInput,
            bankCardId = state.selectedBankCard?.id,
            bankCardNickname = state.selectedBankCard?.cardNickname,
            notes = state.notesInput.trim().takeIf { it.isNotBlank() },
            createdAt = Instant.now()
        )

        viewModelScope.launch {
            claimRepository.insertClaim(claim)
            closeDialog()
        }
    }

    fun deleteClaim(id: Long) {
        viewModelScope.launch {
            claimRepository.deleteClaim(id)
        }
    }

    fun updateClaimStatus(claim: TruzteeClaim, newStatus: ClaimStatus) {
        viewModelScope.launch {
            val resolved = if (newStatus == ClaimStatus.RESOLVED) Instant.now() else null
            claimRepository.updateClaim(claim.copy(status = newStatus, resolvedAt = resolved))
        }
    }
}
