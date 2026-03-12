package com.fanyiadrien.ictu_ex.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * AuthViewModel handles authentication logic for ICTU-Ex.
 *
 * Manages sign-in and sign-up flows with Firebase Authentication.
 * Provides UI state for loading, success, and error states.
 */
class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // UI State
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    // Current user
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    /**
     * Sign in with email and password
     */
    fun signIn(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Email and password cannot be empty")
            return
        }

        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error(getAuthErrorMessage(e))
            }
        }
    }

    /**
     * Sign up with email, password, and user type
     */
    fun signUp(email: String, password: String, userType: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Email and password cannot be empty")
            return
        }

        if (password.length < 6) {
            _authState.value = AuthState.Error("Password must be at least 6 characters")
            return
        }

        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                auth.createUserWithEmailAndPassword(email, password).await()
                // TODO: Save user type to Firestore or user profile
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error(getAuthErrorMessage(e))
            }
        }
    }

    /**
     * Sign out current user
     */
    fun signOut() {
        auth.signOut()
        _authState.value = AuthState.Idle
    }

    /**
     * Reset auth state to idle
     */
    fun resetState() {
        _authState.value = AuthState.Idle
    }

    /**
     * Convert Firebase auth exceptions to user-friendly messages
     */
    private fun getAuthErrorMessage(exception: Exception): String {
        return when (exception.message) {
            "The email address is badly formatted." -> "Please enter a valid email address"
            "The password is invalid or the user does not have a password." -> "Invalid password"
            "There is no user record corresponding to this identifier. The user may have been deleted." -> "No account found with this email"
            "The email address is already in use by another account." -> "An account with this email already exists"
            "A network error (such as timeout, interrupted connection or unreachable host) has occurred." -> "Network error. Please check your connection"
            else -> "Authentication failed. Please try again"
        }
    }
}

/**
 * Authentication UI state
 */
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}
