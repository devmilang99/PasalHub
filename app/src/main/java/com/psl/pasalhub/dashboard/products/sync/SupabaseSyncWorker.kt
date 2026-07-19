package com.psl.pasalhub.dashboard.products.sync

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.psl.pasalhub.core.database.data.ProductDao
import com.psl.pasalhub.core.database.data.ProductEntity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns

@HiltWorker
class SupabaseSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val postgrest: Postgrest,
    private val productDao: ProductDao
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            Log.d("SupabaseSyncWorker", "Starting product sync...")
            val remoteProducts = postgrest["products"]
                .select(columns = Columns.ALL)
                .decodeList<ProductEntity>()

            Log.d("SupabaseSyncWorker", "Fetched ${remoteProducts.size} products from Supabase")
            if (remoteProducts.isNotEmpty()) {
                productDao.insertProducts(remoteProducts)
                Log.d("SupabaseSyncWorker", "Inserted products into local database")
            }
            Result.success()
        } catch (e: Exception) {
            Log.e("SupabaseSyncWorker", "Error syncing products: ${e.message}", e)
            Result.retry()
        }
    }
}
