package com.fanyiadrien.ictu_ex.feature.post

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fanyiadrien.ictu_ex.data.model.Listing
import com.fanyiadrien.ictu_ex.data.model.ListingCategory
import com.fanyiadrien.ictu_ex.data.remote.CloudinaryService
import com.fanyiadrien.ictu_ex.data.repository.ListingRepository
import com.fanyiadrien.ictu_ex.data.repository.NotificationRepository
import com.fanyiadrien.ictu_ex.data.repository.UserRepository
import com.fanyiadrien.ictu_ex.utils.AppError
import com.fanyiadrien.ictu_ex.utils.AppResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostItemViewModel @Inject constructor(
    private val cloudinaryService: CloudinaryService,
    private val listingRepository: ListingRepository,
    private val userRepository: UserRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    var uiState by mutableStateOf(PostItemUiState())
        private set

    // ── Form field updates ────────────────────────────────────────────────────

    fun onTitleChanged(value: String)       { uiState = uiState.copy(title = value, errorMessage = null) }
    fun onDescriptionChanged(value: String) { uiState = uiState.copy(description = value) }
    fun onPriceChanged(value: String)       { uiState = uiState.copy(price = value, errorMessage = null) }
    fun onCategorySelected(cat: ListingCategory) { uiState = uiState.copy(selectedCategory = cat) }

    fun onImageSelected(uri: Uri) {
        uiState = uiState.copy(selectedImageUri = uri, errorMessage = null)
    }

    fun clearError() {
        uiState = uiState.copy(errorMessage = null)
    }

    // ── Post listing — the full 2-step pipeline ───────────────────────────────

    /**
     * Step 1 → Upload image to Cloudinary
     * Step 2 → Save listing to Firestore
     *
     * During step 1: isUploading = true  → screen shows "Uploading image…"
     * During step 2: isSaving = true     → screen shows "Saving listing…"
     * Both states: entire UI is locked, back press blocked
     *
     * @param context  Needed by Cloudinary SDK
     * @param onSuccess Called when listing is saved — screen navigates back
     */
    fun postListing(context: Context, onSuccess: () -> Unit) {
        val imageUri = uiState.selectedImageUri
        if (imageUri == null) {
            uiState = uiState.copy(errorMessage = "Please select an image.")
            return
        }

        // Basic form validation before hitting any network
        val priceValue = uiState.price.toDoubleOrNull()
        if (uiState.title.isBlank()) {
            uiState = uiState.copy(errorMessage = "Please enter a title.")
            return
        }
        if (priceValue == null || priceValue <= 0) {
            uiState = uiState.copy(errorMessage = "Please enter a valid price.")
            return
        }

        viewModelScope.launch {

            // ── Step 1: Upload image ──────────────────────────────────────
            uiState = uiState.copy(
                isUploading  = true,
                isSaving     = false,
                errorMessage = null
            )

            val uploadResult = cloudinaryService.uploadImage(
                context  = context,
                imageUri = imageUri,
                folder   = "listings"
            )

            if (uploadResult is AppResult.Error) {
                uiState = uiState.copy(
                    isUploading  = false,
                    errorMessage = uploadResult.message
                )
                return@launch
            }

            val imageUrl = (uploadResult as AppResult.Success).data

            // ── Step 2: Save to Firestore ─────────────────────────────────
            uiState = uiState.copy(isUploading = false, isSaving = true)

            // Fetch seller's studentId to attach to listing
            val sellerStudentId = when (val u = userRepository.getCurrentUser()) {
                is AppResult.Success -> u.data.studentId
                else -> ""
            }

            val listing = Listing(
                title          = uiState.title.trim(),
                description    = uiState.description.trim(),
                price          = priceValue,
                imageUrl       = imageUrl,
                category       = uiState.selectedCategory.displayName,
                sellerStudentId = sellerStudentId,
                available    = true
            )

            when (val saveResult = listingRepository.postListing(listing)) {
                is AppResult.Success -> {
                    // Notify all buyers about the new listing
                    notificationRepository.notifyBuyersNewListing(
                        sellerId     = saveResult.data.sellerId,
                        sellerName   = sellerStudentId.ifBlank { "A seller" },
                        listingTitle = saveResult.data.title,
                        listingId    = saveResult.data.id,
                        priceXaf     = saveResult.data.price
                    )
                    uiState = uiState.copy(isSaving = false, isSuccess = true)
                    onSuccess()
                }
                is AppResult.Error -> {
                    uiState = uiState.copy(
                        isSaving     = false,
                        errorMessage = saveResult.message
                    )
                }
                else -> Unit
            }
        }
    }
}

// ── UI State ──────────────────────────────────────────────────────────────────

data class PostItemUiState(
    // Form fields
    val title: String = "",
    val description: String = "",
    val price: String = "",
    val selectedCategory: ListingCategory = ListingCategory.TEXTBOOKS,
    val selectedImageUri: Uri? = null,

    // Process states — two separate flags for two-step feedback
    val isUploading: Boolean = false,   // Cloudinary upload in progress
    val isSaving: Boolean = false,      // Firestore save in progress
    val isSuccess: Boolean = false,

    val errorMessage: String? = null
) {
    /** True when ANY async operation is running — used to lock the UI */
    val isLocked: Boolean get() = isUploading || isSaving

    /** Human-readable status message shown in the loading overlay */
    val loadingMessage: String get() = when {
        isUploading -> "Uploading image…\nPlease wait"
        isSaving    -> "Saving your listing…"
        else        -> ""
    }

    /** True when form has minimum required fields filled */
    val isFormValid: Boolean get() =
        title.isNotBlank() &&
                price.isNotBlank() &&
                price.toDoubleOrNull() != null &&
                selectedImageUri != null
}