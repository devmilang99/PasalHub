package com.psl.pasalhub.core.application

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.psl.pasalhub.ai.presentation.AiSearchViewModel
import com.psl.pasalhub.ai.presentation.AISearchScreen
import com.psl.pasalhub.auth.forgotpassword.viewmodel.ForgotPasswordViewModel
import com.psl.pasalhub.auth.login.viewmodel.LoginViewModel
import com.psl.pasalhub.auth.register.viewmodel.RegisterViewModel
import com.psl.pasalhub.dashboard.cart.ui.OrderReviewScreen
import com.psl.pasalhub.dashboard.cart.viewmodel.CartViewModel
import com.psl.pasalhub.dashboard.home.viewmodel.HomeViewModel
import com.psl.pasalhub.dashboard.order.viewmodel.OrderViewModel
import com.psl.pasalhub.dashboard.profile.viewmodel.ProfileViewModel
import com.psl.pasalhub.dashboard.products.repository.Resource
import com.psl.pasalhub.dashboard.ui.DashboardScreen
import com.psl.pasalhub.initial.presentation.InitialViewModel
import com.psl.pasalhub.initial.presentation.splash.SplashScreen
import com.psl.pasalhub.initial.presentation.onboarding.OnboardingScreen
import com.psl.pasalhub.initial.presentation.permission.PermissionScreen
import com.psl.pasalhub.initial.presentation.theme.ThemeSelectionScreen
import com.psl.pasalhub.ui.theme.PasalHubTheme
import com.psl.pasalhub.ui.theme.ProvideDimens
import com.psl.pasalhub.core.viewmodel.MainViewModel
import com.psl.pasalhub.core.application.presentation.AppViewModel
import com.psl.pasalhub.dashboard.products.ui.ProductDetailScreen
import com.psl.pasalhub.dashboard.products.viewmodel.ProductDetailViewModel


