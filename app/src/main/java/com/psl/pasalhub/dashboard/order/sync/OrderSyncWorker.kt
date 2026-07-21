package com.psl.pasalhub.dashboard.order.sync

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.psl.pasalhub.core.database.data.OrderDao
import com.psl.pasalhub.core.database.data.UserDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.first

@HiltWorker
class OrderSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val supabaseClient: SupabaseClient,
    private val orderDao: OrderDao,
    private val userDao: UserDao
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("OrderSyncWorker", "Starting order sync...")
        val sessionUser = supabaseClient.auth.currentUserOrNull()

        if (sessionUser == null) {
            Log.w("OrderSyncWorker", "No Supabase session found. Skipping order sync.")
            return Result.failure()
        }

        val localUser = userDao.getUser().first()
        if (localUser == null || localUser.id != sessionUser.id) {
            Log.w("OrderSyncWorker", "Local user mismatch or missing. Skipping order sync.")
            return Result.failure()
        }

        return try {
            val localOrders = orderDao.getOrders().first()
            Log.d("OrderSyncWorker", "Found ${localOrders.size} local orders to sync")

            val remoteData = localOrders.map {
                RemoteOrder(
                    user_id = sessionUser.id,
                    total_amount = it.totalAmount,
                    items_summary = it.itemsSummary,
                    status = it.status,
                    address = it.address,
                    date = it.date,
                    quantity = it.quantity,
                    price = it.price,
                    seller = it.seller,
                    cancelled_reason = it.cancelledReason,
                    rating = it.rating,
                    review = it.review,
                    progress = it.progress,
                    is_paused = it.isPaused
                )
            }

            if (remoteData.isNotEmpty()) {
                Log.d("OrderSyncWorker", "Upserting ${remoteData.size} orders to Supabase...")
                supabaseClient.postgrest["orders"].upsert(remoteData) {
                    onConflict = "user_id,date"
                }
                Log.d("OrderSyncWorker", "Order upsert successful!")
            } else {
                Log.d("OrderSyncWorker", "No local orders to sync.")
            }
            Result.success()
        } catch (e: Exception) {
            Log.e("OrderSyncWorker", "Order sync failed: ${e.message}", e)
            Result.retry()
        }
    }

    @kotlinx.serialization.Serializable
    data class RemoteOrder(
        val user_id: String,
        val total_amount: Double,
        val items_summary: String,
        val status: String,
        val address: String,
        val date: Long,
        val quantity: Int,
        val price: Double,
        val seller: String,
        val cancelled_reason: String?,
        val rating: Int,
        val review: String?,
        val progress: Int,
        val is_paused: Boolean
    )
}
