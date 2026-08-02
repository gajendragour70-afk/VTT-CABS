package com.vttcabs.customer.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vttcabs.common.VttRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerOTPLoginScreen(
    repository: VttRepository,
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit,
    onBackClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }
    var otp by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var countdown by remember { mutableIntStateOf(0) }
    
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(countdown) {
        if (countdown > 0) {
            kotlinx.coroutines.delay(1000)
            countdown--
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isSignUp) "Create Account" else "Login") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isOtpSent) isOtpSent = false else if (isSignUp) isSignUp = false else onBackClick()
                    }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))
            Icon(Icons.Default.LocalTaxi, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Text("VTT CABS", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text(if (isSignUp) "Sign up to get started" else "Welcome back!", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(32.dp))

            if (!isOtpSent) {
                OutlinedTextField(value = email, onValueChange = { email = it; error = null }, label = { Text("Email Address") },
                    leadingIcon = { Icon(Icons.Default.Email, null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth(), singleLine = true)
                
                if (isSignUp) {
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(value = name, onValueChange = { name = it; error = null }, label = { Text("Full Name") },
                        leadingIcon = { Icon(Icons.Default.Person, null) },
                        modifier = Modifier.fillMaxWidth(), singleLine = true)
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(value = phone, onValueChange = { if (it.length <= 10) phone = it.filter { c -> c.isDigit() } },
                        label = { Text("Phone Number") },
                        leadingIcon = { Icon(Icons.Default.Phone, null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
                        modifier = Modifier.fillMaxWidth(), singleLine = true)
                }
                
                Spacer(Modifier.height(24.dp))
                if (error != null) { Text(error!!, color = MaterialTheme.colorScheme.error, fontSize = 14.sp); Spacer(Modifier.height(8.dp)) }
                
                Button(onClick = {
                    if (email.isBlank() || !email.contains("@")) { error = "Please enter a valid email"; return@Button }
                    if (isSignUp && (name.isBlank() || phone.length != 10)) { error = "Please fill all fields correctly"; return@Button }
                    isLoading = true
                    scope.launch {
                        try {
                            val result = repository.sendOtp(email)
                            if (result.isSuccess) { isOtpSent = true; countdown = 60 }
                            else error = "Failed to send OTP"
                        } catch (e: Exception) { error = e.message ?: "An error occurred" } finally { isLoading = false }
                    }
                }, modifier = Modifier.fillMaxWidth().height(50.dp), enabled = !isLoading && email.isNotBlank()) {
                    if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    else Text(if (isSignUp) "Continue to Sign Up" else "Continue")
                }
                
                Spacer(Modifier.height(16.dp))
                TextButton(onClick = { isSignUp = !isSignUp }) { Text(if (isSignUp) "Already have an account? Login" else "New customer? Sign Up") }
            } else {
                Text("Enter the 6-digit code sent to", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(email, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(24.dp))
                
                OutlinedTextField(value = otp, onValueChange = { if (it.length <= 6) otp = it; error = null },
                    label = { Text("OTP") }, leadingIcon = { Icon(Icons.Default.Lock, null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    modifier = Modifier.fillMaxWidth(), singleLine = true, placeholder = { Text("000000") })
                
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    if (countdown > 0) Text("Resend OTP in ${countdown}s", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    else TextButton(onClick = { scope.launch { repository.sendOtp(email); countdown = 60 } }) { Text("Resend OTP") }
                    TextButton(onClick = { isOtpSent = false }) { Text("Change Email") }
                }
                
                Spacer(Modifier.height(24.dp))
                if (error != null) { Text(error!!, color = MaterialTheme.colorScheme.error, fontSize = 14.sp); Spacer(Modifier.height(8.dp)) }
                
                Button(onClick = {
                    if (otp.length != 6) { error = "Please enter a valid 6-digit OTP"; return@Button }
                    isLoading = true
                    scope.launch {
                        try {
                            val result = repository.verifyOtp(email, otp)
                            if (result.isSuccess) {
                                val user = repository.getUserByEmail(email)
                                if (user != null || isSignUp) {
                                    if (isSignUp) repository.registerCustomer(name, email, phone, "")
                                    onLoginSuccess()
                                } else error = "Account not found. Please sign up first."
                            } else error = result.exceptionOrNull()?.message ?: "Invalid OTP"
                        } catch (e: Exception) { error = e.message ?: "Verification failed" } finally { isLoading = false }
                    }
                }, modifier = Modifier.fillMaxWidth().height(50.dp), enabled = !isLoading && otp.length == 6) {
                    if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    else Text(if (isSignUp) "Sign Up" else "Login")
                }
            }

            Spacer(Modifier.height(32.dp))
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Demo Mode", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Use OTP: 123456 for testing", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    Text("Test customer: test@vtt.com", fontSize = 12.sp)
                }
            }
        }
    }
}
