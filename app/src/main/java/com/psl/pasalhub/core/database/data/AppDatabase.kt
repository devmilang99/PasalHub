package com.psl.pasalhub.core.database.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.psl.pasalhub.BuildConfig

@Database(
    entities = [
        UserEntity::class,
        CartEntity::class,
        OrderEntity::class,
        ProductEntity::class,
        FavoriteEntity::class,
        PointTransactionEntity::class,
        ProductRemoteKeys::class,
        OrderRemoteKeys::class
    ],
    version = 2,
    exportSchema = true,
    autoMigrations = []
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun cartDao(): CartDao
    abstract fun orderDao(): OrderDao
    abstract fun productDao(): ProductDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun pointDao(): PointDao
    abstract fun remoteKeysDao(): RemoteKeysDao

    companion object {
        const val LINKED_DB_VERSION = BuildConfig.DATABASE_VERSION
    }
}
