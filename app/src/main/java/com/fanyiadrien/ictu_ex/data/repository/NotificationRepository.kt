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

    // ── Real-time listener ────────────────────────────────────────────────────

    /**
     * Live stream of notifications for the currently logged-in user.
     * Works for both sellers (NEW_ORDER) and buyers (NEW_LISTING, ORDER_PLACED).
     */
    fun getNotifications(): Flow<List<Notification>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val subscription = firestore.collection("notifications")
            .document(uid)
            .collection("items")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                snapshot?.let { trySend(it.toObjects(Notification::class.java)) }
            }

        awaitClose { subscription.remove() }
    }

    /** Count of unread notifications — used for the badge in the bottom nav. */
    fun getUnreadCount(): Flow<Int> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) { trySend(0); close(); return@callbackFlow }

        val subscription = firestore.collection("notifications")
            .document(uid)
            .collection("items")
            .whereEqualTo("read", false)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                trySend(snapshot?.size() ?: 0)
            }

        awaitClose { subscription.remove() }
    }

    // ── Write: seller posts a new listing → notify all buyers ────────────────

    /**
     * Called by PostItemViewModel after a listing is saved.
     * Fetches every user with userType == "BUYER" and writes a NEW_LISTING
     * notification document under notifications/{buyerUid}/items/{notifId}.
     */
    suspend fun notifyBuyersNewListing(
        sellerId: String,
        sellerName: String,
        listingTitle: String,
        listingId: String,
        priceXaf: Double
    ) {
        val now = System.currentTimeMillis()

        // Fetch all buyers
        val buyerDocs = firestore.collection("users")
            .whereEqualTo("userType", "BUYER")
            .get()
            .await()

        if (buyerDocs.isEmpty) return

        val batch = firestore.batch()

        for (doc in buyerDocs.documents) {
            val buyerUid = doc.id
            if (buyerUid == sellerId) continue   // don't notify yourself

            val notifId = UUID.randomUUID().toString()
            val ref = firestore.collection("notifications")
                .document(buyerUid)
                .collection("items")
                .document(notifId)

            batch.set(ref, mapOf(
                "notifId"      to notifId,
                "type"         to "NEW_LISTING",
                "sellerId"     to sellerId,
                "sellerName"   to sellerName,
                "listingId"    to listingId,
                "itemSummary"  to "\"$listingTitle\" is now available for XAF ${priceXaf.toInt()}",
                "totalXaf"     to priceXaf,
                "read"         to false,
                "createdAt"    to now
            ))
        }

        batch.commit().await()
    }

    // ── Write: buyer checks out → notify seller + confirm to buyer ────────────

    /**
     * Called by CartRepository.checkout() for each unique seller in the cart.
     * Writes a NEW_ORDER notification to the seller.
     */
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
            .document(sellerId)
            .collection("items")
            .document(notifId)
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
            ))
            .await()
    }

    /**
     * Called by CartRepository.checkout() once for the buyer.
     * Confirms their order was placed successfully.
     */
    suspend fun notifyBuyerOrderPlaced(
        buyerId: String,
        orderId: String,
        itemSummary: String,
        totalXaf: Double
    ) {
        val notifId = UUID.randomUUID().toString()
        firestore.collection("notifications")
            .document(buyerId)
            .collection("items")
            .document(notifId)
            .set(mapOf(
                "notifId"     to notifId,
                "type"        to "ORDER_PLACED",
                "orderId"     to orderId,
                "itemSummary" to itemSummary,
                "totalXaf"    to totalXaf,
                "read"        to false,
                "createdAt"   to System.currentTimeMillis()
            ))
            .await()
    }

    // ── Mark read / delete ────────────────────────────────────────────────────

    suspend fun markAsRead(notifId: String) {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("notifications")
            .document(uid).collection("items").document(notifId)
            .update("read", true)
            .await()
    }

    suspend fun deleteNotification(notifId: String) {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("notifications")
            .document(uid).collection("items").document(notifId)
            .delete()
            .await()
    }
}
