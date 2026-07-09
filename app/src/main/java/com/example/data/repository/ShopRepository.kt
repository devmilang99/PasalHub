package com.example.data.repository

import com.example.core.database.data.CartDao
import com.example.core.database.data.CartItem
import com.example.core.database.data.OrderDao
import com.example.core.database.data.OrderEntity
import com.example.core.database.data.UserDao
import com.example.core.database.data.UserEntity
import com.example.data.remote.FakeStoreApi
import com.example.data.remote.ProductDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

sealed interface Resource<out T> {
    object Loading : Resource<Nothing>
    data class Success<out T>(val data: T) : Resource<T>
    data class Error(val message: String) : Resource<Nothing>
}

class ShopRepository(
    private val api: FakeStoreApi,
    private val userDao: UserDao,
    private val cartDao: CartDao,
    private val orderDao: OrderDao
) {
    fun getProducts(): Flow<Resource<List<ProductDto>>> = flow {
        emit(Resource.Loading)
        try {
            val remoteProducts = api.getProducts()
            // Combine remote products with our 300+ sample products for a rich experience
            val combined = (remoteProducts + SampleData.products).distinctBy { it.id }
            emit(Resource.Success(combined))
        } catch (e: Exception) {
            emit(Resource.Success(SampleData.products)) // Graceful fallback to our rich data
        }
    }

    fun getProductsByCategory(category: String): Flow<Resource<List<ProductDto>>> = flow {
        emit(Resource.Loading)
        val normalizedCategory = normalizeCategory(category)
        try {
            val remoteProducts = api.getProductsByCategory(normalizedCategory)
            val localFiltered = SampleData.products.filter { it.category.equals(normalizedCategory, ignoreCase = true) }
            val combined = (remoteProducts + localFiltered).distinctBy { it.id }
            emit(Resource.Success(combined))
        } catch (e: Exception) {
            val filtered = SampleData.products.filter { it.category.equals(normalizedCategory, ignoreCase = true) }
            emit(Resource.Success(filtered))
        }
    }

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

    // Cart operations
    fun getCartItems(): Flow<List<CartItem>> = cartDao.getCartItems()

    suspend fun addToCart(item: CartItem) = cartDao.addToCart(item)

    suspend fun updateCartItem(item: CartItem) = cartDao.updateCartItem(item)

    suspend fun deleteCartItem(item: CartItem) = cartDao.deleteCartItem(item)

    suspend fun clearCart() = cartDao.clearCart()

    // Order operations
    fun getOrders(): Flow<List<OrderEntity>> = orderDao.getOrders()

    suspend fun placeOrder(order: OrderEntity) {
        orderDao.insertOrder(order)
        cartDao.clearCart()
    }

    suspend fun updateOrder(order: OrderEntity) {
        orderDao.updateOrder(order)
    }

    // User operations
    fun getUser(): Flow<UserEntity?> = userDao.getUser()

    suspend fun getUserByEmail(email: String): UserEntity? = userDao.getUserByEmail(email)

    suspend fun saveUser(user: UserEntity) = userDao.insertUser(user)

    suspend fun clearUser() = userDao.clearUser()
}
