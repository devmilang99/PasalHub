package com.psl.pasalhub.dashboard.profile.data

import android.content.Context
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.psl.pasalhub.core.application.domain.AppPreferencesRepository
import com.psl.pasalhub.core.auth.domain.SupabaseAuthRepository
import com.psl.pasalhub.core.database.data.UserDao
import com.psl.pasalhub.core.database.data.UserEntity
import com.psl.pasalhub.core.networking.remote.ProductDto
import com.psl.pasalhub.dashboard.products.repository.ProductRepository
import com.psl.pasalhub.dashboard.products.repository.Resource
import com.psl.pasalhub.dashboard.profile.domain.ProfileRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val productRepository: ProductRepository,
    private val userDao: UserDao,
    private val pointDao: com.psl.pasalhub.core.database.data.PointDao,
    @ApplicationContext private val context: Context,
    private val appPrefs: AppPreferencesRepository,
    private val authRepository: SupabaseAuthRepository
) : ProfileRepository {

    private val prefs = context.getSharedPreferences("pasalhub_settings", Context.MODE_PRIVATE)
    private val pointsPrefs = context.getSharedPreferences("pasalhub_points", Context.MODE_PRIVATE)
    private val passPrefs = context.getSharedPreferences("pasalhub_passwords", Context.MODE_PRIVATE)

    override fun getUser(): Flow<UserEntity?> = userDao.getUser()

    override fun getFavoriteIds(): Flow<Set<Int>> = appPrefs.getFavoriteIds()

    override fun getProducts(): Flow<Resource<List<ProductDto>>> = productRepository.getProducts()

    override fun getMemberPoints(email: String): Flow<Int> = flow {
        val user = userDao.getUserByEmail(email)
        if (user != null) {
            emitAll(pointDao.getTotalPoints(user.id).map { it ?: 250 })
        } else {
            emit(250)
        }
    }

    override fun getPointHistory(userId: String): Flow<List<com.psl.pasalhub.core.database.data.PointTransactionEntity>> {
        return pointDao.getPointHistory(userId)
    }

    override fun getPointHistoryPaged(userId: String): Flow<PagingData<com.psl.pasalhub.core.database.data.PointTransactionEntity>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                pointDao.getPointHistoryPaged(userId)
            }
        ).flow
    }

    override suspend fun addPoints(userId: String, amount: Int, reason: String) {
        pointDao.insertTransaction(
            com.psl.pasalhub.core.database.data.PointTransactionEntity(
                userId = userId,
                amount = amount,
                reason = reason
            )
        )
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
        authRepository.updatePassword(newPass)
        passPrefs.edit().putString("pwd_$email", newPass).apply()
    }

    override suspend fun toggleFavorite(productId: Int) {
        appPrefs.toggleFavorite(productId)
    }
}
