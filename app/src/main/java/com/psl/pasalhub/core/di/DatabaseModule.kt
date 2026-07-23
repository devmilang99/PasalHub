package com.psl.pasalhub.core.di

import android.content.Context
import androidx.room.Room
import com.psl.pasalhub.core.database.data.AppDatabase
import com.psl.pasalhub.core.database.data.CartDao
import com.psl.pasalhub.core.database.data.FavoriteDao
import com.psl.pasalhub.core.database.data.OrderDao
import com.psl.pasalhub.core.database.data.PointDao
import com.psl.pasalhub.core.database.data.ProductDao
import com.psl.pasalhub.core.database.data.RemoteKeysDao
import com.psl.pasalhub.core.database.data.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "pasalhub_db"
        )
            .fallbackToDestructiveMigration(true) // Keep this for version 1 dev, will replace with false once stable
            .build()
    }

    @Provides
    fun provideUserDao(db: AppDatabase): UserDao = db.userDao()

    @Provides
    fun provideCartDao(db: AppDatabase): CartDao = db.cartDao()

    @Provides
    fun provideOrderDao(db: AppDatabase): OrderDao = db.orderDao()

    @Provides
    fun provideProductDao(db: AppDatabase): ProductDao = db.productDao()

    @Provides
    fun provideFavoriteDao(db: AppDatabase): FavoriteDao = db.favoriteDao()

    @Provides
    fun providePointDao(db: AppDatabase): PointDao = db.pointDao()

    @Provides
    fun provideRemoteKeysDao(db: AppDatabase): RemoteKeysDao = db.remoteKeysDao()
}
