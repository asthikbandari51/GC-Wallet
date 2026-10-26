package com.giftvault.app.ui.addcard

import android.net.Uri
import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.giftvault.app.core.receipts.ReceiptStorageManager
import com.giftvault.app.core.templates.TemplateManifest
import com.giftvault.app.data.repository.BankCardRepository
import com.giftvault.app.data.repository.CardRepository
import com.giftvault.app.data.repository.TemplateRepository
import com.giftvault.app.domain.model.BankCard
import com.giftvault.app.domain.model.CardCategory
import com.giftvault.app.domain.model.CardStatus
import com.giftvault.app.domain.model.GiftCard
import com.giftvault.app.domain.model.GiftCardParseResult
import com.giftvault.app.domain.model.ParseSourceType
import com.giftvault.app.domain.usecase.AddCardUseCase
import com.giftvault.app.domain.usecase.ParseGiftCardUseCase
import com.giftvault.app.domain.usecase.UpdateCardUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

@Immutable
data class AddCardUiState(
    val isEditMode: Boolean = false,
    val editingCardId: String? = null,
    val currentStep: Int = 0,
    val brandSearchQuery: String = "",
    val filteredTemplates: List<TemplateManifest> = emptyList(),
    val merchantName: String = "",
    val cardNumber: String = "",
    val pin: String = "",
    val balance: String = "",
    val initialValue: String = "",
    val currency: String = "INR",
    val expiryDate: LocalDate? = null,
    val category: CardCategory = CardCategory.OTHERS,
    val notes: String = "",
    val selectedTemplateKey: String = "other",
    val matchedTemplate: TemplateManifest? = null,
    val availableTemplates: List<TemplateManifest> = emptyList(),
    val bankCards: List<BankCard> = emptyList(),
    val selectedBankCardId: Long? = null,
    val pendingReceiptUris: List<Uri> = emptyList(),
    val showAddReceiptSheet: Boolean = false,
    val parseResult: GiftCardParseResult? = null,
    val showParseReviewSheet: Boolean = false,
    val showPasteTextDialog: Boolean = false,
    val isParsing: Boolean = false,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
    val originalCreatedAt: Instant = Instant.now(),
    val originalFavorite: Boolean = false
)

