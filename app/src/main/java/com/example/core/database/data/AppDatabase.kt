package com.example.core.database.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.BuildConfig

@Database(
    entities = [
        UserEntity::class,
        CartItem::class,
        OrderEntity::class
    ],
    version = 4,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun userDao(): UserDao
    abstract fun cartDao(): CartDao
    abstract fun orderDao(): OrderDao

    companion object {
        const val LINKED_DB_VERSION = BuildConfig.DATABASE_VERSION
    }
}
