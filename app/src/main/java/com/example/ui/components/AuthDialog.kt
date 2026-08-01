package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.ui.theme.VTTBlueDark
import com.example.ui.theme.VTTBluePrimary
import com.example.ui.viewmodel.VttCabViewModel

@Composable
fun AuthDialog(
    viewModel: VttCabViewModel,
    onDismiss: () -> Unit,
    initialRole: UserRole = UserRole.CUSTOMER
) {
    var selectedRoleTab by remember { mutableStateOf(0) } // 0: Customer Login, 1: Customer Signup, 2: Driver, 3: Hidden Admin
    var showPassword by remember { mutableStateOf(false) }
    
    // Login Fields
    var loginEmail by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    
    // Signup Fields
    var signupName by remember { mutableStateOf("") }
    var signupEmail by remember { mutableStateOf("") }
    var signupPhone by remember { mutableStateOf("") }
    var signupPassword by remember { mutableStateOf("") }
    var signupConfirmPassword by remember { mutableStateOf("") }
    
    // Admin Fields
    var adminPhone by remember { mutableStateOf("") }
    var adminPassword by remember { mutableStateOf("") }
    
    val scrollState = rememberScrollState()

    // Show Customer tab by default, but allow access to admin via hidden method
    if (initialRole == UserRole.CUSTOMER) {
        selectedRoleTab = 0
    } else if (initialRole == UserRole.DRIVER) {
        selectedRoleTab = 2
    } else if (initialRole == UserRole.ADMIN) {
        selectedRoleTab = 3
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = when (selectedRoleTab) {
                        0 -> "Customer Login"
                        1 -> "Customer Sign Up"
                        2 -> "Driver Portal"
                        3 -> "Admin Login"
                        else -> "VTT CABS"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (selectedRoleTab == 1) 480.dp else 380.dp)
                    .verticalScroll(scrollState)
            ) {
                // Tab Row for Customer only (hide admin and driver from public view)
                if (selectedRoleTab < 2) {
                    TabRow(
                        selectedTabIndex = selectedRoleTab,
                        containerColor = Color.Transparent,
                        contentColor = VTTBluePrimary
                    ) {
                        Tab(
                            selected = selectedRoleTab == 0,
                            onClick = { selectedRoleTab = 0 },
                            text = { Text("Login", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = selectedRoleTab == 1,
                            onClick = { selectedRoleTab = 1 },
                            text = { Text("Sign Up", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // --- CUSTOMER LOGIN ---
                if (selectedRoleTab == 0) {
                    OutlinedTextField(
                        value = loginEmail,
                        onValueChange = { loginEmail = it },
                        label = { Text("Email or Mobile Number") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = loginPassword,
                        onValueChange = { loginPassword = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (showPassword) "Hide password" else "Show password"
                                )
                            }
                        },
                        visualTransformation = if (showPassword) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = { selectedRoleTab = 3 }, // Hidden admin access
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Admin?", fontSize = 11.sp, color = Color.Gray)
                    }
                }

                // --- CUSTOMER SIGNUP ---
                if (selectedRoleTab == 1) {
                    OutlinedTextField(
                        value = signupName,
                        onValueChange = { signupName = it },
                        label = { Text("Full Name *") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = signupEmail,
                        onValueChange = { signupEmail = it },
                        label = { Text("Email Address *") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = signupPhone,
                        onValueChange = { signupPhone = it },
                        label = { Text("Mobile Number *") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = signupPassword,
                        onValueChange = { signupPassword = it },
                        label = { Text("Password *") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null
                                )
                            }
                        },
                        visualTransformation = if (showPassword) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = signupConfirmPassword,
                        onValueChange = { signupConfirmPassword = it },
                        label = { Text("Confirm Password *") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        visualTransformation = if (showPassword) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // --- DRIVER PORTAL (Login Only, No Registration) ---
                if (selectedRoleTab == 2) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Driver registration is currently closed. To become a driver partner, please contact VTT CABS directly.",
                                fontSize = 12.sp,
                                color = Color(0xFF92400E)
                            )
                        }
                    }
                    
                    OutlinedTextField(
                        value = loginEmail,
                        onValueChange = { loginEmail = it },
                        label = { Text("Email or Mobile Number") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = loginPassword,
                        onValueChange = { loginPassword = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // --- HIDDEN ADMIN LOGIN ---
                if (selectedRoleTab == 3) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = VTTBlueDark),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Admin Access", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Authorized personnel only", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                        }
                    }
                    
                    OutlinedTextField(
                        value = adminPhone,
                        onValueChange = { adminPhone = it },
                        label = { Text("Admin Mobile") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = adminPassword,
                        onValueChange = { adminPassword = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = { selectedRoleTab = 0 },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Back to Customer Login", fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when (selectedRoleTab) {
                        0 -> { // Customer Login
                            viewModel.loginUser(
                                identifier = loginEmail,
                                pass = loginPassword,
                                role = UserRole.CUSTOMER,
                                onResult = { success, _ ->
                                    if (success) onDismiss()
                                }
                            )
                        }
                        1 -> { // Customer Signup
                            if (signupPassword != signupConfirmPassword) {
                                viewModel.showToast("Passwords do not match!")
                                return@Button
                            }
                            if (signupPassword.length < 6) {
                                viewModel.showToast("Password must be at least 6 characters")
                                return@Button
                            }
                            viewModel.signUpCustomer(
                                name = signupName,
                                email = signupEmail,
                                phone = signupPhone,
                                password = signupPassword,
                                onResult = { success, _ ->
                                    if (success) onDismiss()
                                }
                            )
                        }
                        2 -> { // Driver Login
                            viewModel.loginUser(
                                identifier = loginEmail,
                                pass = loginPassword,
                                role = UserRole.DRIVER,
                                onResult = { success, _ ->
                                    if (success) onDismiss()
                                }
                            )
                        }
                        3 -> { // Admin Login
                            viewModel.loginUser(
                                identifier = adminPhone,
                                pass = adminPassword,
                                role = UserRole.ADMIN,
                                onResult = { success, _ ->
                                    if (success) onDismiss()
                                }
                            )
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = VTTBluePrimary)
            ) {
                Text(
                    text = when (selectedRoleTab) {
                        0 -> "Login"
                        1 -> "Create Account"
                        2 -> "Driver Login"
                        3 -> "Admin Login"
                        else -> "Submit"
                    }
                )
            }
        },
        dismissButton = {
            if (selectedRoleTab == 0) {
                TextButton(onClick = { selectedRoleTab = 1 }) {
                    Text("Create Account")
                }
            }
        }
    )
}
