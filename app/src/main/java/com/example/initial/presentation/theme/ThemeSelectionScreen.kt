package com.example.initial.presentation.theme

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.initial.presentation.InitialViewModel

@Composable
fun ThemeSelectionScreen(
    viewModel: InitialViewModel,
    onNavigateNext: () -> Unit
) {
    val isDarkSelected by viewModel.isDarkTheme.collectAsState()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("theme_selection_screen"),
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = if (isDarkSelected) R.drawable.image_bg_dark else R.drawable.image_bg_light),
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "PASAL HUB",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 4.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Select Your Theme",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Choose a layout style that suits your current ambient lighting and personal elegance.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            }

            // Selection Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Light Theme Card
                ThemeCard(
                    title = "Pristine Light",
                    description = "Warm alabaster and deep navy design optimized for luxury clarity and sharp daylight viewing.",
                    isDark = false,
                    isSelected = !isDarkSelected,
                    onSelect = { viewModel.setTheme(false) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("theme_light_card")
                )

                // Dark Theme Card
                ThemeCard(
                    title = "Emerald Dark",
                    description = "Sophisticated emerald green and pure black design for a modern, high-contrast aesthetic.",
                    isDark = true,
                    isSelected = isDarkSelected,
                    onSelect = { viewModel.setTheme(true) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("theme_dark_card")
                )
            }

            // Continue Button
            Button(
                onClick = {
                    onNavigateNext()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("theme_apply_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Apply Theme & Continue",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}
}

@Composable
fun ThemeCard(
    title: String,
    description: String,
    isDark: Boolean,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scaleFactor by animateFloatAsState(targetValue = if (isSelected) 1.02f else 0.98f, label = "scale")
    val borderStroke = if (isSelected) {
        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
    }

    Card(
        modifier = modifier
            .scale(scaleFactor)
            .clip(RoundedCornerShape(24.dp))
            .clickable { onSelect() },
        shape = RoundedCornerShape(24.dp),
        border = borderStroke,
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF1B1B1D) else Color(0xFFFDFBF7)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        if (isDark) {
                            Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFF064E3B)))
                        } else {
                            Brush.linearGradient(listOf(Color(0xFF0C1324), Color(0xFF334155)))
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                    contentDescription = "$title Icon",
                    tint = if (isDark) Color(0xFF141218) else Color(0xFFFEF7FF),
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else Color(0xFF0C1324)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                fontSize = 11.sp,
                color = if (isDark) Color.LightGray else Color(0xFF64748B),
                lineHeight = 16.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Sample Color Palettes Visual Indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(if (isDark) Color(0xFF10B981) else Color(0xFF0C1324)))
                Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(if (isDark) Color(0xFF000000) else Color(0xFFFDFBF7)).border(1.dp, Color.LightGray, CircleShape))
                Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(if (isDark) Color(0xFF34D399) else Color(0xFFF97316)))
            }
        }
    }
}
