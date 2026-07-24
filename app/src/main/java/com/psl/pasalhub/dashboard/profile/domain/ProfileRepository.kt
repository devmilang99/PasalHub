package com.psl.pasalhub.dashboard.profile.domain

import com.psl.pasalhub.core.database.data.UserEntity
import com.psl.pasalhub.core.networking.remote.ProductDto
import com.psl.pasalhub.dashboard.products.repository.Resource
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun getUser(): Flow<UserEntity?>
    fun getFavoriteIds(): Flow<Set<Int>>
    fun getProducts(): Flow<Resource<List<ProductDto>>>
    fun getMemberPoints(email: String): Flow<Int>
    fun getPointHistory(userId: String): Flow<List<com.psl.pasalhub.core.database.data.PointTransactionEntity>>
    fun getPointHistoryPaged(userId: String): Flow<androidx.paging.PagingData<com.psl.pasalhub.core.database.data.PointTransactionEntity>>
    suspend fun addPoints(userId: String, amount: Int, reason: String)
    fun getPassword(email: String): Flow<String>
    fun isDarkTheme(): Flow<Boolean>
    suspend fun updateAddress(address: String)
    suspend fun updatePassword(email: String, newPass: String)
    suspend fun toggleFavorite(productId: Int)
}
