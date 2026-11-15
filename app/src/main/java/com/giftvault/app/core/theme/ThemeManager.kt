package com.giftvault.app.core.theme

import android.content.Context
import android.content.SharedPreferences
import com.giftvault.app.ui.theme.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemeManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("giftvault_prefs", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(
        try {
            ThemeMode.valueOf(prefs.getString("theme_mode", ThemeMode.LIQUID_GLASS.name) ?: ThemeMode.LIQUID_GLASS.name)
        } catch (_: Exception) {
            ThemeMode.LIQUID_GLASS
        }
    )
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _isPrivacyModeEnabled = MutableStateFlow(
        prefs.getBoolean("privacy_mode", false)
    )
    val isPrivacyModeEnabled: StateFlow<Boolean> = _isPrivacyModeEnabled.asStateFlow()

    private val _isAppLockEnabled = MutableStateFlow(
        prefs.getBoolean("app_lock", false)
    )
    val isAppLockEnabled: StateFlow<Boolean> = _isAppLockEnabled.asStateFlow()

    private val _userName = MutableStateFlow<String?>(
        prefs.getString("user_name", null)?.let { saved ->
            if (saved.equals("Asthik Bandari", ignoreCase = true) && !prefs.getBoolean("user_explicitly_asthik", false)) {
                null
            } else {
                saved.ifBlank { null }
            }
        }
    )
    val userName: StateFlow<String?> = _userName.asStateFlow()

    private val _appPin = MutableStateFlow(
        prefs.getString("app_pin", null)
    )
    val appPin: StateFlow<String?> = _appPin.asStateFlow()

    private val _isOnboardingCompleted = MutableStateFlow(
        prefs.getBoolean("onboarding_completed", false)
    )
    val isOnboardingCompleted: StateFlow<Boolean> = _isOnboardingCompleted.asStateFlow()

    private val _excludedCategoriesFromBalance = MutableStateFlow(
        prefs.getStringSet("excluded_balance_categories", null) ?: setOf("OTT_STREAMING", "OTT & Streaming")
    )
    val excludedCategoriesFromBalance: StateFlow<Set<String>> = _excludedCategoriesFromBalance.asStateFlow()

    fun isCategoryExcluded(categoryName: String, displayName: String): Boolean {
        val set = _excludedCategoriesFromBalance.value
        return set.any { excl ->
            excl.equals(categoryName, ignoreCase = true) ||
            excl.equals(displayName, ignoreCase = true)
        }
    }

    private val _expiryAlertDays = MutableStateFlow(
        prefs.getInt("expiry_alert_days", 30)
    )
    val expiryAlertDays: StateFlow<Int> = _expiryAlertDays.asStateFlow()

    private val _hideBankCardCvv = MutableStateFlow(
        prefs.getBoolean("hide_bank_card_cvv", true)
    )
    val hideBankCardCvv: StateFlow<Boolean> = _hideBankCardCvv.asStateFlow()

    private val _requireAuthForCvv = MutableStateFlow(
        prefs.getBoolean("require_auth_for_cvv", true)
    )
    val requireAuthForCvv: StateFlow<Boolean> = _requireAuthForCvv.asStateFlow()

    private val _isDecoyMode = MutableStateFlow(false)
    val isDecoyMode: StateFlow<Boolean> = _isDecoyMode.asStateFlow()

    private val _decoyPin = MutableStateFlow(prefs.getString("decoy_pin", null))
    val decoyPin: StateFlow<String?> = _decoyPin.asStateFlow()

    private val _changesSinceLastBackup = MutableStateFlow(prefs.getInt("changes_since_backup", 0))
    val changesSinceLastBackup: StateFlow<Int> = _changesSinceLastBackup.asStateFlow()

    private val _lastBackupTimestamp = MutableStateFlow(prefs.getLong("last_backup_timestamp", 0L))
    val lastBackupTimestamp: StateFlow<Long> = _lastBackupTimestamp.asStateFlow()

    private val _isHighContrastOutdoor = MutableStateFlow(prefs.getBoolean("high_contrast_outdoor", false))
    val isHighContrastOutdoor: StateFlow<Boolean> = _isHighContrastOutdoor.asStateFlow()

    private val _requireBiometricForSensitive = MutableStateFlow(prefs.getBoolean("require_biometric_sensitive", true))
    val requireBiometricForSensitive: StateFlow<Boolean> = _requireBiometricForSensitive.asStateFlow()

    private val _quietHoursEnabled = MutableStateFlow(prefs.getBoolean("quiet_hours_enabled", true))
    val quietHoursEnabled: StateFlow<Boolean> = _quietHoursEnabled.asStateFlow()

    private val _quietHoursStart = MutableStateFlow(prefs.getInt("quiet_hours_start", 22))
    val quietHoursStart: StateFlow<Int> = _quietHoursStart.asStateFlow()

    private val _quietHoursEnd = MutableStateFlow(prefs.getInt("quiet_hours_end", 8))
    val quietHoursEnd: StateFlow<Int> = _quietHoursEnd.asStateFlow()

    fun setDecoyMode(active: Boolean) {
        _isDecoyMode.value = active
    }

    fun setDecoyPin(pin: String?) {
        prefs.edit().putString("decoy_pin", pin).apply()
        _decoyPin.value = pin
    }

    fun incrementChangesSinceBackup() {
        val next = _changesSinceLastBackup.value + 1
        prefs.edit().putInt("changes_since_backup", next).apply()
        _changesSinceLastBackup.value = next
    }

    fun markBackupCompleted() {
        val now = System.currentTimeMillis()
        prefs.edit().putLong("last_backup_timestamp", now).putInt("changes_since_backup", 0).apply()
        _lastBackupTimestamp.value = now
        _changesSinceLastBackup.value = 0
    }

    fun setHighContrastOutdoor(enabled: Boolean) {
        prefs.edit().putBoolean("high_contrast_outdoor", enabled).apply()
        _isHighContrastOutdoor.value = enabled
    }

    fun setRequireBiometricForSensitive(require: Boolean) {
        prefs.edit().putBoolean("require_biometric_sensitive", require).apply()
        _requireBiometricForSensitive.value = require
    }

    fun setQuietHours(enabled: Boolean, startHour: Int = 22, endHour: Int = 8) {
        prefs.edit()
            .putBoolean("quiet_hours_enabled", enabled)
            .putInt("quiet_hours_start", startHour)
            .putInt("quiet_hours_end", endHour)
            .apply()
        _quietHoursEnabled.value = enabled
        _quietHoursStart.value = startHour
        _quietHoursEnd.value = endHour
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString("theme_mode", mode.name).apply()
        _themeMode.value = mode
    }

    fun setPrivacyMode(enabled: Boolean) {
        prefs.edit().putBoolean("privacy_mode", enabled).apply()
        _isPrivacyModeEnabled.value = enabled
    }

    fun setAppLock(enabled: Boolean) {
        prefs.edit().putBoolean("app_lock", enabled).apply()
        _isAppLockEnabled.value = enabled
    }

    fun setUserName(name: String) {
        val trimmed = name.trim()
        if (trimmed.equals("Asthik Bandari", ignoreCase = true)) {
            prefs.edit().putBoolean("user_explicitly_asthik", true).apply()
        }
        prefs.edit().putString("user_name", trimmed).apply()
        _userName.value = trimmed.ifBlank { null }
    }

    fun setAppPin(pin: String) {
        prefs.edit().putString("app_pin", pin).apply()
        _appPin.value = pin
    }

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean("onboarding_completed", completed).apply()
        _isOnboardingCompleted.value = completed
    }

    fun setExcludedCategories(categories: Set<String>) {
        prefs.edit().putStringSet("excluded_balance_categories", categories).apply()
        _excludedCategoriesFromBalance.value = categories
    }

    fun setExpiryAlertDays(days: Int) {
        prefs.edit().putInt("expiry_alert_days", days).apply()
        _expiryAlertDays.value = days
    }

    fun setHideBankCardCvv(hide: Boolean) {
        prefs.edit().putBoolean("hide_bank_card_cvv", hide).apply()
        _hideBankCardCvv.value = hide
    }

    fun setRequireAuthForCvv(require: Boolean) {
        prefs.edit().putBoolean("require_auth_for_cvv", require).apply()
        _requireAuthForCvv.value = require
    }
}
