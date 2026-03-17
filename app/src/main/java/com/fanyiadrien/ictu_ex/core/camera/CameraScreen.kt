package com.fanyiadrien.ictu_ex.core.camera

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Camera
import androidx.compose.material.icons.rounded.Cameraswitch
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

private const val TAG = "CameraScreen"

/**
 * Full-screen camera screen using CameraX.
 *
 * Returns the captured image URI via [onImageCaptured].
 * Navigate here from PostItemScreen when user picks "Take Photo".
 *

 */
@Composable
fun CameraScreen(
    onImageCaptured: (Uri) -> Unit,
    onBack: () -> Unit
) {
    val context        = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    // Executor for image capture callbacks
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }

    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    var isCapturing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        onDispose { cameraExecutor.shutdown() }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {

        // ── CameraX Preview ───────────────────────────────────────────────
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }
                bindCamera(
                    context       = ctx,
                    previewView   = previewView,
                    lifecycleOwner = lifecycleOwner,
                    lensFacing    = lensFacing,
                    onImageCaptureReady = { capture -> imageCapture = capture }
                )
                previewView
            },
            update = { previewView ->
                // Re-bind when lens switches
                bindCamera(
                    context       = context,
                    previewView   = previewView,
                    lifecycleOwner = lifecycleOwner,
                    lensFacing    = lensFacing,
                    onImageCaptureReady = { capture -> imageCapture = capture }
                )
            },
            modifier = Modifier.fillMaxSize()
        )

        // ── Back button ───────────────────────────────────────────────────
        IconButton(
            onClick  = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.4f))
        ) {
            Icon(
                Icons.Rounded.ArrowBackIosNew,
                contentDescription = "Back",
                tint = Color.White
            )
        }

        // ── Error snackbar ────────────────────────────────────────────────
        errorMessage?.let { msg ->
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 72.dp, start = 24.dp, end = 24.dp),
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text     = msg,
                    color    = MaterialTheme.colorScheme.error,
                    style    = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        // ── Bottom controls: flip + capture ──────────────────────────────
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 48.dp, start = 32.dp, end = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            // Flip camera button
            IconButton(
                onClick  = {
                    lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK)
                        CameraSelector.LENS_FACING_FRONT
                    else
                        CameraSelector.LENS_FACING_BACK
                },
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
            ) {
                Icon(
                    Icons.Rounded.Cameraswitch,
                    contentDescription = "Flip camera",
                    tint     = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Capture button — outer ring + inner circle
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .border(3.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isCapturing) {
                    CircularProgressIndicator(
                        color    = Color.White,
                        modifier = Modifier.size(48.dp),
                        strokeWidth = 3.dp
                    )
                } else {
                    IconButton(
                        onClick  = {
                            isCapturing  = true
                            errorMessage = null
                            takePhoto(
                                context      = context,
                                imageCapture = imageCapture,
                                executor     = cameraExecutor,
                                onSuccess    = { uri ->
                                    isCapturing = false
                                    onImageCaptured(uri)
                                },
                                onError      = { msg ->
                                    isCapturing  = false
                                    errorMessage = msg
                                }
                            )
                        },
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    ) {
                        Icon(
                            Icons.Rounded.Camera,
                            contentDescription = "Capture",
                            tint     = Color.Black,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            // Spacer to balance the flip button on the left
            Spacer(modifier = Modifier.size(52.dp))
        }
    }
}

// ── CameraX binding helper ────────────────────────────────────────────────────

private fun bindCamera(
    context: Context,
    previewView: PreviewView,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    lensFacing: Int,
    onImageCaptureReady: (ImageCapture) -> Unit
) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    cameraProviderFuture.addListener({
        val cameraProvider = cameraProviderFuture.get()

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        val capture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()

        val selector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(lifecycleOwner, selector, preview, capture)
            onImageCaptureReady(capture)
        } catch (e: Exception) {
            // Camera bind failed — handled gracefully via errorMessage state
        }
    }, ContextCompat.getMainExecutor(context))
}

// ── Photo capture helper ──────────────────────────────────────────────────────

private fun takePhoto(
    context: Context,
    imageCapture: ImageCapture?,
    executor: ExecutorService,
    onSuccess: (Uri) -> Unit,
    onError: (String) -> Unit
) {
    Log.d(TAG, "takePhoto() called on thread: ${Thread.currentThread().name}")
    
    if (imageCapture == null) {
        Log.e(TAG, "❌ ImageCapture is null - camera not ready")
        onError("Camera not ready. Please try again.")
        return
    }

    try {
        // Save to app's cache directory — no storage permission needed
        val photoFile = File(
            context.cacheDir,
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date()) + ".jpg"
        )
        Log.d(TAG, "📸 Saving photo to: ${photoFile.absolutePath}")

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            executor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    Log.d(TAG, "✅ Photo saved on thread: ${Thread.currentThread().name}")
                    Log.d(TAG, "✅ Photo saved successfully: ${photoFile.absolutePath}")
                    val uri = Uri.fromFile(photoFile)
                    Log.d(TAG, "📤 Photo URI: $uri")
                    Log.d(TAG, "🔄 Switching to main thread before calling onSuccess...")
                    
                    // CRITICAL FIX: Run callback on main thread to avoid IllegalStateException
                    // Navigation operations MUST happen on the main thread
                    ContextCompat.getMainExecutor(context).execute {
                        Log.d(TAG, "✅ Now on main thread: ${Thread.currentThread().name}")
                        Log.d(TAG, "📤 Calling onSuccess with URI: $uri")
                        onSuccess(uri)
                    }
                }

                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "❌ Photo capture failed on thread: ${Thread.currentThread().name}", exc)
                    Log.e(TAG, "Error code: ${exc.imageCaptureError}, Message: ${exc.message}")
                    ContextCompat.getMainExecutor(context).execute {
                        onError("Failed to capture photo. Please try again.")
                    }
                }
            }
        )
    } catch (e: Exception) {
        Log.e(TAG, "❌ Exception in takePhoto()", e)
        onError("Camera error: ${e.message}")
    }
}