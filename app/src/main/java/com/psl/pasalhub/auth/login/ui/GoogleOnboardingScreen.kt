package com.psl.pasalhub.auth.login.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.psl.pasalhub.auth.login.viewmodel.LoginViewModel
import com.psl.pasalhub.core.application.utils.screens.AuthDialogState
import com.psl.pasalhub.core.application.utils.screens.ModernTextField
import com.psl.pasalhub.core.application.utils.screens.PasalHubAuthDialog
import com.psl.pasalhub.core.application.utils.screens.PasalHubBackground
import com.psl.pasalhub.core.application.utils.screens.PasswordRequirements
import com.psl.pasalhub.ui.theme.LocalDimens
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun GoogleOnboardingScreen(
    viewModel: LoginViewModel,
    onNavigateToDashboard: () -> Unit,
    onExit: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var authDialogState by remember { mutableStateOf<AuthDialogState?>(null) }
    var showExitConfirmation by remember { mutableStateOf(false) }

    val passwordInteractionSource = remember { MutableInteractionSource() }
    val isPasswordFocused by passwordInteractionSource.collectIsFocusedAsState()

    // Error states
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var addressError by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val dimens = LocalDimens.current
    val isDark by viewModel.isDarkTheme.collectAsState()

    // Handle back button with confirmation
    BackHandler(enabled = true) {
        showExitConfirmation = true
    }

    PasalHubBackground(isDark = isDark) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimens.padding),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = { showExitConfirmation = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Exit",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .imePadding(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimens.padding)
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(dimens.large)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Complete Your Profile",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Welcome to Pasal Hub! Please set your secure password and primary delivery address to continue.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(dimens.medium)
                    ) {
                        ModernTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                passwordError = null
                            },
                            label = "Set PasalHub Password",
                            leadingIcon = Icons.Default.Lock,
                            keyboardType = KeyboardType.Password,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            },
                            isError = passwordError != null,
                            errorMessage = passwordError,
                            interactionSource = passwordInteractionSource
                        )

                        if (isPasswordFocused) {
                            PasswordRequirements(password = password)
                        }

                        ModernTextField(
                            value = confirmPassword,
                            onValueChange = {
                                confirmPassword = it
                                confirmPasswordError = null
                            },
                            label = "Confirm Password",
                            leadingIcon = Icons.Default.Lock,
                            keyboardType = KeyboardType.Password,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            isError = confirmPasswordError != null,
                            errorMessage = confirmPasswordError
                        )

                        ModernTextField(
                            value = address,
                            onValueChange = {
                                address = it
                                addressError = null
                            },
                            label = "Delivery Address",
                            leadingIcon = Icons.Default.Home,
                            keyboardType = KeyboardType.Text,
                            isError = addressError != null,
                            errorMessage = addressError
                        )
                    }

                    Button(
                        onClick = {
                            val hasUppercase = password.any { it.isUpperCase() }
                            val hasDigit = password.any { it.isDigit() }
                            val hasSpecial = password.any { !it.isLetterOrDigit() }

                            passwordError = when {
                                password.isBlank() -> "Required"
                                password.length < 8 -> "At least 8 characters"
                                !hasUppercase -> "Missing uppercase"
                                !hasDigit -> "Missing number"
                                !hasSpecial -> "Missing special char"
                                else -> null
                            }

                            confirmPasswordError = when {
                                confirmPassword.isBlank() -> "Required"
                                confirmPassword != password -> "Passwords do not match"
                                else -> null
                            }

                            addressError = if (address.isBlank()) "Required" else null

                            if (passwordError == null && confirmPasswordError == null && addressError == null) {
                                scope.launch {
                                    isSubmitting = true
                                    authDialogState = AuthDialogState.Loading("Saving Profile")
                                    try {
                                        viewModel.completeOnboarding(password, address)
                                        authDialogState =
                                            AuthDialogState.Success("Profile Activated")
                                        delay(1000.milliseconds)
                                        authDialogState = null
                                        onNavigateToDashboard()
                                    } catch (e: Exception) {
                                        authDialogState = AuthDialogState.Error(
                                            e.message ?: "Failed to update profile"
                                        )
                                    } finally {
                                        isSubmitting = false
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(dimens.buttonHeight),
                        shape = RoundedCornerShape(dimens.cardCorner),
                        enabled = !isSubmitting
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 3.dp
                            )
                        } else {
                            Text(
                                text = "Get Started",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            authDialogState?.let { state ->
                PasalHubAuthDialog(
                    onDismissRequest = {
                        if (state is AuthDialogState.Success) {
                            onNavigateToDashboard()
                        }
                        authDialogState = null
                    },
                    state = state
                )
            }

            if (showExitConfirmation) {
                AlertDialog(
                    onDismissRequest = { showExitConfirmation = false },
                    title = { Text("Cancel Registration?") },
                    text = {
                        Text("Your account will not be registered. It is mandatory to fill in the info to use the application.")
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showExitConfirmation = false
                                viewModel.signOut()
                                onExit()
                            }
                        ) {
                            Text("Exit", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showExitConfirmation = false }) {
                            Text("Stay")
                        }
                    }
                )
            }
        }
    }
}
