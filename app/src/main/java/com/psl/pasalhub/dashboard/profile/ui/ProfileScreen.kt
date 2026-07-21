package com.psl.pasalhub.dashboard.profile.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.rounded.AssignmentReturn
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.automirrored.rounded.HelpCenter
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.RateReview
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.SupportAgent
import androidx.compose.material.icons.rounded.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.psl.pasalhub.core.application.utils.screens.MyReviewsScreen
import com.psl.pasalhub.core.application.utils.screens.PasalHubAlertDialog
import com.psl.pasalhub.core.application.utils.screens.formatPrice
import com.psl.pasalhub.core.networking.remote.ProductDto
import com.psl.pasalhub.dashboard.order.viewmodel.OrderViewModel
import com.psl.pasalhub.dashboard.products.repository.Resource
import com.psl.pasalhub.dashboard.profile.viewmodel.ProfileViewModel
import com.psl.pasalhub.ui.theme.LocalDimens

@SuppressLint("ConfigurationScreenWidthHeight")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    orderViewModel: OrderViewModel,
    onLogout: () -> Unit,
    onProductClick: (ProductDto) -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val favoriteIds by viewModel.favoriteIds.collectAsStateWithLifecycle()
    val productResource by viewModel.homeProductsState.collectAsStateWithLifecycle()
    val storedPassword by viewModel.userPassword.collectAsStateWithLifecycle()
    val memberPoints by viewModel.memberPoints.collectAsStateWithLifecycle()
    val isDark by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val dimens = LocalDimens.current
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp

    LaunchedEffect(currentUser) {
        viewModel.loadFavorites()
        viewModel.loadMemberPoints()
        currentUser?.email?.let { email ->
            viewModel.loadPassword(email)
        }
    }

    val favoriteProducts = remember(productResource, favoriteIds) {
        when (productResource) {
            is Resource.Success -> {
                (productResource as Resource.Success<List<ProductDto>>).data.filter {
                    favoriteIds.contains(
                        it.id
                    )
                }
            }

            else -> emptyList()
        }
    }

    var showAddressDialog by remember { mutableStateOf(false) }
    var addressInput by remember(currentUser) { mutableStateOf(currentUser?.address ?: "") }
    var showPasswordSheet by remember { mutableStateOf(false) }
    var showFavoritesSheet by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showSupportSheet by remember { mutableStateOf(false) }
    var showReviewsScreen by remember { mutableStateOf(false) }

    if (showReviewsScreen) {
        MyReviewsScreen(
            viewModel = orderViewModel,
            onBack = { showReviewsScreen = false }
        )
        return
    }

    val bgColor = MaterialTheme.colorScheme.background
    val cardColor = MaterialTheme.colorScheme.surface
    val accentColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onSurface
    val mutedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val borderColor = MaterialTheme.colorScheme.outlineVariant

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        // --- 1. Header Section ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (screenHeight > 800) 140.dp else 120.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(accentColor.copy(alpha = 0.1f), Color.Transparent)
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier
                        .size(if (screenHeight > 800) 85.dp else 75.dp)
                        .shadow(8.dp, CircleShape),
                    shape = CircleShape,
                    color = cardColor,
                    border = BorderStroke(2.dp, Color.White)
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
                            imageVector = Icons.Rounded.Person,
                            contentDescription = "Avatar",
                            tint = accentColor.copy(alpha = 0.4f),
                            modifier = Modifier.padding(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(20.dp))

                Column {
                    Text(
                        text = currentUser?.name ?: "Valued Member",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = textColor
                    )
                    Text(
                        text = currentUser?.email ?: "member@pasalhub.com",
                        style = MaterialTheme.typography.bodyMedium,
                        color = mutedTextColor
                    )
                }
            }
        }

        // --- 2. Middle Section ---
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Row of 3 Separate Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Points",
                    value = memberPoints.toString(),
                    icon = Icons.Rounded.Star,
                    isDark = isDark,
                    accentColor = Color(0xFFFFD700)
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Favorites",
                    value = favoriteIds.size.toString(),
                    icon = Icons.Rounded.Favorite,
                    isDark = isDark,
                    accentColor = Color(0xFFF87171),
                    onClick = { showFavoritesSheet = true }
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Location",
                    value = if (currentUser?.address.isNullOrEmpty()) "Not Set" else currentUser?.address
                        ?: "Not Set",
                    icon = Icons.Rounded.LocationOn,
                    isDark = isDark,
                    accentColor = Color(0xFF4ADE80),
                    onClick = { showAddressDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Main Account Actions List Card
            SectionHeader("ACCOUNT SETTINGS")
            PremiumMenuCard(isDark) {
                if (currentUser?.isGoogleUser == false) {
                    PremiumMenuItem(
                        icon = Icons.Rounded.Security,
                        title = "Change Password",
                        subtitle = "Update your login credentials",
                        onClick = { showPasswordSheet = true },
                        isDark = isDark
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        color = borderColor
                    )
                }
                PremiumMenuItem(
                    icon = Icons.Rounded.RateReview,
                    title = "My Reviews",
                    subtitle = "Manage your feedback & ratings",
                    onClick = { showReviewsScreen = true },
                    isDark = isDark
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    color = borderColor
                )
                PremiumMenuItem(
                    icon = Icons.Rounded.SupportAgent,
                    title = "Customer Support",
                    subtitle = "Need help with your account?",
                    onClick = { showSupportSheet = true },
                    isDark = isDark
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        // --- 3. Footer Section ---
        Box(
            modifier = Modifier
                .padding(dimens.padding)
                .padding(bottom = 8.dp)
        ) {
            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimens.buttonHeight),
                shape = RoundedCornerShape(dimens.cardCorner),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDark) Color(0xFF2E1B1B) else Color(0xFFFFEBEB),
                    contentColor = if (isDark) Color(0xFFF87171) else Color(0xFFD90429)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text("Sign Out Securely", fontWeight = FontWeight.Bold)
            }
        }
    }

    // --- Bottom Sheet & Dialogs ---
    if (showFavoritesSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFavoritesSheet = false },
            containerColor = cardColor,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    "My Wishlist",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
                if (favoriteProducts.isEmpty()) {
                    EmptyFavoritesView(mutedTextColor)
                } else {
                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        favoriteProducts.forEach { prod ->
                            FavoriteListItem(
                                product = prod,
                                onProductClick = {
                                    showFavoritesSheet = false
                                    onProductClick(prod)
                                },
                                onRemove = { viewModel.toggleFavorite(prod.id) },
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
            title = { Text("Update Address", fontWeight = FontWeight.Bold) },
            containerColor = cardColor,
            text = {
                OutlinedTextField(
                    value = addressInput,
                    onValueChange = { addressInput = it },
                    label = { Text("Full Address") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.updateUserAddress(addressInput)
                    showAddressDialog = false
                }) { Text("Save") }
            }
        )
    }

    if (showPasswordSheet) {
        ChangePasswordBottomSheet(
            onDismiss = { showPasswordSheet = false },
            storedPassword = storedPassword ?: "",
            onUpdate = { newPass ->
                viewModel.updatePassword(currentUser?.email ?: "", newPass)
                showPasswordSheet = false
            },
            isDark = isDark
        )
    }

    if (showLogoutDialog) {
        PasalHubAlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = "Sign Out",
            text = "Are you sure you want to exit your session?",
            confirmButtonText = "Sign Out",
            onConfirm = { onLogout() },
            icon = Icons.AutoMirrored.Filled.Logout
        )
    }

    if (showSupportSheet) {
        CustomerSupportBottomSheet(
            onDismiss = { showSupportSheet = false },
            isDark = isDark
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordBottomSheet(
    onDismiss: () -> Unit,
    storedPassword: String,
    onUpdate: (String) -> Unit,
    isDark: Boolean
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val cardColor = if (isDark) Color(0xFF1B1B1D) else Color.White
    val textColor = if (isDark) Color.White else Color(0xFF212529)
    val accentColor = MaterialTheme.colorScheme.primary

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = cardColor,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Change Password",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = textColor
            )

            Text(
                "Update your account security with a new strong password.",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isDark) Color.Gray else Color(0xFF6C757D)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = currentPassword,
                onValueChange = { currentPassword = it },
                label = { Text("Current Password") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                visualTransformation = PasswordVisualTransformation(),
                leadingIcon = {
                    Icon(
                        Icons.Rounded.Lock,
                        contentDescription = null,
                        tint = accentColor
                    )
                }
            )

            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("New Password") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                visualTransformation = PasswordVisualTransformation(),
                leadingIcon = {
                    Icon(
                        Icons.Rounded.VpnKey,
                        contentDescription = null,
                        tint = accentColor
                    )
                }
            )

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm New Password") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                visualTransformation = PasswordVisualTransformation(),
                leadingIcon = {
                    Icon(
                        Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = accentColor
                    )
                }
            )

            val isMatch = newPassword == confirmPassword && newPassword.isNotEmpty()
            val isCurrentCorrect = currentPassword == storedPassword

            Button(
                onClick = { if (isMatch && isCurrentCorrect) onUpdate(newPassword) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = isMatch && isCurrentCorrect,
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Text("Update Password", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerSupportBottomSheet(
    onDismiss: () -> Unit,
    isDark: Boolean
) {
    val cardColor = if (isDark) Color(0xFF1B1B1D) else Color.White
    val textColor = if (isDark) Color.White else Color(0xFF212529)
    val mutedTextColor = if (isDark) Color.Gray else Color(0xFF6C757D)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = cardColor,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp)
        ) {
            Text(
                text = "Customer Support",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = textColor
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "How can we help you today?",
                style = MaterialTheme.typography.bodyMedium,
                color = mutedTextColor
            )

            Spacer(modifier = Modifier.height(24.dp))

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SupportItem(
                    icon = Icons.AutoMirrored.Rounded.HelpCenter,
                    title = "Help Center & FAQs",
                    description = "Find quick answers to common questions",
                    isDark = isDark
                )
                SupportItem(
                    icon = Icons.Rounded.LocalShipping,
                    title = "Shipping Policy",
                    description = "Details about delivery and tracking",
                    isDark = isDark
                )
                SupportItem(
                    icon = Icons.AutoMirrored.Rounded.AssignmentReturn,
                    title = "Returns & Refunds",
                    description = "Learn about our easy return process",
                    isDark = isDark
                )
                SupportItem(
                    icon = Icons.AutoMirrored.Rounded.Chat,
                    title = "Live Chat",
                    description = "Talk to our representative right now",
                    isDark = isDark,
                    badge = "Online"
                )
                SupportItem(
                    icon = Icons.Rounded.Call,
                    title = "Call Support",
                    description = "Speak with us at +1-800-PASAL-HUB",
                    isDark = isDark
                )
            }
        }
    }
}

