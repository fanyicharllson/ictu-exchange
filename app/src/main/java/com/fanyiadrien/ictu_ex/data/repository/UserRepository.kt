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