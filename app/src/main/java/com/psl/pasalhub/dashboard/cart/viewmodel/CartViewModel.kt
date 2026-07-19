package com.psl.pasalhub.dashboard.cart.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psl.pasalhub.dashboard.cart.domain.CartRepository
import com.psl.pasalhub.core.database.data.CartItem
import com.psl.pasalhub.core.database.data.UserEntity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val repository: CartRepository
) : ViewModel() {

    val cartItems: StateFlow<List<CartItem>> = repository.getCartItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var selectedItemIds: Set<Int> by mutableStateOf(emptySet())
        private set

    fun updateSelectedItems(ids: Set<Int>) {
        selectedItemIds = ids
    }

    val currentUser: StateFlow<UserEntity?> = repository.getUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isDarkTheme: StateFlow<Boolean> = repository.isDarkTheme()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun increaseQuantity(item: CartItem) {
        viewModelScope.launch {
            repository.increaseQuantity(item)
        }
    }

    fun decreaseQuantity(item: CartItem) {
        viewModelScope.launch {
            repository.decreaseQuantity(item)
        }
    }

    fun deleteCartItem(item: CartItem) {
        viewModelScope.launch {
            repository.deleteCartItem(item)
        }
    }

    fun deleteMultipleCartItems(items: List<CartItem>) {
        viewModelScope.launch {
            repository.deleteMultipleCartItems(items)
        }
    }

    fun checkoutSelected(
        context: Context,
        selectedItems: List<CartItem>,
        finalTotal: Double,
        paymentMethod: String,
        appliedVoucher: String
    ) {
        viewModelScope.launch {
            repository.checkout(selectedItems, finalTotal, paymentMethod, appliedVoucher)
        }
    }

    fun showNotification(message: String) {
        // Handle notification event if needed
    }
}
