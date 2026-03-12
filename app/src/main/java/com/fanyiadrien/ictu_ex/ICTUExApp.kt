package com.fanyiadrien.ictu_ex

import android.app.Application
import com.cloudinary.android.MediaManager
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class — required for Hilt dependency injection.
 *
 * SETUP: Register this in AndroidManifest.xml:
 *   <application
 *       android:name=".IctuExApp"
 *       ...>
 */
@HiltAndroidApp
class IctuExApp : Application() {

    override fun onCreate() {
        super.onCreate()
        initCloudinary()
    }

    /**
     * Initialise Cloudinary once at app startup.
     * Replace the values below with your actual Cloudinary credentials
     * from your Cloudinary dashboard → Settings → API Keys.
     *
     * ⚠️ In production: move these to a local.properties file
     *    and never commit your api_secret to Git.
     */
    private fun initCloudinary() {
        val config = mapOf(
            "cloud_name" to "YOUR_CLOUD_NAME",   // e.g. "ictu-ex"
            "api_key"    to "YOUR_API_KEY",
            "api_secret" to "YOUR_API_SECRET"
        )
        MediaManager.init(this, config)
    }
}