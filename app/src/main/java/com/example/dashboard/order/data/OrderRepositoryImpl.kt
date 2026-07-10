package com.example.dashboard.order.data

import android.content.Context
import com.example.core.application.domain.AppPreferencesRepository
import com.example.dashboard.order.domain.OrderRepository
import com.example.core.database.data.OrderDao
import com.example.core.database.data.OrderEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class OrderRepositoryImpl(
    private val orderDao: OrderDao,
    private val context: Context,
    private val appPrefs: AppPreferencesRepository
) : OrderRepository {

    private val prefs = context.getSharedPreferences("pasalhub_settings", Context.MODE_PRIVATE)

    override fun getOrders(): Flow<List<OrderEntity>> = orderDao.getOrders()

    override fun isDarkTheme(): Flow<Boolean> = appPrefs.isDarkTheme()

    override suspend fun updateOrderStatus(orderId: Int, status: String) {
        orderDao.getOrders().first().find { it.orderId == orderId }?.let { order ->
            orderDao.updateOrder(order.copy(status = status))
        }
    }

    override suspend fun updateOrderProgress(orderId: Int, progress: Int) {
        orderDao.getOrders().first().find { it.orderId == orderId }?.let { order ->
            orderDao.updateOrder(order.copy(progress = progress))
        }
    }

    override suspend fun cancelOrder(orderId: Int, reason: String) {
        orderDao.getOrders().first().find { it.orderId == orderId }?.let { order ->
            orderDao.updateOrder(order.copy(status = "Cancelled", cancelledReason = reason))
        }
    }

    override suspend fun completeOrder(orderId: Int, rating: Int, review: String) {
        orderDao.getOrders().first().find { it.orderId == orderId }?.let { order ->
            orderDao.updateOrder(order.copy(status = "Completed", rating = rating, review = review))
        }
    }

    override suspend fun placeOrder(order: OrderEntity) {
        orderDao.insertOrder(order)
    }
}
