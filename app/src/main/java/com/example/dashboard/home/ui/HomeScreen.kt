package com.example.dashboard.home.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.ai.presentation.components.AiListeningAnimation
import com.example.ai.presentation.AiSearchViewModel
import kotlinx.coroutines.delay
import com.example.core.networking.remote.ProductDto
import com.example.dashboard.products.repository.Resource
import com.example.dashboard.home.viewmodel.HomeViewModel
import com.example.core.application.utils.screens.formatPrice
import com.example.core.application.utils.screens.shimmerEffect
import com.example.ui.theme.LocalDimens
import kotlin.time.Duration.Companion.milliseconds

data class PromoItem(
    val title: String,
    val subtitle: String,
    val category: String,
    val emoji: String,
    val color: Color
)

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun HomeScreen(
    viewModel: HomeViewModel,
    aiViewModel: AiSearchViewModel,
    onProductClick: (ProductDto) -> Unit,
    onAiSearchClick: () -> Unit = {}
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isDark by viewModel.isDarkTheme.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val productsState by viewModel.homeProductsState.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isAiProcessing by aiViewModel.isAiProcessing.collectAsState()
    val context = LocalContext.current
    val dimens = LocalDimens.current

    var isRefreshing by remember { mutableStateOf(false) }

    // Local state for the text field to ensure smooth typing
    var localSearchQuery by remember { mutableStateOf(searchQuery) }

    // Debounce logic: update ViewModel only after user stops typing for 500ms
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

    var showAddressDialog by remember { mutableStateOf(false) }
    var addressInput by remember { mutableStateOf("") }

    var filterMaxPrice by remember { mutableFloatStateOf(500f) }
    var filterLocation by remember { mutableStateOf("All Locations") }
    var filterCategory by remember { mutableStateOf("all") }
    var filterSortBy by remember { mutableStateOf("Relevance") }
    var showFilterSheet by remember { mutableStateOf(false) }

    val isFilterActive = filterCategory != "all" || filterMaxPrice < 500f || filterLocation != "All Locations" || filterSortBy != "Relevance"

    var currentPromoIndex by remember { mutableIntStateOf(0) }
    val promoItems = remember {
        listOf(
            PromoItem(
                title = "Upgrade Your\nMobile Life",
                subtitle = "LIMITED OFFER",
                category = "electronics",
                emoji = "🎧",
                color = Color(0xFF064E3B) // Deep Forest
            ),
            PromoItem(
                title = "Elegance\nOn Your Wrist",
                subtitle = "EXCLUSIVE DEALS",
                category = "jewelery",
                emoji = "✨",
                color = Color(0xFF10B981) // Emerald
            ),
            PromoItem(
                title = "Comfort\nIn Every Corner",
                subtitle = "NEW ARRIVALS",
                category = "home",
                emoji = "🛋️",
                color = Color(0xFF1F2937) // Deep Charcoal
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
                    }
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

    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = if (isDark) Color(0xFF121212) else Color(0xFFFDFBF7),
            dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Gray.copy(alpha = 0.5f)) },
            shape = RoundedCornerShape(topStart = dimens.large, topEnd = dimens.large)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimens.large)
                    .padding(bottom = 40.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(dimens.large)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Filter & Sort",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isDark) Color.White else Color(0xFF0C1324)
                    )
                    TextButton(
                        onClick = {
                            filterCategory = "all"
                            filterMaxPrice = 500f
                            filterLocation = "All Locations"
                            filterSortBy = "Relevance"
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF10B981))
                    ) {
                        Text("Reset All", fontWeight = FontWeight.Bold)
                    }
                }

                // Category Section
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "CATEGORY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Gray,
                        letterSpacing = 1.5.sp
                    )
                    val categoriesList = listOf("all", "electronics", "fashion", "jewelery", "home")
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(end = 16.dp)
                    ) {
                        items(categoriesList) { cat ->
                            val isSelected = filterCategory == cat
                            FilterChip(
                                selected = isSelected,
                                onClick = { filterCategory = cat },
                                label = {
                                    Text(
                                        cat.replaceFirstChar { it.uppercase() },
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = if (isDark) Color(0xFF1E1E1E) else Color(0xFFE2E4E9),
                                    labelColor = Color.Gray,
                                    selectedContainerColor = Color(0xFF10B981).copy(alpha = 0.15f),
                                    selectedLabelColor = Color(0xFF10B981)
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = Color.Gray.copy(alpha = 0.2f),
                                    selectedBorderColor = Color(0xFF10B981)
                                )
                            )
                        }
                    }
                }

                // Price Range Section
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "PRICE RANGE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.Gray,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            text = if (filterMaxPrice >= 500f) "Any Price" else "Up to Rs. ${filterMaxPrice.toInt()}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = Color(0xFF10B981)
                        )
                    }
                    Slider(
                        value = filterMaxPrice,
                        onValueChange = { filterMaxPrice = it },
                        valueRange = 10f..500f,
                        steps = 49,
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF10B981),
                            activeTrackColor = Color(0xFF10B981),
                            inactiveTrackColor = if (isDark) Color(0xFF252528) else Color(0xFFE9ECEF)
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Rs. 10", fontSize = 11.sp, color = Color.Gray)
                        Text("Rs. 500+", fontSize = 11.sp, color = Color.Gray)
                    }
                }

                // Seller Location Hub
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "SELLER LOCATION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Gray,
                        letterSpacing = 1.5.sp
                    )
                    val locationFilters = listOf("All Locations", "Online Only", "Physical Stores")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        locationFilters.forEach { loc ->
                            val isSelected = filterLocation == loc
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { filterLocation = loc },
                                color = if (isSelected) Color(0xFF10B981).copy(alpha = 0.15f) else (if (isDark) Color(0xFF1E1E1E) else Color(0xFFE2E4E9)),
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = if (isSelected) Color(0xFF10B981) else Color.Transparent
                                )
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        loc,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color(0xFF10B981) else Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                // Sort By Section
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "SORT BY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Gray,
                        letterSpacing = 1.5.sp
                    )
                    val sortOptions = listOf("Relevance", "Price: Low to High", "Price: High to Low")
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        sortOptions.forEach { opt ->
                            val isSelected = filterSortBy == opt
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { filterSortBy = opt },
                                color = if (isSelected) Color(0xFF10B981).copy(alpha = 0.15f) else (if (isDark) Color(0xFF1E1E1E) else Color(0xFFE2E4E9)),
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = if (isSelected) Color(0xFF10B981) else Color.Transparent
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        opt,
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color(0xFF10B981) else (if (isDark) Color.White else Color(0xFF0C1324))
                                    )
                                    if (isSelected) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color(0xFF10B981),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        viewModel.setCategory(filterCategory)
                        showFilterSheet = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Text("Apply Changes", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (isAiProcessing) {
            AiListeningAnimation(
                modifier = Modifier.padding(16.dp),
                text = "PasalHub AI is analyzing your request..."
            )
        }
        
                Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimens.padding, vertical = dimens.small),
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
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    imeAction = androidx.compose.ui.text.input.ImeAction.Search
                ),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                    onSearch = { aiViewModel.performAiSearch(localSearchQuery) }
                ),
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
                    .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), RoundedCornerShape(dimens.cardCorner))
                    .testTag("dashboard_theme_toggle_button")
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
                        .clickable {
                            viewModel.setCategory(category)
                            filterCategory = category
                        }
                        .testTag("category_pill_$category"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(dimens.extraSmall)
                ) {
                    Box(
                        modifier = Modifier
                            .size(dimens.logoSize * 1.3f)
                            .clip(RoundedCornerShape(dimens.cardCorner))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                            .border(
                                width = 1.dp,
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
                viewModel.refreshProducts()
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
                    .padding(horizontal = dimens.small),
                contentPadding = PaddingValues(top = dimens.small, bottom = dimens.medium),
                horizontalArrangement = Arrangement.spacedBy(dimens.small),
                verticalArrangement = Arrangement.spacedBy(dimens.small)
            ) {
                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(this.maxLineSpan) }) {
                    val activePromo = promoItems[currentPromoIndex]
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(activePromo.color, activePromo.color.copy(alpha = 0.8f))
                                )
                            )
                            .clickable { viewModel.setCategory(activePromo.category) }
                            .testTag("promo_banner_container")
                    ) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset(x = 10.dp, y = 10.dp)
                                .size(130.dp)
                                .background(Color.White.copy(alpha = 0.08f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(activePromo.emoji, fontSize = 60.sp, modifier = Modifier.padding(end = 12.dp, bottom = 12.dp))
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
                                        .size(if (idx == currentPromoIndex) 16.dp else 8.dp, 8.dp)
                                        .clip(CircleShape)
                                        .background(if (idx == currentPromoIndex) Color.White else Color.White.copy(alpha = 0.4f))
                                        .clickable { currentPromoIndex = idx }
                                )
                            }
                        }
                    }
                }

                if (isFilterActive) {
                    item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(this.maxLineSpan) }) {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            if (filterCategory != "all") {
                                item {
                                    FilterChip(
                                        selected = true,
                                        onClick = { 
                                            filterCategory = "all"
                                            viewModel.setCategory("all")
                                        },
                                        label = { Text(filterCategory.replaceFirstChar { it.uppercase() }, fontSize = 12.sp) },
                                        trailingIcon = { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                            selectedLabelColor = MaterialTheme.colorScheme.primary,
                                            selectedTrailingIconColor = MaterialTheme.colorScheme.primary
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }
                            }
                            if (filterMaxPrice < 500f) {
                                item {
                                    FilterChip(
                                        selected = true,
                                        onClick = { filterMaxPrice = 500f },
                                        label = { Text("Under Rs. ${filterMaxPrice.toInt()}", fontSize = 12.sp) },
                                        trailingIcon = { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                            selectedLabelColor = MaterialTheme.colorScheme.primary,
                                            selectedTrailingIconColor = MaterialTheme.colorScheme.primary
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }
                            }
                            if (filterLocation != "All Locations") {
                                item {
                                    FilterChip(
                                        selected = true,
                                        onClick = { filterLocation = "All Locations" },
                                        label = { Text(filterLocation, fontSize = 12.sp) },
                                        trailingIcon = { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                            selectedLabelColor = MaterialTheme.colorScheme.primary,
                                            selectedTrailingIconColor = MaterialTheme.colorScheme.primary
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }
                            }
                            if (filterSortBy != "Relevance") {
                                item {
                                    FilterChip(
                                        selected = true,
                                        onClick = { filterSortBy = "Relevance" },
                                        label = { Text(filterSortBy, fontSize = 12.sp) },
                                        trailingIcon = { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                            selectedLabelColor = MaterialTheme.colorScheme.primary,
                                            selectedTrailingIconColor = MaterialTheme.colorScheme.primary
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                when (val resource = productsState) {
                    is Resource.Loading -> {
                        items(4) {
                            ShimmerProductCard()
                        }
                    }
                    is Resource.Error -> {
                        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(this.maxLineSpan) }) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Error loading data: ${resource.message}", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                    is Resource.Success -> {
                        val products = resource.data
                        val priceFiltered = products.filter { it.price <= filterMaxPrice }
                        val locationFiltered = when (filterLocation) {
                            "Online Only" -> priceFiltered.filter { it.id % 2 == 0 }
                            "Physical Stores" -> priceFiltered.filter { it.id % 2 != 0 }
                            else -> priceFiltered
                        }
                        val sortedProducts = when (filterSortBy) {
                            "Price: Low to High" -> locationFiltered.sortedBy { it.price }
                            "Price: High to Low" -> locationFiltered.sortedByDescending { it.price }
                            else -> locationFiltered
                        }

                        if (sortedProducts.isEmpty()) {
                            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(this.maxLineSpan) }) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No curated items matching current filters", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        } else {
                            items(sortedProducts, key = { it.id }) { product ->
                                ProductCardItem(
                                    product = product,
                                    onProductClick = { onProductClick(product) },
                                    onAddClick = {
                                        viewModel.addToCart(product)
                                    },
                                    viewModel = viewModel
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
    viewModel: HomeViewModel
) {
    val context = LocalContext.current
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    val isFavorited = favoriteIds.contains(product.id)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .clickable { onProductClick() }
            .testTag("product_card_${product.id}"),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
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
                    onClick = { 
                        viewModel.toggleFavorite(product.id)
                    },
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
                        modifier = Modifier
                            .size(30.dp)
                            .testTag("add_to_cart_button_${product.id}"),
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalMall, 
                            contentDescription = "Add to Cart Icon", 
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}
