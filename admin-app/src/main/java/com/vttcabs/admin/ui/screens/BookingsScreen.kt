package com.vttcabs.admin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vttcabs.admin.data.model.Booking
import com.vttcabs.admin.data.model.Driver
import com.vttcabs.admin.ui.theme.*
import com.vttcabs.admin.ui.viewmodel.AdminViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingsScreen(
    viewModel: AdminViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val bookings by viewModel.bookings.collectAsState()
    val drivers by viewModel.drivers.collectAsState()
    val isLoading by viewModel.isLoadingBookings.collectAsState()
    
    var selectedTab by remember { mutableStateOf(0) }
    var showAssignDialog by remember { mutableStateOf(false) }
    var selectedBooking by remember { mutableStateOf<Booking?>(null) }
    
    val tabs = listOf("All", "Pending", "Active", "Completed", "Cancelled")
    
    val filteredBookings = when (selectedTab) {
        1 -> bookings.filter { it.bookingStatus in listOf("PENDING", "SEARCHING") }
        2 -> bookings.filter { it.bookingStatus in listOf("ASSIGNED", "ACCEPTED", "DRIVER_ARRIVING", "ARRIVED", "IN_PROGRESS") }
        3 -> bookings.filter { it.bookingStatus in listOf("COMPLETED", "TRIP_COMPLETED") }
        4 -> bookings.filter { it.bookingStatus == "CANCELLED" }
        else -> bookings
    }
    
    LaunchedEffect(Unit) {
        viewModel.loadBookings()
        viewModel.loadDrivers()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Booking Management", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VTTBluePrimary,
                    titleContentColor = Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = { onNavigate("dashboard") }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadBookings() }) {
                        Icon(Icons.Default.Refresh, "Refresh", tint = Color.White)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8FAFC))
        ) {
            // Tab Row
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                edgePadding = 16.dp
            ) {
                tabs.forEachIndexed { index, title ->
                    val count = when (index) {
                        1 -> bookings.count { it.bookingStatus in listOf("PENDING", "SEARCHING") }
                        2 -> bookings.count { it.bookingStatus in listOf("ASSIGNED", "ACCEPTED", "DRIVER_ARRIVING", "ARRIVED", "IN_PROGRESS") }
                        3 -> bookings.count { it.bookingStatus in listOf("COMPLETED", "TRIP_COMPLETED") }
                        4 -> bookings.count { it.bookingStatus == "CANCELLED" }
                        else -> bookings.size
                    }
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text("$title ($count)") }
                    )
                }
            }
            
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (filteredBookings.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.BookOnline, null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No bookings found", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredBookings, key = { it.id }) { booking ->
                        BookingCard(
                            booking = booking,
                            onAssign = {
                                selectedBooking = booking
                                showAssignDialog = true
                            },
                            onCancel = { viewModel.cancelBooking(booking.id) },
                            onComplete = { viewModel.completeBooking(booking.id) }
                        )
                    }
                }
            }
        }
        
        // Assign Driver Dialog
        if (showAssignDialog && selectedBooking != null) {
            AssignDriverDialog(
                booking = selectedBooking!!,
                drivers = drivers.filter { it.approvalStatus == "APPROVED" && it.isActive },
                onDismiss = {
                    showAssignDialog = false
                    selectedBooking = null
                },
                onAssign = { driver ->
                    viewModel.assignDriverToBooking(selectedBooking!!.id, driver)
                    showAssignDialog = false
                    selectedBooking = null
                }
            )
        }
    }
}

@Composable
fun BookingCard(
    booking: Booking,
    onAssign: () -> Unit,
    onCancel: () -> Unit,
    onComplete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "#${booking.id.take(8)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                BookingStatusChip(status = booking.bookingStatus)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Customer Info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                Spacer(modifier = Modifier.width(8.dp))
                Text(booking.customerName, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.width(16.dp))
                Text(booking.customerPhone, fontSize = 12.sp, color = Color.Gray)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Route
            Row(verticalAlignment = Alignment.Top) {
                Column {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(VTTSuccess, RoundedCornerShape(5.dp))
                    )
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(24.dp)
                            .background(Color.Gray.copy(alpha = 0.3f))
                    )
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(VTTDanger, RoundedCornerShape(5.dp))
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(booking.pickupAddress, fontSize = 13.sp, maxLines = 1)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(booking.dropAddress, fontSize = 13.sp, maxLines = 1)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Details Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(booking.pickupDate, fontSize = 11.sp, color = Color.Gray)
                    Text(booking.pickupTime, fontSize = 11.sp, color = Color.Gray)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(booking.vehicleCategory, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Text(
                        "₹${booking.totalFare.toInt()}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = VTTBluePrimary
                    )
                }
            }
            
            // Driver Info (if assigned)
            booking.driverName?.let { driverName ->
                Spacer(modifier = Modifier.height(12.dp))
                Divider()
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DirectionsCar, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(driverName, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(booking.vehicleNumber ?: "", fontSize = 12.sp, color = Color.Gray)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (booking.bookingStatus in listOf("PENDING", "SEARCHING") && booking.driverId == null) {
                    Button(
                        onClick = onAssign,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VTTBluePrimary)
                    ) {
                        Icon(Icons.Default.PersonAdd, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Assign Driver", fontSize = 12.sp)
                    }
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VTTDanger)
                    ) {
                        Icon(Icons.Default.Cancel, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cancel", fontSize = 12.sp)
                    }
                } else if (booking.bookingStatus in listOf("ASSIGNED", "ACCEPTED", "DRIVER_ARRIVING", "ARRIVED", "IN_PROGRESS")) {
                    Button(
                        onClick = onComplete,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VTTSuccess)
                    ) {
                        Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Complete", fontSize = 12.sp)
                    }
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VTTDanger)
                    ) {
                        Icon(Icons.Default.Cancel, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cancel", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun BookingStatusChip(status: String) {
    val (color, text) = when (status) {
        "PENDING", "SEARCHING" -> VTTWarning to "Pending"
        "ASSIGNED", "ACCEPTED" -> VTTBluePrimary to "Assigned"
        "DRIVER_ARRIVING", "ARRIVED" -> VTTInfo to "Arriving"
        "IN_PROGRESS" -> VTTWarning to "In Progress"
        "COMPLETED", "TRIP_COMPLETED" -> VTTSuccess to "Completed"
        "CANCELLED" -> VTTDanger to "Cancelled"
        else -> Color.Gray to status
    }
    
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun AssignDriverDialog(
    booking: Booking,
    drivers: List<Driver>,
    onDismiss: () -> Unit,
    onAssign: (Driver) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Assign Driver", fontWeight = FontWeight.Bold) },
        text = {
            if (drivers.isEmpty()) {
                Text("No available drivers found. Please try again later.")
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 400.dp)
                ) {
                    items(drivers) { driver ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(driver.name, fontWeight = FontWeight.Bold)
                                    Text(
                                        "${driver.vehicleCategory} - ${driver.vehicleNumber}",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                    Text("⭐ ${driver.rating}", fontSize = 11.sp)
                                }
                                Button(
                                    onClick = { onAssign(driver) },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Select")
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
