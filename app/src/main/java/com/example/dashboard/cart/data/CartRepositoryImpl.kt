package com.example.dashboard.cart.data

import android.content.Context
import com.example.dashboard.cart.domain.CartRepository
import com.example.core.database.data.*
import com.example.dashboard.products.repository.ProductRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class CartRepositoryImpl @Inject constructor(
    private val productRepository: ProductRepository,
    private val userDao: UserDao,
    private val cartDao: CartDao,
    private val orderDao: OrderDao,
    @ApplicationContext private val context: Context
) : CartRepository {

    private val prefs = context.getSharedPreferences("pasalhub_settings", Context.MODE_PRIVATE)

    override fun getCartItems(): Flow<List<CartItem>> = cartDao.getCartItems()

    override fun getUser(): Flow<UserEntity?> = userDao.getUser()

    override fun isDarkTheme(): Flow<Boolean> {
        return MutableStateFlow(prefs.getBoolean("dark_theme", true))
    }

    override suspend fun increaseQuantity(item: CartItem) {
        cartDao.updateCartItem(item.copy(quantity = item.quantity + 1))
    }

    override suspend fun decreaseQuantity(item: CartItem) {
        if (item.quantity > 1) {
            cartDao.updateCartItem(item.copy(quantity = item.quantity - 1))
        } else {
            cartDao.deleteCartItem(item)
        }
    }

    override suspend fun deleteCartItem(item: CartItem) {
        cartDao.deleteCartItem(item)
    }

    override suspend fun deleteMultipleCartItems(items: List<CartItem>) {
        items.forEach { cartDao.deleteCartItem(it) }
    }

    override suspend fun checkout(selectedItems: List<CartItem>, finalTotal: Double, paymentMethod: String, appliedVoucher: String) {
        if (selectedItems.isNotEmpty()) {
            val user = userDao.getUser().first()
            val summary = selectedItems.joinToString { "${it.title}|${it.image} x${it.quantity}" }
            val order = OrderEntity(
                totalAmount = finalTotal,
                itemsSummary = summary,
                status = "Placing",
                quantity = selectedItems.sumOf { it.quantity },
                price = selectedItems.firstOrNull()?.price ?: 0.0,
                address = user?.address ?: "Default Address, New York",
                seller = "Pasal Hub",
                date = System.currentTimeMillis()
            )
            val orderId = orderDao.insertOrder(order).toInt()
            productRepository.scheduleOrderTracking(orderId)

            selectedItems.forEach { cartDao.deleteCartItem(it) }
            
            // Note: points logic could also be here or in VM
        }
    }
}
