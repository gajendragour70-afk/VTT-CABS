package com.vttcabs.driver.ui.screens.driver

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
import java.text.SimpleDateFormat
import java.util.*

data class EarningEntry(
    val id: String,
    val date: Long,
    val tripCount: Int,
    val grossEarning: Double,
    val bonus: Double,
    val incentive: Double,
    val penalty: Double,
    val platformFee: Double,
    val netEarning: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverEarningsScreen(
    todayEarnings: Double = 1250.0,
    weekEarnings: Double = 8500.0,
    monthEarnings: Double = 35000.0,
    pendingPayout: Double = 2500.0,
    walletBalance: Double = 15000.0,
    entries: List<EarningEntry> = listOf(
        EarningEntry(
            "1",
            System.currentTimeMillis(),
            8,
            1500.0,
            50.0,
            100.0,
            0.0,
            225.0,
            1425.0
        ),
        EarningEntry(
            "2",
            System.currentTimeMillis() - 86400000,
            6,
            1100.0,
            0.0,
            75.0,
            50.0,
            165.0,
            960.0
        ),
        EarningEntry(
            "3",
            System.currentTimeMillis() - 172800000,
            10,
            1800.0,
            100.0,
            150.0,
            0.0,
            270.0,
            1780.0
        )
    ),
    onRequestPayout: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val dateFormat = remember { SimpleDateFormat("EEE, dd MMM", Locale.getDefault()) }
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Earnings") },
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
            // Stats Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Today's Earnings",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "₹${String.format("%.0f", todayEarnings)}",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Pending Payout",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "₹${String.format("%.0f", pendingPayout)}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        EarningsStatCard(
                            title = "This Week",
                            value = "₹${String.format("%.0f", weekEarnings)}",
                            modifier = Modifier.weight(1f)
                        )
                        EarningsStatCard(
                            title = "This Month",
                            value = "₹${String.format("%.0f", monthEarnings)}",
                            modifier = Modifier.weight(1f)
                        )
                        EarningsStatCard(
                            title = "Wallet",
                            value = "₹${String.format("%.0f", walletBalance)}",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = onRequestPayout,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = pendingPayout > 0
                    ) {
                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Request Payout")
                    }
                }
            }

            // Tab Row
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Daily") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Weekly") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Monthly") }
                )
            }

            // Earnings List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(entries) { entry ->
                    EarningDayCard(entry, dateFormat)
                }

                if (entries.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Money,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No earnings yet",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EarningsStatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun EarningDayCard(
    entry: EarningEntry,
    dateFormat: SimpleDateFormat
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = dateFormat.format(Date(entry.date)),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${entry.tripCount} trips",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    text = "₹${String.format("%.0f", entry.netEarning)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            HorizontalDivider()

            // Breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Gross",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(text = "₹${String.format("%.0f", entry.grossEarning)}")
                }
                Column {
                    Text(
                        text = "Bonus/Incentive",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "+₹${String.format("%.0f", entry.bonus + entry.incentive)}",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Column {
                    Text(
                        text = "Penalty",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "-₹${String.format("%.0f", entry.penalty)}",
                        color = if (entry.penalty > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                }
                Column {
                    Text(
                        text = "Platform Fee",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(text = "-₹${String.format("%.0f", entry.platformFee)}")
                }
            }
        }
    }
}
