package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.repository.ShopRepository
import com.example.ui.screens.*
import com.example.ui.theme.PasalHubTheme
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.LightBackground
import com.example.ui.viewmodel.MainViewModel

class ViewModelFactory(private val repository: ShopRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appContainer = (application as PasalHubApp).container
        val factory = ViewModelFactory(appContainer.shopRepository)
        val mainViewModel: MainViewModel by viewModels { factory }

        setContent {
            val isDarkTheme by mainViewModel.isDarkTheme.collectAsState()
            val context = androidx.compose.ui.platform.LocalContext.current
            
            LaunchedEffect(Unit) {
                mainViewModel.loadSettings(context)
            }

            PasalHubTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = "splash"
                    ) {
                        // 1. Splash Screen
                        composable("splash") {
                            SplashScreen(
                                onNavigateNext = {
                                    val permissionsFlow = mainViewModel.allPermissionsGranted
                                    // We need to collect the state to decide where to go
                                    // But since we are in a callback, we can check the current value
                                    val arePermissionsGranted = mainViewModel.locationPermissionGranted.value &&
                                            mainViewModel.cameraPermissionGranted.value &&
                                            mainViewModel.storagePermissionGranted.value &&
                                            mainViewModel.notificationPermissionGranted.value
                                    
                                    val isThemeSet = mainViewModel.isThemeSet(context)
                                    val isOnboardingDone = mainViewModel.onboardingCompleted.value
                                    
                                    if (arePermissionsGranted && isThemeSet && isOnboardingDone) {
                                        navController.navigate("login") {
                                            popUpTo("splash") { inclusive = true }
                                        }
                                    } else if (!arePermissionsGranted) {
                                        navController.navigate("permission") {
                                            popUpTo("splash") { inclusive = true }
                                        }
                                    } else if (!isThemeSet) {
                                        navController.navigate("theme_selection") {
                                            popUpTo("splash") { inclusive = true }
                                        }
                                    } else {
                                        navController.navigate("onboarding") {
                                            popUpTo("splash") { inclusive = true }
                                        }
                                    }
                                }
                            )
                        }

                        // 2. Permission Screen (Location, Camera, Storage)
                        composable("permission") {
                            PermissionScreen(
                                viewModel = mainViewModel,
                                onNavigateNext = {
                                    navController.navigate("theme_selection") {
                                        popUpTo("permission") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 3. Theme Selection
                        composable("theme_selection") {
                            ThemeSelectionScreen(
                                viewModel = mainViewModel,
                                onNavigateNext = {
                                    navController.navigate("onboarding") {
                                        popUpTo("theme_selection") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 5. Onboarding Screen
                        composable("onboarding") {
                            OnboardingScreen(
                                viewModel = mainViewModel,
                                onNavigateNext = {
                                    navController.navigate("login") {
                                        popUpTo("onboarding") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 6. Login Screen
                        composable("login") {
                            LoginScreen(
                                viewModel = mainViewModel,
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

                        // 6a. Forgot Password Screen
                        composable("forgot_password") {
                            ForgotPasswordScreen(
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        // 7. Register Screen
                        composable("register") {
                            RegisterScreen(
                                viewModel = mainViewModel,
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

                        // 8. Main Dashboard Screen
                        composable("dashboard") {
                            DashboardScreen(
                                viewModel = mainViewModel,
                                onLogout = {
                                    mainViewModel.logout()
                                    navController.navigate("login") {
                                        popUpTo("dashboard") { inclusive = true }
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
