package com.example.dashboard.order.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dashboard.order.domain.OrderRepository
import com.example.core.database.data.OrderEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class OrderViewModel(
    private val repository: OrderRepository
) : ViewModel() {

    val ordersState: StateFlow<List<OrderEntity>> = repository.getOrders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isDarkTheme: StateFlow<Boolean> = repository.isDarkTheme()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun updateOrderStatus(orderId: Int, status: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, status)
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
