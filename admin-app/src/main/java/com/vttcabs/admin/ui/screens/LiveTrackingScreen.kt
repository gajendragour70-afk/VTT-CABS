package com.vttcabs.admin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vttcabs.admin.data.model.Booking
import com.vttcabs.admin.data.model.Driver
import com.vttcabs.admin.ui.theme.*
import com.vttcabs.admin.ui.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveTrackingScreen(
    viewModel: AdminViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val drivers by viewModel.drivers.collectAsState()
    val bookings by viewModel.bookings.collectAsState()
    
    val onlineDrivers = drivers.filter { it.isOnline && it.approvalStatus == "APPROVED" }
    val activeBookings = bookings.filter { 
        it.bookingStatus in listOf("ASSIGNED", "ACCEPTED", "DRIVER_ARRIVING", "ARRIVED", "IN_PROGRESS") 
    }
    
    var selectedTab by remember { mutableStateOf(0) }
    
    LaunchedEffect(Unit) {
        viewModel.loadDrivers()
        viewModel.loadBookings()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Live Tracking", fontWeight = FontWeight.Bold) },
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
                    IconButton(onClick = { 
                        viewModel.loadDrivers()
                        viewModel.loadBookings()
                    }) {
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
            // Stats
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LiveStatCard(
                    modifier = Modifier.weight(1f),
                    title = "Online Drivers",
                    value = "${onlineDrivers.size}",
                    color = VTTSuccess
                )
                LiveStatCard(
                    modifier = Modifier.weight(1f),
                    title = "Active Trips",
                    value = "${activeBookings.size}",
                    color = VTTWarning
                )
            }
            
            // Tab Row
            TabRow(selectedTabIndex = selectedTab, containerColor = Color.White) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Drivers (${onlineDrivers.size})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Trips (${activeBookings.size})") }
                )
            }
            
            when (selectedTab) {
                0 -> OnlineDriversList(
                    drivers = onlineDrivers,
                    bookings = bookings
                )
                1 -> ActiveTripsList(bookings = activeBookings)
            }
        }
    }
}

@Composable
fun LiveStatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(value, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = color)
                Text(title, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun OnlineDriversList(
    drivers: List<Driver>,
    bookings: List<Booking>
) {
    if (drivers.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.LocationOff, null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
                Text("No drivers online", color = Color.Gray)
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(drivers, key = { it.id }) { driver ->
                val driverBooking = bookings.find { 
                    it.driverId == driver.id && it.bookingStatus in listOf("ASSIGNED", "ACCEPTED", "DRIVER_ARRIVING", "ARRIVED", "IN_PROGRESS") 
                }
                OnlineDriverCard(driver = driver, currentBooking = driverBooking)
            }
        }
    }
}

@Composable
fun OnlineDriverCard(
    driver: Driver,
    currentBooking: Booking?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Live indicator
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(VTTSuccess.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = VTTSuccess,
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(driver.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(VTTSuccess)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("LIVE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = VTTSuccess)
                    }
                    Text(
                        "${driver.vehicleCategory} - ${driver.vehicleNumber}",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                    Text("⭐ ${driver.rating}", fontSize = 12.sp)
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text("📍", fontSize = 20.sp)
                    Text(
                        "${String.format("%.4f", driver.currentLat)}, ${String.format("%.4f", driver.currentLng)}",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            }
            
            if (currentBooking != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider()
                Spacer(modifier = Modifier.height(12.dp))
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = VTTBluePrimary.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Current Trip", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            TripStatusChip(status = currentBooking.bookingStatus)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Customer: ${currentBooking.customerName}",
                            fontSize = 12.sp
                        )
                        Text(
                            "From: ${currentBooking.pickupAddress}",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            maxLines = 1
                        )
                        Text(
                            "To: ${currentBooking.dropAddress}",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            maxLines = 1
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "🟢 Available for rides",
                    fontSize = 12.sp,
                    color = VTTSuccess
                )
            }
        }
    }
}

@Composable
fun TripStatusChip(status: String) {
    val (color, text) = when (status) {
        "ASSIGNED" -> VTTBluePrimary to "Assigned"
        "ACCEPTED" -> VTTBluePrimary to "Accepted"
        "DRIVER_ARRIVING" -> VTTWarning to "Arriving"
        "ARRIVED" -> VTTWarning to "Arrived"
        "IN_PROGRESS" -> VTTSuccess to "In Progress"
        else -> Color.Gray to status
    }
    
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            text,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun ActiveTripsList(bookings: List<Booking>) {
    if (bookings.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.LocalTaxi, null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
                Text("No active trips", color = Color.Gray)
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(bookings, key = { it.id }) { booking ->
                ActiveTripCard(booking = booking)
            }
        }
    }
}

@Composable
fun ActiveTripCard(booking: Booking) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("#${booking.id.take(8)}", fontWeight = FontWeight.Bold)
                TripStatusChip(status = booking.bookingStatus)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Customer
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                Spacer(modifier = Modifier.width(8.dp))
                Text(booking.customerName, fontWeight = FontWeight.Medium)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Route
            Row {
                Column {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(VTTSuccess, RoundedCornerShape(5.dp))
                    )
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(20.dp)
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
            
            // Driver
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
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(16.dp), tint = VTTSuccess)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Driver Location: ${booking.pickupLat}, ${booking.pickupLng}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}
