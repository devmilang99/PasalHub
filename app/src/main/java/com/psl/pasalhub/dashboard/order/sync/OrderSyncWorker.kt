package com.psl.pasalhub.dashboard.order.sync

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.psl.pasalhub.core.sync.DataSyncRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.github.jan.supabase.exceptions.HttpRequestException
import kotlinx.coroutines.CancellationException
import java.net.UnknownHostException

@HiltWorker
class OrderSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val dataSyncRepository: DataSyncRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val fetch = inputData.getBoolean("fetch", false)
        Log.d("OrderSyncWorker", "Starting order sync (fetch=$fetch)")

        return try {
            dataSyncRepository.syncOrders(fetch)
            Result.success()
        } catch (e: CancellationException) {
            throw e
        } catch (e: HttpRequestException) {
            val isNetworkIssue = e.cause is UnknownHostException ||
                    e.message?.contains("Unable to resolve host", ignoreCase = true) == true

            if (isNetworkIssue) {
                Log.w(
                    "OrderSyncWorker",
                    "Order sync failed due to network: ${e.message}. Will retry."
                )
            } else {
                Log.e("OrderSyncWorker", "Order sync failed with HTTP error: ${e.message}", e)
            }
            Result.retry()
        } catch (e: Exception) {
            Log.e("OrderSyncWorker", "Order sync failed with error: ${e.message}", e)
            Result.retry()
        }
    }
}
