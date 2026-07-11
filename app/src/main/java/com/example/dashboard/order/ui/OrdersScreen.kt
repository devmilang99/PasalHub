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
import androidx.compose.material.icons.outlined.*
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
import com.example.core.application.utils.screens.parseItemsSummary
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import java.util.*
import androidx.compose.ui.platform.LocalLocale

@Composable
fun OrdersScreen(viewModel: OrderViewModel) {
    val orders by viewModel.ordersState.collectAsState()
    val isDark by viewModel.isDarkTheme.collectAsState()
    val sdf = SimpleDateFormat("MMM dd, yyyy - HH:mm", LocalLocale.current.platformLocale)
    var selectedSubTab by remember { mutableStateOf("Recent") }

    val bgColor = if (isDark) Color.Black else Color(0xFFF8F9FA)
    val textColor = if (isDark) Color.White else Color(0xFF212529)
    val mutedTextColor = if (isDark) Color.Gray else Color(0xFF6C757D)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "My Orders",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textColor
            )
        }

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
                    ModernOrderCard(
                        order = order,
                        sdf = sdf,
                        tabType = selectedSubTab,
                        isDark = isDark,
                        onCancel = { id, reason -> viewModel.cancelOrder(id, reason) },
                        onRate = { id, rating, review -> viewModel.completeOrder(id, rating, review) }
                    )
                }
            }
        }
    }
}


