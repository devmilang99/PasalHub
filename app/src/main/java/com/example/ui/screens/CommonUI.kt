package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.CartItem
import java.util.*

fun formatPrice(price: Double): String {
    val rounded = kotlin.math.round(price).toInt()
    return "Rs. $rounded"
}

fun formatDecimalPrice(price: Double): String {
    return "Rs. ${String.format(Locale.US, "%.2f", price)}"
}

class JaggedEdgeShape(private val isTop: Boolean = false) : Shape {
    override fun createOutline(size: Size, layoutDirection: androidx.compose.ui.unit.LayoutDirection, density: androidx.compose.ui.unit.Density): Outline {
        val path = Path().apply {
            val step = with(density) { 5.dp.toPx() }
            val halfStep = step / 2
            
            if (isTop) {
                moveTo(0f, step)
                var x = 0f
                while (x < size.width) {
                    lineTo(x + halfStep, 0f)
                    lineTo(x + step, step)
                    x += step
                }
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            } else {
                moveTo(0f, 0f)
                lineTo(size.width, 0f)
                lineTo(size.width, size.height - step)
                var x = size.width
                while (x > 0) {
                    lineTo(x - halfStep, size.height)
                    lineTo(x - step, size.height - step)
                    x -= step
                }
                lineTo(0f, size.height - step)
                close()
            }
        }
        return Outline.Generic(path)
    }
}

@Composable
fun DashedDivider(
    modifier: Modifier = Modifier,
    color: Color = Color.Gray.copy(alpha = 0.2f)
) {
    Canvas(modifier = modifier.fillMaxWidth().height(1.dp)) {
        val dashWidth = 8f
        val dashGap = 6f
        val pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashWidth, dashGap), 0f)
        drawLine(
            color = color,
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            pathEffect = pathEffect,
            strokeWidth = 2f
        )
    }
}

@Composable
fun SuccessScreen(
    message: String,
    onContinue: () -> Unit,
    isDark: Boolean = true
) {
    val bgColor = if (isDark) Color(0xFF000000) else Color(0xFFFDFBF7)
    val textColor = if (isDark) Color.White else Color(0xFF0C1324)
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = bgColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically)
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0xFF4CAF50).copy(alpha = 0.25f), Color.Transparent)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(64.dp)
                )
            }
            
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Order Confirmed!",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = textColor,
                    letterSpacing = 0.5.sp,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = message,
                    fontSize = 16.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text(
                    text = "CONTINUE SHOPPING",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
fun PasalHubAlertDialog(
    onDismissRequest: () -> Unit,
    title: String,
    text: String,
    confirmButtonText: String = "OK",
    onConfirm: () -> Unit = onDismissRequest,
    dismissButtonText: String? = null,
    isDark: Boolean = true,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    iconColor: Color = Color(0xFF10B981)
) {
    val bgColor = if (isDark) Color(0xFF121212) else Color(0xFFFDFBF7)
    val textColor = if (isDark) Color.White else Color(0xFF0C1324)
    val textMuted = if (isDark) Color.Gray else Color(0xFF64748B)

    AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = bgColor,
        shape = RoundedCornerShape(28.dp),
        icon = icon?.let {
            {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(it, contentDescription = null, tint = iconColor, modifier = Modifier.size(32.dp))
                }
            }
        },
        title = {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = textColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Text(
                text = text,
                fontSize = 15.sp,
                color = textMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                lineHeight = 22.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = textColor, contentColor = bgColor)
            ) {
                Text(confirmButtonText, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
            }
        },
        dismissButton = dismissButtonText?.let {
            {
                TextButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(it, color = textMuted, fontWeight = FontWeight.Bold)
                }
            }
        }
    )
}

