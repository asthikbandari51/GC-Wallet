package com.giftvault.app.ui.bankcards

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.giftvault.app.data.repository.BankCardRepository
import com.giftvault.app.domain.model.BankCard
import com.giftvault.app.domain.model.BankCardType
import com.giftvault.app.domain.model.CardNetwork
import com.giftvault.app.domain.model.CardTier
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.giftvault.app.core.theme.ThemeManager

@Immutable
enum class BankCardTypeFilter(val displayName: String) {
    ALL("All"),
    CREDIT("Credit"),
    DEBIT("Debit")
}

@Immutable
enum class BankCardNetworkFilter(val displayName: String) {
    ALL("All Networks"),
    RUPAY("RuPay"),
    VISA("Visa"),
    MASTERCARD("Mastercard"),
    AMEX("Amex")
}

@Immutable
data class ManageBankCardsUiState(
    val bankCards: List<BankCard> = emptyList(),
    val filteredCards: List<BankCard> = emptyList(),
    val searchQuery: String = "",
    val typeFilter: BankCardTypeFilter = BankCardTypeFilter.ALL,
    val networkFilter: BankCardNetworkFilter = BankCardNetworkFilter.ALL,
    val isLoading: Boolean = true,
    val isFormOpen: Boolean = false,
    val editingCard: BankCard? = null,
    val nicknameInput: String = "",
    val bankNameInput: String = "PNB",
    val cardTypeInput: BankCardType = BankCardType.CREDIT,
    val networkInput: CardNetwork = CardNetwork.RUPAY,
    val tierInput: CardTier = CardTier.RUPAY_SELECT,
    val cardNumberInput: String = "",
    val cvvInput: String = "",
    val expiryMonthInput: String = "",
    val expiryYearInput: String = "",
    val cardholderNameInput: String = "",
    val spendTargetInput: String = "", // Optional: left blank or "0" means no target
    val currentSpendInput: String = "0",
    val isPrimaryInput: Boolean = false,
    val revealedCardIds: Set<Long> = emptySet(),
    val revealedCvvCardIds: Set<Long> = emptySet(),
    val isPrivacyMode: Boolean = false,
    val hideBankCardCvv: Boolean = true,
    val requireAuthForCvv: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ManageBankCardsViewModel @Inject constructor(
    private val bankCardRepository: BankCardRepository,
    private val themeManager: ThemeManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManageBankCardsUiState())
    val uiState: StateFlow<ManageBankCardsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                bankCardRepository.getAllBankCards(),
                themeManager.isPrivacyModeEnabled,
                themeManager.hideBankCardCvv,
                themeManager.requireAuthForCvv
            ) { cards, isPrivacy, hideCvv, reqAuth ->
                listOf(cards, isPrivacy, hideCvv, reqAuth)
            }.collect { values ->
                @Suppress("UNCHECKED_CAST")
                val cards = values[0] as List<BankCard>
                val isPrivacy = values[1] as Boolean
                val hideCvv = values[2] as Boolean
                val reqAuth = values[3] as Boolean

                _uiState.update { 
                    it.copy(
                        bankCards = cards,
                        isPrivacyMode = isPrivacy,
                        hideBankCardCvv = hideCvv,
                        requireAuthForCvv = reqAuth,
                        isLoading = false
                    ) 
                }
                applyFilterAndSearch()
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFilterAndSearch()
    }

    fun onTypeFilterChanged(filter: BankCardTypeFilter) {
        _uiState.update { 
            it.copy(
                typeFilter = filter,
                networkFilter = if (filter == BankCardTypeFilter.ALL) BankCardNetworkFilter.ALL else it.networkFilter
            ) 
        }
        applyFilterAndSearch()
    }

    fun onNetworkFilterChanged(filter: BankCardNetworkFilter) {
        _uiState.update { it.copy(networkFilter = filter) }
        applyFilterAndSearch()
    }

    private fun applyFilterAndSearch() {
        val state = _uiState.value
        var list = state.bankCards

        // Apply Card Type filter (All, Credit, Debit)
        list = when (state.typeFilter) {
            BankCardTypeFilter.ALL -> list
            BankCardTypeFilter.CREDIT -> list.filter { it.cardType == BankCardType.CREDIT }
            BankCardTypeFilter.DEBIT -> list.filter { it.cardType == BankCardType.DEBIT }
        }

        // Apply Network filter if Credit or Debit is selected
        if (state.typeFilter != BankCardTypeFilter.ALL && state.networkFilter != BankCardNetworkFilter.ALL) {
            list = when (state.networkFilter) {
                BankCardNetworkFilter.ALL -> list
                BankCardNetworkFilter.RUPAY -> list.filter { it.network == CardNetwork.RUPAY }
                BankCardNetworkFilter.VISA -> list.filter { it.network == CardNetwork.VISA }
                BankCardNetworkFilter.MASTERCARD -> list.filter { it.network == CardNetwork.MASTERCARD }
                BankCardNetworkFilter.AMEX -> list.filter { it.network == CardNetwork.AMEX }
            }
        }

        // Apply search query
        if (state.searchQuery.isNotBlank()) {
            val q = state.searchQuery.trim().lowercase()
            list = list.filter {
                it.cardNickname.lowercase().contains(q) ||
                it.bankName.lowercase().contains(q) ||
                it.cardholderName.lowercase().contains(q) ||
                it.cardNumber.contains(q) ||
                it.lastFourDigits.contains(q) ||
                it.network.displayName.lowercase().contains(q) ||
                it.tier.displayName.lowercase().contains(q)
            }
        }

        _uiState.update { it.copy(filteredCards = list) }
    }

    fun openAddDialog() {
        _uiState.update {
            it.copy(
                isFormOpen = true,
                editingCard = null,
                nicknameInput = "",
                bankNameInput = "PNB",
                cardTypeInput = BankCardType.CREDIT,
                networkInput = CardNetwork.RUPAY,
                tierInput = CardTier.RUPAY_SELECT,
                cardNumberInput = "",
                cvvInput = "",
                expiryMonthInput = "",
                expiryYearInput = "",
                cardholderNameInput = "",
                spendTargetInput = "", // blank by default (optional)
                currentSpendInput = "0",
                isPrimaryInput = it.bankCards.isEmpty(),
                errorMessage = null
            )
        }
    }

    fun openEditDialog(card: BankCard) {
        _uiState.update {
            it.copy(
                isFormOpen = true,
                editingCard = card,
                nicknameInput = card.cardNickname,
                bankNameInput = card.bankName,
                cardTypeInput = card.cardType,
                networkInput = card.network,
                tierInput = card.tier,
                cardNumberInput = card.cardNumber,
                cvvInput = card.cvv,
                expiryMonthInput = card.expiryMonth,
                expiryYearInput = card.expiryYear,
                cardholderNameInput = card.cardholderName,
                spendTargetInput = if (card.hasSpendTarget) card.quarterlySpendTarget.toInt().toString() else "",
                currentSpendInput = card.currentQuarterSpend.toInt().toString(),
                isPrimaryInput = card.isPrimary,
                errorMessage = null
            )
        }
    }

    fun closeDialog() {
        _uiState.update { it.copy(isFormOpen = false, editingCard = null, errorMessage = null) }
    }

    fun onNicknameChanged(value: String) = _uiState.update { it.copy(nicknameInput = value) }
    fun onBankNameChanged(value: String) = _uiState.update { it.copy(bankNameInput = value) }
    fun onCardTypeChanged(value: BankCardType) = _uiState.update { it.copy(cardTypeInput = value) }
    
    fun onNetworkChanged(value: CardNetwork) {
        val defaultTierForNetwork = CardTier.getTiersForNetwork(value).firstOrNull() ?: CardTier.GENERIC_CLASSIC
        _uiState.update { 
            it.copy(
                networkInput = value,
                tierInput = defaultTierForNetwork
            ) 
        }
    }

    fun onTierChanged(value: CardTier) = _uiState.update { it.copy(tierInput = value) }

    fun onCardNumberChanged(value: String) {
        val filtered = value.filter { it.isDigit() || it == ' ' }.take(19)
        _uiState.update { it.copy(cardNumberInput = filtered) }
    }

    fun onCvvChanged(value: String) {
        val filtered = value.filter { it.isDigit() }.take(4)
        _uiState.update { it.copy(cvvInput = filtered) }
    }

    fun onExpiryMonthChanged(value: String) {
        val filtered = value.filter { it.isDigit() }.take(2)
        _uiState.update { it.copy(expiryMonthInput = filtered) }
    }

    fun onExpiryYearChanged(value: String) {
        val filtered = value.filter { it.isDigit() }.take(4)
        _uiState.update { it.copy(expiryYearInput = filtered) }
    }

    fun onCardholderNameChanged(value: String) = _uiState.update { it.copy(cardholderNameInput = value) }
    fun onSpendTargetChanged(value: String) = _uiState.update { it.copy(spendTargetInput = value.filter { c -> c.isDigit() || c == '.' }) }
    fun onCurrentSpendChanged(value: String) = _uiState.update { it.copy(currentSpendInput = value.filter { c -> c.isDigit() || c == '.' }) }
    fun onIsPrimaryChanged(value: Boolean) = _uiState.update { it.copy(isPrimaryInput = value) }

    fun toggleCardDetailsReveal(cardId: Long) {
        _uiState.update {
            val updated = if (it.revealedCardIds.contains(cardId)) {
                it.revealedCardIds - cardId
            } else {
                it.revealedCardIds + cardId
            }
            it.copy(revealedCardIds = updated)
        }
    }

    fun toggleCvvReveal(cardId: Long) {
        _uiState.update {
            val updated = if (it.revealedCvvCardIds.contains(cardId)) {
                it.revealedCvvCardIds - cardId
            } else {
                it.revealedCvvCardIds + cardId
            }
            it.copy(revealedCvvCardIds = updated)
        }
    }

    fun saveBankCard() {
        val state = _uiState.value
        if (state.nicknameInput.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter a card nickname (e.g. PNB Select Imperial)") }
            return
        }

        val cleanCardNumber = state.cardNumberInput.replace(" ", "").trim()
        if (cleanCardNumber.length < 12) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid card number (minimum 12-16 digits)") }
            return
        }

        // Optional spend target: blank or 0 means 0.0 (no target)
        val target = state.spendTargetInput.toDoubleOrNull() ?: 0.0
        val currentSpend = state.currentSpendInput.toDoubleOrNull() ?: 0.0

        val bankCard = BankCard(
            id = state.editingCard?.id ?: 0L,
            cardNickname = state.nicknameInput.trim(),
            bankName = state.bankNameInput.trim(),
            cardType = state.cardTypeInput,
            network = state.networkInput,
            tier = state.tierInput,
            cardNumber = cleanCardNumber,
            cvv = state.cvvInput.trim(),
            expiryMonth = state.expiryMonthInput.trim(),
            expiryYear = state.expiryYearInput.trim(),
            cardholderName = state.cardholderNameInput.trim().ifBlank { "Valued Cardholder" },
            isPrimary = state.isPrimaryInput,
            quarterlySpendTarget = if (target < 0.0) 0.0 else target,
            currentQuarterSpend = currentSpend
        )

        viewModelScope.launch {
            if (state.editingCard != null) {
                bankCardRepository.updateBankCard(bankCard)
            } else {
                bankCardRepository.insertBankCard(bankCard)
            }
            closeDialog()
        }
    }

    fun deleteBankCard(cardId: Long) {
        viewModelScope.launch {
            bankCardRepository.deleteBankCard(cardId)
        }
    }

    fun setAsPrimary(cardId: Long) {
        viewModelScope.launch {
            bankCardRepository.setPrimaryCard(cardId)
        }
    }
}
