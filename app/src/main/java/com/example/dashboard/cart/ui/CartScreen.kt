package com.example.dashboard.cart.ui

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
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import coil.compose.AsyncImage
import com.example.core.database.data.CartItem
import com.example.dashboard.cart.viewmodel.CartViewModel
import com.example.core.application.utils.screens.OrderSummaryScreen
import com.example.core.application.utils.screens.formatDecimalPrice
import com.example.core.application.utils.screens.formatPrice

@Composable
fun CartScreen(viewModel: CartViewModel, onBack: () -> Unit, onOrderPlaced: () -> Unit) {
    val cartItemsList by viewModel.cartItems.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isDark by viewModel.isDarkTheme.collectAsState()
    val context = LocalContext.current

    val adaptiveInfo = currentWindowAdaptiveInfo()
    val isExpanded = adaptiveInfo.windowSizeClass.isWidthAtLeastBreakpoint(840)

    // Set of selected item IDs. By default, all items are selected.
    var selectedItemIds by remember(cartItemsList) { 
        mutableStateOf(cartItemsList.map { it.productId }.toSet()) 
    }

    // Voucher selection
    val vouchers = listOf(
        Pair("None", 0.0),
        Pair("PASALPREMIUM", 10.0),
        Pair("PASALSAVINGS", 30.0)
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
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            CartHeader(onBack, isDark, textColor, cartItemsList.isNotEmpty(), onDeleteClick = { showDeleteConfirmDialog = true })

            if (cartItemsList.isEmpty()) {
                EmptyCartView(textColor, mutedTextColor)
            } else {
                if (isExpanded) {
                    // Tablet Layout
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(32.dp)
                    ) {
                        // Left: Cart Items
                        Column(modifier = Modifier.weight(1.5f)) {
                            SelectionControls(
                                selectedCount = selectedItemIds.size,
                                totalCount = cartItemsList.size,
                                allSelected = selectedItemIds.size == cartItemsList.size,
                                onSelectAll = { checked ->
                                    selectedItemIds = if (checked) cartItemsList.map { it.productId }.toSet() else emptySet()
                                },
                                textColor = textColor,
                                mutedTextColor = mutedTextColor
                            )
                            LazyColumn(
                                contentPadding = PaddingValues(bottom = 32.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(cartItemsList) { item ->
                                    CartItemRow(
                                        item = item,
                                        isChecked = selectedItemIds.contains(item.productId),
                                        onCheckedChange = { checked ->
                                            selectedItemIds = if (checked) selectedItemIds + item.productId else selectedItemIds - item.productId
                                        },
                                        onIncrease = { viewModel.increaseQuantity(item) },
                                        onDecrease = { viewModel.decreaseQuantity(item) },
                                        onDelete = { viewModel.deleteCartItem(item) },
                                        isDark = isDark
                                    )
                                }
                            }
                        }

                        // Right: Summary
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 16.dp)
                        ) {
                            val selectedItems = cartItemsList.filter { selectedItemIds.contains(it.productId) }
                            CartSummaryCard(
                                selectedItems = selectedItems,
                                isDark = isDark,
                                cardColor = cardColor,
                                textColor = textColor,
                                mutedTextColor = mutedTextColor,
                                selectedVoucher = selectedVoucher,
                                onVoucherChange = { selectedVoucher = it },
                                vouchers = vouchers,
                                isVoucherExpanded = isVoucherExpanded,
                                onVoucherExpandedChange = { isVoucherExpanded = it },
                                selectedPaymentMethod = selectedPaymentMethod,
                                onPaymentMethodChange = { selectedPaymentMethod = it },
                                onCheckoutClick = {
                                    if (selectedItems.isNotEmpty()) showCheckoutConfirmDialog = true
                                    else viewModel.showNotification("Select an item first.")
                                }
                            )
                        }
                    }
                } else {
                    // Mobile Layout
                    Column(modifier = Modifier.fillMaxSize()) {
                        SelectionControls(
                            selectedCount = selectedItemIds.size,
                            totalCount = cartItemsList.size,
                            allSelected = selectedItemIds.size == cartItemsList.size,
                            onSelectAll = { checked ->
                                selectedItemIds = if (checked) cartItemsList.map { it.productId }.toSet() else emptySet()
                            },
                            textColor = textColor,
                            mutedTextColor = mutedTextColor
                        )
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(cartItemsList) { item ->
                                CartItemRow(
                                    item = item,
                                    isChecked = selectedItemIds.contains(item.productId),
                                    onCheckedChange = { checked ->
                                        selectedItemIds = if (checked) selectedItemIds + item.productId else selectedItemIds - item.productId
                                    },
                                    onIncrease = { viewModel.increaseQuantity(item) },
                                    onDecrease = { viewModel.decreaseQuantity(item) },
                                    onDelete = { viewModel.deleteCartItem(item) },
                                    isDark = isDark
                                )
                            }
                        }
                        
                        val selectedItems = cartItemsList.filter { selectedItemIds.contains(it.productId) }
                        CartSummaryCard(
                            selectedItems = selectedItems,
                            isDark = isDark,
                            cardColor = cardColor,
                            textColor = textColor,
                            mutedTextColor = mutedTextColor,
                            selectedVoucher = selectedVoucher,
                            onVoucherChange = { selectedVoucher = it },
                            vouchers = vouchers,
                            isVoucherExpanded = isVoucherExpanded,
                            onVoucherExpandedChange = { isVoucherExpanded = it },
                            selectedPaymentMethod = selectedPaymentMethod,
                            onPaymentMethodChange = { selectedPaymentMethod = it },
                            onCheckoutClick = {
                                if (selectedItems.isNotEmpty()) showCheckoutConfirmDialog = true
                                else viewModel.showNotification("Select an item first.")
                            }
                        )
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
                selectedItems = selectedItems,
                selectedPaymentMethod = selectedPaymentMethod,
                onDismiss = { showCheckoutConfirmDialog = false },
                onConfirm = {
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
                isDark = isDark,
                selectedVoucher = selectedVoucher
            )
        }
    }
}

