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
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class HomeRepositoryImpl @Inject constructor(
    private val productRepository: ProductRepository,
    private val userDao: UserDao,
    @ApplicationContext private val context: Context,
    private val appPrefs: AppPreferencesRepository
) : HomeRepository {

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

    override fun getFavoriteIds(): Flow<Set<Int>> = appPrefs.getFavoriteIds()

    override suspend fun toggleFavorite(productId: Int) {
        appPrefs.toggleFavorite(productId)
    }

    override suspend fun addToCart(product: ProductDto) {
        val sellerName = when(product.category.lowercase()) {
            "electronics" -> "Tech Gear Hub"
            "jewelery" -> "Elegance Gems"
            "men's clothing", "women's clothing", "clothing" -> "Fashion Central"
            else -> "${product.category.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }} Boutique"
        }
        productRepository.addToCart(
            CartItem(
                productId = product.id,
                title = product.title,
                price = product.price,
                description = product.description,
                category = product.category,
                image = product.image,
                quantity = 1,
                seller = sellerName
            )
        )
    }

    override suspend fun removeFromCart(productId: Int) {
        productRepository.removeFromCart(productId)
    }

    override fun getCartItems(): Flow<List<CartItem>> = productRepository.getCartItems()
}
