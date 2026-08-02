package com.vttcabs.admin.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vttcabs.common.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverVerificationScreen(
    repository: VttRepository,
    onBackClick: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var registrations by remember { mutableStateOf<List<DriverRegistrationEntity>>(emptyList()) }
    var approvedDrivers by remember { mutableStateOf<List<DriverEntity>>(emptyList()) }
    var selectedDriver by remember { mutableStateOf<DriverRegistrationEntity?>(null) }
    var showApprovalDialog by remember { mutableStateOf(false) }
    var showRejectionDialog by remember { mutableStateOf(false) }
    var showReuploadDialog by remember { mutableStateOf(false) }
    var rejectionReason by remember { mutableStateOf("") }
    var reuploadDocs by remember { mutableStateOf(listOf<String>()) }
    
    val scope = rememberCoroutineScope()
    val tabs = listOf("Pending", "Approved", "All Drivers")
    
    LaunchedEffect(selectedTab) {
        registrations = repository.getPendingDriverRegistrations()
        approvedDrivers = repository.driversList.filter { it.approvalStatus == DriverApprovalStatus.APPROVED }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Driver Verification") },
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
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(title)
                                if (index == 0 && registrations.isNotEmpty()) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Badge { Text("${registrations.size}") }
                                }
                            }
                        }
                    )
                }
            }
            
            when (selectedTab) {
                0 -> PendingDriversList(
                    registrations = registrations,
                    onViewDetails = { selectedDriver = it },
                    onApprove = { 
                        selectedDriver = it
                        showApprovalDialog = true
                    },
                    onReject = {
                        selectedDriver = it
                        showRejectionDialog = true
                    },
                    onRequestReupload = {
                        selectedDriver = it
                        showReuploadDialog = true
                    }
                )
                1 -> ApprovedDriversList(drivers = approvedDrivers)
                2 -> AllDriversList(repository = repository)
            }
        }
    }
    
    // Approval Dialog
    if (showApprovalDialog && selectedDriver != null) {
        AlertDialog(
            onDismissRequest = { showApprovalDialog = false },
            title = { Text("Approve Driver") },
            text = {
                Column {
                    Text("Are you sure you want to approve this driver?")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        selectedDriver!!.fullName,
                        fontWeight = FontWeight.Bold
                    )
                    Text(selectedDriver!!.email)
                    Text("Vehicle: ${selectedDriver!!.vehicleNumber}")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            repository.approveDriver(selectedDriver!!.id, "admin1")
                            registrations = repository.getPendingDriverRegistrations()
                            showApprovalDialog = false
                        }
                    }
                ) {
                    Text("Approve")
                }
            },
            dismissButton = {
                TextButton(onClick = { showApprovalDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Rejection Dialog
    if (showRejectionDialog && selectedDriver != null) {
        AlertDialog(
            onDismissRequest = { showRejectionDialog = false },
            title = { Text("Reject Driver") },
            text = {
                Column {
                    Text("Please provide a reason for rejection:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = rejectionReason,
                        onValueChange = { rejectionReason = it },
                        label = { Text("Reason") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            repository.rejectDriver(selectedDriver!!.id, "admin1", rejectionReason)
                            registrations = repository.getPendingDriverRegistrations()
                            showRejectionDialog = false
                            rejectionReason = ""
                        }
                    },
                    enabled = rejectionReason.isNotBlank()
                ) {
                    Text("Reject")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectionDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Request Reupload Dialog
    if (showReuploadDialog && selectedDriver != null) {
        val docOptions = listOf(
            "Aadhaar Front", "Aadhaar Back", "PAN Card", 
            "License Front", "License Back", "Profile Photo",
            "Vehicle Front", "Vehicle Back", "Vehicle Left", "Vehicle Right"
        )
        
        AlertDialog(
            onDismissRequest = { showReuploadDialog = false },
            title = { Text("Request Document Re-upload") },
            text = {
                Column {
                    Text("Select documents that need to be re-uploaded:")
                    Spacer(modifier = Modifier.height(8.dp))
                    docOptions.forEach { doc ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = reuploadDocs.contains(doc),
                                onCheckedChange = { checked ->
                                    reuploadDocs = if (checked) {
                                        reuploadDocs + doc
                                    } else {
                                        reuploadDocs - doc
                                    }
                                }
                            )
                            Text(doc)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            repository.requestDriverReupload(
                                selectedDriver!!.id, 
                                "admin1", 
                                reuploadDocs
                            )
                            registrations = repository.getPendingDriverRegistrations()
                            showReuploadDialog = false
                            reuploadDocs = emptyList()
                        }
                    },
                    enabled = reuploadDocs.isNotEmpty()
                ) {
                    Text("Send Request")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReuploadDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Driver Details Bottom Sheet
    if (selectedDriver != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedDriver = null }
        ) {
            DriverDetailsSheet(
                registration = selectedDriver!!,
                onClose = { selectedDriver = null }
            )
        }
    }
}

@Composable
fun PendingDriversList(
    registrations: List<DriverRegistrationEntity>,
    onViewDetails: (DriverRegistrationEntity) -> Unit,
    onApprove: (DriverRegistrationEntity) -> Unit,
    onReject: (DriverRegistrationEntity) -> Unit,
    onRequestReupload: (DriverRegistrationEntity) -> Unit
) {
    if (registrations.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "No pending applications",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "All driver applications have been reviewed",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(registrations) { registration ->
                PendingDriverCard(
                    registration = registration,
                    onViewDetails = { onViewDetails(registration) },
                    onApprove = { onApprove(registration) },
                    onReject = { onReject(registration) },
                    onRequestReupload = { onRequestReupload(registration) }
                )
            }
        }
    }
}

@Composable
fun PendingDriverCard(
    registration: DriverRegistrationEntity,
    onViewDetails: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onRequestReupload: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        registration.fullName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        registration.email,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        registration.phone,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                AssistChip(
                    onClick = {},
                    label = { Text(registration.approvalStatus.name) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = when (registration.approvalStatus) {
                            DriverApprovalStatus.SUBMITTED -> MaterialTheme.colorScheme.secondaryContainer
                            DriverApprovalStatus.PENDING -> MaterialTheme.colorScheme.tertiaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                "Vehicle: ${registration.vehicleNumber.ifBlank { "Not provided" }}",
                fontSize = 14.sp
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Documents", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row {
                        listOf(
                            registration.aadhaarFrontUrl.isNotBlank(),
                            registration.panCardUrl.isNotBlank(),
                            registration.licenseFrontUrl.isNotBlank(),
                            registration.profilePhotoUrl.isNotBlank(),
                            registration.vehicleFrontPhotoUrl.isNotBlank()
                        ).forEach { uploaded ->
                            Icon(
                                if (uploaded) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (uploaded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onViewDetails,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("View")
                }
                Button(
                    onClick = onApprove,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Approve")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onRequestReupload,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Re-upload")
                }
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reject")
                }
            }
        }
    }
}

@Composable
fun ApprovedDriversList(drivers: List<DriverEntity>) {
    if (drivers.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No approved drivers")
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(drivers) { driver ->
                ApprovedDriverCard(driver = driver)
            }
        }
    }
}

@Composable
fun ApprovedDriverCard(driver: DriverEntity) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    driver.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${driver.vehicleModel} - ${driver.vehicleNumber}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        String.format("%.1f", driver.rating),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    Text(
                        "• ${driver.totalTrips} trips",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (driver.isOnline) Icons.Default.Circle else Icons.Default.Circle,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = if (driver.isOnline) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        if (driver.isOnline) "Online" else "Offline",
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
                Text(
                    "₹${String.format("%.0f", driver.totalEarnings)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllDriversList(repository: VttRepository) {
    var drivers by remember { mutableStateOf<List<DriverEntity>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var filterStatus by remember { mutableStateOf<DriverApprovalStatus?>(null) }
    var selectedDriver by remember { mutableStateOf<DriverEntity?>(null) }
    var showSuspendDialog by remember { mutableStateOf(false) }
    var suspendReason by remember { mutableStateOf("") }
    
    LaunchedEffect(Unit) {
        drivers = repository.driversList
    }
    
    val scope = rememberCoroutineScope()
    
    val filteredDrivers = drivers.filter { driver ->
        (searchQuery.isBlank() || 
            driver.name.contains(searchQuery, ignoreCase = true) ||
            driver.email.contains(searchQuery, ignoreCase = true) ||
            driver.vehicleNumber.contains(searchQuery, ignoreCase = true)) &&
        (filterStatus == null || driver.approvalStatus == filterStatus)
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Search and Filter
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            singleLine = true
        )
        
        // Filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = filterStatus == null,
                onClick = { filterStatus = null },
                label = { Text("All") }
            )
            FilterChip(
                selected = filterStatus == DriverApprovalStatus.APPROVED,
                onClick = { filterStatus = DriverApprovalStatus.APPROVED },
                label = { Text("Approved") }
            )
            FilterChip(
                selected = filterStatus == DriverApprovalStatus.SUSPENDED,
                onClick = { filterStatus = DriverApprovalStatus.SUSPENDED },
                label = { Text("Suspended") }
            )
        }
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredDrivers) { driver ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { selectedDriver = driver }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(driver.name, fontWeight = FontWeight.Bold)
                                Text(driver.email, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(driver.vehicleNumber, fontSize = 12.sp)
                            }
                            AssistChip(
                                onClick = {},
                                label = { Text(driver.approvalStatus.name) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = when (driver.approvalStatus) {
                                        DriverApprovalStatus.APPROVED -> MaterialTheme.colorScheme.primaryContainer
                                        DriverApprovalStatus.SUSPENDED -> MaterialTheme.colorScheme.errorContainer
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    }
                                )
                            )
                        }
                        
                        if (selectedDriver == driver) {
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (driver.approvalStatus == DriverApprovalStatus.APPROVED) {
                                    OutlinedButton(
                                        onClick = { showSuspendDialog = true },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = MaterialTheme.colorScheme.error
                                        )
                                    ) {
                                        Icon(Icons.Default.Block, null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Suspend")
                                    }
                                } else if (driver.approvalStatus == DriverApprovalStatus.SUSPENDED) {
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                repository.activateDriver(driver.id)
                                                drivers = repository.driversList
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Activate")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Suspend Dialog
    if (showSuspendDialog && selectedDriver != null) {
        AlertDialog(
            onDismissRequest = { showSuspendDialog = false },
            title = { Text("Suspend Driver") },
            text = {
                Column {
                    Text("Reason for suspension:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = suspendReason,
                        onValueChange = { suspendReason = it },
                        label = { Text("Reason") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            repository.suspendDriver(selectedDriver!!.id, "admin1", suspendReason)
                            drivers = repository.driversList
                            showSuspendDialog = false
                            suspendReason = ""
                        }
                    },
                    enabled = suspendReason.isNotBlank()
                ) {
                    Text("Suspend")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSuspendDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun DriverDetailsSheet(
    registration: DriverRegistrationEntity,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            "Driver Details",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        SectionTitle("Personal Information")
        DetailItem("Name", registration.fullName)
        DetailItem("Email", registration.email)
        DetailItem("Phone", registration.phone)
        DetailItem("DOB", registration.dateOfBirth)
        DetailItem("Gender", registration.gender)
        DetailItem("Address", "${registration.address}, ${registration.city}, ${registration.state} - ${registration.pincode}")
        
        SectionTitle("Emergency Contact")
        DetailItem("Name", registration.emergencyContactName)
        DetailItem("Phone", registration.emergencyContactPhone)
        DetailItem("Relation", registration.emergencyContactRelation)
        
        SectionTitle("Documents")
        DetailItem("Aadhaar", registration.aadhaarNumber)
        DetailItem("PAN", registration.panCardNumber)
        DetailItem("License", "${registration.drivingLicenseNumber} (Exp: ${registration.licenseExpiryDate})")
        
        SectionTitle("Vehicle Information")
        DetailItem("Category", registration.vehicleCategory)
        DetailItem("Brand/Model", "${registration.vehicleBrand} ${registration.vehicleModel}")
        DetailItem("Year", registration.vehicleYear)
        DetailItem("Number", registration.vehicleNumber)
        DetailItem("Color", registration.vehicleColor)
        
        SectionTitle("Bank Details")
        DetailItem("Bank", registration.bankName)
        DetailItem("Account Holder", registration.accountHolderName)
        DetailItem("Account", "****${registration.accountNumber.takeLast(4)}")
        DetailItem("IFSC", registration.ifscCode)
        DetailItem("UPI", registration.upiId)
        
        // Document thumbnails
        SectionTitle("Uploaded Documents")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DocumentThumbnail("Aadhaar Front", registration.aadhaarFrontUrl)
            DocumentThumbnail("Aadhaar Back", registration.aadhaarBackUrl)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DocumentThumbnail("PAN", registration.panCardUrl)
            DocumentThumbnail("License", registration.licenseFrontUrl)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DocumentThumbnail("Profile", registration.profilePhotoUrl)
            DocumentThumbnail("Selfie", registration.selfieUrl)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onClose,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Close")
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        title,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
    )
}

@Composable
fun DetailItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value.ifBlank { "-" }, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun DocumentThumbnail(label: String, url: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        Card(
            modifier = Modifier.size(80.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (url.isNotBlank()) {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Icon(
                        Icons.Default.BrokenImage,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
        Text(
            label,
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 4.dp)
        )
        Icon(
            if (url.isNotBlank()) Icons.Default.CheckCircle else Icons.Default.Cancel,
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = if (url.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )
    }
}
