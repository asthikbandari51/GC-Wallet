package com.giftvault.app.ui.home

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.giftvault.app.core.ingestion.DetectedVoucherBannerState
import com.giftvault.app.core.ingestion.SmartIngestionManager
import com.giftvault.app.core.theme.ThemeManager
import com.giftvault.app.data.repository.BankCardRepository
import com.giftvault.app.data.repository.CardRepository
import com.giftvault.app.domain.model.BankCard
import com.giftvault.app.domain.model.CardCategory
import com.giftvault.app.domain.model.CardStatus
import com.giftvault.app.domain.model.GiftCard
import com.giftvault.app.domain.usecase.GetCardsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

@Immutable
data class CategoryBalance(
    val category: CardCategory,
    val totalBalance: Double,
    val percentage: Float
)

@Immutable
data class HomeUiState(
    val userName: String? = null,
    val bankCards: List<BankCard> = emptyList(),
    val activeBankCardId: Long? = null,
    val activeBankCard: BankCard? = null,
    val totalBalance: Double = 0.0,
    val totalCards: Int = 0,
    val expiringCards: List<GiftCard> = emptyList(),
    val favoriteCards: List<GiftCard> = emptyList(),
    val topCategory: CardCategory? = null,
    val favoriteBrand: String? = null,
    val categoryBalances: List<CategoryBalance> = emptyList(),
    val detectedVoucher: DetectedVoucherBannerState? = null,
    val hasSpendTarget: Boolean = false,
    val quarterlySpendTarget: Double = 0.0,
    val currentQuarterSpend: Double = 0.0,
    val isPrivacyMode: Boolean = false,
    val isLoading: Boolean = true
)