@Composable
fun SupportItem(
    icon: ImageVector,
    title: String,
    description: String,
    isDark: Boolean,
    badge: String? = null
) {
    val textColor = if (isDark) Color.White else Color(0xFF212529)
    val mutedTextColor = if (isDark) Color.Gray else Color(0xFF6C757D)
    val itemBg = if (isDark) Color(0xFF252528) else Color(0xFFF8F9FA)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(itemBg)
            .clickable { /* Action */ }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                if (badge != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = Color(0xFF4ADE80).copy(alpha = 0.2f),
                        shape = CircleShape
                    ) {
                        Text(
                            text = badge,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF22C55E),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Text(
                text = description,
                style = MaterialTheme.typography.labelMedium,
                color = mutedTextColor
            )
        }

        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = mutedTextColor.copy(alpha = 0.3f),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    isDark: Boolean,
    accentColor: Color,
    onClick: () -> Unit = {}
) {
    val cardColor = if (isDark) Color(0xFF1B1B1D) else Color.White
    val textColor = if (isDark) Color.White else Color(0xFF212529)
    val mutedTextColor = if (isDark) Color.Gray else Color(0xFF6C757D)
    val borderColor =
        if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f)

    Card(
        modifier = modifier
            .height(110.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                color = accentColor.copy(alpha = 0.1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = mutedTextColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        letterSpacing = 2.sp,
        modifier = Modifier.padding(start = 8.dp, bottom = 14.dp)
    )
}

@Composable
fun PremiumMenuCard(isDark: Boolean, content: @Composable ColumnScope.() -> Unit) {
    val cardColor = if (isDark) Color(0xFF1B1B1D) else Color.White
    val borderColor =
        if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            content()
        }
    }
}

