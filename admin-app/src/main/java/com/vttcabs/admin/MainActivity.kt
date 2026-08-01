package com.vttcabs.admin

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vttcabs.admin.ui.screens.*
import com.vttcabs.admin.ui.theme.VTTAdminTheme
import com.vttcabs.admin.ui.theme.VTTBlueDark
import com.vttcabs.admin.ui.viewmodel.AdminViewModel

class MainActivity : ComponentActivity() {
    
    companion object {
        private const val TAG = "VTTAdminApp"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "MainActivity onCreate")
        
        setContent {
            VTTAdminTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    VttAdminApp()
                }
            }
        }
    }
}

@Composable
fun VttAdminApp(
    viewModel: AdminViewModel = viewModel()
) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val currentScreen by viewModel.currentScreen.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val initError by viewModel.initError.collectAsState()
    
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Handle toast messages
    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Log.d("VTTAdminApp", "Toast: $it")
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }
    
    // Show loading screen during initialization
    if (isLoading && !isLoggedIn) {
        LoadingScreen()
    } 
    // Show error screen if initialization failed
    else if (initError != null) {
        ErrorScreen(error = initError!!, onRetry = { viewModel.retryInit() })
    }
    // Show login screen
    else if (!isLoggedIn) {
        LoginScreen(viewModel = viewModel)
    } 
    // Show main admin screen
    else {
        MainAdminScreen(
            viewModel = viewModel,
            currentScreen = currentScreen,
            onNavigate = { viewModel.navigateTo(it) }
        )
    }
}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VTTBlueDark),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "VTT CABS Admin",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Loading...",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun ErrorScreen(
    error: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VTTBlueDark),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "⚠️",
                fontSize = 48.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Connection Error",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            androidx.compose.material3.Button(
                onClick = onRetry,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = Color.White
                )
            ) {
                Text("Retry", color = VTTBlueDark)
            }
        }
    }
}

@Composable
fun MainAdminScreen(
    viewModel: AdminViewModel,
    currentScreen: String,
    onNavigate: (String) -> Unit
) {
    when (currentScreen) {
        "dashboard" -> DashboardScreen(viewModel = viewModel, onNavigate = onNavigate)
        "drivers" -> DriversScreen(viewModel = viewModel, onNavigate = onNavigate)
        "bookings" -> BookingsScreen(viewModel = viewModel, onNavigate = onNavigate)
        "customers" -> CustomersScreen(viewModel = viewModel, onNavigate = onNavigate)
        "vehicles" -> VehiclesScreen(viewModel = viewModel, onNavigate = onNavigate)
        "tracking" -> LiveTrackingScreen(viewModel = viewModel, onNavigate = onNavigate)
        "reports" -> ReportsScreen(viewModel = viewModel, onNavigate = onNavigate)
        else -> DashboardScreen(viewModel = viewModel, onNavigate = onNavigate)
    }
}
