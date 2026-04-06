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

    suspend fun uploadImage(
        context: Context,
        imageUri: Uri,
        folder: String = "listings"
    ): AppResult<String> = suspendCancellableCoroutine { continuation ->

        // Check file size before uploading (5 MB limit)
        val fileSizeBytes = try {
            context.contentResolver.openFileDescriptor(imageUri, "r")?.use { it.statSize } ?: 0L
        } catch (e: Exception) {
            0L
        }

        if (fileSizeBytes > 5 * 1024 * 1024L) {
            if (continuation.isActive) continuation.resume(AppResult.Error(AppError.IMAGE_TOO_LARGE))
            return@suspendCancellableCoroutine
        }

        val requestId = MediaManager.get()
            .upload(imageUri)
            .option("folder", folder)
            .option("resource_type", "image")
            .callback(object : UploadCallback {

                override fun onStart(requestId: String) {}

                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    if (!continuation.isActive) return
                    val secureUrl = resultData["secure_url"] as? String
                    if (secureUrl != null) {
                        continuation.resume(AppResult.Success(secureUrl))
                    } else {
                        continuation.resume(AppResult.Error(AppError.IMAGE_UPLOAD_FAILED))
                    }
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    if (!continuation.isActive) return
                    continuation.resume(
                        AppResult.Error(
                            message = AppError.IMAGE_UPLOAD_FAILED,
                            cause   = Exception(error.description)
                        )
                    )
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {
                    if (!continuation.isActive) return
                    continuation.resume(AppResult.Error(AppError.NETWORK_ERROR))
                }
            })
            .dispatch(context)

        // Cancel the Cloudinary upload if the coroutine is cancelled
        continuation.invokeOnCancellation {
            try { MediaManager.get().cancelRequest(requestId) } catch (_: Exception) {}
        }
    }
}
