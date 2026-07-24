package com.psl.pasalhub.dashboard.cart.sync

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.psl.pasalhub.core.sync.DataSyncRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException

@HiltWorker
class CartSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val dataSyncRepository: DataSyncRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val fetch = inputData.getBoolean("fetch", false)
        Log.d("CartSyncWorker", "Starting cart sync (fetch=$fetch)")

        return try {
            dataSyncRepository.syncCart(fetch)
            Result.success()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("CartSyncWorker", "Cart sync failed: ${e.message}", e)
            Result.retry()
        }
    }
}
