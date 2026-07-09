package com.example.dashboard.home.data

import android.content.Context
import androidx.core.content.edit
import com.example.dashboard.home.domain.HomeRepository
import com.example.core.database.data.UserDao
import com.example.core.database.data.UserEntity
import com.example.core.database.data.CartItem
import com.example.data.remote.ProductDto
import com.example.data.repository.Resource
import com.example.data.repository.ShopRepository
import kotlinx.coroutines.flow.*

class HomeRepositoryImpl(
    private val shopRepository: ShopRepository,
    private val userDao: UserDao,
    private val context: Context
) : HomeRepository {

    private val prefs = context.getSharedPreferences("pasalhub_settings", Context.MODE_PRIVATE)
    private val favPrefs = context.getSharedPreferences("pasalhub_favorites", Context.MODE_PRIVATE)

    override fun getProducts(): Flow<Resource<List<ProductDto>>> = shopRepository.getProducts()

    override fun getProductsByCategory(category: String): Flow<Resource<List<ProductDto>>> = 
        shopRepository.getProductsByCategory(category)

    override fun getUser(): Flow<UserEntity?> = userDao.getUser()

    override suspend fun updateUserAddress(address: String) {
        userDao.getUser().first()?.let { user ->
            userDao.insertUser(user.copy(address = address))
        }
    }

    override fun isDarkTheme(): Flow<Boolean> {
        return flow {
            emit(prefs.getBoolean("dark_theme", true))
        }
    }

    override suspend fun toggleTheme() {
        val current = prefs.getBoolean("dark_theme", true)
        prefs.edit { putBoolean("dark_theme", !current) }
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
        shopRepository.addToCart(
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
