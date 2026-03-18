package com.fanyiadrien.ictu_ex.data.repository

import com.fanyiadrien.ictu_ex.data.model.User
import com.fanyiadrien.ictu_ex.utils.AppError
import com.fanyiadrien.ictu_ex.utils.AppResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    /**
     * Fetches the currently logged-in user's document from Firestore.
     * HomeViewModel calls this to get userType (SELLER/BUYER) and display name.
     */
    suspend fun getCurrentUser(): AppResult<User> {
        val uid = auth.currentUser?.uid
            ?: return AppResult.Error(AppError.UNKNOWN_AUTH_ERROR)

        return try {
            val doc = firestore.collection("users")
                .document(uid)
                .get()
                .await()

            val user = doc.toObject(User::class.java)
                ?: return AppResult.Error(AppError.FETCH_FAILED)

            AppResult.Success(user)
        } catch (e: Exception) {
            AppResult.Error(AppError.FETCH_FAILED, e)
        }
    }

    /**
     * Switches the current user's type between SELLER and BUYER in Firestore.
     */
    suspend fun switchUserType(newType: String): AppResult<Unit> {
        val uid = auth.currentUser?.uid
            ?: return AppResult.Error(AppError.UNKNOWN_AUTH_ERROR)
        return try {
            firestore.collection("users").document(uid)
                .update("userType", newType)
                .await()
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(AppError.SAVE_FAILED, e)
        }
    }

    /**
     * Deletes a user document from Firestore (called before deleting the Auth account).
     */
    suspend fun deleteUser(uid: String): AppResult<Unit> {
        return try {
            firestore.collection("users").document(uid).delete().await()
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(AppError.SAVE_FAILED, e)
        }
    }

    /**
     * Updates the current user's display name and profile image URL in Firestore.
     */
    suspend fun updateProfile(
        displayName: String,
        profileImageUrl: String
    ): AppResult<Unit> {
        val uid = auth.currentUser?.uid
            ?: return AppResult.Error(AppError.UNKNOWN_AUTH_ERROR)
        return try {
            val updates = mutableMapOf<String, Any>("displayName" to displayName)
            if (profileImageUrl.isNotBlank()) updates["profileImageUrl"] = profileImageUrl
            firestore.collection("users").document(uid).update(updates).await()
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(AppError.SAVE_FAILED, e)
        }
    }

    /**
     * Fetches any user by their UID.
     * Used in ItemDetailScreen to show seller info.
     */
    suspend fun getUserById(uid: String): AppResult<User> {
        return try {
            val doc = firestore.collection("users")
                .document(uid)
                .get()
                .await()

            val user = doc.toObject(User::class.java)
                ?: return AppResult.Error(AppError.FETCH_FAILED)

            AppResult.Success(user)
        } catch (e: Exception) {
            AppResult.Error(AppError.FETCH_FAILED, e)
        }
    }
}