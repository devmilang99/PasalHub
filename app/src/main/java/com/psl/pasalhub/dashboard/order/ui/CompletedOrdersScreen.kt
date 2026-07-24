package com.psl.pasalhub.dashboard.order.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.psl.pasalhub.dashboard.order.viewmodel.OrderViewModel
import java.text.SimpleDateFormat

@Composable
fun CompletedOrdersScreen(viewModel: OrderViewModel) {
    val orders = viewModel.completedOrdersPaged.collectAsLazyPagingItems()
    val sdf = SimpleDateFormat("MMM dd, yyyy - HH:mm", LocalLocale.current.platformLocale)

    val bgColor = MaterialTheme.colorScheme.background
    val mutedTextColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(8.dp)
    ) {
        if (orders.loadState.refresh is LoadState.NotLoading && orders.itemCount == 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.ReceiptLong,
                        contentDescription = "Empty orders list icon",
                        modifier = Modifier.size(72.dp),
                        tint = mutedTextColor.copy(alpha = 0.35f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Completed Orders",
                        fontWeight = FontWeight.Bold,
                        color = mutedTextColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Orders in this pipeline will populate dynamically.",
                        fontSize = 12.sp,
                        color = mutedTextColor.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                if (orders.loadState.refresh is LoadState.Loading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }

                items(
                    count = orders.itemCount,
                    key = orders.itemKey { it.orderId }
                ) { index ->
                    val order = orders[index]
                    if (order != null) {
                        ModernOrderCard(
                            order = order,
                            sdf = sdf,
                            tabType = "Completed",
                            onCancel = { id: Int, reason: String ->
                                viewModel.cancelOrder(
                                    id,
                                    reason
                                )
                            },
                            onRate = { id: Int, rating: Int, review: String ->
                                viewModel.completeOrder(id, rating, review)
                            },
                            onSetPause = { id: Int, isPaused: Boolean ->
                                viewModel.setOrderPause(id, isPaused)
                            }
                        )
                    }
                }

                if (orders.loadState.append is LoadState.Loading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }
        }
    }
}