@Composable
fun PremiumMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isDark: Boolean,
    badge: String? = null
) {
    val textColor = if (isDark) Color.White else Color(0xFF212529)
    val mutedTextColor = if (isDark) Color.Gray else Color(0xFF6C757D)
    val itemColor = if (isDark) Color(0xFF252528) else Color(0xFFF1F3F5)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(itemColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(18.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = textColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(
                text = subtitle,
                color = mutedTextColor,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (badge != null) {
            Surface(color = MaterialTheme.colorScheme.primary, shape = CircleShape) {
                Text(
                    badge,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
        }
        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = mutedTextColor.copy(alpha = 0.3f),
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
fun EmptyFavoritesView(mutedTextColor: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(64.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.FavoriteBorder,
                contentDescription = null,
                tint = mutedTextColor.copy(alpha = 0.3f),
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Your wishlist is empty",
                color = mutedTextColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun FavoriteListItem(
    product: ProductDto,
    onProductClick: (ProductDto) -> Unit,
    onRemove: () -> Unit,
    isDark: Boolean
) {
    val textColor = MaterialTheme.colorScheme.onSurface
    val mutedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val itemColor = MaterialTheme.colorScheme.surfaceVariant
    val borderColor = MaterialTheme.colorScheme.outlineVariant

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
                Text(
                    product.title,
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(product.category ?: "General", color = mutedTextColor, fontSize = 12.sp)
                Text(
                    formatPrice(product.price),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp
                )
            }
            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = null,
                    tint = Color(0xFFF87171),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
