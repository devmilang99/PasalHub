package com.psl.pasalhub.dashboard.cart.domain

import com.psl.pasalhub.core.database.data.CartEntity
import com.psl.pasalhub.core.database.data.UserEntity
import kotlinx.coroutines.flow.Flow

interface CartRepository {
    fun getCartItems(): Flow<List<CartEntity>>
    fun getUser(): Flow<UserEntity?>
    fun isDarkTheme(): Flow<Boolean>
    suspend fun increaseQuantity(item: CartEntity)
    suspend fun decreaseQuantity(item: CartEntity)
    suspend fun deleteCartItem(item: CartEntity)
    suspend fun deleteMultipleCartItems(items: List<CartEntity>)
    suspend fun checkout(
        selectedItems: List<CartEntity>,
        finalTotal: Double,
        paymentMethod: String,
        appliedVoucher: String
    )
}
