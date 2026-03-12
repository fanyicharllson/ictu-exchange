package com.fanyiadrien.ictu_ex.utils
/**
 * A sealed wrapper returned by every repository function.
 *
 * Every operation in this app returns AppResult<T> so the UI
 * always knows exactly what happened — success, a known error,
 * or something unexpected.
 *
 * Usage in ViewModel:
 *   when (val result = repository.signUp(...)) {
 *       is AppResult.Success -> navigate to Home
 *       is AppResult.Error   -> show result.message in UI
 *       is AppResult.Loading -> show spinner
 *   }
 */
sealed class AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>()
    data class Error(val message: String, val cause: Exception? = null) : AppResult<Nothing>()
    object Loading : AppResult<Nothing>()
}

/**
 * All user-facing error messages live here.
 * Never hardcode error strings in repositories or ViewModels.
 */
object AppError {
    // Auth errors
    const val INVALID_ICTU_EMAIL    = "Only @ictuniversity.edu.cm email addresses are allowed."
    const val EMAIL_ALREADY_IN_USE  = "This email is already registered. Please sign in."
    const val WRONG_PASSWORD        = "Incorrect password. Please try again."
    const val USER_NOT_FOUND        = "No account found with this email."
    const val WEAK_PASSWORD         = "Password must be at least 6 characters."
    const val NETWORK_ERROR         = "No internet connection. Please check your network."
    const val UNKNOWN_AUTH_ERROR    = "Something went wrong. Please try again."

    // Cloudinary errors
    const val IMAGE_UPLOAD_FAILED   = "Image upload failed. Please try again."
    const val IMAGE_TOO_LARGE       = "Image is too large. Please choose one under 5MB."

    // Firestore errors
    const val SAVE_FAILED           = "Could not save data. Please try again."
    const val FETCH_FAILED          = "Could not load data. Please try again."
}