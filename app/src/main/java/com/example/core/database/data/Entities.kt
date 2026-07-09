package com.example.core.database.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val email: String,
    val name: String,
    val dateOfBirth: String,
    val address: String,
    val isRemembered: Boolean = false,
    val isGoogleUser: Boolean = false,
    val profileImage: String? = null
)

@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey val productId: Int,
    val title: String,
    val price: Double,
    val description: String,
    val category: String,
    val image: String,
    val quantity: Int = 1
)

@Entity(tableName = "orders")
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
    val review: String? = null
)
