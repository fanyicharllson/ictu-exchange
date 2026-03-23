package com.fanyiadrien.ictu_ex.data.repository

import com.fanyiadrien.ictu_ex.data.model.CartItem
import com.fanyiadrien.ictu_ex.data.model.Listing
import com.fanyiadrien.ictu_ex.utils.AppError
import com.fanyiadrien.ictu_ex.utils.AppResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    // ── Shared cart state (survives screen navigation) ────────────────────────
    private val _items = MutableStateFlow<List<CartItem>>(emptyList())
    val items: StateFlow<List<CartItem>> = _items.asStateFlow()

    // ── Add / remove / quantity ───────────────────────────────────────────────

    fun addListing(listing: Listing) {
        _items.update { current ->
            val existing = current.find { it.listingId == listing.id }
            if (existing != null) {
                // Already in cart — just bump quantity
                current.map {
                    if (it.listingId == listing.id) it.copy(quantity = it.quantity + 1) else it
                }
            } else {
                current + CartItem(
                    id        = UUID.randomUUID().toString(),
                    listingId = listing.id,
                    title     = listing.title,
                    imageUrl  = listing.imageUrl,
                    priceXaf  = listing.price,
                    size      = "STD",
                    colorHex  = "#6200EE",
                    quantity  = 1
                )
            }
        }
    }

    fun increment(itemId: String) {
        _items.update { list ->
            list.map { if (it.id == itemId) it.copy(quantity = it.quantity + 1) else it }
        }
    }

    fun decrement(itemId: String) {
        _items.update { list ->
            list.map {
                if (it.id == itemId && it.quantity > 1) it.copy(quantity = it.quantity - 1) else it
            }
        }
    }

    fun remove(itemId: String) {
        _items.update { list -> list.filter { it.id != itemId } }
    }

    fun isInCart(listingId: String): Boolean =
        _items.value.any { it.listingId == listingId }

    // ── Checkout ──────────────────────────────────────────────────────────────

    /**
     * 1. Writes an order document to Firestore under "orders/{orderId}".
     * 2. For every unique seller in the cart, writes a notification document
     *    to "notifications/{sellerId}/items/{notifId}" so the seller's device
     *    can pick it up via a Firestore listener or FCM data message.
     * 3. Clears the local cart on success.
     */
    suspend fun checkout(
        discountPercent: Int,
        promoCode: String
    ): AppResult<String> {
        val buyerId = auth.currentUser?.uid
            ?: return AppResult.Error(AppError.UNKNOWN_AUTH_ERROR)

        val cartItems = _items.value
        if (cartItems.isEmpty()) return AppResult.Error("Cart is empty.")

        val orderId  = UUID.randomUUID().toString()
        val subtotal = cartItems.sumOf { it.lineTotal }
        val discount = subtotal * discountPercent / 100.0
        val total    = subtotal - discount
        val now      = System.currentTimeMillis()

        return try {
            val batch = firestore.batch()

            // ── 1. Order document ─────────────────────────────────────────
            val orderRef = firestore.collection("orders").document(orderId)
            batch.set(orderRef, mapOf(
                "orderId"         to orderId,
                "buyerId"         to buyerId,
                "items"           to cartItems.map { item ->
                    mapOf(
                        "listingId" to item.listingId,
                        "title"     to item.title,
                        "quantity"  to item.quantity,
                        "priceXaf"  to item.priceXaf,
                        "lineTotal" to item.lineTotal
                    )
                },
                "subtotalXaf"     to subtotal,
                "discountXaf"     to discount,
                "totalXaf"        to total,
                "promoCode"       to promoCode,
                "status"          to "PENDING",
                "createdAt"       to now
            ))

            // ── 2. Seller notifications ───────────────────────────────────
            // Fetch each listing to get its sellerId, then write one
            // notification per seller (grouped by seller, not per item).
            val sellerItems = mutableMapOf<String, MutableList<CartItem>>()

            for (item in cartItems) {
                val doc = firestore.collection("listings")
                    .document(item.listingId)
                    .get()
                    .await()
                val sellerId = doc.getString("sellerId") ?: continue
                sellerItems.getOrPut(sellerId) { mutableListOf() }.add(item)
            }

            for ((sellerId, items) in sellerItems) {
                val notifId  = UUID.randomUUID().toString()
                val notifRef = firestore
                    .collection("notifications")
                    .document(sellerId)
                    .collection("items")
                    .document(notifId)

                val itemSummary = items.joinToString(", ") {
                    "${it.title} ×${it.quantity}"
                }
                val sellerTotal = items.sumOf { it.lineTotal }

                batch.set(notifRef, mapOf(
                    "notifId"     to notifId,
                    "type"        to "NEW_ORDER",
                    "orderId"     to orderId,
                    "buyerId"     to buyerId,
                    "itemSummary" to itemSummary,
                    "totalXaf"    to sellerTotal,
                    "read"        to false,
                    "createdAt"   to now
                ))
            }

            batch.commit().await()

            // ── 3. Clear cart ─────────────────────────────────────────────
            _items.update { emptyList() }

            AppResult.Success(orderId)

        } catch (e: Exception) {
            AppResult.Error(AppError.SAVE_FAILED, e)
        }
    }
}
