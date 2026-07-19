package com.psl.pasalhub.dashboard.cart.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.psl.pasalhub.core.database.data.CartDao
import com.psl.pasalhub.core.database.data.CartItem
import com.psl.pasalhub.core.database.data.ProductDao
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
    private val productDao: ProductDao
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val user = supabaseClient.auth.currentUserOrNull() ?: return Result.failure()

        return try {
            // 1. Fetch Remote Cart
            val remoteCart = supabaseClient.postgrest["cart"]
                .select {
                    filter {
                        eq("user_id", user.id)
                    }
                }
                .decodeList<RemoteCartItem>()

            // 2. Update Local Database from Remote
            if (remoteCart.isNotEmpty()) {
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
                            seller = "Sync'd Store" // Or fetch seller if available
                        )
                        cartDao.addToCart(localItem)
                    }
                }
            }

            // 3. Sync Local Cart to Remote
            val localCart = cartDao.getCartItems().first()
            val remoteData = localCart.map {
                mapOf(
                    "user_id" to user.id,
                    "product_id" to it.productId,
                    "quantity" to it.quantity
                )
            }

            if (remoteData.isNotEmpty()) {
                // Upsert to Supabase
                supabaseClient.postgrest["cart"].upsert(remoteData) {
                    onConflict = "user_id, product_id"
                }
            }
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    @kotlinx.serialization.Serializable
    data class RemoteCartItem(
        val user_id: String,
        val product_id: Int,
        val quantity: Int
    )
}
