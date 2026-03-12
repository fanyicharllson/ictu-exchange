package com.fanyiadrien.ictu_ex.data.repository

import com.fanyiadrien.ictu_ex.data.model.User
import com.fanyiadrien.ictu_ex.utils.AppError
import com.fanyiadrien.ictu_ex.utils.AppResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    // ─── Email Validation ─────────────────────────────────────────────────────

    /**
     * ICTU-Ex rule: only @ictuniversity.edu.cm emails are allowed.
     * Called before even hitting Firebase — fast, free check.
     */
    private fun isValidIctuEmail(email: String): Boolean {
        return email.trim().endsWith("@ictuniversity.edu.cm")
    }

    // ─── Sign Up ──────────────────────────────────────────────────────────────

    /**
     * Creates a new Firebase Auth account + saves user document to Firestore.
     *
     * @param email       Must end with @ictuniversity.edu.cm
     * @param password    Minimum 6 characters (Firebase rule)
     * @param displayName Student's full name
     * @param studentId   ICTU student ID number
     * @param userType    "SELLER" or "BUYER"
     *
     * Usage in AuthViewModel:
     *   val result = authRepository.signUp(email, password, name, id, userType)
     *   when (result) {
     *       is AppResult.Success -> navController.navigate(Screen.Home.route) { ... }
     *       is AppResult.Error   -> uiState = uiState.copy(error = result.message)
     *   }
     */
    suspend fun signUp(
        email: String,
        password: String,
        displayName: String,
        studentId: String,
        userType: String
    ): AppResult<User> {
        // 1. Validate ICTU email before calling Firebase
        if (!isValidIctuEmail(email)) {
            return AppResult.Error(AppError.INVALID_ICTU_EMAIL)
        }

        return try {
            // 2. Create Firebase Auth account
            val authResult = auth
                .createUserWithEmailAndPassword(email.trim(), password)
                .await()

            val uid = authResult.user?.uid
                ?: return AppResult.Error(AppError.UNKNOWN_AUTH_ERROR)

            // 3. Build user document
            val user = User(
                uid            = uid,
                email          = email.trim(),
                displayName    = displayName.trim(),
                studentId      = studentId.trim(),
                userType       = userType,              // "SELLER" or "BUYER"
                createdAt      = System.currentTimeMillis()
            )

            // 4. Save to Firestore under "users/{uid}"
            firestore.collection("users")
                .document(uid)
                .set(user)
                .await()

            AppResult.Success(user)

        } catch (e: FirebaseAuthWeakPasswordException) {
            AppResult.Error(AppError.WEAK_PASSWORD, e)
        } catch (e: FirebaseAuthUserCollisionException) {
            AppResult.Error(AppError.EMAIL_ALREADY_IN_USE, e)
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            AppResult.Error(AppError.INVALID_ICTU_EMAIL, e)
        } catch (e: Exception) {
            if (e.message?.contains("network", ignoreCase = true) == true) {
                AppResult.Error(AppError.NETWORK_ERROR, e)
            } else {
                AppResult.Error(AppError.UNKNOWN_AUTH_ERROR, e)
            }
        }
    }

    // ─── Sign In ──────────────────────────────────────────────────────────────

    /**
     * Signs in an existing user with email and password.
     *
     * Usage in AuthViewModel:
     *   val result = authRepository.signIn(email, password)
     *   when (result) {
     *       is AppResult.Success -> navController.navigate(Screen.Home.route) { ... }
     *       is AppResult.Error   -> uiState = uiState.copy(error = result.message)
     *   }
     */
    suspend fun signIn(
        email: String,
        password: String
    ): AppResult<User> {
        if (!isValidIctuEmail(email)) {
            return AppResult.Error(AppError.INVALID_ICTU_EMAIL)
        }

        return try {
            val authResult = auth
                .signInWithEmailAndPassword(email.trim(), password)
                .await()

            val uid = authResult.user?.uid
                ?: return AppResult.Error(AppError.UNKNOWN_AUTH_ERROR)

            // Fetch user document from Firestore
            val document = firestore.collection("users")
                .document(uid)
                .get()
                .await()

            val user = document.toObject(User::class.java)
                ?: return AppResult.Error(AppError.UNKNOWN_AUTH_ERROR)

            AppResult.Success(user)

        } catch (e: FirebaseAuthInvalidUserException) {
            AppResult.Error(AppError.USER_NOT_FOUND, e)
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            AppResult.Error(AppError.WRONG_PASSWORD, e)
        } catch (e: Exception) {
            if (e.message?.contains("network", ignoreCase = true) == true) {
                AppResult.Error(AppError.NETWORK_ERROR, e)
            } else {
                AppResult.Error(AppError.UNKNOWN_AUTH_ERROR, e)
            }
        }
    }

    // ─── Session ──────────────────────────────────────────────────────────────

    /** Returns true if a user is already logged in. Use in MainActivity to skip onboarding. */
    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    /** Signs out the current user. */
    fun signOut() = auth.signOut()

    /** Returns the current logged-in user's UID, or null if not logged in. */
    fun currentUserId(): String? = auth.currentUser?.uid
}