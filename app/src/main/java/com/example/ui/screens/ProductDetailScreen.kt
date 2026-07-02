package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.window.core.layout.WindowWidthSizeClass
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.local.CartItem
import com.example.data.remote.ProductDto
import com.example.data.repository.Resource
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    product: ProductDto,
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onProductClick: (ProductDto) -> Unit,
    onOrderPlaced: () -> Unit
) {
    val context = LocalContext.current
    val reviews = remember(product) { getMockReviewsForCategory(product.category) }
    var showBuyNowSheet by remember { mutableStateOf(false) }
    var showBuyNowReceipt by remember { mutableStateOf(false) }
    var buyNowVoucher by remember { mutableStateOf(Pair("None", 0.0)) }
    var buyNowPaymentMethod by remember { mutableStateOf("E-sewa") }
    
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    val isFavorited = favoriteIds.contains(product.id)
    val currentUser by viewModel.currentUser.collectAsState()
    val isDark by viewModel.isDarkTheme.collectAsState()

    val adaptiveInfo = currentWindowAdaptiveInfo()
    val isExpanded = adaptiveInfo.windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED
    
    LaunchedEffect(product) {
        viewModel.loadFavorites(context)
    }

    val productResource by viewModel.productsState.collectAsState()
    val similarProducts = remember(productResource, product) {
        when (productResource) {
            is Resource.Success -> {
                (productResource as Resource.Success<List<ProductDto>>).data
                    .filter { it.category == product.category && it.id != product.id }
            }
            else -> emptyList()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("item_detail_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Adaptive Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                        .testTag("detail_back_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Navigate Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { viewModel.notificationEvent.tryEmit("Shared exclusive link!") },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.onSurface)
                    }

                    IconButton(
                        onClick = { viewModel.toggleFavorite(context, product.id) },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                    ) {
                        Icon(
                            imageVector = if (isFavorited) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorited) Color.Red else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            if (isExpanded) {
                // Tablet Layout: Two Columns
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    // Left Column: Image
                    Column(modifier = Modifier.weight(1f)) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF2D2D30) else Color.White),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        ) {
                            Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                                AsyncImage(model = product.image, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                            }
                        }
                    }

                    // Right Column: Details
                    Column(
                        modifier = Modifier
                            .weight(1.2f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        ProductInfoSection(product, isDark)
                        Spacer(modifier = Modifier.height(24.dp))
                        ReviewSection(reviews)
                        if (similarProducts.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(24.dp))
                            SimilarProductsSection(similarProducts, isDark, onProductClick)
                        }
                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }
            } else {
                // Mobile Layout: Single Column
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth().height(300.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF2D2D30) else Color.White),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                            AsyncImage(model = product.image, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    ProductInfoSection(product, isDark)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp), color = Color.Gray.copy(alpha = 0.1f))
                    ReviewSection(reviews)
                    if (similarProducts.isNotEmpty()) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp), color = Color.Gray.copy(alpha = 0.1f))
                        SimilarProductsSection(similarProducts, isDark, onProductClick)
                    }
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }

        // Adaptive Bottom Bar
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .then(if (isExpanded) Modifier.width(450.dp).padding(bottom = 24.dp) else Modifier.fillMaxWidth()),
            shape = if (isExpanded) RoundedCornerShape(28.dp) else RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            shadowElevation = 16.dp,
            border = if (isExpanded) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { viewModel.addToCart(product) },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.LocalMall, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add to Bag", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Button(
                    onClick = { showBuyNowSheet = true },
                    modifier = Modifier.weight(1.2f).height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Buy Now", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }

        if (showBuyNowSheet) {
            BuyNowBottomSheet(
                product = product,
                viewModel = viewModel,
                selectedVoucher = buyNowVoucher,
                onVoucherChange = { buyNowVoucher = it },
                selectedPaymentMethod = buyNowPaymentMethod,
                onPaymentMethodChange = { buyNowPaymentMethod = it },
                onDismiss = { showBuyNowSheet = false },
                onConfirmCheckout = {
                    showBuyNowSheet = false
                    showBuyNowReceipt = true
                },
                isDark = isDark
            )
        }

        if (showBuyNowReceipt) {
            val tempCartItem = remember(product) {
                CartItem(
                    productId = product.id,
                    title = product.title,
                    price = product.price,
                    description = product.description,
                    category = product.category,
                    image = product.image,
                    quantity = 1
                )
            }
            val addressText = currentUser?.address ?: "Default Address, New York"
            OrderSummaryScreen(
                listOf(tempCartItem),
                buyNowPaymentMethod,
                { buyNowPaymentMethod = it },
                buyNowVoucher,
                { buyNowVoucher = it },
                listOf(Pair("None", 0.0), Pair("PASALPREMIUM", 150.0), Pair("PASALSAVINGS", 300.0)),
                { showBuyNowReceipt = false },
                {
                    val discounted = (product.price - buyNowVoucher.second).coerceAtLeast(0.0)
                    viewModel.placeDirectOrder(context, product, discounted * 1.05, buyNowPaymentMethod, buyNowVoucher.first)
                    showBuyNowReceipt = false
                    onOrderPlaced()
                },
                currentUserAddress = addressText,
                isDark = isDark
            )
        }
    }
}

