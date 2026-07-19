package com.psl.pasalhub.dashboard.profile.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.psl.pasalhub.core.database.data.FavoriteDao
import com.psl.pasalhub.core.database.data.FavoriteEntity
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
    private val favoriteDao: FavoriteDao
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val user = supabaseClient.auth.currentUserOrNull() ?: return Result.failure()

        return try {
            // 1. Fetch Remote Favorites
            val remoteFavorites = supabaseClient.postgrest["favorites"]
                .select {
                    filter {
                        eq("user_id", user.id)
                    }
                }
                .decodeList<RemoteFavoriteItem>()

            // 2. Update Local Favorites
            if (remoteFavorites.isNotEmpty()) {
                remoteFavorites.forEach { remoteFav ->
                    favoriteDao.addFavorite(FavoriteEntity(remoteFav.product_id, user.id))
                }
            }

            // 3. Sync Local Favorites to Remote
            val localFavorites = favoriteDao.getFavorites().first()
            val remoteData = localFavorites.map {
                mapOf(
                    "user_id" to user.id,
                    "product_id" to it.productId
                )
            }

            if (remoteData.isNotEmpty()) {
                supabaseClient.postgrest["favorites"].upsert(remoteData) {
                    onConflict = "user_id, product_id"
                }
            }
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    @kotlinx.serialization.Serializable
    data class RemoteFavoriteItem(
        val user_id: String,
        val product_id: Int
    )
}
