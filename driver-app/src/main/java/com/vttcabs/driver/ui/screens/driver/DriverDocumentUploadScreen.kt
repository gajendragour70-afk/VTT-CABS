package com.vttcabs.driver.ui.screens.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class DocumentInfo(
    val id: String,
    val name: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val isUploaded: Boolean,
    val isRequired: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverDocumentUploadScreen(
    onDocumentClick: (String) -> Unit = {},
    onSubmit: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    var documents by remember {
        mutableStateOf(
            listOf(
                DocumentInfo("profile", "Profile Photo", "Recent passport size photo", Icons.Default.Person, true, true),
                DocumentInfo("selfie", "Driver Selfie", "Photo holding ID proof", Icons.Default.CameraAlt, true, true),
                DocumentInfo("aadhaar_front", "Aadhaar Card (Front)", "Front side of Aadhaar", Icons.Default.Badge, false, true),
                DocumentInfo("aadhaar_back", "Aadhaar Card (Back)", "Back side of Aadhaar", Icons.Default.Badge, false, true),
                DocumentInfo("pan", "PAN Card", "Permanent Account Number card", Icons.Default.CreditCard, false, true),
                DocumentInfo("license_front", "Driving License (Front)", "Front side of license", Icons.Default.DirectionsCar, false, true),
                DocumentInfo("license_back", "Driving License (Back)", "Back side of license", Icons.Default.DirectionsCar, false, true),
                DocumentInfo("rc", "RC (Registration Certificate)", "Vehicle registration certificate", Icons.Default.Article, false, true),
                DocumentInfo("insurance", "Insurance", "Vehicle insurance document", Icons.Default.Security, false, true),
                DocumentInfo("puc", "Pollution Certificate", "PUC certificate", Icons.Default.Cloud, false, true)
            )
        )
    }

    val requiredDocs = documents.filter { it.isRequired && !it.isUploaded }
    val allRequiredUploaded = requiredDocs.isEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Upload Documents") },
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                            text = "Upload clear, readable images of all documents. Blurry or incomplete documents may result in rejection.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            // Progress
            item {
                val uploadedCount = documents.count { it.isUploaded }
                val totalCount = documents.size
                
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Document Verification",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$uploadedCount/$totalCount uploaded",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { uploadedCount.toFloat() / totalCount },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            // Required Documents Header
            item {
                Text(
                    text = "Required Documents",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Documents
            items(documents) { doc ->
                DocumentCard(
                    document = doc,
                    onClick = { onDocumentClick(doc.id) }
                )
            }

            // Submit Button
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onSubmit,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = allRequiredUploaded
                ) {
                    Icon(Icons.Default.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Submit for Verification")
                }
                
                if (!allRequiredUploaded) {
                    Text(
                        text = "Please upload all required documents",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Spacer
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun DocumentCard(
    document: DocumentInfo,
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
                // Document Icon/Preview
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(
                            width = 1.dp,
                            color = if (document.isUploaded)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (document.isUploaded) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Uploaded",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Icon(
                            document.icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = document.name,
                            fontWeight = FontWeight.Medium
                        )
                        if (document.isRequired) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "*",
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = document.description,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Status/Action
            if (document.isUploaded) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "Uploaded",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            } else {
                FilledTonalButton(
                    onClick = onClick,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        if (document.isUploaded) Icons.Default.Edit else Icons.Default.Upload,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (document.isUploaded) "Update" else "Upload")
                }
            }
        }
    }
}
