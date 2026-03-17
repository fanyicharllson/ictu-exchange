package com.fanyiadrien.ictu_ex.data.repository

import android.util.Log
import com.fanyiadrien.ictu_ex.data.model.Listing
import com.fanyiadrien.ictu_ex.utils.AppError
import com.fanyiadrien.ictu_ex.utils.AppResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "ListingRepository"

@Singleton
class ListingRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    private val listingsCollection = firestore.collection("listings")

    /**
     * Fetches all available listings, newest first.
     * HomeViewModel calls this on load and on pull-to-refresh.
     */
    suspend fun getAllListings(): AppResult<List<Listing>> {
        Log.d(TAG, "📡 getAllListings() called")
        return try {
            val snapshot = listingsCollection
                .whereEqualTo("available", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            Log.d(TAG, "✅ Firestore query successful, got ${snapshot.documents.size} documents")
            
            if (snapshot.documents.isEmpty()) {
                Log.w(TAG, "⚠️ Firestore returned empty documents list (no listings with available=true)")
            } else {
                snapshot.documents.forEachIndexed { index, doc ->
                    Log.d(TAG, "📋 Doc #${index + 1}: id=${doc.id}, title=${doc.get("title") ?: "N/A"}, available=${doc.get("available") ?: "N/A"}")
                }
            }

            val listings = snapshot.documents.mapNotNull { doc ->
                try {
                    val listing = doc.toObject(Listing::class.java)?.copy(id = doc.id)
                    if (listing != null) {
                        Log.d(TAG, "✅ Mapped document to Listing: ${listing.title}")
                    } else {
                        Log.w(TAG, "⚠️ Failed to map document ${doc.id} to Listing object")
                    }
                    listing
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error mapping document ${doc.id} to Listing: ${e.message}", e)
                    null
                }
            }

            Log.d(TAG, "✅ Successfully parsed ${listings.size} listings from ${snapshot.documents.size} documents")
            AppResult.Success(listings)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception in getAllListings(): ${e.javaClass.simpleName}")
            Log.e(TAG, "❌ Error message: ${e.message}")
            Log.e(TAG, "❌ Full stacktrace:", e)
            
            if (e.message?.contains("network", ignoreCase = true) == true) {
                Log.e(TAG, "🌐 Classified as NETWORK_ERROR")
                AppResult.Error(AppError.NETWORK_ERROR, e)
            } else if (e.message?.contains("permission", ignoreCase = true) == true) {
                Log.e(TAG, "🔐 Detected PERMISSION error - likely Firestore security rules issue")
                AppResult.Error(AppError.FETCH_FAILED, e)
            } else {
                Log.e(TAG, "❌ Classified as FETCH_FAILED")
                AppResult.Error(AppError.FETCH_FAILED, e)
            }
        }
    }

    /**
     * Fetches a single listing by its Firestore document ID.
     * ItemDetailScreen calls this.
     */
    suspend fun getListingById(listingId: String): AppResult<Listing> {
        return try {
            val doc = listingsCollection.document(listingId).get().await()
            val listing = doc.toObject(Listing::class.java)?.copy(id = doc.id)
                ?: return AppResult.Error(AppError.FETCH_FAILED)
            AppResult.Success(listing)
        } catch (e: Exception) {
            AppResult.Error(AppError.FETCH_FAILED, e)
        }
    }

    /**
     * Posts a new listing to Firestore.
     * PostItemViewModel calls this after Cloudinary returns the image URL.
     *
     * @param listing  Fully built Listing object with imageUrl already set
     */
    suspend fun postListing(listing: Listing): AppResult<Listing> {
        val uid = auth.currentUser?.uid
            ?: return AppResult.Error(AppError.UNKNOWN_AUTH_ERROR)

        return try {
            val docRef = listingsCollection.document()  // auto-generate ID

            val listingWithId = listing.copy(
                id        = docRef.id,
                sellerId  = uid,
                createdAt = System.currentTimeMillis()
            )

            docRef.set(listingWithId).await()
            AppResult.Success(listingWithId)
        } catch (e: Exception) {
            AppResult.Error(AppError.SAVE_FAILED, e)
        }
    }

    /**
     * Marks a listing as sold/unavailable.
     * Called after QR delivery confirmation.
     */
    suspend fun markListingUnavailable(listingId: String): AppResult<Unit> {
        return try {
            listingsCollection.document(listingId)
                .update("available", false)
                .await()
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(AppError.SAVE_FAILED, e)
        }
    }

    /**
     * Fetches all listings posted by the current logged-in seller.
     * ProfileScreen uses this to show "My Listings".
     */
    suspend fun getMyListings(): AppResult<List<Listing>> {
        val uid = auth.currentUser?.uid
            ?: return AppResult.Error(AppError.UNKNOWN_AUTH_ERROR)

        return try {
            val snapshot = listingsCollection
                .whereEqualTo("sellerId", uid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val listings = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Listing::class.java)?.copy(id = doc.id)
            }

            AppResult.Success(listings)
        } catch (e: Exception) {
            AppResult.Error(AppError.FETCH_FAILED, e)
        }
    }
}