package com.psl.pasalhub.ai.presentation

import android.Manifest
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Checkroom
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.psl.pasalhub.ai.domain.model.AiChatMessage
import com.psl.pasalhub.ai.presentation.components.AiChatBubble
import com.psl.pasalhub.ai.presentation.components.AiListeningAnimation
import com.psl.pasalhub.ai.presentation.components.MovingGradientsBackground
import com.psl.pasalhub.ai.presentation.components.UserChatBubble
import com.psl.pasalhub.core.application.utils.screens.formatPrice
import com.psl.pasalhub.core.networking.remote.ProductDto
import com.psl.pasalhub.core.viewmodel.MainViewModel
import com.psl.pasalhub.dashboard.products.repository.Resource
import com.psl.pasalhub.ui.theme.LocalDimens

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalLayoutApi::class,
    ExperimentalPermissionsApi::class
)
@Composable
fun AISearchScreen(
    viewModel: MainViewModel,
    aiViewModel: AiSearchViewModel,
    onBackClick: () -> Unit,
    onProductClick: (ProductDto) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val dimens = LocalDimens.current
    val isAiProcessing by aiViewModel.isAiProcessing.collectAsStateWithLifecycle()
    val aiSearchError by aiViewModel.aiSearchError.collectAsStateWithLifecycle()
    val productsState by aiViewModel.aiProductsState.collectAsStateWithLifecycle()
    val messages by aiViewModel.messages.collectAsStateWithLifecycle()
    val searchHistory by aiViewModel.searchHistory.collectAsStateWithLifecycle()
    val isDark by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let { aiViewModel.performVisualSearch(it) }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, it)
                ImageDecoder.decodeBitmap(source)
            } else {
                @Suppress("DEPRECATION")
                val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                // getBitmap doesn't guarantee software config or mutability needed for AI processing sometimes,
                // so we ensure it's in a standard format if needed, though usually, this suffices for basic migration.
                // The branch for P+ above is the modern way.
                bitmap
            }
            aiViewModel.performVisualSearch(bitmap)
        }
    }

    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    val username = user?.name?.split(" ")?.firstOrNull() ?: "Explorer"

    var showHistorySheet by remember { mutableStateOf(false) }
    var isSearchFocused by remember { mutableStateOf(false) }
    val isKeyboardVisible = WindowInsets.isImeVisible

    LaunchedEffect(aiSearchError) {
        if (aiSearchError != null) {
            Log.e("AISearchScreen", "Displaying AI Search error: $aiSearchError")
        }
    }

    BackHandler {
        if (messages.isNotEmpty()) {
            aiViewModel.clearSearch()
        } else {
            searchQuery = ""
            onBackClick()
        }
    }

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
                            if (messages.isNotEmpty()) {
                                aiViewModel.clearSearch()
                            } else {
                                searchQuery = ""
                                onBackClick()
                            }
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Icon(
                            if (messages.isNotEmpty()) Icons.Rounded.Close else Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = if (messages.isNotEmpty()) "Clear Search" else "Back"
                        )
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
                .padding(paddingValues)
        ) {
            MovingGradientsBackground(isProcessing = isAiProcessing, isDark = isDark)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .consumeWindowInsets(paddingValues)
                    .padding(horizontal = dimens.padding)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    if (aiSearchError != null) {
                        ErrorStateView(error = aiSearchError!!) {
                            aiViewModel.performAiSearch("best deals")
                        }
                    } else {
                        ResultsOrHistory(
                            messages = messages,
                            isAiProcessing = isAiProcessing,
                            productsState = productsState,
                            searchQuery = searchQuery,
                            username = username,
                            isDark = isDark,
                            onProductClick = onProductClick,
                            onQuickActionClick = {
                                aiViewModel.performAiSearch(it)
                                searchQuery = ""
                            }
                        )
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
                    onCameraClick = {
                        if (cameraPermissionState.status.isGranted) {
                            cameraLauncher.launch()
                        } else {
                            cameraPermissionState.launchPermissionRequest()
                        }
                    },
                    onGalleryClick = {
                        galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    onSearch = {
                        if (searchQuery.isNotBlank()) {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            aiViewModel.performAiSearch(searchQuery)
                            searchQuery = ""
                        }
                    }
                )

                // Branding Footer will appear here if needed or at the bottom of the list
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
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
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
                modifier = Modifier
                    .padding(start = 12.dp)
                    .size(24.dp)
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

            if (query.isEmpty() && !isProcessing) {
                IconButton(onClick = onCameraClick) {
                    Icon(
                        Icons.Rounded.PhotoCamera,
                        contentDescription = "Camera",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    )
                }
                IconButton(onClick = onGalleryClick) {
                    Icon(
                        Icons.Rounded.Image,
                        contentDescription = "Gallery",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    )
                }
            }

            AnimatedVisibility(visible = query.isNotEmpty() && !isProcessing) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        Icons.Rounded.Close,
                        contentDescription = "Clear",
                        modifier = Modifier.size(20.dp)
                    )
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
    messages: List<AiChatMessage>,
    isAiProcessing: Boolean,
    productsState: Resource<List<ProductDto>>,
    searchQuery: String,
    username: String,
    isDark: Boolean,
    onProductClick: (ProductDto) -> Unit,
    onQuickActionClick: (String) -> Unit
) {
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    LaunchedEffect(messages.size, isAiProcessing) {
        if (messages.isNotEmpty() || isAiProcessing) {
            val totalItems = messages.size + (if (isAiProcessing) 1 else 0)
            if (totalItems > 0) {
                listState.animateScrollToItem(totalItems - 1)
            }
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(LocalDimens.current.small),
        contentPadding = PaddingValues(top = 4.dp, bottom = LocalDimens.current.medium)
    ) {
        // Show chat messages history
        items(messages, key = { it.id }) { message ->
            if (message.isUser) {
                UserChatBubble(message = message)
            } else {
                AiChatBubble(message = message.text ?: "")
            }
        }

        when (productsState) {
            is Resource.Success if productsState.data.isNotEmpty() -> {
                val products = productsState.data
                item {
                    Text(
                        "Top recommendations",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }

                items(products, key = { it.id }) { product ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(animationSpec = tween(600)) + slideInVertically { 50 },
                        label = "product_entry"
                    ) {
                        AiResultProductCard(
                            product = product,
                            onProductClick = { onProductClick(product) },
                            isDark = isDark
                        )
                    }
                }
            }

            is Resource.Success if messages.isNotEmpty() && !isAiProcessing -> {
                // Show empty view if we don't have products for the query and AI is done thinking
                item {
                    val lastUserQuery = messages.lastOrNull { it.isUser }?.text ?: ""
                    EmptyResultsView(query = lastUserQuery)
                }
            }

            is Resource.Error -> {
                item { Text("Error loading results", modifier = Modifier.padding(16.dp)) }
            }

            is Resource.Loading -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            else -> {
                if (messages.isEmpty()) {
                    // Initial State: Welcome Message
                    item {
                        var visible by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) { visible = true }

                        AnimatedVisibility(
                            visible = visible,
                            enter = fadeIn(tween(1000)) + slideInVertically(tween(1000)) { 40 }
                        ) {
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
                    }
                    item {
                        QuickActionCards { query ->
                            onQuickActionClick(query)
                        }
                    }
                }
            }
        }

        if (isAiProcessing) {
            item(key = "processing_animation") {
                AiListeningAnimation(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                )
            }
        }

        if (messages.isEmpty() && !isAiProcessing) {
            item {
                BrandingFooter()
            }
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
        "Latest tech" to "Latest electronics",
        "Summer fashion" to "Summer fashion and clothing",
        "Gaming gear" to "Gaming gear and accessories",
        "Home decor" to "Home decor and appliances"
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
fun ActionCard(
    label: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
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
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(if (dimens.padding > 24.dp) 24.dp else 20.dp)
            )
            Spacer(modifier = Modifier.height(dimens.extraSmall))
            Text(
                label,
                style = if (dimens.padding > 24.dp) MaterialTheme.typography.titleSmall else MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "\"$subtitle\"",
                style = (if (dimens.padding > 24.dp) MaterialTheme.typography.bodySmall else MaterialTheme.typography.labelSmall).copy(
                    fontStyle = FontStyle.Italic
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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
    LaunchedEffect(error) {
        Log.d("AISearchScreen", "ErrorStateView shown with error: $error")
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
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
            "No direct matches found",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            "We couldn't find items for \"$query\". Try using broader keywords or checking another category.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
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
