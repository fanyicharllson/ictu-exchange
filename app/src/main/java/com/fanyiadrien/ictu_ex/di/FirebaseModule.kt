package com.fanyiadrien.ictu_ex.di

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Provides Firebase instances as singletons.
 * Hilt injects these automatically into any repository that needs them.
 * You never need to call FirebaseAuth.getInstance() manually anywhere else.
 */
@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    private const val TAG = "ICTU_FirebaseModule"

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        Log.d(TAG, "Providing FirebaseAuth instance")
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore {
        Log.d(TAG, "Providing FirebaseFirestore instance")
        return FirebaseFirestore.getInstance()
    }
}