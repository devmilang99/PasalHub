package com.example.dashboard.home.data

import android.content.Context
import androidx.core.content.edit
import com.example.core.application.domain.AppPreferencesRepository
import com.example.dashboard.home.domain.HomeRepository
import com.example.core.database.data.UserDao
import com.example.core.database.data.UserEntity
import com.example.core.database.data.CartItem
import com.example.core.networking.remote.ProductDto
import com.example.dashboard.products.repository.Resource
import com.example.dashboard.products.repository.ProductRepository
import kotlinx.coroutines.flow.*

class HomeRepositoryImpl(
    private val productRepository: ProductRepository,
    private val userDao: UserDao,
    private val context: Context,
    private val appPrefs: AppPreferencesRepository
) : HomeRepository {

    private val prefs = context.getSharedPreferences("pasalhub_settings", Context.MODE_PRIVATE)
    private val favPrefs = context.getSharedPreferences("pasalhub_favorites", Context.MODE_PRIVATE)

    override fun getProducts(): Flow<Resource<List<ProductDto>>> = productRepository.getProducts()

    override fun getProductsByCategory(category: String): Flow<Resource<List<ProductDto>>> = 
        productRepository.getProductsByCategory(category)

    override fun getUser(): Flow<UserEntity?> = userDao.getUser()

    override suspend fun updateUserAddress(address: String) {
        userDao.getUser().first()?.let { user ->
            userDao.insertUser(user.copy(address = address))
        }
    }

    override fun isDarkTheme(): Flow<Boolean> = appPrefs.isDarkTheme()

    override suspend fun toggleTheme() {
        appPrefs.toggleTheme()
    }

    override fun getFavoriteIds(): Flow<Set<Int>> = flow {
        val favStrings = favPrefs.getStringSet("fav_set", emptySet()) ?: emptySet()
        emit(favStrings.mapNotNull { it.toIntOrNull() }.toSet())
    }

    override suspend fun toggleFavorite(productId: Int) {
        val current = favPrefs.getStringSet("fav_set", emptySet())?.toMutableSet() ?: mutableSetOf()
        if (current.contains(productId.toString())) {
            current.remove(productId.toString())
        } else {
            current.add(productId.toString())
        }
        favPrefs.edit().putStringSet("fav_set", current).apply()
    }

    override suspend fun addToCart(product: ProductDto) {
        productRepository.addToCart(
            CartItem(
                productId = product.id,
                title = product.title,
                price = product.price,
                description = product.description,
                category = product.category,
                image = product.image,
                quantity = 1
            )
        )
    }
}
