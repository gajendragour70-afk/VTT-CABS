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

data class CustomerInfo(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val totalRides: Int,
    val walletBalance: Double,
    val isActive: Boolean,
    val createdAt: Long
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomersScreen(
    customers: List<CustomerInfo> = listOf(
        CustomerInfo("1", "John Doe", "john@example.com", "9876543210", 25, 500.0, true, System.currentTimeMillis() - 86400000),
        CustomerInfo("2", "Jane Smith", "jane@example.com", "9876543211", 18, 200.0, true, System.currentTimeMillis() - 172800000),
        CustomerInfo("3", "Bob Wilson", "bob@example.com", "9876543212", 5, 0.0, false, System.currentTimeMillis() - 604800000)
    ),
    onCustomerClick: (String) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredCustomers = customers.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
        it.email.contains(searchQuery, ignoreCase = true) ||
        it.phone.contains(searchQuery)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Customers") },
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
                placeholder = { Text("Search customers...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                singleLine = true
            )

            // Stats
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AdminStatCard(
                    title = "Total Customers",
                    value = customers.size.toString(),
                    modifier = Modifier.weight(1f)
                )
                AdminStatCard(
                    title = "Active",
                    value = customers.count { it.isActive }.toString(),
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
                items(filteredCustomers) { customer ->
                    CustomerCard(
                        customer = customer,
                        onClick = { onCustomerClick(customer.id) }
                    )
                }

                if (filteredCustomers.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No customers found",
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
private fun CustomerCard(
    customer: CustomerInfo,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = customer.name.take(1).uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = customer.name,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = customer.email,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = customer.phone,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${customer.totalRides} rides",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "₹${customer.walletBalance.toInt()}",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = if (customer.isActive)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = if (customer.isActive) "Active" else "Inactive",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 10.sp
                    )
                }
            }
        }
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
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
            Text(
                text = title,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
