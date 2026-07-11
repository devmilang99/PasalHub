package com.example.core.application.utils.screens

import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import com.example.core.application.utils.NetworkUtils
import com.example.core.database.data.CartItem
import com.example.core.networking.remote.ProductDto
import java.util.*
import kotlin.math.round

fun formatPrice(price: Double): String {
    val rounded = round(price).toInt()
    return "Rs. $rounded"
}

fun formatDecimalPrice(price: Double): String {
    return "Rs. ${String.format(Locale.US, "%.2f", price)}"
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
    icon: ImageVector? = null,
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
                colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Color.White else Color(0xFF0C1324), contentColor = if (isDark) Color.Black else Color.White)
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
fun PasalHubAuthDialog(
    onDismissRequest: () -> Unit,
    state: AuthDialogState,
    isDark: Boolean = true
) {
    val bgColor = if (isDark) Color(0xFF121212) else Color(0xFFFDFBF7)
    val textColor = if (isDark) Color.White else Color(0xFF0C1324)
    val textMuted = if (isDark) Color.Gray else Color(0xFF64748B)

    val infiniteTransition = rememberInfiniteTransition(label = "auth_loading")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "loading_scale"
    )

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        var visible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { visible = true }

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(400)) + scaleIn(initialScale = 0.8f, animationSpec = tween(400, easing = FastOutSlowInEasing)),
            exit = fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = 0.8f, animationSpec = tween(300))
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(32.dp),
                color = bgColor,
                tonalElevation = 8.dp,
                shadowElevation = 12.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .then(
                                if (state is AuthDialogState.Loading) Modifier.graphicsLayer(scaleX = scale, scaleY = scale)
                                else Modifier
                            )
                            .clip(CircleShape)
                            .background(state.color.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        when (state) {
                            is AuthDialogState.Loading -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(40.dp),
                                    color = state.color,
                                    strokeWidth = 3.dp
                                )
                            }
                            else -> {
                                Icon(
                                    imageVector = state.icon,
                                    contentDescription = null,
                                    tint = state.color,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = state.title,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = textColor,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = state.message,
                        fontSize = 15.sp,
                        color = textMuted,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )

                    if (state !is AuthDialogState.Loading) {
                        Button(
                            onClick = onDismissRequest,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (state is AuthDialogState.Success) state.color else textColor,
                                contentColor = if (state is AuthDialogState.Success) Color.White else bgColor
                            )
                        ) {
                            Text(
                                if (state is AuthDialogState.Success) "CONTINUE" else "TRY AGAIN",
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

sealed class AuthDialogState(
    val title: String,
    val message: String,
    val icon: ImageVector,
    val color: Color
) {
    class Loading(action: String) : AuthDialogState(
        title = "Processing",
        message = "Please wait while we secure your $action...",
        icon = Icons.Default.Sync,
        color = Color(0xFF3B82F6)
    )
    class Success(action: String) : AuthDialogState(
        title = "Success!",
        message = "Your $action was successful. Welcome to Pasal Hub!",
        icon = Icons.Default.CheckCircle,
        color = Color(0xFF10B981)
    )
    class Error(message: String) : AuthDialogState(
        title = "Authentication Failed",
        message = message,
        icon = Icons.Default.Error,
        color = Color(0xFFEF4444)
    )
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
        val (mainPart, qty) = if (lastIndex != -1) {
            val titlePart = trimmed.substring(0, lastIndex).trim()
            val qtyPart = trimmed.substring(lastIndex + 2).trim()
            Pair(titlePart, qtyPart.toIntOrNull() ?: 1)
        } else {
            Pair(trimmed, 1)
        }
        
        val pipeIndex = mainPart.lastIndexOf("|")
        val (title, imageUrlFromSummary) = if (pipeIndex != -1) {
            Pair(mainPart.substring(0, pipeIndex).trim(), mainPart.substring(pipeIndex + 1).trim())
        } else {
            Pair(mainPart, null)
        }
        
        val (defaultImageUrl, bgColor) = getProductVisualDetails(title)
        ParsedOrderItem(
            title = title,
            quantity = qty,
            imageUrl = imageUrlFromSummary ?: defaultImageUrl,
            bgColor = bgColor
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuyNowBottomSheet(
    product: ProductDto,
    selectedVoucher: Pair<String, Double>,
    onVoucherChange: (Pair<String, Double>) -> Unit,
    selectedPaymentMethod: String,
    onPaymentMethodChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirmCheckout: () -> Unit,
    isDark: Boolean
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val vouchers = listOf(Pair("None", 0.0), Pair("PASALPREMIUM", 10.0), Pair("PASALSAVINGS", 30.0))
    var isVoucherExpanded by remember { mutableStateOf(false) }

    val subtotal = product.price
    val discountAmount = if (selectedVoucher.first == "PASALSAVINGS" && subtotal < 30.0) subtotal else selectedVoucher.second
    val discountedSubtotal = (subtotal - discountAmount).coerceAtLeast(0.0)
    val taxAmount = discountedSubtotal * 0.05
    val finalTotal = discountedSubtotal + taxAmount

    val paymentMethods = listOf("Cash on Delivery", "Credit / Debit Card", "E-sewa")

    val bgColor = if (isDark) Color(0xFF1B1B1D) else Color(0xFFFDFBF7)
    val cardColor = if (isDark) Color(0xFF161618) else Color(0xFFFDFBF7)
    val textColor = if (isDark) Color.White else Color(0xFF0C1324)
    val mutedTextColor = if (isDark) Color.Gray else Color(0xFF64748B)
    val itemBg = if (isDark) Color(0xFF0F0F10) else Color(0xFFE2E4E9)
    val dropdownBg = if (isDark) Color(0xFF1E1E20) else Color.White

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = bgColor
    ) {
        Column(modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 20.dp, vertical = 8.dp).verticalScroll(rememberScrollState())) {
            Text(text = "Checkout Order", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = textColor)
            Spacer(modifier = Modifier.height(16.dp))
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = cardColor), border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f))) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(80.dp).clip(RoundedCornerShape(20.dp)).background(if (isDark) Color(0xFF2D2D30) else Color.White).padding(8.dp), contentAlignment = Alignment.Center) {
                        AsyncImage(model = product.image, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = product.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = textColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(text = product.category.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFF4CAF50), letterSpacing = 1.sp)
                        Text(text = formatPrice(product.price), fontSize = 17.sp, fontWeight = FontWeight.Black, color = textColor)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "Apply Promo Voucher", fontSize = 12.sp, color = mutedTextColor, fontWeight = FontWeight.Bold)
                Box(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(14.dp)).background(itemBg).clickable { isVoucherExpanded = true }.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(text = if (selectedVoucher.first == "None") "Select a voucher" else "Voucher: ${selectedVoucher.first}", fontSize = 14.sp, color = if (selectedVoucher.first == "None") mutedTextColor else Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = mutedTextColor)
                    }
                    DropdownMenu(expanded = isVoucherExpanded, onDismissRequest = { isVoucherExpanded = false }, modifier = Modifier.background(dropdownBg)) {
                        vouchers.forEach { v ->
                            DropdownMenuItem(
                                text = {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = if (v.first == "None") "No Promo" else v.first, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = textColor)
                                        if (v.second > 0) Text(text = "-Rs. ${v.second.toInt()}", color = Color(0xFF4CAF50), fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                                    }
                                },
                                onClick = { onVoucherChange(v); isVoucherExpanded = false }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Payment Method", fontSize = 12.sp, color = mutedTextColor, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    paymentMethods.forEach { method ->
                        val isSelected = selectedPaymentMethod == method
                        val icon = when { method.contains("Cash") -> Icons.Default.Money; method.contains("Card") -> Icons.Default.CreditCard; else -> Icons.Default.Smartphone }
                        val shortLabel = when { method.contains("Cash") -> "Cash"; method.contains("Card") -> "Card"; else -> "E-sewa" }
                        Box(modifier = Modifier.weight(1f).height(64.dp).clip(RoundedCornerShape(12.dp)).background(if (isSelected) Color(0xFF4CAF50) else (if (isDark) Color(0xFF2D2D30) else Color(0xFFE2E4E9))).clickable { onPaymentMethodChange(method) }, contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(imageVector = icon, contentDescription = null, tint = if (isSelected) Color.White else mutedTextColor, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = shortLabel, color = if (isSelected) Color.White else mutedTextColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Subtotal", color = mutedTextColor, fontSize = 14.sp)
                        Text(text = formatPrice(subtotal), color = textColor, fontSize = 14.sp)
                    }
                    if (discountAmount > 0.0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Discount", color = Color(0xFF4CAF50), fontSize = 14.sp)
                            Text(text = "-${formatPrice(discountAmount)}", color = Color(0xFF4CAF50), fontSize = 14.sp)
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Tax (5%)", color = mutedTextColor, fontSize = 14.sp)
                        Text(text = formatDecimalPrice(taxAmount), color = textColor, fontSize = 14.sp)
                    }
                    HorizontalDivider(color = if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Total", color = textColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(text = formatPrice(finalTotal), color = Color(0xFF4CAF50), fontSize = 22.sp, fontWeight = FontWeight.Black)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (NetworkUtils.isNetworkAvailable(context)) {
                            onConfirmCheckout()
                        } else {
                            Toast.makeText(context, "No internet connection. Please check your network.", Toast.LENGTH_SHORT).show()
                        }
                    }, 
                    modifier = Modifier.fillMaxWidth().height(54.dp), 
                    shape = RoundedCornerShape(16.dp), 
                    colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Color(0xFF4CAF50) else Color(0xFF0C1324), contentColor = Color.White)
                ) {
                    Text(text = "Place Order Now", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

data class ProductReview(val reviewer: String, val rating: Int, val date: String, val comment: String)

fun getMockReviewsForCategory(category: String): List<ProductReview> {
    return when (category.lowercase()) {
        "electronics" -> listOf(
            ProductReview("Jeevan Sharma", 5, "June 15, 2026", "Absolutely phenomenal build quality and speed!"),
            ProductReview("Neha Karki", 4, "May 28, 2026", "Extremely powerful. Battery and screen are gorgeous."),
            ProductReview("Kamal Thapa", 5, "April 10, 2026", "The ultimate premium product. Fully worth the price.")
        )
        "jewelery" -> listOf(
            ProductReview("Priya Shrestha", 5, "June 20, 2026", "Stunningly brilliant sheen! Looks even more gorgeous in person."),
            ProductReview("Sanjiv Basnet", 5, "June 02, 2026", "Pure elegance. Pristine luxury packaging."),
            ProductReview("Mohan Khatiwada", 4, "May 12, 2026", "Exceptional detail. A bit dainty but highly reflective.")
        )
        else -> listOf(
            ProductReview("Vikram Chaudhary", 5, "June 18, 2026", "Superb fabric quality, feels incredibly luxurious."),
            ProductReview("Roshan Jairu", 4, "June 05, 2026", "Highly fashion-forward and premium material."),
            ProductReview("Sanjay Gupta", 5, "May 24, 2026", "Extremely comfortable and stylish.")
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
