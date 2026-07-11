package com.example.dashboard.order.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.application.domain.AppPreferencesRepository
import com.example.dashboard.order.domain.OrderRepository
import com.example.core.database.data.OrderEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderViewModel @Inject constructor(
    private val repository: OrderRepository,
    private val appPrefs: AppPreferencesRepository
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
                    repository.scheduleOrderTracking(order.orderId)
                }
            }
        }
    }

    fun updateOrderStatus(orderId: Int, status: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, status)
            if (status == "Placed") {
                repository.scheduleOrderTracking(orderId)
            }
        }
    }

    fun placeOrder(order: OrderEntity) {
        viewModelScope.launch {
            val orderId = repository.placeOrder(order)
            appPrefs.emitNotification("Your order #ORD-${1000 + orderId} has been placed successfully!")
            if (order.status in listOf("Placed", "Packaging", "Sent for Delivery", "Placing")) {
                repository.scheduleOrderTracking(orderId)
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
