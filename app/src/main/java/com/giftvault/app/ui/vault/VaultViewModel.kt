package com.giftvault.app.ui.vault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.giftvault.app.data.repository.CardRepository
import com.giftvault.app.domain.model.CardCategory
import com.giftvault.app.domain.model.GiftCard
import com.giftvault.app.domain.usecase.GetCardsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

import androidx.compose.runtime.Immutable
import com.giftvault.app.core.theme.ThemeManager
import com.giftvault.app.domain.model.CardStatus

@Immutable
enum class SortOption(val displayName: String) {
    RECENTLY_ADDED("Recently Added"),
    BALANCE_HIGH("Value: High to Low"),
    BALANCE_LOW("Value: Low to High"),
    EXPIRY_SOON("Expiring Soonest"),
    EXPIRY_LATEST("Expiring Latest"),
    NAME("Brand Name (A → Z)")
}

@Immutable
enum class ViewMode { GRID, LIST }

@Immutable
data class VaultUiState(
    val cards: List<GiftCard> = emptyList(),
    val filteredCards: List<GiftCard> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: CardCategory? = null,
    val sortOption: SortOption = SortOption.RECENTLY_ADDED,
    val viewMode: ViewMode = ViewMode.LIST,
    val showFavoritesOnly: Boolean = false,
    val showArchivedOnly: Boolean = false,
    val isPrivacyMode: Boolean = false,
    val excludedCategories: Set<String> = emptySet(),
    val spendableBalance: Double = 0.0,
    val isLoading: Boolean = true
)

@HiltViewModel
class VaultViewModel @Inject constructor(
    private val getCardsUseCase: GetCardsUseCase,
    private val cardRepository: CardRepository,
    private val themeManager: ThemeManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(VaultUiState())
    val uiState: StateFlow<VaultUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                getCardsUseCase(),
                themeManager.isPrivacyModeEnabled,
                themeManager.excludedCategoriesFromBalance
            ) { cards, isPrivacy, excludedCats ->
                Triple(cards, isPrivacy, excludedCats)
            }.collect { (cards, isPrivacy, excludedCats) ->
                val activeCards = cards.filter { it.status != CardStatus.ARCHIVED }
                val spendableCards = activeCards.filter { card ->
                    !excludedCats.any { excl ->
                        card.category.displayName.equals(excl, ignoreCase = true) ||
                        card.category.name.equals(excl, ignoreCase = true)
                    }
                }
                val spendableTotal = spendableCards.sumOf { it.balance }

                _uiState.update { 
                    it.copy(
                        cards = cards, 
                        isPrivacyMode = isPrivacy, 
                        excludedCategories = excludedCats,
                        spendableBalance = spendableTotal,
                        isLoading = false
                    ) 
                }
                filterAndSortCards()
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        filterAndSortCards()
    }

    fun onCategorySelected(category: CardCategory?) {
        _uiState.update { it.copy(selectedCategory = category) }
        filterAndSortCards()
    }

    fun onSortOptionChanged(sort: SortOption) {
        _uiState.update { it.copy(sortOption = sort) }
        filterAndSortCards()
    }

    fun onViewModeToggled() {
        _uiState.update { 
            it.copy(viewMode = if (it.viewMode == ViewMode.GRID) ViewMode.LIST else ViewMode.GRID) 
        }
    }

    fun onFavoritesToggled() {
        _uiState.update { it.copy(showFavoritesOnly = !it.showFavoritesOnly) }
        filterAndSortCards()
    }

    fun onArchivedToggled() {
        _uiState.update { it.copy(showArchivedOnly = !it.showArchivedOnly) }
        filterAndSortCards()
    }

    fun toggleFavorite(cardId: String) {
        viewModelScope.launch {
            val card = _uiState.value.cards.find { it.id == cardId }
            if (card != null) {
                cardRepository.updateCard(card.copy(isFavorite = !card.isFavorite))
            }
        }
    }

    fun archiveCard(cardId: String) {
        viewModelScope.launch {
            cardRepository.archiveCard(cardId)
        }
    }

    fun unarchiveCard(cardId: String) {
        viewModelScope.launch {
            cardRepository.unarchiveCard(cardId)
        }
    }

    fun deleteCard(cardId: String) {
        viewModelScope.launch {
            cardRepository.deleteCard(cardId)
        }
    }

    private fun filterAndSortCards() {
        val state = _uiState.value
        var result = state.cards

        // Filter archived status
        result = if (state.showArchivedOnly) {
            result.filter { it.status == CardStatus.ARCHIVED }
        } else {
            result.filter { it.status != CardStatus.ARCHIVED }
        }

        if (state.searchQuery.isNotBlank()) {
            val query = state.searchQuery.lowercase().trim()
            result = result.filter { 
                it.merchantName.lowercase().contains(query) || 
                it.cardNumber.lowercase().contains(query) ||
                it.category.displayName.lowercase().contains(query) ||
                it.notes?.lowercase()?.contains(query) == true
            }
        }

        if (state.selectedCategory != null) {
            result = result.filter { it.category == state.selectedCategory }
        }

        if (state.showFavoritesOnly) {
            result = result.filter { it.isFavorite }
        }

        result = when (state.sortOption) {
            SortOption.NAME -> result.sortedBy { it.merchantName.lowercase() }
            SortOption.BALANCE_HIGH -> result.sortedByDescending { it.balance }
            SortOption.BALANCE_LOW -> result.sortedBy { it.balance }
            SortOption.EXPIRY_SOON -> result.sortedBy { it.expiryDate ?: java.time.LocalDate.MAX }
            SortOption.EXPIRY_LATEST -> result.sortedByDescending { it.expiryDate ?: java.time.LocalDate.MIN }
            SortOption.RECENTLY_ADDED -> result.sortedByDescending { it.createdAt }
        }

        _uiState.update { it.copy(filteredCards = result) }
    }
}
