package com.psl.pasalhub.dashboard.products.repository

import android.content.Context
import androidx.work.*
import com.psl.pasalhub.core.database.data.CartDao
import com.psl.pasalhub.core.database.data.CartItem
import com.psl.pasalhub.core.database.data.OrderDao
import com.psl.pasalhub.core.database.data.OrderEntity
import com.psl.pasalhub.core.database.data.ProductDao
import com.psl.pasalhub.core.database.data.ProductEntity
import com.psl.pasalhub.core.database.data.UserDao
import com.psl.pasalhub.core.database.data.UserEntity
import com.psl.pasalhub.core.networking.remote.ProductDto
import com.psl.pasalhub.dashboard.cart.sync.CartSyncWorker
import com.psl.pasalhub.dashboard.order.sync.OrderSyncWorker
import com.psl.pasalhub.dashboard.order.worker.OrderTrackingWorker
import com.psl.pasalhub.dashboard.products.sync.SupabaseSyncWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

sealed interface Resource<out T> {
    object Loading : Resource<Nothing>
    data class Success<out T>(val data: T) : Resource<T>
    data class Error(val message: String) : Resource<Nothing>
}

@Singleton
class ProductRepository @Inject constructor(
    private val userDao: UserDao,
    private val cartDao: CartDao,
    private val orderDao: OrderDao,
    private val productDao: ProductDao,
    @ApplicationContext private val context: Context
) {
    fun getProducts(): Flow<Resource<List<ProductDto>>> = flow {
        emit(Resource.Loading)
        productDao.getAllProducts().collect { entities ->
            if (entities.isEmpty()) {
                syncProducts()
                emit(Resource.Loading)
            } else {
                emit(Resource.Success(entities.map { it.toDto() }))
            }
        }
    }

    fun getProductsByCategory(category: String): Flow<Resource<List<ProductDto>>> = flow {
        emit(Resource.Loading)
        val normalizedCategory = normalizeCategory(category)
        productDao.getProductsByCategory(normalizedCategory).collect { entities ->
            emit(Resource.Success(entities.map { it.toDto() }))
        }
    }

    private fun syncProducts() {
        val syncRequest = OneTimeWorkRequestBuilder<SupabaseSyncWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "supabase_product_sync",
            ExistingWorkPolicy.KEEP,
            syncRequest
        )
    }

    private fun ProductEntity.toDto() = ProductDto(
        id = id,
        title = title,
        price = price,
        description = description ?: "",
        category = category ?: "General",
        image = image ?: ""
    )

    private fun normalizeCategory(category: String): String {
        return when (category.lowercase()) {
            "men's clothing", "mens clothing", "men clothing", "shirts", "clothing" -> "clothing"
            "women's clothing", "womens clothing", "women clothing", "dresses", "fashion" -> "clothing"
            "jewelery", "jewelry", "rings", "necklaces" -> "jewelery"
            "electronics", "gadgets", "tech" -> "electronics"
            "home", "appliances", "home_appliances", "kitchen" -> "home_appliances"
            "footwear", "shoes", "sneakers", "boots" -> "footwear"
            else -> category
        }
    }

    suspend fun getProductById(id: Int): ProductDto? {
        return productDao.getProductById(id)?.toDto()
    }

    // Cart operations
    fun getCartItems(): Flow<List<CartItem>> = cartDao.getCartItems()

    suspend fun addToCart(item: CartItem) {
        cartDao.addToCart(item)
        scheduleCartSync()
    }

    suspend fun removeFromCart(productId: Int) {
        cartDao.getCartItems().first().find { it.productId == productId }?.let {
            cartDao.deleteCartItem(it)
        }
        scheduleCartSync()
    }

    suspend fun updateCartItem(item: CartItem) {
        cartDao.updateCartItem(item)
        scheduleCartSync()
    }

    suspend fun clearCart() {
        cartDao.clearCart()
        scheduleCartSync()
    }

    // Order operations
    fun getOrders(): Flow<List<OrderEntity>> = orderDao.getOrders()

    suspend fun placeOrder(order: OrderEntity): Int {
        val id = orderDao.insertOrder(order)
        cartDao.clearCart()
        scheduleOrderSync()
        scheduleCartSync()
        return id.toInt()
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

    fun scheduleOrderTracking(orderId: Int) {
        val trackingRequest = OneTimeWorkRequestBuilder<OrderTrackingWorker>()
            .setInputData(workDataOf("order_id" to orderId))
            .addTag("order_tracking_$orderId")
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "order_tracking_$orderId",
            ExistingWorkPolicy.REPLACE,
            trackingRequest
        )
    }

    suspend fun updateOrder(order: OrderEntity) {
        orderDao.updateOrder(order)
    }

    // User operations
    fun getUser(): Flow<UserEntity?> = userDao.getUser()
    suspend fun clearUser() = userDao.clearUser()
}
