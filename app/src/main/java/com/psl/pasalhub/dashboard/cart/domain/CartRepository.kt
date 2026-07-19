package com.psl.pasalhub.dashboard.cart.domain

import com.psl.pasalhub.core.database.data.CartItem
import com.psl.pasalhub.core.database.data.UserEntity
import kotlinx.coroutines.flow.Flow

interface CartRepository {
    fun getCartItems(): Flow<List<CartItem>>
    fun getUser(): Flow<UserEntity?>
    fun isDarkTheme(): Flow<Boolean>
    suspend fun increaseQuantity(item: CartItem)
    suspend fun decreaseQuantity(item: CartItem)
    suspend fun deleteCartItem(item: CartItem)
    suspend fun deleteMultipleCartItems(items: List<CartItem>)
    suspend fun checkout(
        selectedItems: List<CartItem>,
        finalTotal: Double,
        paymentMethod: String,
        appliedVoucher: String
    )
}
