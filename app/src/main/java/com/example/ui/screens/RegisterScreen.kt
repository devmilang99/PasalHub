package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: MainViewModel,
    onNavigateBackToLogin: () -> Unit,
    onNavigateToDashboard: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var isRegistering by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var authDialogState by remember { mutableStateOf<AuthDialogState?>(null) }

    // Error states
    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var dobError by remember { mutableStateOf<String?>(null) }
    var addressError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val isDark by viewModel.isDarkTheme.collectAsState()

    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()

    // Date Picker State
    val datePickerState = rememberDatePickerState()
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val date = Date(it)
                        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        dateOfBirth = format.format(date)
                        dobError = null
                    }
                    showDatePicker = false
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag("register_screen"),
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Create Account",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onNavigateBackToLogin,
                            modifier = Modifier
                                .padding(8.dp)
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Branding Header
                Surface(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.primary,
                    tonalElevation = 4.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = null,
                            modifier = Modifier.size(30.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Join Pasal Hub",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                Text(
                    text = "Start your premium shopping journey",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Input Section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ModernTextField(
                        value = name,
                        onValueChange = { 
                            name = it
                            nameError = if (it.isBlank()) "Required" else null
                        },
                        label = "Full Name",
                        leadingIcon = Icons.Default.Person,
                        isError = nameError != null,
                        errorMessage = nameError,
                        testTag = "register_name_input"
                    )

                    ModernTextField(
                        value = email,
                        onValueChange = { 
                            email = it
                            emailError = when {
                                it.isBlank() -> "Required"
                                !emailRegex.matches(it) -> "Invalid format"
                                else -> null
                            }
                        },
                        label = "Email Address",
                        leadingIcon = Icons.Default.Email,
                        keyboardType = KeyboardType.Email,
                        isError = emailError != null,
                        errorMessage = emailError,
                        testTag = "register_email_input"
                    )

                    ModernTextField(
                        value = password,
                        onValueChange = { 
                            password = it
                            passwordError = if (it.length < 6) "Min 6 chars" else null
                        },
                        label = "Password",
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
                        testTag = "register_password_input"
                    )

                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()
                    
                    LaunchedEffect(isPressed) {
                        if (isPressed) showDatePicker = true
                    }

                    ModernTextField(
                        value = dateOfBirth,
                        onValueChange = { },
                        label = "Date of Birth",
                        leadingIcon = Icons.Default.DateRange,
                        placeholder = "Select Date",
                        readOnly = true,
                        interactionSource = interactionSource,
                        isError = dobError != null,
                        errorMessage = dobError,
                        testTag = "register_dob_input"
                    )

                    ModernTextField(
                        value = address,
                        onValueChange = { 
                            address = it
                            addressError = if (it.isBlank()) "Required" else null
                        },
                        label = "Shipping Address",
                        leadingIcon = Icons.Default.LocalShipping,
                        testTag = "register_address_input",
                        singleLine = false,
                        minLines = 2,
                        isError = addressError != null,
                        errorMessage = addressError
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Button
                Button(
                    onClick = {
                        nameError = if (name.isBlank()) "Required" else null
                        emailError = when {
                            email.isBlank() -> "Required"
                            !emailRegex.matches(email) -> "Invalid"
                            else -> null
                        }
                        passwordError = if (password.length < 6) "Short" else null
                        dobError = if (dateOfBirth.isBlank()) "Required" else null
                        addressError = if (address.isBlank()) "Required" else null

                        if (nameError == null && emailError == null && passwordError == null && 
                            dobError == null && addressError == null) {
                            scope.launch {
                                isRegistering = true
                                authDialogState = AuthDialogState.Loading("Registration")
                                delay(1200)
                                isRegistering = false
                                viewModel.registerUser(name, email, dateOfBirth, address, true)
                                authDialogState = AuthDialogState.Success("Account Creation")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("register_submit_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    enabled = !isRegistering
                ) {
                    if (isRegistering) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Create Account",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Footer
                Row(
                    modifier = Modifier.padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Already registered? ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Sign In",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable { onNavigateBackToLogin() }
                            .testTag("login_navigation_link")
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
                isDark = isDark
            )
        }
    }
}

@Composable
fun ModernTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null,
    testTag: String = "",
    placeholder: String? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    readOnly: Boolean = false,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    isError: Boolean = false,
    errorMessage: String? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, style = MaterialTheme.typography.bodyMedium) },
            placeholder = placeholder?.let { { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) } },
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = if (isError) {
                { Icon(Icons.Default.Error, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.error) }
            } else {
                trailingIcon
            },
            modifier = modifier
                .fillMaxWidth()
                .testTag(testTag),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                errorBorderColor = MaterialTheme.colorScheme.error
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = ImeAction.Next
            ),
            visualTransformation = visualTransformation,
            singleLine = singleLine,
            minLines = minLines,
            readOnly = readOnly,
            interactionSource = interactionSource,
            isError = isError
        )
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(start = 12.dp, top = 2.dp)
            )
        }
    }
}
