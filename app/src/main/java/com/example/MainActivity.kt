package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalTaxi
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.ui.components.AuthDialog
import com.example.ui.components.VttHeader
import com.example.ui.screens.admin.AdminWebPanelScreen
import com.example.ui.screens.customer.CustomerBookingScreen
import com.example.ui.screens.customer.CustomerHistoryScreen
import com.example.ui.screens.customer.CustomerPaymentsScreen
import com.example.ui.screens.customer.CustomerProfileScreen
import com.example.ui.screens.customer.CustomerSupportScreen
import com.example.ui.screens.driver.DriverDashboardScreen
import com.example.ui.screens.notifications.NotificationScreen
import com.example.ui.theme.VTTBlueDark
import com.example.ui.theme.VTTBluePrimary
import com.example.ui.theme.VTTCabsTheme
import com.example.ui.viewmodel.VttCabViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: VttCabViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            VTTCabsTheme {
                VttCabsApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun VttCabsApp(viewModel: VttCabViewModel) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val currentRole by viewModel.currentRole.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val toastMsg by viewModel.toastMessage.collectAsState()
    val notifications by viewModel.notifications.collectAsState()

    var showAuthDialog by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var customerNavTab by remember { mutableStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(toastMsg) {
        toastMsg?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearToast()
        }
    }

    if (showAuthDialog) {
        AuthDialog(
            viewModel = viewModel,
            onDismiss = { showAuthDialog = false },
            initialRole = currentRole
        )
    }

    if (showProfileDialog) {
        com.example.ui.components.CustomerProfileDialog(
            viewModel = viewModel,
            onDismiss = { showProfileDialog = false }
        )
    }

    if (!isLoggedIn) {
        // App starts on Login Screen first (two buttons: Continue as Customer / Continue as Driver)
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                com.example.ui.screens.auth.LoginSelectionScreen(
                    viewModel = viewModel,
                    onSelectRole = { role ->
                        viewModel.switchRole(role)
                        showAuthDialog = true
                    }
                )
            }
        }
    } else {
        // User authenticated -> open Customer or Driver Dashboard
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                if (currentRole == UserRole.CUSTOMER) {
                    VttHeader(
                        currentRole = currentRole,
                        currentUser = currentUser,
                        onRoleSelected = { role ->
                            viewModel.switchRole(role)
                        },
                        unreadNotificationCount = notifications.size,
                        onNotificationsClick = {
                            customerNavTab = 1
                        },
                        onAuthClick = {
                            showProfileDialog = true
                        }
                    )
                }
            },
            bottomBar = {
                if (currentRole == UserRole.CUSTOMER) {
                    NavigationBar(
                        containerColor = Color.White,
                        contentColor = VTTBluePrimary
                    ) {
                        NavigationBarItem(
                            selected = customerNavTab == 0,
                            onClick = { customerNavTab = 0 },
                            icon = { Icon(Icons.Default.LocalTaxi, contentDescription = "Book Ride") },
                            label = { Text("Book Ride", fontWeight = FontWeight.Bold, fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = VTTBluePrimary,
                                selectedTextColor = VTTBluePrimary,
                                indicatorColor = Color(0xFFEFF6FF)
                            )
                        )
                        NavigationBarItem(
                            selected = customerNavTab == 1,
                            onClick = { customerNavTab = 1 },
                            icon = { Icon(Icons.Default.History, contentDescription = "My Trips") },
                            label = { Text("My Trips", fontWeight = FontWeight.Bold, fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = VTTBluePrimary,
                                selectedTextColor = VTTBluePrimary,
                                indicatorColor = Color(0xFFEFF6FF)
                            )
                        )
                        NavigationBarItem(
                            selected = customerNavTab == 2,
                            onClick = { customerNavTab = 2 },
                            icon = { Icon(Icons.Default.Payment, contentDescription = "Payments") },
                            label = { Text("Payments", fontWeight = FontWeight.Bold, fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = VTTBluePrimary,
                                selectedTextColor = VTTBluePrimary,
                                indicatorColor = Color(0xFFEFF6FF)
                            )
                        )
                        NavigationBarItem(
                            selected = customerNavTab == 3,
                            onClick = { customerNavTab = 3 },
                            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                            label = { Text("Profile", fontWeight = FontWeight.Bold, fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = VTTBluePrimary,
                                selectedTextColor = VTTBluePrimary,
                                indicatorColor = Color(0xFFEFF6FF)
                            )
                        )
                        NavigationBarItem(
                            selected = customerNavTab == 4,
                            onClick = { customerNavTab = 4 },
                            icon = { Icon(Icons.Default.SupportAgent, contentDescription = "Support") },
                            label = { Text("Support", fontWeight = FontWeight.Bold, fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = VTTBluePrimary,
                                selectedTextColor = VTTBluePrimary,
                                indicatorColor = Color(0xFFEFF6FF)
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentRole) {
                    UserRole.CUSTOMER -> {
                        when (customerNavTab) {
                            0 -> CustomerBookingScreen(viewModel = viewModel)
                            1 -> CustomerHistoryScreen(viewModel = viewModel)
                            2 -> CustomerPaymentsScreen(viewModel = viewModel)
                            3 -> CustomerProfileScreen(viewModel = viewModel)
                            4 -> CustomerSupportScreen(viewModel = viewModel)
                        }
                    }
                    UserRole.DRIVER -> {
                        DriverDashboardScreen(viewModel = viewModel)
                    }
                    UserRole.ADMIN -> {
                        com.example.ui.screens.auth.LoginSelectionScreen(
                            viewModel = viewModel,
                            onSelectRole = { role ->
                                viewModel.switchRole(role)
                                showAuthDialog = true
                            }
                        )
                    }
                }
            }
        }
    }
}