import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // These ViewModels are shared or used for app-level state
        val mainViewModel: MainViewModel by viewModels()
        val appViewModel: AppViewModel by viewModels()
        val initialViewModel: InitialViewModel by viewModels()

        setContent {
            val isDarkTheme by appViewModel.isDarkTheme.collectAsState()
            val windowSizeClass = calculateWindowSizeClass(this)

            PasalHubTheme(darkTheme = isDarkTheme) {
                ProvideDimens(windowSizeClass = windowSizeClass) {
                    Surface(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        PasalHubNavHost(
                            mainViewModel = mainViewModel,
                            appViewModel = appViewModel,
                            initialViewModel = initialViewModel
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PasalHubNavHost(
    mainViewModel: MainViewModel,
    appViewModel: AppViewModel,
    initialViewModel: InitialViewModel
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val navController = rememberNavController()

    LaunchedEffect(Unit) {
        mainViewModel.loadSettings(context)
    }

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(
                viewModel = initialViewModel,
                onNavigateNext = {
                    val arePermissionsGranted =
                        initialViewModel.locationPermissionGranted.value &&
                                initialViewModel.cameraPermissionGranted.value &&
                                initialViewModel.storagePermissionGranted.value &&
                                initialViewModel.notificationPermissionGranted.value

                    val isFlowCompleted = initialViewModel.isFlowCompleted.value
                    val isOnboardingDone = initialViewModel.onboardingCompleted.value
                    val isThemeSet = initialViewModel.isThemeSet.value
                    val currentUser = initialViewModel.currentUser.value

                    if (currentUser?.isRemembered == true) {
                        navController.navigate("dashboard?startTab=0") {
                            popUpTo("splash") { inclusive = true }
                        }
                    } else if (isFlowCompleted) {
                        navController.navigate("login") {
                            popUpTo("splash") { inclusive = true }
                        }
                    } else if (!isOnboardingDone) {
                        navController.navigate("onboarding") {
                            popUpTo("splash") { inclusive = true }
                        }
                    } else if (!arePermissionsGranted) {
                        navController.navigate("permission") {
                            popUpTo("splash") { inclusive = true }
                        }
                    } else {
                        navController.navigate("theme_selection") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                }
            )
        }

        composable("permission") {
            PermissionScreen(
                viewModel = initialViewModel,
                onNavigateNext = {
                    navController.navigate("theme_selection") {
                        popUpTo("permission") { inclusive = true }
                    }
                }
            )
        }

        composable("theme_selection") {
            ThemeSelectionScreen(
                viewModel = initialViewModel,
                onNavigateNext = {
                    navController.navigate("login") {
                        popUpTo("theme_selection") { inclusive = true }
                    }
                }
            )
        }

        composable("onboarding") {
            OnboardingScreen(
                viewModel = initialViewModel,
                onNavigateNext = {
                    navController.navigate("permission") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }

        composable("login") {
            val loginViewModel: LoginViewModel = hiltViewModel()
            com.psl.pasalhub.auth.login.ui.LoginScreen(
                viewModel = loginViewModel,
                onNavigateToRegister = {
                    navController.navigate("register")
                },
                onNavigateToDashboard = {
                    navController.navigate("dashboard?startTab=0") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToForgotPassword = {
                    navController.navigate("forgot_password")
                }
            )
        }

        composable("forgot_password") {
            val forgotPasswordViewModel: ForgotPasswordViewModel = hiltViewModel()
            com.psl.pasalhub.auth.forgotpassword.ui.ForgotPasswordScreen(
                viewModel = forgotPasswordViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("register") {
            val registerViewModel: RegisterViewModel = hiltViewModel()
            com.psl.pasalhub.auth.register.ui.RegisterScreen(
                viewModel = registerViewModel,
                onNavigateBackToLogin = {
                    navController.popBackStack()
                },
                onNavigateToDashboard = {
                    navController.navigate("dashboard?startTab=0") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("ai_search") {
            val aiSearchViewModel: AiSearchViewModel = hiltViewModel()
            AISearchScreen(
                viewModel = mainViewModel,
                aiViewModel = aiSearchViewModel,
                onBackClick = {
                    navController.navigate("dashboard?startTab=0")
                },
                onProductClick = { product ->
                    navController.navigate("product_detail/${product.id}")
                }
            )
        }

        composable("dashboard?startTab={startTab}") { backStackEntry ->
            val startTab = backStackEntry.arguments?.getString("startTab")?.toIntOrNull() ?: 0
            val aiSearchViewModel: AiSearchViewModel = hiltViewModel()
            val homeViewModel: HomeViewModel = hiltViewModel()
            val orderViewModel: OrderViewModel = hiltViewModel()
            val cartViewModel: CartViewModel = hiltViewModel()
            val profileViewModel: ProfileViewModel = hiltViewModel()
            DashboardScreen(
                viewModel = mainViewModel,
                appViewModel = appViewModel,
                aiViewModel = aiSearchViewModel,
                homeViewModel = homeViewModel,
                orderViewModel = orderViewModel,
                cartViewModel = cartViewModel,
                profileViewModel = profileViewModel,
                onLogout = {
                    mainViewModel.logout(context)
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onAiSearchClick = {
                    navController.navigate("ai_search")
                },
                onFilterClick = {
                    // Filter removed
                },
                onProductClick = { product ->
                    navController.navigate("product_detail/${product.id}")
                },
                onOrderReview = { items, subtotal, tax, discount, total, voucher, method, address ->
                    val ids = items.map { it.productId }.joinToString(",")
                    navController.navigate("order_review?subtotal=$subtotal&tax=$tax&discount=$discount&total=$total&voucher=$voucher&paymentMethod=$method&address=$address&selectedIds=$ids")
                },
                initialTab = startTab
            )
        }

        composable("order_review?subtotal={subtotal}&tax={tax}&discount={discount}&total={total}&voucher={voucher}&paymentMethod={paymentMethod}&address={address}&selectedIds={selectedIds}") { backStackEntry ->
            val cartViewModel: CartViewModel = hiltViewModel()
            val cartItems by cartViewModel.cartItems.collectAsState()
            val isDark by appViewModel.isDarkTheme.collectAsState()
            val context = androidx.compose.ui.platform.LocalContext.current

            val subtotal = backStackEntry.arguments?.getString("subtotal")?.toDoubleOrNull() ?: 0.0
            val tax = backStackEntry.arguments?.getString("tax")?.toDoubleOrNull() ?: 0.0
            val discount = backStackEntry.arguments?.getString("discount")?.toDoubleOrNull() ?: 0.0
            val total = backStackEntry.arguments?.getString("total")?.toDoubleOrNull() ?: 0.0
            val voucher = backStackEntry.arguments?.getString("voucher") ?: "None"
            val paymentMethod = backStackEntry.arguments?.getString("paymentMethod") ?: ""
            val address = backStackEntry.arguments?.getString("address") ?: ""
            val selectedIdsStr = backStackEntry.arguments?.getString("selectedIds") ?: ""
            val selectedIds = remember(selectedIdsStr) {
                selectedIdsStr.split(",").filter { it.isNotEmpty() }.mapNotNull { it.toIntOrNull() }
                    .toSet()
            }

            OrderReviewScreen(
                items = cartItems.filter { it.productId in selectedIds },
                subtotal = subtotal,
                tax = tax,
                discount = discount,
                total = total,
                voucher = voucher,
                paymentMethod = paymentMethod,
                address = address,
                isDark = isDark,
                onBack = { navController.popBackStack() },
                onConfirmOrder = {
                    val selectedItems = cartItems.filter { it.productId in selectedIds }
                    cartViewModel.checkoutSelected(
                        context = context,
                        selectedItems = selectedItems,
                        finalTotal = total,
                        paymentMethod = paymentMethod,
                        appliedVoucher = voucher
                    )
                    appViewModel.postNotification("Your order has been placed successfully!")
                    navController.navigate("dashboard?startTab=2") {
                        popUpTo("dashboard?startTab=1") { inclusive = true }
                    }
                }
            )
        }

        composable("product_detail/{productId}") { backStackEntry ->
            val productId =
                backStackEntry.arguments?.getString("productId")?.toIntOrNull()
            val productsState by mainViewModel.homeProductsState.collectAsState()
            val product = when (productsState) {
                is Resource.Success -> (productsState as Resource.Success).data.find { it.id == productId }
                else -> null
            }

            if (product != null) {
                val productDetailViewModel: ProductDetailViewModel = hiltViewModel()
                ProductDetailScreen(
                    product = product,
                    viewModel = mainViewModel,
                    detailViewModel = productDetailViewModel,
                    onBack = { navController.popBackStack() },
                    onProductClick = { newProduct ->
                        navController.navigate("product_detail/${newProduct.id}")
                    },
                    onOrderPlaced = {
                        navController.navigate("dashboard?startTab=2") {
                            popUpTo("dashboard?startTab=0") { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