@Composable
fun OrderSummaryScreen(
    selectedItems: List<CartItem>,
    selectedPaymentMethod: String,
    onPaymentMethodChange: (String) -> Unit,
    selectedVoucher: Pair<String, Double>,
    onVoucherChange: (Pair<String, Double>) -> Unit,
    vouchers: List<Pair<String, Double>>,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    currentUserAddress: String = "Default Address, New York",
    isDark: Boolean = true
) {
    val itemsSubtotal = selectedItems.sumOf { it.price * it.quantity }
    val discountAmount = if (itemsSubtotal > 0.0) {
        if (selectedVoucher.first == "PASALSAVINGS" && itemsSubtotal < 300.0) {
            itemsSubtotal
        } else {
            selectedVoucher.second
        }
    } else {
        0.0
    }
    val discountedSubtotal = (itemsSubtotal - discountAmount).coerceAtLeast(0.0)
    val taxAmount = discountedSubtotal * 0.05
    val finalTotal = discountedSubtotal + taxAmount

    val scrollState = rememberScrollState()
    var footerVisible by remember { mutableStateOf(true) }
    var lastScrollPosition by remember { mutableIntStateOf(0) }

    LaunchedEffect(scrollState.isScrollInProgress) {
        if (!scrollState.isScrollInProgress) {
            // Show footer when scrolling stops
            footerVisible = true
        }
    }

    LaunchedEffect(scrollState.value) {
        val currentPosition = scrollState.value
        val delta = currentPosition - lastScrollPosition
        
        if (delta > 20 && currentPosition > 100) {
            footerVisible = false
        } else if (delta < -20) {
            footerVisible = true
        }
        lastScrollPosition = currentPosition
    }

    var showExitConfirm by remember { mutableStateOf(false) }

    val bgColor = if (isDark) Color(0xFF000000) else Color(0xFFFDFBF7)
    val cardColor = if (isDark) Color(0xFF121212) else Color(0xFFFDFBF7)
    val textColor = if (isDark) Color.White else Color(0xFF0C1324)
    val textMuted = if (isDark) Color.Gray else Color(0xFF64748B)
    val accentColor = Color(0xFF10B981)
    val borderColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = bgColor
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // High-End Custom Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { showExitConfirm = true },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(cardColor)
                        .border(1.dp, borderColor, CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textColor, modifier = Modifier.size(20.dp))
                }
                
                Text(
                    text = "Order Review",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = textColor,
                    letterSpacing = (-0.5).sp
                )

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Security, contentDescription = "Secure", tint = accentColor, modifier = Modifier.size(20.dp))
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 1. ITEMS SECTION
                SectionHeader("Purchasing Items", selectedItems.size.toString())
                
                if (selectedItems.size == 1) {
                    val item = selectedItems.first()
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = cardColor),
                        border = BorderStroke(1.dp, borderColor)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(160.dp)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(if (isDark) Color(0xFF1B1B1D) else Color(0xFFF1F3F5))
                                    .padding(20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = item.image,
                                    contentDescription = item.title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = item.title,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = textColor,
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${item.quantity} Unit • ${formatPrice(item.price)} each",
                                    fontSize = 14.sp,
                                    color = textMuted,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = cardColor),
                        border = BorderStroke(1.dp, borderColor)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            selectedItems.forEach { item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(if (isDark) Color(0xFF1B1B1D) else Color(0xFFE2E4E9))
                                            .padding(8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AsyncImage(
                                            model = item.image,
                                            contentDescription = item.title,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = item.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(text = "Qty: ${item.quantity}", fontSize = 12.sp, color = textMuted, fontWeight = FontWeight.Medium)
                                    }
                                    Text(text = formatPrice(item.price * item.quantity), fontSize = 14.sp, fontWeight = FontWeight.Black, color = textColor)
                                }
                                if (item != selectedItems.last()) HorizontalDivider(color = borderColor)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // COMBINED FOOTER: LOGISTICS + SUMMARY + ACTIONS
            AnimatedVisibility(
                visible = footerVisible,
                enter = expandVertically(expandFrom = Alignment.Bottom) + fadeIn(),
                exit = shrinkVertically(shrinkTowards = Alignment.Bottom) + fadeOut()
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = cardColor,
                    border = BorderStroke(1.dp, borderColor),
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 24.dp)
                            .padding(top = 20.dp, bottom = 24.dp)
                            .navigationBarsPadding(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 1. Logistics & Payment Summary (Moved here)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Outlined.LocationOn, null, tint = textMuted, modifier = Modifier.size(16.dp))
                            Text(currentUserAddress, fontSize = 12.sp, color = textColor, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                            
                            val paymentIcon = when {
                                selectedPaymentMethod.contains("Cash") -> Icons.Outlined.Payments
                                selectedPaymentMethod.contains("Card") -> Icons.Outlined.CreditCard
                                else -> Icons.Outlined.Smartphone
                            }
                            Icon(paymentIcon, null, tint = accentColor, modifier = Modifier.size(16.dp))
                            Text(selectedPaymentMethod.split(" ").first(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textColor)
                        }
                        
                        HorizontalDivider(color = borderColor, thickness = 0.5.dp)

                        // 2. Pricing Breakdown
                        PriceRow("Subtotal", formatDecimalPrice(itemsSubtotal), textColor, textMuted)
                        if (discountAmount > 0.0) {
                            PriceRow("Voucher Savings", "-${formatDecimalPrice(discountAmount)}", accentColor, accentColor, isBold = true)
                        }
                        PriceRow("VAT (5%)", formatDecimalPrice(taxAmount), textColor, textMuted)
                        
                        DashedDivider(modifier = Modifier.padding(vertical = 4.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total Amount", fontSize = 18.sp, fontWeight = FontWeight.Black, color = textColor)
                            Text(formatPrice(finalTotal), fontSize = 24.sp, fontWeight = FontWeight.Black, color = accentColor)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showExitConfirm = true },
                                modifier = Modifier.height(52.dp).weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.5.dp, if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f))
                            ) {
                                Text("CANCEL", fontWeight = FontWeight.Bold, color = textMuted, letterSpacing = 0.5.sp, fontSize = 13.sp)
                            }

                            Button(
                                onClick = onConfirm,
                                modifier = Modifier.height(52.dp).weight(2f),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = textColor, contentColor = bgColor)
                            ) {
                                Text("PLACE ORDER NOW", fontWeight = FontWeight.Black, fontSize = 14.sp, letterSpacing = 0.5.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showExitConfirm) {
        AlertDialog(
            onDismissRequest = { showExitConfirm = false },
            title = { Text("Discontinue Order?", fontWeight = FontWeight.Black, color = textColor) },
            text = { Text("Your progress will not be saved. Are you sure you want to cancel this checkout?", color = textMuted) },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("Exit Checkout", color = Color(0xFFF44336), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitConfirm = false }) {
                    Text("Stay Here", color = accentColor, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = cardColor,
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
private fun SectionHeader(title: String, badge: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title.uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = Color.Gray,
            letterSpacing = 1.5.sp
        )
        Surface(
            color = Color.Gray.copy(alpha = 0.1f),
            shape = RoundedCornerShape(6.dp)
        ) {
            Text(
                text = badge.uppercase(),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, textColor: Color, textMuted: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(icon, contentDescription = null, tint = textMuted, modifier = Modifier.size(20.dp))
        Column {
            Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textMuted)
            Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = textColor, lineHeight = 20.sp)
        }
    }
}

@Composable
private fun PriceRow(label: String, value: String, textColor: Color, labelColor: Color, isBold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 14.sp, color = labelColor, fontWeight = if (isBold) FontWeight.ExtraBold else FontWeight.Medium)
        Text(text = value, fontSize = 14.sp, color = textColor, fontWeight = FontWeight.ExtraBold)
    }
}

