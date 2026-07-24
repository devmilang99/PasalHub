package com.psl.pasalhub.core.database.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "product_remote_keys")
data class ProductRemoteKeys(
    @PrimaryKey val productId: Int,
    val prevKey: Int?,
    val nextKey: Int?
)

@Entity(tableName = "order_remote_keys")
data class OrderRemoteKeys(
    @PrimaryKey val orderId: Int,
    val prevKey: Int?,
    val nextKey: Int?
)
