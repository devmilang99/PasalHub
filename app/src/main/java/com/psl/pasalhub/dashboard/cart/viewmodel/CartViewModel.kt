package com.psl.pasalhub.dashboard.cart.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psl.pasalhub.core.database.data.CartEntity
import com.psl.pasalhub.core.database.data.UserEntity
import com.psl.pasalhub.core.sync.SyncManager
import com.psl.pasalhub.core.sync.SyncType
import com.psl.pasalhub.dashboard.cart.domain.CartRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val repository: CartRepository,
    private val syncManager: SyncManager
) : ViewModel() {

    init {
        // Trigger immediate sync on startup to fetch items from other devices
        syncCart()
    }

    val cartItems: StateFlow<List<CartEntity>> = repository.getCartItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isSyncing: StateFlow<Boolean> = syncManager.isCartSyncing
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun syncCart() {
        viewModelScope.launch {
            syncManager.triggerSync(SyncType.CART, fetch = true)
        }
    }

    var selectedIds: Set<String> by mutableStateOf(emptySet())
        private set

    fun updateSelectedItems(ids: Set<String>) {
        selectedIds = ids
    }

    val currentUser: StateFlow<UserEntity?> = repository.getUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isDarkTheme: StateFlow<Boolean> = repository.isDarkTheme()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun increaseQuantity(item: CartEntity) {
        viewModelScope.launch {
            repository.increaseQuantity(item)
        }
    }

    fun decreaseQuantity(item: CartEntity) {
        viewModelScope.launch {
            repository.decreaseQuantity(item)
        }
    }

    fun deleteCartItem(item: CartEntity) {
        viewModelScope.launch {
            repository.deleteCartItem(item)
        }
    }

    fun deleteMultipleCartItems(items: List<CartEntity>) {
        viewModelScope.launch {
            repository.deleteMultipleCartItems(items)
        }
    }

    fun checkoutSelected(
        context: Context,
        selectedItems: List<CartEntity>,
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
