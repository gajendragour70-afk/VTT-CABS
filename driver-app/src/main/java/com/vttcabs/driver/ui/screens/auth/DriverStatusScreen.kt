package com.vttcabs.driver.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vttcabs.common.DriverApprovalStatus
import com.vttcabs.common.DriverRegistrationEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverStatusScreen(
    registration: DriverRegistrationEntity,
    onUploadDocuments: () -> Unit,
    onLogout: () -> Unit
) {
    val statusColor = when (registration.approvalStatus) {
        DriverApprovalStatus.DRAFT -> MaterialTheme.colorScheme.secondary
        DriverApprovalStatus.SUBMITTED, DriverApprovalStatus.PENDING -> MaterialTheme.colorScheme.tertiary
        DriverApprovalStatus.APPROVED -> MaterialTheme.colorScheme.primary
        DriverApprovalStatus.REJECTED -> MaterialTheme.colorScheme.error
        DriverApprovalStatus.SUSPENDED -> MaterialTheme.colorScheme.error
    }
    
    val statusIcon = when (registration.approvalStatus) {
        DriverApprovalStatus.DRAFT -> Icons.Default.Edit
        DriverApprovalStatus.SUBMITTED, DriverApprovalStatus.PENDING -> Icons.Default.HourglassTop
        DriverApprovalStatus.APPROVED -> Icons.Default.CheckCircle
        DriverApprovalStatus.REJECTED -> Icons.Default.Cancel
        DriverApprovalStatus.SUSPENDED -> Icons.Default.Block
    }
    
    val statusMessage = when (registration.approvalStatus) {
        DriverApprovalStatus.DRAFT -> "Your registration is incomplete"
        DriverApprovalStatus.SUBMITTED -> "Your application is under review"
        DriverApprovalStatus.PENDING -> "Your application is being verified"
        DriverApprovalStatus.APPROVED -> "Congratulations! You are approved"
        DriverApprovalStatus.REJECTED -> "Your application was rejected"
        DriverApprovalStatus.SUSPENDED -> "Your account is suspended"
    }
    
    val reuploadDocs = if (registration.reuploadRequestedDocs.isNotBlank()) {
        registration.reuploadRequestedDocs.split(",").map { it.trim() }
    } else emptyList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registration Status") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout")
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
            // Status Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = statusColor.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            statusIcon,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = statusColor
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            registration.approvalStatus.name,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            statusMessage,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Rejection reason
            if (registration.approvalStatus == DriverApprovalStatus.REJECTED && registration.rejectionReason.isNotBlank()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Rejection Reason",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(registration.rejectionReason)
                        }
                    }
                }
            }

            // Re-upload required documents
            if (reuploadDocs.isNotEmpty() && registration.approvalStatus != DriverApprovalStatus.APPROVED) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Documents to Re-upload",
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            reuploadDocs.forEach { doc ->
                                Row(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Upload,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(doc)
                                }
                            }
                        }
                    }
                }
            }

            // Progress tracking
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Registration Progress",
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        ProgressItem("Personal Information", registration.personalInfoComplete)
                        ProgressItem("Documents", registration.documentsComplete)
                        ProgressItem("Vehicle Info", registration.vehicleInfoComplete)
                        ProgressItem("Bank Details", registration.bankDetailsComplete)
                    }
                }
            }

            // Application details
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Application Details",
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        DetailRow("Name", registration.fullName)
                        DetailRow("Email", registration.email)
                        DetailRow("Phone", registration.phone)
                        DetailRow("Submitted", if (registration.submittedAt > 0) {
                            java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
                                .format(java.util.Date(registration.submittedAt))
                        } else "Not submitted")
                    }
                }
            }

            // Action buttons
            if (registration.approvalStatus == DriverApprovalStatus.REJECTED || 
                registration.approvalStatus == DriverApprovalStatus.DRAFT) {
                item {
                    Button(
                        onClick = onUploadDocuments,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Icon(Icons.Default.Upload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Upload/Update Documents")
                    }
                }
            }

            // Help section
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Help,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Need Help?",
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Contact our support team at support@vttcabs.com or call +91 98765 43210",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProgressItem(label: String, isComplete: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (isComplete) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (isComplete) 
                MaterialTheme.colorScheme.primary 
            else 
                MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            label,
            color = if (isComplete) 
                MaterialTheme.colorScheme.onSurface 
            else 
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp
        )
        Text(
            value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
