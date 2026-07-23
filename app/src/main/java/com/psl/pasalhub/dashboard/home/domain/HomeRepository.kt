package com.psl.pasalhub.dashboard.home.domain

import androidx.paging.PagingData
import com.psl.pasalhub.core.database.data.UserEntity
import com.psl.pasalhub.core.networking.remote.ProductDto
import com.psl.pasalhub.dashboard.products.repository.Resource
import kotlinx.coroutines.flow.Flow

interface HomeRepository {
    fun getProducts(): Flow<Resource<List<ProductDto>>>
    fun getProductsByCategory(category: String): Flow<Resource<List<ProductDto>>>
    fun getProductsPaged(category: String? = null): Flow<PagingData<ProductDto>>
    fun getUser(): Flow<UserEntity?>
    suspend fun updateUserAddress(address: String)
    fun isDarkTheme(): Flow<Boolean>
    suspend fun toggleTheme()
    fun getFavoriteIds(): Flow<Set<Int>>
    suspend fun toggleFavorite(productId: Int)
    suspend fun addToCart(product: ProductDto)
    suspend fun removeFromCart(productId: Int)
    fun getCartItems(): Flow<List<com.psl.pasalhub.core.database.data.CartEntity>>
}
