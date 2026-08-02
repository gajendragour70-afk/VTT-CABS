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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class VehicleInfo(
    val id: String,
    val vehicleNumber: String,
    val vehicleType: String,
    val vehicleModel: String,
    val driverName: String,
    val driverId: String,
    val isVerified: Boolean,
    val rcExpiry: String,
    val insuranceExpiry: String,
    val pucExpiry: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehiclesScreen(
    vehicles: List<VehicleInfo> = listOf(
        VehicleInfo("1", "KA-01-AB-1234", "Sedan", "Honda City", "Ramesh Kumar", "D1", true, "2025-06-30", "2025-03-31", "2024-12-31"),
        VehicleInfo("2", "KA-02-CD-5678", "SUV", "Toyota Innova", "Suresh Patel", "D2", true, "2025-08-15", "2025-05-20", "2024-11-30"),
        VehicleInfo("3", "KA-03-EF-9012", "Auto", " Bajaj Auto", "Mahesh Singh", "D3", false, "2025-01-15", "2024-09-10", "2024-10-31"),
        VehicleInfo("4", "KA-04-GH-3456", "Hatchback", "Maruti Swift", "Rajesh Gupta", "D4", true, "2025-09-01", "2025-06-15", "2024-12-15")
    ),
    onVehicleClick: (String) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("all") }
    val filteredVehicles = vehicles.filter { vehicle ->
        val matchesSearch = vehicle.vehicleNumber.contains(searchQuery, ignoreCase = true) ||
                vehicle.vehicleModel.contains(searchQuery, ignoreCase = true) ||
                vehicle.driverName.contains(searchQuery, ignoreCase = true)
        
        val matchesFilter = when (selectedFilter) {
            "verified" -> vehicle.isVerified
            "unverified" -> !vehicle.isVerified
            else -> true
        }
        
        matchesSearch && matchesFilter
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vehicles") },
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
            // Search
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search vehicle or driver...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                singleLine = true
            )

            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == "all",
                    onClick = { selectedFilter = "all" },
                    label = { Text("All") }
                )
                FilterChip(
                    selected = selectedFilter == "verified",
                    onClick = { selectedFilter = "verified" },
                    label = { Text("Verified") },
                    leadingIcon = if (selectedFilter == "verified") {
                        { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                    } else null
                )
                FilterChip(
                    selected = selectedFilter == "unverified",
                    onClick = { selectedFilter = "unverified" },
                    label = { Text("Pending") },
                    leadingIcon = if (selectedFilter == "unverified") {
                        { Icon(Icons.Default.Pending, null, Modifier.size(18.dp)) }
                    } else null
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stats
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AdminStatCard(
                    title = "Total",
                    value = vehicles.size.toString(),
                    modifier = Modifier.weight(1f)
                )
                AdminStatCard(
                    title = "Verified",
                    value = vehicles.count { it.isVerified }.toString(),
                    modifier = Modifier.weight(1f)
                )
                AdminStatCard(
                    title = "Pending",
                    value = vehicles.count { !it.isVerified }.toString(),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredVehicles) { vehicle ->
                    VehicleCard(
                        vehicle = vehicle,
                        onClick = { onVehicleClick(vehicle.id) }
                    )
                }

                if (filteredVehicles.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No vehicles found",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VehicleCard(
    vehicle: VehicleInfo,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = vehicle.vehicleNumber,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${vehicle.vehicleType} - ${vehicle.vehicleModel}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = if (vehicle.isVerified)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.errorContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (vehicle.isVerified) Icons.Default.Verified else Icons.Default.Pending,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (vehicle.isVerified) "Verified" else "Pending",
                            fontSize = 12.sp
                        )
                    }
                }
            }

            HorizontalDivider()

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Driver: ${vehicle.driverName}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Document Expiry Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DocumentExpiryChip("RC", vehicle.rcExpiry)
                DocumentExpiryChip("Insurance", vehicle.insuranceExpiry)
                DocumentExpiryChip("PUC", vehicle.pucExpiry)
            }
        }
    }
}

@Composable
private fun DocumentExpiryChip(label: String, expiryDate: String) {
    val isExpiringSoon = try {
        val expiry = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).parse(expiryDate)
        expiry?.before(java.util.Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000)) == true
    } catch (e: Exception) { false }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            if (isExpiringSoon) Icons.Default.Warning else Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = if (isExpiringSoon) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(2.dp))
        Text(
            text = "$label: ${expiryDate.takeLast(5)}",
            fontSize = 10.sp,
            color = if (isExpiringSoon) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AdminStatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            Text(
                text = title,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
