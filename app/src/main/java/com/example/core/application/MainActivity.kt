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
import com.example.initial.di.AppContainer
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

class ViewModelFactory(
    private val container: AppContainer
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                MainViewModel(
                    container.productRepository,
                    container.appPreferencesRepository
                ) as T
            }
            modelClass.isAssignableFrom(AiSearchViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                AiSearchViewModel(container.productRepository, container.geminiSearchRouter) as T
            }
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                LoginViewModel(container.loginRepository) as T
            }
            modelClass.isAssignableFrom(RegisterViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                RegisterViewModel(container.registerRepository) as T
            }
            modelClass.isAssignableFrom(ForgotPasswordViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                ForgotPasswordViewModel(container.forgotPasswordRepository) as T
            }
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                HomeViewModel(container.homeRepository) as T
            }
            modelClass.isAssignableFrom(OrderViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                OrderViewModel(
                    container.orderRepository,
                    container.appPreferencesRepository,
                    container.notificationHelper
                ) as T
            }
            modelClass.isAssignableFrom(CartViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                CartViewModel(container.cartRepository) as T
            }
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                ProfileViewModel(container.profileRepository) as T
            }
            modelClass.isAssignableFrom(InitialViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                InitialViewModel(container.initialRepository) as T
            }
            modelClass.isAssignableFrom(ProductDetailViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                ProductDetailViewModel(
                    container.productRepository,
                    container.appPreferencesRepository
                ) as T
            }
            modelClass.isAssignableFrom(AppViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                AppViewModel(container.appPreferencesRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appContainer = (application as PasalHubApp).container
        val factory = ViewModelFactory(appContainer)
        
        // These ViewModels are shared or used for app-level state
        val mainViewModel: MainViewModel by viewModels { factory }
        val appViewModel: AppViewModel by viewModels { factory }
        val initialViewModel: InitialViewModel by viewModels { factory }

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
                                            navController.navigate("dashboard") {
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
                                val loginViewModel: LoginViewModel = viewModel(factory = factory)
                                com.example.auth.login.ui.LoginScreen(
                                    viewModel = loginViewModel,
                                    onNavigateToRegister = {
                                        navController.navigate("register")
                                    },
                                    onNavigateToDashboard = {
                                        navController.navigate("dashboard") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    },
                                    onNavigateToForgotPassword = {
                                        navController.navigate("forgot_password")
                                    }
                                )
                            }

                            composable("forgot_password") {
                                val forgotPasswordViewModel: ForgotPasswordViewModel = viewModel(factory = factory)
                                com.example.auth.forgotpassword.ui.ForgotPasswordScreen(
                                    viewModel = forgotPasswordViewModel,
                                    onNavigateBack = {
                                        navController.popBackStack()
                                    }
                                )
                            }

                            composable("register") {
                                val registerViewModel: RegisterViewModel = viewModel(factory = factory)
                                com.example.auth.register.ui.RegisterScreen(
                                    viewModel = registerViewModel,
                                    onNavigateBackToLogin = {
                                        navController.popBackStack()
                                    },
                                    onNavigateToDashboard = {
                                        navController.navigate("dashboard") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    }
                                )
                            }

                            composable("ai_search") {
                                val aiSearchViewModel: AiSearchViewModel = viewModel(factory = factory)
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

                            composable("dashboard") {
                                val aiSearchViewModel: AiSearchViewModel = viewModel(factory = factory)
                                val homeViewModel: HomeViewModel = viewModel(factory = factory)
                                val orderViewModel: OrderViewModel = viewModel(factory = factory)
                                val cartViewModel: CartViewModel = viewModel(factory = factory)
                                val profileViewModel: ProfileViewModel = viewModel(factory = factory)
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
                                            popUpTo("dashboard") { inclusive = true }
                                        }
                                    },
                                    onAiSearchClick = {
                                        navController.navigate("ai_search")
                                    },
                                    onProductClick = { product ->
                                        navController.navigate("product_detail/${product.id}")
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
                                    val productDetailViewModel: ProductDetailViewModel = viewModel(factory = factory)
                                    ProductDetailScreen(
                                        product = product,
                                        viewModel = mainViewModel,
                                        detailViewModel = productDetailViewModel,
                                        onBack = { navController.popBackStack() },
                                        onProductClick = { newProduct ->
                                            navController.navigate("product_detail/${newProduct.id}")
                                        },
                                        onOrderPlaced = {
                                            navController.navigate("dashboard") {
                                                popUpTo("dashboard") { inclusive = false }
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
