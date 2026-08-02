package com.vttcabs.admin.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToDrivers: () -> Unit,
    onNavigateToBookings: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("VTT Admin") },
                actions = {
                    IconButton(onClick = onNavigateToAnalytics) {
                        Icon(Icons.Default.Analytics, contentDescription = "Analytics")
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                    label = { Text("Dashboard") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { 
                        selectedTab = 1
                        onNavigateToDrivers()
                    },
                    icon = { Icon(Icons.Default.DirectionsCar, contentDescription = null) },
                    label = { Text("Drivers") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { 
                        selectedTab = 2
                        onNavigateToBookings()
                    },
                    icon = { Icon(Icons.Default.Receipt, contentDescription = null) },
                    label = { Text("Bookings") }
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Stats Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.DirectionsCar,
                        value = "45",
                        label = "Active Drivers",
                        color = Color(0xFF22C55E)
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.People,
                        value = "120",
                        label = "Total Users",
                        color = Color(0xFF3B82F6)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Receipt,
                        value = "78",
                        label = "Today's Rides",
                        color = Color(0xFFF59E0B)
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.CurrencyRupee,
                        value = "₹15,250",
                        label = "Today's Revenue",
                        color = Color(0xFF8B5CF6)
                    )
                }
            }

            // Pending Approvals
            item {
                Text(
                    text = "Pending Driver Approvals",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(3) { index ->
                PendingDriverCard(
                    name = listOf("Rajesh Kumar", "Amit Singh", "Priya Sharma")[index],
                    vehicle = listOf("Maruti Swift", "Hyundai Creta", "Honda City")[index],
                    status = listOf("SUBMITTED", "PENDING", "DOCUMENTS")[index]
                )
            }

            // Recent Bookings
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Recent Bookings",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(5) { index ->
                RecentBookingCard(
                    customer = "Customer ${index + 1}",
                    driver = "Driver ${index + 1}",
                    pickup = "Sector 15",
                    drop = "Cyber Hub",
                    fare = "₹${150 + index * 20}",
                    status = if (index % 3 == 0) "Completed" else "In Progress"
                )
            }
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Card(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun PendingDriverCard(
    name: String,
    vehicle: String,
    status: String
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name.first().toString(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = vehicle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AssistChip(
                onClick = { },
                label = { Text(status) }
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = { }) {
                Icon(Icons.Default.Check, contentDescription = "Approve", tint = Color(0xFF22C55E))
            }
            IconButton(onClick = { }) {
                Icon(Icons.Default.Close, contentDescription = "Reject", tint = Color(0xFFEF4444))
            }
        }
    }
}

@Composable
fun RecentBookingCard(
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
                    text = customer,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = fare,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "Driver: $driver",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$pickup → $drop",
                    style = MaterialTheme.typography.bodySmall
                )
                AssistChip(
                    onClick = { },
                    label = { Text(status, style = MaterialTheme.typography.labelSmall) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (status == "Completed")
                            Color(0xFFDCFCE7) else Color(0xFFFEF3C7)
                    )
                )
            }
        }
    }
}
