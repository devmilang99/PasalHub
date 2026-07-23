package com.psl.pasalhub.core.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.psl.pasalhub.dashboard.cart.sync.CartSyncWorker
import com.psl.pasalhub.dashboard.order.sync.OrderSyncWorker
import com.psl.pasalhub.dashboard.products.sync.FavoriteSyncWorker
import com.psl.pasalhub.dashboard.products.sync.SupabaseSyncWorker
import com.psl.pasalhub.dashboard.profile.sync.ProfileSyncWorker
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
                // Only show refreshing state when actively RUNNING.
                // ENQUEUED can stay stuck if network is unavailable or waiting for retry.
                workInfos.any { it.state == WorkInfo.State.RUNNING }
            }
            .distinctUntilChanged()
    }

    fun triggerSync(type: SyncType, immediate: Boolean = false, fetch: Boolean = false) {
        triggerWorkManager(type, immediate, fetch)
    }

    fun triggerAllSyncs(fetch: Boolean = false) {
        triggerSync(SyncType.CART, fetch)
        triggerSync(SyncType.ORDERS, fetch)
        triggerSync(SyncType.FAVORITES, fetch)
        triggerSync(SyncType.PRODUCTS, fetch)
    }

    /**
     * Triggers a sequential multi-step sync chain for login.
     * Products -> Favorites -> Cart -> Orders -> Profile
     */
    fun triggerLoginSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val inputData = Data.Builder()
            .putBoolean("fetch", true)
            .build()

        // Create sequential chain: Profile -> Products -> Favorites -> Cart -> Orders
        workManager.beginUniqueWork(
            "login_sync_chain",
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequest.Builder(ProfileSyncWorker::class.java)
                .setConstraints(constraints)
                .setInputData(inputData)
                .build()
        ).then(
            OneTimeWorkRequest.Builder(SupabaseSyncWorker::class.java)
                .setConstraints(constraints)
                .setInputData(inputData)
                .build()
        ).then(
            OneTimeWorkRequest.Builder(FavoriteSyncWorker::class.java)
                .setConstraints(constraints)
                .setInputData(inputData)
                .build()
        ).then(
            OneTimeWorkRequest.Builder(CartSyncWorker::class.java)
                .setConstraints(constraints)
                .setInputData(inputData)
                .build()
        ).then(
            OneTimeWorkRequest.Builder(OrderSyncWorker::class.java)
                .setConstraints(constraints)
                .setInputData(inputData)
                .build()
        ).enqueue()
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

    private fun triggerWorkManager(type: SyncType, immediate: Boolean, fetch: Boolean) {
        val workClass = when (type) {
            SyncType.CART -> CartSyncWorker::class.java
            SyncType.FAVORITES -> FavoriteSyncWorker::class.java
            SyncType.ORDERS -> OrderSyncWorker::class.java
            SyncType.PRODUCTS -> SupabaseSyncWorker::class.java
            SyncType.PROFILE -> ProfileSyncWorker::class.java
        }

        val inputData = Data.Builder()
            .putBoolean("fetch", fetch)
            .build()

        val syncRequestBuilder = OneTimeWorkRequest.Builder(workClass)
            .setInputData(inputData)
            .setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            )

        // Debounce immediate syncs (like cart updates) to prevent rapid worker replacement
        if (immediate && !fetch) {
            syncRequestBuilder.setInitialDelay(5, TimeUnit.SECONDS)
        }

        val syncRequest = syncRequestBuilder.build()

        // Use REPLACE if it's a fetch request or an immediate request, 
        // otherwise KEEP to avoid cancelling running syncs.
        val policy = if (fetch || immediate) ExistingWorkPolicy.REPLACE else ExistingWorkPolicy.KEEP
        
        workManager.enqueueUniqueWork(
            "sync_${type.name.lowercase()}",
            policy,
            syncRequest
        )
    }
}
