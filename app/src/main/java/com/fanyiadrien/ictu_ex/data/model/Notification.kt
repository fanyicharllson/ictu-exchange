package com.fanyiadrien.ictu_ex.data.model

/**
 * Represents a notification stored in Firestore.
 *
 * Collection: "notifications/{userId}/items"
 *
 * Types:
 *  - NEW_LISTING  → written to all buyers when a seller posts a new item
 *  - NEW_ORDER    → written to the seller when a buyer checks out
 *  - ORDER_PLACED → written to the buyer confirming their own checkout
 */
data class Notification(
    val notifId: String = "",
    val type: String = "",
    val orderId: String = "",
    val buyerId: String = "",
    val sellerId: String = "",
    val itemSummary: String = "",
    val totalXaf: Double = 0.0,
    val read: Boolean = false,
    val createdAt: Long = 0L
)
