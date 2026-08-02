package com.vttcabs.customer.ui.screens.customer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vttcabs.common.*
import com.vttcabs.customer.viewmodel.CustomerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    viewModel: CustomerViewModel,
    onBookingCreated: (String) -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showPickupDialog by remember { mutableStateOf(false) }
    var showDropDialog by remember { mutableStateOf(false) }
    var showVehicleDialog by remember { mutableStateOf(false) }
    var showTypeDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(uiState.currentBooking) {
        if (uiState.currentBooking != null) {
            onBookingCreated(uiState.currentBooking!!.id)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Booking") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Pickup Location
            Card(
                onClick = { showPickupDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.TripOrigin,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Pickup Location",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = uiState.pickupLocation?.name ?: "Select pickup location",
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Drop Location
            Card(
                onClick = { showDropDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Drop Location",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = uiState.dropLocation?.name ?: "Select drop location",
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Booking Type
            Text(
                text = "Booking Type",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Card(
                onClick = { showTypeDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.EventNote, contentDescription = null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = uiState.selectedBookingType.name.replace("_", " "),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Vehicle Category
            Text(
                text = "Select Vehicle",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Card(
                onClick = { showVehicleDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.DirectionsCar, contentDescription = null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = uiState.selectedVehicleCategory.displayName,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = uiState.selectedVehicleCategory.description,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Fare Estimate
            if (uiState.fareBreakdown != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Estimated Fare",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "₹${String.format("%.2f", uiState.estimatedFare)}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "${String.format("%.1f", uiState.distanceKm)} km • ~${uiState.durationMins} mins",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Book Button
            Button(
                onClick = { viewModel.createBooking() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !uiState.isLoading &&
                        uiState.pickupLocation != null &&
                        uiState.dropLocation != null
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Confirm Booking", fontSize = 18.sp)
                }
            }
        }
    }
    
    // Pickup Location Dialog
    if (showPickupDialog) {
        LocationSelectionDialog(
            title = "Select Pickup",
            locations = PopularPlaces.list,
            onSelect = { place ->
                viewModel.setPickupLocation(place)
                showPickupDialog = false
            },
            onDismiss = { showPickupDialog = false }
        )
    }
    
    // Drop Location Dialog
    if (showDropDialog) {
        LocationSelectionDialog(
            title = "Select Drop",
            locations = PopularPlaces.list,
            onSelect = { place ->
                viewModel.setDropLocation(place)
                showDropDialog = false
            },
            onDismiss = { showDropDialog = false }
        )
    }
    
    // Vehicle Category Dialog
    if (showVehicleDialog) {
        AlertDialog(
            onDismissRequest = { showVehicleDialog = false },
            title = { Text("Select Vehicle") },
            text = {
                Column {
                    VehicleCategory.entries.forEach { category ->
                        TextButton(
                            onClick = {
                                viewModel.setVehicleCategory(category)
                                showVehicleDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("${category.displayName} - ${category.description}")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showVehicleDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Booking Type Dialog
    if (showTypeDialog) {
        AlertDialog(
            onDismissRequest = { showTypeDialog = false },
            title = { Text("Select Booking Type") },
            text = {
                Column {
                    BookingType.entries.forEach { type ->
                        TextButton(
                            onClick = {
                                viewModel.setBookingType(type)
                                showTypeDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(type.name.replace("_", " "))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTypeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun LocationSelectionDialog(
    title: String,
    locations: List<PopularPlace>,
    onSelect: (PopularPlace) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                locations.forEach { place ->
                    TextButton(
                        onClick = { onSelect(place) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(place.name, fontWeight = FontWeight.Medium)
                                Text(
                                    place.address,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
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

object PopularPlaces {
    val list = listOf(
        PopularPlace("MG Road Metro", "Mahatma Gandhi Road, Bangalore", 12.9755, 77.6063, "metro"),
        PopularPlace("Kempegowda Airport", "Devanahalli, Bangalore", 13.1979, 77.7063, "airport"),
        PopularPlace("Majestic Bus Station", "Bangalore Central", 12.9762, 77.5715, "bus"),
        PopularPlace("Electronic City", "Hosur Road, Bangalore", 12.8452, 77.6603, "office"),
        PopularPlace("Whitefield", "ITPL Main Road, Bangalore", 12.9698, 77.7499, "office"),
        PopularPlace("Indiranagar", "100 Feet Road, Bangalore", 12.9713, 77.6403, "residential"),
        PopularPlace("Koramangala", "5th Block, Bangalore", 12.9352, 77.6245, "residential"),
        PopularPlace("HSR Layout", "Sector 2, Bangalore", 12.9121, 77.6445, "residential")
    )
}
