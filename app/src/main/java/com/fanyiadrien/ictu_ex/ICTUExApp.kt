package com.fanyiadrien.ictu_ex

import android.app.Application
import com.cloudinary.android.MediaManager
import dagger.hilt.android.HiltAndroidApp
import com.fanyiadrien.ictu_ex.BuildConfig

/**
 * Application class — required for Hilt dependency injection.
 */
@HiltAndroidApp
class IctuExApp : Application() {

    override fun onCreate() {
        super.onCreate()
        initCloudinary()
    }

    /**
     * Initialise Cloudinary once at app startup.
     */
    private fun initCloudinary() {
        val config: Map<String, String> = mapOf(
            "cloud_name" to BuildConfig.CLOUDINARY_CLOUD_NAME,
            "api_key" to BuildConfig.CLOUDINARY_API_KEY,
            "api_secret" to BuildConfig.CLOUDINARY_API_SECRET
        )

        try {
            MediaManager.init(this, config)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
