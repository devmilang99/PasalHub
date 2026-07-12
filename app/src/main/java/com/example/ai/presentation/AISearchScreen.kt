package com.example.ai.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ai.presentation.components.AiListeningAnimation
import com.example.core.application.utils.screens.formatPrice
import com.example.core.networking.remote.ProductDto
import com.example.dashboard.products.repository.Resource
import com.example.core.viewmodel.MainViewModel
import com.example.ui.theme.LocalDimens

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AISearchScreen(
    viewModel: MainViewModel,
    aiViewModel: AiSearchViewModel,
    onBackClick: () -> Unit,
    onProductClick: (ProductDto) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val dimens = LocalDimens.current
    val isAiProcessing by aiViewModel.isAiProcessing.collectAsState()
    val aiSearchError by aiViewModel.aiSearchError.collectAsState()
    val productsState by aiViewModel.aiProductsState.collectAsState()
    val recommendationMessage by aiViewModel.recommendationMessage.collectAsState()
    val searchHistory by aiViewModel.searchHistory.collectAsState()
    val isDark by viewModel.isDarkTheme.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    
    val user by viewModel.currentUser.collectAsState()
    val username = user?.name?.split(" ")?.firstOrNull() ?: "Explorer"
    
    var showHistorySheet by remember { mutableStateOf(false) }
    var isSearchFocused by remember { mutableStateOf(false) }
    val isKeyboardVisible = WindowInsets.isImeVisible

    val bgBrush = Brush.linearGradient(
        colors = if (isDark) listOf(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
            MaterialTheme.colorScheme.surface
        ) else listOf(
            Color(0xFFF8FAFC),
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
            Color(0xFFF8FAFC)
        )
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                Icons.Rounded.AutoAwesome, 
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "AI Assistant", 
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            searchQuery = ""
                            aiViewModel.clearSearch()
                            onBackClick()
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showHistorySheet = true },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(Icons.Rounded.History, contentDescription = "History")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgBrush)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = dimens.padding)
                    .imePadding()
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    AnimatedContent(
                        targetState = isAiProcessing to productsState,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(500)) togetherWith fadeOut(animationSpec = tween(500))
                        },
                        label = "content_transition"
                    ) { (processing, state) ->
                        if (processing) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                AiListeningAnimation(
                                    modifier = Modifier.fillMaxWidth(0.8f),
                                    text = "Analyzing your intent..."
                                )
                            }
                        } else if (aiSearchError != null) {
                            ErrorStateView(error = aiSearchError!!) {
                                aiViewModel.performAiSearch("best deals")
                            }
                        } else {
                            ResultsOrHistory(
                                productsState = state,
                                recommendationMessage = recommendationMessage,
                                searchQuery = searchQuery,
                                username = username,
                                isDark = isDark,
                                onProductClick = onProductClick,
                                onQuickActionClick = { 
                                    searchQuery = it
                                    aiViewModel.performAiSearch(it)
                                }
                            )
                        }
                    }
                }

                // Modern Search Bar
                SearchInputBar(
                    query = searchQuery,
                    onQueryChange = { 
                        searchQuery = it
                        if (it.isEmpty()) aiViewModel.clearSearch()
                    },
                    isProcessing = isAiProcessing,
                    onFocusChange = { isSearchFocused = it },
                    onSearch = {
                        if (searchQuery.isNotBlank()) {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            aiViewModel.performAiSearch(searchQuery)
                        }
                    }
                )

                // Suggestions below search bar when focused and keyboard is visible
                AnimatedVisibility(
                    visible = isSearchFocused && isKeyboardVisible && !isAiProcessing,
                    enter = slideInVertically { -it } + fadeIn(),
                    exit = slideOutVertically { -it } + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 16.dp)
                            .background(
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                RoundedCornerShape(16.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Text(
                            "SUGGESTIONS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Column(
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(
                                "Nike White Sneakers under 200",
                                "Noise Cancelling Headphones",
                                "Organic Cotton T-Shirt",
                                "Gold Plated Necklace",
                                "HomeMaster Air Purifier"
                            ).forEach { suggestion ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { 
                                            searchQuery = suggestion
                                            aiViewModel.performAiSearch(suggestion)
                                            focusManager.clearFocus()
                                            isSearchFocused = false
                                        }
                                        .padding(vertical = 10.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Rounded.Search,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = suggestion,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showHistorySheet) {
            ModalBottomSheet(
                onDismissRequest = { showHistorySheet = false },
                sheetState = rememberModalBottomSheetState(),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 48.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Recent Searches",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (searchHistory.isNotEmpty()) {
                            TextButton(onClick = { aiViewModel.clearHistory() }) {
                                Text("Clear All", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (searchHistory.isEmpty()) {
                        Text(
                            "No recent searches yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    } else {
                        searchHistory.forEach { historyItem ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        searchQuery = historyItem
                                        aiViewModel.performAiSearch(historyItem)
                                        showHistorySheet = false
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Rounded.History,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = historyItem,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchInputBar(
    query: String,
    onQueryChange: (String) -> Unit,
    isProcessing: Boolean,
    onFocusChange: (Boolean) -> Unit,
    onSearch: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val borderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = LocalDimens.current.small, top = LocalDimens.current.small),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        shadowElevation = 8.dp,
        border = if (isProcessing) BorderStroke(
            2.dp, 
            MaterialTheme.colorScheme.primary.copy(alpha = borderAlpha)
        ) else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Rounded.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 12.dp).size(24.dp)
            )
            
            TextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { 
                    Text(
                        "Ask anything to find products...", 
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    ) 
                },
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged { onFocusChange(it.isFocused) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                maxLines = 5,
                enabled = !isProcessing
            )

            AnimatedVisibility(visible = query.isNotEmpty() && !isProcessing) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Rounded.Close, contentDescription = "Clear", modifier = Modifier.size(20.dp))
                }
            }

            IconButton(
                onClick = onSearch,
                enabled = !isProcessing && query.isNotBlank(),
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (query.isNotBlank() && !isProcessing) MaterialTheme.colorScheme.primary 
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        CircleShape
                    )
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.Send,
                    contentDescription = "Send",
                    tint = if (query.isNotBlank() && !isProcessing) MaterialTheme.colorScheme.onPrimary
                           else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ResultsOrHistory(
    productsState: Resource<List<ProductDto>>,
    recommendationMessage: String?,
    searchQuery: String,
    username: String,
    isDark: Boolean,
    onProductClick: (ProductDto) -> Unit,
    onQuickActionClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(LocalDimens.current.small),
        contentPadding = PaddingValues(top = 4.dp, bottom = LocalDimens.current.medium)
    ) {
        when {
            productsState is Resource.Success && productsState.data.isNotEmpty() -> {
                val products = productsState.data
                recommendationMessage?.let { message ->
                    item {
                        AiResponseBubble(message = message)
                    }
                }

                item {
                    Text(
                        "Top recommendations",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }

                items(products) { product ->
                    AiResultProductCard(
                        product = product,
                        onProductClick = { onProductClick(product) },
                        isDark = isDark
                    )
                }
            }
            productsState is Resource.Success && searchQuery.isNotEmpty() -> {
                item { EmptyResultsView(query = searchQuery) }
            }
            productsState is Resource.Error -> {
                item { Text("Error loading results", modifier = Modifier.padding(16.dp)) }
            }
            productsState is Resource.Loading -> {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            else -> {
                // Initial State: Welcome Message
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = LocalDimens.current.large),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = CircleShape,
                            modifier = Modifier.size(LocalDimens.current.logoSize)
                        ) {
                            Icon(
                                Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.padding(LocalDimens.current.small),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Hi, $username!",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Text(
                            text = "What can I help you find today?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                item {
                    QuickActionCards { query ->
                        onQuickActionClick(query)
                    }
                }
            }
        }

        item {
            BrandingFooter()
        }
    }
}

@Composable
fun QuickActionCards(onActionClick: (String) -> Unit) {
    val actions = listOf(
        Triple("Latest tech", Icons.Rounded.Devices, "Latest electronics"),
        Triple("Summer fashion", Icons.Rounded.Checkroom, "Organic clothing"),
        Triple("Gaming gear", Icons.Rounded.SportsEsports, "Best gaming gear"),
        Triple("Home decor", Icons.Rounded.Home, "Modern appliances")
    )

    val actionQueries = mapOf(
        "Latest tech" to "latest PasalHub electronics",
        "Summer fashion" to "Organic cotton clothing",
        "Gaming gear" to "PasalHub gaming gear",
        "Home decor" to "HomeMaster modern appliances"
    )

    Column(modifier = Modifier.padding(top = LocalDimens.current.medium)) {
        Text(
            "Quick Explore",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(LocalDimens.current.small))
        Row(
            horizontalArrangement = Arrangement.spacedBy(LocalDimens.current.small),
            modifier = Modifier.fillMaxWidth()
        ) {
            actions.take(2).forEach { (label, icon, subtitle) ->
                ActionCard(label, subtitle, icon, modifier = Modifier.weight(1f)) { 
                    onActionClick(actionQueries[label] ?: label) 
                }
            }
        }
        Spacer(modifier = Modifier.height(LocalDimens.current.small))
        Row(
            horizontalArrangement = Arrangement.spacedBy(LocalDimens.current.small),
            modifier = Modifier.fillMaxWidth()
        ) {
            actions.drop(2).forEach { (label, icon, subtitle) ->
                ActionCard(label, subtitle, icon, modifier = Modifier.weight(1f)) { 
                    onActionClick(actionQueries[label] ?: label) 
                }
            }
        }
    }
}

@Composable
fun ActionCard(label: String, subtitle: String, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val dimens = LocalDimens.current
    Surface(
        onClick = onClick,
        modifier = modifier.height(if (dimens.padding > 24.dp) 120.dp else 95.dp),
        shape = RoundedCornerShape(dimens.cardCorner),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(dimens.small),
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(if (dimens.padding > 24.dp) 24.dp else 20.dp))
            Spacer(modifier = Modifier.height(dimens.extraSmall))
            Text(label, style = if (dimens.padding > 24.dp) MaterialTheme.typography.titleSmall else MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                text = "\"$subtitle\"",
                style = (if (dimens.padding > 24.dp) MaterialTheme.typography.bodySmall else MaterialTheme.typography.labelSmall).copy(fontStyle = FontStyle.Italic),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun AiResponseBubble(message: String) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 24.dp, bottomStart = 24.dp, bottomEnd = 24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Icon(
                Icons.Rounded.AutoAwesome,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun AiResultProductCard(
    product: ProductDto,
    onProductClick: () -> Unit,
    isDark: Boolean
) {
    val dimens = LocalDimens.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (dimens.padding > 24.dp) 180.dp else 165.dp)
            .clip(RoundedCornerShape(dimens.cardCorner))
            .clickable { onProductClick() },
        shape = RoundedCornerShape(dimens.cardCorner),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Image Section with subtle gradient
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(if (dimens.padding > 24.dp) 140.dp else 120.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = if (isDark) listOf(Color(0xFF2C2C2E), Color(0xFF1C1C1E))
                                     else listOf(Color(0xFFF8FAFC), Color(0xFFF1F5F9))
                        )
                    )
                    .padding(12.dp)
            ) {
                AsyncImage(
                    model = product.image,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
                
                // AI Badge
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Text(
                        "AI MATCH",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(dimens.small)
            ) {
                Text(
                    text = product.category.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
                
                Text(
                    text = product.title,
                    style = if (dimens.padding > 24.dp) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = if (dimens.padding > 24.dp) 22.sp else 18.sp
                )

                Text(
                    text = product.description,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 14.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
                
                Spacer(modifier = Modifier.weight(1f))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 2.dp)
                ) {
                    Icon(
                        Icons.Default.Storefront,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Official Store",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "Price",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Text(
                            text = formatPrice(product.price),
                            style = if (dimens.padding > 24.dp) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        modifier = Modifier
                            .size(36.dp)
                            .clickable { onProductClick() }
                    ) {
                        Icon(
                            Icons.Rounded.ChevronRight,
                            contentDescription = "Details",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ErrorStateView(error: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            shape = CircleShape,
            modifier = Modifier.size(64.dp)
        ) {
            Icon(
                Icons.Rounded.ErrorOutline,
                contentDescription = null,
                modifier = Modifier.padding(16.dp),
                tint = MaterialTheme.colorScheme.error
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Something went wrong",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            error,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Try again")
        }
    }
}

@Composable
fun EmptyResultsView(query: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Rounded.SearchOff,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "No results found for \"$query\"",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Try rephrasing your question for better AI results.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(start = 32.dp, end = 32.dp, top = 4.dp)
        )
    }
}

@Composable
fun BrandingFooter() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.alpha(0.5f)
        ) {
            Icon(
                Icons.Rounded.AutoAwesome,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Powered by Gemini AI",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
