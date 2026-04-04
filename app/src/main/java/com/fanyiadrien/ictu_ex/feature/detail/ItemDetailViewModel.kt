package com.fanyiadrien.ictu_ex.feature.detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fanyiadrien.ictu_ex.data.model.Listing
import com.fanyiadrien.ictu_ex.data.model.User
import com.fanyiadrien.ictu_ex.data.repository.CartRepository
import com.fanyiadrien.ictu_ex.data.repository.ListingRepository
import com.fanyiadrien.ictu_ex.data.repository.UserRepository
import com.fanyiadrien.ictu_ex.utils.AppResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ItemDetailViewModel @Inject constructor(
    private val listingRepository: ListingRepository,
    private val userRepository: UserRepository,
    private val cartRepository: CartRepository,
    savedStateHandle: SavedStateHandle         // reads listingId from nav args automatically
) : ViewModel() {

    var uiState by mutableStateOf(ItemDetailUiState())
        private set

    init {
        // Pull listingId injected by NavGraph
        val listingId = savedStateHandle.get<String>("listingId") ?: ""
        loadDetail(listingId)
    }

    fun addToCart() {
        uiState.listing?.let {
            cartRepository.addListing(it)
            uiState = uiState.copy(inCart = true)
        }
    }

    private fun loadDetail(listingId: String) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)

            // Fetch listing and seller in parallel feel
            when (val result = listingRepository.getListingById(listingId)) {
                is AppResult.Success -> {
        uiState = uiState.copy(listing = result.data, inCart = cartRepository.isInCart(result.data.id))
                    // Now fetch the seller using sellerId from listing
                    loadSeller(result.data.sellerId)
                }
                is AppResult.Error -> {
                    uiState = uiState.copy(isLoading = false, errorMessage = result.message)
                }
                else -> Unit
            }
        }
    }

    private suspend fun loadSeller(sellerId: String) {
        when (val result = userRepository.getUserById(sellerId)) {
            is AppResult.Success -> {
                uiState = uiState.copy(
                    seller    = result.data,
                    isLoading = false
                )
            }
            is AppResult.Error -> {
                // Listing still shows even if seller fetch fails
                uiState = uiState.copy(isLoading = false)
            }
            else -> Unit
        }
    }
}

data class ItemDetailUiState(
    val listing: Listing? = null,
    val seller: User? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val inCart: Boolean = false
)