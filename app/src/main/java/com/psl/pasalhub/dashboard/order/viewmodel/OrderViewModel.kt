package com.psl.pasalhub.dashboard.order.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.psl.pasalhub.core.application.domain.AppPreferencesRepository
import com.psl.pasalhub.core.database.data.OrderEntity
import com.psl.pasalhub.dashboard.order.domain.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderViewModel @Inject constructor(
    private val repository: OrderRepository,
    private val appPrefs: AppPreferencesRepository
) : ViewModel() {

    val ordersState: StateFlow<List<OrderEntity>> = repository.getOrders()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val recentOrdersPaged: Flow<PagingData<OrderEntity>> = repository.getOrdersPaged(
        listOf("Placing", "Placed", "Packaging", "Sent for Delivery")
    ).cachedIn(viewModelScope)

    val completedOrdersPaged: Flow<PagingData<OrderEntity>> = repository.getOrdersPaged(
        listOf("Delivered", "Completed")
    ).cachedIn(viewModelScope)

    val cancelledOrdersPaged: Flow<PagingData<OrderEntity>> = repository.getOrdersPaged(
        listOf("Cancelled")
    ).cachedIn(viewModelScope)

    val ordersPaged: Flow<PagingData<OrderEntity>> = repository.getOrdersPaged()
        .cachedIn(viewModelScope)

    val isDarkTheme: StateFlow<Boolean> = repository.isDarkTheme()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    init {
        // Resume simulation for any transitional orders
        viewModelScope.launch {
            repository.getOrders().first().forEach { order ->
                if (order.status in listOf("Placing", "Placed", "Packaging", "Sent for Delivery")) {
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

    fun setOrderPause(orderId: Int, isPaused: Boolean) {
        viewModelScope.launch {
            repository.setOrderPause(orderId, isPaused)
        }
    }

    fun completeOrder(orderId: Int, rating: Int, review: String) {
        viewModelScope.launch {
            repository.completeOrder(orderId, rating, review)
        }
    }

    fun emitNotification(message: String) {
        viewModelScope.launch {
            appPrefs.emitNotification(message)
        }
    }
}
