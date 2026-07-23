package com.psl.pasalhub.dashboard.order.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.psl.pasalhub.core.database.data.AppDatabase
import com.psl.pasalhub.core.database.data.OrderEntity
import com.psl.pasalhub.core.database.data.OrderRemoteKeys
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)
class OrderRemoteMediator @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val database: AppDatabase
) : RemoteMediator<Int, OrderEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, OrderEntity>
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> {
                val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                remoteKeys?.nextKey?.minus(1) ?: 0
            }

            LoadType.PREPEND -> {
                val remoteKeys = getRemoteKeyForFirstItem(state)
                val prevKey = remoteKeys?.prevKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                prevKey
            }

            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state)
                val nextKey = remoteKeys?.nextKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                nextKey
            }
        }

        try {
            val user = supabaseClient.auth.currentUserOrNull() ?: return MediatorResult.Error(
                Exception("User not logged in")
            )

            val from = page * state.config.pageSize
            val to = from + state.config.pageSize - 1

            val orders = supabaseClient.postgrest["orders"]
                .select(columns = Columns.ALL) {
                    filter {
                        eq("user_id", user.id)
                    }
                    order("date", Order.DESCENDING)
                    range(from.toLong(), to.toLong())
                }
                .decodeList<OrderEntity>()

            val endOfPaginationReached = orders.isEmpty()

            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    database.remoteKeysDao().clearOrderRemoteKeys()
                    // We might not want to clear all orders if we have local-only state (though sync usually handles it)
                    // For now, let's keep it simple.
                }
                val prevKey = if (page == 0) null else page - 1
                val nextKey = if (endOfPaginationReached) null else page + 1
                val keys = orders.map {
                    OrderRemoteKeys(orderId = it.orderId, prevKey = prevKey, nextKey = nextKey)
                }
                database.remoteKeysDao().insertAllOrderKeys(keys)
                // Order insertion logic might be more complex if we need to preserve local changes
                // But for list pagination, remote is the source of truth for history.
                orders.forEach { database.orderDao().insertOrder(it) }
            }
            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (exception: Exception) {
            return MediatorResult.Error(exception)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, OrderEntity>): OrderRemoteKeys? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()
            ?.let { order ->
                database.remoteKeysDao().getOrderRemoteKeysById(order.orderId)
            }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, OrderEntity>): OrderRemoteKeys? {
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()
            ?.let { order ->
                database.remoteKeysDao().getOrderRemoteKeysById(order.orderId)
            }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(state: PagingState<Int, OrderEntity>): OrderRemoteKeys? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.orderId?.let { orderId ->
                database.remoteKeysDao().getOrderRemoteKeysById(orderId)
            }
        }
    }
}
