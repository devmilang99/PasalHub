package com.psl.pasalhub.core.database.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "users")
@Serializable
data class UserEntity(
    @PrimaryKey val id: String, // Supabase UID
    val email: String,
    val name: String,
    val dateOfBirth: String,
    val address: String,
    val isRemembered: Boolean = false,
    val isGoogleUser: Boolean = false,
    val profileImage: String? = null
)

@Entity(tableName = "favorites")
@Serializable
data class FavoriteEntity(
    @PrimaryKey val productId: Int,
    val userId: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "cart_items")
@Serializable
data class CartItem(
    @PrimaryKey val productId: Int,
    val title: String,
    val price: Double,
    val description: String,
    val category: String,
    val image: String,
    val quantity: Int = 1,
    val seller: String = "Official Store",
    val isSynced: Boolean = true
)

@Entity(tableName = "orders")
@Serializable
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val orderId: Int = 0,
    val date: Long = System.currentTimeMillis(),
    val totalAmount: Double,
    val itemsSummary: String,
    val status: String = "Delivered",
    val quantity: Int = 1,
    val price: Double = 0.0,
    val address: String = "",
    val seller: String = "Pasal Hub",
    val cancelledReason: String? = null,
    val rating: Int = 0,
    val review: String? = null,
    val progress: Int = 0,
    val isPaused: Boolean = false,
    val isSynced: Boolean = true
)
