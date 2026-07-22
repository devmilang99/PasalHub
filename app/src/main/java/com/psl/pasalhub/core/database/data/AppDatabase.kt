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
        FavoriteEntity::class
    ],
    version = BuildConfig.DATABASE_VERSION,
    exportSchema = true,
    autoMigrations = []
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun cartDao(): CartDao
    abstract fun orderDao(): OrderDao
    abstract fun productDao(): ProductDao
    abstract fun favoriteDao(): FavoriteDao

    companion object {
        const val LINKED_DB_VERSION = BuildConfig.DATABASE_VERSION
    }
}
