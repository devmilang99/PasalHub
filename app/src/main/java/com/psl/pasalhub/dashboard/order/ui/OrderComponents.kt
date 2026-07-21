package com.psl.pasalhub.dashboard.order.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.RateReview
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.psl.pasalhub.core.application.utils.screens.parseItemsSummary
import com.psl.pasalhub.core.database.data.OrderEntity
import com.psl.pasalhub.ui.theme.PasalHubTheme
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun ModernOrderCard(
    order: OrderEntity,
    sdf: SimpleDateFormat,
    tabType: String,
    onCancel: (Int, String) -> Unit = { _, _ -> },
    onRate: (Int, Int, String) -> Unit = { _, _, _ -> },
    onSetPause: (Int, Boolean) -> Unit = { _, _ -> }
) {
    val isDark = !PasalHubTheme.colors.isLight
    val parsedItems = remember(order.itemsSummary) { parseItemsSummary(order.itemsSummary) }
    val isGridView = parsedItems.size > 1

    val timeLeft = (10 - order.progress).coerceAtLeast(0)
    var showCancellationSheet by remember { mutableStateOf(false) }

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
        "Placing" if timeLeft > 0 -> PasalHubTheme.colors.error.copy(alpha = 0.15f)
        "Placed" -> PasalHubTheme.colors.info.copy(alpha = 0.15f)
        "Packaging", "Packing" -> PasalHubTheme.colors.info.copy(alpha = 0.15f)
        "Sent for Delivery", "On Way", "Shipping" -> PasalHubTheme.colors.success.copy(alpha = 0.15f)
        "Delivered", "Completed" -> PasalHubTheme.colors.success.copy(alpha = 0.15f)
        "Cancelled" -> PasalHubTheme.colors.error.copy(alpha = 0.15f)
        else -> PasalHubTheme.colors.outline.copy(alpha = 0.15f)
    }

    val statusTextColor = when (order.status) {
        "Placing" if timeLeft > 0 -> PasalHubTheme.colors.error
        "Placed" -> PasalHubTheme.colors.info
        "Packaging", "Packing" -> PasalHubTheme.colors.info
        "Sent for Delivery", "On Way", "Shipping" -> PasalHubTheme.colors.success
        "Delivered", "Completed" -> PasalHubTheme.colors.success
        "Cancelled" -> PasalHubTheme.colors.error
        else -> PasalHubTheme.colors.textSecondary
    }

    val cardColor = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onSurface
    val mutedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("order_card_${order.orderId}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            if (order.status == "Placing" && timeLeft > 0) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = PasalHubTheme.colors.error.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, PasalHubTheme.colors.error.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    progress = { timeLeft / 10f },
                                    modifier = Modifier.fillMaxSize(),
                                    color = PasalHubTheme.colors.error,
                                    strokeWidth = 3.dp,
                                    trackColor = PasalHubTheme.colors.error.copy(alpha = 0.15f),
                                )
                                Text(
                                    text = timeLeft.toString(),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = PasalHubTheme.colors.error
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Cancellation Window",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PasalHubTheme.colors.error
                                )
                                Text(
                                    text = "This is a cancellation window and user can cancel in this window.",
                                    fontSize = 10.sp,
                                    color = textColor.copy(alpha = 0.6f),
                                    lineHeight = 14.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                onSetPause(order.orderId, true)
                                showCancellationSheet = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PasalHubTheme.colors.error,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                        ) {
                            Text(
                                text = "CANCEL ORDER",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }

                if (showCancellationSheet) {
                    CancellationBottomSheet(
                        onDismiss = {
                            showCancellationSheet = false
                            onSetPause(order.orderId, false)
                        },
                        onConfirm = { reason: String ->
                            showCancellationSheet = false
                            onCancel(order.orderId, reason)
                        }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "#ORD-${1000 + order.orderId}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Text(
                        text = sdf.format(Date(order.date)),
                        fontSize = 11.sp,
                        color = mutedTextColor
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = PasalHubTheme.colors.primary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = order.address.ifEmpty { "Kathmandu, Nepal" }.split(",").first()
                                .trim(),
                            fontSize = 11.sp,
                            color = mutedTextColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusBgColor)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = displayStatus,
                        color = statusTextColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            HorizontalDivider(
                color = mutedTextColor.copy(alpha = 0.1f),
                modifier = Modifier.padding(vertical = 12.dp)
            )

            if (isGridView) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    parsedItems.chunked(3).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowItems.forEach { item ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(item.bgColor)
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AsyncImage(
                                        model = item.imageUrl,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            }
                            if (rowItems.size < 3) {
                                repeat(3 - rowItems.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        parsedItems.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(item.bgColor)
                                        .padding(6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AsyncImage(
                                        model = item.imageUrl,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = item.title,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Storefront,
                                            contentDescription = null,
                                            tint = mutedTextColor,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = order.seller,
                                            fontSize = 12.sp,
                                            color = mutedTextColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Rs. ${order.totalAmount.toInt()}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = PasalHubTheme.colors.info
                        )
                        Text(
                            text = "${order.quantity} items",
                            fontSize = 11.sp,
                            color = mutedTextColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
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
                        PasalHubTheme.colors.success,
                        PasalHubTheme.colors.success.copy(alpha = 0.7f),
                        PasalHubTheme.colors.success
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Sensors,
                                contentDescription = null,
                                tint = PasalHubTheme.colors.info.copy(alpha = pulseAlpha),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "LIVE TRACKING ACTIVE",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
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
                        Text(
                            text = liveStatusText,
                            fontSize = 11.sp,
                            color = mutedTextColor.copy(alpha = 0.8f)
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${(animatedProgress * 100).toInt()}%",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = PasalHubTheme.colors.success
                        )
                        if (order.status != "Delivered" && order.status != "Completed" && order.status != "Cancelled") {
                            val secondsRemaining = ((100 - (animatedProgress * 100)) * 0.75).toInt()
                            Text(
                                text = "~${secondsRemaining}s left",
                                fontSize = 9.sp,
                                color = mutedTextColor.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

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
                                        .background(
                                            if (isActive) PasalHubTheme.colors.success else mutedTextColor.copy(
                                                alpha = 0.3f
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (isActive) Color.White else mutedTextColor.copy(
                                            alpha = 0.6f
                                        ),
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
                            val segmentProgress =
                                ((animatedProgress - segmentStart) / (segmentEnd - segmentStart)).coerceIn(
                                    0f,
                                    1f
                                )

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(PasalHubTheme.colors.surfaceVariant)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(segmentProgress)
                                        .fillMaxHeight()
                                        .background(
                                            if (segmentProgress > 0f) flowBrush else Brush.linearGradient(
                                                listOf(Color.Transparent, Color.Transparent)
                                            )
                                        )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
            if (tabType == "Completed") {
                if (order.rating > 0) {
                    HorizontalDivider(
                        color = mutedTextColor.copy(alpha = 0.1f),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDark) Color(
                                0xFF1E1E20
                            ) else Color(0xFFF8F9FA)
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isDark) Color(0xFF2D2D30) else Color(
                                                0xFFE2E4E9
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Outlined.RateReview,
                                        null,
                                        tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF1976D2),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Your Feedback",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textColor
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        for (i in 1..5) {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = null,
                                                tint = if (i <= order.rating) Color(0xFFFFB200) else mutedTextColor.copy(
                                                    alpha = 0.3f
                                                ),
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDark) Color(0xFF1E293B) else Color(0xFFE3F2FD),
                            contentColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1976D2)
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.StarOutline, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "RATE & REVIEW PRODUCT",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
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
                            }
                        )
                    }
                }
            } else if (tabType == "Cancelled") {
                HorizontalDivider(
                    color = mutedTextColor.copy(alpha = 0.1f),
                    modifier = Modifier.padding(vertical = 12.dp)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isDark) Color(0xFF2C1E1E) else Color(0xFFFFEBEE))
                        .padding(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = Color(0xFFF87171),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Cancellation Reason:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF87171)
                        )
                        Text(
                            text = order.cancelledReason ?: "Customer requested cancellation",
                            fontSize = 12.sp,
                            color = textColor,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            if (isGridView) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = null,
                            tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF1976D2),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Rs. ${order.totalAmount.toInt()}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isDark) Color(0xFF38BDF8) else Color(0xFF1976D2)
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = null,
                            tint = if (isDark) Color.Gray else Color(0xFF6C757D),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${order.quantity} items",
                            fontSize = 14.sp,
                            color = if (isDark) Color.Gray else Color(0xFF6C757D),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CancellationBottomSheet(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
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

    val bgColor = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onSurface
    val mutedTextColor = MaterialTheme.colorScheme.onSurfaceVariant

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
                null,
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
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { expanded = true }
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = selectedReason, color = textColor, fontWeight = FontWeight.Bold)
                    Icon(Icons.Default.ArrowDropDown, null, tint = mutedTextColor)
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    reasons.forEach { reason ->
                        DropdownMenuItem(
                            text = { Text(reason, color = textColor) },
                            onClick = { selectedReason = reason; expanded = false })
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(
                        1.5.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                ) {
                    Text("GO BACK", fontWeight = FontWeight.Bold, color = mutedTextColor)
                }
                Button(
                    onClick = { onConfirm(selectedReason) },
                    modifier = Modifier
                        .weight(1.5f)
                        .height(54.dp),
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
    onSubmit: (Int, String) -> Unit
) {
    var rating by remember { mutableIntStateOf(0) }
    var reviewText by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val bgColor = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onSurface
    val mutedTextColor = MaterialTheme.colorScheme.onSurfaceVariant

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
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (i in 1..5) {
                    val isSelected = i <= rating
                    Icon(
                        imageVector = if (isSelected) Icons.Default.Star else Icons.Default.StarOutline,
                        contentDescription = "Star $i",
                        tint = if (isSelected) Color(0xFFFFB200) else mutedTextColor.copy(alpha = 0.3f),
                        modifier = Modifier
                            .size(44.dp)
                            .clickable { rating = i })
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedTextField(
                value = reviewText,
                onValueChange = { reviewText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                placeholder = {
                    Text(
                        "Share your experience with this product...",
                        fontSize = 14.sp
                    )
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = mutedTextColor.copy(alpha = 0.2f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
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
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = "SUBMIT REVIEW",
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
