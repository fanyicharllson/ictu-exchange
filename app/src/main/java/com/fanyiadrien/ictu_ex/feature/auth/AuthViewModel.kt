package com.fanyiadrien.ictu_ex.feature.auth

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fanyiadrien.ictu_ex.data.repository.AuthRepository
import com.fanyiadrien.ictu_ex.utils.AppResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Shared ViewModel for all auth screens:
 * SignUpScreen, SignInScreen, CheckStatusScreen.
 *
 * Your teammate injects this into any auth screen like:
 *   val viewModel: AuthViewModel = hiltViewModel()
 *
 * Then reads state and calls functions:
 *   viewModel.signUp(...)
 *   viewModel.uiState.isLoading
 *   viewModel.uiState.errorMessage
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    companion object {
        private const val TAG = "ICTU_AuthVM"
    }

    // ─── UI State ─────────────────────────────────────────────────────────────
    // One state object — screen reads this and reacts automatically

    var uiState by mutableStateOf(AuthUiState())
        private set

    // ─── Actions ──────────────────────────────────────────────────────────────

    /**
     * Call from SignUpScreen when user taps "Create Account".
     *
     * Example:
     *   viewModel.signUp(
     *       email       = emailInput,
     *       password    = passwordInput,
     *       displayName = nameInput,
     *       studentId   = studentIdInput,
     *       userType    = userType,       // passed from CheckStatusScreen
     *       onSuccess   = {
     *           navController.navigate(Screen.Home.route) {
     *               popUpTo(Screen.Onboarding.route) { inclusive = true }
     *           }
     *       }
     *   )
     */
    fun signUp(
        email: String,
        password: String,
        displayName: String,
        studentId: String,
        userType: String,
        onSuccess: () -> Unit
    ) {
        Log.d(TAG, "signUp() called for userType=$userType, email=$email")
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)

            when (val result = authRepository.signUp(email, password, displayName, studentId, userType)) {
                is AppResult.Success -> {
                    Log.d(TAG, "signUp() success")
                    uiState = uiState.copy(isLoading = false)
                    onSuccess()
                }
                is AppResult.Error -> {
                    Log.e(TAG, "signUp() error: ${result.message}", result.cause)
                    uiState = uiState.copy(
                        isLoading    = false,
                        errorMessage = result.message   // ready to display in UI
                    )
                }
                else -> Unit
            }
        }
    }

    /**
     * Call from SignInScreen when user taps "Sign In".
     *
     * Example:
     *   viewModel.signIn(
     *       email    = emailInput,
     *       password = passwordInput,
     *       onSuccess = {
     *           navController.navigate(Screen.Home.route) {
     *               popUpTo(Screen.Onboarding.route) { inclusive = true }
     *           }
     *       }
     *   )
     */
    fun signIn(
        email: String,
        password: String,
        onSuccess: () -> Unit
    ) {
        Log.d(TAG, "signIn() called for email=$email")
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)

            when (val result = authRepository.signIn(email, password)) {
                is AppResult.Success -> {
                    Log.d(TAG, "signIn() success")
                    uiState = uiState.copy(isLoading = false)
                    onSuccess()
                }
                is AppResult.Error -> {
                    Log.e(TAG, "signIn() error: ${result.message}", result.cause)
                    uiState = uiState.copy(
                        isLoading    = false,
                        errorMessage = result.message
                    )
                }
                else -> Unit
            }
        }
    }

    /** Call this when user dismisses an error snackbar or dialog. */
    fun clearError() {
        uiState = uiState.copy(errorMessage = null)
    }
}

// ─── UI State Model ───────────────────────────────────────────────────────────

/**
 * Everything the screen needs to know about current auth state.
 *
 * In your Composable:
 *   if (uiState.isLoading) CircularProgressIndicator()
 *   uiState.errorMessage?.let { ErrorText(it) }
 */
data class AuthUiState(
    val isLoading: Boolean    = false,
    val errorMessage: String? = null    // null means no error
)