package com.giftvault.app.ui.rupay

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.giftvault.app.data.repository.BankCardRepository
import com.giftvault.app.domain.model.BankCard
import com.giftvault.app.data.repository.CardRepository
import com.giftvault.app.domain.model.CardCategory
import com.giftvault.app.domain.model.CardStatus
import com.giftvault.app.domain.model.GiftCard
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

@Immutable
data class RuPayRedeemUiState(
    val bankCards: List<BankCard> = emptyList(),
    val selectedCard: BankCard? = null,
    val isLoading: Boolean = true,
    val claimedVoucherCode: String? = null
)

@HiltViewModel
class RuPayRedeemViewModel @Inject constructor(
    private val bankCardRepository: BankCardRepository,
    private val cardRepository: CardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RuPayRedeemUiState())
    val uiState: StateFlow<RuPayRedeemUiState> = _uiState.asStateFlow()

    private val _voucherClaimedEvent = MutableSharedFlow<GiftCard>()
    val voucherClaimedEvent: SharedFlow<GiftCard> = _voucherClaimedEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            bankCardRepository.getAllBankCards().collect { cards ->
                val primary = cards.firstOrNull { it.isPrimary } ?: cards.firstOrNull()
                _uiState.update {
                    it.copy(
                        bankCards = cards,
                        selectedCard = it.selectedCard ?: primary,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun selectCard(card: BankCard) {
        _uiState.update { it.copy(selectedCard = card) }
    }

    fun saveClaimedVoucher(code: String, pin: String?, merchant: String, amount: Double = 500.0) {
        viewModelScope.launch {
            val card = GiftCard(
                id = UUID.randomUUID().toString(),
                merchantName = merchant.ifBlank { "RuPay Rewards Voucher" },
                templateKey = "other",
                cardNumber = code,
                pin = pin,
                balance = amount,
                initialValue = amount,
                currency = "INR",
                expiryDate = LocalDate.now().plusMonths(3),
                category = CardCategory.ENTERTAINMENT,
                notes = "Auto-claimed via RuPay Automation Bot",
                status = CardStatus.ACTIVE,
                isFavorite = false,
                linkedCardId = _uiState.value.selectedCard?.id,
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )
            cardRepository.addCard(card)
            _uiState.update { it.copy(claimedVoucherCode = code) }
            _voucherClaimedEvent.emit(card)
        }
    }
}
