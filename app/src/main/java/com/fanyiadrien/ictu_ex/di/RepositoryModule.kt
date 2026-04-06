package com.fanyiadrien.ictu_ex.di

import com.fanyiadrien.ictu_ex.data.repository.CartRepository
import com.fanyiadrien.ictu_ex.data.repository.ListingRepository
import com.fanyiadrien.ictu_ex.data.repository.NotificationRepository
import com.fanyiadrien.ictu_ex.data.repository.UserRepository
import com.fanyiadrien.ictu_ex.data.repository.WishlistRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.components.SingletonComponent
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideCartRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore,
        notificationRepository: NotificationRepository
    ): CartRepository = CartRepository(auth, firestore, notificationRepository)

    @Provides
    @Singleton
    fun provideUserRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): UserRepository = UserRepository(auth, firestore)

    @Provides
    @Singleton
    fun provideListingRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): ListingRepository = ListingRepository(auth, firestore)

    @Provides
    @Singleton
    fun provideNotificationRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): NotificationRepository = NotificationRepository(auth, firestore)

    @Provides
    @Singleton
    fun provideWishlistRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): WishlistRepository = WishlistRepository(auth, firestore)
}