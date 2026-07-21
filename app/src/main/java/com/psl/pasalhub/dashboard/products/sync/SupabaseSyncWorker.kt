package com.psl.pasalhub.dashboard.products.sync

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.psl.pasalhub.core.database.data.ProductDao
import com.psl.pasalhub.core.database.data.ProductEntity
import com.psl.pasalhub.core.database.data.UserDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns

@HiltWorker
class SupabaseSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val supabaseClient: SupabaseClient,
    private val productDao: ProductDao,
    private val userDao: UserDao
) : CoroutineWorker(appContext, workerParams) {

    private val postgrest = supabaseClient.postgrest

    override suspend fun doWork(): Result {
        Log.d("SupabaseSyncWorker", "Starting product sync (Fetch from Supabase)...")
        val user = supabaseClient.auth.currentUserOrNull()

        return try {
            val remoteProducts = postgrest["products"]
                .select(columns = Columns.ALL)
                .decodeList<ProductEntity>()

            Log.d("SupabaseSyncWorker", "Fetched ${remoteProducts.size} products from Supabase")
            if (remoteProducts.isNotEmpty()) {
                productDao.insertProducts(remoteProducts)
                if (user != null) {
                    userDao.updateLastSyncTime(user.id, System.currentTimeMillis())
                }
                Log.d("SupabaseSyncWorker", "Successfully cached products locally.")
            } else {
                Log.w("SupabaseSyncWorker", "No products found in remote database.")
            }
            Result.success()
        } catch (e: Exception) {
            Log.e("SupabaseSyncWorker", "Product sync failed: ${e.message}", e)
            Result.retry()
        }
    }
}
