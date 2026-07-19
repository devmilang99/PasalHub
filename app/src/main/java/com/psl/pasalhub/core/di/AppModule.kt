package com.psl.pasalhub.core.di

import android.content.Context
import com.psl.pasalhub.ai.data.GeminiSearchRouter
import com.psl.pasalhub.core.application.utils.NotificationHelper
import com.psl.pasalhub.core.database.data.CartDao
import com.psl.pasalhub.core.database.data.OrderDao
import com.psl.pasalhub.core.database.data.ProductDao
import com.psl.pasalhub.core.database.data.UserDao
import com.psl.pasalhub.dashboard.products.repository.ProductRepository
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
        userDao: UserDao,
        cartDao: CartDao,
        orderDao: OrderDao,
        productDao: ProductDao,
        @ApplicationContext context: Context
    ): ProductRepository {
        return ProductRepository(userDao, cartDao, orderDao, productDao, context)
    }

    @Provides
    @Singleton
    fun provideGeminiSearchRouter(): GeminiSearchRouter {
        return GeminiSearchRouter()
    }

    @Provides
    @Singleton
    fun provideNotificationHelper(@ApplicationContext context: Context): NotificationHelper {
        return NotificationHelper(context)
    }
}
