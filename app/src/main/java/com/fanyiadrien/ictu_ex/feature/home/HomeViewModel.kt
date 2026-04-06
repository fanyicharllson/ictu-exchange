package com.fanyiadrien.ictu_ex.feature.home

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fanyiadrien.ictu_ex.data.model.Listing
import com.fanyiadrien.ictu_ex.data.model.ListingCategory
import com.fanyiadrien.ictu_ex.data.model.User
import com.fanyiadrien.ictu_ex.data.repository.CartRepository
import com.fanyiadrien.ictu_ex.data.repository.ListingRepository
import com.fanyiadrien.ictu_ex.data.repository.NotificationRepository
import com.fanyiadrien.ictu_ex.data.repository.UserRepository
import com.fanyiadrien.ictu_ex.data.repository.WishlistRepository
import com.fanyiadrien.ictu_ex.utils.AppResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "HomeViewModel"

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val listingRepository: ListingRepository,
    private val userRepository: UserRepository,
    private val wishlistRepository: WishlistRepository,
    private val cartRepository: CartRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    var uiState by mutableStateOf(HomeUiState())
        private set

    init {
        loadCurrentUser()
        fetchListings()
        loadWishlist()
        observeCart()
        observeUnreadCount()
    }

    private fun observeCart() {
        viewModelScope.launch {
            cartRepository.items.collect { items ->
                uiState = uiState.copy(cartItemCount = items.sumOf { it.quantity })
            }
        }
    }

    private fun observeUnreadCount() {
        viewModelScope.launch {
            notificationRepository.getUnreadCount().collect { count ->
                uiState = uiState.copy(unreadNotifCount = count)
            }
        }
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            when (val result = userRepository.getCurrentUser()) {
                is AppResult.Success -> {
                    uiState = uiState.copy(currentUser = result.data)
                }
                else -> Unit
            }
        }
    }

    private fun loadWishlist() {
        viewModelScope.launch {
            val ids = wishlistRepository.getWishlistedIds()
            uiState = uiState.copy(wishlistedIds = ids.toSet())
        }
    }

    fun toggleWishlist(listingId: String) {
        viewModelScope.launch {
            val isAdded = wishlistRepository.toggleWishlist(listingId)
            val current = uiState.wishlistedIds.toMutableSet()
            if (isAdded) current.add(listingId) else current.remove(listingId)
            uiState = uiState.copy(wishlistedIds = current)
        }
    }

    fun fetchListings() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            when (val result = listingRepository.getAllListings()) {
                is AppResult.Success -> {
                    val filtered = applyFilter(
                        result.data,
                        uiState.selectedCategory,
                        uiState.searchQuery
                    )
                    uiState = uiState.copy(
                        isLoading = false,
                        allListings = result.data,
                        filteredListings = filtered
                    )
                }
                is AppResult.Error -> {
                    uiState = uiState.copy(isLoading = false, errorMessage = result.message)
                }
                else -> Unit
            }
        }
    }

    fun onCategorySelected(category: ListingCategory) {
        uiState = uiState.copy(
            selectedCategory = category,
            filteredListings = applyFilter(uiState.allListings, category, uiState.searchQuery)
        )
    }

    fun onSearchQueryChanged(query: String) {
        uiState = uiState.copy(
            searchQuery = query,
            filteredListings = applyFilter(uiState.allListings, uiState.selectedCategory, query)
        )
    }

    private fun applyFilter(
        listings: List<Listing>,
        category: ListingCategory,
        query: String
    ): List<Listing> {
        return listings
            .filter { listing ->
                category == ListingCategory.ALL ||
                        listing.category.equals(category.displayName, ignoreCase = true)
            }
            .filter { listing ->
                query.isBlank() ||
                        listing.title.contains(query, ignoreCase = true) ||
                        listing.description.contains(query, ignoreCase = true)
            }
    }
}

data class HomeUiState(
    val currentUser: User? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val allListings: List<Listing> = emptyList(),
    val filteredListings: List<Listing> = emptyList(),
    val selectedCategory: ListingCategory = ListingCategory.ALL,
    val searchQuery: String = "",
    val wishlistedIds: Set<String> = emptySet(),
    val cartItemCount: Int = 0,
    val unreadNotifCount: Int = 0
) {
    val isEmpty: Boolean get() = !isLoading && allListings.isEmpty()
    val isSeller: Boolean get() = currentUser?.userType == "SELLER"
}
