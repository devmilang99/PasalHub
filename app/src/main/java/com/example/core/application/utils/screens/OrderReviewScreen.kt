package com.example.core.application.utils.screens

import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.core.database.data.CartItem

@SuppressLint("FrequentlyChangingValue")
@Composable
fun OrderReviewScreen(
    selectedItems: List<CartItem>,
    selectedPaymentMethod: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    currentUserAddress: String = "Default Address, New York",
    isDark: Boolean = true,
    selectedVoucher: Pair<String, Double> = Pair("None", 0.0)
) {
    val itemsSubtotal = selectedItems.sumOf { it.price * it.quantity }
    val discountAmount = if (itemsSubtotal > 0.0) {
        if (selectedVoucher.first == "PASALSAVINGS" && itemsSubtotal < 30.0) {
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

    LaunchedEffect(scrollState) {
        var lastScrollPos = 0
        snapshotFlow { scrollState.value to scrollState.isScrollInProgress }
            .collect { (currentPosition, inProgress) ->
                if (!inProgress) {
                    footerVisible = true
                } else {
                    val delta = currentPosition - lastScrollPos
                    if (delta > 20 && currentPosition > 100) {
                        footerVisible = false
                    } else if (delta < -20) {
                        footerVisible = true
                    }
                }
                lastScrollPos = currentPosition
            }
    }

    var showExitConfirm by remember { mutableStateOf(false) }

    val bgColor = if (isDark) Color(0xFF000000) else Color(0xFFF9FAFB)
    val cardColor = if (isDark) Color(0xFF111111) else Color(0xFFFFFFFF)
    val textColor = if (isDark) Color.White else Color(0xFF0F172A)
    val textMuted = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val accentColor = Color(0xFF10B981)
    val neonGreen = Color(0xFF00D1A0)
    val borderColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.04f)

    Surface(
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        color = bgColor
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { showExitConfirm = true },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0xFF1A1A1A) else Color(0xFFF1F5F9))
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textColor, modifier = Modifier.size(20.dp))
                }
                
                Text(
                    text = "Order Review",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = textColor,
                    letterSpacing = (-0.5).sp
                )

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0xFF062016) else accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Security, contentDescription = "Secure", tint = if (isDark) accentColor else accentColor, modifier = Modifier.size(22.dp))
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Purchasing Items Header with Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PURCHASING ITEMS",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = textMuted,
                        letterSpacing = 1.2.sp
                    )
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isDark) Color(0xFF1A1A1A) else Color(0xFFF1F5F9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = selectedItems.size.toString(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = textMuted
                        )
                    }
                }
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    border = BorderStroke(1.dp, borderColor)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        selectedItems.forEachIndexed { index, item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(if (isDark) Color(0xFF1A1A1A) else Color(0xFFF1F5F9))
                                        .padding(10.dp),
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
                                    Text(
                                        text = item.title,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = textColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Quantity: ${item.quantity}",
                                        fontSize = 12.sp,
                                        color = textMuted,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = formatDecimalPrice(item.price * item.quantity),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    color = textColor
                                )
                            }
                            if (index < selectedItems.size - 1) {
                                HorizontalDivider(color = borderColor.copy(alpha = 0.5f), thickness = 0.5.dp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(100.dp)) // Buffer for fixed footer
            }
        }

        // Fixed Footer
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            AnimatedVisibility(
                visible = footerVisible,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = cardColor,
                    border = BorderStroke(1.dp, borderColor),
                    shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
                    shadowElevation = 24.dp
                ) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 24.dp)
                            .padding(top = 28.dp, bottom = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Outlined.LocationOn, null, tint = accentColor, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(currentUserAddress, fontSize = 13.sp, color = textColor, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            
                            val paymentIcon = when {
                                selectedPaymentMethod.contains("Cash") -> Icons.Outlined.Payments
                                selectedPaymentMethod.contains("Card") -> Icons.Outlined.CreditCard
                                else -> Icons.Outlined.Smartphone
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(paymentIcon, null, tint = textMuted, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(selectedPaymentMethod.split(" ").first(), fontSize = 13.sp, fontWeight = FontWeight.Black, color = textColor)
                            }
                        }
                        
                        HorizontalDivider(color = borderColor, thickness = 0.5.dp)

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            PriceRowItem("Subtotal", formatDecimalPrice(itemsSubtotal), textColor, textMuted)
                            if (discountAmount > 0.0) {
                                PriceRowItem("Voucher Discount", "-${formatDecimalPrice(discountAmount)}", accentColor, accentColor, isBold = true)
                            }
                            PriceRowItem("Estimated Tax", formatDecimalPrice(taxAmount), textColor, textMuted)
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total Amount", fontSize = 18.sp, fontWeight = FontWeight.Black, color = textColor)
                            Text(formatDecimalPrice(finalTotal), fontSize = 28.sp, fontWeight = FontWeight.Black, color = neonGreen, letterSpacing = (-1).sp)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showExitConfirm = true },
                                modifier = Modifier.height(58.dp).weight(1f),
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(1.5.dp, borderColor)
                            ) {
                                Text("CANCEL", fontWeight = FontWeight.Black, color = textMuted, letterSpacing = 0.5.sp, fontSize = 13.sp)
                            }

                            Button(
                                onClick = onConfirm,
                                modifier = Modifier.height(58.dp).weight(2.2f),
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = textColor, contentColor = bgColor),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                            ) {
                                Text("PLACE ORDER NOW", fontWeight = FontWeight.Black, fontSize = 15.sp, letterSpacing = 0.5.sp)
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
            confirmButton = {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Exit Checkout", fontWeight = FontWeight.ExtraBold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showExitConfirm = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Stay & Continue", color = textMuted, fontWeight = FontWeight.Bold)
                }
            },
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444).copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Warning, null, tint = Color(0xFFEF4444), modifier = Modifier.size(28.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Discontinue Order?",
                        fontWeight = FontWeight.Black,
                        color = textColor,
                        fontSize = 22.sp,
                        textAlign = TextAlign.Center
                    )
                }
            },
            text = {
                Text(
                    "Your progress will not be saved. Are you sure you want to cancel this checkout?",
                    color = textMuted,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    lineHeight = 20.sp
                )
            },
            containerColor = cardColor,
            shape = RoundedCornerShape(32.dp),
            tonalElevation = 8.dp
        )
    }
}

@Composable
private fun PriceRowItem(label: String, value: String, textColor: Color, labelColor: Color, isBold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 14.sp, color = labelColor, fontWeight = if (isBold) FontWeight.ExtraBold else FontWeight.Medium)
        Text(text = value, fontSize = 14.sp, color = textColor, fontWeight = FontWeight.ExtraBold)
    }
}
