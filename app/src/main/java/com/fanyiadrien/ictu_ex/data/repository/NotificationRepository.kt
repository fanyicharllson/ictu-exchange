package com.fanyiadrien.ictu_ex.data.repository

import com.fanyiadrien.ictu_ex.data.model.Notification
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    // ── Real-time reads ───────────────────────────────────────────────────────

    /** Live stream of all notifications for the current user, newest first. */
    fun getNotifications(): Flow<List<Notification>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) { trySend(emptyList()); close(); return@callbackFlow }

        val sub = firestore.collection("notifications")
            .document(uid).collection("items")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                snap?.let { trySend(it.toObjects(Notification::class.java)) }
            }

        awaitClose { sub.remove() }
    }

    /** Live count of unread notifications — drives the badge in IctuBottomNav. */
    fun getUnreadCount(): Flow<Int> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) { trySend(0); close(); return@callbackFlow }

        val sub = firestore.collection("notifications")
            .document(uid).collection("items")
            .whereEqualTo("read", false)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                trySend(snap?.size() ?: 0)
            }

        awaitClose { sub.remove() }
    }

    // ── Writes ────────────────────────────────────────────────────────────────

    /**
     * Called by PostItemViewModel after a listing is saved to Firestore.
     */
    suspend fun notifyBuyersNewListing(
        sellerId: String,
        sellerName: String,
        listingTitle: String,
        listingId: String,
        priceXaf: Double
    ) {
        val buyerDocs = firestore.collection("users")
            .whereEqualTo("userType", "BUYER")
            .get().await()

        if (buyerDocs.isEmpty) return

        val batch = firestore.batch()
        val now = System.currentTimeMillis()

        for (doc in buyerDocs.documents) {
            val buyerUid = doc.id
            if (buyerUid == sellerId) continue

            val notifId = UUID.randomUUID().toString()
            val ref = firestore.collection("notifications")
                .document(buyerUid).collection("items").document(notifId)

            batch.set(ref, mapOf(
                "notifId"     to notifId,
                "type"        to "NEW_LISTING",
                "sellerId"    to sellerId,
                "sellerName"  to sellerName,
                "listingId"   to listingId,
                "itemSummary" to "\"$listingTitle\" is now available for XAF ${priceXaf.toInt()}",
                "totalXaf"    to priceXaf,
                "read"        to false,
                "createdAt"   to now
            ))
        }

        batch.commit().await()
    }

    suspend fun notifySellerNewOrder(
        sellerId: String,
        buyerId: String,
        buyerName: String,
        orderId: String,
        itemSummary: String,
        totalXaf: Double
    ) {
        val notifId = UUID.randomUUID().toString()
        firestore.collection("notifications")
            .document(sellerId).collection("items").document(notifId)
            .set(mapOf(
                "notifId"     to notifId,
                "type"        to "NEW_ORDER",
                "orderId"     to orderId,
                "buyerId"     to buyerId,
                "buyerName"   to buyerName,
                "itemSummary" to itemSummary,
                "totalXaf"    to totalXaf,
                "read"        to false,
                "createdAt"   to System.currentTimeMillis()
            )).await()
    }

    suspend fun notifyBuyerOrderPlaced(
        buyerId: String,
        orderId: String,
        itemSummary: String,
        totalXaf: Double
    ) {
        val notifId = UUID.randomUUID().toString()
        firestore.collection("notifications")
            .document(buyerId).collection("items").document(notifId)
            .set(mapOf(
                "notifId"     to notifId,
                "type"        to "ORDER_PLACED",
                "orderId"     to orderId,
                "itemSummary" to itemSummary,
                "totalXaf"    to totalXaf,
                "read"        to false,
                "createdAt"   to System.currentTimeMillis()
            )).await()
    }

    /**
     * Notify user that they added an item to their favorites.
     */
    suspend fun notifyUserItemFavorited(
        userId: String,
        listingId: String,
        listingTitle: String
    ) {
        val notifId = UUID.randomUUID().toString()
        firestore.collection("notifications")
            .document(userId).collection("items").document(notifId)
            .set(mapOf(
                "notifId"     to notifId,
                "type"        to "ITEM_FAVORITED",
                "listingId"   to listingId,
                "itemSummary" to "You added \"$listingTitle\" to your favorites.",
                "read"        to false,
                "createdAt"   to System.currentTimeMillis()
            )).await()
    }

    /**
     * Written when a buyer adds a listing to the cart.
     */
    suspend fun notifyBuyerCartAdded(
        listingId: String,
        itemSummary: String
    ) {
        val buyerId = auth.currentUser?.uid ?: return
        val notifId = UUID.randomUUID().toString()
        firestore.collection("notifications")
            .document(buyerId).collection("items").document(notifId)
            .set(mapOf(
                "notifId"     to notifId,
                "type"        to "CART_ADDED",
                "listingId"   to listingId,
                "itemSummary" to itemSummary,
                "read"        to false,
                "createdAt"   to System.currentTimeMillis()
            )).await()
    }

    // ── Mark read / delete ────────────────────────────────────────────────────

    suspend fun markAsRead(notifId: String) {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("notifications")
            .document(uid).collection("items").document(notifId)
            .update("read", true).await()
    }

    suspend fun deleteNotification(notifId: String) {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("notifications")
            .document(uid).collection("items").document(notifId)
            .delete().await()
    }
}