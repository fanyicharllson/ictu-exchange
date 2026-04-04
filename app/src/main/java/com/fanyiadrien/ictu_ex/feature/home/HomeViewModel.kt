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
import com.fanyiadrien.ictu_ex.data.repository.ListingRepository
import com.fanyiadrien.ictu_ex.data.repository.UserRepository
import com.fanyiadrien.ictu_ex.utils.AppResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "HomeViewModel"

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val listingRepository: ListingRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    var uiState by mutableStateOf(HomeUiState())
        private set

    init {
        Log.d(TAG, "🚀 HomeViewModel initialized")
        loadCurrentUser()
        fetchListings()
    }

    private fun loadCurrentUser() {
        Log.d(TAG, "📥 Loading current user...")
        viewModelScope.launch {
            when (val result = userRepository.getCurrentUser()) {
                is AppResult.Success -> {
                    Log.d(TAG, "✅ Current user loaded: ${result.data.displayName}")
                    uiState = uiState.copy(currentUser = result.data)
                }
                is AppResult.Error -> {
                    Log.e(TAG, "❌ Failed to load current user: ${result.message}")
                }
                else -> {
                    Log.w(TAG, "⚠️ Unexpected result type loading user")
                }
            }
        }
    }

    fun fetchListings() {
        Log.d(TAG, "📡 fetchListings() called - requesting data from repository...")
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            Log.d(TAG, "⏳ Set isLoading = true, errorMessage = null")
            
            when (val result = listingRepository.getAllListings()) {
                is AppResult.Success -> {
                    Log.d(TAG, "✅ Successfully fetched ${result.data.size} listings from Firestore")
                    if (result.data.isEmpty()) {
                        Log.w(TAG, "⚠️ No listings returned from Firestore (empty list)")
                    } else {
                        Log.d(TAG, "📋 First listing: ${result.data.first().title}")
                    }
                    
                    val filtered = applyFilter(
                        result.data,
                        uiState.selectedCategory,
                        uiState.searchQuery
                    )
                    Log.d(TAG, "🔍 After filtering: ${filtered.size} listings")
                    
                    uiState = uiState.copy(
                        isLoading = false,
                        allListings = result.data,
                        filteredListings = filtered
                    )
                    Log.d(TAG, "✅ UI state updated with listings")
                }

                is AppResult.Error -> {
                    Log.e(TAG, "❌ Error fetching listings: ${result.message}")
                    uiState = uiState.copy(
                        isLoading = false, 
                        errorMessage = result.message
                    )
                }

                else -> {
                    Log.w(TAG, "⚠️ Unexpected result type: ${result::class.simpleName}")
                }
            }
        }
    }

    fun onCategorySelected(category: ListingCategory) {
        Log.d(TAG, "🏷️ Category selected: ${category.displayName}")
        uiState = uiState.copy(
            selectedCategory = category,
            filteredListings = applyFilter(uiState.allListings, category, uiState.searchQuery)
        )
    }

    fun onSearchQueryChanged(query: String) {
        Log.d(TAG, "🔍 Search query changed: '$query'")
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
) {
    /** True only when loading is done AND there are genuinely no listings yet */
    val isEmpty: Boolean get() = !isLoading && allListings.isEmpty()

    /** True if user is a seller — drives + button visibility */
    val isSeller: Boolean get() = currentUser?.userType == "SELLER"
}