package com.giftvault.app.ui.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.giftvault.app.data.repository.CardRepository
import com.giftvault.app.data.repository.TransactionRepository
import com.giftvault.app.domain.model.Transaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class ActivityItem(
    val transaction: Transaction,
    val merchantName: String,
    val templateKey: String
)

data class ActivityUiState(
    val activities: List<ActivityItem> = emptyList(),
    val filterType: String? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class ActivityViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val cardRepository: CardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActivityUiState())
    val uiState: StateFlow<ActivityUiState> = _uiState.asStateFlow()
    
    private var allActivities: List<ActivityItem> = emptyList()

    init {
        loadActivities()
    }

    private fun loadActivities() {
        combine(
            transactionRepository.getAllTransactions(),
            cardRepository.getAllCards()
        ) { transactions, cards ->
            val items = transactions.mapNotNull { transaction ->
                val card = cards.find { it.id == transaction.cardId }
                if (card != null) {
                    ActivityItem(
                        transaction = transaction,
                        merchantName = card.merchantName,
                        templateKey = card.templateKey
                    )
                } else {
                    null
                }
            }.sortedByDescending { it.transaction.createdAt }
            
            allActivities = items
            applyFilter()
        }.launchIn(viewModelScope)
    }

    fun setFilterType(type: String?) {
        _uiState.update { it.copy(filterType = type) }
        applyFilter()
    }
    
    private fun applyFilter() {
        val currentFilter = _uiState.value.filterType
        
        val filtered = if (currentFilter == null || currentFilter == "All") {
            allActivities
        } else {
            allActivities.filter { item ->
                val typeStr = item.transaction.type.toString()
                when (currentFilter) {
                    "Redeemed" -> typeStr.contains("REDEEM", ignoreCase = true)
                    "Added" -> typeStr.contains("ADD", ignoreCase = true)
                    "Adjustments" -> typeStr.contains("ADJUST", ignoreCase = true)
                    else -> true
                }
            }
        }
        
        _uiState.update { 
            it.copy(
                activities = filtered,
                isLoading = false
            )
        }
    }
}
