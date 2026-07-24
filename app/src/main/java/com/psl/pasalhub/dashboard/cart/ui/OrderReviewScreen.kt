package com.psl.pasalhub.dashboard.cart.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.psl.pasalhub.core.application.utils.screens.ParsedOrderItem
import com.psl.pasalhub.core.application.utils.screens.PasalHubAlertDialog
import com.psl.pasalhub.core.application.utils.screens.formatDecimalPrice
import com.psl.pasalhub.core.application.utils.screens.formatPrice
import com.psl.pasalhub.core.application.utils.screens.getProductVisualDetails
import com.psl.pasalhub.core.database.data.CartEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderReviewScreen(
    items: List<CartEntity>,
    subtotal: Double,
    tax: Double,
    discount: Double,
    total: Double,
    voucher: String,
    paymentMethod: String,
    address: String,
    isDark: Boolean,
    onBack: () -> Unit,
    onConfirmOrder: () -> Unit
) {
    var showBackConfirmDialog by remember { mutableStateOf(false) }

    BackHandler {
        showBackConfirmDialog = true
    }

    val parsedItems = remember(items) {
        items.map { item ->
            val (_, bgColor) = getProductVisualDetails(item.title)
            ParsedOrderItem(
                title = item.title,
                quantity = item.quantity,
                imageUrl = item.image,
                bgColor = bgColor,
                seller = item.seller
            )
        }
    }

    val backgroundColor = if (isDark) Color(0xFF0C0C0E) else Color(0xFFF8F9FA)
    val surfaceColor = if (isDark) Color(0xFF161618) else Color.White
    val textColor = if (isDark) Color.White else Color(0xFF1A1C1E)
    val mutedTextColor = if (isDark) Color(0xFF9EA3AE) else Color(0xFF6C757D)
    val primaryColor = Color(0xFF4CAF50)
    val secondaryColor = if (isDark) Color(0xFF2C2C2E) else Color(0xFFE9ECEF)

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Review Order",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.5).sp
                            )
                        )
                        Text(
                            "Checkout Step 2 of 2",
                            style = MaterialTheme.typography.labelSmall,
                            color = primaryColor
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { showBackConfirmDialog = true },
                        modifier = Modifier
                            .padding(8.dp)
                            .clip(CircleShape)
                            .background(secondaryColor)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            "Back",
                            modifier = Modifier.size(20.dp),
                            tint = textColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor,
                    titleContentColor = textColor
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = surfaceColor,
                tonalElevation = 8.dp,
                shadowElevation = 16.dp,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .navigationBarsPadding()
                ) {
                    Button(
                        onClick = onConfirmOrder,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .shadow(12.dp, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(0.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            primaryColor.copy(alpha = 0.9f),
                                            primaryColor
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    "Place Order",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForward,
                                    null,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Unified Order Details Card (Items, Address, Payment)
            item {
                SectionHeader("Order Items", "${items.size} items", primaryColor)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = surfaceColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column {
                        // Items Section
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (parsedItems.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No items selected", color = mutedTextColor)
                                }
                            } else if (parsedItems.size > 2) {
                                // Grid View for more than 2 items
                                val columns = 4
                                parsedItems.chunked(columns).forEach { rowItems ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 4.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        rowItems.forEach { item ->
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .aspectRatio(1f)
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .background(
                                                        if (isDark) Color(0xFF1B1B1D) else item.bgColor.copy(
                                                            alpha = 0.12f
                                                        )
                                                    )
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
                                        if (rowItems.size < columns) {
                                            repeat(columns - rowItems.size) {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            } else {
                                // Standard List View for single item
                                parsedItems.forEach { item ->
                                    PremiumOrderItemRow(item, textColor, mutedTextColor)
                                }
                            }
                        }

                        // Divider between Items and Info
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            color = mutedTextColor.copy(alpha = 0.1f)
                        )

                        // Unified Info Section (Address & Payment side-by-side)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left: Shipping Address
                            Row(
                                modifier = Modifier.weight(1.1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(primaryColor.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.LocationOn,
                                        null,
                                        tint = primaryColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = address,
                                    color = textColor,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Right: Payment Method
                            Row(
                                modifier = Modifier.weight(0.9f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(primaryColor.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Payment,
                                        null,
                                        tint = primaryColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                val displayPayment = when {
                                    paymentMethod.contains("Cash", ignoreCase = true) -> "Cash"
                                    paymentMethod.contains("Card", ignoreCase = true) -> "Card"
                                    paymentMethod.contains("E-sewa", ignoreCase = true) -> "E-sewa"
                                    else -> paymentMethod
                                }
                                Text(
                                    text = displayPayment,
                                    color = textColor,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.CheckCircle,
                                    null,
                                    tint = primaryColor,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Order Summary Section
            item {
                SectionHeader("Price Details", null, primaryColor)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = surfaceColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        SummaryRowModern(
                            "Order Subtotal",
                            formatDecimalPrice(subtotal),
                            mutedTextColor,
                            textColor
                        )
                        SummaryRowModern(
                            "Platform Tax (5%)",
                            formatDecimalPrice(tax),
                            mutedTextColor,
                            textColor
                        )

                        if (discount > 0) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(primaryColor.copy(alpha = 0.1f))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.ConfirmationNumber,
                                        null,
                                        tint = primaryColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Voucher ($voucher)",
                                        color = primaryColor,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    "- ${formatDecimalPrice(discount)}",
                                    color = primaryColor,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = mutedTextColor.copy(alpha = 0.1f)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Total Payable",
                                color = textColor,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                formatPrice(total),
                                color = primaryColor,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    "By placing this order, you agree to PasalHub's Terms of Service and Privacy Policy.",
                    fontSize = 11.sp,
                    color = mutedTextColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        if (showBackConfirmDialog) {
            PasalHubAlertDialog(
                onDismissRequest = { showBackConfirmDialog = false },
                title = "Cancel Checkout?",
                text = "Are you sure you want to go back? Your selection will be saved, but you'll need to review the order details again.",
                confirmButtonText = "Go Back",
                dismissButtonText = "Stay Here",
                onConfirm = {
                    showBackConfirmDialog = false
                    onBack()
                },
                icon = Icons.Default.Warning
            )
        }
    }
}

@Composable
fun PremiumOrderItemRow(item: ParsedOrderItem, textColor: Color, mutedTextColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(item.bgColor.copy(alpha = 0.8f))
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

        Column(modifier = Modifier.weight(1f)) {
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
                    text = item.seller,
                    fontSize = 12.sp,
                    color = mutedTextColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(mutedTextColor.copy(alpha = 0.1f))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                "x${item.quantity}",
                color = textColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
fun SectionHeader(title: String, subtitle: String?, accentColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground,
            letterSpacing = (-0.5).sp
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
        }
    }
}

@Composable
fun SummaryRowModern(label: String, value: String, labelColor: Color, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = labelColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Text(value, color = valueColor, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}
