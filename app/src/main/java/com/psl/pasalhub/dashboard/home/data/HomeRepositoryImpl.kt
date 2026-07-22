package com.psl.pasalhub.dashboard.home.data

import android.content.Context
import com.psl.pasalhub.core.application.domain.AppPreferencesRepository
import com.psl.pasalhub.core.database.data.CartEntity
import com.psl.pasalhub.core.database.data.UserDao
import com.psl.pasalhub.core.database.data.UserEntity
import com.psl.pasalhub.core.networking.remote.ProductDto
import com.psl.pasalhub.dashboard.home.domain.HomeRepository
import com.psl.pasalhub.dashboard.products.repository.ProductRepository
import com.psl.pasalhub.dashboard.products.repository.Resource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
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
        val user = userDao.getUser().first() ?: return
        val sellerName = when (product.category.lowercase()) {
            "electronics" -> "Tech Gear Hub"
            "jewelery" -> "Elegance Gems"
            "men's clothing", "women's clothing", "clothing" -> "Fashion Central"
            else -> "${product.category.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }} Boutique"
        }
        productRepository.addToCart(
            CartEntity(
                userId = user.id,
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
        appPrefs.emitNotification("Successfully added ${product.title} to your cart!")
    }

    override suspend fun removeFromCart(productId: Int) {
        val cartItems = productRepository.getCartItems().first()
        val item = cartItems.find { it.productId == productId }
        productRepository.removeFromCart(productId)
        item?.let {
            appPrefs.emitNotification("${it.title} removed from cart")
        }
    }

    override fun getCartItems(): Flow<List<CartEntity>> = productRepository.getCartItems()
}
