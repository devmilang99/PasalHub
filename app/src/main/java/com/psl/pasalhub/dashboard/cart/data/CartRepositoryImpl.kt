package com.psl.pasalhub.dashboard.cart.data

import android.content.Context
import com.psl.pasalhub.core.application.domain.AppPreferencesRepository
import com.psl.pasalhub.core.database.data.CartDao
import com.psl.pasalhub.core.database.data.CartEntity
import com.psl.pasalhub.core.database.data.OrderDao
import com.psl.pasalhub.core.database.data.OrderEntity
import com.psl.pasalhub.core.database.data.UserDao
import com.psl.pasalhub.core.database.data.UserEntity
import com.psl.pasalhub.core.sync.SyncManager
import com.psl.pasalhub.core.sync.SyncType
import com.psl.pasalhub.dashboard.cart.domain.CartRepository
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
    private val syncManager: SyncManager,
    @ApplicationContext private val context: Context
) : CartRepository {

    override fun getCartItems(): Flow<List<CartEntity>> = cartDao.getCartItems()

    override fun getUser(): Flow<UserEntity?> = userDao.getUser()

    override fun isDarkTheme(): Flow<Boolean> = appPrefs.isDarkTheme()

    override suspend fun increaseQuantity(item: CartEntity) {
        cartDao.updateCartItem(item.copy(quantity = item.quantity + 1))
        syncManager.triggerSync(SyncType.CART, immediate = true, fetch = false)
    }

    override suspend fun decreaseQuantity(item: CartEntity) {
        if (item.quantity > 1) {
            cartDao.updateCartItem(item.copy(quantity = item.quantity - 1))
        } else {
            cartDao.deleteCartItem(item)
        }
        syncManager.triggerSync(SyncType.CART, immediate = true, fetch = false)
    }

    override suspend fun deleteCartItem(item: CartEntity) {
        cartDao.deleteCartItem(item)
        syncManager.triggerSync(SyncType.CART, immediate = true, fetch = false)
    }

    override suspend fun deleteMultipleCartItems(items: List<CartEntity>) {
        cartDao.deleteMultipleCartItems(items)
        syncManager.triggerSync(SyncType.CART, immediate = true, fetch = false)
    }

    override suspend fun checkout(
        selectedItems: List<CartEntity>,
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

            cartDao.deleteMultipleCartItems(selectedItems)

            syncManager.triggerSync(SyncType.CART, immediate = true, fetch = false)
            syncManager.triggerSync(SyncType.ORDERS, immediate = true, fetch = false)
        }
    }
}
