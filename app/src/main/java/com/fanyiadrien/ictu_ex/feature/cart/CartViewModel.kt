package com.fanyiadrien.ictu_ex.feature.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fanyiadrien.ictu_ex.data.repository.CartRepository
import com.fanyiadrien.ictu_ex.utils.AppResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CartUiState(
    val promoCode: String = "",
    val promoApplied: Boolean = false,
    val discountPercent: Int = 0,
    val isCheckingOut: Boolean = false,
    val snackbarMessage: String? = null,
    val checkoutOrderId: String? = null   // non-null = checkout succeeded
)

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository
) : ViewModel() {

    // Items come directly from the repository's shared StateFlow
    val cartItems = cartRepository.items

    // Local UI-only state (promo, loading, snackbar)
    private val _ui = MutableStateFlow(CartUiState())

    // Combined state the screen observes
    val uiState: StateFlow<FullCartState> = combine(cartItems, _ui) { items, ui ->
        val subtotal = items.sumOf { it.lineTotal }
        val discount = subtotal * ui.discountPercent / 100.0
        FullCartState(
            items           = items,
            promoCode       = ui.promoCode,
            promoApplied    = ui.promoApplied,
            discountPercent = ui.discountPercent,
            subtotal        = subtotal,
            discount        = discount,
            total           = subtotal - discount,
            itemCount       = items.sumOf { it.quantity },
            isCheckingOut   = ui.isCheckingOut,
            snackbarMessage = ui.snackbarMessage,
            checkoutOrderId = ui.checkoutOrderId
        )
    }.stateIn(
        scope         = viewModelScope,
        started       = SharingStarted.WhileSubscribed(5_000),
        initialValue  = FullCartState()
    )

    // ── Quantity ──────────────────────────────────────────────────────────────

    fun increment(itemId: String) = cartRepository.increment(itemId)
    fun decrement(itemId: String) = cartRepository.decrement(itemId)
    fun removeItem(itemId: String) = cartRepository.remove(itemId)

    // ── Promo code ────────────────────────────────────────────────────────────

    fun onPromoCodeChange(code: String) {
        _ui.update { it.copy(promoCode = code) }
    }

    fun applyPromoCode() {
        val code = _ui.value.promoCode.trim().uppercase()
        val (pct, msg) = when (code) {
            "ICTU10"  -> 10 to "10% discount applied! 🎉"
            "STUDENT" -> 15 to "Student discount: 15% off! 🎓"
            "CAMPUS5" ->  5 to "Campus deal: 5% off! 🏫"
            else      ->  0 to "Invalid promo code."
        }
        _ui.update { it.copy(discountPercent = pct, promoApplied = pct > 0, snackbarMessage = msg) }
    }

    // ── Checkout ──────────────────────────────────────────────────────────────

    fun checkout() {
        viewModelScope.launch {
            _ui.update { it.copy(isCheckingOut = true) }
            val result = cartRepository.checkout(
                discountPercent = _ui.value.discountPercent,
                promoCode       = _ui.value.promoCode
            )
            when (result) {
                is AppResult.Success -> _ui.update {
                    it.copy(
                        isCheckingOut   = false,
                        checkoutOrderId = result.data,
                        snackbarMessage = "Order placed! Sellers have been notified. 🎉"
                    )
                }
                is AppResult.Error -> _ui.update {
                    it.copy(isCheckingOut = false, snackbarMessage = result.message)
                }
                else -> _ui.update { it.copy(isCheckingOut = false) }
            }
        }
    }

    fun dismissSnackbar() {
        _ui.update { it.copy(snackbarMessage = null) }
    }
}

// ── Combined state exposed to the screen ─────────────────────────────────────
data class FullCartState(
    val items: List<com.fanyiadrien.ictu_ex.data.model.CartItem> = emptyList(),
    val promoCode: String = "",
    val promoApplied: Boolean = false,
    val discountPercent: Int = 0,
    val subtotal: Double = 0.0,
    val discount: Double = 0.0,
    val total: Double = 0.0,
    val itemCount: Int = 0,
    val isCheckingOut: Boolean = false,
    val snackbarMessage: String? = null,
    val checkoutOrderId: String? = null
)
