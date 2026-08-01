package com.vttcabs.admin

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vttcabs.admin.ui.screens.*
import com.vttcabs.admin.ui.theme.VTTAdminTheme
import com.vttcabs.admin.ui.viewmodel.AdminViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
    
    val context = androidx.compose.ui.platform.LocalContext.current
    
    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }
    
    if (!isLoggedIn) {
        LoginScreen(viewModel = viewModel)
    } else {
        MainAdminScreen(
            viewModel = viewModel,
            currentScreen = currentScreen,
            onNavigate = { viewModel.navigateTo(it) }
        )
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
