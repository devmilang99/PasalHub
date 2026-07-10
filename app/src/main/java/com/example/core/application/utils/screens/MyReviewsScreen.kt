package com.example.core.application.utils.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

data class ReviewMock(
    val id: Int,
    val productName: String,
    val productImage: String,
    val rating: Int,
    val comment: String,
    val date: String
)

val mockReviews = listOf(
    ReviewMock(1, "Fjallraven - Foldsack No. 1 Backpack", "https://fakestoreapi.com/img/81fPKd-2AYL._AC_SL1500_.jpg", 5, "Amazing build quality. Perfect for my laptop and daily commute.", "20 Oct 2023"),
    ReviewMock(2, "Mens Casual Premium Slim Fit T-Shirts", "https://fakestoreapi.com/img/71-3HjGNDUL._AC_SY879._SX._UX._SY._UY_.jpg", 4, "Great fit, but the color is slightly different from the photos.", "15 Oct 2023"),
    ReviewMock(3, "SanDisk SSD PLUS 1TB Internal SSD", "https://fakestoreapi.com/img/61U7T1koQqL._AC_SX679_.jpg", 5, "Blazing fast speeds! My laptop feels like new again.", "02 Oct 2023"),
    ReviewMock(4, "White Gold Plated Princess", "https://fakestoreapi.com/img/71YAIFU48IL._AC_UL640_QL65_ML3_.jpg", 3, "It's beautiful but smaller than I expected.", "28 Sep 2023")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyReviewsScreen(
    onBack: () -> Unit,
    isDark: Boolean
) {
    val bgColor = if (isDark) Color(0xFF0F0F10) else Color(0xFFF8F9FA)
    val textColor = if (isDark) Color.White else Color(0xFF212529)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("My Reviews", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = bgColor,
                    scrolledContainerColor = Color.Unspecified,
                    navigationIconContentColor = textColor,
                    titleContentColor = textColor,
                    actionIconContentColor = Color.Unspecified
                )
            )
        },
        containerColor = bgColor
    ) { paddingValues ->
        if (mockReviews.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("You haven't posted any reviews yet.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(mockReviews) { review ->
                    ReviewItemCard(review = review, isDark = isDark)
                }
            }
        }
    }
}

@Composable
fun ReviewItemCard(review: ReviewMock, isDark: Boolean) {
    val cardColor = if (isDark) Color(0xFF1B1B1D) else Color.White
    val textColor = if (isDark) Color.White else Color(0xFF212529)
    val mutedTextColor = if (isDark) Color.Gray else Color(0xFF6C757D)
    val accentColor = MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White
                ) {
                    AsyncImage(
                        model = review.productImage,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().padding(4.dp),
                        contentScale = ContentScale.Fit
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = review.productName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = review.date,
                        style = MaterialTheme.typography.labelSmall,
                        color = mutedTextColor
                    )
                }
                IconButton(onClick = { /* Edit Review */ }) {
                    Icon(Icons.Rounded.Edit, contentDescription = "Edit", tint = accentColor, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                repeat(5) { index ->
                    Icon(
                        imageVector = if (index < review.rating) Icons.Rounded.Star else Icons.Rounded.Star,
                        contentDescription = null,
                        tint = if (index < review.rating) Color(0xFFFFD700) else Color.Gray.copy(alpha = 0.3f),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${review.rating}.0",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = review.comment,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor.copy(alpha = 0.8f),
                lineHeight = 20.sp
            )
        }
    }
}
