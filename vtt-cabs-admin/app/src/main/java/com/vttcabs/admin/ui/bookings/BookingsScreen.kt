package com.vttcabs.admin.ui.bookings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingsScreen(
    onNavigateBack: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("All", "Active", "Completed", "Cancelled")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bookings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(10) { index ->
                    BookingCard(
                        id = "BK${1000 + index}",
                        customer = "Customer ${index + 1}",
                        driver = if (index % 3 != 0) "Driver ${index + 1}" else "Not Assigned",
                        pickup = listOf("Sector 15", "Cyber Hub", "IFFCO Chowk", "Ambience Mall")[index % 4],
                        drop = listOf("Cyber Hub", "HUDA City Centre", "Rajiv Chowk", "Sector 62")[index % 4],
                        fare = "₹${150 + index * 30}",
                        status = when (index % 5) {
                            0 -> "Completed"
                            1 -> "Active"
                            2 -> "In Progress"
                            3 -> "Cancelled"
                            else -> "Pending"
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun BookingCard(
    id: String,
    customer: String,
    driver: String,
    pickup: String,
    drop: String,
    fare: String,
    status: String
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = id,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = fare,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Customer: $customer",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Driver: $driver",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.TripOrigin,
                    contentDescription = null,
                    tint = androidx.compose.ui.graphics.Color(0xFF22C55E),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = pickup, style = MaterialTheme.typography.bodySmall)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = androidx.compose.ui.graphics.Color(0xFFEF4444),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = drop, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AssistChip(
                    onClick = { },
                    label = { Text(status) }
                )
                IconButton(onClick = { }) {
                    Icon(Icons.Default.Visibility, contentDescription = "View Details")
                }
            }
        }
    }
}
