package com.example.dashboard.order.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.core.database.data.OrderEntity
import com.example.dashboard.order.viewmodel.OrderViewModel
import com.example.ui.screens.parseItemsSummary
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun OrdersScreen(viewModel: OrderViewModel) {
    val orders by viewModel.ordersState.collectAsState()
    val isDark by viewModel.isDarkTheme.collectAsState()
    val sdf = SimpleDateFormat("MMM dd, yyyy - HH:mm", Locale.getDefault())
    var selectedSubTab by remember { mutableStateOf("Recent") }
    var orderToRate by remember { mutableStateOf<OrderEntity?>(null) }

    orderToRate?.let { order ->
        RateAndReviewDialog(
            order = order,
            viewModel = viewModel,
            onDismiss = { orderToRate = null },
            isDark = isDark
        )
    }

    val bgColor = if (isDark) Color.Black else Color(0xFFF8F9FA)
    val textColor = if (isDark) Color.White else Color(0xFF212529)
    val mutedTextColor = if (isDark) Color.Gray else Color(0xFF6C757D)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(16.dp)
    ) {
        Text(
            text = "My Orders",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = textColor,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            shape = RoundedCornerShape(16.dp),
            color = if (isDark) Color(0xFF161618) else Color(0xFFE9ECEF),
            border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("Recent", "Completed", "Cancelled").forEach { tab ->
                    val isSelected = selectedSubTab == tab
                    val tabColor = when (tab) {
                        "Recent" -> Color(0xFFFF9800)
                        "Completed" -> Color(0xFF4CAF50)
                        "Cancelled" -> Color(0xFFF44336)
                        else -> Color.Gray
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) tabColor else Color.Transparent)
                            .clickable { selectedSubTab = tab }
                            .testTag("orders_tab_selector_$tab"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else mutedTextColor
                        )
                    }
                }
            }
        }

        val filteredOrders = when (selectedSubTab) {
            "Recent" -> orders.filter { it.status in listOf("Placing", "Placed", "Packaging", "Sent for Delivery") }
            "Completed" -> orders.filter { it.status in listOf("Delivered", "Completed") }
            "Cancelled" -> orders.filter { it.status == "Cancelled" }
            else -> emptyList()
        }

        if (filteredOrders.isEmpty()) {
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
                        text = "No $selectedSubTab Orders",
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
                    if (order.status == "Placing" && selectedSubTab == "Recent") {
                        PlacingOrderCountdownCard(order = order, viewModel = viewModel, sdf = sdf, isDark = isDark)
                    } else {
                        ModernOrderCard(
                            order = order,
                            sdf = sdf,
                            tabType = selectedSubTab,
                            onRateClick = { orderToRate = order },
                            isDark = isDark
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PlacingOrderCountdownCard(
    order: OrderEntity,
    viewModel: OrderViewModel,
    sdf: SimpleDateFormat,
    isDark: Boolean
) {
    var timeLeft by remember { mutableStateOf(10) }
    var isPaused by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var customReason by remember { mutableStateOf("") }
    val reasons = listOf("Changed my mind", "Shipping is too slow", "Incorrect address details", "Found a better deal", "Other")
    var selectedReason by remember { mutableStateOf(reasons[0]) }

    val cardColor = if (isDark) Color(0xFF1B1B1D) else Color.White
    val textColor = if (isDark) Color.White else Color(0xFF212529)
    val mutedTextColor = if (isDark) Color.Gray else Color(0xFF6C757D)

    LaunchedEffect(isPaused) {
        if (!isPaused) {
            while (timeLeft > 0) {
                kotlinx.coroutines.delay(1000)
                timeLeft--
            }
            viewModel.updateOrderStatus(order.orderId, "Placed")
        }
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = {
                showCancelDialog = false
                isPaused = false
            },
            title = { Text("Cancel Order Reasons", fontWeight = FontWeight.Bold, color = textColor) },
            containerColor = cardColor,
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Please select or provide the reason why you wish to cancel this premium order:",
                        fontSize = 13.sp,
                        color = mutedTextColor
                    )
                    reasons.forEach { reason ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedReason = reason }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedReason == reason,
                                onClick = { selectedReason = reason },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Color(0xFFE57373),
                                    unselectedColor = mutedTextColor
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = reason, fontSize = 14.sp, color = textColor)
                        }
                    }
                    if (selectedReason == "Other") {
                        OutlinedTextField(
                            value = customReason,
                            onValueChange = { customReason = it },
                            placeholder = { Text("Write cancellation reason here...", fontSize = 12.sp, color = mutedTextColor) },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = textColor,
                                unfocusedTextColor = textColor,
                                focusedBorderColor = Color(0xFFE57373),
                                unfocusedBorderColor = mutedTextColor.copy(alpha = 0.4f)
                            )
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val finalReason = if (selectedReason == "Other") customReason else selectedReason
                        viewModel.cancelOrder(order.orderId, if (finalReason.isBlank()) "No cancellation reason selected" else finalReason)
                        showCancelDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373), contentColor = Color.White)
                ) {
                    Text("Cancel Order")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showCancelDialog = false
                        isPaused = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = mutedTextColor)
                ) {
                    Text("Resume")
                }
            }
        )
    }

    val parsedItems = remember(order.itemsSummary) { parseItemsSummary(order.itemsSummary) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("placing_countdown_card_${order.orderId}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = BorderStroke(1.5.dp, Color(0xFFD48A37))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier.size(36.dp).border(2.dp, Color(0xFFE57373), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = timeLeft.toString(), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE57373))
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = Color(0xFFE57373), modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "CANCELLATION WINDOW", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE57373))
                        }
                        Text(text = "Hurry! Window closing soon...", fontSize = 12.sp, color = textColor)
                    }
                }

                Button(
                    onClick = {
                        isPaused = true
                        showCancelDialog = true
                    },
                    modifier = Modifier.height(34.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color(0xFFE57373)),
                    border = BorderStroke(1.dp, Color(0xFFE57373)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                ) {
                    Text(text = "CANCEL", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            HorizontalDivider(color = mutedTextColor.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "#ORD-${1000 + order.orderId}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = textColor)
                    Text(text = sdf.format(Date(order.date)), fontSize = 11.sp, color = mutedTextColor)
                }

                Box(
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Color(0xFF2C2415)).padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(text = "PENDING", color = Color(0xFFFFB74D), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                parsedItems.forEach { item ->
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(44.dp).clip(RoundedCornerShape(8.dp)).background(item.bgColor).padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(model = item.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(text = item.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Storefront, contentDescription = null, tint = mutedTextColor, modifier = Modifier.size(10.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Eco Store", fontSize = 11.sp, color = mutedTextColor)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFFFB74D), modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "DELIVERY ADDRESS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFB74D), letterSpacing = 1.sp)
                }
                Text(text = order.address, fontSize = 12.sp, color = textColor.copy(alpha = 0.9f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Column {
                    Text(text = "Total Amount", fontSize = 11.sp, color = mutedTextColor)
                    Text(text = "Rs. ${order.totalAmount.toInt()}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFFFFB74D))
                }
                Text(text = "${order.quantity} items", fontSize = 12.sp, color = mutedTextColor, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun RateAndReviewDialog(order: OrderEntity, viewModel: OrderViewModel, onDismiss: () -> Unit, isDark: Boolean) {
    var rating by remember { mutableStateOf(5) }
    var reviewText by remember { mutableStateOf("") }

    val cardColor = if (isDark) Color(0xFF1B1B1D) else Color.White
    val textColor = if (isDark) Color.White else Color(0xFF212529)
    val mutedTextColor = if (isDark) Color.Gray else Color(0xFF6C757D)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rate & Review Order", fontWeight = FontWeight.Bold, color = textColor) },
        containerColor = cardColor,
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Provide your ratings and detailed feedback for Order #AUR-${1000 + order.orderId} from ${order.seller}:",
                    fontSize = 13.sp, color = mutedTextColor, textAlign = TextAlign.Center
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 8.dp)) {
                    for (i in 1..5) {
                        val isSelected = i <= rating
                        Icon(
                            imageVector = if (isSelected) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = null,
                            tint = if (isSelected) Color(0xFFFFB200) else mutedTextColor.copy(alpha = 0.4f),
                            modifier = Modifier.size(34.dp).clickable { rating = i }
                        )
                    }
                }

                OutlinedTextField(
                    value = reviewText,
                    onValueChange = { reviewText = it },
                    placeholder = { Text("Write your review (Optional)...", fontSize = 13.sp, color = mutedTextColor) },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor, unfocusedTextColor = textColor,
                        focusedBorderColor = Color(0xFF4CAF50), unfocusedBorderColor = mutedTextColor.copy(alpha = 0.4f)
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { viewModel.completeOrder(order.orderId, rating, reviewText); onDismiss() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50), contentColor = Color.White)
            ) {
                Text("Submit Review")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = mutedTextColor)) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ModernOrderCard(order: OrderEntity, sdf: SimpleDateFormat, tabType: String, onRateClick: () -> Unit, isDark: Boolean) {
    val parsedItems = remember(order.itemsSummary) { parseItemsSummary(order.itemsSummary) }

    val progressPercent = when (order.status) {
        "Placed" -> 10
        "Packaging", "Packing" -> 30
        "Sent for Delivery", "On Way" -> 80
        "Delivered", "Completed" -> 100
        else -> 0
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progressPercent / 100f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "DeliveryProgressAnimation"
    )

    val displayStatus = when (order.status) {
        "Placed", "Packaging", "Packing" -> "PACKING"
        "Sent for Delivery", "On Way" -> "ON WAY"
        "Delivered" -> "DELIVERED"
        "Completed" -> "COMPLETED"
        "Cancelled" -> "CANCELLED"
        else -> order.status.uppercase()
    }

    val statusBgColor = when (order.status) {
        "Placed", "Packaging", "Packing" -> if (isDark) Color(0xFF1E293B) else Color(0xFFE0F2F1)
        "Sent for Delivery", "On Way" -> if (isDark) Color(0xFF142B23) else Color(0xFFE8F5E9)
        "Delivered", "Completed" -> if (isDark) Color(0xFF1E2E20) else Color(0xFFF1F8E9)
        "Cancelled" -> if (isDark) Color(0xFF2E1B1B) else Color(0xFFFFEBEE)
        else -> if (isDark) Color(0xFF2C2D30) else Color(0xFFECEFF1)
    }

    val statusTextColor = when (order.status) {
        "Placed", "Packaging", "Packing" -> if (isDark) Color(0xFF38BDF8) else Color(0xFF00796B)
        "Sent for Delivery", "On Way" -> if (isDark) Color(0xFF4ADE80) else Color(0xFF2E7D32)
        "Delivered", "Completed" -> if (isDark) Color(0xFF4ADE80) else Color(0xFF33691E)
        "Cancelled" -> if (isDark) Color(0xFFF87171) else Color(0xFFC62828)
        else -> if (isDark) Color.White else Color(0xFF455A64)
    }

    val cardColor = if (isDark) Color(0xFF1B1B1D) else Color.White
    val textColor = if (isDark) Color.White else Color(0xFF212529)
    val mutedTextColor = if (isDark) Color.Gray else Color(0xFF6C757D)
    val borderColor = if (isDark) Color.Gray.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.05f)

    Card(
        modifier = Modifier.fillMaxWidth().testTag("order_card_${order.orderId}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(text = "#ORD-${1000 + order.orderId}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = textColor)
                    Text(text = sdf.format(Date(order.date)), fontSize = 11.sp, color = mutedTextColor)
                }

                Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(statusBgColor).padding(horizontal = 10.dp, vertical = 6.dp)) {
                    Text(text = displayStatus, color = statusTextColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            HorizontalDivider(color = mutedTextColor.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                parsedItems.forEach { item ->
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(8.dp)).background(item.bgColor).padding(4.dp), contentAlignment = Alignment.Center) {
                            AsyncImage(model = item.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = item.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Storefront, contentDescription = null, tint = mutedTextColor, modifier = Modifier.size(10.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Eco Store", fontSize = 11.sp, color = mutedTextColor)
                            }
                        }
                    }
                }
            }

            if (tabType == "Recent" || tabType == "Completed") {
                Spacer(modifier = Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val addrIconColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF00796B)
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = addrIconColor, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "DELIVERY ADDRESS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = addrIconColor, letterSpacing = 1.sp)
                    }
                    Text(text = order.address, fontSize = 12.sp, color = textColor.copy(alpha = 0.9f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (tabType == "Recent") {
                val flowTransition = rememberInfiniteTransition(label = "FlowTransition")
                val flowOffset by flowTransition.animateFloat(
                    initialValue = -500f,
                    targetValue = 1000f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "FlowOffset"
                )

                val flowBrush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF4CAF50),
                        Color(0xFF81C784),
                        Color(0xFF4CAF50)
                    ),
                    start = Offset(flowOffset, 0f),
                    end = Offset(flowOffset + 300f, 0f),
                    tileMode = TileMode.Repeated
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Order Progress", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textColor)
                    Text(text = "${(animatedProgress * 100).toInt()}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Segmented Progress Bar matching the provided image
                val stepData = listOf(
                    Pair("Placed", Icons.Default.Inventory2),
                    Pair("Packing", Icons.Default.Inbox),
                    Pair("Shipping", Icons.Default.LocalShipping),
                    Pair("Delivered", Icons.Default.CheckCircle)
                )
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        stepData.forEachIndexed { index, data ->
                            val label = data.first
                            val icon = data.second
                            val segmentStart = index.toFloat() / stepData.size
                            val isActive = animatedProgress > segmentStart
                            
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isActive) textColor else mutedTextColor.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(if (isActive) Color(0xFF4CAF50) else mutedTextColor.copy(alpha = 0.3f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (isActive) Color.White else mutedTextColor.copy(alpha = 0.6f),
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        stepData.forEachIndexed { index, _ ->
                            val segmentStart = index.toFloat() / stepData.size
                            val segmentEnd = (index + 1).toFloat() / stepData.size
                            val segmentProgress = ((animatedProgress - segmentStart) / (segmentEnd - segmentStart)).coerceIn(0f, 1f)

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(if (isDark) Color(0xFF2D2D30) else Color(0xFFE9ECEF))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(segmentProgress)
                                        .fillMaxHeight()
                                        .background(if (segmentProgress > 0f) flowBrush else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent)))
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
            if (tabType == "Completed") {
                HorizontalDivider(color = mutedTextColor.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 12.dp))
                if (order.rating > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = "Your Rating: ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = mutedTextColor)
                        for (i in 1..5) {
                            Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = if (i <= order.rating) Color(0xFFFFB200) else mutedTextColor.copy(alpha = 0.3f), modifier = Modifier.size(16.dp))
                        }
                    }
                    order.review?.let { review ->
                        if (review.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(if (isDark) Color(0xFF252528) else Color(0xFFF1F3F5)).padding(12.dp)) {
                                Text(text = "\"$review\"", fontSize = 12.sp, color = textColor, lineHeight = 16.sp)
                            }
                        }
                    }
                }
            } else if (tabType == "Cancelled") {
                HorizontalDivider(color = mutedTextColor.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 12.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(if (isDark) Color(0xFF2C1E1E) else Color(0xFFFFEBEE)).padding(12.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = Color(0xFFF87171), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(text = "Cancellation Reason:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF87171))
                        Text(text = order.cancelledReason ?: "Customer requested cancellation", fontSize = 12.sp, color = textColor, lineHeight = 16.sp)
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Column {
                    Text(text = "Total Amount", fontSize = 11.sp, color = mutedTextColor)
                    Text(text = "Rs. ${order.totalAmount.toInt()}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = if (isDark) Color(0xFF29B6F6) else Color(0xFF0288D1))
                }
                Text(text = "${order.quantity} items", fontSize = 12.sp, color = mutedTextColor, fontWeight = FontWeight.Medium)
            }

            if (tabType == "Completed" && order.rating <= 0) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onRateClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Rate & Review Order", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
