package com.example.dashboard.profile.data

import android.content.Context
import androidx.core.content.edit
import com.example.dashboard.profile.domain.ProfileRepository
import com.example.core.database.data.UserDao
import com.example.core.database.data.UserEntity
import com.example.data.remote.ProductDto
import com.example.data.repository.Resource
import com.example.data.repository.ShopRepository
import kotlinx.coroutines.flow.*

class ProfileRepositoryImpl(
    private val shopRepository: ShopRepository,
    private val userDao: UserDao,
    private val context: Context
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

    override fun getProducts(): Flow<Resource<List<ProductDto>>> = shopRepository.getProducts()

    override fun getMemberPoints(email: String): Flow<Int> = flow {
        emit(pointsPrefs.getInt("pts_$email", 250))
    }

    override fun getPassword(email: String): Flow<String> = flow {
        emit(passPrefs.getString("pwd_$email", "password") ?: "password")
    }

    override fun isDarkTheme(): Flow<Boolean> = flow {
        emit(prefs.getBoolean("dark_theme", true))
    }

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
