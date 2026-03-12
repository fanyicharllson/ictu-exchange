package com.fanyiadrien.ictu_ex.data.model

/**
 * Represents an ICTU-Ex user stored in Firestore.
 *
 * Collection: "users"
 * Document ID: Firebase Auth UID
 */
data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val studentId: String = "",         // ICTU student ID
    val userType: String = "",          // "SELLER" or "BUYER"
    val profileImageUrl: String = "",
    val createdAt: Long = 0L
)