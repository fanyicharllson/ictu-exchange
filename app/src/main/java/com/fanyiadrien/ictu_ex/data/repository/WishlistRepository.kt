package com.fanyiadrien.ictu_ex.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WishlistRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    private fun getWishlistCollection() = auth.currentUser?.uid?.let { uid ->
        firestore.collection("users").document(uid).collection("wishlist")
    }

    suspend fun toggleWishlist(listingId: String): Boolean {
        val collection = getWishlistCollection() ?: return false
        val doc = collection.document(listingId).get().await()
        
        return if (doc.exists()) {
            collection.document(listingId).delete().await()
            false
        } else {
            collection.document(listingId).set(mapOf("listingId" to listingId, "timestamp" to System.currentTimeMillis())).await()
            true
        }
    }

    suspend fun getWishlistedIds(): List<String> {
        val collection = getWishlistCollection() ?: return emptyList()
        return try {
            val snapshot = collection.get().await()
            snapshot.documents.map { it.id }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
