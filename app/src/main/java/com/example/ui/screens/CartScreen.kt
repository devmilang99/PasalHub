package com.example.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.LocalMall
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.local.CartItem
import com.example.ui.viewmodel.MainViewModel

@Composable
fun CartScreen(viewModel: MainViewModel, onBack: () -> Unit, onOrderPlaced: () -> Unit) {
    val cartItemsList by viewModel.cartItems.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isDark by viewModel.isDarkTheme.collectAsState()
    val context = LocalContext.current

    // Set of selected item IDs. By default, all items are selected.
    var selectedItemIds by remember(cartItemsList) { 
        mutableStateOf(cartItemsList.map { it.productId }.toSet()) 
    }

    // Voucher selection
    val vouchers = listOf(
        Pair("None", 0.0),
        Pair("PASALPREMIUM", 150.0),
        Pair("PASALSAVINGS", 300.0)
    )
    var selectedVoucher by remember { mutableStateOf(vouchers[0]) }
    var isVoucherExpanded by remember { mutableStateOf(false) }

    // Payment methods
    val paymentMethods = listOf("Cash on Delivery", "Credit / Debit Card", "E-sewa")
    var selectedPaymentMethod by remember { mutableStateOf(paymentMethods[0]) }

    // Dialog states
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showCheckoutConfirmDialog by remember { mutableStateOf(false) }

    val bgColor = if (isDark) Color.Black else Color(0xFFFDFBF7)
    val textColor = if (isDark) Color.White else Color(0xFF0C1324)
    val mutedTextColor = if (isDark) Color.Gray else Color(0xFF64748B)
    val cardColor = if (isDark) Color(0xFF1B1B1D) else Color(0xFFFDFBF7)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Upper Title & Selection Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 16.dp, top = 20.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = textColor
                    )
                }
                
                Text(
                    text = "My Shopping Cart",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    modifier = Modifier.weight(1f)
                )

                if (cartItemsList.isNotEmpty()) {
                    IconButton(
                        onClick = { showDeleteConfirmDialog = true },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0xFF231415) else Color(0xFFFFEBEE))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Selected",
                            tint = if (isDark) Color(0xFFE57373) else Color(0xFFC62828),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            if (cartItemsList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Outlined.LocalMall,
                            contentDescription = "Empty Cart Icon",
                            modifier = Modifier.size(72.dp),
                            tint = mutedTextColor.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Your shopping bag is empty.",
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Add some items from the main tab to get started.",
                            fontSize = 13.sp,
                            color = mutedTextColor
                        )
                    }
                }
            } else {
                // Selection row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val allSelected = selectedItemIds.size == cartItemsList.size
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            selectedItemIds = if (allSelected) emptySet() else cartItemsList.map { it.productId }.toSet()
                        }
                    ) {
                        Checkbox(
                            checked = allSelected && cartItemsList.isNotEmpty(),
                            onCheckedChange = { checked ->
                                selectedItemIds = if (checked) cartItemsList.map { it.productId }.toSet() else emptySet()
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFF4CAF50),
                                uncheckedColor = mutedTextColor
                            ),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (allSelected) "Deselect All" else "Select All",
                            fontSize = 14.sp,
                            color = textColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text = "${selectedItemIds.size}/${cartItemsList.size} items",
                        fontSize = 13.sp,
                        color = mutedTextColor
                    )
                }

                // List of cart items (Scrollable)
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(cartItemsList) { item ->
                        val isChecked = selectedItemIds.contains(item.productId)
                        CartItemRow(
                            item = item,
                            isChecked = isChecked,
                            onCheckedChange = { checked ->
                                selectedItemIds = if (checked) {
                                    selectedItemIds + item.productId
                                } else {
                                    selectedItemIds - item.productId
                                }
                            },
                            onIncrease = { viewModel.increaseQuantity(item) },
                            onDecrease = { viewModel.decreaseQuantity(item) },
                            onDelete = { viewModel.deleteCartItem(item) },
                            isDark = isDark
                        )
                    }
                }

                // Bottom Sheet / Docked Summary Panel
                val selectedItems = cartItemsList.filter { selectedItemIds.contains(it.productId) }
                val itemsSubtotal = selectedItems.sumOf { it.price * it.quantity }
                
                // Calculate voucher deduction
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
                val taxRate = 0.05 // 5% matching the image
                val taxAmount = discountedSubtotal * taxRate
                val finalTotal = discountedSubtotal + taxAmount

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding(),
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Voucher selection & Selected items avatars row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Voucher selection Box (Dropdown)
                            Box(modifier = Modifier.weight(1.8f)) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isDark) Color(0xFF0F0F10) else Color(0xFFE2E4E9))
                                        .clickable { isVoucherExpanded = true }
                                        .padding(horizontal = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (selectedVoucher.first == "None") "Select a voucher" else "Voucher: ${selectedVoucher.first}",
                                        fontSize = 13.sp,
                                        color = if (selectedVoucher.first == "None") mutedTextColor else Color(0xFF4CAF50),
                                        fontWeight = FontWeight.Medium
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Dropdown Arrow",
                                        tint = mutedTextColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                
                                DropdownMenu(
                                    expanded = isVoucherExpanded,
                                    onDismissRequest = { isVoucherExpanded = false },
                                    modifier = Modifier.background(if (isDark) Color(0xFF1E1E20) else Color.White)
                                ) {
                                    vouchers.forEach { v ->
                                        DropdownMenuItem(
                                            text = {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = if (v.first == "None") "No Promo" else v.first,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp,
                                                        color = textColor
                                                    )
                                                    if (v.second > 0) {
                                                        Text(
                                                            text = "-Rs. ${v.second.toInt()}",
                                                            color = Color(0xFF4CAF50),
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 13.sp
                                                        )
                                                    }
                                                }
                                            },
                                            onClick = {
                                                selectedVoucher = v
                                                isVoucherExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Selected items avatars (Right side)
                            if (selectedItems.isNotEmpty()) {
                                Column(
                                    modifier = Modifier.weight(1.2f),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(
                                        text = "Selected",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = mutedTextColor,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy((-8).dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        selectedItems.take(3).forEach { item ->
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.White)
                                                    .border(1.dp, Color.Gray.copy(alpha = 0.3f), CircleShape)
                                                    .padding(2.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                AsyncImage(
                                                    model = ImageRequest.Builder(LocalContext.current)
                                                        .data(item.image)
                                                        .crossfade(true)
                                                        .build(),
                                                    contentDescription = null,
                                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                                    contentScale = ContentScale.Fit
                                                )
                                            }
                                        }
                                        if (selectedItems.size > 3) {
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isDark) Color(0xFF2D2D30) else Color(0xFFE2E4E9))
                                                    .border(1.dp, textColor.copy(alpha = 0.8f), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "+${selectedItems.size - 3}",
                                                    fontSize = 9.sp,
                                                    color = textColor,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1.2f))
                            }
                        }

                        // Payment Options Row (Cash, Card, Pay)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val paymentOptions = listOf(
                                Triple("Cash", Icons.Default.Money, "Cash on Delivery"),
                                Triple("Card", Icons.Default.CreditCard, "Credit / Debit Card"),
                                Triple("E-sewa", Icons.Default.Smartphone, "E-sewa")
                            )
                            paymentOptions.forEach { (label, icon, methodValue) ->
                                val isSelected = selectedPaymentMethod == methodValue
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(46.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) Color(0xFF4CAF50) else (if (isDark) Color(0xFF2D2D30) else Color(0xFFE2E4E9)))
                                    .clickable { selectedPaymentMethod = methodValue }
                                    .padding(horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = label,
                                            tint = if (isSelected) Color.White else mutedTextColor,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = label,
                                            color = if (isSelected) Color.White else mutedTextColor,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        // Subtotal
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Subtotal",
                                color = mutedTextColor,
                                fontSize = 14.sp
                            )
                            Text(
                                text = formatDecimalPrice(itemsSubtotal),
                                color = textColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Tax (5%)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Tax (5%)",
                                color = mutedTextColor,
                                fontSize = 14.sp
                            )
                            Text(
                                text = formatDecimalPrice(taxAmount),
                                color = textColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        if (selectedVoucher.first != "None" && discountAmount > 0.0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocalOffer,
                                        contentDescription = "Voucher Icon",
                                        tint = Color(0xFF4CAF50),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Voucher Discount (${selectedVoucher.first})",
                                        color = Color(0xFF4CAF50),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Text(
                                    text = "-${formatDecimalPrice(discountAmount)}",
                                    color = Color(0xFF4CAF50),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        HorizontalDivider(color = mutedTextColor.copy(alpha = 0.2f))

                        // Total Estimate
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total",
                                color = textColor,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = formatPrice(finalTotal),
                                color = Color(0xFF4CAF50),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        // "Checkout Now" button
                        Button(
                            onClick = {
                                if (selectedItems.isNotEmpty()) {
                                    showCheckoutConfirmDialog = true
                                } else {
                                    viewModel.notificationEvent.tryEmit("Please select at least one item to purchase.")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("checkout_selected_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDark) Color(0xFF0F0F10) else Color(0xFF0C1324),
                                contentColor = Color.White
                            ),
                            enabled = selectedItems.isNotEmpty()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Checkout Now",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Arrow Forward",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Dialog: Bulk Delete Confirm
        if (showDeleteConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = false },
                title = { Text("Delete Selected Items?", fontWeight = FontWeight.Bold, color = textColor) },
                containerColor = cardColor,
                text = { Text("Are you sure you want to remove the selected ${selectedItemIds.size} items from your shopping cart?", color = mutedTextColor) },
                confirmButton = {
                    Button(
                        onClick = {
                            val itemsToDelete = cartItemsList.filter { selectedItemIds.contains(it.productId) }
                            viewModel.deleteMultipleCartItems(itemsToDelete)
                            selectedItemIds = emptySet()
                            showDeleteConfirmDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Color(0xFFE57373) else Color(0xFFC62828))
                    ) {
                        Text("Delete", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmDialog = false }) {
                        Text("Cancel", color = mutedTextColor)
                    }
                }
            )
        }

        // Custom Premium Order Summary Slide-up Overlay
        if (showCheckoutConfirmDialog) {
            val selectedItems = remember(selectedItemIds, cartItemsList) {
                cartItemsList.filter { selectedItemIds.contains(it.productId) }
            }
            val addressText = currentUser?.address ?: "Default Address, New York"
            OrderSummaryScreen(
                selectedItems,
                selectedPaymentMethod,
                { selectedPaymentMethod = it },
                selectedVoucher,
                { selectedVoucher = it },
                vouchers,
                { showCheckoutConfirmDialog = false },
                {
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

                    viewModel.checkoutSelected(
                        context = context,
                        selectedItems = selectedItems,
                        finalTotal = finalTotal,
                        paymentMethod = selectedPaymentMethod,
                        appliedVoucher = selectedVoucher.first
                    )
                    showCheckoutConfirmDialog = false
                    onOrderPlaced()
                },
                currentUserAddress = addressText,
                isDark = isDark
            )
        }
    }
}

@Composable
fun CartItemRow(
    item: CartItem,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onDelete: () -> Unit,
    isDark: Boolean
) {
    val textColor = if (isDark) Color.White else Color(0xFF0C1324)
    val mutedTextColor = if (isDark) Color.Gray else Color(0xFF64748B)
    val itemColor = if (isDark) Color(0xFF161618) else Color.White
    val borderColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f)

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = itemColor),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFF4CAF50),
                    uncheckedColor = mutedTextColor.copy(alpha = 0.5f)
                ),
                modifier = Modifier.testTag("cart_checkbox_${item.productId}")
            )
            
            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
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

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = item.category.uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF4CAF50),
                    letterSpacing = 1.sp
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatPrice(item.price),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = textColor
                    )
                    
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (isDark) Color(0xFF252528) else Color(0xFFE2E4E9))
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onDecrease, modifier = Modifier.size(28.dp)) {
                            Icon(
                                imageVector = if (item.quantity > 1) Icons.Default.Remove else Icons.Default.Delete,
                                contentDescription = "Decrease",
                                tint = if (item.quantity > 1) textColor else (if (isDark) Color(0xFFE57373) else Color(0xFFC62828)),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(
                            text = item.quantity.toString(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        IconButton(onClick = onIncrease, modifier = Modifier.size(28.dp)) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Increase",
                                tint = textColor,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
