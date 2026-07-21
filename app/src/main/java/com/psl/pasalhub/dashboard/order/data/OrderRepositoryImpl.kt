package com.psl.pasalhub.dashboard.order.data

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.psl.pasalhub.core.application.domain.AppPreferencesRepository
import com.psl.pasalhub.core.database.data.OrderDao
import com.psl.pasalhub.core.database.data.OrderEntity
import com.psl.pasalhub.core.sync.SyncManager
import com.psl.pasalhub.core.sync.SyncType
import com.psl.pasalhub.dashboard.order.domain.OrderRepository
import com.psl.pasalhub.dashboard.order.worker.OrderTrackingWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class OrderRepositoryImpl @Inject constructor(
    private val orderDao: OrderDao,
    @ApplicationContext private val context: Context,
    private val appPrefs: AppPreferencesRepository,
    private val syncManager: SyncManager
) : OrderRepository {

    private val prefs = context.getSharedPreferences("pasalhub_settings", Context.MODE_PRIVATE)

    override fun getOrders(): Flow<List<OrderEntity>> = orderDao.getOrders()

    override fun isDarkTheme(): Flow<Boolean> = appPrefs.isDarkTheme()

    override suspend fun updateOrderStatus(orderId: Int, status: String) {
        orderDao.getOrders().first().find { it.orderId == orderId }?.let { order ->
            orderDao.updateOrder(order.copy(status = status))
            scheduleOrderSync()
        }
    }

    override suspend fun updateOrderProgress(orderId: Int, progress: Int) {
        orderDao.getOrders().first().find { it.orderId == orderId }?.let { order ->
            orderDao.updateOrder(order.copy(progress = progress))
            scheduleOrderSync()
        }
    }

    override suspend fun cancelOrder(orderId: Int, reason: String) {
        orderDao.getOrders().first().find { it.orderId == orderId }?.let { order ->
            orderDao.updateOrder(order.copy(status = "Cancelled", cancelledReason = reason))
            // Cancel any active tracking work
            WorkManager.getInstance(context).cancelUniqueWork("order_tracking_$orderId")
            scheduleOrderSync()
        }
    }

    override suspend fun completeOrder(orderId: Int, rating: Int, review: String) {
        orderDao.getOrders().first().find { it.orderId == orderId }?.let { order ->
            orderDao.updateOrder(order.copy(status = "Completed", rating = rating, review = review))
            scheduleOrderSync()
        }
    }

    override suspend fun placeOrder(order: OrderEntity): Int {
        val id = orderDao.insertOrder(order)
        scheduleOrderSync()
        return id.toInt()
    }

    private fun scheduleOrderSync() {
        syncManager.triggerSync(SyncType.ORDERS, immediate = true)
    }

    override suspend fun setOrderPause(orderId: Int, isPaused: Boolean) {
        orderDao.getOrders().first().find { it.orderId == orderId }?.let { order ->
            orderDao.updateOrder(order.copy(isPaused = isPaused))
        }
    }

    override suspend fun toggleOrderPause(orderId: Int) {
        orderDao.getOrders().first().find { it.orderId == orderId }?.let { order ->
            orderDao.updateOrder(order.copy(isPaused = !order.isPaused))
        }
    }

    override suspend fun resumeAllPausedOrders() {
        val allOrders = orderDao.getOrders().first()
        allOrders.filter { it.isPaused }.forEach { order ->
            orderDao.updateOrder(order.copy(isPaused = false))
        }
    }

    override fun scheduleOrderTracking(orderId: Int) {
        val trackingRequest = OneTimeWorkRequestBuilder<OrderTrackingWorker>()
            .setInputData(workDataOf("order_id" to orderId))
            .addTag("order_tracking_$orderId")
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "order_tracking_$orderId",
            ExistingWorkPolicy.REPLACE,
            trackingRequest
        )
    }
}