@Composable
fun ProductInfoSection(product: ProductDto, isDark: Boolean) {
    Column {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = product.category.uppercase(),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = product.title,
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground,
            lineHeight = 32.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatPrice(product.price),
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Default.Star, null, tint = Color(0xFFFFB200), modifier = Modifier.size(20.dp))
                Text("4.8 (124 reviews)", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp), color = Color.Gray.copy(alpha = 0.1f))

        Text(text = "Description", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = product.description, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 24.sp)
    }
}

@Composable
fun ReviewSection(reviews: List<ProductReview>) {
    Column {
        Text(text = "Customer Reviews (${reviews.size})", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            reviews.forEach { r ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = r.reviewer, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = r.date, fontSize = 11.sp, color = Color.Gray)
                        }
                        Row(modifier = Modifier.padding(vertical = 4.dp)) {
                            repeat(5) { i ->
                                Icon(Icons.Default.Star, null, tint = if (i < r.rating) Color(0xFFFFB200) else Color.LightGray, modifier = Modifier.size(14.dp))
                            }
                        }
                        Text(text = r.comment, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 20.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun SimilarProductsSection(similarProducts: List<ProductDto>, isDark: Boolean, onProductClick: (ProductDto) -> Unit) {
    Column {
        Text(text = "Similar Premium Products", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(similarProducts) { simProduct ->
                Card(
                    modifier = Modifier.width(160.dp).clickable { onProductClick(simProduct) },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(modifier = Modifier.size(100.dp).clip(RoundedCornerShape(16.dp)).background(if (isDark) Color(0xFF3D3D42) else Color.White).padding(10.dp), contentAlignment = Alignment.Center) {
                            AsyncImage(model = simProduct.image, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = simProduct.title, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(text = formatPrice(simProduct.price), fontSize = 14.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuyNowBottomSheet(
    product: ProductDto,
    viewModel: MainViewModel,
    selectedVoucher: Pair<String, Double>,
    onVoucherChange: (Pair<String, Double>) -> Unit,
    selectedPaymentMethod: String,
    onPaymentMethodChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirmCheckout: () -> Unit,
    isDark: Boolean
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val vouchers = listOf(Pair("None", 0.0), Pair("PASALPREMIUM", 150.0), Pair("PASALSAVINGS", 300.0))
    var isVoucherExpanded by remember { mutableStateOf(false) }

    val subtotal = product.price
    val discountAmount = if (selectedVoucher.first == "PASALSAVINGS" && subtotal < 300.0) subtotal else selectedVoucher.second
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
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp).verticalScroll(rememberScrollState())) {
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
                Button(onClick = onConfirmCheckout, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Color(0xFF4CAF50) else Color(0xFF0C1324), contentColor = Color.White)) {
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
            ProductReview("Aarav Sharma", 5, "June 15, 2026", "Absolutely phenomenal build quality and speed!"),
            ProductReview("Neha Patel", 4, "May 28, 2026", "Extremely powerful. Battery and screen are gorgeous."),
            ProductReview("Kabir Singh", 5, "April 10, 2026", "The ultimate premium product. Fully worth the price.")
        )
        "jewelery" -> listOf(
            ProductReview("Priya Sen", 5, "June 20, 2026", "Stunningly brilliant sheen! Looks even more gorgeous in person."),
            ProductReview("Ananya Roy", 5, "June 02, 2026", "Pure elegance. Pristine luxury packaging."),
            ProductReview("Rohan Mehta", 4, "May 12, 2026", "Exceptional detail. A bit dainty but highly reflective.")
        )
        else -> listOf(
            ProductReview("Vikram Malhotra", 5, "June 18, 2026", "Superb fabric quality, feels incredibly luxurious."),
            ProductReview("Meera Joshi", 4, "June 05, 2026", "Highly fashion-forward and premium material."),
            ProductReview("Ishaan Gupta", 5, "May 24, 2026", "Extremely comfortable and stylish.")
        )
    }
}
