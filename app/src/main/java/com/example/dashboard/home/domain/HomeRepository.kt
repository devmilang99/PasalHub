package com.example.dashboard.home.domain

import com.example.core.database.data.UserEntity
import com.example.data.remote.ProductDto
import com.example.data.repository.Resource
import kotlinx.coroutines.flow.Flow

interface HomeRepository {
    fun getProducts(): Flow<Resource<List<ProductDto>>>
    fun getProductsByCategory(category: String): Flow<Resource<List<ProductDto>>>
    fun getUser(): Flow<UserEntity?>
    suspend fun updateUserAddress(address: String)
    fun isDarkTheme(): Flow<Boolean>
    suspend fun toggleTheme()
    fun getFavoriteIds(): Flow<Set<Int>>
    suspend fun toggleFavorite(productId: Int)
    suspend fun addToCart(product: ProductDto)
}
