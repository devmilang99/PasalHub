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
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        // Use a secure key for production. For this example, we use a fixed key.
        // In a real app, generate this key and store it in the Android Keystore.
        val passphrase = "pasalhub-secure-key-2026".toByteArray()
        val factory = SupportOpenHelperFactory(passphrase)

        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "pasalhub_db"
        )
            .openHelperFactory(factory)
            .fallbackToDestructiveMigration(true)
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
