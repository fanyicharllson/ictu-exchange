package com.fanyiadrien.ictu_ex.feature.home

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

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val listingRepository: ListingRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    var uiState by mutableStateOf(HomeUiState())
        private set

    init {
        loadCurrentUser()
        fetchListings()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            when (val result = userRepository.getCurrentUser()) {
                is AppResult.Success -> uiState = uiState.copy(currentUser = result.data)
                else -> Unit
            }
        }
    }

    fun fetchListings() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            when (val result = listingRepository.getAllListings()) {
                is AppResult.Success -> {
                    uiState = uiState.copy(
                        isLoading = false,
                        allListings = result.data,
                        filteredListings = applyFilter(
                            result.data,
                            uiState.selectedCategory,
                            uiState.searchQuery
                        )
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
    val searchQuery: String = ""
) {
    /** True only when loading is done AND there are genuinely no listings yet */
    val isEmpty: Boolean get() = !isLoading && allListings.isEmpty()

    /** True if user is a seller — drives + button visibility */
    val isSeller: Boolean get() = currentUser?.userType == "SELLER"
}