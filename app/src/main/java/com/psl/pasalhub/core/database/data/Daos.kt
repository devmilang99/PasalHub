package com.psl.pasalhub.core.database.data

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users LIMIT 1")
    fun getUser(): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("UPDATE users SET hasSyncedCart = :hasSynced WHERE id = :userId")
    suspend fun updateSyncStatus(userId: String, hasSynced: Boolean)

    @Query("UPDATE users SET hasSyncedFavorites = :hasSynced WHERE id = :userId")
    suspend fun updateFavoriteSyncStatus(userId: String, hasSynced: Boolean)

    @Query("UPDATE users SET lastFullSyncTime = :timestamp WHERE id = :userId")
    suspend fun updateLastSyncTime(userId: String, timestamp: Long)

    @Query("DELETE FROM users")
    suspend fun clearUser()
}

@Dao
interface PointDao {
    @Query("SELECT * FROM point_transactions WHERE user_id = :userId ORDER BY timestamp DESC")
    fun getPointHistory(userId: String): Flow<List<PointTransactionEntity>>

    @Query("SELECT * FROM point_transactions WHERE user_id = :userId ORDER BY timestamp DESC")
    fun getPointHistoryPaged(userId: String): PagingSource<Int, PointTransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: PointTransactionEntity)

    @Query("SELECT SUM(amount) FROM point_transactions WHERE user_id = :userId")
    fun getTotalPoints(userId: String): Flow<Int?>

    @Query("DELETE FROM point_transactions")
    suspend fun clearAll()
}

@Dao
interface CartDao {
    @Query("SELECT * FROM cart")
    fun getCartItems(): Flow<List<CartEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToCart(item: CartEntity)

    @Update
    suspend fun updateCartItem(item: CartEntity)

    @Delete
    suspend fun deleteCartItem(item: CartEntity)

    @Delete
    suspend fun deleteMultipleCartItems(items: List<CartEntity>)

    @Query("DELETE FROM cart")
    suspend fun clearCart()
}

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites")
    fun getFavorites(): Flow<List<FavoriteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteEntity)

    @Delete
    suspend fun removeFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE productId = :productId")
    suspend fun removeFavoriteById(productId: Int)

    @Query("DELETE FROM favorites")
    suspend fun clearFavorites()
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY date DESC")
    fun getOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders ORDER BY date DESC")
    fun getOrdersPaged(): PagingSource<Int, OrderEntity>

    @Query("SELECT * FROM orders WHERE status IN (:statuses) ORDER BY date DESC")
    fun getOrdersByStatusPaged(statuses: List<String>): PagingSource<Int, OrderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity): Long

    @Update
    suspend fun updateOrder(order: OrderEntity)
}

@Dao
interface RemoteKeysDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllProductKeys(remoteKey: List<ProductRemoteKeys>)

    @Query("SELECT * FROM product_remote_keys WHERE productId = :id")
    suspend fun getProductRemoteKeysById(id: Int): ProductRemoteKeys?

    @Query("DELETE FROM product_remote_keys")
    suspend fun clearProductRemoteKeys()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllOrderKeys(remoteKey: List<OrderRemoteKeys>)

    @Query("SELECT * FROM order_remote_keys WHERE orderId = :id")
    suspend fun getOrderRemoteKeysById(id: Int): OrderRemoteKeys?

    @Query("DELETE FROM order_remote_keys")
    suspend fun clearOrderRemoteKeys()
}
