package com.psl.pasalhub.core.database.data

import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.psl.pasalhub.core.sync.TimestampSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Entity(tableName = "users")
@Serializable
@Immutable
data class UserEntity(
    @PrimaryKey val id: String, // Supabase UID
    val email: String,
    val name: String,
    val dateOfBirth: String,
    val address: String,
    val isRemembered: Boolean = false,
    val isGoogleUser: Boolean = false,
    val profileImage: String? = null,
    val isProfileComplete: Boolean = false,
    val hasSyncedCart: Boolean = false,
    val hasSyncedFavorites: Boolean = false,
    val isOnboardingDone: Boolean = false,
    val lastFullSyncTime: Long = 0L
)

@Entity(tableName = "favorites")
@Serializable
@Immutable
data class FavoriteEntity(
    @PrimaryKey val productId: Int,
    val userId: String,
    @ColumnInfo("added_At") @SerialName("added_At") @Serializable(with = TimestampSerializer::class)
    val addedAt: Long = System.currentTimeMillis(),
    @ColumnInfo("updated_At") @SerialName("updated_At") @Serializable(with = TimestampSerializer::class)
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "cart",
    indices = [
        Index(value = ["user_id"]),
        Index(value = ["user_id", "product_id"], unique = true)
    ]
)
@Serializable
@Immutable
data class CartEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "user_id") @SerialName("user_id") val userId: String,
    @ColumnInfo(name = "product_id") @SerialName("product_id") val productId: Int,
    val title: String,
    val price: Double,
    val description: String,
    val category: String,
    val image: String,
    val quantity: Int = 1,
    val seller: String = "Official Store",
    @ColumnInfo(name = "is_synced") @SerialName("is_synced") val isSynced: Boolean = true,
    @ColumnInfo("updated_at") @SerialName("updated_at") @Serializable(with = TimestampSerializer::class) val updatedAt: Long = System.currentTimeMillis(),
    @ColumnInfo("created_at") @SerialName("created_at") @Serializable(with = TimestampSerializer::class) val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "orders")
@Serializable
@Immutable
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) @SerialName("orderId") val orderId: Int = 0,
    @SerialName("date") @Serializable(with = TimestampSerializer::class) val date: Long = System.currentTimeMillis(),
    @SerialName("totalAmount") val totalAmount: Double,
    @SerialName("itemsSummary") val itemsSummary: String,
    @SerialName("status") val status: String = "Delivered",
    @SerialName("quantity") val quantity: Int = 1,
    @SerialName("price") val price: Double = 0.0,
    @SerialName("address") val address: String = "",
    @SerialName("seller") val seller: String = "Pasal Hub",
    @SerialName("cancelled_reason") val cancelledReason: String? = null,
    @SerialName("rating") val rating: Int = 0,
    @SerialName("review") val review: String? = null,
    @SerialName("progress") val progress: Int = 0,
    @SerialName("isPaused") val isPaused: Boolean = false,
    @SerialName("isSynced") val isSynced: Boolean = true,
    @ColumnInfo("updated_at") @SerialName("updated_at") @Serializable(with = TimestampSerializer::class) val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "point_transactions")
@Serializable
@Immutable
data class PointTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "user_id") val userId: String,
    val amount: Int,
    val reason: String,
    @ColumnInfo(name = "timestamp") @Serializable(with = TimestampSerializer::class)
    val timestamp: Long = System.currentTimeMillis()
)
