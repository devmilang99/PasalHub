package com.example.data.repository

import com.example.data.local.CartDao
import com.example.data.local.CartItem
import com.example.data.local.OrderDao
import com.example.data.local.OrderEntity
import com.example.data.local.UserDao
import com.example.data.local.UserEntity
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
            if (remoteProducts.isNotEmpty()) {
                emit(Resource.Success(remoteProducts))
            } else {
                emit(Resource.Success(fallbackProducts))
            }
        } catch (e: Exception) {
            emit(Resource.Success(fallbackProducts)) // Graceful fallback
        }
    }

    fun getProductsByCategory(category: String): Flow<Resource<List<ProductDto>>> = flow {
        emit(Resource.Loading)
        try {
            val remoteProducts = api.getProductsByCategory(category)
            if (remoteProducts.isNotEmpty()) {
                emit(Resource.Success(remoteProducts))
            } else {
                val filtered = fallbackProducts.filter { it.category.equals(category, ignoreCase = true) }
                emit(Resource.Success(filtered))
            }
        } catch (e: Exception) {
            val filtered = fallbackProducts.filter { it.category.equals(category, ignoreCase = true) }
            emit(Resource.Success(filtered))
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

    // High quality local fallback data
    private val fallbackProducts = listOf(
        ProductDto(
            id = 1,
            title = "Pasal Hub Wireless Noise Cancelling Headphones",
            price = 299.99,
            description = "Experience premium high-fidelity audio with hybrid active noise cancellation, ambient transparency mode, and 40-hour battery life. Perfect for travel or intense work focus.",
            category = "electronics",
            image = "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=600&q=80"
        ),
        ProductDto(
            id = 2,
            title = "Pasal Hub Smart Watch Series 5",
            price = 349.99,
            description = "Stay connected with style. Features stunning AMOLED edge-to-edge display, 24/7 continuous heart rate monitoring, blood oxygen levels, step tracking, and elegant sleep insights.",
            category = "electronics",
            image = "https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=600&q=80"
        ),
        ProductDto(
            id = 3,
            title = "Premium Leather Suede Jacket",
            price = 189.50,
            description = "Crafted from 100% fine suede leather, this jacket offers a modern tailored fit with double-stitched durability. Featuring premium matte black zippers and multiple convenient pockets.",
            category = "fashion",
            image = "https://images.unsplash.com/photo-1551028719-00167b16eac5?auto=format&fit=crop&w=600&q=80"
        ),
        ProductDto(
            id = 4,
            title = "Classic Gold Signet Ring",
            price = 120.00,
            description = "Solid 18K gold-plated sterling silver signet ring with a pristine mirror finish. A timeless, unisex statement piece designed to accompany you on any occasion.",
            category = "jewelery",
            image = "https://images.unsplash.com/photo-1605100804763-247f67b3557e?auto=format&fit=crop&w=600&q=80"
        ),
        ProductDto(
            id = 5,
            title = "Minimalist Ceramic Coffee Dripper Set",
            price = 45.00,
            description = "Pour-over coffee maker set complete with professional-grade high-fire matte black ceramic dripper, wooden support ring, and heat-resistant borosilicate glass server.",
            category = "home",
            image = "https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?auto=format&fit=crop&w=600&q=80"
        ),
        ProductDto(
            id = 6,
            title = "Luxury Silk Comfort Pillowcase Pair",
            price = 75.00,
            description = "Woven from 100% organic mulberry silk of the highest grade (22 Momme). Friction-reducing surface that is extremely gentle on hair and skin, ensuring deep, luxurious sleep.",
            category = "home",
            image = "https://images.unsplash.com/photo-1522771739844-6a9f6d5f14af?auto=format&fit=crop&w=600&q=80"
        )
    )
}
