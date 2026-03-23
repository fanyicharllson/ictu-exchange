package com.fanyiadrien.ictu_ex.data.model

import androidx.compose.ui.graphics.Color

/**
 * Represents a single item held in the student's cart.
 * Wraps a Listing with cart-specific state: chosen size, color, quantity.
 */
data class CartItem(
    val id: String,
    val listingId: String,
    val title: String,
    val imageUrl: String,
    val priceXaf: Double,
    val size: String,       // e.g. "S", "M", "L", "A4", "STD"
    val colorHex: String,   // e.g. "#6200EE"
    val quantity: Int = 1
) {
    val lineTotal: Double get() = priceXaf * quantity
}
