plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.googleDevtoolsKsp)
    alias(libs.plugins.googleServices)
    alias(libs.plugins.daggerHiltAndroid)
}

android {
    namespace = "com.fanyiadrien.ictu_ex"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.fanyiadrien.ictu_ex"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Cloudinary Configuration (Consider using gradle.properties for sensitive data)
        buildConfigField("String", "CLOUDINARY_CLOUD_NAME", "\"your_cloud_name\"")
        buildConfigField("String", "CLOUDINARY_API_KEY", "\"your_api_key\"")
        buildConfigField("String", "CLOUDINARY_API_SECRET", "\"your_api_secret\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.core.splashscreen)

    // ICTU-Ex Features
    implementation(libs.androidx.biometric)

    // Offline-First Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.androidx.fragment)
    ksp(libs.room.compiler) // Using KSP here

    // QR Code Scanning
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.google.barcode.scanning)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.logging)

    // Cloudinary SDK
    implementation(libs.cloudinary.android)

    // Coil (To display the Cloudinary URLs in your UI)
    implementation(libs.coil.compose)
    // Hilt Core
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler) // Use ksp instead of kapt for better performance
    // Hilt + Compose (Required for hiltViewModel())
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.material.icons.extended)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
