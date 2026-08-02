package com.vttcabs.customer.ui.screens.customer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vttcabs.common.BookingEntity
import com.vttcabs.common.RatingEntity
import com.vttcabs.common.UserRole
import com.vttcabs.common.VttRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RatingReviewScreen(
    repository: VttRepository,
    booking: BookingEntity,
    onSubmit: () -> Unit,
    onSkip: () -> Unit,
    onBackClick: () -> Unit
) {
    var rating by remember { mutableIntStateOf(0) }
    var review by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rate Your Ride") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.DirectionsCar, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(8.dp))
                        Text("Trip Completed", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(booking.pickupAddress.take(20), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Icon(Icons.Default.ArrowDownward, null, modifier = Modifier.size(16.dp))
                                Text(booking.dropAddress.take(20), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(String.format("%.1f km", booking.distanceKm), fontWeight = FontWeight.Bold)
                                Text("${booking.durationMins} mins", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("₹${String.format("%.0f", booking.totalFare)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text(booking.paymentMethod.name, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
            
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(modifier = Modifier.size(56.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Text(booking.driverName?.firstOrNull()?.toString() ?: "D", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(booking.driverName ?: "Driver", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text("${booking.vehicleModel} • ${booking.vehicleNumber}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("How was your trip?", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            (1..5).forEach { star ->
                                Icon(
                                    if (star <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                                    "$star star",
                                    modifier = Modifier.size(48.dp).clickable { rating = star },
                                    tint = if (star <= rating) Color(0xFFFFC107) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            when (rating) { 1 -> "Poor" 2 -> "Fair" 3 -> "Good" 4 -> "Very Good" 5 -> "Excellent" else -> "Tap to rate" },
                            color = when (rating) { 1 -> MaterialTheme.colorScheme.error 2 -> Color(0xFFFF9800) 3 -> Color(0xFFFFC107) 4 -> Color(0xFF8BC34A) 5 -> MaterialTheme.colorScheme.primary else -> MaterialTheme.colorScheme.onSurfaceVariant },
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            if (rating > 0) {
                item {
                    OutlinedTextField(
                        value = review,
                        onValueChange = { review = it },
                        label = { Text("Additional comments (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5
                    )
                }
            }
            
            item {
                Button(
                    onClick = {
                        isLoading = true
                        scope.launch {
                            try {
                                repository.submitRating(bookingId = booking.id, fromUserId = booking.customerId, toUserId = booking.driverId ?: "", fromUserType = UserRole.CUSTOMER, toUserType = UserRole.DRIVER, rating = rating, review = review)
                                onSubmit()
                            } catch (e: Exception) { } finally { isLoading = false }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = rating > 0 && !isLoading
                ) {
                    if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    else { Icon(Icons.Default.Send, null); Spacer(Modifier.width(8.dp)); Text("Submit Rating") }
                }
            }
            
            item { TextButton(onClick = onSkip, modifier = Modifier.fillMaxWidth()) { Text("Skip for now") } }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewRatingsScreen(repository: VttRepository, driverId: String, onBackClick: () -> Unit) {
    var ratings by remember { mutableStateOf<List<RatingEntity>>(emptyList()) }
    
    LaunchedEffect(driverId) { ratings = repository.getDriverRatings(driverId) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Ratings") },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val avgRating = ratings.map { it.rating }.average().takeIf { !it.isNaN() } ?: 0.0
                        Text(String.format("%.1f", avgRating), fontSize = 36.sp, fontWeight = FontWeight.Bold)
                        Row { (1..5).forEach { star -> Icon(if (star <= avgRating.toInt()) Icons.Default.Star else Icons.Default.StarBorder, null, modifier = Modifier.size(16.dp), tint = Color(0xFFFFC107)) } }
                        Text("${ratings.size} trips", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            
            if (ratings.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.RateReview, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(16.dp))
                        Text("No ratings yet", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(ratings) { rating ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Row { (1..5).forEach { star -> Icon(if (star <= rating.rating) Icons.Default.Star else Icons.Default.StarBorder, null, modifier = Modifier.size(16.dp), tint = Color(0xFFFFC107)) } }
                                    Text(java.text.SimpleDateFormat("dd MMM", java.util.Locale.getDefault()).format(java.util.Date(rating.createdAt)), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (rating.review.isNotBlank()) { Spacer(Modifier.height(4.dp)); Text(rating.review, fontSize = 14.sp) }
                            }
                        }
                    }
                }
            }
        }
    }
}
