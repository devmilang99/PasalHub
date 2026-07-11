package com.example.core.di

import android.content.Context
import com.example.ai.data.GeminiSearchRouter
import com.example.core.application.utils.NotificationHelper
import com.example.core.database.data.CartDao
import com.example.core.database.data.OrderDao
import com.example.core.database.data.UserDao
import com.example.core.networking.remote.FakeStoreApi
import com.example.dashboard.products.repository.ProductRepository
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideProductRepository(
        api: FakeStoreApi,
        userDao: UserDao,
        cartDao: CartDao,
        orderDao: OrderDao,
        @ApplicationContext context: Context
    ): ProductRepository {
        return ProductRepository(api, userDao, cartDao, orderDao, context)
    }

    @Provides
    @Singleton
    fun provideGeminiSearchRouter(moshi: Moshi): GeminiSearchRouter {
        return GeminiSearchRouter(moshi)
    }

    @Provides
    @Singleton
    fun provideNotificationHelper(@ApplicationContext context: Context): NotificationHelper {
        return NotificationHelper(context)
    }
}
