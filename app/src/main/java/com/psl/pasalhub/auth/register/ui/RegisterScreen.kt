package com.psl.pasalhub.auth.register.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.psl.pasalhub.R
import com.psl.pasalhub.auth.register.viewmodel.RegisterViewModel
import com.psl.pasalhub.core.application.utils.screens.AuthDialogState
import com.psl.pasalhub.core.application.utils.screens.PasalHubAuthDialog
import com.psl.pasalhub.core.application.utils.screens.PasalHubBackground
import com.psl.pasalhub.ui.theme.LocalDimens
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
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

    val passwordInteractionSource = remember { MutableInteractionSource() }
    val isPasswordFocused by passwordInteractionSource.collectIsFocusedAsState()

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
    val dimens = LocalDimens.current

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

    PasalHubBackground(isDark = isDark) {
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
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Unspecified,
                        navigationIconContentColor = Color.Unspecified,
                        titleContentColor = Color.Unspecified,
                        actionIconContentColor = Color.Unspecified
                    )
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .imePadding()
                    .padding(horizontal = dimens.padding)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Branding Header
                Surface(
                    modifier = Modifier
                        .size(dimens.logoSize)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.primary,
                    tonalElevation = 4.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = null,
                            modifier = Modifier.size(dimens.logoSize * 0.5f),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(dimens.small))

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

                Spacer(modifier = Modifier.height(dimens.medium))

                // Input Section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(dimens.small)
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
                        testTag = "register_password_input",
                        interactionSource = passwordInteractionSource
                    )

                    // Password Requirements Checklist
                    if (isPasswordFocused) {
                        PasswordRequirements(password = password)
                    }

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

                Spacer(modifier = Modifier.height(dimens.large))

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
                            dobError == null && addressError == null
                        ) {
                            scope.launch {
                                isRegistering = true
                                authDialogState = AuthDialogState.Loading("Registration")
                                try {
                                    viewModel.signUp(name, email, password)
                                    authDialogState = AuthDialogState.Success("Account Creation")
                                } catch (e: Exception) {
                                    authDialogState =
                                        AuthDialogState.Error(e.message ?: "Registration failed")
                                } finally {
                                    isRegistering = false
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dimens.buttonHeight)
                        .testTag("register_submit_button"),
                    shape = RoundedCornerShape(dimens.cardCorner),
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

                Spacer(modifier = Modifier.height(dimens.medium))

                // Footer
                Row(
                    modifier = Modifier.padding(bottom = dimens.medium),
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
                        onNavigateBackToLogin()
                    }
                    authDialogState = null
                },
                state = state,

                )
        }
    }
}

@Composable
fun PasswordRequirements(password: String) {
    val requirements = listOf(
        "At least 8 characters" to (password.length >= 8),
        "At least one uppercase" to password.any { it.isUpperCase() },
        "At least one number" to password.any { it.isDigit() },
        "At least one special char" to password.any { !it.isLetterOrDigit() }
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        requirements.forEach { (text, isValid) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (isValid) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = if (isValid) Color(0xFF4ADE80) else MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = 0.5f
                    )
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isValid) Color(0xFF4ADE80) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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
            placeholder = placeholder?.let {
                {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            },
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = if (isError) {
                {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            } else {
                trailingIcon
            },
            modifier = modifier
                .fillMaxWidth()
                .testTag(testTag),
            shape = RoundedCornerShape(LocalDimens.current.cardCorner),
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
