package com.vttcabs.customer.ui.screens.customer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

data class ScheduledRide(
    val id: String,
    val pickupAddress: String,
    val dropAddress: String,
    val date: Long,
    val time: String,
    val vehicleType: String,
    val estimatedFare: Double,
    val status: String // "scheduled", "cancelled"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduledRidesScreen(
    rides: List<ScheduledRide> = listOf(
        ScheduledRide(
            "1",
            "123 MG Road, Bangalore",
            "456 Indiranagar, Bangalore",
            System.currentTimeMillis() + 86400000, // Tomorrow
            "10:30 AM",
            "Sedan",
            350.0,
            "scheduled"
        ),
        ScheduledRide(
            "2",
            "Home",
            "Kempegowda Airport",
            System.currentTimeMillis() + 172800000, // Day after tomorrow
            "06:00 AM",
            "Innova Crysta",
            1200.0,
            "scheduled"
        )
    ),
    onCancelRide: (String) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    var showCancelDialog by remember { mutableStateOf<ScheduledRide?>(null) }
    val dateFormat = remember { SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scheduled Rides") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (rides.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.EventBusy,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No scheduled rides",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Schedule a ride in advance",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(rides) { ride ->
                    ScheduledRideCard(
                        ride = ride,
                        dateFormat = dateFormat,
                        onCancel = { showCancelDialog = ride }
                    )
                }
            }
        }
    }

    // Cancel Dialog
    showCancelDialog?.let { ride ->
        AlertDialog(
            onDismissRequest = { showCancelDialog = null },
            title = { Text("Cancel Scheduled Ride?") },
            text = {
                Column {
                    Text("Are you sure you want to cancel this scheduled ride?")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${dateFormat.format(Date(ride.date))} at ${ride.time}",
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onCancelRide(ride.id)
                        showCancelDialog = null
                    }
                ) {
                    Text("Cancel Ride", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = null }) {
                    Text("Keep Ride")
                }
            }
        )
    }
}

@Composable
private fun ScheduledRideCard(
    ride: ScheduledRide,
    dateFormat: SimpleDateFormat,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Date and Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Event,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = dateFormat.format(Date(ride.date)),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = ride.time,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = ride.vehicleType,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            HorizontalDivider()

            // Locations
            Row {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            Icons.Default.TripOrigin,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "From",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = ride.pickupAddress,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "To",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = ride.dropAddress,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            HorizontalDivider()

            // Fare and Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Estimated Fare",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "₹${String.format("%.0f", ride.estimatedFare)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Row {
                    OutlinedButton(
                        onClick = onCancel,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cancel")
                    }
                }
            }
        }
    }
}