@Composable
fun CartHeader(onBack: () -> Unit, isDark: Boolean, textColor: Color, hasItems: Boolean, onDeleteClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 16.dp, top = 20.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = textColor)
        }
        Text("My Shopping Cart", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = textColor, modifier = Modifier.weight(1f))
        if (hasItems) {
            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.size(36.dp).clip(CircleShape).background(if (isDark) Color(0xFF231415) else Color(0xFFFFEBEE))
            ) {
                Icon(Icons.Default.Delete, "Delete", tint = if (isDark) Color(0xFFE57373) else Color(0xFFC62828), modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun EmptyCartView(textColor: Color, mutedTextColor: Color) {
    Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.8f), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Outlined.LocalMall, null, modifier = Modifier.size(72.dp), tint = mutedTextColor.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(16.dp))
            Text("Your shopping bag is empty.", fontWeight = FontWeight.Bold, color = textColor)
            Text("Add some items from the main tab to get started.", fontSize = 13.sp, color = mutedTextColor)
        }
    }
}

@Composable
fun SelectionControls(selectedCount: Int, totalCount: Int, allSelected: Boolean, onSelectAll: (Boolean) -> Unit, textColor: Color, mutedTextColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onSelectAll(!allSelected) }) {
            Checkbox(
                checked = allSelected, onCheckedChange = onSelectAll,
                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF4CAF50), uncheckedColor = mutedTextColor),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (allSelected) "Deselect All" else "Select All", fontSize = 14.sp, color = textColor, fontWeight = FontWeight.Medium)
        }
        Text("$selectedCount/$totalCount items", fontSize = 13.sp, color = mutedTextColor)
    }
}

