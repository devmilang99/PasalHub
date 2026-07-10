package com.example.initial.presentation.permission

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.initial.presentation.InitialViewModel

data class PermissionStepData(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val testTag: String
)

@Composable
fun PermissionScreen(
    viewModel: InitialViewModel,
    onNavigateNext: () -> Unit
) {
    val isDark by viewModel.isDarkTheme.collectAsState()
    val locationGranted by viewModel.locationPermissionGranted.collectAsState()
    val cameraGranted by viewModel.cameraPermissionGranted.collectAsState()
    val storageGranted by viewModel.storagePermissionGranted.collectAsState()
    val notificationGranted by viewModel.notificationPermissionGranted.collectAsState()

    val steps = listOf(
        PermissionStepData(
            title = "Location Access",
            description = "Needed to suggest nearest physical pickup hubs and calculate express delivery timeframes.",
            icon = Icons.Default.LocationOn,
            testTag = "step_location"
        ),
        PermissionStepData(
            title = "Camera Access",
            description = "Used to scan product barcodes, verify QR vouchers, and scan bank card details safely.",
            icon = Icons.Default.CameraAlt,
            testTag = "step_camera"
        ),
        PermissionStepData(
            title = "Storage & Assets",
            description = "Allows secure caching of high-definition e-commerce product listings and offline invoice logs.",
            icon = Icons.Default.Folder,
            testTag = "step_storage"
        ),
        PermissionStepData(
            title = "Instant Notifications",
            description = "Receive instantaneous updates on exclusive price drops, order tracking checkpoints, and live delivery status.",
            icon = Icons.Default.Notifications,
            testTag = "step_notification"
        )
    )

    var currentStep by remember { mutableIntStateOf(0) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            val allGranted = permissions.values.all { it }
            if (allGranted) {
                when (currentStep) {
                    0 -> viewModel.setLocationPermission(true)
                    1 -> viewModel.setCameraPermission(true)
                    2 -> viewModel.setStoragePermission(true)
                    3 -> viewModel.setNotificationPermission(true)
                }
            }
        }
    )

    val currentPermissionGranted = when (currentStep) {
        0 -> locationGranted
        1 -> cameraGranted
        2 -> storageGranted
        3 -> notificationGranted
        else -> false
    }

    LaunchedEffect(currentStep, currentPermissionGranted) {
        if (currentPermissionGranted) {
            if (currentStep < steps.lastIndex) {
                currentStep++
            } else {
                onNavigateNext()
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("permission_screen"),
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = if (isDark) R.drawable.image_bg_dark else R.drawable.image_bg_light),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
            // Header showing step counter
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "STEP ${currentStep + 1} OF ${steps.size}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 3.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "PASAL HUB SECURITY",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    letterSpacing = 1.5.sp
                )
            }

            // Central graphic
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.05f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .background(
                            if (currentPermissionGranted) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(
                                if (currentPermissionGranted) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = steps[currentStep].icon,
                            contentDescription = "${steps[currentStep].title} Icon",
                            tint = if (currentPermissionGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(54.dp)
                        )
                    }
                }

                if (currentPermissionGranted) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = "Permission Granted",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.background, CircleShape)
                            .align(Alignment.BottomEnd)
                            .offset(x = (-45).dp, y = (-45).dp)
                    )
                }
            }

            // Description text
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = steps[currentStep].title,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = steps[currentStep].description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Action Button and Progress Indicators
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    steps.forEachIndexed { index, _ ->
                        val isSelected = index == currentStep
                        val dotWidth by animateDpAsState(
                            targetValue = if (isSelected) 24.dp else 8.dp,
                            label = "dotWidth"
                        )
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(dotWidth)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                )
                        )
                    }
                }

                Button(
                    onClick = {
                        val permissionsToRequest = when (currentStep) {
                            0 -> arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                            1 -> arrayOf(Manifest.permission.CAMERA)
                            2 -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                arrayOf(
                                    Manifest.permission.READ_MEDIA_IMAGES,
                                    Manifest.permission.READ_MEDIA_VIDEO
                                )
                            } else {
                                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                            }
                            3 -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                arrayOf(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                emptyArray()
                            }
                            else -> emptyArray()
                        }
                        
                        if (permissionsToRequest.isNotEmpty()) {
                            permissionLauncher.launch(permissionsToRequest)
                        } else {
                            when (currentStep) {
                                0 -> viewModel.setLocationPermission(true)
                                1 -> viewModel.setCameraPermission(true)
                                2 -> viewModel.setStoragePermission(true)
                                3 -> viewModel.setNotificationPermission(true)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("permission_next_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = "Grant Permission",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Pasal Hub is sandboxed, enabling secure and reliable permission validation.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    }
}
}
