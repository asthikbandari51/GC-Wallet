package com.giftvault.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.giftvault.app.core.notifications.NotificationHelper
import com.giftvault.app.core.theme.ThemeManager
import com.giftvault.app.ui.theme.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

import androidx.compose.runtime.Immutable

import com.giftvault.app.data.repository.CardRepository
import com.giftvault.app.domain.model.CardCategory
import com.giftvault.app.domain.model.CardStatus
import com.giftvault.app.domain.model.GiftCard

@Immutable
data class SettingsUiState(
    val userName: String? = null,
    val themeMode: ThemeMode = ThemeMode.LIGHT,
    val isPrivacyModeEnabled: Boolean = false,
    val isAppLockEnabled: Boolean = false,
    val hideBankCardCvv: Boolean = true,
    val requireAuthForCvv: Boolean = false,
    val expiryAlertDays: Int = 30,
    val excludedCategories: Set<String> = setOf(CardCategory.OTT_STREAMING.name),
    val archivedCards: List<GiftCard> = emptyList(),
    val appVersion: String = "9.2.0",
    val changesSinceLastBackup: Int = 0,
    val isHighContrastOutdoor: Boolean = false,
    val requireBiometricForSensitive: Boolean = false,
    val quietHoursEnabled: Boolean = false,
    val quietHoursStart: Int = 22,
    val quietHoursEnd: Int = 7,
    val hasDecoyPin: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val themeManager: ThemeManager,
    private val notificationHelper: NotificationHelper,
    private val cardRepository: CardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                themeManager.userName,
                themeManager.themeMode,
                themeManager.isPrivacyModeEnabled,
                themeManager.isAppLockEnabled,
                themeManager.hideBankCardCvv,
                themeManager.requireAuthForCvv,
                themeManager.expiryAlertDays,
                themeManager.excludedCategoriesFromBalance,
                cardRepository.getAllCards(),
                themeManager.changesSinceLastBackup,
                themeManager.isHighContrastOutdoor,
                themeManager.requireBiometricForSensitive,
                themeManager.quietHoursEnabled
            ) { values ->
                val name = values[0] as String?
                val mode = values[1] as ThemeMode
                val privacy = values[2] as Boolean
                val appLock = values[3] as Boolean
                val hideCvv = values[4] as Boolean
                val reqAuth = values[5] as Boolean
                val expiryDays = values[6] as Int
                @Suppress("UNCHECKED_CAST")
                val excluded = values[7] as Set<String>
                @Suppress("UNCHECKED_CAST")
                val allCards = values[8] as List<GiftCard>
                val archived = allCards.filter { it.status == CardStatus.ARCHIVED }
                val changes = values[9] as Int
                val highContrast = values[10] as Boolean
                val reqBiometric = values[11] as Boolean
                val quietHours = values[12] as Boolean

                SettingsUiState(
                    userName = name,
                    themeMode = mode,
                    isPrivacyModeEnabled = privacy,
                    isAppLockEnabled = appLock,
                    hideBankCardCvv = hideCvv,
                    requireAuthForCvv = reqAuth,
                    expiryAlertDays = expiryDays,
                    excludedCategories = excluded,
                    archivedCards = archived,
                    appVersion = "9.2.0",
                    changesSinceLastBackup = changes,
                    isHighContrastOutdoor = highContrast,
                    requireBiometricForSensitive = reqBiometric,
                    quietHoursEnabled = quietHours,
                    quietHoursStart = themeManager.quietHoursStart.value,
                    quietHoursEnd = themeManager.quietHoursEnd.value,
                    hasDecoyPin = !themeManager.decoyPin.value.isNullOrBlank()
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun setHighContrastOutdoor(enabled: Boolean) {
        themeManager.setHighContrastOutdoor(enabled)
    }

    fun setRequireBiometricForSensitive(enabled: Boolean) {
        themeManager.setRequireBiometricForSensitive(enabled)
    }

    fun setQuietHours(enabled: Boolean, startHour: Int = 22, endHour: Int = 7) {
        themeManager.setQuietHours(enabled, startHour, endHour)
    }

    fun setDecoyPin(pin: String?) {
        themeManager.setDecoyPin(pin)
    }

    fun setUserName(name: String) {
        themeManager.setUserName(name)
    }

    fun setThemeMode(mode: ThemeMode) {
        themeManager.setThemeMode(mode)
    }

    fun togglePrivacyMode(enabled: Boolean) {
        themeManager.setPrivacyMode(enabled)
    }

    fun toggleAppLock(enabled: Boolean) {
        themeManager.setAppLock(enabled)
    }

    fun toggleHideBankCardCvv(enabled: Boolean) {
        themeManager.setHideBankCardCvv(enabled)
    }

    fun toggleRequireAuthForCvv(enabled: Boolean) {
        themeManager.setRequireAuthForCvv(enabled)
    }

    fun setExpiryAlertDays(days: Int) {
        themeManager.setExpiryAlertDays(days)
    }

    fun toggleExcludedCategory(categoryName: String) {
        val current = _uiState.value.excludedCategories.toMutableSet()
        val matching = current.filter { it.equals(categoryName, ignoreCase = true) }
        if (matching.isNotEmpty()) {
            matching.forEach { current.remove(it) }
        } else {
            current.add(categoryName)
        }
        themeManager.setExcludedCategories(current)
    }

    fun restoreArchivedCard(cardId: String) {
        viewModelScope.launch {
            cardRepository.unarchiveCard(cardId)
        }
    }

    fun deleteCardPermanently(cardId: String) {
        viewModelScope.launch {
            cardRepository.deleteCard(cardId)
        }
    }

    fun emptyRecycleBin() {
        viewModelScope.launch {
            _uiState.value.archivedCards.forEach { card ->
                cardRepository.deleteCard(card.id)
            }
        }
    }

    fun sendTestNotification() {
        notificationHelper.showExpiryNotification(
            cardId = "test-card-id",
            merchantName = "GiftVault",
            expiryDaysText = "3 days"
        )
    }
}
