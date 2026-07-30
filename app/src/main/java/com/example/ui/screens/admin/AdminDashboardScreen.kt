package com.example.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BookingEntity
import com.example.data.model.BookingStatus
import com.example.data.model.DriverApprovalStatus
import com.example.data.model.DriverEntity
import com.example.ui.theme.VTTBlueContainer
import com.example.ui.theme.VTTBlueDark
import com.example.ui.theme.VTTBluePrimary
import com.example.ui.theme.VTTDanger
import com.example.ui.theme.VTTSuccess
import com.example.ui.viewmodel.VttCabViewModel

@Composable
fun AdminDashboardScreen(
    viewModel: VttCabViewModel,
    modifier: Modifier = Modifier
) {
    val allBookings by viewModel.allBookings.collectAsState()
    val allDrivers by viewModel.allDrivers.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0: Live Bookings, 1: Driver Approvals & Fleet
    var driverFilterStatus by remember { mutableStateOf<DriverApprovalStatus?>(null) } // null = All
    var driverSearchQuery by remember { mutableStateOf("") }

    var selectedBookingForAssign by remember { mutableStateOf<BookingEntity?>(null) }
    var driverToReject by remember { mutableStateOf<DriverEntity?>(null) }
    var rejectionReasonInput by remember { mutableStateOf("") }
    var selectedDriverForDocs by remember { mutableStateOf<DriverEntity?>(null) }
    var previewDocTitle by remember { mutableStateOf("") }
    var previewDocUrl by remember { mutableStateOf("") }
    var zoomScale by remember { mutableStateOf(1f) }

    var driverForReuploadModal by remember { mutableStateOf<DriverEntity?>(null) }
    var selectedDocsToRequest by remember { mutableStateOf<Set<String>>(emptySet()) }

    val totalRevenue = allBookings.filter { it.bookingStatus == BookingStatus.COMPLETED }.sumOf { it.totalFare }
    val activeTripsCount = allBookings.count { it.bookingStatus == BookingStatus.IN_PROGRESS || it.bookingStatus == BookingStatus.ASSIGNED }
    val onlineDriversCount = allDrivers.count { it.isOnline && it.approvalStatus == DriverApprovalStatus.APPROVED }
    val pendingDriversCount = allDrivers.count { it.approvalStatus == DriverApprovalStatus.PENDING }

    val filteredDrivers = allDrivers.filter { drv ->
        val matchesStatus = driverFilterStatus == null || drv.approvalStatus == driverFilterStatus
        val query = driverSearchQuery.trim().lowercase()
        val matchesSearch = query.isEmpty() ||
                drv.name.lowercase().contains(query) ||
                drv.phone.lowercase().contains(query) ||
                drv.email.lowercase().contains(query) ||
                drv.vehicleNumber.lowercase().contains(query) ||
                drv.licenceNumber.lowercase().contains(query)
        matchesStatus && matchesSearch
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Executive Metrics Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            colors = CardDefaults.cardColors(containerColor = VTTBlueDark),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color.White)
                    Text(" VTT Fleet & Dispatch Operations", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    AdminMetricTile("Revenue", "₹${totalRevenue.toInt()}", modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(6.dp))
                    AdminMetricTile("Active Trips", "$activeTripsCount", modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(6.dp))
                    AdminMetricTile("Online Drivers", "$onlineDriversCount", modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(6.dp))
                    AdminMetricTile("Pending Approvals", "$pendingDriversCount", highlight = pendingDriversCount > 0, modifier = Modifier.weight(1f))
                }
            }
        }

        // Main Navigation Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = VTTBluePrimary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Live Bookings (${allBookings.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Drivers & Approvals (${allDrivers.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        if (pendingDriversCount > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFD97706)
                            ) {
                                Text(
                                    "$pendingDriversCount",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            )
        }

        if (selectedTab == 0) {
            // Live Bookings Monitor
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(allBookings) { booking ->
                    AdminBookingCard(
                        booking = booking,
                        onAssignDriverClick = { selectedBookingForAssign = booking }
                    )
                }
            }
        } else {
            // Driver Fleet & Verification Panel
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                // Search Field
                OutlinedTextField(
                    value = driverSearchQuery,
                    onValueChange = { driverSearchQuery = it },
                    placeholder = { Text("Search driver name, phone, vehicle plate, DL...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (driverSearchQuery.isNotEmpty()) {
                            IconButton(onClick = { driverSearchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = null)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Filter Chips Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = driverFilterStatus == null,
                            onClick = { driverFilterStatus = null },
                            label = { Text("All (${allDrivers.size})", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = VTTBluePrimary, selectedLabelColor = Color.White)
                        )
                    }
                    item {
                        FilterChip(
                            selected = driverFilterStatus == DriverApprovalStatus.PENDING,
                            onClick = { driverFilterStatus = DriverApprovalStatus.PENDING },
                            label = { Text("Pending ($pendingDriversCount)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFD97706), selectedLabelColor = Color.White)
                        )
                    }
                    item {
                        FilterChip(
                            selected = driverFilterStatus == DriverApprovalStatus.APPROVED,
                            onClick = { driverFilterStatus = DriverApprovalStatus.APPROVED },
                            label = { Text("Approved (${allDrivers.count { it.approvalStatus == DriverApprovalStatus.APPROVED }})", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = VTTSuccess, selectedLabelColor = Color.White)
                        )
                    }
                    item {
                        FilterChip(
                            selected = driverFilterStatus == DriverApprovalStatus.REJECTED,
                            onClick = { driverFilterStatus = DriverApprovalStatus.REJECTED },
                            label = { Text("Rejected (${allDrivers.count { it.approvalStatus == DriverApprovalStatus.REJECTED }})", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = VTTDanger, selectedLabelColor = Color.White)
                        )
                    }
                    item {
                        FilterChip(
                            selected = driverFilterStatus == DriverApprovalStatus.SUSPENDED,
                            onClick = { driverFilterStatus = DriverApprovalStatus.SUSPENDED },
                            label = { Text("Suspended (${allDrivers.count { it.approvalStatus == DriverApprovalStatus.SUSPENDED }})", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color.Gray, selectedLabelColor = Color.White)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (filteredDrivers.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No drivers match your search filter.", color = Color.Gray, fontSize = 13.sp)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(filteredDrivers) { drv ->
                            AdminDriverVerificationCard(
                                driver = drv,
                                onApprove = {
                                    viewModel.adminUpdateDriverStatus(drv.id, DriverApprovalStatus.APPROVED)
                                },
                                onReject = {
                                    driverToReject = drv
                                },
                                onSuspend = {
                                    viewModel.adminUpdateDriverStatus(drv.id, DriverApprovalStatus.SUSPENDED)
                                },
                                onRequestReupload = {
                                    driverForReuploadModal = drv
                                    selectedDocsToRequest = emptySet()
                                },
                                onViewDoc = { title, url ->
                                    selectedDriverForDocs = drv
                                    previewDocTitle = title
                                    previewDocUrl = url
                                    zoomScale = 1.0f
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal: Admin Driver Rejection Reason Input
    if (driverToReject != null) {
        val drv = driverToReject!!
        AlertDialog(
            onDismissRequest = { driverToReject = null },
            title = { Text("Reject Driver Application", fontWeight = FontWeight.Bold, color = VTTDanger) },
            text = {
                Column {
                    Text("Specify reason for rejecting ${drv.name}'s registration application:", fontSize = 12.sp, color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = rejectionReasonInput,
                        onValueChange = { rejectionReasonInput = it },
                        label = { Text("Rejection Reason *") },
                        placeholder = { Text("e.g. Invalid DL or RC document blurred") },
                        singleLine = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.adminUpdateDriverStatus(
                            driverId = drv.id,
                            status = DriverApprovalStatus.REJECTED,
                            reason = rejectionReasonInput.ifBlank { "Document Verification Failed" }
                        )
                        driverToReject = null
                        rejectionReasonInput = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VTTDanger)
                ) {
                    Text("Confirm Rejection")
                }
            },
            dismissButton = {
                TextButton(onClick = { driverToReject = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Modal: Request Document Re-upload
    if (driverForReuploadModal != null) {
        val drv = driverForReuploadModal!!
        val docOptions = listOf(
            "Aadhaar Front", "Aadhaar Back", "PAN Card",
            "Driving Licence Front", "Driving Licence Back",
            "Vehicle RC", "Vehicle Insurance", "Vehicle PUC", "Vehicle Permit",
            "Driver Profile Photo", "Vehicle Front Photo", "Vehicle Back Photo",
            "Vehicle Left Side Photo", "Vehicle Right Side Photo", "Vehicle Interior Photo", "Vehicle Condition Photos"
        )
        AlertDialog(
            onDismissRequest = { driverForReuploadModal = null },
            title = { Text("Request Document Re-upload", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = VTTBlueDark) },
            text = {
                Column(modifier = Modifier.fillMaxWidth().height(320.dp).verticalScroll(rememberScrollState())) {
                    Text("Select documents that require re-uploading by ${drv.name}:", fontSize = 12.sp, color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(10.dp))

                    docOptions.forEach { docName ->
                        val isChecked = selectedDocsToRequest.contains(docName)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedDocsToRequest = if (isChecked) {
                                        selectedDocsToRequest - docName
                                    } else {
                                        selectedDocsToRequest + docName
                                    }
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            androidx.compose.material3.Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checked ->
                                    selectedDocsToRequest = if (checked) {
                                        selectedDocsToRequest + docName
                                    } else {
                                        selectedDocsToRequest - docName
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(docName, fontSize = 13.sp, fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (selectedDocsToRequest.isNotEmpty()) {
                            viewModel.adminRequestReuploadDocs(drv.id, selectedDocsToRequest.toList())
                        } else {
                            viewModel.showToast("Please select at least one document.")
                        }
                        driverForReuploadModal = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VTTBluePrimary)
                ) {
                    Text("Send Request (${selectedDocsToRequest.size})")
                }
            },
            dismissButton = {
                TextButton(onClick = { driverForReuploadModal = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Modal: Enhanced Interactive Document Viewer & Image Zoom Modal
    if (selectedDriverForDocs != null) {
        val drv = selectedDriverForDocs!!
        AlertDialog(
            onDismissRequest = { selectedDriverForDocs = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Description, contentDescription = null, tint = VTTBluePrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(previewDocTitle, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = VTTBlueContainer
                    ) {
                        Text(
                            text = "${(zoomScale * 100).toInt()}% Zoom",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = VTTBlueDark,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Driver: ${drv.name} • ${drv.vehicleNumber}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.DarkGray)
                    Text("Storage URL: $previewDocUrl", fontSize = 10.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Zoom Controls Bar
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { if (zoomScale > 0.5f) zoomScale -= 0.25f },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Zoom -", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = { zoomScale = 1.0f },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Reset", fontSize = 11.sp)
                        }
                        OutlinedButton(
                            onClick = { if (zoomScale < 3.0f) zoomScale += 0.25f },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Zoom +", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.AssignmentTurnedIn,
                                contentDescription = null,
                                tint = VTTSuccess,
                                modifier = Modifier.size((48 * zoomScale).dp.coerceIn(32.dp, 120.dp))
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("High Resolution Inspection View", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = VTTBlueDark)
                            Text("Aadhaar: ${drv.aadhaarNumber}", fontSize = 11.sp, color = Color.DarkGray)
                            Text("DL No: ${drv.licenceNumber}", fontSize = 11.sp, color = Color.DarkGray)
                            Text("Vehicle: ${drv.vehicleBrand} ${drv.vehicleModel} (${drv.vehicleNumber})", fontSize = 11.sp, color = Color.DarkGray)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Download Document Button
                    Button(
                        onClick = {
                            viewModel.showToast("Downloading $previewDocTitle ($previewDocUrl)... Saved to Downloads.")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = VTTBlueDark),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Download Original Document", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                if (drv.approvalStatus == DriverApprovalStatus.PENDING) {
                    Button(
                        onClick = {
                            viewModel.adminUpdateDriverStatus(drv.id, DriverApprovalStatus.APPROVED)
                            selectedDriverForDocs = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = VTTSuccess)
                    ) {
                        Text("Approve Driver", fontWeight = FontWeight.Bold)
                    }
                } else {
                    TextButton(onClick = { selectedDriverForDocs = null }) {
                        Text("Close Inspection")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedDriverForDocs = null }) {
                    Text("Close")
                }
            }
        )
    }

    // Modal: Admin Manual Dispatch Override
    if (selectedBookingForAssign != null) {
        val booking = selectedBookingForAssign!!
        AlertDialog(
            onDismissRequest = { selectedBookingForAssign = null },
            title = { Text("Manual Driver Dispatch", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Assign available driver for Booking #${booking.id.take(6)}:")
                    Spacer(modifier = Modifier.height(10.dp))
                    allDrivers.filter { it.isOnline && it.approvalStatus == DriverApprovalStatus.APPROVED }.forEach { drv ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    viewModel.adminAssignDriver(booking.id, drv)
                                    selectedBookingForAssign = null
                                },
                            shape = RoundedCornerShape(10.dp),
                            color = VTTBlueContainer.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(drv.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("${drv.vehicleModel} • ${drv.vehicleNumber}", fontSize = 11.sp, color = Color.Gray)
                                }
                                Text("Assign", fontWeight = FontWeight.Bold, color = VTTBluePrimary, fontSize = 12.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedBookingForAssign = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun AdminMetricTile(
    title: String,
    value: String,
    highlight: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = if (highlight) Color(0xFFD97706) else Color.White.copy(alpha = 0.15f)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontSize = 9.sp, color = Color.White.copy(alpha = 0.9f))
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
        }
    }
}

@Composable
fun AdminBookingCard(
    booking: BookingEntity,
    onAssignDriverClick: () -> Unit
) {
    val statusColor = when (booking.bookingStatus) {
        BookingStatus.COMPLETED -> VTTSuccess
        BookingStatus.CANCELLED -> VTTDanger
        BookingStatus.SEARCHING -> VTTBluePrimary
        else -> VTTBlueDark
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("#${booking.id.take(6).uppercase()} • ${booking.customerName}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        booking.bookingStatus.name,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text("Pickup: ${booking.pickupAddress}", fontSize = 11.sp, color = Color.DarkGray, maxLines = 1, modifier = Modifier.padding(top = 4.dp))
            Text("Drop: ${booking.dropAddress}", fontSize = 11.sp, color = Color.DarkGray, maxLines = 1)
            if (booking.pickupDate.isNotBlank() || booking.pickupTime.isNotBlank()) {
                Text("Date/Time: ${booking.pickupDate} • ${booking.pickupTime}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = VTTBlueDark)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Fare: ₹${booking.totalFare}", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = VTTBluePrimary)
                Text("Driver: ${booking.driverName ?: "Unassigned"}", fontSize = 12.sp, color = Color.Gray)

                if (booking.bookingStatus == BookingStatus.PENDING || booking.bookingStatus == BookingStatus.SEARCHING) {
                    OutlinedButton(
                        onClick = onAssignDriverClick,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Assign Driver", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminDriverVerificationCard(
    driver: DriverEntity,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onSuspend: () -> Unit,
    onRequestReupload: () -> Unit,
    onViewDoc: (String, String) -> Unit
) {
    val statusColor = when (driver.approvalStatus) {
        DriverApprovalStatus.APPROVED -> VTTSuccess
        DriverApprovalStatus.PENDING -> Color(0xFFD97706)
        DriverApprovalStatus.REJECTED -> VTTDanger
        DriverApprovalStatus.SUSPENDED -> Color.Gray
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Avatar, Name, Category & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(statusColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = statusColor)
                    }

                    Column(modifier = Modifier.padding(start = 10.dp)) {
                        Text(driver.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("${driver.vehicleBrand} ${driver.vehicleModel} • ${driver.vehicleColor} (${driver.seatingCapacity} Seater)", fontSize = 11.sp, color = Color.DarkGray)
                        Text(driver.vehicleNumber, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = VTTBluePrimary)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = when (driver.approvalStatus) {
                            DriverApprovalStatus.PENDING -> "PENDING APPROVAL"
                            DriverApprovalStatus.APPROVED -> "ACTIVE / APPROVED"
                            DriverApprovalStatus.REJECTED -> "REJECTED"
                            DriverApprovalStatus.SUSPENDED -> "SUSPENDED"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = Color(0xFFE2E8F0))
            Spacer(modifier = Modifier.height(8.dp))

            // Details Grid: Phone, Email, Address, DOB, Emergency Contact, DL, Aadhaar, PAN
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Phone: ${driver.phone}", fontSize = 11.sp, color = Color.DarkGray)
                    Text("Email: ${driver.email}", fontSize = 11.sp, color = Color.DarkGray)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("DOB: ${driver.dob.ifBlank { "N/A" }}", fontSize = 11.sp, color = Color.Gray)
                    Text("Emergency Contact: ${driver.emergencyContact.ifBlank { "N/A" }}", fontSize = 11.sp, color = Color.Gray)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Address: ${driver.address}", fontSize = 11.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("DL No: ${driver.licenceNumber}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = VTTBlueDark)
                    Text("Aadhaar: ${driver.aadhaarNumber}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = VTTBlueDark)
                    Text("PAN: ${driver.panCardNumber}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = VTTBlueDark)
                }
            }

            if (driver.rejectionReason.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = VTTDanger.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Rejection Reason: ${driver.rejectionReason}",
                        fontSize = 11.sp,
                        color = VTTDanger,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            if (driver.reuploadRequestedDocs.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFEF3C7),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Re-upload Requested: ${driver.reuploadRequestedDocs}",
                        fontSize = 11.sp,
                        color = Color(0xFF92400E),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text("Mandatory Uploaded Documents & Photos (Tap to Zoom / Download):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = VTTBlueDark)
            Spacer(modifier = Modifier.height(6.dp))

            // Uploaded Document Buttons (Grid of 14 Document & Photo tiles)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedButton(onClick = { onViewDoc("Profile Photo", driver.profilePhotoUrl) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(6.dp)) { Text("Photo", fontSize = 9.sp) }
                    OutlinedButton(onClick = { onViewDoc("Aadhaar Front", driver.aadhaarFrontUrl) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(6.dp)) { Text("Aadhaar F", fontSize = 9.sp) }
                    OutlinedButton(onClick = { onViewDoc("Aadhaar Back", driver.aadhaarBackUrl) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(6.dp)) { Text("Aadhaar B", fontSize = 9.sp) }
                    OutlinedButton(onClick = { onViewDoc("PAN Card", driver.panCardUrl) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(6.dp)) { Text("PAN", fontSize = 9.sp) }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedButton(onClick = { onViewDoc("DL Front", driver.licenceFrontUrl) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(6.dp)) { Text("DL Front", fontSize = 9.sp) }
                    OutlinedButton(onClick = { onViewDoc("DL Back", driver.licenceBackUrl) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(6.dp)) { Text("DL Back", fontSize = 9.sp) }
                    OutlinedButton(onClick = { onViewDoc("Vehicle RC", driver.rcDocUrl) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(6.dp)) { Text("RC Doc", fontSize = 9.sp) }
                    OutlinedButton(onClick = { onViewDoc("Insurance", driver.insuranceUrl) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(6.dp)) { Text("Insurance", fontSize = 9.sp) }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedButton(onClick = { onViewDoc("PUC Certificate", driver.pucUrl) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(6.dp)) { Text("PUC Cert", fontSize = 9.sp) }
                    OutlinedButton(onClick = { onViewDoc("Vehicle Permit", driver.permitUrl) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(6.dp)) { Text("Permit", fontSize = 9.sp) }
                    OutlinedButton(onClick = { onViewDoc("Veh Front Photo", driver.vehicleFrontPhotoUrl) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(6.dp)) { Text("Veh Front", fontSize = 9.sp) }
                    OutlinedButton(onClick = { onViewDoc("Veh Back Photo", driver.vehicleBackPhotoUrl) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(6.dp)) { Text("Veh Back", fontSize = 9.sp) }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedButton(onClick = { onViewDoc("Veh Left Photo", driver.vehicleLeftSidePhotoUrl) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(6.dp)) { Text("Veh Left", fontSize = 9.sp) }
                    OutlinedButton(onClick = { onViewDoc("Veh Right Photo", driver.vehicleRightSidePhotoUrl) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(6.dp)) { Text("Veh Right", fontSize = 9.sp) }
                    OutlinedButton(onClick = { onViewDoc("Veh Interior", driver.vehicleInteriorPhotoUrl) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(6.dp)) { Text("Interior", fontSize = 9.sp) }
                    OutlinedButton(onClick = { onViewDoc("Condition (4 Photos)", driver.vehicleConditionPhotosUrl) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(6.dp)) { Text("Condition", fontSize = 9.sp) }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Approval Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onRequestReupload,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = VTTBluePrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Request Re-upload", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                Row {
                    if (driver.approvalStatus == DriverApprovalStatus.PENDING || driver.approvalStatus == DriverApprovalStatus.REJECTED || driver.approvalStatus == DriverApprovalStatus.SUSPENDED) {
                        if (driver.approvalStatus == DriverApprovalStatus.PENDING) {
                            OutlinedButton(
                                onClick = onReject,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = VTTDanger),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.padding(end = 6.dp)
                            ) {
                                Text("Reject", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = onApprove,
                            colors = ButtonDefaults.buttonColors(containerColor = VTTSuccess),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                if (driver.approvalStatus == DriverApprovalStatus.PENDING) "Approve Driver" else "Reactivate Driver",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else if (driver.approvalStatus == DriverApprovalStatus.APPROVED) {
                        OutlinedButton(
                            onClick = onSuspend,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD97706)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Suspend Driver", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
