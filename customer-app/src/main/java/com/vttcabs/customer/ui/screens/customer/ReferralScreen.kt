package com.vttcabs.customer.ui.screens.customer

import android.content.Intent
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ReferralEarning(
    val id: String,
    val friendName: String,
    val friendPhone: String,
    val amount: Double,
    val status: String, // "pending", "completed"
    val date: Long
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReferralScreen(
    referralCode: String = "VTTABC123",
    totalEarnings: Double = 150.0,
    totalReferrals: Int = 3,
    completedReferrals: Int = 1,
    earnings: List<ReferralEarning> = listOf(
        ReferralEarning("1", "John Doe", "9876543210", 50.0, "completed", System.currentTimeMillis() - 86400000),
        ReferralEarning("2", "Jane Smith", "9876543211", 100.0, "pending", System.currentTimeMillis() - 172800000)
    ),
    onShareClick: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val clipboardManager = LocalClipboardManager.current
    var showCopiedSnackbar by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Refer & Earn") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = {
            if (showCopiedSnackbar) {
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { showCopiedSnackbar = false }) {
                            Text("OK")
                        }
                    }
                ) {
                    Text("Referral code copied!")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.CardGiftcard,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Earn ₹100 for every friend",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Your friend gets ₹50 off their first ride too!",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Stats Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Total Earned",
                        value = "₹${totalEarnings.toInt()}",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Successful Referrals",
                        value = "$completedReferrals/$totalReferrals",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Referral Code Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Your Referral Code",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = referralCode,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 4.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(referralCode))
                                    showCopiedSnackbar = true
                                }
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Copy")
                            }
                            Button(onClick = onShareClick) {
                                Icon(Icons.Default.Share, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Share")
                            }
                        }
                    }
                }
            }

            // How it works
            item {
                Text(
                    text = "How it works",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        StepItem(
                            number = 1,
                            title = "Share your code",
                            description = "Send your referral code to friends"
                        )
                        StepItem(
                            number = 2,
                            title = "Friend signs up",
                            description = "They use your code on their first ride"
                        )
                        StepItem(
                            number = 3,
                            title = "Both earn rewards",
                            description = "You get ₹100, friend gets ₹50 off"
                        )
                    }
                }
            }

            // Earnings History
            if (earnings.isNotEmpty()) {
                item {
                    Text(
                        text = "Your Earnings",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(earnings) { earning ->
                    ReferralEarningItem(earning)
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StepItem(
    number: Int,
    title: String,
    description: String
) {
    Row(
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = number.toString(),
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ReferralEarningItem(earning: ReferralEarning) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = earning.friendName,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = earning.friendPhone.take(5) + "****" + earning.friendPhone.takeLast(2),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹${earning.amount.toInt()}",
                    fontWeight = FontWeight.Bold,
                    color = if (earning.status == "completed")
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = earning.status.replaceFirstChar { it.uppercase() },
                    fontSize = 10.sp,
                    color = if (earning.status == "completed")
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
