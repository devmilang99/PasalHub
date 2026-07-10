package com.example.dashboard.order.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.application.domain.AppPreferencesRepository
import com.example.core.application.utils.NotificationHelper
import com.example.dashboard.order.domain.OrderRepository
import com.example.core.database.data.OrderEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class OrderViewModel(
    private val repository: OrderRepository,
    private val appPrefs: AppPreferencesRepository,
    private val notificationHelper: NotificationHelper? = null
) : ViewModel() {

    val ordersState: StateFlow<List<OrderEntity>> = repository.getOrders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isDarkTheme: StateFlow<Boolean> = repository.isDarkTheme()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    init {
        // Resume simulation for any transitional orders
        viewModelScope.launch {
            repository.getOrders().first().forEach { order ->
                if (order.status in listOf("Placed", "Packaging", "Sent for Delivery")) {
                    startOrderTrackingSimulation(order.orderId)
                }
            }
        }
    }

    fun startOrderTrackingSimulation(orderId: Int) {
        viewModelScope.launch {
            val statusSequence = listOf("Placed", "Packaging", "Sent for Delivery", "Delivered", "Completed")
            val progressMapping = mapOf(
                "Placed" to 5..20,
                "Packaging" to 21..50,
                "Sent for Delivery" to 51..95,
                "Delivered" to 100..100
            )

            var lastReportedStatus = ""
            var currentProgress = repository.getOrders().first().find { it.orderId == orderId }?.progress ?: 0

            while (true) {
                val currentOrders = repository.getOrders().first()
                val order = currentOrders.find { it.orderId == orderId } ?: break
                
                if (order.status == "Cancelled" || order.status == "Completed") {
                    notificationHelper?.showOrderUpdateNotification(order.orderId, order.status, 100)
                    break
                }
                
                val currentStatus = order.status
                val range = progressMapping[currentStatus] ?: (0..0)
                
                if (currentProgress < range.first) {
                    currentProgress = range.first
                }

                if (currentStatus != "Delivered") {
                    if (currentProgress < range.last) {
                        currentProgress += 1 // Increment by 1 for 60s total (100 * 600ms = 60s)
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
                } else {
                    // Delivered
                    currentProgress = 100
                    repository.updateOrderProgress(order.orderId, 100)
                    notificationHelper?.showOrderUpdateNotification(order.orderId, currentStatus, 100)
                    break 
                }

                if (currentStatus != lastReportedStatus || currentProgress % 10 == 0 || currentProgress == range.last) {
                    notificationHelper?.showOrderUpdateNotification(order.orderId, currentStatus, currentProgress)
                    
                    if (currentStatus != lastReportedStatus) {
                        val message = when (currentStatus) {
                            "Packaging" -> "Your order #ORD-${1000 + order.orderId} is being packed!"
                            "Sent for Delivery" -> "Order #ORD-${1000 + order.orderId} is on its way!"
                            "Delivered" -> "Order #ORD-${1000 + order.orderId} has been delivered!"
                            else -> "Order #ORD-${1000 + order.orderId} status: $currentStatus"
                        }
                        appPrefs.emitNotification(message)
                        lastReportedStatus = currentStatus
                    }
                }
                
                if (currentStatus == "Delivered") break
                
                kotlinx.coroutines.delay(600.milliseconds)
            }
        }
    }

    fun updateOrderStatus(orderId: Int, status: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, status)
            if (status == "Placed") {
                startOrderTrackingSimulation(orderId)
            }
        }
    }

    fun placeOrder(order: OrderEntity) {
        viewModelScope.launch {
            repository.placeOrder(order)
            appPrefs.emitNotification("Your order #ORD-${1000 + order.orderId} has been placed successfully!")
            if (order.status in listOf("Placed", "Packaging", "Sent for Delivery")) {
                startOrderTrackingSimulation(order.orderId)
            }
        }
    }

    fun cancelOrder(orderId: Int, reason: String) {
        viewModelScope.launch {
            repository.cancelOrder(orderId, reason)
        }
    }

    fun completeOrder(orderId: Int, rating: Int, review: String) {
        viewModelScope.launch {
            repository.completeOrder(orderId, rating, review)
        }
    }
}
