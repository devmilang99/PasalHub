package com.example.dashboard.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import coil.compose.AsyncImage
import com.example.core.networking.remote.ProductDto
import com.example.ai.presentation.AiSearchViewModel
import com.example.core.application.presentation.AppViewModel
import com.example.dashboard.cart.ui.CartScreen
import com.example.dashboard.cart.viewmodel.CartViewModel
import com.example.dashboard.home.ui.HomeScreen
import com.example.dashboard.home.viewmodel.HomeViewModel
import com.example.dashboard.order.ui.OrdersScreen
import com.example.dashboard.order.viewmodel.OrderViewModel
import com.example.dashboard.profile.ui.ProfileScreen
import com.example.dashboard.profile.viewmodel.ProfileViewModel
import com.example.core.application.utils.screens.PasalHubAlertDialog
import com.example.core.application.utils.screens.SuccessScreen
import com.example.core.viewmodel.MainViewModel
import com.example.ui.theme.LocalDimens

@OptIn(ExperimentalMaterial3Api::class)
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
    onProductClick: (ProductDto) -> Unit,
    initialTab: Int = 0
) {
    var selectedTab by remember { mutableIntStateOf(initialTab) } // 0: Home, 1: Cart, 2: Order, 3: Profile

    LaunchedEffect(initialTab) {
        selectedTab = initialTab
    }
    val cartItems by cartViewModel.cartItems.collectAsState()
    val cartItemCount = cartItems.size
    val isDark by viewModel.isDarkTheme.collectAsState()
    val orders by orderViewModel.ordersState.collectAsState()
    val recentOrdersCount = orders.count { it.status in listOf("Placing", "Placed", "Packaging", "Sent for Delivery") }
    var notificationMessage by remember { mutableStateOf<String?>(null) }
    var showNotificationDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val adaptiveInfo = currentWindowAdaptiveInfo()
    val isExpanded = adaptiveInfo.windowSizeClass.isWidthAtLeastBreakpoint(840)
    val isMedium = adaptiveInfo.windowSizeClass.isWidthAtLeastBreakpoint(600) && !isExpanded
    val useNavRail = adaptiveInfo.windowSizeClass.isWidthAtLeastBreakpoint(600)
    val dimens = LocalDimens.current

    LaunchedEffect(Unit) {
        appViewModel.notificationEvent.collect { message ->
            if (message.contains("to your cart", ignoreCase = true)) {
                snackbarHostState.showSnackbar(
                    message = message,
                    duration = SnackbarDuration.Short
                )
            } else {
                notificationMessage = message
                showNotificationDialog = true
            }
        }
    }

    if (showNotificationDialog && notificationMessage != null) {
        val isOrderSuccess = notificationMessage!!.contains("placed", ignoreCase = true) || 
                             notificationMessage!!.contains("confirmed", ignoreCase = true)

        if (isOrderSuccess) {
            SuccessScreen(
                message = notificationMessage!!,
                onContinue = { 
                    showNotificationDialog = false
                    notificationMessage = null 
                },
                isDark = isDark
            )
        } else {
            PasalHubAlertDialog(
                onDismissRequest = { 
                    showNotificationDialog = false
                    notificationMessage = null
                },
                title = "Notification",
                text = notificationMessage!!,
                confirmButtonText = "Dismiss",
                icon = if (notificationMessage!!.contains("added", ignoreCase = true)) Icons.Default.ShoppingCart else Icons.Default.Notifications,
                isDark = isDark
            )
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
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .testTag("navigation_rail"),
                containerColor = MaterialTheme.colorScheme.surface,
                header = {
                    Surface(
                        modifier = Modifier
                            .padding(top = dimens.medium, bottom = dimens.extraLarge)
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
                    icon = { Icon(if (selectedTab == 0) Icons.Default.Home else Icons.Outlined.Home, contentDescription = "Home Tab") },
                    label = { Text("Home") },
                    modifier = Modifier.testTag("nav_rail_home")
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
                            Icon(if (selectedTab == 1) Icons.Default.LocalMall else Icons.Outlined.LocalMall, contentDescription = "Cart Tab")
                        }
                    },
                    label = { Text("Cart") },
                    modifier = Modifier.testTag("nav_rail_cart")
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
                            Icon(if (selectedTab == 2) Icons.Default.ReceiptLong else Icons.Outlined.ReceiptLong, contentDescription = "Orders Tab")
                        }
                    },
                    label = { Text("Orders") },
                    modifier = Modifier.testTag("nav_rail_orders")
                )
                
                Spacer(modifier = Modifier.weight(1f))

                NavigationRailItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = {
                        val currentUser by profileViewModel.currentUser.collectAsState()
                        val avatarUrl = currentUser?.profileImage ?: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=150&q=80"
                        AsyncImage(
                            model = avatarUrl,
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
                    },
                    label = { Text("Profile") },
                    modifier = Modifier
                        .testTag("nav_rail_profile")
                        .padding(bottom = 16.dp)
                )
            }
        }

        Scaffold(
            modifier = Modifier
                .weight(1f)
                .testTag("dashboard_scaffold"),
            snackbarHost = { 
                SnackbarHost(hostState = snackbarHostState) { data ->
                    Snackbar(
                        modifier = Modifier.padding(horizontal = dimens.small, vertical = dimens.small),
                        shape = RoundedCornerShape(dimens.cardCorner),
                        containerColor = if (isDark) Color(0xFF1E1E20) else Color(0xFF323232),
                        contentColor = Color.White,
                        actionContentColor = MaterialTheme.colorScheme.primary
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(data.visuals.message, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            },
            bottomBar = {
                val showBottomNav = !useNavRail && when {
                    selectedTab == 1 -> cartItems.size <= 3
                    else -> true
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
                                icon = { Icon(if (selectedTab == 0) Icons.Default.Home else Icons.Outlined.Home, contentDescription = "Home Tab") },
                                label = { Text("Home", fontSize = 11.sp) },
                                modifier = Modifier.testTag("nav_home_tab")
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
                                        Icon(if (selectedTab == 1) Icons.Default.LocalMall else Icons.Outlined.LocalMall, contentDescription = "Cart Tab")
                                    }
                                },
                                label = { Text("Cart", fontSize = 11.sp) },
                                modifier = Modifier.testTag("nav_cart_tab")
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
                                        Icon(if (selectedTab == 2) Icons.Default.ReceiptLong else Icons.Outlined.ReceiptLong, contentDescription = "Orders Tab")
                                    }
                                },
                                label = { Text("Orders", fontSize = 11.sp) },
                                modifier = Modifier.testTag("nav_orders_tab")
                            )
                            NavigationBarItem(
                                selected = selectedTab == 3,
                                onClick = { selectedTab = 3 },
                                icon = {
                                    val currentUser by profileViewModel.currentUser.collectAsState()
                                    val avatarUrl = currentUser?.profileImage ?: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=150&q=80"
                                    AsyncImage(
                                        model = avatarUrl,
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
                                },
                                label = { Text("Profile", fontSize = 11.sp) },
                                modifier = Modifier.testTag("nav_profile_tab")
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
                    1 -> CartScreen(cartViewModel, onBack = { selectedTab = 0 }, onOrderPlaced = { selectedTab = 2 })
                    2 -> OrdersScreen(orderViewModel)
                    3 -> ProfileScreen(profileViewModel, onLogout, onProductClick = onProductClick)
                }
            }
        }
    }
}
