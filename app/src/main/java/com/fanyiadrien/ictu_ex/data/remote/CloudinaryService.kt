package com.fanyiadrien.ictu_ex.data.remote

import android.content.Context
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.fanyiadrien.ictu_ex.utils.AppError
import com.fanyiadrien.ictu_ex.utils.AppResult
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class CloudinaryService @Inject constructor() {

    /**
     * Uploads an image to Cloudinary and returns the secure URL.
     *
     * @param context  Android context (needed by Cloudinary SDK)
     * @param imageUri URI of the image picked from gallery
     * @param folder   Cloudinary folder to organise uploads (e.g. "listings", "profiles")
     *
     * Usage:
     *   val result = cloudinaryService.uploadImage(context, uri, "listings")
     *   when (result) {
     *       is AppResult.Success -> saveUrlToFirestore(result.data)
     *       is AppResult.Error   -> showError(result.message)
     *   }
     */
    suspend fun uploadImage(
        context: Context,
        imageUri: Uri,
        folder: String = "listings"
    ): AppResult<String> = suspendCancellableCoroutine { continuation ->

        // Check file size before uploading (5MB limit)
        val fileSizeBytes = context.contentResolver
            .openFileDescriptor(imageUri, "r")?.statSize ?: 0L
        val fiveMbInBytes = 5 * 1024 * 1024L

        if (fileSizeBytes > fiveMbInBytes) {
            continuation.resume(AppResult.Error(AppError.IMAGE_TOO_LARGE))
            return@suspendCancellableCoroutine
        }

        MediaManager.get()
            .upload(imageUri)
            .option("folder", folder)
            .option("resource_type", "image")
            .callback(object : UploadCallback {

                override fun onStart(requestId: String) { /* upload started */ }

                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                    // Optional: emit progress if you want a progress bar
                }

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val secureUrl = resultData["secure_url"] as? String
                    if (secureUrl != null) {
                        continuation.resume(AppResult.Success(secureUrl))
                    } else {
                        continuation.resume(AppResult.Error(AppError.IMAGE_UPLOAD_FAILED))
                    }
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    continuation.resume(
                        AppResult.Error(
                            message = AppError.IMAGE_UPLOAD_FAILED,
                            cause   = Exception(error.description)
                        )
                    )
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {
                    continuation.resume(AppResult.Error(AppError.NETWORK_ERROR))
                }
            })
            .dispatch(context)
    }
}