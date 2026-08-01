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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vttcabs.admin.data.model.Booking
import com.vttcabs.admin.ui.theme.*
import com.vttcabs.admin.ui.viewmodel.AdminViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: AdminViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val bookings by viewModel.bookings.collectAsState()
    val drivers by viewModel.drivers.collectAsState()
    
    var selectedTab by remember { mutableStateOf(0) }
    var showNotificationDialog by remember { mutableStateOf(false) }
    
    val tabs = listOf("Daily", "Weekly", "Monthly", "Broadcast")
    
    // Calculate stats based on time period
    val now = System.currentTimeMillis()
    val dayStart = getStartOfDay(now)
    val weekStart = getStartOfWeek(now)
    val monthStart = getStartOfMonth(now)
    
    val (periodBookings, periodRevenue) = when (selectedTab) {
        0 -> {
            val filtered = bookings.filter { it.createdAt >= dayStart }
            filtered to filtered.filter { it.paymentStatus == "COMPLETED" }.sumOf { it.totalFare }
        }
        1 -> {
            val filtered = bookings.filter { it.createdAt >= weekStart }
            filtered to filtered.filter { it.paymentStatus == "COMPLETED" }.sumOf { it.totalFare }
        }
        2 -> {
            val filtered = bookings.filter { it.createdAt >= monthStart }
            filtered to filtered.filter { it.paymentStatus == "COMPLETED" }.sumOf { it.totalFare }
        }
        else -> bookings to bookings.filter { it.paymentStatus == "COMPLETED" }.sumOf { it.totalFare }
    }
    
    LaunchedEffect(Unit) {
        viewModel.loadBookings()
        viewModel.loadDrivers()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reports & Analytics", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VTTBluePrimary,
                    titleContentColor = Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = { onNavigate("dashboard") }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
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
            TabRow(selectedTabIndex = selectedTab, containerColor = Color.White) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }
            
            if (selectedTab < 3) {
                // Stats Overview
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Revenue Card
                    item {
                        RevenueCard(
                            period = tabs[selectedTab],
                            revenue = periodRevenue,
                            bookings = periodBookings.size
                        )
                    }
                    
                    // Stats Grid
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ReportStatCard(
                                modifier = Modifier.weight(1f),
                                title = "Total Bookings",
                                value = "${periodBookings.size}",
                                icon = Icons.Default.BookOnline,
                                color = VTTBluePrimary
                            )
                            ReportStatCard(
                                modifier = Modifier.weight(1f),
                                title = "Completed",
                                value = "${periodBookings.count { it.bookingStatus in listOf("COMPLETED", "TRIP_COMPLETED") }}",
                                icon = Icons.Default.CheckCircle,
                                color = VTTSuccess
                            )
                        }
                    }
                    
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ReportStatCard(
                                modifier = Modifier.weight(1f),
                                title = "Cancelled",
                                value = "${periodBookings.count { it.bookingStatus == "CANCELLED" }}",
                                icon = Icons.Default.Cancel,
                                color = VTTDanger
                            )
                            ReportStatCard(
                                modifier = Modifier.weight(1f),
                                title = "Active Drivers",
                                value = "${drivers.count { it.isOnline }}",
                                icon = Icons.Default.DirectionsCar,
                                color = VTTWarning
                            )
                        }
                    }
                    
                    // Export Section
                    item {
                        Text(
                            "Export Report",
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
                            ExportButton(
                                modifier = Modifier.weight(1f),
                                title = "PDF Report",
                                icon = Icons.Default.PictureAsPdf,
                                color = VTTDanger,
                                onClick = { /* PDF export */ }
                            )
                            ExportButton(
                                modifier = Modifier.weight(1f),
                                title = "Excel",
                                icon = Icons.Default.TableChart,
                                color = VTTSuccess,
                                onClick = { /* Excel export */ }
                            )
                        }
                    }
                    
                    // Recent Bookings
                    item {
                        Text(
                            "Recent Bookings",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    
                    items(periodBookings.take(10)) { booking ->
                        ReportBookingCard(booking = booking)
                    }
                }
            } else {
                // Broadcast Notifications
                BroadcastNotificationsPanel(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun RevenueCard(
    period: String,
    revenue: Double,
    bookings: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = VTTSuccess),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                "$period Revenue",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
            Text(
                formatCurrency(revenue),
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.BookOnline,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "$bookings total bookings",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun ReportStatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 24.sp)
            Text(title, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun ExportButton(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(60.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(icon, null, tint = color)
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, color = color)
    }
}

@Composable
fun ReportBookingCard(booking: Booking) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "#${booking.id.take(8)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    booking.customerName,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    formatDateTime(booking.createdAt),
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "₹${booking.totalFare.toInt()}",
                    fontWeight = FontWeight.Bold,
                    color = VTTBluePrimary
                )
                StatusBadge(status = booking.bookingStatus)
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val color = when (status) {
        "COMPLETED", "TRIP_COMPLETED" -> VTTSuccess
        "CANCELLED" -> VTTDanger
        else -> VTTWarning
    }
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            status.replace("_", " "),
            fontSize = 10.sp,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun BroadcastNotificationsPanel(viewModel: AdminViewModel) {
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    
    val context = LocalContext.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Send Broadcast Notification",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "This notification will be sent to all drivers and customers.",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    placeholder = { Text("e.g., New App Update") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Message") },
                    placeholder = { Text("Enter notification message...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 5
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = {
                        if (title.isNotBlank() && message.isNotBlank()) {
                            viewModel.sendBroadcastNotification(title, message)
                            title = ""
                            message = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = title.isNotBlank() && message.isNotBlank()
                ) {
                    Icon(Icons.Default.Send, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Send Broadcast")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            "Quick Notifications",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        val quickNotifications = listOf(
            Pair("App Update", "VTT CABS app has been updated. Please update to the latest version."),
            Pair("Surge Pricing", "Surge pricing is active in your area. Check fare before booking."),
            Pair("Holiday Offer", "Flat 20% off on all rides this holiday season! Use code: HOLIDAY20"),
            Pair("Maintenance", "Scheduled maintenance tonight from 2 AM to 4 AM. Service may be affected.")
        )
        
        quickNotifications.forEach { (t, m) ->
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(t, fontWeight = FontWeight.Medium)
                        Text(m, fontSize = 12.sp, color = Color.Gray, maxLines = 2)
                    }
                    IconButton(
                        onClick = {
                            viewModel.sendBroadcastNotification(t, m)
                        }
                    ) {
                        Icon(Icons.Default.Send, null, tint = VTTBluePrimary)
                    }
                }
            }
        }
    }
}

private fun getStartOfDay(timestamp: Long): Long {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = timestamp
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}

private fun getStartOfWeek(timestamp: Long): Long {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = timestamp
    calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}

private fun getStartOfMonth(timestamp: Long): Long {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = timestamp
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}

private fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    return formatter.format(amount)
}

private fun formatDateTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
