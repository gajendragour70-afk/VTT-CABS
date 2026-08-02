package com.vttcabs.admin.ui.screens.admin

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
import androidx.compose.ui.unit.sp
import com.vttcabs.admin.viewmodel.AdminViewModel
import com.vttcabs.common.BookingEntity
import com.vttcabs.common.BookingStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: AdminViewModel, onLogout: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard") },
                actions = {
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Stats Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AdminStatCard(
                        title = "Total Revenue",
                        value = "₹${String.format("%.0f", uiState.totalRevenue)}",
                        icon = Icons.Default.CurrencyRupee,
                        modifier = Modifier.weight(1f)
                    )
                    AdminStatCard(
                        title = "Today's Trips",
                        value = uiState.todayTrips.toString(),
                        icon = Icons.Default.DirectionsCar,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AdminStatCard(
                        title = "Total Drivers",
                        value = uiState.drivers.size.toString(),
                        icon = Icons.Default.People,
                        modifier = Modifier.weight(1f)
                    )
                    AdminStatCard(
                        title = "Pending Bookings",
                        value = uiState.pendingBookings.size.toString(),
                        icon = Icons.Default.Pending,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Pending Bookings Section
            item {
                Text(
                    text = "Pending Bookings (${uiState.pendingBookings.size})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            if (uiState.pendingBookings.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No pending bookings", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                items(uiState.pendingBookings) { booking ->
                    PendingBookingCard(
                        booking = booking,
                        drivers = uiState.drivers,
                        onApprove = { driverId -> viewModel.approveBooking(booking.id, driverId) },
                        onCancel = { viewModel.cancelBooking(booking.id) }
                    )
                }
            }
            
            // Recent Bookings
            item {
                Text(
                    text = "Recent Bookings",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            items(uiState.bookings.take(10)) { booking ->
                AdminBookingCard(booking = booking)
            }
        }
    }
    
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.logout()
                    showLogoutDialog = false
                    onLogout()
                }) {
                    Text("Logout", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun AdminStatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text(text = title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendingBookingCard(
    booking: BookingEntity,
    drivers: List<com.vttcabs.common.DriverEntity>,
    onApprove: (String) -> Unit,
    onCancel: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val availableDrivers = drivers.filter { it.isOnline && it.isApproved }
    
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = booking.customerName, fontWeight = FontWeight.Bold)
                    Text(text = booking.vehicleCategory.displayName, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(text = "₹${String.format("%.0f", booking.totalFare)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.TripOrigin, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = booking.pickupAddress, modifier = Modifier.weight(1f), fontSize = 14.sp)
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = booking.dropAddress, modifier = Modifier.weight(1f), fontSize = 14.sp)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onCancel,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Cancel")
                }
                
                Box(modifier = Modifier.weight(1f)) {
                    Button(onClick = { expanded = true }) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Assign Driver")
                    }
                    
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        if (availableDrivers.isEmpty()) {
                            DropdownMenuItem(text = { Text("No drivers online") }, onClick = { expanded = false })
                        } else {
                            availableDrivers.forEach { driver ->
                                DropdownMenuItem(
                                    text = { Text("${driver.name} - ${driver.vehicleNumber}") },
                                    onClick = {
                                        onApprove(driver.id)
                                        expanded = false
                                    }
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
fun AdminBookingCard(booking: BookingEntity) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = booking.customerName, fontWeight = FontWeight.Medium)
                Text(text = "${booking.pickupAddress} → ${booking.dropAddress}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "₹${String.format("%.0f", booking.totalFare)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                AdminBookingStatusChip(status = booking.bookingStatus)
            }
        }
    }
}

@Composable
fun AdminBookingStatusChip(status: BookingStatus) {
    val (color, text) = when (status) {
        BookingStatus.PENDING, BookingStatus.SEARCHING -> Pair(MaterialTheme.colorScheme.tertiary, "Pending")
        BookingStatus.ASSIGNED, BookingStatus.ACCEPTED -> Pair(MaterialTheme.colorScheme.primary, "Assigned")
        BookingStatus.DRIVER_ARRIVING, BookingStatus.ARRIVED -> Pair(MaterialTheme.colorScheme.secondary, "Arriving")
        BookingStatus.TRIP_STARTED, BookingStatus.IN_PROGRESS -> Pair(MaterialTheme.colorScheme.primary, "In Progress")
        BookingStatus.TRIP_COMPLETED, BookingStatus.COMPLETED -> Pair(MaterialTheme.colorScheme.tertiary, "Completed")
        BookingStatus.CANCELLED -> Pair(MaterialTheme.colorScheme.error, "Cancelled")
    }
    
    Surface(color = color.copy(alpha = 0.1f), shape = MaterialTheme.shapes.small) {
        Text(text = text, color = color, fontSize = 10.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
    }
}
