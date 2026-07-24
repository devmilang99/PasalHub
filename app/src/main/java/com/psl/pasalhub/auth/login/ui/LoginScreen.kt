package com.psl.pasalhub.auth.login.ui

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.psl.pasalhub.BuildConfig
import com.psl.pasalhub.auth.login.viewmodel.LoginViewModel
import com.psl.pasalhub.core.application.utils.screens.AuthDialogState
import com.psl.pasalhub.core.application.utils.screens.LoginTextField
import com.psl.pasalhub.core.application.utils.screens.PasalHubAuthDialog
import com.psl.pasalhub.core.application.utils.screens.PasalHubBackground
import com.psl.pasalhub.ui.theme.LocalDimens
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onNavigateToRegister: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToForgotPassword: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }
    var isLoggingIn by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var authDialogState by remember { mutableStateOf<AuthDialogState?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val dimens = LocalDimens.current

    val currentUser by viewModel.currentUser.collectAsState()
    val lastEmail by viewModel.lastEmail.collectAsState()
    val isDark by viewModel.isDarkTheme.collectAsState()

    // Autofill email if not remembered
    LaunchedEffect(lastEmail) {
        if (email.isBlank() && lastEmail.isNotEmpty() && currentUser?.isRemembered != true) {
            email = lastEmail
        }
    }

    PasalHubBackground(isDark = isDark) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag("login_screen"),
            containerColor = Color.Transparent
        ) { innerPadding ->
            val maxWidth = if (dimens.padding >= 24.dp) 480.dp else Dp.Unspecified
            val primaryTextColor = MaterialTheme.colorScheme.onBackground
            val secondaryTextColor = MaterialTheme.colorScheme.onSurfaceVariant

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                val screenHeight = maxHeight
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        modifier = Modifier
                            .widthIn(max = maxWidth)
                            .fillMaxWidth()
                            .heightIn(min = screenHeight)
                            .padding(horizontal = dimens.padding),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.weight(1f))

                        // Branding Section
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(dimens.small)
                        ) {
                            Surface(
                                modifier = Modifier
                                    .size(dimens.logoSize)
                                    .clip(CircleShape),
                                color = MaterialTheme.colorScheme.primary,
                                tonalElevation = 8.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.ShoppingBag,
                                        contentDescription = null,
                                        modifier = Modifier.size(dimens.logoSize * 0.6f),
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }

                            Text(
                                text = "PASAL HUB",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 4.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Welcome Back",
                                    style = if (dimens.padding > 24.dp) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = primaryTextColor
                                )

                                Text(
                                    text = "Sign in to access your curated collection",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = secondaryTextColor,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Core Form
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(dimens.medium)
                        ) {
                            LoginTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = "Email Address",
                                leadingIcon = Icons.Default.Email,
                                isDark = isDark,
                                keyboardType = KeyboardType.Email,
                                testTag = "login_email_input"
                            )

                            LoginTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = "Password",
                                leadingIcon = Icons.Default.Lock,
                                isDark = isDark,
                                keyboardType = KeyboardType.Password,
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = null,
                                            tint = secondaryTextColor
                                        )
                                    }
                                },
                                testTag = "login_password_input"
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { rememberMe = !rememberMe }
                                ) {
                                    Checkbox(
                                        checked = rememberMe,
                                        onCheckedChange = { rememberMe = it },
                                        colors = CheckboxDefaults.colors(
                                            uncheckedColor = secondaryTextColor.copy(alpha = 0.6f),
                                            checkedColor = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                    Text(
                                        text = "Keep me signed in",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = secondaryTextColor
                                    )
                                }

                                Text(
                                    text = "Forgot Password?",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.clickable { onNavigateToForgotPassword() }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(dimens.large))

                        // Login Button
                        Button(
                            onClick = {
                                scope.launch {
                                    isLoggingIn = true
                                    authDialogState = AuthDialogState.Loading("Authenticating")
                                    try {
                                        viewModel.signIn(email, password)
                                        authDialogState = AuthDialogState.Success("Secure Login")
                                        delay(800.milliseconds)
                                        authDialogState = null
                                        onNavigateToDashboard()
                                    } catch (e: Exception) {
                                        authDialogState =
                                            AuthDialogState.Error(e.message ?: "Login failed")
                                    } finally {
                                        isLoggingIn = false
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(dimens.buttonHeight)
                                .testTag("login_button"),
                            shape = RoundedCornerShape(dimens.cardCorner),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            enabled = !isLoggingIn
                        ) {
                            if (isLoggingIn) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 3.dp
                                )
                            } else {
                                Text(
                                    text = "Sign In Securely",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Divider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HorizontalDivider(
                                modifier = Modifier.weight(1f),
                                color = secondaryTextColor.copy(alpha = 0.2f)
                            )
                            Text(
                                text = "OR CONTINUE WITH",
                                style = MaterialTheme.typography.labelSmall,
                                color = secondaryTextColor.copy(alpha = 0.5f),
                                modifier = Modifier.padding(horizontal = dimens.medium),
                                letterSpacing = 2.sp
                            )
                            HorizontalDivider(
                                modifier = Modifier.weight(1f),
                                color = secondaryTextColor.copy(alpha = 0.2f)
                            )
                        }

                        Spacer(modifier = Modifier.height(dimens.medium))

                        // Social Login
                        OutlinedButton(
                            onClick = {
                                Log.d("Google Login Clicked", BuildConfig.SUPABASE_SERVER_CLIENT_ID)
                                val serverClientId = BuildConfig.SUPABASE_SERVER_CLIENT_ID.ifEmpty {
                                    "" // Should be handled or provide a default if possible
                                }

                                if (serverClientId.isEmpty()) {
                                    authDialogState =
                                        AuthDialogState.Error("Supabase Google Client ID not configured. Please check your .env file.")
                                    return@OutlinedButton
                                }

                                val credentialManager = CredentialManager.create(context)
                                val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
                                    .setFilterByAuthorizedAccounts(false)
                                    .setServerClientId(serverClientId)
                                    .setAutoSelectEnabled(false)
                                    .build()

                                val request: GetCredentialRequest = GetCredentialRequest.Builder()
                                    .addCredentialOption(googleIdOption)
                                    .build()

                                scope.launch {
                                    authDialogState = AuthDialogState.Loading("Google Login")
                                    delay(400.milliseconds)
                                    authDialogState = null

                                    try {
                                        val result = credentialManager.getCredential(
                                            context = context,
                                            request = request
                                        )

                                        authDialogState = AuthDialogState.Loading("Secure Access")

                                        val idToken = when (val credential = result.credential) {
                                            is CustomCredential -> {
                                                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                                    GoogleIdTokenCredential.createFrom(credential.data).idToken
                                                } else {
                                                    null
                                                }
                                            }

                                            is GoogleIdTokenCredential -> credential.idToken
                                            else -> null
                                        }

                                        if (idToken != null) {
                                            try {
                                                viewModel.googleSignIn(idToken)
                                                authDialogState = null
                                                onNavigateToDashboard()
                                            } catch (e: Exception) {
                                                authDialogState =
                                                    AuthDialogState.Error("Supabase Auth failed: ${e.localizedMessage}")
                                            }
                                        } else {
                                            authDialogState =
                                                AuthDialogState.Error("Could not retrieve Google account information.")
                                        }
                                    } catch (e: Exception) {
                                        authDialogState =
                                            if (e !is androidx.credentials.exceptions.GetCredentialCancellationException) {
                                                AuthDialogState.Error("Google Sign-In failed: ${e.localizedMessage}")
                                            } else {
                                                null
                                            }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(dimens.buttonHeight),
                            shape = RoundedCornerShape(dimens.cardCorner),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                secondaryTextColor.copy(alpha = 0.2f)
                            )
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    tint = primaryTextColor
                                )
                                Spacer(modifier = Modifier.width(dimens.small))
                                Text(
                                    text = "Sign in with Google",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = primaryTextColor,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Footer
                        Row(modifier = Modifier.padding(bottom = dimens.medium)) {
                            Text(
                                text = "New to Pasal Hub? ",
                                style = MaterialTheme.typography.bodyMedium,
                                color = secondaryTextColor
                            )
                            Text(
                                text = "Create Account",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable { onNavigateToRegister() }
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
                    state = state,

                    )
            }
        }
    }
}
