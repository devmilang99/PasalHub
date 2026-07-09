package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerSupportScreen(
    onBack: () -> Unit,
    isDark: Boolean
) {
    val bgColor = if (isDark) Color(0xFF0F0F10) else Color(0xFFF8F9FA)
    val cardColor = if (isDark) Color(0xFF1B1B1D) else Color.White
    val textColor = if (isDark) Color.White else Color(0xFF212529)
    val mutedTextColor = if (isDark) Color.Gray else Color(0xFF6C757D)
    val accentColor = MaterialTheme.colorScheme.primary

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Customer Support", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = bgColor,
                    titleContentColor = textColor,
                    navigationIconContentColor = textColor
                )
            )
        },
        containerColor = bgColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Rounded.SupportAgent,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(80.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "How can we help you today?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = textColor,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Our support team is available 24/7 to assist you with any inquiries.",
                style = MaterialTheme.typography.bodyMedium,
                color = mutedTextColor,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            SupportOptionCard(
                icon = Icons.Rounded.Chat,
                title = "Live Chat",
                description = "Chat with our support team in real-time",
                isDark = isDark,
                onClick = { /* Handle Chat */ }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            SupportOptionCard(
                icon = Icons.Rounded.Email,
                title = "Email Support",
                description = "Send us an email and we'll reply within 24h",
                isDark = isDark,
                onClick = { /* Handle Email */ }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            SupportOptionCard(
                icon = Icons.Rounded.Call,
                title = "Phone Call",
                description = "Call us for urgent assistance",
                isDark = isDark,
                onClick = { /* Handle Call */ }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            SupportOptionCard(
                icon = Icons.Rounded.Help,
                title = "FAQs",
                description = "Find answers to frequently asked questions",
                isDark = isDark,
                onClick = { /* Handle FAQs */ }
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            Text(
                text = "Pasal Hub Support v1.0.2",
                style = MaterialTheme.typography.labelSmall,
                color = mutedTextColor
            )
        }
    }
}

@Composable
fun SupportOptionCard(
    icon: ImageVector,
    title: String,
    description: String,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val cardColor = if (isDark) Color(0xFF1B1B1D) else Color.White
    val textColor = if (isDark) Color.White else Color(0xFF212529)
    val mutedTextColor = if (isDark) Color.Gray else Color(0xFF6C757D)
    val accentColor = MaterialTheme.colorScheme.primary

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = cardColor),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = accentColor.copy(alpha = 0.1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.padding(12.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(20.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = mutedTextColor
                )
            }
            
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = mutedTextColor.copy(alpha = 0.5f)
            )
        }
    }
}
