package com.example.core.application

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ai.presentation.AiSearchViewModel
import com.example.ai.presentation.AISearchScreen
import com.example.auth.forgotpassword.viewmodel.ForgotPasswordViewModel
import com.example.auth.login.viewmodel.LoginViewModel
import com.example.auth.register.viewmodel.RegisterViewModel
import com.example.dashboard.cart.viewmodel.CartViewModel
import com.example.dashboard.home.viewmodel.HomeViewModel
import com.example.dashboard.order.viewmodel.OrderViewModel
import com.example.dashboard.profile.viewmodel.ProfileViewModel
import com.example.dashboard.products.repository.Resource
import com.example.dashboard.ui.DashboardScreen
import com.example.initial.presentation.InitialViewModel
import com.example.initial.presentation.splash.SplashScreen
import com.example.initial.presentation.onboarding.OnboardingScreen
import com.example.initial.presentation.permission.PermissionScreen
import com.example.initial.presentation.theme.ThemeSelectionScreen
import com.example.ui.theme.PasalHubTheme
import com.example.ui.theme.ProvideDimens
import com.example.core.viewmodel.MainViewModel
import com.example.core.application.presentation.AppViewModel
import com.example.dashboard.products.ui.ProductDetailScreen
import com.example.dashboard.products.viewmodel.ProductDetailViewModel

import androidx.hilt.navigation.compose.hiltViewModel
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
            val context = androidx.compose.ui.platform.LocalContext.current
            val windowSizeClass = calculateWindowSizeClass(this)

            LaunchedEffect(Unit) {
                mainViewModel.loadSettings(context)
            }

            PasalHubTheme(darkTheme = isDarkTheme) {
                ProvideDimens(windowSizeClass = windowSizeClass) {
                    Surface(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val navController = rememberNavController()

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
                                com.example.auth.login.ui.LoginScreen(
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
                                com.example.auth.forgotpassword.ui.ForgotPasswordScreen(
                                    viewModel = forgotPasswordViewModel,
                                    onNavigateBack = {
                                        navController.popBackStack()
                                    }
                                )
                            }

                            composable("register") {
                                val registerViewModel: RegisterViewModel = hiltViewModel()
                                com.example.auth.register.ui.RegisterScreen(
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
                                        navController.popBackStack()
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
                                    onProductClick = { product ->
                                        navController.navigate("product_detail/${product.id}")
                                    },
                                    initialTab = startTab
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
                }
            }
        }
    }
}
