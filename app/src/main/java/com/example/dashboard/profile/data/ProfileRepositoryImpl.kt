package com.example.dashboard.profile.data

import android.content.Context
import com.example.core.application.domain.AppPreferencesRepository
import com.example.dashboard.profile.domain.ProfileRepository
import com.example.core.database.data.UserDao
import com.example.core.database.data.UserEntity
import com.example.core.networking.remote.ProductDto
import com.example.dashboard.products.repository.Resource
import com.example.dashboard.products.repository.ProductRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val productRepository: ProductRepository,
    private val userDao: UserDao,
    @ApplicationContext private val context: Context,
    private val appPrefs: AppPreferencesRepository
) : ProfileRepository {

    private val prefs = context.getSharedPreferences("pasalhub_settings", Context.MODE_PRIVATE)
    private val pointsPrefs = context.getSharedPreferences("pasalhub_points", Context.MODE_PRIVATE)
    private val passPrefs = context.getSharedPreferences("pasalhub_passwords", Context.MODE_PRIVATE)
    private val favPrefs = context.getSharedPreferences("pasalhub_favorites", Context.MODE_PRIVATE)

    override fun getUser(): Flow<UserEntity?> = userDao.getUser()

    override fun getFavoriteIds(): Flow<Set<Int>> = flow {
        val favStrings = favPrefs.getStringSet("fav_set", emptySet()) ?: emptySet()
        emit(favStrings.mapNotNull { it.toIntOrNull() }.toSet())
    }

    override fun getProducts(): Flow<Resource<List<ProductDto>>> = productRepository.getProducts()

    override fun getMemberPoints(email: String): Flow<Int> = flow {
        emit(pointsPrefs.getInt("pts_$email", 250))
    }

    override fun getPassword(email: String): Flow<String> = flow {
        emit(passPrefs.getString("pwd_$email", "password") ?: "password")
    }

    override fun isDarkTheme(): Flow<Boolean> = appPrefs.isDarkTheme()

    override suspend fun updateAddress(address: String) {
        userDao.getUser().first()?.let { user ->
            userDao.insertUser(user.copy(address = address))
        }
    }

    override suspend fun updatePassword(email: String, newPass: String) {
        passPrefs.edit().putString("pwd_$email", newPass).apply()
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
}
