package com.example.dashboard.order.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.core.application.utils.NotificationHelper
import com.example.dashboard.order.domain.OrderRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlin.time.Duration.Companion.milliseconds

@HiltWorker
class OrderTrackingWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: OrderRepository,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val orderId = inputData.getInt("order_id", -1)
        if (orderId == -1) return Result.failure()

        val statusSequence = listOf("Placed", "Packaging", "Sent for Delivery", "Delivered", "Completed")
        val progressMapping = mapOf(
            "Placed" to 5..20,
            "Packaging" to 21..50,
            "Sent for Delivery" to 51..95,
            "Delivered" to 100..100
        )

        var lastReportedStatus = ""
        // Initialize progress from current state
        var currentProgress = repository.getOrders().first().find { it.orderId == orderId }?.progress ?: 0

        while (true) {
            val currentOrders = repository.getOrders().first()
            val order = currentOrders.find { it.orderId == orderId } ?: break
            
            if (order.status == "Cancelled" || order.status == "Completed") {
                notificationHelper.showOrderUpdateNotification(order.orderId, order.status, 100)
                break
            }

            if (order.status == "Placing") {
                val elapsedTime = System.currentTimeMillis() - order.date
                if (elapsedTime < 10000) { // 10 sec cancel window
                    delay(1000)
                    continue
                } else {
                    repository.updateOrderStatus(orderId, "Placed")
                    continue
                }
            }
            
            val currentStatus = order.status
            val range = progressMapping[currentStatus] ?: (0..0)
            
            if (currentProgress < range.first) {
                currentProgress = range.first
            }

            // Reporting logic
            if (currentStatus != lastReportedStatus) {
                notificationHelper.showOrderUpdateNotification(order.orderId, currentStatus, currentProgress)
                lastReportedStatus = currentStatus
            }

            if (currentStatus == "Delivered" || currentStatus == "Completed") {
                break
            }

            if (currentProgress < range.last) {
                currentProgress += 1 
                if (currentProgress > range.last) currentProgress = range.last
                repository.updateOrderProgress(order.orderId, currentProgress)
            } else {
                val nextIndex = statusSequence.indexOf(currentStatus) + 1
                if (nextIndex in statusSequence.indices) {
                    val nextStatus = statusSequence[nextIndex]
                    repository.updateOrderStatus(order.orderId, nextStatus)
                    repository.updateOrderProgress(order.orderId, progressMapping[nextStatus]?.first ?: 0)
                }
            }
            
            delay(750.milliseconds)
        }

        return Result.success()
    }
}
