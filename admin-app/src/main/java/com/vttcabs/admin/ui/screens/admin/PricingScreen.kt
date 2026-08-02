package com.vttcabs.admin.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class PricingConfig(
    val vehicleCategory: String,
    val baseFare: Double,
    val perKm: Double,
    val perMin: Double,
    val minFare: Double,
    val commissionPercent: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PricingScreen(
    pricingConfigs: List<PricingConfig> = listOf(
        PricingConfig("Auto", 30.0, 10.0, 1.0, 50.0, 15.0),
        PricingConfig("Hatchback", 50.0, 12.0, 1.5, 80.0, 15.0),
        PricingConfig("Sedan", 60.0, 14.0, 2.0, 100.0, 15.0),
        PricingConfig("SUV", 80.0, 18.0, 2.5, 150.0, 15.0),
        PricingConfig("Innova Crysta", 100.0, 22.0, 3.0, 200.0, 15.0)
    ),
    onSavePricing: (List<PricingConfig>) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    var configs by remember { mutableStateOf(pricingConfigs) }
    var editedConfigs by remember { mutableStateOf(configs) }
    var showSaveDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pricing Management") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { showSaveDialog = true }) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save")
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
            // Info Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Pricing changes will apply to all new bookings immediately.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Vehicle Category Pricing",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            items(editedConfigs) { config ->
                PricingCard(
                    config = config,
                    onConfigChange = { newConfig ->
                        editedConfigs = editedConfigs.map {
                            if (it.vehicleCategory == config.vehicleCategory) newConfig else it
                        }
                    }
                )
            }

            // Surge Pricing Section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Surge Pricing",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            item {
                SurgePricingCard()
            }
        }
    }

    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Save Pricing") },
            text = { Text("Are you sure you want to save these pricing changes? They will apply to all new bookings.") },
            confirmButton = {
                TextButton(onClick = {
                    onSavePricing(editedConfigs)
                    configs = editedConfigs
                    showSaveDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun PricingCard(
    config: PricingConfig,
    onConfigChange: (PricingConfig) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = config.vehicleCategory,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PricingTextField(
                    label = "Base Fare",
                    value = config.baseFare.toString(),
                    prefix = "₹",
                    modifier = Modifier.weight(1f),
                    onValueChange = {
                        it.toDoubleOrNull()?.let { value ->
                            onConfigChange(config.copy(baseFare = value))
                        }
                    }
                )
                PricingTextField(
                    label = "Per KM",
                    value = config.perKm.toString(),
                    prefix = "₹",
                    modifier = Modifier.weight(1f),
                    onValueChange = {
                        it.toDoubleOrNull()?.let { value ->
                            onConfigChange(config.copy(perKm = value))
                        }
                    }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PricingTextField(
                    label = "Per Min",
                    value = config.perMin.toString(),
                    prefix = "₹",
                    modifier = Modifier.weight(1f),
                    onValueChange = {
                        it.toDoubleOrNull()?.let { value ->
                            onConfigChange(config.copy(perMin = value))
                        }
                    }
                )
                PricingTextField(
                    label = "Min Fare",
                    value = config.minFare.toString(),
                    prefix = "₹",
                    modifier = Modifier.weight(1f),
                    onValueChange = {
                        it.toDoubleOrNull()?.let { value ->
                            onConfigChange(config.copy(minFare = value))
                        }
                    }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                PricingTextField(
                    label = "Commission",
                    value = config.commissionPercent.toString(),
                    suffix = "%",
                    modifier = Modifier.width(120.dp),
                    onValueChange = {
                        it.toDoubleOrNull()?.let { value ->
                            onConfigChange(config.copy(commissionPercent = value))
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun PricingTextField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    prefix: String? = null,
    suffix: String? = null,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 12.sp) },
        modifier = modifier,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        prefix = prefix?.let { { Text(it) } },
        suffix = suffix?.let { { Text(it) } }
    )
}

@Composable
private fun SurgePricingCard() {
    var surgeEnabled by remember { mutableStateOf(false) }
    var surgeMultiplier by remember { mutableStateOf("1.5") }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Enable Surge Pricing",
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Multiply fares during high demand",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = surgeEnabled,
                    onCheckedChange = { surgeEnabled = it }
                )
            }

            if (surgeEnabled) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Surge Multiplier:")
                    PricingTextField(
                        label = "Multiplier",
                        value = surgeMultiplier,
                        modifier = Modifier.width(100.dp),
                        onValueChange = { surgeMultiplier = it }
                    )
                    Text("x")
                }
            }
        }
    }
}
