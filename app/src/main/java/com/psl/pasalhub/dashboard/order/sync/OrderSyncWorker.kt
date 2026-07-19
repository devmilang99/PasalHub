package com.psl.pasalhub.dashboard.order.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.psl.pasalhub.core.database.data.OrderDao
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
    private val orderDao: OrderDao
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val user = supabaseClient.auth.currentUserOrNull() ?: return Result.failure()

        return try {
            val localOrders = orderDao.getOrders().first()
            val remoteData = localOrders.map {
                mapOf(
                    "user_id" to user.id,
                    "total_amount" to it.totalAmount,
                    "items_summary" to it.itemsSummary,
                    "status" to it.status,
                    "address" to it.address,
                    "date" to it.date,
                    "quantity" to it.quantity,
                    "seller" to it.seller
                )
            }

            if (remoteData.isNotEmpty()) {
                // Using upsert if we have a way to identify orders remotely (e.g. by date + user_id or local orderId)
                // For simplicity, let's just insert new ones or upsert if we add a 'local_id' column to Supabase
                supabaseClient.postgrest["orders"].upsert(remoteData) {
                    onConflict =
                        "user_id, date" // Assuming date is unique enough for a user's order or use a proper ID
                }
            }
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
