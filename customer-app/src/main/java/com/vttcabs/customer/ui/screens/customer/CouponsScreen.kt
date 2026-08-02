package com.vttcabs.customer.ui.screens.customer

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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Coupon(
    val id: String,
    val code: String,
    val description: String,
    val discountType: String, // "percentage" or "flat"
    val discountValue: Double,
    val minOrderValue: Double,
    val maxDiscount: Double?,
    val expiresAt: Long,
    val isUsed: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CouponsScreen(
    availableCoupons: List<Coupon> = listOf(
        Coupon("1", "FIRST50", "50% off on first ride", "percentage", 50.0, 200.0, 100.0, System.currentTimeMillis() + 604800000),
        Coupon("2", "FLAT100", "Flat ₹100 off", "flat", 100.0, 500.0, null, System.currentTimeMillis() + 1209600000),
        Coupon("3", "WEEKEND20", "20% off on weekends", "percentage", 20.0, 300.0, 200.0, System.currentTimeMillis() + 2592000000)
    ),
    usedCoupons: List<Coupon> = emptyList(),
    onBackClick: () -> Unit = {},
    onApplyCoupon: (String) -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }
    var couponCode by remember { mutableStateOf("") }
    var showApplyDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Coupons") },
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
            // Apply Coupon Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Have a coupon code?",
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = couponCode,
                            onValueChange = { couponCode = it.uppercase() },
                            placeholder = { Text("Enter code") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Button(
                            onClick = { 
                                if (couponCode.isNotBlank()) {
                                    onApplyCoupon(couponCode)
                                }
                            }
                        ) {
                            Text("Apply")
                        }
                    }
                }
            }

            // Tab Row
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Available (${availableCoupons.size})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Used (${usedCoupons.size})") }
                )
            }

            // Coupon List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val coupons = if (selectedTab == 0) availableCoupons else usedCoupons

                items(coupons) { coupon ->
                    CouponCard(
                        coupon = coupon,
                        onApply = { onApplyCoupon(coupon.code) }
                    )
                }

                if (coupons.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.LocalOffer,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (selectedTab == 0) "No coupons available" else "No used coupons",
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
private fun CouponCard(
    coupon: Coupon,
    onApply: () -> Unit
) {
    val isExpired = coupon.expiresAt < System.currentTimeMillis()
    val isUsed = coupon.isUsed

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isExpired || isUsed)
                MaterialTheme.colorScheme.surfaceVariant
            else
                MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocalOffer,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = coupon.code,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        textDecoration = if (isExpired || isUsed) TextDecoration.LineThrough else TextDecoration.None
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = coupon.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (coupon.discountType == "percentage") 
                        "${coupon.discountValue.toInt()}% off (Min ₹${coupon.minOrderValue.toInt()})" 
                    else 
                        "₹${coupon.discountValue.toInt()} off (Min ₹${coupon.minOrderValue.toInt()})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                if (coupon.maxDiscount != null) {
                    Text(
                        text = "Max discount: ₹${coupon.maxDiscount.toInt()}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isExpired) "Expired" else if (isUsed) "Already used" else "Valid for ${(coupon.expiresAt - System.currentTimeMillis()) / 86400000} days",
                    fontSize = 10.sp,
                    color = if (isExpired) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!isExpired && !isUsed) {
                Button(onClick = onApply) {
                    Text("Apply")
                }
            }
        }
    }
}
