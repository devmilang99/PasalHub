package com.psl.pasalhub.initial.presentation.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.psl.pasalhub.core.application.utils.NetworkUtils
import com.psl.pasalhub.core.application.utils.screens.PasalHubBackground
import com.psl.pasalhub.initial.presentation.InitialViewModel
import com.psl.pasalhub.ui.theme.PasalHubTheme
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    viewModel: InitialViewModel,
    onNavigateNext: () -> Unit
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val isDark by viewModel.isDarkTheme.collectAsState()
    val fadeAnim = remember { Animatable(0f) }
    var isAutologinActive by remember { mutableStateOf(false) }
    var showNoNetworkDialog by remember { mutableStateOf(false) }
    var retryTrigger by remember { mutableIntStateOf(0) }

    LaunchedEffect(currentUser, retryTrigger) {
        delay(500)

        if (!NetworkUtils.isNetworkAvailable(context)) {
            showNoNetworkDialog = true
            return@LaunchedEffect
        }

        if (currentUser?.isRemembered == true) {
            isAutologinActive = true
            delay(2000)
            onNavigateNext()
        } else {
            fadeAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1000)
            )
            delay(1500)
            onNavigateNext()
        }
    }

    if (showNoNetworkDialog) {
        AlertDialog(
            onDismissRequest = { },
            icon = {
                Icon(
                    imageVector = Icons.Rounded.WifiOff,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = PasalHubTheme.colors.error
                )
            },
            title = {
                Text(
                    text = "No Internet Connection",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    text = "Pasal Hub requires an active internet connection to provide you with the best experience. Please check your network settings and try again.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (NetworkUtils.isNetworkAvailable(context)) {
                            showNoNetworkDialog = false
                            retryTrigger++
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PasalHubTheme.colors.primary
                    )
                ) {
                    Text("Retry")
                }
            },
            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
            containerColor = PasalHubTheme.colors.surface,
            textContentColor = PasalHubTheme.colors.textSecondary,
            titleContentColor = PasalHubTheme.colors.textPrimary
        )
    }

    PasalHubBackground(isDark = isDark) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .testTag("splash_screen")
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp)
                .alpha(if (isAutologinActive) 1f else fadeAnim.value),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val primaryTextColor =
                if (isDark) MaterialTheme.colorScheme.primary else Color(0xFF0C1324)
            val secondaryTextColor =
                if (isDark) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.7f)
            val mutedTextColor =
                if (isDark) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f) else Color.Black.copy(
                    alpha = 0.5f
                )

            if (isAutologinActive && currentUser != null) {
                Spacer(modifier = Modifier.weight(1.2f))

                Text(
                    text = "Welcome back,",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Light,
                    color = secondaryTextColor,
                    letterSpacing = 2.sp
                )

                Text(
                    text = currentUser?.name?.uppercase() ?: "VALUED MEMBER",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = primaryTextColor,
                    letterSpacing = 4.sp,
                    fontFamily = FontFamily.SansSerif
                )

                Spacer(modifier = Modifier.weight(1f))

                CircularProgressIndicator(
                    color = primaryTextColor,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(40.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))
            } else {
                Spacer(modifier = Modifier.weight(1.2f))

                Text(
                    text = "PASAL HUB",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = primaryTextColor,
                    letterSpacing = 8.sp,
                    fontFamily = FontFamily.SansSerif
                )

                Text(
                    text = "C U R A T E D",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isDark) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f) else Color.Black.copy(
                        alpha = 0.7f
                    ),
                    letterSpacing = 8.sp,
                    fontFamily = FontFamily.SansSerif,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                CircularProgressIndicator(
                    color = primaryTextColor,
                    strokeWidth = 3.dp,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("splash_loading")
                )

                Spacer(modifier = Modifier.height(32.dp))
            }

            Text(
                text = "Premium E-Commerce Experience",
                fontSize = 12.sp,
                fontWeight = FontWeight.Light,
                color = mutedTextColor,
                letterSpacing = 2.sp
            )
        }
    }
}
