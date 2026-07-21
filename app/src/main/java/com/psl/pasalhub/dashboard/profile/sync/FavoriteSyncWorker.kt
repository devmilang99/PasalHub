package com.psl.pasalhub.dashboard.profile.sync

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.psl.pasalhub.core.database.data.FavoriteDao
import com.psl.pasalhub.core.database.data.FavoriteEntity
import com.psl.pasalhub.core.database.data.UserDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.first

@HiltWorker
class FavoriteSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val supabaseClient: SupabaseClient,
    private val favoriteDao: FavoriteDao,
    private val userDao: UserDao
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("FavoriteSyncWorker", "Starting favorites sync...")
        val sessionUser = supabaseClient.auth.currentUserOrNull()

        if (sessionUser == null) {
            Log.w("FavoriteSyncWorker", "No Supabase session found. Skipping favorites sync.")
            return Result.failure()
        }

        val localUser = userDao.getUser().first()
        if (localUser == null || localUser.id != sessionUser.id) {
            Log.w("FavoriteSyncWorker", "Local user mismatch or missing. Skipping favorites sync.")
            return Result.failure()
        }

        return try {
            // 1. Initial Restore from Supabase if never synced
            if (!localUser.hasSyncedFavorites) {
                Log.d("FavoriteSyncWorker", "First sync: Restoring from Supabase...")
                val remoteFavorites = supabaseClient.postgrest["favorites"]
                    .select {
                        filter {
                            eq("user_id", sessionUser.id)
                        }
                    }
                    .decodeList<RemoteFavoriteItem>()

                remoteFavorites.forEach { remoteFav ->
                    favoriteDao.addFavorite(
                        FavoriteEntity(
                            productId = remoteFav.product_id,
                            userId = sessionUser.id,
                            updatedAt = remoteFav.updated_at
                        )
                    )
                }
                userDao.updateFavoriteSyncStatus(sessionUser.id, true)
                Log.d(
                    "FavoriteSyncWorker",
                    "Restore complete. Restored ${remoteFavorites.size} favorites."
                )
            }

            // 2. Local State is Source of Truth - Sync Local -> Remote
            val localFavorites = favoriteDao.getFavorites().first()
            val localProductIds = localFavorites.map { it.productId }.toSet()

            // A. Cleanup Remote (Remove favorites that are no longer in local database)
            val currentRemoteFavorites = supabaseClient.postgrest["favorites"]
                .select {
                    filter {
                        eq("user_id", sessionUser.id)
                    }
                }
                .decodeList<RemoteFavoriteItem>()

            val itemsToRemove = currentRemoteFavorites.filter { it.product_id !in localProductIds }

            if (itemsToRemove.isNotEmpty()) {
                Log.d(
                    "FavoriteSyncWorker",
                    "Removing ${itemsToRemove.size} favorites from Supabase..."
                )
                itemsToRemove.forEach { item ->
                    supabaseClient.postgrest["favorites"].delete {
                        filter {
                            eq("user_id", sessionUser.id)
                            eq("product_id", item.product_id)
                        }
                    }
                }
            }

            // B. Push New to Remote
            val remoteData = localFavorites.map {
                RemoteFavoriteItem(
                    user_id = sessionUser.id,
                    product_id = it.productId,
                    updated_at = it.updatedAt
                )
            }

            if (remoteData.isNotEmpty()) {
                Log.d("FavoriteSyncWorker", "Upserting ${remoteData.size} favorites to Supabase...")
                supabaseClient.postgrest["favorites"].upsert(remoteData) {
                    onConflict = "user_id,product_id"
                }
            }

            Log.d("FavoriteSyncWorker", "Favorites sync successful!")
            Result.success()
        } catch (e: Exception) {
            Log.e("FavoriteSyncWorker", "Favorites sync failed: ${e.message}", e)
            Result.retry()
        }
    }

    @kotlinx.serialization.Serializable
    data class RemoteFavoriteItem(
        val user_id: String,
        val product_id: Int,
        val updated_at: Long = System.currentTimeMillis()
    )
}
