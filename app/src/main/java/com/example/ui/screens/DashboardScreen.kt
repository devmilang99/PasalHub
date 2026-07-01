package com.example.ui.screens

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.remote.ProductDto
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Home, 1: Cart, 2: Order, 3: Profile
    val cartItems by viewModel.cartItems.collectAsState()
    val cartItemCount = cartItems.size
    val isDark by viewModel.isDarkTheme.collectAsState()
    val orders by viewModel.ordersState.collectAsState()
    val recentOrdersCount = orders.count { it.status in listOf("Placing", "Placed", "Packaging", "Sent for Delivery") }
    var selectedProductForDetail by remember { mutableStateOf<ProductDto?>(null) }
    var notificationMessage by remember { mutableStateOf<String?>(null) }
    var showNotificationDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.notificationEvent.collect { message ->
            notificationMessage = message
            showNotificationDialog = true
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

    BackHandler(enabled = selectedProductForDetail != null || selectedTab != 0) {
        if (selectedProductForDetail != null) {
            selectedProductForDetail = null
        } else {
            selectedTab = 0
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        bottomBar = {
            val showBottomNav = when {
                selectedProductForDetail != null -> false
                selectedTab == 1 -> cartItems.size <= 3 // Show for 1-3 items, hide for > 3 as requested
                else -> true
            }

            if (showBottomNav) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .navigationBarsPadding()
                ) {
                    NavigationBar(
                        modifier = Modifier
                            .testTag("bottom_navigation_bar")
                            .clip(RoundedCornerShape(24.dp))
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(24.dp)
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
                                            Badge {
                                                Text(text = cartItemCount.toString())
                                            }
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
                            modifier = Modifier.testTag("nav_cart_tab")
                        )
                        NavigationBarItem(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (recentOrdersCount > 0) {
                                            Badge {
                                                Text(text = recentOrdersCount.toString())
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        if (selectedTab == 2) Icons.Default.ReceiptLong else Icons.Outlined.ReceiptLong,
                                        contentDescription = "Orders Tab"
                                    )
                                }
                            },
                            label = { Text("Orders", fontSize = 11.sp) },
                            modifier = Modifier.testTag("nav_orders_tab")
                        )
                        NavigationBarItem(
                            selected = selectedTab == 3,
                            onClick = { selectedTab = 3 },
                            icon = {
                                val avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=150&q=80"
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
                0 -> HomeScreen(viewModel, onProductClick = { selectedProductForDetail = it })
                1 -> CartScreen(viewModel, onBack = { selectedTab = 0 }, onOrderPlaced = { selectedTab = 2 })
                2 -> OrdersScreen(viewModel)
                3 -> ProfileScreen(viewModel, onLogout, onProductClick = { selectedProductForDetail = it })
            }

            selectedProductForDetail?.let { product ->
                ProductDetailScreen(
                    product = product,
                    viewModel = viewModel,
                    onBack = { selectedProductForDetail = null },
                    onProductClick = { selectedProductForDetail = it },
                    onOrderPlaced = {
                        selectedProductForDetail = null
                        selectedTab = 2
                    }
                )
            }
        }
    }
}