@Composable
fun ModernOrderCard(
    order: OrderEntity,
    sdf: SimpleDateFormat,
    tabType: String,
    isDark: Boolean,
    onCancel: (Int, String) -> Unit = { _, _ -> },
    onRate: (Int, Int, String) -> Unit = { _, _, _ -> }
) {
    val parsedItems = remember(order.itemsSummary) { parseItemsSummary(order.itemsSummary) }

    var timeLeft by remember { mutableIntStateOf(10) }
    var isTimerPaused by remember { mutableStateOf(false) }
    var showCancellationSheet by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = order.date) {
        val elapsed = (System.currentTimeMillis() - order.date) / 1000
        timeLeft = (10 - elapsed).toInt().coerceAtLeast(0)
    }

    LaunchedEffect(key1 = isTimerPaused) {
        if (!isTimerPaused) {
            while (timeLeft > 0) {
                delay(1000)
                timeLeft--
            }
        }
    }

    // Logic: Every 5 sec update by 15% (simulated status mapping)
    // Placed (5s) -> Packing (10s) -> Shipping (15s) -> Delivered (100%)
    val progressPercent = if (order.status == "Cancelled") 0 else order.progress

    val animatedProgress by animateFloatAsState(
        targetValue = progressPercent / 100f,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
        label = "DeliveryProgressAnimation"
    )

    val displayStatus = when (order.status) {
        "Placing" if timeLeft > 0 -> "CANCEL WINDOW"
        "Placed" -> "PLACED"
        "Packaging", "Packing" -> "PACKING"
        "Sent for Delivery", "On Way", "Shipping" -> "ON WAY"
        "Delivered" -> "DELIVERED"
        "Completed" -> "COMPLETED"
        "Cancelled" -> "CANCELLED"
        else -> order.status.uppercase()
    }

    val statusBgColor = when (order.status) {
        "Placing" if timeLeft > 0 -> if (isDark) Color(0xFF2E1B1B) else Color(0xFFFFEBEE)
        "Placed" -> if (isDark) Color(0xFF1E293B) else Color(0xFFE3F2FD)
        "Packaging", "Packing" -> if (isDark) Color(0xFF1E293B) else Color(0xFFE0F2F1)
        "Sent for Delivery", "On Way", "Shipping" -> if (isDark) Color(0xFF142B23) else Color(0xFFE8F5E9)
        "Delivered", "Completed" -> if (isDark) Color(0xFF1E2E20) else Color(0xFFF1F8E9)
        "Cancelled" -> if (isDark) Color(0xFF2E1B1B) else Color(0xFFFFEBEE)
        else -> if (isDark) Color(0xFF2C2D30) else Color(0xFFECEFF1)
    }

    val statusTextColor = when (order.status) {
        "Placing" if timeLeft > 0 -> if (isDark) Color(0xFFF87171) else Color(0xFFC62828)
        "Placed" -> if (isDark) Color(0xFF38BDF8) else Color(0xFF1976D2)
        "Packaging", "Packing" -> if (isDark) Color(0xFF38BDF8) else Color(0xFF00796B)
        "Sent for Delivery", "On Way", "Shipping" -> if (isDark) Color(0xFF4ADE80) else Color(0xFF2E7D32)
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
            if (order.status == "Placing" && timeLeft > 0) {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = (if (isDark) Color(0xFF2E1B1B) else Color(0xFFFFEBEE)).copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, Color(0xFFF87171).copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Resized Clock Timer
                            val infiniteTransition = rememberInfiniteTransition(label = "ClockHandLarge")
                            val rotation by infiniteTransition.animateFloat(
                                initialValue = 0f,
                                targetValue = 360f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1000, easing = LinearEasing),
                                    repeatMode = RepeatMode.Restart
                                ),
                                label = "Rotation"
                            )

                            Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    progress = { timeLeft / 10f },
                                    modifier = Modifier.fillMaxSize(),
                                    color = Color(0xFFF87171),
                                    strokeWidth = 3.dp,
                                    trackColor = Color(0xFFF87171).copy(alpha = 0.15f),
                                )
                                Text(
                                    text = timeLeft.toString(),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFF87171)
                                )
                                Box(
                                    modifier = Modifier.size(14.dp).drawBehind {
                                        rotate(rotation) {
                                            drawLine(
                                                color = Color(0xFFF87171).copy(alpha = 0.6f),
                                                start = center, end = Offset(center.x, 0f),
                                                strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round
                                            )
                                        }
                                    }
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Cancellation Window",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color.White else Color(0xFFC62828)
                                )
                                Text(
                                    text = "This is a cancellation window and user can cancel in this window.",
                                    fontSize = 10.sp,
                                    color = (if (isDark) Color.White else Color.Black).copy(alpha = 0.6f),
                                    lineHeight = 14.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                isTimerPaused = true
                                showCancellationSheet = true
                            },
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF87171),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                        ) {
                            Text(text = "CANCEL ORDER", fontSize = 13.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
                        }
                    }
                }
                
                if (showCancellationSheet) {
                    CancellationBottomSheet(
                        onDismiss = {
                            showCancellationSheet = false
                            isTimerPaused = false
                        },
                        onConfirm = { reason: String ->
                            showCancellationSheet = false
                            onCancel(order.orderId, reason)
                        },
                        isDark = isDark
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(text = "#ORD-${1000 + order.orderId}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = textColor)
                    Text(text = sdf.format(Date(order.date)), fontSize = 11.sp, color = mutedTextColor)
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF00796B),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = order.address.split(",").first().trim(),
                            fontSize = 11.sp,
                            color = mutedTextColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(statusBgColor).padding(horizontal = 10.dp, vertical = 6.dp)) {
                    Text(text = displayStatus, color = statusTextColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Space between header and items
            Spacer(modifier = Modifier.height(8.dp))

            HorizontalDivider(color = mutedTextColor.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 12.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                Spacer(modifier = Modifier.width(16.dp))
                Column(horizontalAlignment = Alignment.End) {

                    Text(text = "Rs. ${order.totalAmount.toInt()}", fontSize = 16.sp, fontWeight = FontWeight.Black, color = if (isDark) Color(0xFF29B6F6) else Color(0xFF0288D1))
                    Text(text = "${order.quantity} items", fontSize = 11.sp, color = mutedTextColor, fontWeight = FontWeight.Medium)
                }
            }



            Spacer(modifier = Modifier.height(16.dp))

            val isCancellationActive = order.status == "Placing" && timeLeft > 0

            if (tabType == "Recent" && !isCancellationActive) {
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

                val infiniteTransition = rememberInfiniteTransition(label = "LivePulse")
                val pulseAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.4f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(800, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "PulseAlpha"
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Sensors,
                                contentDescription = null,
                                tint = (if (isDark) Color(0xFF38BDF8) else Color(0xFF1976D2)).copy(alpha = pulseAlpha),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "LIVE TRACKING ACTIVE", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textColor)
                        }

                        val liveStatusText = when {
                            order.status == "Placing" -> "Waiting for final confirmation..."
                            order.status == "Placed" -> "Order confirmed, preparing for dispatch"
                            order.status == "Packaging" -> "Items are being packed by the seller"
                            order.status == "Sent for Delivery" && order.progress < 75 -> "On the way to your local hub"
                            order.status == "Sent for Delivery" && order.progress >= 75 -> "Out for final delivery nearby"
                            order.status == "Delivered" -> "Package arrived at your doorstep"
                            else -> "Fetching live status..."
                        }
                        Text(text = liveStatusText, fontSize = 11.sp, color = mutedTextColor.copy(alpha = 0.8f))
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "${(animatedProgress * 100).toInt()}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                        if (order.status != "Delivered" && order.status != "Completed" && order.status != "Cancelled") {
                            val secondsRemaining = ((100 - (animatedProgress * 100)) * 0.75).toInt()
                            Text(text = "~${secondsRemaining}s left", fontSize = 9.sp, color = mutedTextColor.copy(alpha = 0.6f))
                        }
                    }
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
                if (order.rating > 0) {
                    HorizontalDivider(color = mutedTextColor.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1E1E20) else Color(0xFFF8F9FA)),
                        border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(if (isDark) Color(0xFF2D2D30) else Color(0xFFE2E4E9)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Outlined.RateReview, null, tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF1976D2), modifier = Modifier.size(16.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = "Your Feedback", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textColor)
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                        for (i in 1..5) {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = null,
                                                tint = if (i <= order.rating) Color(0xFFFFB200) else mutedTextColor.copy(alpha = 0.3f),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            order.review?.let { review ->
                                if (review.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "\"$review\"",
                                        fontSize = 12.sp,
                                        color = mutedTextColor,
                                        lineHeight = 18.sp,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                    )
                                }
                            }
                        }
                    }
                } else {
                    var showRatingSheet by remember { mutableStateOf(false) }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { showRatingSheet = true },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDark) Color(0xFF1E293B) else Color(0xFFE3F2FD),
                            contentColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1976D2)
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.StarOutline, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = "RATE & REVIEW PRODUCT", fontSize = 13.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
                        }
                    }

                    if (showRatingSheet) {
                        RatingBottomSheet(
                            orderId = order.orderId,
                            productTitle = parsedItems.firstOrNull()?.title ?: "Product",
                            onDismiss = { showRatingSheet = false },
                            onSubmit = { rating, review ->
                                onRate(order.orderId, rating, review)
                                showRatingSheet = false
                            },
                            isDark = isDark
                        )
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

            if (tabType == "Completed" && order.rating <= 0) {
                // Button was handled above in the rating block
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CancellationBottomSheet(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    isDark: Boolean
) {
    val reasons = listOf(
        "Changed my mind",
        "Found a better price elsewhere",
        "Ordered by mistake",
        "Shipping takes too long",
        "Items no longer needed",
        "Other"
    )
    var selectedReason by remember { mutableStateOf(reasons[0]) }
    var expanded by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    val bgColor = if (isDark) Color(0xFF161618) else Color.White
    val textColor = if (isDark) Color.White else Color(0xFF0C1324)
    val mutedTextColor = if (isDark) Color.Gray else Color(0xFF64748B)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = bgColor,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Cancel,
                contentDescription = null,
                tint = Color(0xFFF87171),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Cancel Your Order?",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = textColor
            )
            Text(
                text = "Please select a reason for cancellation",
                fontSize = 14.sp,
                color = mutedTextColor,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Box(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isDark) Color(0xFF2D2D30) else Color(0xFFF1F5F9))
                        .clickable { expanded = true }
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = selectedReason, color = textColor, fontWeight = FontWeight.Bold)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = mutedTextColor)
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth(0.85f).background(if (isDark) Color(0xFF1E1E20) else Color.White)
                ) {
                    reasons.forEach { reason ->
                        DropdownMenuItem(
                            text = { Text(reason, color = textColor) },
                            onClick = {
                                selectedReason = reason
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.1f))
                ) {
                    Text("GO BACK", fontWeight = FontWeight.Bold, color = mutedTextColor)
                }
                Button(
                    onClick = { onConfirm(selectedReason) },
                    modifier = Modifier.weight(1.5f).height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF87171))
                ) {
                    Text("CONFIRM CANCEL", fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RatingBottomSheet(
    orderId: Int,
    productTitle: String,
    onDismiss: () -> Unit,
    onSubmit: (Int, String) -> Unit,
    isDark: Boolean
) {
    var rating by remember { mutableIntStateOf(0) }
    var reviewText by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val bgColor = if (isDark) Color(0xFF161618) else Color.White
    val textColor = if (isDark) Color.White else Color(0xFF0C1324)
    val mutedTextColor = if (isDark) Color.Gray else Color(0xFF64748B)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = bgColor,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Rate Your Purchase",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = textColor
            )
            Text(
                text = productTitle,
                fontSize = 14.sp,
                color = mutedTextColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Star Rating
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (i in 1..5) {
                    val isSelected = i <= rating
                    Icon(
                        imageVector = if (isSelected) Icons.Default.Star else Icons.Default.StarOutline,
                        contentDescription = "Star $i",
                        tint = if (isSelected) Color(0xFFFFB200) else mutedTextColor.copy(alpha = 0.3f),
                        modifier = Modifier
                            .size(44.dp)
                            .clickable { rating = i }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = reviewText,
                onValueChange = { reviewText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                placeholder = { Text("Share your experience with this product...", fontSize = 14.sp) },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1976D2),
                    unfocusedBorderColor = mutedTextColor.copy(alpha = 0.2f),
                    focusedContainerColor = if (isDark) Color(0xFF1B1B1D) else Color(0xFFF8F9FA),
                    unfocusedContainerColor = if (isDark) Color(0xFF1B1B1D) else Color(0xFFF8F9FA)
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { if (rating > 0) onSubmit(rating, reviewText) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = rating > 0,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1976D2),
                    contentColor = Color.White
                )
            ) {
                Text(text = "SUBMIT REVIEW", fontWeight = FontWeight.Black, fontSize = 15.sp, letterSpacing = 1.sp)
            }
        }
    }
}