private data class UserAndBankBundle(
    val userName: String?,
    val bankCards: List<BankCard>,
    val activeCardId: Long?,
    val detectedVoucher: DetectedVoucherBannerState?,
    val isPrivacyMode: Boolean,
    val excludedCategories: Set<String>,
    val expiryAlertDays: Int
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getCardsUseCase: GetCardsUseCase,
    private val cardRepository: CardRepository,
    private val bankCardRepository: BankCardRepository,
    private val smartIngestionManager: SmartIngestionManager,
    private val themeManager: ThemeManager
) : ViewModel() {

    private val _activeBankCardId = MutableStateFlow<Long?>(null)

    init {
        smartIngestionManager.checkClipboard()
    }

    fun onResume() {
        smartIngestionManager.checkClipboard()
    }

    private val userAndBankBundle: Flow<UserAndBankBundle> = combine(
        themeManager.userName,
        bankCardRepository.getAllBankCards(),
        _activeBankCardId,
        smartIngestionManager.detectedVoucher,
        themeManager.isPrivacyModeEnabled
    ) { userName, bankCards, activeCardId, detectedVoucher, isPrivacy ->
        Triple(Triple(userName, bankCards, activeCardId), detectedVoucher, isPrivacy)
    }.combine(
        combine(
            themeManager.excludedCategoriesFromBalance,
            themeManager.expiryAlertDays
        ) { excluded, expiryDays -> Pair(excluded, expiryDays) }
    ) { (userBankTriple, voucher, isPrivacy), (excludedCats, expiryDays) ->
        val (userName, bankCards, activeCardId) = userBankTriple
        UserAndBankBundle(userName, bankCards, activeCardId, voucher, isPrivacy, excludedCats, expiryDays)
    }

    val uiState: StateFlow<HomeUiState> = combine(
        getCardsUseCase.invoke(),
        userAndBankBundle
    ) { rawCards, userData ->
        val activeCardId = userData.activeCardId
        val activeCard = userData.bankCards.find { it.id == activeCardId }

        val scopedCards = if (activeCardId != null) {
            rawCards.filter { it.linkedCardId == activeCardId }
        } else {
            rawCards
        }

        // Active, non-archived cards
        val activeCards = scopedCards.filter { it.status != com.giftvault.app.domain.model.CardStatus.ARCHIVED }

        // Exclude user-configured categories from total spendable vault balance
        val spendableCards = activeCards.filter { card ->
            !userData.excludedCategories.any { excl ->
                card.category.displayName.equals(excl, ignoreCase = true) ||
                card.category.name.equals(excl, ignoreCase = true)
            }
        }
        val totalBalance = spendableCards.sumOf { it.balance }

        val expiryCutoff = java.time.LocalDate.now().plusDays(userData.expiryAlertDays.toLong())
        val expiringCards = activeCards.filter {
            it.expiryDate != null && !it.isExpired && (it.expiryDate <= expiryCutoff)
        }.sortedBy { it.expiryDate }

        val favoriteCards = activeCards.filter { it.isFavorite }

        val spendableCategoryGroup = spendableCards.groupBy { it.category }
        val catBalances = spendableCategoryGroup.map { (cat, cards) ->
            val catTotal = cards.sumOf { it.balance }
            val pct = if (totalBalance > 0.0) ((catTotal / totalBalance).toFloat()).coerceIn(0f, 1f) else 0f
            CategoryBalance(cat, catTotal, pct)
        }.sortedByDescending { it.totalBalance }

        val topCat = catBalances.firstOrNull()?.category 
            ?: activeCards.groupBy { it.category }.maxByOrNull { it.value.size }?.key

        val positiveCards = activeCards.filter { it.balance > 0 }
        val favoriteBrandName = favoriteCards.firstOrNull()?.merchantName
            ?: (if (positiveCards.isNotEmpty()) positiveCards else activeCards)
                .groupBy { it.merchantName }
                .maxByOrNull { it.value.size }
                ?.key

        // Spend target calculation (Item 1: optional, only show if target > 0)
        val hasTarget: Boolean
        val qTarget: Double
        val qSpend: Double

        if (activeCard != null) {
            hasTarget = activeCard.hasSpendTarget
            qTarget = activeCard.quarterlySpendTarget
            qSpend = activeCard.currentQuarterSpend
        } else {
            val targetedCards = userData.bankCards.filter { it.hasSpendTarget }
            hasTarget = targetedCards.isNotEmpty()
            qTarget = targetedCards.sumOf { it.quarterlySpendTarget }
            qSpend = targetedCards.sumOf { it.currentQuarterSpend }
        }

        HomeUiState(
            userName = userData.userName,
            bankCards = userData.bankCards,
            activeBankCardId = activeCardId,
            activeBankCard = activeCard,
            totalBalance = totalBalance,
            totalCards = activeCards.size,
            expiringCards = expiringCards,
            favoriteCards = favoriteCards,
            topCategory = topCat,
            favoriteBrand = favoriteBrandName,
            categoryBalances = catBalances,
            detectedVoucher = userData.detectedVoucher,
            hasSpendTarget = hasTarget,
            quarterlySpendTarget = qTarget,
            currentQuarterSpend = qSpend,
            isPrivacyMode = userData.isPrivacyMode,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    fun selectBankCard(cardId: Long?) {
        _activeBankCardId.value = cardId
    }

    fun togglePrivacyMode() {
        themeManager.setPrivacyMode(!uiState.value.isPrivacyMode)
    }

    fun dismissDetectedVoucher() {
        smartIngestionManager.dismissBanner()
    }

    fun ingestDetectedVoucher() {
        val detected = uiState.value.detectedVoucher ?: return
        val result = detected.parseResult
        val activeCardId = uiState.value.activeBankCardId

        viewModelScope.launch {
            val giftCard = GiftCard(
                id = UUID.randomUUID().toString(),
                merchantName = result.merchantName ?: "Gift Voucher",
                templateKey = result.templateKey ?: "other",
                cardNumber = result.cardCode ?: detected.code,
                pin = result.pin,
                balance = result.amount ?: 0.0,
                initialValue = result.amount ?: 0.0,
                currency = "INR",
                expiryDate = result.expiryDate,
                category = com.giftvault.app.core.templates.TemplateRegistry.getCategoryForMerchant(result.merchantName ?: "Others"),
                notes = result.details ?: "Auto-ingested from clipboard",
                status = CardStatus.ACTIVE,
                isFavorite = false,
                linkedCardId = activeCardId,
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )
            cardRepository.addCard(giftCard)
            smartIngestionManager.dismissBanner()
        }
    }
}
