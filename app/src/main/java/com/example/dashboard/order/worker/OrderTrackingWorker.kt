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

        // Wait a bit to ensure DB write from placeOrder is finished
        delay(500.milliseconds)

        val statusSequence = listOf("Placed", "Packaging", "Sent for Delivery", "Delivered", "Completed")
        val progressMapping = mapOf(
            "Placed" to 11..25,
            "Packaging" to 26..55,
            "Sent for Delivery" to 56..95,
            "Delivered" to 100..100
        )

        var lastReportedStatus = "INITIAL_UNREPORTED"
        var currentProgress = 0

        while (true) {
            val currentOrders = repository.getOrders().first()
            val order = currentOrders.find { it.orderId == orderId } ?: break
            
            currentProgress = order.progress

            if (lastReportedStatus == "INITIAL_UNREPORTED") {
                notificationHelper.showOrderUpdateNotification(order.orderId, order.status, currentProgress, order.itemsSummary, order.seller, order.isPaused)
                lastReportedStatus = order.status
            }

            if (order.status == "Cancelled" || order.status == "Completed") {
                notificationHelper.showOrderUpdateNotification(order.orderId, order.status, 100, order.itemsSummary, order.seller, order.isPaused)
                break
            }

            if (order.status == "Placing") {
                if (order.isPaused) {
                    if (lastReportedStatus != "Paused") {
                        notificationHelper.showOrderUpdateNotification(order.orderId, order.status, currentProgress, order.itemsSummary, order.seller, true)
                        lastReportedStatus = "Paused"
                    }
                    delay(1000.milliseconds)
                    continue
                }

                if (currentProgress < 10) {
                    currentProgress += 1
                    repository.updateOrderProgress(orderId, currentProgress)
                    notificationHelper.showOrderUpdateNotification(order.orderId, order.status, currentProgress, order.itemsSummary, order.seller, false)
                    lastReportedStatus = order.status
                    delay(1000.milliseconds)
                    continue
                } else { // Window finished, transition to Placed
                    repository.updateOrderStatus(orderId, "Placed")
                    repository.updateOrderProgress(orderId, 11)
                    // Trigger notification immediately for the status change
                    notificationHelper.showOrderUpdateNotification(order.orderId, "Placed", 11, order.itemsSummary, order.seller, false)
                    lastReportedStatus = "Placed"
                    delay(800.milliseconds) // Delay to let DB settle and avoid rapid notification updates
                    continue
                }
            }

            if (order.isPaused) {
                if (lastReportedStatus != "Paused") {
                    notificationHelper.showOrderUpdateNotification(order.orderId, order.status, currentProgress, order.itemsSummary, order.seller, true)
                    lastReportedStatus = "Paused"
                }
                delay(1000.milliseconds)
                continue
            }

            val currentStatus = order.status
            val range = progressMapping[currentStatus] ?: (0..0)
            
            if (currentProgress < range.first) {
                currentProgress = range.first
            }

            // Reporting logic
            if (currentStatus != lastReportedStatus) {
                notificationHelper.showOrderUpdateNotification(order.orderId, currentStatus, currentProgress, order.itemsSummary, order.seller, false)
                lastReportedStatus = currentStatus
            }

            if (currentStatus == "Delivered") {
                // Keep it for a while then exit
                delay(2000.milliseconds)
                break
            }

            if (currentProgress < range.last) {
                currentProgress += 1 
                if (currentProgress > range.last) currentProgress = range.last
                repository.updateOrderProgress(order.orderId, currentProgress)
                // Update notification for live progress
                notificationHelper.showOrderUpdateNotification(order.orderId, currentStatus, currentProgress, order.itemsSummary, order.seller, false)
            } else {
                val nextIndex = statusSequence.indexOf(currentStatus) + 1
                if (nextIndex in statusSequence.indices) {
                    val nextStatus = statusSequence[nextIndex]
                    val nextProgress = progressMapping[nextStatus]?.first ?: 0
                    repository.updateOrderStatus(order.orderId, nextStatus)
                    repository.updateOrderProgress(order.orderId, nextProgress)
                    
                    // Trigger notification immediately for transition
                    notificationHelper.showOrderUpdateNotification(order.orderId, nextStatus, nextProgress, order.itemsSummary, order.seller, false)
                    lastReportedStatus = nextStatus
                    delay(800.milliseconds)
                }
            }
            
            delay(800.milliseconds)
        }

        return Result.success()
    }
}