@Composable
fun CartSummaryCard(
    selectedItems: List<CartItem>, isDark: Boolean, cardColor: Color, textColor: Color, mutedTextColor: Color,
    selectedVoucher: Pair<String, Double>, onVoucherChange: (Pair<String, Double>) -> Unit,
    vouchers: List<Pair<String, Double>>, isVoucherExpanded: Boolean, onVoucherExpandedChange: (Boolean) -> Unit,
    selectedPaymentMethod: String, onPaymentMethodChange: (String) -> Unit, onCheckoutClick: () -> Unit
) {
    val itemsSubtotal = selectedItems.sumOf { it.price * it.quantity }
    val discountAmount = if (itemsSubtotal > 0.0) {
        if (selectedVoucher.first == "PASALSAVINGS" && itemsSubtotal < 30.0) itemsSubtotal else selectedVoucher.second
    } else 0.0
    val discountedSubtotal = (itemsSubtotal - discountAmount).coerceAtLeast(0.0)
    val taxAmount = discountedSubtotal * 0.05
    val finalTotal = discountedSubtotal + taxAmount

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.weight(1.8f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(12.dp)).background(if (isDark) Color(0xFF0F0F10) else Color(0xFFE2E4E9)).clickable { onVoucherExpandedChange(true) }.padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = if (selectedVoucher.first == "None") "Select voucher" else "Voucher: ${selectedVoucher.first}", fontSize = 13.sp, color = if (selectedVoucher.first == "None") mutedTextColor else Color(0xFF4CAF50), fontWeight = FontWeight.Medium)
                        Icon(Icons.Default.ArrowDropDown, null, tint = mutedTextColor, modifier = Modifier.size(20.dp))
                    }
                    DropdownMenu(expanded = isVoucherExpanded, onDismissRequest = { onVoucherExpandedChange(false) }, modifier = Modifier.background(if (isDark) Color(0xFF1E1E20) else Color.White)) {
                        vouchers.forEach { v ->
                            DropdownMenuItem(text = {
                                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(v.first, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = textColor)
                                    if (v.second > 0) Text("-Rs. ${v.second.toInt()}", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }, onClick = { onVoucherChange(v); onVoucherExpandedChange(false) })
                        }
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                if (selectedItems.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy((-8).dp), verticalAlignment = Alignment.CenterVertically) {
                        selectedItems.take(3).forEach { item ->
                            Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(Color.White).border(1.dp, Color.Gray.copy(alpha = 0.3f), CircleShape).padding(2.dp)) {
                                AsyncImage(model = item.image, contentDescription = null, modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Fit)
                            }
                        }
                        if (selectedItems.size > 3) {
                            Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(if (isDark) Color(0xFF2D2D30) else Color(0xFFE2E4E9)).border(1.dp, textColor.copy(alpha = 0.8f), CircleShape), contentAlignment = Alignment.Center) {
                                Text("+${selectedItems.size - 3}", fontSize = 9.sp, color = textColor, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(Triple("Cash", Icons.Default.Money, "Cash on Delivery"), Triple("Card", Icons.Default.CreditCard, "Credit / Debit Card"), Triple("E-sewa", Icons.Default.Smartphone, "E-sewa")).forEach { (label, icon, method) ->
                    val isSel = selectedPaymentMethod == method
                    Box(modifier = Modifier.weight(1f).height(46.dp).clip(RoundedCornerShape(12.dp)).background(if (isSel) Color(0xFF4CAF50) else (if (isDark) Color(0xFF2D2D30) else Color(0xFFE2E4E9))).clickable { onPaymentMethodChange(method) }, contentAlignment = Alignment.Center) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(icon, label, tint = if (isSel) Color.White else mutedTextColor, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(label, color = if (isSel) Color.White else mutedTextColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            SummaryRow("Subtotal", formatDecimalPrice(itemsSubtotal), mutedTextColor, textColor)
            SummaryRow("Tax (5%)", formatDecimalPrice(taxAmount), mutedTextColor, textColor)
            if (discountAmount > 0.0) SummaryRow("Discount (${selectedVoucher.first})", "-${
                formatDecimalPrice(
                    discountAmount
                )
            }", Color(0xFF4CAF50), Color(0xFF4CAF50))
            HorizontalDivider(color = mutedTextColor.copy(alpha = 0.2f))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total", color = textColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(formatPrice(finalTotal), color = Color(0xFF4CAF50), fontSize = 20.sp, fontWeight = FontWeight.Black)
            }
            Button(onClick = onCheckoutClick, modifier = Modifier.fillMaxWidth().height(52.dp).testTag("checkout_selected_button"), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Color(0xFF0F0F10) else Color(0xFF0C1324), contentColor = Color.White), enabled = selectedItems.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Checkout Now", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String, labelColor: Color, valueColor: Color) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = labelColor, fontSize = 14.sp)
        Text(value, color = valueColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
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
