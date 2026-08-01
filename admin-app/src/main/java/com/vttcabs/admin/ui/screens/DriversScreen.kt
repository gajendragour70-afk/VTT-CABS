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
import com.vttcabs.admin.data.model.Driver
import com.vttcabs.admin.ui.theme.*
import com.vttcabs.admin.ui.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriversScreen(
    viewModel: AdminViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val drivers by viewModel.drivers.collectAsState()
    val isLoading by viewModel.isLoadingDrivers.collectAsState()
    val selectedDriver by viewModel.selectedDriver.collectAsState()
    
    var showApprovalDialog by remember { mutableStateOf(false) }
    var showDetailDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        viewModel.loadDrivers()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Driver Management", fontWeight = FontWeight.Bold) },
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
                    IconButton(onClick = { viewModel.loadDrivers() }) {
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
            // Stats Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val pendingCount = drivers.count { it.approvalStatus == "PENDING" }
                val approvedCount = drivers.count { it.approvalStatus == "APPROVED" }
                val suspendedCount = drivers.count { it.approvalStatus in listOf("SUSPENDED", "REJECTED") }
                
                StatChip("Pending", pendingCount, VTTWarning)
                StatChip("Approved", approvedCount, VTTSuccess)
                StatChip("Inactive", suspendedCount, VTTDanger)
            }
            
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (drivers.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.DirectionsCar, null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No drivers found", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(drivers, key = { it.id }) { driver ->
                        DriverCard(
                            driver = driver,
                            onViewDetails = {
                                viewModel.selectDriver(driver)
                                showDetailDialog = true
                            },
                            onApprove = { viewModel.approveDriver(driver.id) },
                            onReject = { viewModel.rejectDriver(driver.id) },
                            onToggleActive = {
                                if (driver.isActive) {
                                    viewModel.suspendDriver(driver.id)
                                } else {
                                    viewModel.activateDriver(driver.id)
                                }
                            }
                        )
                    }
                }
            }
        }
        
        // Driver Details Dialog
        if (showDetailDialog && selectedDriver != null) {
            DriverDetailDialog(
                driver = selectedDriver!!,
                onDismiss = {
                    showDetailDialog = false
                    viewModel.clearSelectedDriver()
                }
            )
        }
    }
}

@Composable
fun StatChip(label: String, count: Int, color: Color) {
    Card(
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "$label: $count",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = color
            )
        }
    }
}

@Composable
fun DriverCard(
    driver: Driver,
    onViewDetails: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onToggleActive: () -> Unit
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
                // Avatar
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(VTTBluePrimary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        driver.name.firstOrNull()?.uppercase() ?: "D",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = VTTBluePrimary
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        driver.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        driver.phone,
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                    Text(
                        "${driver.vehicleCategory} - ${driver.vehicleNumber}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                
                // Status Badge
                StatusBadge(status = driver.approvalStatus, isOnline = driver.isOnline)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DriverStatItem("Rating", "⭐ ${driver.rating}")
                DriverStatItem("Trips", "${driver.totalTrips}")
                DriverStatItem("Earnings", "₹${driver.totalEarnings.toInt()}")
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onViewDetails,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Visibility, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Details", fontSize = 12.sp)
                }
                
                when (driver.approvalStatus) {
                    "PENDING" -> {
                        Button(
                            onClick = onApprove,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = VTTSuccess)
                        ) {
                            Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Approve", fontSize = 12.sp)
                        }
                    }
                    "APPROVED" -> {
                        Button(
                            onClick = onToggleActive,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (driver.isOnline) VTTWarning else VTTSuccess
                            )
                        ) {
                            Icon(
                                if (driver.isOnline) Icons.Default.Block else Icons.Default.Check,
                                null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (driver.isOnline) "Suspend" else "Active", fontSize = 12.sp)
                        }
                    }
                    else -> {
                        Button(
                            onClick = onToggleActive,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = VTTBluePrimary)
                        ) {
                            Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reactivate", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DriverStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(label, fontSize = 11.sp, color = Color.Gray)
    }
}

@Composable
fun StatusBadge(status: String, isOnline: Boolean) {
    val (color, text) = when {
        status == "PENDING" -> VTTWarning to "Pending"
        status == "REJECTED" -> VTTDanger to "Rejected"
        status == "SUSPENDED" -> VTTDanger to "Suspended"
        status == "APPROVED" && isOnline -> VTTSuccess to "Online"
        status == "APPROVED" && !isOnline -> Color.Gray to "Offline"
        else -> Color.Gray to status
    }
    
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (status == "APPROVED" && isOnline) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(text, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = color)
        }
    }
}

@Composable
fun DriverDetailDialog(
    driver: Driver,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Driver Details", fontWeight = FontWeight.Bold)
        },
        text = {
            LazyColumn {
                item {
                    Text("Personal Information", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                    InfoRow("Name", driver.name)
                    InfoRow("Email", driver.email)
                    InfoRow("Phone", driver.phone)
                }
                
                item { Spacer(modifier = Modifier.height(16.dp)) }
                
                item {
                    Text("Vehicle Information", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                    InfoRow("Category", driver.vehicleCategory)
                    InfoRow("Model", driver.vehicleModel)
                    InfoRow("Number", driver.vehicleNumber)
                }
                
                item { Spacer(modifier = Modifier.height(16.dp)) }
                
                item {
                    Text("Documents", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                    InfoRow("Aadhaar", driver.documents.aadhaarNumber)
                    InfoRow("PAN Card", driver.documents.panCardNumber)
                    InfoRow("License", driver.documents.licenceNumber)
                }
                
                item { Spacer(modifier = Modifier.height(16.dp)) }
                
                item {
                    Text("Statistics", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                    InfoRow("Rating", "⭐ ${driver.rating}")
                    InfoRow("Total Trips", "${driver.totalTrips}")
                    InfoRow("Earnings", "₹${driver.totalEarnings.toInt()}")
                    InfoRow("Wallet Balance", "₹${driver.walletBalance.toInt()}")
                }
                
                item { Spacer(modifier = Modifier.height(16.dp)) }
                
                item {
                    Text("Location", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                    InfoRow("Latitude", "${driver.currentLat}")
                    InfoRow("Longitude", "${driver.currentLng}")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = Color.Gray)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}
