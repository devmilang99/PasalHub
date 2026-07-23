package com.psl.pasalhub.dashboard.products.repository

import android.content.Context
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.psl.pasalhub.core.application.domain.AppPreferencesRepository
import com.psl.pasalhub.core.database.data.AppDatabase
import com.psl.pasalhub.core.database.data.CartDao
import com.psl.pasalhub.core.database.data.CartEntity
import com.psl.pasalhub.core.database.data.OrderDao
import com.psl.pasalhub.core.database.data.OrderEntity
import com.psl.pasalhub.core.database.data.ProductDao
import com.psl.pasalhub.core.database.data.ProductEntity
import com.psl.pasalhub.core.database.data.UserDao
import com.psl.pasalhub.core.database.data.UserEntity
import com.psl.pasalhub.core.networking.remote.ProductDto
import com.psl.pasalhub.core.sync.SyncManager
import com.psl.pasalhub.core.sync.SyncType
import com.psl.pasalhub.dashboard.order.worker.OrderTrackingWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
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
    private val syncManager: SyncManager,
    private val appPreferencesRepository: AppPreferencesRepository,
    private val database: AppDatabase,
    private val supabaseClient: io.github.jan.supabase.SupabaseClient,
    @ApplicationContext private val context: Context
) {
    private val FRESHNESS_THRESHOLD = 24 * 60 * 60 * 1000L // 24 hours

    fun getProducts(): Flow<Resource<List<ProductDto>>> = flow {
        emit(Resource.Loading)

        val lastSync = appPreferencesRepository.getLastProductsSyncTime()
        val isStale = System.currentTimeMillis() - lastSync > FRESHNESS_THRESHOLD

        // Only trigger sync once per flow collection if stale or empty
        var syncTriggered = false

        productDao.getAllProducts().collect { entities ->
            if ((entities.isEmpty() || isStale) && !syncTriggered) {
                syncTriggered = true
                syncProducts()
            }

            if (entities.isEmpty() && syncTriggered) {
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

    fun getProductsPaged(category: String? = null): Flow<PagingData<ProductDto>> {
        val normalizedCategory = category?.let { normalizeCategory(it) }

        @OptIn(ExperimentalPagingApi::class)
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                initialLoadSize = 20
            ),
            remoteMediator = com.psl.pasalhub.dashboard.products.data.ProductRemoteMediator(
                supabaseClient = supabaseClient,
                database = database,
                category = normalizedCategory
            ),
            pagingSourceFactory = {
                if (normalizedCategory == null || normalizedCategory == "all") {
                    productDao.getProductsPaged()
                } else {
                    productDao.getProductsByCategoryPaged(normalizedCategory)
                }
            }
        ).flow.map { pagingData: PagingData<ProductEntity> ->
            pagingData.map { entity -> entity.toDto() }
        }
    }

    private fun syncProducts() {
        syncManager.triggerSync(SyncType.PRODUCTS)
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
    fun getCartItems(): Flow<List<CartEntity>> = cartDao.getCartItems()

    suspend fun addToCart(item: CartEntity) {
        cartDao.addToCart(item)
        syncManager.triggerSync(SyncType.CART, immediate = true, fetch = false)
    }

    suspend fun removeFromCart(productId: Int) {
        cartDao.getCartItems().first().find { it.productId == productId }?.let {
            cartDao.deleteCartItem(it)
            syncManager.triggerSync(SyncType.CART, immediate = true, fetch = false)
        }
    }

    suspend fun updateCartItem(item: CartEntity) {
        cartDao.updateCartItem(item)
        syncManager.triggerSync(SyncType.CART, immediate = true, fetch = false)
    }

    suspend fun clearCart() {
        cartDao.clearCart()
        syncManager.triggerSync(SyncType.CART, immediate = true, fetch = false)
    }

    // Order operations
    fun getOrders(): Flow<List<OrderEntity>> = orderDao.getOrders()

    suspend fun placeOrder(order: OrderEntity, productIdToRemove: Int? = null): Int {
        val id = orderDao.insertOrder(order)
        syncManager.triggerSync(SyncType.ORDERS, immediate = true, fetch = false)

        if (productIdToRemove != null) {
            removeFromCart(productIdToRemove)
        }

        return id.toInt()
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
