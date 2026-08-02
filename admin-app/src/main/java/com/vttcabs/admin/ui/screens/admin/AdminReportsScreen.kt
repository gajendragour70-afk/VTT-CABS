package com.vttcabs.admin.ui.screens.admin

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vttcabs.common.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminReportsScreen(
    repository: VttRepository,
    onBackClick: () -> Unit
) {
    var stats by remember { mutableStateOf(AdminDashboardStats()) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var recentBookings by remember { mutableStateOf<List<BookingEntity>>(emptyList()) }
    var sosAlerts by remember { mutableStateOf<List<SosAlertEntity>>(emptyList()) }
    
    LaunchedEffect(Unit) {
        stats = repository.getAdminDashboardStats()
        recentBookings = repository.bookingsList.take(20)
        sosAlerts = repository.sosAlertsList
    }
    
    val tabs = listOf("Overview", "Bookings", "SOS Alerts")
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard") },
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
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(title)
                                if (index == 2 && stats.sosAlerts > 0) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Badge { Text("${stats.sosAlerts}") }
                                }
                            }
                        }
                    )
                }
            }
            
            when (selectedTab) {
                0 -> OverviewTab(stats = stats)
                1 -> BookingsTab(bookings = recentBookings)
                2 -> SosAlertsTab(
                    alerts = sosAlerts,
                    onAcknowledge = { alertId ->
                        // Handle acknowledge
                    },
                    onResolve = { alertId ->
                        // Handle resolve
                    }
                )
            }
        }
    }
}

@Composable
fun OverviewTab(stats: AdminDashboardStats) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Revenue Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Total Revenue",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        "₹${String.format("%.2f", stats.totalRevenue)}",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        RevenueStat("Today", stats.todayRevenue)
                        RevenueStat("This Week", stats.weekRevenue)
                        RevenueStat("This Month", stats.monthRevenue)
                    }
                }
            }
        }
        
        // Quick Stats
        item {
            Text(
                "Quick Stats",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
        
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Drivers",
                    value = "${stats.activeDrivers}/${stats.totalDrivers}",
                    subtitle = "Active",
                    icon = Icons.Default.DirectionsCar,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Customers",
                    value = "${stats.totalCustomers}",
                    subtitle = "Registered",
                    icon = Icons.Default.People,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Bookings",
                    value = "${stats.completedTrips}",
                    subtitle = "Completed",
                    icon = Icons.Default.Book,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Cancelled",
                    value = "${stats.cancelledTrips}",
                    subtitle = "Cancelled",
                    icon = Icons.Default.Cancel,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        // Driver Applications
        item {
            Text(
                "Driver Applications",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
        
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    ApplicationRow(
                        status = "Pending",
                        count = stats.pendingDriverApplications,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    ApplicationRow(
                        status = "Approved",
                        count = stats.totalDrivers - stats.suspendedDrivers - stats.rejectedDrivers,
                        color = Color(0xFF4CAF50)
                    )
                    ApplicationRow(
                        status = "Rejected",
                        count = stats.rejectedDrivers,
                        color = MaterialTheme.colorScheme.error
                    )
                    ApplicationRow(
                        status = "Suspended",
                        count = stats.suspendedDrivers,
                        color = Color(0xFFFF9800)
                    )
                }
            }
        }
        
        // Performance Metrics
        item {
            Text(
                "Performance",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
        
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFC107)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Average Rating")
                        }
                        Text(
                            String.format("%.1f", stats.averageRating),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RevenueStat(label: String, value: Double) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "₹${String.format("%.0f", value)}",
            fontWeight = FontWeight.Bold
        )
        Text(
            label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontSize = 14.sp)
                Icon(
                    icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                subtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ApplicationRow(
    status: String,
    count: Int,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(12.dp),
                shape = MaterialTheme.shapes.small,
                color = color
            ) {}
            Spacer(modifier = Modifier.width(12.dp))
            Text(status)
        }
        Text(
            "$count",
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun BookingsTab(bookings: List<BookingEntity>) {
    if (bookings.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Book,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "No bookings yet",
                    fontSize = 18.sp
                )
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(bookings) { booking ->
                BookingReportCard(booking = booking)
            }
        }
    }
}

@Composable
fun BookingReportCard(booking: BookingEntity) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        booking.customerName,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        "${booking.pickupAddress.take(25)} → ${booking.dropAddress.take(25)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "₹${String.format("%.0f", booking.totalFare)}",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    StatusChip(status = booking.bookingStatus)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    booking.vehicleCategory.displayName,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    java.text.SimpleDateFormat("dd MMM, HH:mm", java.util.Locale.getDefault())
                        .format(java.util.Date(booking.createdAt)),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun StatusChip(status: BookingStatus) {
    val color = when (status) {
        BookingStatus.COMPLETED, BookingStatus.TRIP_COMPLETED -> Color(0xFF4CAF50)
        BookingStatus.CANCELLED -> MaterialTheme.colorScheme.error
        BookingStatus.ASSIGNED, BookingStatus.ACCEPTED -> Color(0xFF2196F3)
        BookingStatus.PENDING, BookingStatus.SEARCHING -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.secondary
    }
    
    AssistChip(
        onClick = {},
        label = { 
            Text(
                status.name.replace("_", " "),
                fontSize = 10.sp
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = color.copy(alpha = 0.2f),
            labelColor = color
        ),
        modifier = Modifier.height(24.dp)
    )
}

@Composable
fun SosAlertsTab(
    alerts: List<SosAlertEntity>,
    onAcknowledge: (String) -> Unit,
    onResolve: (String) -> Unit
) {
    val activeAlerts = alerts.filter { it.status == "ACTIVE" }
    
    if (activeAlerts.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color(0xFF4CAF50)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "No active SOS alerts",
                    fontSize = 18.sp
                )
                Text(
                    "All clear!",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(activeAlerts) { alert ->
                SosAlertReportCard(
                    alert = alert,
                    onAcknowledge = { onAcknowledge(alert.id) },
                    onResolve = { onResolve(alert.id) }
                )
            }
        }
    }
}

@Composable
fun SosAlertReportCard(
    alert: SosAlertEntity,
    onAcknowledge: () -> Unit,
    onResolve: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "SOS Alert",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                AssistChip(
                    onClick = {},
                    label = { Text(alert.status) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "${alert.userType.name} (${alert.userId.take(8)}...)",
                    fontSize = 14.sp
                )
            }
            
            Row {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "Location: ${String.format("%.4f", alert.latitude)}, ${String.format("%.4f", alert.longitude)}",
                    fontSize = 14.sp
                )
            }
            
            Row {
                Icon(
                    Icons.Default.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    java.text.SimpleDateFormat("dd MMM, HH:mm:ss", java.util.Locale.getDefault())
                        .format(java.util.Date(alert.triggeredAt)),
                    fontSize = 14.sp
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onAcknowledge,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Acknowledge")
                }
                Button(
                    onClick = onResolve,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Resolve")
                }
            }
        }
    }
}
