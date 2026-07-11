package com.example.dashboard.products.ui

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import coil.compose.AsyncImage
import com.example.core.database.data.CartItem
import com.example.core.networking.remote.ProductDto
import com.example.dashboard.products.viewmodel.ProductDetailViewModel
import com.example.dashboard.products.repository.Resource
import com.example.core.application.utils.screens.BuyNowBottomSheet
import com.example.core.application.utils.screens.OrderReviewScreen
import com.example.core.application.utils.screens.ProductReview
import com.example.core.application.utils.screens.formatPrice
import com.example.core.application.utils.screens.getMockReviewsForCategory
import com.example.core.viewmodel.MainViewModel
import com.example.ui.theme.LocalDimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    product: ProductDto,
    viewModel: MainViewModel,
    detailViewModel: ProductDetailViewModel,
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
    
    val favoriteIds by detailViewModel.favoriteIds.collectAsState()
    val isFavorited = favoriteIds.contains(product.id)
    val currentUser by viewModel.currentUser.collectAsState()
    val isDark by detailViewModel.isDarkTheme.collectAsState()

    val adaptiveInfo = currentWindowAdaptiveInfo()
    val isExpanded = adaptiveInfo.windowSizeClass.isWidthAtLeastBreakpoint(840)
    val dimens = LocalDimens.current
    
    LaunchedEffect(product) {
        detailViewModel.loadFavorites(context)
        detailViewModel.loadSettings(context)
    }

    val productResource by viewModel.homeProductsState.collectAsState()
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
                    .statusBarsPadding()
                    .padding(dimens.medium),
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
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Navigate Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(dimens.small)) {
                    IconButton(
                        onClick = { detailViewModel.notificationEvent.tryEmit("Shared exclusive link!") },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.onSurface)
                    }

                    IconButton(
                        onClick = { detailViewModel.toggleFavorite(context, product.id) },
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
                        .padding(horizontal = dimens.padding),
                    horizontalArrangement = Arrangement.spacedBy(dimens.extraLarge)
                ) {
                    // Left Column: Image
                    Column(modifier = Modifier.weight(1f)) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f),
                            shape = RoundedCornerShape(dimens.cardCorner * 1.5f),
                            colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF2D2D30) else Color.White),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        ) {
                            Box(modifier = Modifier.fillMaxSize().padding(dimens.large), contentAlignment = Alignment.Center) {
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
                        Spacer(modifier = Modifier.height(dimens.large))
                        ReviewSection(reviews)
                        if (similarProducts.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(dimens.large))
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
                        .padding(horizontal = dimens.padding)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth().height(if (dimens.padding > 24.dp) 400.dp else 300.dp),
                        shape = RoundedCornerShape(dimens.cardCorner * 1.5f),
                        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF2D2D30) else Color.White),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Box(modifier = Modifier.fillMaxSize().padding(dimens.large), contentAlignment = Alignment.Center) {
                            AsyncImage(model = product.image, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                        }
                    }
                    Spacer(modifier = Modifier.height(dimens.large))
                    ProductInfoSection(product, isDark)
                    HorizontalDivider(modifier = Modifier.padding(vertical = dimens.medium), color = Color.Gray.copy(alpha = 0.1f))
                    ReviewSection(reviews)
                    if (similarProducts.isNotEmpty()) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = dimens.medium), color = Color.Gray.copy(alpha = 0.1f))
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
                .navigationBarsPadding()
                .then(if (isExpanded) Modifier.width(500.dp).padding(bottom = dimens.large) else Modifier.fillMaxWidth()),
            shape = if (isExpanded) RoundedCornerShape(dimens.extraLarge) else RoundedCornerShape(topStart = dimens.large, topEnd = dimens.large),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            shadowElevation = 16.dp,
            border = if (isExpanded) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null
        ) {
            Row(
                modifier = Modifier.padding(dimens.medium),
                horizontalArrangement = Arrangement.spacedBy(dimens.small),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { detailViewModel.addToCart(product) },
                    modifier = Modifier.weight(1f).height(dimens.buttonHeight),
                    shape = RoundedCornerShape(dimens.cardCorner),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.LocalMall, null, modifier = Modifier.size(if (dimens.padding > 24.dp) 22.dp else 18.dp))
                    Spacer(modifier = Modifier.width(dimens.extraSmall))
                    Text("Add to Cart", fontWeight = FontWeight.Bold, fontSize = if (dimens.padding > 24.dp) 16.sp else 14.sp)
                }

                Button(
                    onClick = { showBuyNowSheet = true },
                    modifier = Modifier.weight(1.2f).height(dimens.buttonHeight),
                    shape = RoundedCornerShape(dimens.cardCorner)
                ) {
                    Text("Buy Now", fontSize = if (dimens.padding > 24.dp) 18.sp else 16.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }

        if (showBuyNowSheet) {
            BuyNowBottomSheet(
                product = product,
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
            OrderReviewScreen(
                selectedItems = listOf(tempCartItem),
                selectedPaymentMethod = buyNowPaymentMethod,
                onDismiss = { showBuyNowReceipt = false },
                onConfirm = {
                    val discounted = (product.price - buyNowVoucher.second).coerceAtLeast(0.0)
                    detailViewModel.placeDirectOrder(
                        context,
                        product,
                        discounted * 1.05,
                        buyNowPaymentMethod,
                        buyNowVoucher.first
                    )
                    showBuyNowReceipt = false
                    onOrderPlaced()
                },
                currentUserAddress = addressText,
                isDark = isDark,
                selectedVoucher = buyNowVoucher
            )
        }
    }
}

