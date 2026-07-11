package com.example.dashboard.products.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.application.domain.AppPreferencesRepository
import com.example.core.database.data.CartItem
import com.example.core.database.data.OrderEntity
import com.example.core.networking.remote.ProductDto
import com.example.dashboard.products.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.core.content.edit
import javax.inject.Inject

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val repository: ProductRepository,
    private val appPrefs: AppPreferencesRepository
) : ViewModel() {

    private val _favoriteIds = MutableStateFlow<Set<Int>>(emptySet())
    val favoriteIds: StateFlow<Set<Int>> = _favoriteIds.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    // Flow for notifications triggered from this screen
    val notificationEvent = MutableSharedFlow<String>(extraBufferCapacity = 1)

    init {
        viewModelScope.launch {
            notificationEvent.collect { message ->
                appPrefs.emitNotification(message)
            }
        }
    }

    fun loadSettings(context: Context) {
        val prefs = context.getSharedPreferences("pasalhub_settings", Context.MODE_PRIVATE)
        _isDarkTheme.value = prefs.getBoolean("dark_theme", true)
    }

    fun loadFavorites(context: Context) {
        val prefs = context.getSharedPreferences("pasalhub_favorites", Context.MODE_PRIVATE)
        val favStrings = prefs.getStringSet("fav_set", emptySet()) ?: emptySet()
        _favoriteIds.value = favStrings.mapNotNull { it.toIntOrNull() }.toSet()
    }

    fun toggleFavorite(context: Context, productId: Int) {
        val prefs = context.getSharedPreferences("pasalhub_favorites", Context.MODE_PRIVATE)
        val current = _favoriteIds.value.toMutableSet()
        if (current.contains(productId)) {
            current.remove(productId)
        } else {
            current.add(productId)
        }
        _favoriteIds.value = current
        prefs.edit { putStringSet("fav_set", current.map { it.toString() }.toSet()) }
    }

    fun addToCart(product: ProductDto) {
        viewModelScope.launch {
            val cartItems = repository.getCartItems().first()
            val existing = cartItems.find { it.productId == product.id }
            if (existing != null) {
                repository.updateCartItem(existing.copy(quantity = existing.quantity + 1))
            } else {
                repository.addToCart(
                    CartItem(
                        productId = product.id,
                        title = product.title,
                        price = product.price,
                        description = product.description,
                        category = product.category,
                        image = product.image,
                        quantity = 1
                    )
                )
            }
            appPrefs.emitNotification("Successfully added ${product.title} to your cart!")
        }
    }

    fun placeDirectOrder(context: Context, product: ProductDto, finalTotal: Double, paymentMethod: String, appliedVoucher: String) {
        viewModelScope.launch {
            val user = repository.getUser().first()
            val userAddress = user?.address ?: "Default Address, New York"
            val order = OrderEntity(
                totalAmount = finalTotal,
                itemsSummary = "${product.title}|${product.image} x1 (Direct Buy via $paymentMethod${if (appliedVoucher.isNotEmpty()) " [$appliedVoucher]" else ""})",
                status = "Placing",
                quantity = 1,
                price = product.price,
                address = userAddress,
                seller = "${product.category.replaceFirstChar { it.uppercase() }} Luxury Direct",
                date = System.currentTimeMillis()
            )
            val orderId = repository.placeOrder(order)
            repository.scheduleOrderTracking(orderId)

            // Rewards simulation
            val email = user?.email ?: "guest"
            val pointsReward = (finalTotal / 10).toInt().coerceAtLeast(50)
            val pointsPrefs = context.getSharedPreferences("pasalhub_points", Context.MODE_PRIVATE)
            val currentPoints = pointsPrefs.getInt("pts_$email", 250)
            pointsPrefs.edit().putInt("pts_$email", currentPoints + pointsReward).apply()
        }
    }
}