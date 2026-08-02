package com.vttcabs.customer.ui.screens.customer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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

data class InvoiceData(
    val bookingId: String,
    val bookingNumber: String,
    val date: Long,
    val pickupAddress: String,
    val dropAddress: String,
    val distance: Double,
    val duration: Int,
    val vehicleType: String,
    val driverName: String,
    val driverPhone: String,
    val vehicleNumber: String,
    val baseFare: Double,
    val distanceFare: Double,
    val timeFare: Double,
    val surge: Double,
    val couponDiscount: Double,
    val tollCharges: Double,
    val totalFare: Double,
    val paymentMethod: String,
    val tripOtp: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceScreen(
    invoice: InvoiceData = InvoiceData(
        bookingId = "123",
        bookingNumber = "VTT-2024-001",
        date = System.currentTimeMillis(),
        pickupAddress = "123 MG Road, Bangalore",
        dropAddress = "456 Indiranagar, Bangalore",
        distance = 12.5,
        duration = 30,
        vehicleType = "Sedan",
        driverName = "Ramesh Kumar",
        driverPhone = "9876543210",
        vehicleNumber = "KA-01-AB-1234",
        baseFare = 50.0,
        distanceFare = 150.0,
        timeFare = 30.0,
        surge = 0.0,
        couponDiscount = 20.0,
        tollCharges = 0.0,
        totalFare = 210.0,
        paymentMethod = "Cash",
        tripOtp = "1234"
    ),
    onShareClick: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Invoice") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onShareClick) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Download, contentDescription = "Download PDF")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Trip Completed",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Booking #${invoice.bookingNumber}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = dateFormat.format(Date(invoice.date)),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Trip Details
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Trip Details",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    
                    LocationRow(
                        icon = Icons.Default.TripOrigin,
                        iconTint = MaterialTheme.colorScheme.primary,
                        label = "From",
                        address = invoice.pickupAddress
                    )
                    
                    LocationRow(
                        icon = Icons.Default.LocationOn,
                        iconTint = MaterialTheme.colorScheme.error,
                        label = "To",
                        address = invoice.dropAddress
                    )
                    
                    HorizontalDivider()
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Distance",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${String.format("%.1f", invoice.distance)} km",
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Column {
                            Text(
                                text = "Duration",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${invoice.duration} mins",
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Column {
                            Text(
                                text = "OTP",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = invoice.tripOtp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Driver Details
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Driver Details",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = invoice.driverName,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = invoice.vehicleType,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        IconButton(onClick = { }) {
                            Icon(Icons.Default.Phone, contentDescription = "Call")
                        }
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Vehicle",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = invoice.vehicleNumber,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Fare Breakdown
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Fare Details",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    
                    FareRow(label = "Base Fare", amount = invoice.baseFare)
                    FareRow(label = "Distance Charge", amount = invoice.distanceFare)
                    FareRow(label = "Time Charge", amount = invoice.timeFare)
                    if (invoice.surge > 0) {
                        FareRow(label = "Surge", amount = invoice.surge)
                    }
                    if (invoice.couponDiscount > 0) {
                        FareRow(
                            label = "Coupon Discount",
                            amount = -invoice.couponDiscount,
                            isDiscount = true
                        )
                    }
                    if (invoice.tollCharges > 0) {
                        FareRow(label = "Toll Charges", amount = invoice.tollCharges)
                    }
                    
                    HorizontalDivider()
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Total",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "₹${String.format("%.2f", invoice.totalFare)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Payment
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (invoice.paymentMethod == "Cash") Icons.Default.Money else Icons.Default.AccountBalance,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Payment Method",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = invoice.paymentMethod,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Text(
                        text = "₹${String.format("%.2f", invoice.totalFare)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }

            // Help
            OutlinedButton(
                onClick = { },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Help, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Need Help with this trip?")
            }
        }
    }
}

@Composable
private fun LocationRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: androidx.compose.ui.graphics.Color,
    label: String,
    address: String
) {
    Row(
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = address,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun FareRow(
    label: String,
    amount: Double,
    isDiscount: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = if (isDiscount) "-₹${String.format("%.2f", kotlin.math.abs(amount))}" else "₹${String.format("%.2f", amount)}",
            color = if (isDiscount) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}
