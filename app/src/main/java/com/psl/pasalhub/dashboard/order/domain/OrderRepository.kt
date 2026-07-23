package com.psl.pasalhub.dashboard.order.domain

import androidx.paging.PagingData
import com.psl.pasalhub.core.database.data.OrderEntity
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    fun getOrders(): Flow<List<OrderEntity>>
    fun getOrdersPaged(statuses: List<String>? = null): Flow<PagingData<OrderEntity>>
    fun isDarkTheme(): Flow<Boolean>
    suspend fun updateOrderStatus(orderId: Int, status: String)
    suspend fun updateOrderProgress(orderId: Int, progress: Int)
    suspend fun cancelOrder(orderId: Int, reason: String)
    suspend fun completeOrder(orderId: Int, rating: Int, review: String)
    suspend fun placeOrder(order: OrderEntity): Int
    fun scheduleOrderTracking(orderId: Int)
    suspend fun setOrderPause(orderId: Int, isPaused: Boolean)
    suspend fun toggleOrderPause(orderId: Int)
    suspend fun resumeAllPausedOrders()
}
