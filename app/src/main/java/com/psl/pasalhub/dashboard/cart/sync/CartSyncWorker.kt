package com.psl.pasalhub.dashboard.cart.sync

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.psl.pasalhub.core.database.data.CartDao
import com.psl.pasalhub.core.database.data.CartItem
import com.psl.pasalhub.core.database.data.ProductDao
import com.psl.pasalhub.core.database.data.UserDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.first

@HiltWorker
class CartSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val supabaseClient: SupabaseClient,
    private val cartDao: CartDao,
    private val productDao: ProductDao,
    private val userDao: UserDao
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("CartSyncWorker", "Starting cart sync...")
        val sessionUser = supabaseClient.auth.currentUserOrNull()

        if (sessionUser == null) {
            Log.w("CartSyncWorker", "No Supabase session found. Sync aborted.")
            return Result.failure()
        }

        val localUser = userDao.getUser().first()
        if (localUser == null || localUser.id != sessionUser.id) {
            Log.w("CartSyncWorker", "Local user mismatch or missing. Sync aborted.")
            return Result.failure()
        }

        Log.d("CartSyncWorker", "Syncing for user: ${sessionUser.email}")

        return try {
            // 1. Restore from Supabase if never synced
            if (!localUser.hasSyncedCart) {
                Log.d("CartSyncWorker", "First sync: Restoring from Supabase backup...")
                val remoteCart = supabaseClient.postgrest["cart"]
                    .select {
                        filter {
                            eq("user_id", sessionUser.id)
                        }
                    }
                    .decodeList<RemoteCartItem>()

                remoteCart.forEach { remoteItem ->
                    val product = productDao.getProductById(remoteItem.product_id)
                    if (product != null) {
                        val localItem = CartItem(
                            productId = product.id,
                            title = product.title,
                            price = product.price,
                            description = product.description ?: "",
                            category = product.category ?: "General",
                            image = product.image ?: "",
                            quantity = remoteItem.quantity,
                            seller = "Sync'd Store",
                            updatedAt = remoteItem.updated_at
                        )
                        cartDao.addToCart(localItem)
                    }
                }
                userDao.updateSyncStatus(localUser.id, true)
                Log.d("CartSyncWorker", "Restore complete. Restored ${remoteCart.size} items.")
            }

            // 2. Sync Local -> Remote (Backup)
            val localCart = cartDao.getCartItems().first()
            val remoteData = localCart.map {
                RemoteCartItem(
                    user_id = sessionUser.id,
                    product_id = it.productId,
                    quantity = it.quantity,
                    updated_at = it.updatedAt
                )
            }

            if (remoteData.isNotEmpty()) {
                Log.d("CartSyncWorker", "Upserting ${remoteData.size} items to Supabase...")
                supabaseClient.postgrest["cart"].upsert(remoteData) {
                    onConflict = "user_id,product_id"
                }
            }

            // 3. Cleanup Remote (Remove items that are no longer in local cart)
            // Fetch remote again to see what's there
            val currentRemoteCart = supabaseClient.postgrest["cart"]
                .select {
                    filter {
                        eq("user_id", sessionUser.id)
                    }
                }
                .decodeList<RemoteCartItem>()

            val localProductIds = localCart.map { it.productId }.toSet()
            val itemsToRemove = currentRemoteCart.filter { it.product_id !in localProductIds }

            if (itemsToRemove.isNotEmpty()) {
                Log.d(
                    "CartSyncWorker",
                    "Removing ${itemsToRemove.size} items from Supabase backup (Syncing deletions/checkouts)..."
                )
                itemsToRemove.forEach { item ->
                    supabaseClient.postgrest["cart"].delete {
                        filter {
                            eq("user_id", sessionUser.id)
                            eq("product_id", item.product_id)
                        }
                    }
                }
            }

            Log.d("CartSyncWorker", "Sync successful!")
            Result.success()
        } catch (e: Exception) {
            Log.e("CartSyncWorker", "Sync failed: ${e.message}", e)
            Result.retry()
        }
    }

    @kotlinx.serialization.Serializable
    data class RemoteCartItem(
        val user_id: String,
        val product_id: Int,
        val quantity: Int,
        val updated_at: Long = System.currentTimeMillis()
    )
}
