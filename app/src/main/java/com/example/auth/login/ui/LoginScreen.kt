package com.example.auth.login.ui

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.BuildConfig
import com.example.auth.login.viewmodel.LoginViewModel
import com.example.ui.screens.AuthDialogState
import com.example.ui.screens.LoginTextField
import com.example.ui.screens.PasalHubAuthDialog
import com.example.ui.theme.LocalDimens
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
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

    // Autofill email if not remembered
    LaunchedEffect(lastEmail) {
        if (email.isBlank() && lastEmail.isNotEmpty() && currentUser?.isRemembered != true) {
            email = lastEmail
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background Image with Premium Overlay
        Image(
            painter = painterResource(id = R.drawable.img_splash_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        // Gradient Scrim for readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.3f),
                            Color.Black.copy(alpha = 0.7f),
                            Color.Black
                        )
                    )
                )
        )

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag("login_screen"),
            containerColor = Color.Transparent
        ) { innerPadding ->
            val maxWidth = if (dimens.padding >= 24.dp) 480.dp else Dp.Unspecified
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .imePadding(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = maxWidth)
                        .fillMaxWidth()
                        .padding(horizontal = dimens.padding)
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Spacer(modifier = Modifier.height(dimens.large))

                    // Branding Section
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

                    Spacer(modifier = Modifier.height(dimens.small))

                    Text(
                        text = "PASAL HUB",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 4.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(dimens.extraSmall))

                    Text(
                        text = "Welcome Back",
                        style = if (dimens.padding > 24.dp) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )

                    Text(
                        text = "Sign in to access your curated collection",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(dimens.medium))

                    // Core Form
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(dimens.small)
                    ) {
                        LoginTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = "Email Address",
                            leadingIcon = Icons.Default.Email,
                            keyboardType = KeyboardType.Email,
                            testTag = "login_email_input"
                        )

                        LoginTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = "Password",
                            leadingIcon = Icons.Default.Lock,
                            keyboardType = KeyboardType.Password,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.7f)
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
                                        uncheckedColor = Color.White.copy(alpha = 0.6f),
                                        checkedColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                                Text(
                                    text = "Keep me signed in",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.8f)
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

                    Spacer(modifier = Modifier.height(dimens.medium))

                    // Login Button
                    Button(
                        onClick = {
                            if (!viewModel.isValidatedUser(email, password)) {
                                authDialogState = AuthDialogState.Error("Access Denied: You are not a validated Pasal Hub member. Please check your credentials.")
                                return@Button
                            }
                            
                            scope.launch {
                                isLoggingIn = true
                                authDialogState = AuthDialogState.Loading("Authenticating")
                                delay(1200.milliseconds)
                                isLoggingIn = false
                                viewModel.registerUser(
                                    context = context,
                                    name = email.substringBefore("@").replaceFirstChar { it.uppercase() },
                                    email = email,
                                    dateOfBirth = "1995-10-15",
                                    address = "123 Pasal Hub Blvd",
                                    rememberMe = rememberMe,
                                    isGoogleUser = false
                                )
                                authDialogState = AuthDialogState.Success("Secure Login")
                                delay(800.milliseconds)
                                authDialogState = null
                                onNavigateToDashboard()
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

                    Spacer(modifier = Modifier.height(dimens.medium))

                    // Divider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = Color.White.copy(alpha = 0.2f)
                        )
                        Text(
                            text = "OR CONTINUE WITH",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.padding(horizontal = dimens.medium),
                            letterSpacing = 2.sp
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = Color.White.copy(alpha = 0.2f)
                        )
                    }

                    Spacer(modifier = Modifier.height(dimens.medium))

                    // Social Login
                    OutlinedButton(
                        onClick = { 
                            Log.d("GoogleSignIn", "Sign-in button clicked")
                            
                            // Fallback to the Web Client ID from google-services.json if BuildConfig is empty
                            val serverClientId = if (BuildConfig.GOOGLE_SERVER_CLIENT_ID.isNotEmpty()) {
                                BuildConfig.GOOGLE_SERVER_CLIENT_ID
                            } else {
                                "1079515205022-ig535etmdi5l9sc98hrj1ojb610sgc7p.apps.googleusercontent.com"
                            }
                            
                            Log.d("GoogleSignIn", "Using Server Client ID: $serverClientId")

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
                                // 1. INITIATE: Show processing dialog immediately on click
                                authDialogState = AuthDialogState.Loading("Google Login")
                                delay(400) // Brief delay so user sees the dialog start
                                
                                // 2. HIDE: Hide dialog before Google UI takes over
                                authDialogState = null 
                                
                                try {
                                    val result = credentialManager.getCredential(
                                        context = context,
                                        request = request
                                    )
                                    
                                    // 3. VALIDATING: Show dialog again while validating with Firebase
                                    authDialogState = AuthDialogState.Loading("Secure Access")
                                    
                                    val credential = result.credential
                                    Log.d("GoogleSignIn", "Received credential type: ${credential.type}")

                                    val idToken = when (credential) {
                                        is GoogleIdTokenCredential -> {
                                            Log.d("GoogleSignIn", "Successfully identified GoogleIdTokenCredential")
                                            credential.idToken
                                        }
                                        else -> {
                                            // Manual parsing fallback
                                            if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                                Log.d("GoogleSignIn", "Manually parsing GoogleIdTokenCredential")
                                                GoogleIdTokenCredential.createFrom(credential.data).idToken
                                            } else {
                                                null
                                            }
                                        }
                                    }

                                    if (idToken != null) {
                                        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                                        FirebaseAuth.getInstance().signInWithCredential(firebaseCredential)
                                            .addOnCompleteListener { task ->
                                                if (task.isSuccessful) {
                                                    val user = task.result?.user
                                                    Log.d("GoogleSignIn", "Firebase Sign-In Success: ${user?.email}")
                                                    viewModel.registerUser(
                                                        context = context,
                                                        name = user?.displayName ?: "User",
                                                        email = user?.email ?: "user@google.com",
                                                        dateOfBirth = "2000-01-01",
                                                        address = "KTM HQ",
                                                        rememberMe = true,
                                                        isGoogleUser = true,
                                                        profileImage = user?.photoUrl?.toString()
                                                    )
                                                    // 4. LOGIN DIRECTLY: Skip Success dialog and navigate immediately
                                                    authDialogState = null
                                                    onNavigateToDashboard()
                                                } else {
                                                    val errorMsg = task.exception?.localizedMessage ?: "Unknown Firebase error"
                                                    Log.e("GoogleSignIn", "Firebase Sign-In Failed: $errorMsg")
                                                    authDialogState = AuthDialogState.Error("Authentication failed: $errorMsg")
                                                }
                                            }
                                    } else {
                                        Log.e("GoogleSignIn", "No ID Token found in credential")
                                        authDialogState = AuthDialogState.Error("Could not retrieve Google account information.")
                                    }
                                } catch (e: Exception) {
                                    Log.e("GoogleSignIn", "Credential Manager Error", e)
                                    // Only show error if the user didn't manually cancel the picker
                                    if (e !is androidx.credentials.exceptions.GetCredentialCancellationException) {
                                        authDialogState = AuthDialogState.Error("Google Sign-In failed: ${e.localizedMessage}")
                                    } else {
                                        authDialogState = null
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(dimens.buttonHeight),
                        shape = RoundedCornerShape(dimens.cardCorner),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(dimens.small))
                            Text(
                                text = "Sign in with Google",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(dimens.medium))

                    // Footer
                    Row(modifier = Modifier.padding(bottom = dimens.medium)) {
                        Text(
                            text = "New to Pasal Hub? ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f)
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

            authDialogState?.let { state ->
                PasalHubAuthDialog(
                    onDismissRequest = { 
                        if (state is AuthDialogState.Success) {
                            onNavigateToDashboard()
                        }
                        authDialogState = null
                    },
                    state = state,
                    isDark = true
                )
            }
        }
    }
}
