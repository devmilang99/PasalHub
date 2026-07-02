package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.remote.ProductDto
import com.example.data.repository.Resource
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    onLogout: () -> Unit,
    onProductClick: (ProductDto) -> Unit
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    val productResource by viewModel.productsState.collectAsState()
    val storedPassword by viewModel.userPassword.collectAsState()
    val memberPoints by viewModel.memberPoints.collectAsState()
    val isDark by viewModel.isDarkTheme.collectAsState()

    LaunchedEffect(currentUser) {
        viewModel.loadFavorites(context)
        viewModel.loadMemberPoints(context)
        currentUser?.email?.let { email ->
            viewModel.loadPassword(context, email)
        }
    }

    val favoriteProducts = remember(productResource, favoriteIds) {
        when (productResource) {
            is Resource.Success -> {
                (productResource as Resource.Success<List<ProductDto>>).data.filter { favoriteIds.contains(it.id) }
            }
            else -> emptyList()
        }
    }

    var showAddressDialog by remember { mutableStateOf(false) }
    var addressInput by remember(currentUser) { mutableStateOf(currentUser?.address ?: "") }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var currentPasswordInput by remember { mutableStateOf("") }
    var newPasswordInput by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showFavoritesSheet by remember { mutableStateOf(false) }
    var favoriteSearchQuery by remember { mutableStateOf("") }
    var showLogoutDialog by remember { mutableStateOf(false) }

    val bgColor = if (isDark) Color(0xFF0F0F10) else Color(0xFFF8F9FA)
    val cardColor = if (isDark) Color(0xFF1B1B1D) else Color.White
    val itemColor = if (isDark) Color(0xFF252528) else Color(0xFFF1F3F5)
    val textColor = if (isDark) Color.White else Color(0xFF212529)
    val mutedTextColor = if (isDark) Color.Gray else Color(0xFF6C757D)
    val accentColor = Color(0xFF4CAF50)
    val borderColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        // --- Header (User Image, Points, Favorites) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = if (isDark) {
                            listOf(Color(0xFF1B1B1D), Color(0xFF0F0F10))
                        } else {
                            listOf(Color(0xFFE9ECEF), Color(0xFFF8F9FA))
                        }
                    )
                )
                .padding(top = 48.dp, bottom = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(itemColor)
                            .border(2.dp, accentColor.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (currentUser?.profileImage != null) {
                            AsyncImage(
                                model = currentUser?.profileImage,
                                contentDescription = "Avatar",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Avatar",
                                tint = textColor,
                                modifier = Modifier.size(50.dp)
                            )
                        }
                    }
                    Surface(
                        modifier = Modifier.size(28.dp),
                        shape = CircleShape,
                        color = accentColor,
                        shadowElevation = 4.dp
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit Profile",
                            modifier = Modifier.padding(6.dp),
                            tint = Color.White
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = currentUser?.name ?: "Valued Guest",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Text(
                    text = currentUser?.email ?: "guest@pasalhub.com",
                    fontSize = 14.sp,
                    color = mutedTextColor,
                    modifier = Modifier.padding(top = 2.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(0.8f),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ProfileStat(label = "Points", value = memberPoints.toString(), icon = Icons.Outlined.MonetizationOn, isDark = isDark)
                    VerticalDivider(modifier = Modifier.height(32.dp), color = textColor.copy(alpha = 0.1f))
                    ProfileStat(label = "Favorites", value = favoriteIds.size.toString(), icon = Icons.Outlined.FavoriteBorder, isDark = isDark)
                }
            }
        }

        // --- Scrollable Content (Settings, Shopping, Support) ---
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            // --- Account Section ---
            SectionHeader("ACCOUNT SETTINGS", isDark)
            ProfileMenuCard(isDark) {
                ProfileMenuItem(
                    icon = Icons.Outlined.LocationOn,
                    title = "Shipping Address",
                    subtitle = currentUser?.address ?: "Set your default address",
                    onClick = { showAddressDialog = true },
                    isDark = isDark
                )
                HorizontalDivider(color = textColor.copy(alpha = 0.05f), modifier = Modifier.padding(horizontal = 16.dp))
                ProfileMenuItem(
                    icon = Icons.Outlined.Lock,
                    title = "Security & Privacy",
                    subtitle = "Update password and secure access",
                    onClick = { showPasswordDialog = true },
                    isDark = isDark
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- Shopping Section ---
            SectionHeader("SHOPPING ACTIVITY", isDark)
            ProfileMenuCard(isDark) {
                ProfileMenuItem(
                    icon = Icons.Outlined.Favorite,
                    title = "My Favorites",
                    subtitle = "${favoriteProducts.size} items saved to your list",
                    onClick = { showFavoritesSheet = true },
                    isDark = isDark,
                    trailingContent = {
                        if (favoriteProducts.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(accentColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(favoriteProducts.size.toString(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- Support Section ---
            SectionHeader("SUPPORT", isDark)
            ProfileMenuCard(isDark) {
                ProfileMenuItem(
                    icon = Icons.AutoMirrored.Outlined.HelpOutline,
                    title = "Help & Support",
                    subtitle = "FAQs and 24/7 Customer Service",
                    onClick = { showHelpDialog = true },
                    isDark = isDark
                )
                HorizontalDivider(color = textColor.copy(alpha = 0.05f), modifier = Modifier.padding(horizontal = 16.dp))
                ProfileMenuItem(
                    icon = Icons.Outlined.VerifiedUser,
                    title = "Privacy Policy",
                    subtitle = "How we protect your data",
                    onClick = { showPrivacyDialog = true },
                    isDark = isDark
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        // --- Footer (Logout) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .padding(bottom = 16.dp)
        ) {
            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDark) Color(0xFF2E1B1B) else Color(0xFFFFEBEB),
                    contentColor = if (isDark) Color(0xFFF87171) else Color(0xFFD90429)
                ),
                border = BorderStroke(1.dp, (if (isDark) Color(0xFFF87171) else Color(0xFFD90429)).copy(alpha = 0.2f))
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("Logout Securely", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }

    // --- Favorites Bottom Sheet ---
    if (showFavoritesSheet) {
        ModalBottomSheet(
            onDismissRequest = { 
                showFavoritesSheet = false
                favoriteSearchQuery = ""
            },
            containerColor = cardColor,
            dragHandle = { BottomSheetDefaults.DragHandle(color = mutedTextColor.copy(alpha = 0.5f)) },
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            val filteredFavorites = remember(favoriteProducts, favoriteSearchQuery) {
                favoriteProducts.filter { 
                    it.title.contains(favoriteSearchQuery, ignoreCase = true) ||
                    it.category.contains(favoriteSearchQuery, ignoreCase = true)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "My Favorites",
                        style = MaterialTheme.typography.headlineSmall,
                        color = textColor,
                        fontWeight = FontWeight.ExtraBold
                    )
                    if (favoriteProducts.isNotEmpty()) {
                        Text(
                            "${favoriteProducts.size} Items",
                            color = accentColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (favoriteProducts.isNotEmpty()) {
                    OutlinedTextField(
                        value = favoriteSearchQuery,
                        onValueChange = { favoriteSearchQuery = it },
                        placeholder = { Text("Search in favorites...", color = mutedTextColor, fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = mutedTextColor) },
                        trailingIcon = {
                            if (favoriteSearchQuery.isNotEmpty()) {
                                IconButton(onClick = { favoriteSearchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = mutedTextColor)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = borderColor,
                            unfocusedContainerColor = itemColor,
                            focusedContainerColor = itemColor
                        )
                    )
                }
                
                if (favoriteProducts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(64.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.FavoriteBorder, contentDescription = null, tint = mutedTextColor, modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Your list is empty", color = mutedTextColor, fontSize = 16.sp)
                        }
                    }
                } else if (filteredFavorites.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(64.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No matching items found", color = mutedTextColor, fontSize = 14.sp)
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        filteredFavorites.forEach { prod ->
                            FavoriteListItem(
                                product = prod,
                                onProductClick = {
                                    showFavoritesSheet = false
                                    favoriteSearchQuery = ""
                                    onProductClick(it)
                                },
                                onRemove = { viewModel.toggleFavorite(context, prod.id) },
                                isDark = isDark
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddressDialog) {
        AlertDialog(
            onDismissRequest = { showAddressDialog = false },
            title = { Text("Update Shipping Address", fontWeight = FontWeight.Bold, color = textColor) },
            containerColor = cardColor,
            text = {
                OutlinedTextField(
                    value = addressInput,
                    onValueChange = { addressInput = it },
                    label = { Text("Shipping Address") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = mutedTextColor.copy(alpha = 0.5f)
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateUserAddress(addressInput)
                        viewModel.notificationEvent.tryEmit("Address updated successfully!")
                        showAddressDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showAddressDialog = false }) { Text("Cancel", color = mutedTextColor) } }
        )
    }

    if (showPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showPasswordDialog = false },
            title = { Text("Change Password", fontWeight = FontWeight.Bold, color = textColor) },
            containerColor = cardColor,
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = currentPasswordInput,
                        onValueChange = { currentPasswordInput = it },
                        label = { Text("Current Password") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = mutedTextColor.copy(alpha = 0.5f)
                        )
                    )
                    OutlinedTextField(
                        value = newPasswordInput,
                        onValueChange = { newPasswordInput = it },
                        label = { Text("New Password") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = mutedTextColor.copy(alpha = 0.5f)
                        )
                    )
                    OutlinedTextField(
                        value = confirmPasswordInput,
                        onValueChange = { confirmPasswordInput = it },
                        label = { Text("Confirm New Password") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = mutedTextColor.copy(alpha = 0.5f)
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (currentPasswordInput != storedPassword) {
                            viewModel.notificationEvent.tryEmit("Incorrect current password!")
                        } else if (newPasswordInput != confirmPasswordInput) {
                            viewModel.notificationEvent.tryEmit("New passwords do not match!")
                        } else if (newPasswordInput.isEmpty()) {
                            viewModel.notificationEvent.tryEmit("Password cannot be empty!")
                        } else {
                            viewModel.updatePassword(context, currentUser?.email ?: "", newPasswordInput)
                            viewModel.notificationEvent.tryEmit("Password updated successfully!")
                            showPasswordDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) { Text("Update") }
            },
            dismissButton = { TextButton(onClick = { showPasswordDialog = false }) { Text("Cancel", color = mutedTextColor) } }
        )
    }

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = { Text("Help & Support", fontWeight = FontWeight.Bold, color = textColor) },
            containerColor = cardColor,
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("How can we help you today?", fontWeight = FontWeight.Bold, color = textColor)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("1. Track Order: Go to 'Orders' tab to see real-time updates.", color = mutedTextColor, fontSize = 13.sp)
                    Text("2. Returns: Contact support@pasalhub.com for returns.", color = mutedTextColor, fontSize = 13.sp)
                    Text("3. Payments: We support COD, Cards, and E-sewa.", color = mutedTextColor, fontSize = 13.sp)
                }
            },
            confirmButton = {
                Button(onClick = { showHelpDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = accentColor)) {
                    Text("Close")
                }
            }
        )
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("Privacy Policy", fontWeight = FontWeight.Bold, color = textColor) },
            containerColor = cardColor,
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("Your privacy is our priority.", fontWeight = FontWeight.Bold, color = textColor)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("We only collect data necessary to process your luxury orders and provide a personalized experience. We do not sell your personal information to third parties.", color = mutedTextColor, fontSize = 13.sp)
                }
            },
            confirmButton = {
                Button(onClick = { showPrivacyDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = accentColor)) {
                    Text("I Understand")
                }
            }
        )
    }

    if (showLogoutDialog) {
        PasalHubAlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = "Sign Out",
            text = "Are you sure you want to sign out? You will need to log in again to access your favorites and profile details.",
            confirmButtonText = "Sign Out",
            onConfirm = {
                showLogoutDialog = false
                onLogout()
            },
            dismissButtonText = "Cancel",
            isDark = isDark,
            icon = Icons.AutoMirrored.Filled.Logout,
            iconColor = if (isDark) Color(0xFFF87171) else Color(0xFFD90429)
        )
    }
}

@Composable
fun SectionHeader(title: String, isDark: Boolean) {
    Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = FontWeight.Black,
        color = Color(0xFF4CAF50),
        letterSpacing = 1.5.sp,
        modifier = Modifier.padding(start = 8.dp, bottom = 12.dp)
    )
}

@Composable
fun ProfileStat(label: String, value: String, icon: ImageVector, isDark: Boolean) {
    val textColor = if (isDark) Color.White else Color(0xFF212529)
    val mutedTextColor = if (isDark) Color.Gray else Color(0xFF6C757D)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Black, color = textColor)
        }
        Text(text = label, fontSize = 12.sp, color = mutedTextColor)
    }
}

@Composable
fun ProfileMenuCard(isDark: Boolean, content: @Composable ColumnScope.() -> Unit) {
    val cardColor = if (isDark) Color(0xFF1B1B1D) else Color.White
    val borderColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isDark: Boolean,
    trailingContent: @Composable (() -> Unit)? = null
) {
    val textColor = if (isDark) Color.White else Color(0xFF212529)
    val mutedTextColor = if (isDark) Color.Gray else Color(0xFF6C757D)
    val itemColor = if (isDark) Color(0xFF252528) else Color(0xFFF1F3F5)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(itemColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = textColor, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = textColor, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(text = subtitle, color = mutedTextColor, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (trailingContent != null) {
            trailingContent()
            Spacer(modifier = Modifier.width(8.dp))
        }
        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = mutedTextColor.copy(alpha = 0.3f))
    }
}

@Composable
fun FavoriteListItem(
    product: ProductDto,
    onProductClick: (ProductDto) -> Unit,
    onRemove: () -> Unit,
    isDark: Boolean
) {
    val textColor = if (isDark) Color.White else Color(0xFF212529)
    val mutedTextColor = if (isDark) Color.Gray else Color(0xFF6C757D)
    val itemColor = if (isDark) Color(0xFF252528) else Color(0xFFF1F3F5)
    val borderColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onProductClick(product) },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = itemColor),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = product.image,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(product.title, color = textColor, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(product.category, color = mutedTextColor, fontSize = 12.sp)
                Text(formatPrice(product.price), color = Color(0xFF4CAF50), fontWeight = FontWeight.Black, fontSize = 14.sp)
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Favorite, contentDescription = null, tint = Color(0xFFF87171), modifier = Modifier.size(20.dp))
            }
        }
    }
}
