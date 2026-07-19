package com.psl.pasalhub.initial.presentation.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import com.psl.pasalhub.R
import com.psl.pasalhub.core.application.utils.NetworkUtils
import com.psl.pasalhub.core.application.utils.screens.PasalHubBackground
import com.psl.pasalhub.initial.presentation.InitialViewModel
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
            title = { Text("No Internet Connection", fontWeight = FontWeight.Bold) },
            text = { Text("Pasal Hub requires an active internet connection to provide you with the best experience. Please check your network settings and try again.") },
            confirmButton = {
                Button(onClick = {
                    if (NetworkUtils.isNetworkAvailable(context)) {
                        showNoNetworkDialog = false
                        retryTrigger++
                    }
                }) {
                    Text("Retry")
                }
            }
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