@HiltViewModel
class AddCardViewModel @Inject constructor(
    private val addCardUseCase: AddCardUseCase,
    private val updateCardUseCase: UpdateCardUseCase,
    private val cardRepository: CardRepository,
    private val bankCardRepository: BankCardRepository,
    private val templateRepository: TemplateRepository,
    private val receiptStorageManager: ReceiptStorageManager,
    private val parseGiftCardUseCase: ParseGiftCardUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddCardUiState())
    val uiState: StateFlow<AddCardUiState> = _uiState.asStateFlow()

    private val editingCardIdFromNav: String? = savedStateHandle.get<String>("cardId")

    init {
        loadTemplatesAndCard()
    }

    private fun loadTemplatesAndCard() {
        viewModelScope.launch {
            val templates = templateRepository.getAllTemplates()
            _uiState.update { 
                it.copy(
                    availableTemplates = templates,
                    filteredTemplates = templates
                ) 
            }

            // Load bank cards for profile linking
            bankCardRepository.getAllBankCards().collect { bankCards ->
                val primaryCard = bankCards.firstOrNull { it.isPrimary }
                _uiState.update { state ->
                    state.copy(
                        bankCards = bankCards,
                        selectedBankCardId = state.selectedBankCardId ?: primaryCard?.id
                    )
                }
            }
        }

        viewModelScope.launch {
            if (!editingCardIdFromNav.isNullOrBlank()) {
                val existingCard = cardRepository.getCardById(editingCardIdFromNav).firstOrNull()
                if (existingCard != null) {
                    val matched = templateRepository.getTemplate(existingCard.templateKey)
                    _uiState.update { state ->
                        state.copy(
                            isEditMode = true,
                            editingCardId = existingCard.id,
                            merchantName = existingCard.merchantName,
                            cardNumber = existingCard.cardNumber,
                            pin = existingCard.pin ?: "",
                            balance = existingCard.balance.toString(),
                            initialValue = existingCard.initialValue.toString(),
                            currency = existingCard.currency,
                            expiryDate = existingCard.expiryDate,
                            category = existingCard.category,
                            notes = existingCard.notes ?: "",
                            selectedTemplateKey = existingCard.templateKey,
                            matchedTemplate = matched,
                            selectedBankCardId = existingCard.linkedCardId,
                            originalCreatedAt = existingCard.createdAt,
                            originalFavorite = existingCard.isFavorite,
                            currentStep = 1
                        )
                    }
                }
            }
        }
    }

    fun onBrandSearchChanged(query: String) {
        _uiState.update { state ->
            val filtered = if (query.isBlank()) {
                state.availableTemplates
            } else {
                state.availableTemplates.filter {
                    it.displayName.contains(query, ignoreCase = true) ||
                    it.category.contains(query, ignoreCase = true) ||
                    it.templateKey.contains(query, ignoreCase = true)
                }
            }
            state.copy(
                brandSearchQuery = query,
                filteredTemplates = filtered
            )
        }
    }

    fun nextStep() {
        if (canProceedFromStep(_uiState.value.currentStep)) {
            _uiState.update { it.copy(currentStep = (it.currentStep + 1).coerceAtMost(3)) }
        }
    }

    fun previousStep() {
        _uiState.update { it.copy(currentStep = (it.currentStep - 1).coerceAtLeast(0)) }
    }

    fun goToStep(step: Int) {
        if (step in 0..3) {
            _uiState.update { it.copy(currentStep = step) }
        }
    }

    fun canProceedFromStep(step: Int): Boolean {
        val state = _uiState.value
        return when (step) {
            0 -> state.selectedTemplateKey.isNotBlank()
            1 -> state.cardNumber.isNotBlank() && state.balance.toDoubleOrNull() != null
            2 -> true
            3 -> true
            else -> true
        }
    }

    fun onMerchantNameChanged(name: String) {
        viewModelScope.launch {
            val matched = templateRepository.getTemplateForMerchant(name)
            val autoCategory = com.giftvault.app.core.templates.TemplateRegistry.getCategoryForMerchant(name)
            val templateKey = matched?.templateKey ?: com.giftvault.app.core.templates.TemplateRegistry.getFallbackTemplate(autoCategory).templateKey
            _uiState.update { 
                it.copy(
                    merchantName = name,
                    matchedTemplate = matched ?: com.giftvault.app.core.templates.TemplateRegistry.getTemplate(templateKey),
                    selectedTemplateKey = templateKey,
                    category = autoCategory
                ) 
            }
        }
    }

    fun onCardNumberChanged(number: String) {
        _uiState.update { it.copy(cardNumber = number) }
    }

    fun onPinChanged(pin: String) {
        _uiState.update { it.copy(pin = pin) }
    }

    fun onBalanceChanged(balance: String) {
        _uiState.update { it.copy(balance = balance) }
    }

    fun onInitialValueChanged(value: String) {
        _uiState.update { it.copy(initialValue = value) }
    }

    fun onNotesChanged(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    fun onCategoryChanged(category: CardCategory) {
        _uiState.update { it.copy(category = category) }
    }

    fun onBankCardSelected(id: Long?) {
        _uiState.update { it.copy(selectedBankCardId = id) }
    }

    fun onTemplateSelected(template: TemplateManifest) {
        _uiState.update { 
            it.copy(
                selectedTemplateKey = template.templateKey,
                matchedTemplate = template,
                merchantName = if (it.merchantName.isBlank() && template.templateKey != "other") template.displayName else it.merchantName,
                category = CardCategory.fromDisplayName(template.category)
            ) 
        }
    }

    fun onExpiryDateChanged(date: LocalDate?) {
        _uiState.update { it.copy(expiryDate = date) }
    }
    
    fun onCurrencyChanged(currency: String) {
        _uiState.update { it.copy(currency = currency) }
    }

    fun addPendingReceiptUri(uri: Uri) {
        _uiState.update { it.copy(pendingReceiptUris = it.pendingReceiptUris + uri) }
    }

    fun removePendingReceiptUri(uri: Uri) {
        _uiState.update { it.copy(pendingReceiptUris = it.pendingReceiptUris - uri) }
    }

    fun showAddReceiptSheet() {
        _uiState.update { it.copy(showAddReceiptSheet = true) }
    }

    fun hideAddReceiptSheet() {
        _uiState.update { it.copy(showAddReceiptSheet = false) }
    }

    fun saveCard() {
        val currentState = _uiState.value
        
        val merchant = if (currentState.merchantName.isBlank()) {
            currentState.matchedTemplate?.displayName ?: "Generic Card"
        } else {
            currentState.merchantName
        }

        if (currentState.cardNumber.isBlank()) {
            _uiState.update { it.copy(error = "Card Number is required") }
            return
        }

        val balanceDouble = currentState.balance.toDoubleOrNull()
        if (balanceDouble == null) {
            _uiState.update { it.copy(error = "Invalid balance amount") }
            return
        }
        
        val initialDouble = currentState.initialValue.toDoubleOrNull() ?: balanceDouble
        val cardId = currentState.editingCardId ?: UUID.randomUUID().toString()

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val card = GiftCard(
                    id = cardId,
                    merchantName = merchant,
                    templateKey = currentState.selectedTemplateKey,
                    cardNumber = currentState.cardNumber,
                    pin = currentState.pin.takeIf { it.isNotBlank() },
                    balance = balanceDouble,
                    initialValue = initialDouble,
                    currency = currentState.currency,
                    expiryDate = currentState.expiryDate,
                    category = currentState.category,
                    notes = currentState.notes.takeIf { it.isNotBlank() },
                    status = CardStatus.ACTIVE,
                    isFavorite = currentState.originalFavorite,
                    linkedCardId = currentState.selectedBankCardId,
                    createdAt = currentState.originalCreatedAt,
                    updatedAt = Instant.now()
                )
                
                if (currentState.isEditMode) {
                    updateCardUseCase(card)
                } else {
                    addCardUseCase(card)
                }

                // Save attached receipt images if any
                currentState.pendingReceiptUris.forEach { uri ->
                    receiptStorageManager.saveReceiptImage(cardId, uri)
                }

                _uiState.update { it.copy(isLoading = false, isSaved = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to save card") }
            }
        }
    }
    
    fun parseFromImage(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isParsing = true, error = null) }
            try {
                val result = parseGiftCardUseCase.parseFromImage(uri)
                _uiState.update {
                    it.copy(
                        isParsing = false,
                        parseResult = result,
                        showParseReviewSheet = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isParsing = false, error = "Failed to parse image: ${e.message}")
                }
            }
        }
    }

    fun parseFromText(text: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isParsing = true, error = null) }
            try {
                val result = parseGiftCardUseCase.parseFromText(text, ParseSourceType.PASTED_TEXT)
                _uiState.update {
                    it.copy(
                        isParsing = false,
                        parseResult = result,
                        showParseReviewSheet = true,
                        showPasteTextDialog = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isParsing = false, error = "Failed to parse text: ${e.message}")
                }
            }
        }
    }

    fun applyParseResult(
        merchant: String,
        number: String,
        pin: String,
        balance: String,
        expiry: LocalDate?,
        templateKey: String,
        category: CardCategory,
        receiptUri: Uri?
    ) {
        _uiState.update { state ->
            val matched = state.availableTemplates.find { it.templateKey == templateKey }
            val newReceipts = if (receiptUri != null) {
                state.pendingReceiptUris + receiptUri
            } else {
                state.pendingReceiptUris
            }

            state.copy(
                merchantName = merchant,
                cardNumber = number,
                pin = pin,
                balance = balance.ifBlank { state.balance },
                initialValue = balance.ifBlank { state.initialValue },
                expiryDate = expiry,
                selectedTemplateKey = templateKey,
                matchedTemplate = matched,
                category = category,
                pendingReceiptUris = newReceipts,
                showParseReviewSheet = false
            )
        }
    }

    fun showPasteTextDialog() {
        _uiState.update { it.copy(showPasteTextDialog = true) }
    }

    fun hidePasteTextDialog() {
        _uiState.update { it.copy(showPasteTextDialog = false) }
    }

    fun hideParseReviewSheet() {
        _uiState.update { it.copy(showParseReviewSheet = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
