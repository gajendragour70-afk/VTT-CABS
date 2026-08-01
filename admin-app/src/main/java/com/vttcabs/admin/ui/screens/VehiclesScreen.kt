package com.vttcabs.admin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vttcabs.admin.data.model.FareRule
import com.vttcabs.admin.data.model.Vehicle
import com.vttcabs.admin.ui.theme.*
import com.vttcabs.admin.ui.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehiclesScreen(
    viewModel: AdminViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val vehicles by viewModel.vehicles.collectAsState()
    val fareRules by viewModel.fareRules.collectAsState()
    val isLoading by viewModel.isLoadingVehicles.collectAsState()
    
    var selectedTab by remember { mutableStateOf(0) }
    var showAddVehicleDialog by remember { mutableStateOf(false) }
    var showEditFareDialog by remember { mutableStateOf(false) }
    var selectedRule by remember { mutableStateOf<FareRule?>(null) }
    
    LaunchedEffect(Unit) {
        viewModel.loadVehicles()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vehicle & Fare Management", fontWeight = FontWeight.Bold) },
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
                    IconButton(onClick = { viewModel.loadVehicles() }) {
                        Icon(Icons.Default.Refresh, "Refresh", tint = Color.White)
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = { showAddVehicleDialog = true },
                    containerColor = VTTBluePrimary
                ) {
                    Icon(Icons.Default.Add, "Add Vehicle", tint = Color.White)
                }
            }
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
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Vehicles") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Fare Settings") }
                )
            }
            
            when (selectedTab) {
                0 -> VehiclesList(
                    vehicles = vehicles,
                    isLoading = isLoading,
                    onEdit = { vehicle ->
                        selectedRule = FareRule(
                            id = vehicle.id,
                            vehicleCategory = vehicle.category,
                            baseFare = vehicle.baseFare,
                            perKmRate = vehicle.perKmRate,
                            perMinRate = vehicle.perMinRate
                        )
                        showEditFareDialog = true
                    },
                    onDelete = { viewModel.deleteVehicle(it.id) }
                )
                1 -> FareRulesList(
                    fareRules = fareRules,
                    onEdit = { rule ->
                        selectedRule = rule
                        showEditFareDialog = true
                    }
                )
            }
        }
        
        // Add Vehicle Dialog
        if (showAddVehicleDialog) {
            AddVehicleDialog(
                onDismiss = { showAddVehicleDialog = false },
                onAdd = { vehicle ->
                    viewModel.addVehicle(vehicle)
                    showAddVehicleDialog = false
                }
            )
        }
        
        // Edit Fare Rule Dialog
        if (showEditFareDialog && selectedRule != null) {
            EditFareRuleDialog(
                rule = selectedRule!!,
                onDismiss = {
                    showEditFareDialog = false
                    selectedRule = null
                },
                onSave = { rule ->
                    viewModel.updateFareRule(rule)
                    showEditFareDialog = false
                    selectedRule = null
                }
            )
        }
    }
}

@Composable
fun VehiclesList(
    vehicles: List<Vehicle>,
    isLoading: Boolean,
    onEdit: (Vehicle) -> Unit,
    onDelete: (Vehicle) -> Unit
) {
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (vehicles.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.LocalTaxi, null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
                Text("No vehicles found", color = Color.Gray)
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(vehicles, key = { it.id }) { vehicle ->
                VehicleCard(
                    vehicle = vehicle,
                    onEdit = { onEdit(vehicle) },
                    onDelete = { onDelete(vehicle) }
                )
            }
        }
    }
}

@Composable
fun VehicleCard(
    vehicle: Vehicle,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
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
                Column {
                    Text(
                        vehicle.category,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        vehicle.name,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (vehicle.isActive) VTTSuccess.copy(alpha = 0.1f) else VTTDanger.copy(alpha = 0.1f)
                ) {
                    Text(
                        if (vehicle.isActive) "Active" else "Inactive",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (vehicle.isActive) VTTSuccess else VTTDanger,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(vehicle.description, fontSize = 13.sp, color = Color.Gray)
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Pricing
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PriceItem("Base Fare", "₹${vehicle.baseFare.toInt()}")
                PriceItem("Per KM", "₹${vehicle.perKmRate.toInt()}")
                PriceItem("Per Min", "₹${vehicle.perMinRate.toInt()}")
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit", fontSize = 12.sp)
                }
                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VTTDanger)
                ) {
                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun PriceItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = VTTBluePrimary)
        Text(label, fontSize = 11.sp, color = Color.Gray)
    }
}