@Composable
fun ProductInfoSection(product: ProductDto, isDark: Boolean) {
    val dimens = LocalDimens.current
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

        Spacer(modifier = Modifier.height(dimens.small))

        Text(
            text = product.title,
            fontSize = if (dimens.padding > 24.dp) 32.sp else 26.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground,
            lineHeight = if (dimens.padding > 24.dp) 40.sp else 32.sp
        )

        Spacer(modifier = Modifier.height(dimens.medium))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatPrice(product.price),
                fontSize = if (dimens.padding > 24.dp) 40.sp else 32.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Default.Star, null, tint = Color(0xFFFFB200), modifier = Modifier.size(if (dimens.padding > 24.dp) 24.dp else 20.dp))
                Text("4.8 (124 reviews)", fontSize = if (dimens.padding > 24.dp) 16.sp else 14.sp, fontWeight = FontWeight.Bold)
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = dimens.large), color = Color.Gray.copy(alpha = 0.1f))

        Text(text = "Description", fontSize = if (dimens.padding > 24.dp) 22.sp else 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(dimens.small))
        Text(text = product.description, fontSize = if (dimens.padding > 24.dp) 17.sp else 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 24.sp)
    }
}

@Composable
fun ReviewSection(reviews: List<ProductReview>) {
    val dimens = LocalDimens.current
    Column {
        Text(text = "Customer Reviews (${reviews.size})", fontSize = if (dimens.padding > 24.dp) 22.sp else 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(dimens.medium))
        Column(verticalArrangement = Arrangement.spacedBy(dimens.small)) {
            reviews.forEach { r ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(dimens.cardCorner),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(dimens.medium)) {
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
    val dimens = LocalDimens.current
    Column {
        Text(text = "Similar Premium Products", fontSize = if (dimens.padding > 24.dp) 22.sp else 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(dimens.medium))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(dimens.medium)) {
            items(similarProducts) { simProduct ->
                Card(
                    modifier = Modifier.width(if (dimens.padding > 24.dp) 200.dp else 160.dp).clickable { onProductClick(simProduct) },
                    shape = RoundedCornerShape(dimens.cardCorner),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(dimens.small), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(modifier = Modifier.size(if (dimens.padding > 24.dp) 140.dp else 100.dp).clip(RoundedCornerShape(dimens.small)).background(if (isDark) Color(0xFF3D3D42) else Color.White).padding(10.dp), contentAlignment = Alignment.Center) {
                            AsyncImage(model = simProduct.image, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                        }
                        Spacer(modifier = Modifier.height(dimens.small))
                        Text(text = simProduct.title, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(text = formatPrice(simProduct.price), fontSize = 14.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}
