package com.fanyiadrien.ictu_ex.feature.profile

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fanyiadrien.ictu_ex.data.remote.CloudinaryService
import com.fanyiadrien.ictu_ex.data.repository.UserRepository
import com.fanyiadrien.ictu_ex.utils.AppResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditProfileUiState(
    val displayName: String = "",
    val email: String = "",
    val avatarUrl: String = "",
    val pendingImageUri: Uri? = null,   // local URI before upload
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val cloudinaryService: CloudinaryService
) : ViewModel() {

    var uiState by mutableStateOf(EditProfileUiState())
        private set

    init { loadUser() }

    private fun loadUser() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            when (val result = userRepository.getCurrentUser()) {
                is AppResult.Success -> uiState = uiState.copy(
                    displayName = result.data.displayName,
                    email       = result.data.email,
                    avatarUrl   = result.data.profileImageUrl,
                    isLoading   = false
                )
                is AppResult.Error -> uiState = uiState.copy(
                    isLoading    = false,
                    errorMessage = result.message
                )
                else -> uiState = uiState.copy(isLoading = false)
            }
        }
    }

    fun onDisplayNameChange(value: String) {
        uiState = uiState.copy(displayName = value, errorMessage = null)
    }

    fun onImagePicked(uri: Uri) {
        uiState = uiState.copy(pendingImageUri = uri)
    }

    fun clearFeedback() {
        uiState = uiState.copy(successMessage = null, errorMessage = null)
    }

    fun updateProfile(context: Context) {
        val name = uiState.displayName.trim()
        if (name.isBlank()) {
            uiState = uiState.copy(errorMessage = "Display name cannot be empty.")
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isSaving = true, errorMessage = null)

            // Upload new avatar if one was picked
            val finalAvatarUrl = uiState.pendingImageUri?.let { uri ->
                when (val upload = cloudinaryService.uploadImage(context, uri, "profiles")) {
                    is AppResult.Success -> upload.data
                    is AppResult.Error   -> {
                        uiState = uiState.copy(isSaving = false, errorMessage = upload.message)
                        return@launch
                    }
                    else -> uiState.avatarUrl
                }
            } ?: uiState.avatarUrl

            when (val result = userRepository.updateProfile(name, finalAvatarUrl)) {
                is AppResult.Success -> uiState = uiState.copy(
                    isSaving        = false,
                    avatarUrl       = finalAvatarUrl,
                    pendingImageUri = null,
                    successMessage  = "Profile updated successfully!"
                )
                is AppResult.Error -> uiState = uiState.copy(
                    isSaving     = false,
                    errorMessage = result.message
                )
                else -> uiState = uiState.copy(isSaving = false)
            }
        }
    }
}
