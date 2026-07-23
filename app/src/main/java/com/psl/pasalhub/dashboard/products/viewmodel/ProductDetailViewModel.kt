package com.psl.pasalhub.dashboard.products.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psl.pasalhub.core.application.domain.AppPreferencesRepository
import com.psl.pasalhub.core.database.data.CartEntity
import com.psl.pasalhub.core.database.data.OrderEntity
import com.psl.pasalhub.core.networking.remote.ProductDto
import com.psl.pasalhub.dashboard.products.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val repository: ProductRepository,
    private val profileRepository: com.psl.pasalhub.dashboard.profile.domain.ProfileRepository,
    private val appPrefs: AppPreferencesRepository
) : ViewModel() {

    private val _isDarkTheme = appPrefs.isDarkTheme()
    val isDarkTheme: Flow<Boolean> = _isDarkTheme

    val favoriteIds: Flow<Set<Int>> = appPrefs.getFavoriteIds()

    val globalNotificationEvent = appPrefs.getNotificationEvent()

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
        // Handled by appPrefs flow
    }

    fun loadFavorites(context: Context) {
        // Handled by appPrefs flow
    }

    fun toggleFavorite(context: Context, productId: Int) {
        viewModelScope.launch {
            appPrefs.toggleFavorite(productId)
        }
    }

    fun addToCart(product: ProductDto, quantity: Int) {
        viewModelScope.launch {
            val user = repository.getUser().first() ?: return@launch
            val cartItems = repository.getCartItems().first()
            val existing = cartItems.find { it.productId == product.id && it.userId == user.id }
            if (existing != null) {
                repository.updateCartItem(existing.copy(quantity = existing.quantity + quantity))
            } else {
                val sellerName = when (product.category.lowercase()) {
                    "electronics" -> "Tech Gear Hub"
                    "jewelery" -> "Elegance Gems"
                    "men's clothing", "women's clothing", "clothing" -> "Fashion Central"
                    else -> "${product.category.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }} Boutique"
                }
                repository.addToCart(
                    CartEntity(
                        userId = user.id,
                        productId = product.id,
                        title = product.title,
                        price = product.price,
                        description = product.description,
                        category = product.category,
                        image = product.image,
                        quantity = quantity,
                        seller = sellerName
                    )
                )
            }
            appPrefs.emitNotification("Successfully added $quantity x ${product.title} to your cart!")
        }
    }

    fun placeDirectOrder(
        context: Context,
        product: ProductDto,
        quantity: Int,
        finalTotal: Double,
        paymentMethod: String,
        appliedVoucher: String
    ) {
        viewModelScope.launch {
            val user = repository.getUser().first()
            val userAddress = user?.address ?: "Default Address, New York"
            val order = OrderEntity(
                totalAmount = finalTotal,
                itemsSummary = "${product.title}|${product.image} x$quantity (Direct Buy via $paymentMethod${if (appliedVoucher.isNotEmpty()) " [$appliedVoucher]" else ""})",
                status = "Placing",
                quantity = quantity,
                price = product.price,
                address = userAddress,
                seller = "${product.category.replaceFirstChar { it.uppercase() }} Luxury Direct",
                date = System.currentTimeMillis()
            )
            val orderId = repository.placeOrder(order, product.id)
            repository.scheduleOrderTracking(orderId)
            appPrefs.emitNotification("Order placed successfully!")

            // Rewards simulation
            user?.let { u ->
                val pointsReward = (finalTotal / 10).toInt().coerceAtLeast(50)
                profileRepository.addPoints(u.id, pointsReward, "Reward for Order #${orderId}")
            }
        }
    }
}
