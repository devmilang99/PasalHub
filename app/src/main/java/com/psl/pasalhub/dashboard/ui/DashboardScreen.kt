package com.psl.pasalhub.dashboard.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalMall
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.psl.pasalhub.ai.presentation.AiSearchViewModel
import com.psl.pasalhub.core.application.presentation.AppViewModel
import com.psl.pasalhub.core.database.data.CartItem
import com.psl.pasalhub.core.networking.remote.ProductDto
import com.psl.pasalhub.core.viewmodel.MainViewModel
import com.psl.pasalhub.dashboard.cart.ui.CartScreen
import com.psl.pasalhub.dashboard.cart.viewmodel.CartViewModel
import com.psl.pasalhub.dashboard.home.ui.HomeScreen
import com.psl.pasalhub.dashboard.home.viewmodel.HomeViewModel
import com.psl.pasalhub.dashboard.order.ui.OrdersScreen
import com.psl.pasalhub.dashboard.order.viewmodel.OrderViewModel
import com.psl.pasalhub.dashboard.profile.ui.ProfileScreen
import com.psl.pasalhub.dashboard.profile.viewmodel.ProfileViewModel
import com.psl.pasalhub.ui.theme.LocalDimens
import kotlinx.coroutines.flow.debounce
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class, kotlinx.coroutines.FlowPreview::class)
@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    appViewModel: AppViewModel,
    aiViewModel: AiSearchViewModel,
    homeViewModel: HomeViewModel,
    orderViewModel: OrderViewModel,
    cartViewModel: CartViewModel,
    profileViewModel: ProfileViewModel,
    onLogout: () -> Unit,
    onAiSearchClick: () -> Unit = {},
    onFilterClick: () -> Unit = {},
    onProductClick: (ProductDto) -> Unit,
    onOrderReview: (List<CartItem>, Double, Double, Double, Double, String, String, String) -> Unit = { _, _, _, _, _, _, _, _ -> },
    initialTab: Int = 0
) {
    var selectedTab by remember { mutableIntStateOf(initialTab) } // 0: Home, 1: Cart, 2: Order, 3: Profile

    LaunchedEffect(initialTab) {
        selectedTab = initialTab
    }
    val cartItems by cartViewModel.cartItems.collectAsStateWithLifecycle()
    val cartItemCount by remember { derivedStateOf { cartItems.size } }
    val isDark by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val orders by orderViewModel.ordersState.collectAsStateWithLifecycle()
    val isSyncing by appViewModel.isSyncing.collectAsStateWithLifecycle()
    val recentOrdersCount by remember {
        derivedStateOf {
            orders.count {
                it.status in listOf(
                    "Placing",
                    "Placed",
                    "Packaging",
                    "Sent for Delivery"
                )
            }
        }
    }
    val snackbarHostState = remember { SnackbarHostState() }

    val adaptiveInfo = currentWindowAdaptiveInfo()
    val isExpanded = adaptiveInfo.windowSizeClass.isWidthAtLeastBreakpoint(840)
    val isMedium = adaptiveInfo.windowSizeClass.isWidthAtLeastBreakpoint(600) && !isExpanded
    val useNavRail = adaptiveInfo.windowSizeClass.isWidthAtLeastBreakpoint(600)
    val dimens = LocalDimens.current

    val navItemColors = NavigationBarItemDefaults.colors(
        indicatorColor = Color.Transparent,
        selectedIconColor = MaterialTheme.colorScheme.primary,
        selectedTextColor = MaterialTheme.colorScheme.primary,
        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    )

    val navRailItemColors = NavigationRailItemDefaults.colors(
        indicatorColor = Color.Transparent,
        selectedIconColor = MaterialTheme.colorScheme.primary,
        selectedTextColor = MaterialTheme.colorScheme.primary,
        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    )

    LaunchedEffect(Unit) {
        appViewModel.notificationEvent
            .debounce(500.milliseconds)
            .collect { message ->
                // Filter out "Added to Cart" notifications from Dashboard as they are handled in ProductDetailScreen
                if (!message.contains("to your cart", ignoreCase = true)) {
                    snackbarHostState.showSnackbar(
                        message = message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
    }

    BackHandler(enabled = selectedTab != 0) {
        selectedTab = 0
    }

    Row(modifier = Modifier.fillMaxSize()) {
        if (useNavRail) {
            NavigationRail(
                modifier = Modifier
                    .fillMaxHeight()
                    .testTag("navigation_rail"),
                containerColor = MaterialTheme.colorScheme.surface,
                header = {
                    Surface(
                        modifier = Modifier

                            .size(dimens.logoSize)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.ShoppingBag,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(dimens.logoSize * 0.5f)
                            )
                        }
                    }
                }
            ) {
                NavigationRailItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            if (selectedTab == 0) Icons.Default.Home else Icons.Outlined.Home,
                            contentDescription = "Home Tab"
                        )
                    },
                    label = { Text("Home") },
                    modifier = Modifier.testTag("nav_rail_home"),
                    colors = navRailItemColors
                )
                NavigationRailItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (cartItemCount > 0) {
                                    Badge { Text(text = cartItemCount.toString()) }
                                }
                            }
                        ) {
                            Icon(
                                if (selectedTab == 1) Icons.Default.LocalMall else Icons.Outlined.LocalMall,
                                contentDescription = "Cart Tab"
                            )
                        }
                    },
                    label = { Text("Cart") },
                    modifier = Modifier.testTag("nav_rail_cart"),
                    colors = navRailItemColors
                )
                NavigationRailItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (recentOrdersCount > 0) {
                                    Badge { Text(text = recentOrdersCount.toString()) }
                                }
                            }
                        ) {
                            Icon(
                                if (selectedTab == 2) Icons.Default.ReceiptLong else Icons.Outlined.ReceiptLong,
                                contentDescription = "Orders Tab"
                            )
                        }
                    },
                    label = { Text("Orders") },
                    modifier = Modifier.testTag("nav_rail_orders"),
                    colors = navRailItemColors
                )

                Spacer(modifier = Modifier.weight(1f))

                NavigationRailItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = {
                        val currentUser by profileViewModel.currentUser.collectAsStateWithLifecycle()
                        if (currentUser?.profileImage != null) {
                            AsyncImage(
                                model = currentUser?.profileImage,
                                contentDescription = "Profile Avatar",
                                modifier = Modifier
                                    .size(if (dimens.padding > 24.dp) 40.dp else 32.dp)
                                    .clip(CircleShape)
                                    .border(
                                        width = if (selectedTab == 3) 2.dp else 1.dp,
                                        color = if (selectedTab == 3) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                        shape = CircleShape
                                    )
                            )
                        } else {
                            Icon(
                                if (selectedTab == 3) Icons.Default.Person else Icons.Outlined.Person,
                                contentDescription = "Profile Tab"
                            )
                        }
                    },
                    label = { Text("Profile") },
                    modifier = Modifier
                        .testTag("nav_rail_profile")
                        .padding(bottom = 16.dp),
                    colors = navRailItemColors
                )
            }
        }

        Scaffold(
            modifier = Modifier
                .weight(1f)
                .testTag("dashboard_scaffold"),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                if (isSyncing) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Synchronizing your data...",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            },
            bottomBar = {
                val showBottomNav by remember {
                    derivedStateOf {
                        !useNavRail && when {
                            selectedTab == 1 -> cartItems.size <= 3
                            else -> true
                        }
                    }
                }

                if (showBottomNav) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = dimens.padding, vertical = dimens.small)
                            .navigationBarsPadding()
                    ) {
                        NavigationBar(
                            modifier = Modifier
                                .testTag("bottom_navigation_bar")
                                .clip(RoundedCornerShape(dimens.extraLarge))
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(dimens.extraLarge)
                                ),
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                            tonalElevation = 0.dp
                        ) {
                            NavigationBarItem(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                icon = {
                                    Icon(
                                        if (selectedTab == 0) Icons.Default.Home else Icons.Outlined.Home,
                                        contentDescription = "Home Tab"
                                    )
                                },
                                label = { Text("Home", fontSize = 11.sp) },
                                modifier = Modifier.testTag("nav_home_tab"),
                                colors = navItemColors
                            )
                            NavigationBarItem(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                icon = {
                                    BadgedBox(
                                        badge = {
                                            if (cartItemCount > 0) {
                                                Badge { Text(text = cartItemCount.toString()) }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            if (selectedTab == 1) Icons.Default.LocalMall else Icons.Outlined.LocalMall,
                                            contentDescription = "Cart Tab"
                                        )
                                    }
                                },
                                label = { Text("Cart", fontSize = 11.sp) },
                                modifier = Modifier.testTag("nav_cart_tab"),
                                colors = navItemColors
                            )
                            NavigationBarItem(
                                selected = selectedTab == 2,
                                onClick = { selectedTab = 2 },
                                icon = {
                                    BadgedBox(
                                        badge = {
                                            if (recentOrdersCount > 0) {
                                                Badge { Text(text = recentOrdersCount.toString()) }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            if (selectedTab == 2) Icons.AutoMirrored.Filled.ReceiptLong else Icons.AutoMirrored.Outlined.ReceiptLong,
                                            contentDescription = "Orders Tab"
                                        )
                                    }
                                },
                                label = { Text("Orders", fontSize = 11.sp) },
                                modifier = Modifier.testTag("nav_orders_tab"),
                                colors = navItemColors
                            )
                            NavigationBarItem(
                                selected = selectedTab == 3,
                                onClick = { selectedTab = 3 },
                                icon = {
                                    val currentUser by profileViewModel.currentUser.collectAsStateWithLifecycle()
                                    if (currentUser?.profileImage != null) {
                                        AsyncImage(
                                            model = currentUser?.profileImage,
                                            contentDescription = "Profile Avatar",
                                            modifier = Modifier
                                                .size(26.dp)
                                                .clip(CircleShape)
                                                .border(
                                                    width = if (selectedTab == 3) 2.dp else 1.dp,
                                                    color = if (selectedTab == 3) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                                    shape = CircleShape
                                                )
                                        )
                                    } else {
                                        Icon(
                                            if (selectedTab == 3) Icons.Default.Person else Icons.Outlined.Person,
                                            contentDescription = "Profile Tab"
                                        )
                                    }
                                },
                                label = { Text("Profile", fontSize = 11.sp) },
                                modifier = Modifier.testTag("nav_profile_tab"),
                                colors = navItemColors
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (selectedTab) {
                    0 -> HomeScreen(
                        homeViewModel,
                        aiViewModel = aiViewModel,
                        onProductClick = onProductClick,
                        onAiSearchClick = onAiSearchClick
                    )

                    1 -> CartScreen(
                        cartViewModel,
                        onBack = { selectedTab = 0 },
                        onOrderReview = onOrderReview
                    )

                    2 -> OrdersScreen(orderViewModel)
                    3 -> ProfileScreen(
                        profileViewModel,
                        orderViewModel,
                        onLogout,
                        onProductClick = onProductClick
                    )
                }
            }
        }
    }
}
