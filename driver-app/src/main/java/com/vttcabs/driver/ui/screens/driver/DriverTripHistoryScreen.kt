package com.vttcabs.driver.ui.screens.driver

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

data class TripHistoryEntry(
    val id: String,
    val date: Long,
    val pickupAddress: String,
    val dropAddress: String,
    val customerName: String,
    val fare: Double,
    val earning: Double,
    val status: String // "completed", "cancelled"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverTripHistoryScreen(
    trips: List<TripHistoryEntry> = listOf(
        TripHistoryEntry(
            "1",
            System.currentTimeMillis(),
            "123 MG Road",
            "456 Indiranagar",
            "John D.",
            350.0,
            280.0,
            "completed"
        ),
        TripHistoryEntry(
            "2",
            System.currentTimeMillis() - 3600000,
            "789 Koramangala",
            "101 Jayanagar",
            "Jane S.",
            280.0,
            224.0,
            "completed"
        ),
        TripHistoryEntry(
            "3",
            System.currentTimeMillis() - 7200000,
            "Home",
            "Airport",
            "Bob M.",
            1200.0,
            960.0,
            "completed"
        ),
        TripHistoryEntry(
            "4",
            System.currentTimeMillis() - 86400000,
            "MG Road",
            "Whitefield",
            "Alice K.",
            450.0,
            0.0, // Cancelled - no earning
            "cancelled"
        )
    ),
    onTripClick: (String) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    // Group trips by date
    val groupedTrips = trips.groupBy { dateFormat.format(Date(it.date)) }

    var selectedTab by remember { mutableStateOf(0) }
    val filteredTrips = if (selectedTab == 0) trips else trips.filter { it.status == "cancelled" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trip History") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Stats
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${trips.count { it.status == "completed" }}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                        Text(
                            text = "Total Trips",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "₹${trips.filter { it.status == "completed" }.sumOf { it.earning }.toInt()}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Total Earnings",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Tab Row
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("All") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Cancelled") }
                )
            }

            // Trips List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredTrips) { trip ->
                    TripHistoryCard(
                        trip = trip,
                        timeFormat = timeFormat,
                        onClick = { onTripClick(trip.id) }
                    )
                }

                if (filteredTrips.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.History,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No trips found",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TripHistoryCard(
    trip: TripHistoryEntry,
    timeFormat: SimpleDateFormat,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (trip.status == "cancelled")
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = timeFormat.format(Date(trip.date)),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = if (trip.status == "completed")
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = if (trip.status == "completed") "Completed" else "Cancelled",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

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
                        Text(
                            text = trip.pickupAddress,
                            fontSize = 14.sp,
                            maxLines = 1
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = trip.dropAddress,
                            fontSize = 14.sp,
                            maxLines = 1
                        )
                    }
                }
            }

            HorizontalDivider()

            // Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = trip.customerName,
                        fontSize = 14.sp
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    if (trip.status == "completed") {
                        Text(
                            text = "₹${trip.earning.toInt()}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Earning",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "No Earning",
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
