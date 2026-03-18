package com.fanyiadrien.ictu_ex.feature.settings

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fanyiadrien.ictu_ex.data.repository.AuthRepository
import com.fanyiadrien.ictu_ex.data.repository.UserRepository
import com.fanyiadrien.ictu_ex.ui.theme.ThemeMode
import com.fanyiadrien.ictu_ex.utils.AppResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    // Appearance
    val themeMode: ThemeMode = ThemeMode.AUTO,

    // Role
    val currentUserType: String = "",       // "SELLER" or "BUYER", loaded from Firestore
    val isSwitchingRole: Boolean = false,

    // Sensors
    val shakeToReportEnabled: Boolean = true,
    val lightSensorEnabled: Boolean = true,
    val lightSensorThreshold: Float = 10f,   // lux value below which dark mode triggers

    // Security
    val biometricLockEnabled: Boolean = true,

    // Notifications
    val newListingAlertsEnabled: Boolean = true,
    val chatNotificationsEnabled: Boolean = true,

    // Feedback
    val isClearingCache: Boolean = false,
    val isDeletingAccount: Boolean = false,
    val snackbarMessage: String? = null,

    // Info
    val appVersion: String = "1.0.0",
    val cacheSize: String = "Calculating…"
)

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    var uiState by mutableStateOf(SettingsUiState())
        private set

    init { loadCurrentUserType() }

    private fun loadCurrentUserType() {
        viewModelScope.launch {
            when (val result = userRepository.getCurrentUser()) {
                is AppResult.Success -> uiState = uiState.copy(
                    currentUserType = result.data.userType
                )
                else -> Unit
            }
        }
    }

    fun switchUserType() {
        val newType = if (uiState.currentUserType == "SELLER") "BUYER" else "SELLER"
        viewModelScope.launch {
            uiState = uiState.copy(isSwitchingRole = true)
            when (val result = userRepository.switchUserType(newType)) {
                is AppResult.Success -> uiState = uiState.copy(
                    isSwitchingRole = false,
                    currentUserType = newType,
                    snackbarMessage = "You are now a $newType. Changes take effect on next visit to Home."
                )
                is AppResult.Error -> uiState = uiState.copy(
                    isSwitchingRole = false,
                    snackbarMessage = result.message
                )
                else -> uiState = uiState.copy(isSwitchingRole = false)
            }
        }
    }

    // ── Appearance ────────────────────────────────────────────────────────────

    /** Called when the user picks a theme in Settings; also propagates up to MainActivity. */
    fun setThemeMode(mode: ThemeMode) {
        uiState = uiState.copy(themeMode = mode)
    }

    // ── Sensors ───────────────────────────────────────────────────────────────

    fun toggleShakeToReport(enabled: Boolean) {
        uiState = uiState.copy(shakeToReportEnabled = enabled)
    }

    fun toggleLightSensor(enabled: Boolean) {
        uiState = uiState.copy(lightSensorEnabled = enabled)
    }

    /** Adjust the lux threshold at which the light sensor triggers dark mode (5–50 lux). */
    fun setLightSensorThreshold(lux: Float) {
        uiState = uiState.copy(lightSensorThreshold = lux)
    }

    // ── Security ──────────────────────────────────────────────────────────────

    fun toggleBiometricLock(enabled: Boolean) {
        uiState = uiState.copy(biometricLockEnabled = enabled)
    }

    // ── Notifications ─────────────────────────────────────────────────────────

    fun toggleNewListingAlerts(enabled: Boolean) {
        uiState = uiState.copy(newListingAlertsEnabled = enabled)
    }

    fun toggleChatNotifications(enabled: Boolean) {
        uiState = uiState.copy(chatNotificationsEnabled = enabled)
    }

    // ── Cache ─────────────────────────────────────────────────────────────────

    fun clearCache(context: Context) {
        viewModelScope.launch {
            uiState = uiState.copy(isClearingCache = true)
            try {
                context.cacheDir.deleteRecursively()
                uiState = uiState.copy(
                    isClearingCache = false,
                    cacheSize = "0 KB",
                    snackbarMessage = "Cache cleared successfully."
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isClearingCache = false,
                    snackbarMessage = "Failed to clear cache."
                )
            }
        }
    }

    fun computeCacheSize(context: Context) {
        viewModelScope.launch {
            val bytes = context.cacheDir.walkTopDown().sumOf { it.length() }
            val label = when {
                bytes < 1024        -> "$bytes B"
                bytes < 1024 * 1024 -> "${bytes / 1024} KB"
                else                -> "${bytes / (1024 * 1024)} MB"
            }
            uiState = uiState.copy(cacheSize = label)
        }
    }

    // ── Account ───────────────────────────────────────────────────────────────

    fun deleteAccount(onComplete: () -> Unit) {
        viewModelScope.launch {
            uiState = uiState.copy(isDeletingAccount = true)
            // Delete Firestore user document first, then Firebase Auth account
            val uid = authRepository.currentUserId()
            if (uid != null) {
                userRepository.deleteUser(uid)
            }
            authRepository.signOut()
            uiState = uiState.copy(isDeletingAccount = false)
            onComplete()
        }
    }

    fun dismissSnackbar() {
        uiState = uiState.copy(snackbarMessage = null)
    }
}
