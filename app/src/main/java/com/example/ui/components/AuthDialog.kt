package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.data.model.VehicleCategory
import com.example.data.remote.SupabaseService
import com.example.ui.theme.VTTBlueContainer
import com.example.ui.theme.VTTBlueDark
import com.example.ui.theme.VTTBluePrimary
import com.example.ui.theme.VTTSuccess
import com.example.ui.viewmodel.VttCabViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthDialog(
    viewModel: VttCabViewModel,
    onDismiss: () -> Unit,
    initialRole: UserRole = UserRole.CUSTOMER
) {
    var selectedRoleTab by remember {
        mutableStateOf(
            when (initialRole) {
                UserRole.DRIVER -> 1
                UserRole.ADMIN -> 2
                else -> 0
            }
        )
    } // 0: Customer, 1: Driver, 2: Admin
    var isSignUp by remember { mutableStateOf(false) }
    var showForgotPasswordModal by remember { mutableStateOf(false) }
    var isOtpMode by remember { mutableStateOf(false) }
    var otpInput by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }

    // Customer / Common Fields
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Driver Specific Registration Fields
    var address by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var emergencyContact by remember { mutableStateOf("") }

    // Vehicle Details
    var selectedVehicleCategory by remember { mutableStateOf(VehicleCategory.SEDAN) }
    var vehicleBrand by remember { mutableStateOf("") }
    var vehicleModel by remember { mutableStateOf("") }
    var vehicleYear by remember { mutableStateOf("") }
    var vehicleNumber by remember { mutableStateOf("") }
    var vehicleColor by remember { mutableStateOf("") }
    var seatingCapacity by remember { mutableStateOf("4") }

    // Document & ID Numbers
    var aadhaarNumber by remember { mutableStateOf("") }
    var panCardNumber by remember { mutableStateOf("") }
    var licenceNumber by remember { mutableStateOf("") }

    // Bank Details
    var bankName by remember { mutableStateOf("") }
    var accountNumber by remember { mutableStateOf("") }
    var ifscCode by remember { mutableStateOf("") }

    // Mandatory Document & Photo Upload States
    var aadhaarFrontFileName by remember { mutableStateOf<String?>(null) }
    var aadhaarBackFileName by remember { mutableStateOf<String?>(null) }
    var panCardFileName by remember { mutableStateOf<String?>(null) }
    var dlFrontFileName by remember { mutableStateOf<String?>(null) }
    var dlBackFileName by remember { mutableStateOf<String?>(null) }
    var rcFileName by remember { mutableStateOf<String?>(null) }
    var insuranceFileName by remember { mutableStateOf<String?>(null) }
    var pucFileName by remember { mutableStateOf<String?>(null) }
    var permitFileName by remember { mutableStateOf<String?>(null) }
    var photoFileName by remember { mutableStateOf<String?>(null) }
    var vehFrontFileName by remember { mutableStateOf<String?>(null) }
    var vehBackFileName by remember { mutableStateOf<String?>(null) }
    var vehLeftFileName by remember { mutableStateOf<String?>(null) }
    var vehRightFileName by remember { mutableStateOf<String?>(null) }
    var vehInteriorFileName by remember { mutableStateOf<String?>(null) }
    var vehConditionFileName by remember { mutableStateOf<String?>(null) }

    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    val supabaseService = remember { SupabaseService() }
    val scrollState = rememberScrollState()

    if (showForgotPasswordModal) {
        ForgotPasswordDialog(
            viewModel = viewModel,
            onDismiss = { showForgotPasswordModal = false }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = when {
                            isSignUp && selectedRoleTab == 1 -> "Driver Application Sign Up"
                            isSignUp -> "Customer Account Sign Up"
                            selectedRoleTab == 1 -> "Driver Portal Login"
                            selectedRoleTab == 2 -> "Admin Operations Login"
                            else -> "Customer Login"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (supabaseService.isConfigured) VTTSuccess.copy(alpha = 0.15f) else Color(0xFFFEF3C7)
                    ) {
                        Text(
                            text = if (supabaseService.isConfigured) "Supabase Auth" else "VTT Secure Auth",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (supabaseService.isConfigured) VTTSuccess else Color(0xFFD97706),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp)
                    .verticalScroll(scrollState)
            ) {
                // Role Tabs (Mobile App: Customer & Driver Partner Only)
                TabRow(
                    selectedTabIndex = if (selectedRoleTab > 1) 0 else selectedRoleTab,
                    containerColor = Color.Transparent,
                    contentColor = VTTBluePrimary
                ) {
                    Tab(
                        selected = selectedRoleTab == 0,
                        onClick = { selectedRoleTab = 0 },
                        text = { Text("Customer", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedRoleTab == 1,
                        onClick = { selectedRoleTab = 1 },
                        text = { Text("Driver Partner", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // --- CUSTOMER SIGNUP / LOGIN ---
                if (selectedRoleTab == 0) {
                    if (isSignUp) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Full Name *") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_name_input")
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Mobile Number (+91) *") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (!isSignUp && isOtpMode) {
                        OutlinedTextField(
                            value = phone.ifBlank { email },
                            onValueChange = { phone = it; email = it },
                            label = { Text("Mobile Number (+91) *") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        if (isOtpSent) {
                            OutlinedTextField(
                                value = otpInput,
                                onValueChange = { otpInput = it },
                                label = { Text("Enter 6-Digit Verification OTP *") },
                                placeholder = { Text("482109") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        } else {
                            Button(
                                onClick = {
                                    val target = if (phone.isNotBlank()) phone else if (email.isNotBlank()) email else "+91 9876543210"
                                    isOtpSent = true
                                    otpInput = "482109"
                                    viewModel.showToast("OTP sent to $target. Demo OTP: 482109")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = VTTBluePrimary),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Send Verification OTP")
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        TextButton(
                            onClick = { isOtpMode = false },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Switch to Password Login", fontSize = 12.sp, color = VTTBluePrimary)
                        }
                    } else {
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text(if (isSignUp) "Email Address *" else "Email or Phone Number *") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_email_input")
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password *") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_password_input")
                        )

                        if (!isSignUp) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(onClick = { isOtpMode = true; isOtpSent = false }) {
                                    Text("🔑 Login with OTP", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = VTTBluePrimary)
                                }
                                TextButton(onClick = { showForgotPasswordModal = true }) {
                                    Text("Forgot Password?", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = VTTBluePrimary)
                                }
                            }
                        }
                    }
                }

                // --- DRIVER SIGNUP / LOGIN ---
                if (selectedRoleTab == 1) {
                    if (isSignUp) {
                        // Driver Full Sign Up Form
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = VTTBluePrimary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Your documents are under verification. Please wait for VTT CABS Admin approval.",
                                    fontSize = 11.sp,
                                    color = VTTBlueDark,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Section 1: Personal Details
                        Text("1. Personal Details", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = VTTBlueDark)
                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Full Name (as per Licence) *") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Mobile Number (+91) *") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Address *") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Create Password *") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("Home Address *") },
                            leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = dob,
                                onValueChange = { dob = it },
                                label = { Text("Date of Birth (YYYY-MM-DD)") },
                                placeholder = { Text("1995-08-20") },
                                leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = emergencyContact,
                                onValueChange = { emergencyContact = it },
                                label = { Text("Emergency Contact") },
                                placeholder = { Text("+91 9876543211") },
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))

                        // Section 2: Vehicle Details
                        Text("2. Vehicle Specifications", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = VTTBlueDark)
                        Spacer(modifier = Modifier.height(6.dp))

                        ExposedDropdownMenuBox(
                            expanded = categoryDropdownExpanded,
                            onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedVehicleCategory.displayName,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Vehicle Type / Category *") },
                                leadingIcon = { Icon(Icons.Default.DirectionsCar, contentDescription = null) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = categoryDropdownExpanded,
                                onDismissRequest = { categoryDropdownExpanded = false }
                            ) {
                                VehicleCategory.values().forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text("${cat.displayName} (${cat.capacity} Seater)") },
                                        onClick = {
                                            selectedVehicleCategory = cat
                                            categoryDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = vehicleBrand,
                                onValueChange = { vehicleBrand = it },
                                label = { Text("Vehicle Brand *") },
                                placeholder = { Text("Toyota / Honda") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = vehicleModel,
                                onValueChange = { vehicleModel = it },
                                label = { Text("Vehicle Model *") },
                                placeholder = { Text("Innova Crysta") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = vehicleYear,
                                onValueChange = { vehicleYear = it },
                                label = { Text("Manufacturing Year") },
                                placeholder = { Text("2023") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = vehicleColor,
                                onValueChange = { vehicleColor = it },
                                label = { Text("Vehicle Color") },
                                placeholder = { Text("White / Silver") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = vehicleNumber,
                                onValueChange = { vehicleNumber = it },
                                label = { Text("Vehicle Number Plate *") },
                                placeholder = { Text("KA-01-MJ-4821") },
                                singleLine = true,
                                modifier = Modifier.weight(1.2f)
                            )
                            OutlinedTextField(
                                value = seatingCapacity,
                                onValueChange = { seatingCapacity = it },
                                label = { Text("Seating Capacity") },
                                singleLine = true,
                                modifier = Modifier.weight(0.8f)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))

                        // Section 3: Identity & Licensing Documents
                        Text("3. Identity & Licensing Documents", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = VTTBlueDark)
                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = aadhaarNumber,
                            onValueChange = { aadhaarNumber = it },
                            label = { Text("Aadhaar Number (12 Digits) *") },
                            leadingIcon = { Icon(Icons.Default.CreditCard, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                DocumentUploadTile(
                                    title = "Aadhaar Front",
                                    fileName = aadhaarFrontFileName,
                                    onUpload = { aadhaarFrontFileName = "Aadhaar_Front.jpg" }
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                DocumentUploadTile(
                                    title = "Aadhaar Back",
                                    fileName = aadhaarBackFileName,
                                    onUpload = { aadhaarBackFileName = "Aadhaar_Back.jpg" }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = panCardNumber,
                            onValueChange = { panCardNumber = it },
                            label = { Text("PAN Card Number *") },
                            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        DocumentUploadTile(
                            title = "PAN Card Photo / PDF",
                            fileName = panCardFileName,
                            onUpload = { panCardFileName = "PAN_Card.pdf" }
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = licenceNumber,
                            onValueChange = { licenceNumber = it },
                            label = { Text("Driving Licence Number *") },
                            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                DocumentUploadTile(
                                    title = "DL Front Side",
                                    fileName = dlFrontFileName,
                                    onUpload = { dlFrontFileName = "DL_Front.jpg" }
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                DocumentUploadTile(
                                    title = "DL Back Side",
                                    fileName = dlBackFileName,
                                    onUpload = { dlBackFileName = "DL_Back.jpg" }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))

                        // Section 4: Vehicle & Inspection Uploads
                        Text("4. Vehicle Papers & Inspection Photos", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = VTTBlueDark)
                        Spacer(modifier = Modifier.height(6.dp))

                        DocumentUploadTile(
                            title = "Vehicle RC (Registration Certificate)",
                            fileName = rcFileName,
                            onUpload = { rcFileName = "Vehicle_RC.pdf" }
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        DocumentUploadTile(
                            title = "Vehicle Insurance Policy",
                            fileName = insuranceFileName,
                            onUpload = { insuranceFileName = "Vehicle_Insurance.pdf" }
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        DocumentUploadTile(
                            title = "Pollution Certificate (PUC)",
                            fileName = pucFileName,
                            onUpload = { pucFileName = "PUC_Certificate.pdf" }
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        DocumentUploadTile(
                            title = "Commercial Vehicle Permit (if applicable)",
                            fileName = permitFileName,
                            onUpload = { permitFileName = "Commercial_Permit.pdf" }
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Photos (Profile & Vehicle 360 Angle)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.DarkGray)
                        Spacer(modifier = Modifier.height(4.dp))

                        DocumentUploadTile(
                            title = "Driver Profile Photo (Clear Facing)",
                            fileName = photoFileName,
                            onUpload = { photoFileName = "Driver_Profile.jpg" }
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                DocumentUploadTile(
                                    title = "Vehicle Front Photo",
                                    fileName = vehFrontFileName,
                                    onUpload = { vehFrontFileName = "Veh_Front.jpg" }
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                DocumentUploadTile(
                                    title = "Vehicle Back Photo",
                                    fileName = vehBackFileName,
                                    onUpload = { vehBackFileName = "Veh_Back.jpg" }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                DocumentUploadTile(
                                    title = "Vehicle Left Side",
                                    fileName = vehLeftFileName,
                                    onUpload = { vehLeftFileName = "Veh_Left.jpg" }
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                DocumentUploadTile(
                                    title = "Vehicle Right Side",
                                    fileName = vehRightFileName,
                                    onUpload = { vehRightFileName = "Veh_Right.jpg" }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))

                        DocumentUploadTile(
                            title = "Vehicle Interior Photo (Dashboard & Seats)",
                            fileName = vehInteriorFileName,
                            onUpload = { vehInteriorFileName = "Veh_Interior.jpg" }
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        DocumentUploadTile(
                            title = "Vehicle Condition Photos (Min 4 Clear Angles)",
                            fileName = vehConditionFileName,
                            onUpload = { vehConditionFileName = "4_Condition_Photos.zip" }
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        // Section 5: Bank Account & Payout Details
                        Text("5. Bank Account & Weekly Payouts", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = VTTBlueDark)
                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = bankName,
                            onValueChange = { bankName = it },
                            label = { Text("Bank Name *") },
                            placeholder = { Text("State Bank of India / HDFC Bank") },
                            leadingIcon = { Icon(Icons.Default.CreditCard, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = accountNumber,
                                onValueChange = { accountNumber = it },
                                label = { Text("Account Number *") },
                                placeholder = { Text("30987123456") },
                                singleLine = true,
                                modifier = Modifier.weight(1.2f)
                            )
                            OutlinedTextField(
                                value = ifscCode,
                                onValueChange = { ifscCode = it },
                                label = { Text("IFSC Code *") },
                                placeholder = { Text("SBIN0001234") },
                                singleLine = true,
                                modifier = Modifier.weight(0.8f)
                            )
                        }
                    } else {
                        // Driver Login Form
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Registered Email or Mobile Number") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Driver Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showForgotPasswordModal = true }) {
                                Text("Forgot Password?", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = VTTBluePrimary)
                            }
                        }
                    }
                }

                // --- ADMIN LOGIN ---
                if (selectedRoleTab == 2) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF3E8FF),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Secure Admin Operations Panel\nPublic registration disabled. Authorized credentials required.",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF5B21B6)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Admin Email (admin@vtt.com)") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Admin Passcode") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedRoleTab == 1 && isSignUp) {
                        // Driver Registration
                        if (name.isBlank() || phone.isBlank() || email.isBlank()) {
                            viewModel.showToast("Please complete all mandatory driver fields.")
                            return@Button
                        }
                        viewModel.registerDriver(
                            name = name,
                            phone = phone,
                            email = email,
                            pass = password,
                            address = address.ifBlank { "Bangalore" },
                            dob = dob.ifBlank { "1995-08-20" },
                            emergencyContact = emergencyContact.ifBlank { "+91 9876543211" },
                            vehicleCategory = selectedVehicleCategory,
                            vehicleBrand = vehicleBrand.ifBlank { "Toyota" },
                            vehicleModel = vehicleModel.ifBlank { "Innova Crysta" },
                            vehicleYear = vehicleYear.ifBlank { "2023" },
                            vehicleNumber = vehicleNumber.ifBlank { "KA-01-MJ-9999" },
                            vehicleColor = vehicleColor.ifBlank { "White" },
                            seatingCapacity = seatingCapacity.toIntOrNull() ?: 4,
                            aadhaarNumber = aadhaarNumber.ifBlank { "9876-5432-1098" },
                            aadhaarFrontUrl = aadhaarFrontFileName ?: "Aadhaar_Front.jpg",
                            aadhaarBackUrl = aadhaarBackFileName ?: "Aadhaar_Back.jpg",
                            panCardNumber = panCardNumber.ifBlank { "ABCDE1234F" },
                            panCardUrl = panCardFileName ?: "PAN_Card.pdf",
                            licenceNumber = licenceNumber.ifBlank { "DL-042023009182" },
                            licenceFrontUrl = dlFrontFileName ?: "DL_Front.jpg",
                            licenceBackUrl = dlBackFileName ?: "DL_Back.jpg",
                            rcDocUrl = rcFileName ?: "Vehicle_RC.pdf",
                            insuranceUrl = insuranceFileName ?: "Vehicle_Insurance.pdf",
                            pucUrl = pucFileName ?: "PUC_Certificate.pdf",
                            permitUrl = permitFileName ?: "Commercial_Permit.pdf",
                            profilePhotoUrl = photoFileName ?: "Driver_Profile.jpg",
                            vehicleFrontPhotoUrl = vehFrontFileName ?: "Veh_Front.jpg",
                            vehicleBackPhotoUrl = vehBackFileName ?: "Veh_Back.jpg",
                            vehicleLeftSidePhotoUrl = vehLeftFileName ?: "Veh_Left.jpg",
                            vehicleRightSidePhotoUrl = vehRightFileName ?: "Veh_Right.jpg",
                            vehicleInteriorPhotoUrl = vehInteriorFileName ?: "Veh_Interior.jpg",
                            vehicleConditionPhotosUrl = vehConditionFileName ?: "4_Condition_Photos.zip",
                            bankName = bankName.ifBlank { "State Bank of India" },
                            accountNumber = accountNumber.ifBlank { "30987123456" },
                            ifscCode = ifscCode.ifBlank { "SBIN0001234" }
                        )
                        onDismiss()
                    } else if (isSignUp) {
                        // Customer Registration
                        viewModel.registerUser(
                            name = name.ifBlank { "Customer" },
                            email = email.ifBlank { "customer@vtt.com" },
                            phone = phone.ifBlank { "+91 9876543210" },
                            pass = password,
                            role = UserRole.CUSTOMER
                        )
                        onDismiss()
                    } else if (isOtpMode && selectedRoleTab == 0) {
                        if (!isOtpSent) {
                            val target = if (phone.isNotBlank()) phone else if (email.isNotBlank()) email else "+91 9876543210"
                            isOtpSent = true
                            otpInput = "482109"
                            viewModel.showToast("OTP sent to $target. Demo OTP: 482109")
                        } else {
                            viewModel.loginUser(
                                identifier = phone.ifBlank { email.ifBlank { "user@vtt.com" } },
                                pass = "123456",
                                role = UserRole.CUSTOMER,
                                onResult = { success, _ ->
                                    if (success) onDismiss()
                                }
                            )
                        }
                    } else {
                        // Login
                        val role = when (selectedRoleTab) {
                            1 -> UserRole.DRIVER
                            2 -> UserRole.ADMIN
                            else -> UserRole.CUSTOMER
                        }
                        viewModel.loginUser(
                            identifier = email.ifBlank {
                                when (role) {
                                    UserRole.DRIVER -> "driver1@vtt.com"
                                    UserRole.ADMIN -> "admin@vtt.com"
                                    else -> "user@vtt.com"
                                }
                            },
                            pass = password,
                            role = role,
                            onResult = { success, _ ->
                                if (success) onDismiss()
                            }
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = VTTBluePrimary)
            ) {
                Text(
                    text = when {
                        isSignUp && selectedRoleTab == 1 -> "Submit Driver Application"
                        isSignUp -> "Create Account"
                        isOtpMode && !isOtpSent -> "Send OTP"
                        isOtpMode -> "Verify OTP & Login"
                        else -> "Login"
                    }
                )
            }
        },
        dismissButton = {
            if (selectedRoleTab != 2) {
                TextButton(onClick = { isSignUp = !isSignUp }) {
                    Text(if (isSignUp) "Switch to Login" else "Create Account")
                }
            }
        }
    )
}

@Composable
fun DocumentUploadTile(
    title: String,
    fileName: String?,
    onUpload: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (fileName != null) VTTSuccess.copy(alpha = 0.12f) else Color(0xFFF3F4F6),
        modifier = Modifier.fillMaxWidth().clickable { onUpload() }
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (fileName != null) Icons.Default.CheckCircle else Icons.Default.FileUpload,
                    contentDescription = null,
                    tint = if (fileName != null) VTTSuccess else Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.DarkGray)
                    Text(
                        text = fileName ?: "Tap to select file from device",
                        fontSize = 10.sp,
                        color = if (fileName != null) VTTSuccess else Color.Gray,
                        fontWeight = if (fileName != null) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
            Text(
                text = if (fileName != null) "Uploaded ✓" else "Upload",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (fileName != null) VTTSuccess else VTTBluePrimary
            )
        }
    }
}

@Composable
fun ForgotPasswordDialog(
    viewModel: VttCabViewModel,
    onDismiss: () -> Unit
) {
    var step by remember { mutableStateOf(1) } // 1: Input Email/Phone, 2: OTP & New Pass
    var emailOrPhone by remember { mutableStateOf("") }
    var otpInput by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (step == 1) "Forgot Password" else "Enter OTP Verification",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            Column {
                if (step == 1) {
                    Text("Enter your registered Email or Mobile Number to receive a 6-digit OTP reset code:", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = emailOrPhone,
                        onValueChange = { emailOrPhone = it },
                        label = { Text("Email or Mobile Number") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                    ) {
                        Text(
                            "OTP sent to $emailOrPhone. (Demo OTP: 123456)",
                            fontSize = 11.sp,
                            color = Color(0xFF92400E),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(8.dp)
                        )
                    }

                    OutlinedTextField(
                        value = otpInput,
                        onValueChange = { otpInput = it },
                        label = { Text("6-Digit OTP Code") },
                        leadingIcon = { Icon(Icons.Default.Security, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("New Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (step == 1) {
                        if (emailOrPhone.isBlank()) {
                            viewModel.showToast("Please enter email or phone.")
                            return@Button
                        }
                        step = 2
                        viewModel.showToast("OTP code sent to $emailOrPhone")
                    } else {
                        if (otpInput.isBlank() || newPassword.isBlank()) {
                            viewModel.showToast("Please enter OTP and new password.")
                            return@Button
                        }
                        viewModel.resetPasswordWithOtp(emailOrPhone, newPassword)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = VTTBluePrimary)
            ) {
                Text(if (step == 1) "Send OTP Code" else "Reset Password")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
