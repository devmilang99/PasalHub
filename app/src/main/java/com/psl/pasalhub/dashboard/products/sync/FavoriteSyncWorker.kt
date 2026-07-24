package com.psl.pasalhub.dashboard.products.sync

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
class FavoriteSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val dataSyncRepository: DataSyncRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val fetch = inputData.getBoolean("fetch", false)
        Log.d("FavoriteSyncWorker", "Starting favorites sync (fetch=$fetch)")

        return try {
            dataSyncRepository.syncFavorites(fetch)
            Result.success()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("FavoriteSyncWorker", "Favorites sync failed: ${e.message}", e)
            Result.retry()
        }
    }
}
