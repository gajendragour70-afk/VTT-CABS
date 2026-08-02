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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class WalletTransaction(
    val id: String,
    val type: String, // "credit" or "debit"
    val amount: Double,
    val description: String,
    val date: Long,
    val bookingId: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    balance: Double = 250.0,
    transactions: List<WalletTransaction> = listOf(
        WalletTransaction("1", "credit", 100.0, "Welcome Bonus", System.currentTimeMillis() - 86400000),
        WalletTransaction("2", "debit", 150.0, "Trip to Airport", System.currentTimeMillis() - 43200000, "BK001"),
        WalletTransaction("3", "credit", 50.0, "Referral Bonus", System.currentTimeMillis() - 172800000)
    ),
    onAddMoney: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Wallet") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            // Balance Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Available Balance",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "₹${String.format("%.2f", balance)}",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onAddMoney,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.onPrimary,
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add Money")
                        }
                    }
                }
            }

            // Quick Actions
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionCard(
                        icon = Icons.Default.CardGiftcard,
                        title = "Coupons",
                        modifier = Modifier.weight(1f),
                        onClick = { }
                    )
                    QuickActionCard(
                        icon = Icons.Default.CardMembership,
                        title = "Refer & Earn",
                        modifier = Modifier.weight(1f),
                        onClick = { }
                    )
                }
            }

            // Transaction History Header
            item {
                Text(
                    text = "Transaction History",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Transactions
            items(transactions) { transaction ->
                TransactionItem(transaction)
            }

            if (transactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Receipt,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No transactions yet",
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
private fun QuickActionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null)
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = title)
        }
    }
}

@Composable
private fun TransactionItem(transaction: WalletTransaction) {
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
                    if (transaction.type == "credit") Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                    contentDescription = null,
                    tint = if (transaction.type == "credit") 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = transaction.description,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (transaction.type == "credit") "Added" else "Spent",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = "${if (transaction.type == "credit") "+" else "-"}₹${String.format("%.2f", transaction.amount)}",
                fontWeight = FontWeight.Bold,
                color = if (transaction.type == "credit") 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.error
            )
        }
    }
}
