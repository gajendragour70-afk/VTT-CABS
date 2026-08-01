package com.vttcabs.admin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vttcabs.admin.data.model.DashboardStats
import com.vttcabs.admin.ui.theme.*
import com.vttcabs.admin.ui.viewmodel.AdminViewModel
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: AdminViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val stats by viewModel.dashboardStats.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadDashboardStats()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("VTT CABS Admin", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VTTBluePrimary,
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { viewModel.loadDashboardStats() }) {
                        Icon(Icons.Default.Refresh, "Refresh", tint = Color.White)
                    }
                    IconButton(onClick = { viewModel.logout() }) {
                        Icon(Icons.Default.Logout, "Logout", tint = Color.White)
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = VTTBluePrimary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Loading dashboard...", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color(0xFFF8FAFC)),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Welcome Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = VTTBluePrimary),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Welcome, Admin!",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Here's your business overview",
                                    fontSize = 14.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                            Icon(
                                Icons.Default.Dashboard,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.3f),
                                modifier = Modifier.size(60.dp)
                            )
                        }
                    }
                }
                
                // Stats Grid
                item {
                    Text(
                        "Overview",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            modifier = Modifier.weight(1f),
                            title = "Total Bookings",
                            value = if (stats.totalBookings > 0) stats.totalBookings.toString() else "0",
                            icon = Icons.Default.BookOnline,
                            color = VTTBluePrimary,
                            subtitle = if (stats.totalBookings == 0) "No bookings yet" else null
                        )
                        StatCard(
                            modifier = Modifier.weight(1f),
                            title = "Active Trips",
                            value = if (stats.activeTrips > 0) stats.activeTrips.toString() else "0",
                            icon = Icons.Default.DirectionsCar,
                            color = VTTWarning,
                            subtitle = if (stats.activeTrips == 0) "No active trips" else null
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
                            title = "Completed",
                            value = if (stats.completedTrips > 0) stats.completedTrips.toString() else "0",
                            icon = Icons.Default.CheckCircle,
                            color = VTTSuccess,
                            subtitle = if (stats.completedTrips == 0) "No completed trips" else null
                        )
                        StatCard(
                            modifier = Modifier.weight(1f),
                            title = "Cancelled",
                            value = if (stats.cancelledTrips > 0) stats.cancelledTrips.toString() else "0",
                            icon = Icons.Default.Cancel,
                            color = VTTDanger,
                            subtitle = if (stats.cancelledTrips == 0) "No cancelled trips" else null
                        )
                    }
                }
                
                // Pending Driver Verification Card
                item {
                    PendingDriversCard(
                        pendingCount = 0,
                        onClick = { onNavigate("drivers") }
                    )
                }
                
                // Revenue Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = VTTSuccess),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.AttachMoney,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    "Total Revenue",
                                    fontSize = 14.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Text(
                                    formatCurrency(stats.totalRevenue),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
                
                // Quick Actions
                item {
                    Text(
                        "Quick Actions",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(listOf(
                            Triple("Drivers", Icons.Default.DirectionsCar, "drivers"),
                            Triple("Bookings", Icons.Default.BookOnline, "bookings"),
                            Triple("Customers", Icons.Default.People, "customers"),
                            Triple("Vehicles", Icons.Default.LocalTaxi, "vehicles"),
                            Triple("Reports", Icons.Default.Assessment, "reports")
                        )) { (title, icon, route) ->
                            ActionCard(
                                title = title,
                                icon = icon,
                                onClick = { onNavigate(route) }
                            )
                        }
                    }
                }
                
                // Users Stats
                item {
                    Text(
                        "Users",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            modifier = Modifier.weight(1f),
                            title = "Total Drivers",
                            value = if (stats.totalDrivers > 0) stats.totalDrivers.toString() else "0",
                            icon = Icons.Default.DirectionsCar,
                            color = VTTInfo,
                            subtitle = if (stats.activeDrivers > 0) "${stats.activeDrivers} online" else "No drivers online"
                        )
                        StatCard(
                            modifier = Modifier.weight(1f),
                            title = "Customers",
                            value = if (stats.totalCustomers > 0) stats.totalCustomers.toString() else "0",
                            icon = Icons.Default.People,
                            color = VTTBluePrimary,
                            subtitle = if (stats.totalCustomers == 0) "No customers yet" else null
                        )
                    }
                }
                
                // No Data Message
                if (stats.totalBookings == 0 && stats.totalDrivers == 0 && stats.totalCustomers == 0) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = VTTBlueContainer),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = VTTBlueDark,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    "No Data Available",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = VTTBlueDark
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Once drivers register and customers start booking, you'll see the data here.",
                                    fontSize = 14.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
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
fun PendingDriversCard(
    pendingCount: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = VTTWarning.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.PendingActions,
                contentDescription = null,
                tint = VTTWarning,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Pending Driver Verification",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = VTTBlueDark
                )
                Text(
                    if (pendingCount > 0) "$pendingCount driver(s) awaiting approval" else "No pending verifications",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "View",
                tint = VTTBluePrimary
            )
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    subtitle: String? = null
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(color.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
            Text(
                title,
                fontSize = 13.sp,
                color = Color.Gray
            )
            subtitle?.let {
                Text(
                    it,
                    fontSize = 11.sp,
                    color = VTTSuccess
                )
            }
        }
    }
}

@Composable
fun ActionCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = VTTBluePrimary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    return formatter.format(amount)
}
