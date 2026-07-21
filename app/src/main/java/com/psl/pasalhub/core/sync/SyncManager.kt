package com.psl.pasalhub.core.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.psl.pasalhub.dashboard.cart.sync.CartSyncWorker
import com.psl.pasalhub.dashboard.order.sync.OrderSyncWorker
import com.psl.pasalhub.dashboard.products.sync.SupabaseSyncWorker
import com.psl.pasalhub.dashboard.profile.sync.FavoriteSyncWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

enum class SyncType {
    CART, FAVORITES, ORDERS, PROFILE, PRODUCTS
}

@Singleton
class SyncManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val workManager = WorkManager.getInstance(context)

    val isCartSyncing: Flow<Boolean> = getSyncStatusFlow("sync_cart")
    val isOrdersSyncing: Flow<Boolean> = getSyncStatusFlow("sync_orders")
    val isFavoritesSyncing: Flow<Boolean> = getSyncStatusFlow("sync_favorites")
    val isProductsSyncing: Flow<Boolean> = getSyncStatusFlow("sync_products")

    val isSyncing: Flow<Boolean> = isProductsSyncing

    private fun getSyncStatusFlow(uniqueWorkName: String): Flow<Boolean> {
        return workManager.getWorkInfosForUniqueWorkFlow(uniqueWorkName)
            .map { workInfos ->
                workInfos.any { it.state == WorkInfo.State.RUNNING || it.state == WorkInfo.State.ENQUEUED }
            }
            .distinctUntilChanged()
    }

    fun triggerSync(type: SyncType, immediate: Boolean = false) {
        // We use REPLACE policy for immediate syncs to ensure they start right away
        triggerWorkManager(type, immediate)
    }

    fun triggerAllSyncs(immediate: Boolean = false) {
        triggerSync(SyncType.CART, immediate)
        triggerSync(SyncType.ORDERS, immediate)
        triggerSync(SyncType.FAVORITES, immediate)
        triggerSync(SyncType.PRODUCTS, immediate)
    }

    fun schedulePeriodicSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        // Periodic sync for products and profile data every 4 hours
        val periodicWorkRequest = PeriodicWorkRequestBuilder<SupabaseSyncWorker>(4, TimeUnit.HOURS)
            .setConstraints(constraints)
            .addTag("periodic_sync")
            .build()

        workManager.enqueueUniquePeriodicWork(
            "periodic_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            periodicWorkRequest
        )
    }

    private fun triggerWorkManager(type: SyncType, immediate: Boolean) {
        val workClass = when (type) {
            SyncType.CART -> CartSyncWorker::class.java
            SyncType.ORDERS -> OrderSyncWorker::class.java
            SyncType.FAVORITES -> FavoriteSyncWorker::class.java
            SyncType.PRODUCTS -> SupabaseSyncWorker::class.java
            SyncType.PROFILE -> null
        } ?: return

        val syncRequest = OneTimeWorkRequest.Builder(workClass)
            .setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            )
            // If immediate, we might want to set high priority if available (Expedited), 
            // but for now, just REPLACE will do to ensure the latest trigger wins.
            .build()

        workManager.enqueueUniqueWork(
            "sync_${type.name.lowercase()}",
            if (immediate) ExistingWorkPolicy.REPLACE else ExistingWorkPolicy.KEEP,
            syncRequest
        )
    }
}