@Composable
fun FareRulesList(
    fareRules: List<FareRule>,
    onEdit: (FareRule) -> Unit
) {
    if (fareRules.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.AttachMoney, null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
                Text("No fare rules configured", color = Color.Gray)
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(fareRules, key = { it.id }) { rule ->
                FareRuleCard(rule = rule, onEdit = { onEdit(rule) })
            }
        }
    }
}

@Composable
fun FareRuleCard(
    rule: FareRule,
    onEdit: () -> Unit
) {
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
                Text(
                    rule.vehicleCategory,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                if (rule.surgeMultiplier > 1.0) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = VTTWarning.copy(alpha = 0.1f)
                    ) {
                        Text(
                            "${rule.surgeMultiplier}x Surge",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = VTTWarning,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PriceItem("Base Fare", "₹${rule.baseFare.toInt()}")
                PriceItem("Per KM", "₹${rule.perKmRate}")
                PriceItem("Per Min", "₹${rule.perMinRate}")
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = onEdit,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Edit Fare Rules")
            }
        }
    }
}

@Composable
fun AddVehicleDialog(
    onDismiss: () -> Unit,
    onAdd: (Vehicle) -> Unit
) {
    var category by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var baseFare by remember { mutableStateOf("") }
    var perKmRate by remember { mutableStateOf("") }
    var perMinRate by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Vehicle", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category *") },
                    placeholder = { Text("e.g., SEDAN, SUV") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name *") },
                    placeholder = { Text("e.g., Honda City") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = baseFare,
                        onValueChange = { baseFare = it },
                        label = { Text("Base Fare") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = perKmRate,
                        onValueChange = { perKmRate = it },
                        label = { Text("Per KM") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = perMinRate,
                    onValueChange = { perMinRate = it },
                    label = { Text("Per Minute") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val vehicle = Vehicle(
                        category = category.uppercase(),
                        name = name,
                        description = description,
                        baseFare = baseFare.toDoubleOrNull() ?: 0.0,
                        perKmRate = perKmRate.toDoubleOrNull() ?: 0.0,
                        perMinRate = perMinRate.toDoubleOrNull() ?: 0.0
                    )
                    onAdd(vehicle)
                },
                enabled = category.isNotBlank() && name.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditFareRuleDialog(
    rule: FareRule,
    onDismiss: () -> Unit,
    onSave: (FareRule) -> Unit
) {
    var baseFare by remember { mutableStateOf(rule.baseFare.toString()) }
    var perKmRate by remember { mutableStateOf(rule.perKmRate.toString()) }
    var perMinRate by remember { mutableStateOf(rule.perMinRate.toString()) }
    var minimumFare by remember { mutableStateOf(rule.minimumFare.toString()) }
    var surgeMultiplier by remember { mutableStateOf(rule.surgeMultiplier.toString()) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Fare: ${rule.vehicleCategory}", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = baseFare,
                    onValueChange = { baseFare = it },
                    label = { Text("Base Fare (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = perKmRate,
                    onValueChange = { perKmRate = it },
                    label = { Text("Per KM Rate (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = perMinRate,
                    onValueChange = { perMinRate = it },
                    label = { Text("Per Minute Rate (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = minimumFare,
                    onValueChange = { minimumFare = it },
                    label = { Text("Minimum Fare (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = surgeMultiplier,
                    onValueChange = { surgeMultiplier = it },
                    label = { Text("Surge Multiplier") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updatedRule = rule.copy(
                        baseFare = baseFare.toDoubleOrNull() ?: rule.baseFare,
                        perKmRate = perKmRate.toDoubleOrNull() ?: rule.perKmRate,
                        perMinRate = perMinRate.toDoubleOrNull() ?: rule.perMinRate,
                        minimumFare = minimumFare.toDoubleOrNull() ?: rule.minimumFare,
                        surgeMultiplier = surgeMultiplier.toDoubleOrNull() ?: rule.surgeMultiplier
                    )
                    onSave(updatedRule)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
