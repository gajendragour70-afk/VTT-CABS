package com.vttcabs.customer.ui.screens.map

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vttcabs.common.BookingStatus
import com.vttcabs.customer.viewmodel.CustomerViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackingScreen(
    viewModel: CustomerViewModel,
    bookingId: String,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var driverLat by remember { mutableStateOf(0.0) }
    var driverLng by remember { mutableStateOf(0.0) }
    
    LaunchedEffect(bookingId) {
        viewModel.loadBooking(bookingId)
    }
    
    // Simulate driver movement
    LaunchedEffect(uiState.currentBooking) {
        if (uiState.currentBooking != null) {
            driverLat = uiState.currentBooking!!.driverLat
            driverLng = uiState.currentBooking!!.driverLng
            while (true) {
                delay(3000)
                // Simulate driver moving towards pickup
                val pickup = uiState.currentBooking!!.pickupLat
                val plng = uiState.currentBooking!!.pickupLng
                driverLat += (pickup - driverLat) * 0.1
                driverLng += (plng - driverLng) * 0.1
            }
        }
    }
    
    val booking = uiState.currentBooking
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Track Your Ride") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (booking == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Map placeholder
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Map,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Live Tracking",
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (driverLat != 0.0) {
                                Text(
                                    text = "Driver Location: ${String.format("%.4f", driverLat)}, ${String.format("%.4f", driverLng)}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                
                // Booking Info Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        // Status
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Status",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = booking.bookingStatus.label,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                            StatusIndicator(status = booking.bookingStatus)
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Pickup & Drop
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.TripOrigin,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Pickup",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = booking.pickupAddress,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Drop",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = booking.dropAddress,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        
                        if (booking.driverName != null) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Divider()
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Driver Info
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    modifier = Modifier.size(48.dp),
                                    shape = MaterialTheme.shapes.medium,
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val driverInitial = booking.driverName?.firstOrNull()?.toString() ?: "D"
                                        Text(
                                            text = driverInitial,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = booking.driverName ?: "Driver Assigned",
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = booking.vehicleModel ?: "",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = booking.vehicleNumber ?: "",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(onClick = { }) {
                                    Icon(Icons.Default.Phone, contentDescription = "Call Driver")
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Fare
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Total Fare",
                                fontSize = 16.sp
                            )
                            Text(
                                text = "₹${String.format("%.2f", booking.totalFare)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusIndicator(status: BookingStatus) {
    val (color, icon) = when (status) {
        BookingStatus.PENDING, BookingStatus.SEARCHING -> Pair(MaterialTheme.colorScheme.tertiary, Icons.Default.HourglassEmpty)
        BookingStatus.ASSIGNED, BookingStatus.ACCEPTED -> Pair(MaterialTheme.colorScheme.primary, Icons.Default.CheckCircle)
        BookingStatus.DRIVER_ARRIVING, BookingStatus.ARRIVED -> Pair(MaterialTheme.colorScheme.secondary, Icons.Default.DirectionsCar)
        BookingStatus.TRIP_STARTED, BookingStatus.IN_PROGRESS -> Pair(MaterialTheme.colorScheme.primary, Icons.Default.PlayArrow)
        BookingStatus.TRIP_COMPLETED, BookingStatus.COMPLETED -> Pair(MaterialTheme.colorScheme.tertiary, Icons.Default.Done)
        BookingStatus.CANCELLED -> Pair(MaterialTheme.colorScheme.error, Icons.Default.Cancel)
    }
    
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = status.name,
                color = color,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
