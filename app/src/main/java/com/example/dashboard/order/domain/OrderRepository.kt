package com.example.dashboard.order.domain

import com.example.core.database.data.OrderEntity
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    fun getOrders(): Flow<List<OrderEntity>>
    fun isDarkTheme(): Flow<Boolean>
    suspend fun updateOrderStatus(orderId: Int, status: String)
    suspend fun updateOrderProgress(orderId: Int, progress: Int)
    suspend fun cancelOrder(orderId: Int, reason: String)
    suspend fun completeOrder(orderId: Int, rating: Int, review: String)
    suspend fun placeOrder(order: OrderEntity)
}
