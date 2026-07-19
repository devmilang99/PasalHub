package com.psl.pasalhub.dashboard.order.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.psl.pasalhub.dashboard.order.viewmodel.OrderViewModel
import java.text.SimpleDateFormat

@Composable
fun RecentOrdersScreen(viewModel: OrderViewModel) {
    val orders by viewModel.ordersState.collectAsState()
    val sdf = SimpleDateFormat("MMM dd, yyyy - HH:mm", LocalLocale.current.platformLocale)

    val bgColor = MaterialTheme.colorScheme.background
    val mutedTextColor = MaterialTheme.colorScheme.onSurfaceVariant

    val filteredOrders =
        orders.filter { it.status in listOf("Placing", "Placed", "Packaging", "Sent for Delivery") }

    // Logic to detect when the cancellation window closes
    val previousStatuses = remember { mutableStateMapOf<Int, String>() }
    LaunchedEffect(filteredOrders) {
        filteredOrders.forEach { order ->
            val prevStatus = previousStatuses[order.orderId]
            if (prevStatus == "Placing" && order.status == "Placed") {
                viewModel.emitNotification("Order #ORD-${1000 + order.orderId} confirmed! Tracking started.")
            }
            previousStatuses[order.orderId] = order.status
        }
    }

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
