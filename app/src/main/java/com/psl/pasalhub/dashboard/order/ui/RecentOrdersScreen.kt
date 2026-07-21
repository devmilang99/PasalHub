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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.psl.pasalhub.dashboard.order.viewmodel.OrderViewModel
import java.text.SimpleDateFormat

@Composable
fun RecentOrdersScreen(viewModel: OrderViewModel) {
    val orders by viewModel.ordersState.collectAsStateWithLifecycle()
    val sdf = SimpleDateFormat("MMM dd, yyyy - HH:mm", LocalLocale.current.platformLocale)

    val bgColor = MaterialTheme.colorScheme.background
    val mutedTextColor = MaterialTheme.colorScheme.onSurfaceVariant

    val filteredOrders =
        orders.filter { it.status in listOf("Placing", "Placed", "Packaging", "Sent for Delivery") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(8.dp)
    ) {
        if (filteredOrders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.AutoMirrored.Outlined.ReceiptLong,
                        contentDescription = "Empty orders list icon",
                        modifier = Modifier.size(72.dp),
                        tint = mutedTextColor.copy(alpha = 0.35f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Recent Orders",
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
                items(filteredOrders, key = { it.orderId }) { order ->
                    ModernOrderCard(
                        order = order,
                        sdf = sdf,
                        tabType = "Recent",
                        onCancel = { id: Int, reason: String -> viewModel.cancelOrder(id, reason) },
                        onRate = { id: Int, rating: Int, review: String ->
                            viewModel.completeOrder(
                                id,
                                rating,
                                review
                            )
                        },
                        onSetPause = { id: Int, isPaused: Boolean ->
                            viewModel.setOrderPause(
                                id,
                                isPaused
                            )
                        }
                    )
                }
            }
        }
    }
}
