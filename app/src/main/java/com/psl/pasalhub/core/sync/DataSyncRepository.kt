package com.psl.pasalhub.core.sync

import android.util.Log
import com.psl.pasalhub.core.database.data.CartDao
import com.psl.pasalhub.core.database.data.CartEntity
import com.psl.pasalhub.core.database.data.FavoriteDao
import com.psl.pasalhub.core.database.data.FavoriteEntity
import com.psl.pasalhub.core.database.data.OrderDao
import com.psl.pasalhub.core.database.data.OrderEntity
import com.psl.pasalhub.core.database.data.UserDao
import com.psl.pasalhub.core.database.data.UserEntity
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.first
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataSyncRepository @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val cartDao: CartDao,
    private val favoriteDao: FavoriteDao,
    private val orderDao: OrderDao,
    private val userDao: UserDao
) {
    suspend fun syncCart(fetchFromRemote: Boolean = true) {
        val user = supabaseClient.auth.currentUserOrNull() ?: return

        Log.d("DataSyncRepository", "Cart sync (fetch=$fetchFromRemote) for user: ${user.email}")

        try {
            // 1. Fetch Remote State
            val remoteCart = supabaseClient.postgrest["cart"]
                .select {
                    filter {
                        eq("user_id", user.id)
                    }
                }
                .decodeList<RemoteCartItem>()

            if (fetchFromRemote) {
                // 2. Fetch Local State
                val localCart = cartDao.getCartItems().first()
                val localProductIds = localCart.map { it.productId }.toSet()

                // 3. Remote -> Local (Additive Merge)
                val itemsToRestore = remoteCart.filter { it.product_id !in localProductIds }
                if (itemsToRestore.isNotEmpty()) {
                    Log.d(
                        "DataSyncRepository",
                        "Restoring ${itemsToRestore.size} remote items to local cart"
                    )
                    itemsToRestore.forEach { remoteItem ->
                        cartDao.addToCart(
                            CartEntity(
                                id = remoteItem.id,
                                userId = user.id,
                                productId = remoteItem.product_id,
                                title = remoteItem.title,
                                price = remoteItem.price,
                                description = remoteItem.description,
                                category = remoteItem.category,
                                image = remoteItem.image,
                                quantity = remoteItem.quantity,
                                seller = remoteItem.seller,
                                isSynced = true,
                                updatedAt = remoteItem.updated_at,
                                createdAt = remoteItem.created_at
                            )
                        )
                    }
                }
            }

            // 4. Local -> Remote (Reconcile Deletions & Upsert)
            val updatedLocalCart = cartDao.getCartItems().first()
            val localProductIds = updatedLocalCart.map { it.productId }.toSet()

            // Identify items in Supabase that are no longer in Local Cart
            val itemsToDelete = remoteCart.filter { it.product_id !in localProductIds }
            if (itemsToDelete.isNotEmpty()) {
                Log.d(
                    "DataSyncRepository",
                    "Deleting ${itemsToDelete.size} items from Supabase cart"
                )
                val productIdsToDelete = itemsToDelete.map { it.product_id }
                supabaseClient.postgrest["cart"].delete {
                    filter {
                        eq("user_id", user.id)
                        isIn("product_id", productIdsToDelete)
                    }
                }
            }

            val remoteData = updatedLocalCart.map {
                RemoteCartItem(
                    id = it.id,
                    user_id = user.id,
                    product_id = it.productId,
                    title = it.title,
                    price = it.price,
                    description = it.description,
                    category = it.category,
                    image = it.image,
                    quantity = it.quantity,
                    seller = it.seller,
                    is_synced = it.isSynced,
                    updated_at = it.updatedAt,
                    created_at = it.createdAt
                )
            }

            if (remoteData.isNotEmpty()) {
                Log.d("DataSyncRepository", "Upserting ${remoteData.size} items to Supabase cart")
                supabaseClient.postgrest["cart"].upsert(remoteData) {
                    onConflict = "user_id,product_id"
                }
            }

            userDao.updateSyncStatus(user.id, true)
        } catch (e: Exception) {
            Log.e("DataSyncRepository", "Cart sync failed: ${e.message}", e)
            throw e
        }
    }

    suspend fun syncFavorites(fetchFromRemote: Boolean = true) {
        val user = supabaseClient.auth.currentUserOrNull() ?: return

        Log.d(
            "DataSyncRepository",
            "Favorites sync (fetch=$fetchFromRemote) for user: ${user.email}"
        )

        try {
            // 1. Fetch Remote State
            val remoteFavorites = supabaseClient.postgrest["favourites"]
                .select {
                    filter {
                        eq("user_id", user.id)
                    }
                }
                .decodeList<RemoteFavoriteItem>()

            if (fetchFromRemote) {
                // 2. Fetch Local State
                val localFavorites = favoriteDao.getFavorites().first()
                val localProductIds = localFavorites.map { it.productId }.toSet()

                // 3. Remote -> Local
                val itemsToRestore = remoteFavorites.filter { it.product_id !in localProductIds }
                if (itemsToRestore.isNotEmpty()) {
                    Log.d("DataSyncRepository", "Restoring ${itemsToRestore.size} remote favorites")
                    itemsToRestore.forEach { remoteFav ->
                        favoriteDao.addFavorite(
                            FavoriteEntity(
                                productId = remoteFav.product_id,
                                userId = user.id,
                                addedAt = remoteFav.added_at,
                                updatedAt = remoteFav.updated_at
                            )
                        )
                    }
                }
            }

            // 4. Local -> Remote (Reconcile Deletions & Upsert)
            val updatedLocalFavorites = favoriteDao.getFavorites().first()
            val localProductIds = updatedLocalFavorites.map { it.productId }.toSet()

            // Identify items in Supabase that are no longer in Local Favorites
            val itemsToDelete = remoteFavorites.filter { it.product_id !in localProductIds }
            if (itemsToDelete.isNotEmpty()) {
                Log.d(
                    "DataSyncRepository",
                    "Deleting ${itemsToDelete.size} favorites from Supabase"
                )
                val productIdsToDelete = itemsToDelete.map { it.product_id }
                supabaseClient.postgrest["favourites"].delete {
                    filter {
                        eq("user_id", user.id)
                        isIn("product_id", productIdsToDelete)
                    }
                }
            }

            val remoteData = updatedLocalFavorites.map {
                RemoteFavoriteItem(
                    user_id = user.id,
                    product_id = it.productId,
                    added_at = it.addedAt,
                    updated_at = it.updatedAt
                )
            }

            if (remoteData.isNotEmpty()) {
                Log.d("DataSyncRepository", "Upserting ${remoteData.size} favorites to Supabase")
                supabaseClient.postgrest["favourites"].upsert(remoteData) {
                    onConflict = "user_id,product_id"
                }
            }

            userDao.updateFavoriteSyncStatus(user.id, true)
        } catch (e: Exception) {
            Log.e("DataSyncRepository", "Favorites sync failed: ${e.message}", e)
            throw e
        }
    }

    suspend fun syncOrders(fetchFromRemote: Boolean = true) {
        val user = supabaseClient.auth.currentUserOrNull() ?: return

        Log.d("DataSyncRepository", "Order sync (fetch=$fetchFromRemote) for user: ${user.email}")

        try {
            if (fetchFromRemote) {
                // 1. Fetch Remote State
                val remoteOrders = supabaseClient.postgrest["orders"]
                    .select {
                        filter {
                            eq("user_id", user.id)
                        }
                    }
                    .decodeList<RemoteOrderItem>()

                // 2. Fetch Local State
                val localOrders = orderDao.getOrders().first()
                val localOrderDates = localOrders.map { it.date }.toSet()

                // 3. Remote -> Local
                val ordersToRestore = remoteOrders.filter { it.date !in localOrderDates }
                if (ordersToRestore.isNotEmpty()) {
                    Log.d("DataSyncRepository", "Restoring ${ordersToRestore.size} remote orders")
                    ordersToRestore.forEach { remoteOrder ->
                        orderDao.insertOrder(
                            OrderEntity(
                                orderId = remoteOrder.orderId,
                                date = remoteOrder.date,
                                totalAmount = remoteOrder.totalAmount,
                                itemsSummary = remoteOrder.itemsSummary,
                                status = remoteOrder.status,
                                quantity = remoteOrder.quantity,
                                price = remoteOrder.price,
                                address = remoteOrder.address ?: "",
                                seller = remoteOrder.seller,
                                cancelledReason = remoteOrder.cancelled_reason,
                                rating = remoteOrder.rating,
                                review = remoteOrder.review,
                                progress = remoteOrder.progress,
                                isPaused = remoteOrder.isPaused,
                                isSynced = remoteOrder.isSynced,
                                updatedAt = remoteOrder.updated_at
                            )
                        )
                    }
                }
            }

            // 4. Local -> Remote
            val updatedLocalOrders = orderDao.getOrders().first()
            val remoteData = updatedLocalOrders.map {
                RemoteOrderItem(
                    orderId = it.orderId,
                    user_id = user.id,
                    date = it.date,
                    totalAmount = it.totalAmount,
                    itemsSummary = it.itemsSummary,
                    status = it.status,
                    quantity = it.quantity,
                    price = it.price,
                    address = it.address,
                    seller = it.seller,
                    cancelled_reason = it.cancelledReason,
                    rating = it.rating,
                    review = it.review,
                    progress = it.progress,
                    isPaused = it.isPaused,
                    isSynced = it.isSynced,
                    updated_at = it.updatedAt
                )
            }

            if (remoteData.isNotEmpty()) {
                Log.d("DataSyncRepository", "Upserting ${remoteData.size} orders to Supabase")
                supabaseClient.postgrest["orders"].upsert(remoteData) {
                    onConflict = "user_id,date"
                }
            }
        } catch (e: Exception) {
            Log.e("DataSyncRepository", "Order sync failed: ${e.message}", e)
            throw e
        }
    }

    suspend fun syncProfile(fetchFromRemote: Boolean = true) {
        val user = supabaseClient.auth.currentUserOrNull() ?: return
        Log.d("DataSyncRepository", "Profile sync (fetch=$fetchFromRemote) for user: ${user.email}")

        try {
            if (fetchFromRemote) {
                // Fetch profile from Postgrest
                val profile = supabaseClient.postgrest["profiles"]
                    .select(columns = Columns.ALL) {
                        filter {
                            eq("id", user.id)
                        }
                    }
                    .decodeSingle<Map<String, JsonElement?>>()

                val name = profile["name"]?.jsonPrimitive?.content ?: ""
                val dob = profile["date_of_birth"]?.jsonPrimitive?.content ?: ""
                val address = profile["address"]?.jsonPrimitive?.content ?: ""
                val isComplete =
                    profile["is_profile_complete"]?.jsonPrimitive?.booleanOrNull ?: false
                val isOnboardingDone =
                    profile["onboarding_done"]?.jsonPrimitive?.booleanOrNull ?: false

                val existingLocalUser = userDao.getUserById(user.id)
                val hasSynced = existingLocalUser?.hasSyncedCart ?: false
                val hasSyncedFav = existingLocalUser?.hasSyncedFavorites ?: false

                // More resilient Google user detection
                val providerFromAppMetadata =
                    user.appMetadata?.get("provider")?.jsonPrimitive?.content
                val providerFromUserMetadata = user.userMetadata?.get("iss")?.jsonPrimitive?.content
                val isGoogle = providerFromAppMetadata == "google" ||
                        providerFromUserMetadata?.contains("google") == true ||
                        user.identities?.any { it.provider == "google" } == true

                val userEntity = UserEntity(
                    id = user.id,
                    email = user.email ?: "",
                    name = name,
                    dateOfBirth = dob,
                    address = address,
                    isRemembered = true,
                    isGoogleUser = isGoogle,
                    profileImage = user.userMetadata?.get("avatar_url")?.toString()
                        ?.removeSurrounding("\""),
                    isProfileComplete = isComplete,
                    hasSyncedCart = hasSynced,
                    hasSyncedFavorites = hasSyncedFav,
                    isOnboardingDone = isOnboardingDone
                )
                userDao.insertUser(userEntity)
                Log.d("DataSyncRepository", "Successfully synced profile from remote.")
            } else {
                // Local -> Remote (Upsert local profile to Supabase)
                val localUser = userDao.getUserById(user.id) ?: return
                val profileData = kotlinx.serialization.json.buildJsonObject {
                    put("id", localUser.id)
                    put("email", localUser.email)
                    put("name", localUser.name)
                    put("address", localUser.address)
                    put("date_of_birth", localUser.dateOfBirth)
                    put("is_profile_complete", localUser.isProfileComplete)
                    put("onboarding_done", localUser.isOnboardingDone)
                }
                supabaseClient.postgrest["profiles"].upsert(profileData)
                Log.d("DataSyncRepository", "Successfully pushed profile to remote.")
            }
        } catch (e: Exception) {
            Log.e("DataSyncRepository", "Profile sync failed: ${e.message}", e)
            if (fetchFromRemote) {
                // Fallback for new users or offline login
                val existingLocalUser = userDao.getUserById(user.id)
                val hasSynced = existingLocalUser?.hasSyncedCart ?: false
                val hasSyncedFav = existingLocalUser?.hasSyncedFavorites ?: false

                // More resilient Google user detection
                val providerFromAppMetadata =
                    user.appMetadata?.get("provider")?.jsonPrimitive?.content
                val providerFromUserMetadata = user.userMetadata?.get("iss")?.jsonPrimitive?.content
                val isGoogle = providerFromAppMetadata == "google" ||
                        providerFromUserMetadata?.contains("google") == true ||
                        user.identities?.any { it.provider == "google" } == true

                val userEntity = UserEntity(
                    id = user.id,
                    email = user.email ?: "",
                    name = user.userMetadata?.get("full_name")?.jsonPrimitive?.content ?: "",
                    dateOfBirth = "",
                    address = "",
                    isRemembered = true,
                    isGoogleUser = isGoogle,
                    profileImage = user.userMetadata?.get("avatar_url")?.jsonPrimitive?.content,
                    isProfileComplete = false,
                    hasSyncedCart = hasSynced,
                    hasSyncedFavorites = hasSyncedFav
                )
                userDao.insertUser(userEntity)
                Log.d("DataSyncRepository", "Using fallback profile local data.")
            }
            throw e
        }
    }

    @Serializable
    data class RemoteCartItem(
        val id: String,
        val user_id: String,
        val product_id: Int,
        val title: String,
        val price: Double,
        val description: String,
        val category: String,
        val image: String,
        val quantity: Int,
        val seller: String,
        val is_synced: Boolean,
        @Serializable(with = TimestampSerializer::class)
        val updated_at: Long,
        @Serializable(with = TimestampSerializer::class)
        val created_at: Long
    )

    @Serializable
    data class RemoteFavoriteItem(
        val user_id: String,
        val product_id: Int,
        @Serializable(with = TimestampSerializer::class)
        val added_at: Long,
        @Serializable(with = TimestampSerializer::class)
        val updated_at: Long
    )

    @Serializable
    data class RemoteOrderItem(
        @SerialName("orderId") val orderId: Int,
        val user_id: String,
        val date: Long,
        val totalAmount: Double,
        val itemsSummary: String,
        val status: String,
        val quantity: Int,
        val price: Double,
        val address: String?,
        val seller: String,
        @SerialName("cancelled_reason") val cancelled_reason: String?,
        val rating: Int,
        val review: String?,
        val progress: Int,
        val isPaused: Boolean,
        val isSynced: Boolean,
        @Serializable(with = TimestampSerializer::class)
        val updated_at: Long
    )
}