data class ParsedOrderItem(
    val title: String,
    val quantity: Int,
    val imageUrl: String,
    val bgColor: Color
)

fun getProductVisualDetails(title: String): Pair<String, Color> {
    val lowerTitle = title.lowercase()
    return when {
        lowerTitle.contains("nike") || lowerTitle.contains("shoe") || lowerTitle.contains("air max") -> {
            Pair(
                "https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=600&q=80",
                Color(0xFFD32F2F)
            )
        }
        lowerTitle.contains("sony") || lowerTitle.contains("headphone") || lowerTitle.contains("wh-1000xm5") || lowerTitle.contains("wireless noise") -> {
            Pair(
                "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=600&q=80",
                Color(0xFFFBC02D)
            )
        }
        lowerTitle.contains("canon") || lowerTitle.contains("camera") || lowerTitle.contains("eos") -> {
            Pair(
                "https://images.unsplash.com/photo-1516035069371-29a1b244cc32?auto=format&fit=crop&w=600&q=80",
                Color(0xFF1E1E1E)
            )
        }
        lowerTitle.contains("smart watch") || lowerTitle.contains("watch") -> {
            Pair(
                "https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=600&q=80",
                Color(0xFF00ACC1)
            )
        }
        lowerTitle.contains("jacket") || lowerTitle.contains("leather") -> {
            Pair(
                "https://images.unsplash.com/photo-1551028719-00167b16eac5?auto=format&fit=crop&w=600&q=80",
                Color(0xFF8D6E63)
            )
        }
        lowerTitle.contains("ring") || lowerTitle.contains("gold") -> {
            Pair(
                "https://images.unsplash.com/photo-1605100804763-247f67b3557e?auto=format&fit=crop&w=600&q=80",
                Color(0xFFFFA000)
            )
        }
        else -> {
            Pair(
                "https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?auto=format&fit=crop&w=600&q=80",
                Color(0xFF90A4AE)
            )
        }
    }
}

fun parseItemsSummary(itemsSummary: String): List<ParsedOrderItem> {
    if (itemsSummary.isBlank()) return emptyList()
    val parts = itemsSummary.split(",")
    return parts.mapNotNull { part ->
        val trimmed = part.trim()
        if (trimmed.isEmpty()) return@mapNotNull null
        
        val lastIndex = trimmed.lastIndexOf(" x")
        val (title, qty) = if (lastIndex != -1) {
            val titlePart = trimmed.substring(0, lastIndex).trim()
            val qtyPart = trimmed.substring(lastIndex + 2).trim()
            Pair(titlePart, qtyPart.toIntOrNull() ?: 1)
        } else {
            Pair(trimmed, 1)
        }
        
        val (imageUrl, bgColor) = getProductVisualDetails(title)
        ParsedOrderItem(
            title = title,
            quantity = qty,
            imageUrl = imageUrl,
            bgColor = bgColor
        )
    }
}

fun Modifier.shimmerEffect(): Modifier = composed {
    var size by remember { mutableStateOf(IntSize.Zero) }
    val transition = rememberInfiniteTransition(label = "shimmer")
    val startOffsetX by transition.animateFloat(
        initialValue = -2 * size.width.toFloat(),
        targetValue = 2 * size.width.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    background(
        brush = Brush.linearGradient(
            colors = listOf(
                Color.LightGray.copy(alpha = 0.5f),
                Color.LightGray.copy(alpha = 0.2f),
                Color.LightGray.copy(alpha = 0.5f),
            ),
            start = Offset(startOffsetX, 0f),
            end = Offset(startOffsetX + size.width.toFloat(), size.height.toFloat())
        )
    ).onGloballyPositioned {
        size = it.size
    }
}
