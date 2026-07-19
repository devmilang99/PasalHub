package com.psl.pasalhub.dashboard.cart.data

import android.content.Context
import androidx.work.*
import com.psl.pasalhub.core.application.domain.AppPreferencesRepository
import com.psl.pasalhub.dashboard.cart.domain.CartRepository
import com.psl.pasalhub.core.database.data.*
import com.psl.pasalhub.dashboard.cart.sync.CartSyncWorker
import com.psl.pasalhub.dashboard.order.sync.OrderSyncWorker
import com.psl.pasalhub.dashboard.products.repository.ProductRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class CartRepositoryImpl @Inject constructor(
    private val productRepository: ProductRepository,
    private val userDao: UserDao,
    private val cartDao: CartDao,
    private val orderDao: OrderDao,
    private val appPrefs: AppPreferencesRepository,
    @ApplicationContext private val context: Context
) : CartRepository {

    override fun getCartItems(): Flow<List<CartItem>> = cartDao.getCartItems()

    override fun getUser(): Flow<UserEntity?> = userDao.getUser()

    override fun isDarkTheme(): Flow<Boolean> = appPrefs.isDarkTheme()

    override suspend fun increaseQuantity(item: CartItem) {
        cartDao.updateCartItem(item.copy(quantity = item.quantity + 1))
        scheduleCartSync()
    }

    override suspend fun decreaseQuantity(item: CartItem) {
        if (item.quantity > 1) {
            cartDao.updateCartItem(item.copy(quantity = item.quantity - 1))
        } else {
            cartDao.deleteCartItem(item)
        }
        scheduleCartSync()
    }

    override suspend fun deleteCartItem(item: CartItem) {
        cartDao.deleteCartItem(item)
        scheduleCartSync()
    }

    override suspend fun deleteMultipleCartItems(items: List<CartItem>) {
        items.forEach { cartDao.deleteCartItem(it) }
        scheduleCartSync()
    }

    override suspend fun checkout(
        selectedItems: List<CartItem>,
        finalTotal: Double,
        paymentMethod: String,
        appliedVoucher: String
    ) {
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

            scheduleCartSync()
            scheduleOrderSync()
        }
    }

    private fun scheduleCartSync() {
        val syncRequest = OneTimeWorkRequestBuilder<CartSyncWorker>()
            .setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            )
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork("cart_sync", ExistingWorkPolicy.REPLACE, syncRequest)
    }

    private fun scheduleOrderSync() {
        val syncRequest = OneTimeWorkRequestBuilder<OrderSyncWorker>()
            .setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            )
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork("order_sync", ExistingWorkPolicy.REPLACE, syncRequest)
    }
}
