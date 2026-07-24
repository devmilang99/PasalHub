package com.psl.pasalhub.dashboard.home.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.psl.pasalhub.R
import com.psl.pasalhub.ai.presentation.AiSearchViewModel
import com.psl.pasalhub.ai.presentation.components.AiListeningAnimation
import com.psl.pasalhub.core.application.utils.screens.formatPrice
import com.psl.pasalhub.core.application.utils.screens.shimmerEffect
import com.psl.pasalhub.core.networking.remote.ProductDto
import com.psl.pasalhub.dashboard.home.viewmodel.HomeViewModel
import com.psl.pasalhub.dashboard.products.repository.Resource
import com.psl.pasalhub.ui.theme.LocalDimens
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

data class PromoItem(
    val title: String,
    val subtitle: String,
    val category: String,
    val emoji: String,
    val color: Color
)

@Composable
@OptIn(ExperimentalMaterial3Api::class, kotlinx.coroutines.FlowPreview::class)
fun HomeScreen(
    viewModel: HomeViewModel,
    aiViewModel: AiSearchViewModel,
    onProductClick: (ProductDto) -> Unit,
    onAiSearchClick: () -> Unit = {}
) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isDark by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val productsState by viewModel.homeProductsState.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val isAiProcessing by aiViewModel.isAiProcessing.collectAsStateWithLifecycle()
    val isFilterActive by viewModel.isFilterActive.collectAsStateWithLifecycle()
    val maxPrice by viewModel.maxPrice.collectAsStateWithLifecycle()
    val sellerLocation by viewModel.sellerLocation.collectAsStateWithLifecycle()
    val sortBy by viewModel.sortBy.collectAsStateWithLifecycle()
    val cartItemIds by viewModel.cartItemIds.collectAsStateWithLifecycle()
    val favoriteIds by viewModel.favoriteIds.collectAsStateWithLifecycle()

    val paginatedProducts = viewModel.paginatedProducts.collectAsLazyPagingItems()

    val dimens = LocalDimens.current
    val context = LocalContext.current

    var isRefreshing by remember { mutableStateOf(false) }
    var localSearchQuery by remember { mutableStateOf(searchQuery) }

    LaunchedEffect(localSearchQuery) {
        if (localSearchQuery == searchQuery) return@LaunchedEffect
        delay(500.milliseconds)
        viewModel.setSearchQuery(localSearchQuery)
    }

    LaunchedEffect(productsState) {
        if (productsState !is Resource.Loading) {
            isRefreshing = false
        }
    }

    LaunchedEffect(paginatedProducts.loadState.refresh) {
        if (paginatedProducts.loadState.refresh !is LoadState.Loading) {
            isRefreshing = false
        }
    }

    var showAddressDialog by remember { mutableStateOf(false) }
    var addressInput by remember { mutableStateOf("") }
    var showFilterSheet by remember { mutableStateOf(false) }

    if (showFilterSheet) {
        FilterSortBottomSheet(
            currentCategory = selectedCategory,
            currentMaxPrice = maxPrice,
            currentLocation = sellerLocation,
            currentSortBy = sortBy,
            onDismiss = { showFilterSheet = false },
            onApply = { category, price, location, sort ->
                viewModel.setFilters(category, price, location, sort)
                showFilterSheet = false
            },
            onReset = {
                viewModel.resetFilters()
                showFilterSheet = false
            }
        )
    }

    var currentPromoIndex by remember { mutableIntStateOf(0) }
    val promoItems = remember {
        listOf(
            PromoItem(
                title = "Upgrade Your\nMobile Life",
                subtitle = "LIMITED OFFER",
                category = "electronics",
                emoji = "🎧",
                color = Color(0xFF064E3B)
            ),
            PromoItem(
                title = "Elegance\nOn Your Wrist",
                subtitle = "EXCLUSIVE DEALS",
                category = "jewelery",
                emoji = "✨",
                color = Color(0xFF10B981)
            ),
            PromoItem(
                title = "Comfort\nIn Every Corner",
                subtitle = "NEW ARRIVALS",
                category = "home",
                emoji = "🛋️",
                color = Color(0xFF1F2937)
            )
        )
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(5000.milliseconds)
            currentPromoIndex = (currentPromoIndex + 1) % promoItems.size
        }
    }

    if (showAddressDialog) {
        AlertDialog(
            onDismissRequest = { showAddressDialog = false },
            title = { Text("Change Delivery Address", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = addressInput,
                    onValueChange = { addressInput = it },
                    label = { Text("Enter your address") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(dimens.cardCorner)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateUserAddress(addressInput)
                        showAddressDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Text("Save Address")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddressDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            if (isAiProcessing) {
                AiListeningAnimation(
                    modifier = Modifier.padding(16.dp)

                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = dimens.padding,
                        end = dimens.padding,
                        top = dimens.extraSmall,
                        bottom = dimens.small
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimens.small)
            ) {
                OutlinedTextField(
                    value = localSearchQuery,
                    onValueChange = { localSearchQuery = it },
                    placeholder = { Text("Search curated shop items...", fontSize = 14.sp) },
                    leadingIcon = {
                        IconButton(onClick = onAiSearchClick) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI Search",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (localSearchQuery.isNotEmpty()) {
                                IconButton(onClick = { localSearchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                            IconButton(
                                onClick = { showFilterSheet = true },
                                modifier = Modifier.testTag("search_filter_button")
                            ) {
                                BadgedBox(
                                    badge = {
                                        if (isFilterActive) {
                                            Badge(
                                                containerColor = MaterialTheme.colorScheme.primary,
                                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.size(10.dp)
                                            )
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FilterList,
                                        contentDescription = "Filter Icon",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(55.dp)
                        .testTag("dashboard_search_bar"),
                    shape = RoundedCornerShape(dimens.cardCorner),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                IconButton(
                    onClick = { viewModel.toggleTheme() },
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(dimens.cardCorner))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                            RoundedCornerShape(dimens.cardCorner)
                        )
                ) {
                    Icon(
                        imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Toggle Theme Icon",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            val categories = listOf("all", "electronics", "fashion", "jewelery", "home")
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = dimens.small),
                contentPadding = PaddingValues(horizontal = dimens.padding),
                horizontalArrangement = Arrangement.spacedBy(dimens.padding)
            ) {
                items(categories) { category ->
                    val isSelected = category == selectedCategory
                    val emoji = when (category) {
                        "electronics" -> "📱"
                        "fashion" -> "👕"
                        "jewelery" -> "✨"
                        "home" -> "🛋️"
                        else -> "🛍️"
                    }

                    Column(
                        modifier = Modifier
                            .clickable { viewModel.setCategory(category) }
                            .testTag("category_pill_$category"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(dimens.extraSmall)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(dimens.logoSize * 1.3f)
                                .clip(RoundedCornerShape(dimens.cardCorner))
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(dimens.cardCorner)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = (dimens.logoSize.value * 0.5f).sp)
                        }
                        Text(
                            text = if (category == "all") "All" else category.replaceFirstChar { it.uppercase() },
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            val pullToRefreshState = rememberPullToRefreshState()
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    isRefreshing = true
                    paginatedProducts.refresh()
                },
                state = pullToRefreshState,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = if (dimens.padding > 24.dp) 200.dp else 160.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = dimens.small)
                        .testTag("home_product_grid"),
                    contentPadding = PaddingValues(top = dimens.small, bottom = dimens.medium),
                    horizontalArrangement = Arrangement.spacedBy(dimens.small),
                    verticalArrangement = Arrangement.spacedBy(dimens.small)
                ) {
                    item(span = { GridItemSpan(this.maxLineSpan) }) {
                        Column {
                            // Active Promo Banner
                            val activePromo = promoItems[currentPromoIndex]
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(
                                                activePromo.color,
                                                activePromo.color.copy(alpha = 0.8f)
                                            )
                                        )
                                    )
                                    .clickable { viewModel.setCategory(activePromo.category) }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .offset(x = 10.dp, y = 10.dp)
                                        .size(130.dp)
                                        .background(Color.White.copy(alpha = 0.08f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        activePromo.emoji,
                                        fontSize = 60.sp,
                                        modifier = Modifier.padding(end = 12.dp, bottom = 12.dp)
                                    )
                                }

                                Column(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .padding(20.dp),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = activePromo.subtitle,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFEADDFF),
                                        letterSpacing = 1.sp
                                    )

                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            text = activePromo.title,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White,
                                            lineHeight = 24.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(20.dp))
                                                .background(Color.White)
                                                .padding(horizontal = 12.dp)
                                        ) {
                                            Text(
                                                text = "Explore Now",
                                                color = activePromo.color,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    promoItems.forEachIndexed { idx, _ ->
                                        Box(
                                            modifier = Modifier
                                                .size(
                                                    if (idx == currentPromoIndex) 16.dp else 8.dp,
                                                    8.dp
                                                )
                                                .clip(CircleShape)
                                                .background(
                                                    if (idx == currentPromoIndex) Color.White else Color.White.copy(
                                                        alpha = 0.4f
                                                    )
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (isFilterActive) {
                        item(span = { GridItemSpan(this.maxLineSpan) }) {
                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                if (selectedCategory != "all") {
                                    item {
                                        FilterBadge(
                                            text = selectedCategory.replaceFirstChar { it.uppercase() },
                                            onRemove = { viewModel.setCategory("all") }
                                        )
                                    }
                                }
                                if (maxPrice < 500f) {
                                    item {
                                        FilterBadge(
                                            text = "Under Rs. ${maxPrice.toInt()}",
                                            onRemove = {
                                                viewModel.setFilters(
                                                    selectedCategory,
                                                    500f,
                                                    sellerLocation,
                                                    sortBy
                                                )
                                            }
                                        )
                                    }
                                }
                                if (sellerLocation != "All Locations") {
                                    item {
                                        FilterBadge(
                                            text = sellerLocation,
                                            onRemove = {
                                                viewModel.setFilters(
                                                    selectedCategory,
                                                    maxPrice,
                                                    "All Locations",
                                                    sortBy
                                                )
                                            }
                                        )
                                    }
                                }
                                if (sortBy != "Relevance") {
                                    item {
                                        FilterBadge(
                                            text = sortBy,
                                            onRemove = {
                                                viewModel.setFilters(
                                                    selectedCategory,
                                                    maxPrice,
                                                    sellerLocation,
                                                    "Relevance"
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    items(
                        count = paginatedProducts.itemCount,
                        key = paginatedProducts.itemKey { it.id }
                    ) { index ->
                        val product = paginatedProducts[index]
                        if (product != null) {
                            ProductCardItem(
                                product = product,
                                onProductClick = { onProductClick(product) },
                                onAddClick = { viewModel.addToCart(product) },
                                viewModel = viewModel,
                                isInCart = cartItemIds.contains(product.id),
                                isFavorited = favoriteIds.contains(product.id)
                            )
                        }
                    }

                    // Handle LoadState for append (bottom of list)
                    if (paginatedProducts.loadState.append is LoadState.Loading) {
                        items(2) { ShimmerProductCard() }
                    }

                    if (paginatedProducts.loadState.refresh is LoadState.Loading && paginatedProducts.itemCount == 0) {
                        items(6) { ShimmerProductCard() }
                    }

                    if (paginatedProducts.loadState.refresh is LoadState.Error && paginatedProducts.itemCount == 0) {
                        val error = paginatedProducts.loadState.refresh as LoadState.Error
                        item(span = { GridItemSpan(this.maxLineSpan) }) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Error: ${error.error.localizedMessage}",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }

                    if (paginatedProducts.loadState.refresh is LoadState.NotLoading && paginatedProducts.itemCount == 0) {
                        item(span = { GridItemSpan(this.maxLineSpan) }) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No items matching current criteria",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilterBadge(text: String, onRemove: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.height(32.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Text(
                text,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                Icons.Default.Close,
                contentDescription = null,
                modifier = Modifier
                    .size(14.dp)
                    .clickable { onRemove() },
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun ShimmerProductCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp)
                    .shimmerEffect()
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Box(
                    modifier = Modifier
                        .width(50.dp)
                        .height(10.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerEffect()
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerEffect()
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerEffect()
                )
                Spacer(modifier = Modifier.weight(1f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(16.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .shimmerEffect()
                    )
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .shimmerEffect()
                    )
                }
            }
        }
    }
}

@Composable
fun ProductCardItem(
    product: ProductDto,
    onProductClick: () -> Unit,
    onAddClick: () -> Unit,
    viewModel: HomeViewModel,
    isInCart: Boolean,
    isFavorited: Boolean
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .clickable { onProductClick() }
            .testTag("product_card_${product.id}"),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(125.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(product.image)
                        .crossfade(true)
                        .build(),
                    contentDescription = product.title,
                    modifier = Modifier.fillMaxHeight(),
                    contentScale = ContentScale.Fit,
                    error = painterResource(id = R.drawable.img_onboarding_ecommerce)
                )

                IconButton(
                    onClick = { viewModel.toggleFavorite(product.id) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 8.dp, y = (-8).dp)
                        .size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorited) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorited) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(12.dp)
            ) {
                Text(
                    text = product.category.uppercase(),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = product.title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = product.description,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 13.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatPrice(product.price),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    IconButton(
                        onClick = onAddClick,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalMall,
                            contentDescription = "Add to Cart",
                            modifier = Modifier.size(18.dp),
                            tint = if (isInCart) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
